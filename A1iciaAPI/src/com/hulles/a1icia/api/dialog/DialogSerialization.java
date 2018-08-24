/*******************************************************************************
 * Copyright © 2017, 2018 Hulles Industries LLC
 * All rights reserved
 *  
 * This file is part of A1icia.
 *  
 * A1icia is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *    
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * SPDX-License-Identifer: GPL-3.0-or-later
 *******************************************************************************/
package com.hulles.a1icia.api.dialog;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hulles.a1icia.api.A1iciaConstants;
import com.hulles.a1icia.api.jebus.JebusApiHub;
import com.hulles.a1icia.api.remote.A1icianID;
import com.hulles.a1icia.api.shared.A1iciaAPIException;
import com.hulles.a1icia.api.shared.SharedUtils;

/**
 * This is how we send requests and responses back and forth between A1icia Central and
 * remote stations.
 * 
 * @author hulles
 *
 */
public class DialogSerialization {
	final static Logger LOGGER = Logger.getLogger("A1iciaApi.DialogSerialization");
	final static Level LOGLEVEL = A1iciaConstants.getA1iciaLogLevel();
	private static final int MAXHEADROOM = JebusApiHub.getMaxHardOutputBufferLimit();
	
	/**
	 * Deserialize a byte array for an A1ician. We deserialize at least the header,
	 * and if it's either a broadcast message or sent to the A1ician, we also 
	 * deserialize the body.
	 * 
	 * @param a1icianID The ID of the A1ician
	 * @param bytes The byte array containing the serialized objects
	 * @return A Dialog, or null if it was not sent to the A1ician or there was a problem deserializing
	 * the data
	 */
	public static Dialog deSerialize(A1icianID a1icianID, byte[] bytes) {
		ByteArrayInputStream byteInputStream;
		Object object;
		DialogHeader header;
		Dialog dialog = null;
		Dialog notOurDialog;
		A1icianID dialogA1icianID;
		A1icianID bcastID = A1iciaConstants.getBroadcastA1icianID();
		String debugString;
		boolean debugging;
		
		SharedUtils.checkNotNull(a1icianID);
		SharedUtils.checkNotNull(bytes);
		debugging = LOGGER.isLoggable(LOGLEVEL);
		LOGGER.log(LOGLEVEL, "DESERIALIZING");
		byteInputStream = new ByteArrayInputStream(bytes);
		try (ObjectInputStream objectInputStream = new ObjectInputStream(byteInputStream)) {
			object = objectInputStream.readObject();
			header = (DialogHeader) object;
			dialogA1icianID = header.getToA1icianID();
			if (dialogA1icianID.equals(bcastID) || dialogA1icianID.equals(a1icianID)) {
				object = objectInputStream.readObject();
				dialog = (Dialog) object;
				if (debugging) {
					if (dialog instanceof DialogRequest) {
						debugString = ((DialogRequest)dialog).toString();
					} else {
						debugString = ((DialogResponse)dialog).toString();
					}
					LOGGER.log(LOGLEVEL, debugString);
				}
			} else if (debugging) {
				object = objectInputStream.readObject();
				notOurDialog = (Dialog) object;
				debugString = "NOT OUR ALICIANID\n";
				if (notOurDialog instanceof DialogRequest) {
					debugString += ((DialogRequest)notOurDialog).toString();
				} else {
					debugString += ((DialogResponse)notOurDialog).toString();
				}
				LOGGER.log(LOGLEVEL, debugString);
			}
		} catch (InvalidClassException e) {
			throw new A1iciaAPIException("DialogSerialization:deSerialize: invalid class", e);
		} catch (ClassNotFoundException e) {
			throw new A1iciaAPIException("DialogSerialization:deSerialize: class not found", e);
		} catch (StreamCorruptedException e) {
			throw new A1iciaAPIException("DialogSerialization:deSerialize: stream corrupted", e);
		} catch (OptionalDataException e) {
			throw new A1iciaAPIException("DialogSerialization:deSerialize: optional data", e);
		} catch (IOException e) {
			throw new A1iciaAPIException("DialogSerialization:deSerialize: IO exception", e);
		}
		LOGGER.log(LOGLEVEL, "FINISHED DESERIALIZING");
		return dialog;
	}

	/**
	 * Given a DialogHeader and a Dialog, serialize them and return the resulting byte array.
	 * 
	 * @param header The DialogHeader
	 * @param dialog The Dialog
	 * @return The serialized objects as a byte array
	 */
	public static byte[] serialize(DialogHeader header, Dialog dialog) {
		ByteArrayOutputStream byteOutputStream;
		String debugString;
		DialogRequest request;
		DialogResponse response;
		
		LOGGER.log(LOGLEVEL, "IN SERIALIZE");
		SharedUtils.checkNotNull(header);
		SharedUtils.checkNotNull(dialog);
		if (dialog instanceof DialogRequest) {
			request = (DialogRequest) dialog;
			if (!request.isValid()) {
				System.err.println(request.toString());
				throw new A1iciaAPIException("DialogSerialization:serialize: invalid dialog request");
			}
			LOGGER.log(LOGLEVEL, "DEBUG DIALOGREQUEST");
			debugString = request.toString();
		} else {
			response = (DialogResponse) dialog;
			if (!response.isValid()) {
				System.err.println(response.toString());
				throw new A1iciaAPIException("DialogSerialization:serialize: invalid dialog response");
			}
			LOGGER.log(LOGLEVEL, "DEBUG DIALOGRESPONSE");
			debugString = response.toString();
		}
		LOGGER.log(LOGLEVEL, debugString);
		LOGGER.log(LOGLEVEL, "READY TO WRITE OBJECTS");
		byteOutputStream = new ByteArrayOutputStream();
		try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteOutputStream)) {
			objectOutputStream.writeObject(header);
			objectOutputStream.writeObject(dialog);
			objectOutputStream.flush();
			objectOutputStream.close();
		} catch (InvalidClassException e) {
			throw new A1iciaAPIException("DialogSerialization:serialize: invalid class", e);
		} catch (NotSerializableException e) {
			throw new A1iciaAPIException("DialogSerialization:serialize: not serializable", e);
		} catch (IOException e) {
			throw new A1iciaAPIException("DialogSerialization:serialize: IO exception");
		}
		LOGGER.log(LOGLEVEL, "WROTE OBJECTS");
		if (byteOutputStream.size() > MAXHEADROOM) {
			throw new A1iciaAPIException("DialogSerialization: serialized size exceeds Redis max");
		}
		return byteOutputStream.toByteArray();
	}
}
