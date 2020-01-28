/*******************************************************************************
 * Copyright © 2017, 2018 Hulles Industries LLC
 * All rights reserved
 *  
 * This file is part of Alixia.
 *  
 * Alixia is free software: you can redistribute it and/or modify
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
package com.hulles.alixia.api.dialog;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hulles.alixia.api.AlixiaConstants;
import com.hulles.alixia.api.jebus.JebusHub;
import com.hulles.alixia.api.remote.AlixianID;
import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.SharedUtils;

/**
 * This is how we send requests and responses back and forth between Alixia Central and
 * remote stations.
 * 
 * @author hulles
 *
 */
public class DialogSerialization {
	final static Logger LOGGER = LoggerFactory.getLogger(DialogSerialization.class);
	private static final int MAXHEADROOM = JebusHub.getMaxHardOutputBufferLimit();
	
	/**
	 * Deserialize a byte array for an Alixian. We deserialize at least the header,
	 * and if it's either a broadcast message or sent to the Alixian, we also 
	 * deserialize the body.
	 * 
	 * @param alixianID The ID of the Alixian
	 * @param bytes The byte array containing the serialized objects
	 * @return A Dialog, or null if it was not sent to the Alixian or there was a problem deserializing
	 * the data
	 */
	public static Dialog deSerialize(AlixianID alixianID, byte[] bytes) {
		ByteArrayInputStream byteInputStream;
		Object object;
		DialogHeader header;
		Dialog dialog = null;
		Dialog notOurDialog;
		AlixianID dialogAlixianID;
		AlixianID bcastID = AlixiaConstants.getBroadcastAlixianID();
		String debugString;
		boolean debugging;
		
		SharedUtils.checkNotNull(alixianID);
		SharedUtils.checkNotNull(bytes);
		debugging = LOGGER.isDebugEnabled();
		LOGGER.debug("DESERIALIZING");
		byteInputStream = new ByteArrayInputStream(bytes);
		try (ObjectInputStream objectInputStream = new ObjectInputStream(byteInputStream)) {
			object = objectInputStream.readObject();
			header = (DialogHeader) object;
			dialogAlixianID = header.getToAlixianID();
			if (dialogAlixianID.equals(bcastID) || dialogAlixianID.equals(alixianID)) {
				object = objectInputStream.readObject();
				dialog = (Dialog) object;
				if (debugging) {
					if (dialog instanceof DialogRequest) {
						debugString = ((DialogRequest)dialog).toString();
					} else {
						debugString = ((DialogResponse)dialog).toString();
					}
					LOGGER.debug(debugString);
				}
			} else if (debugging) {
				object = objectInputStream.readObject();
				notOurDialog = (Dialog) object;
				debugString = "NOT OUR ALIXIANID\n";
				if (notOurDialog instanceof DialogRequest) {
					debugString += ((DialogRequest)notOurDialog).toString();
				} else {
					debugString += ((DialogResponse)notOurDialog).toString();
				}
				LOGGER.debug(debugString);
			}
		} catch (InvalidClassException e) {
			throw new AlixiaException("DialogSerialization:deSerialize: invalid class", e);
		} catch (ClassNotFoundException e) {
			throw new AlixiaException("DialogSerialization:deSerialize: class not found", e);
		} catch (StreamCorruptedException e) {
			throw new AlixiaException("DialogSerialization:deSerialize: stream corrupted", e);
		} catch (OptionalDataException e) {
			throw new AlixiaException("DialogSerialization:deSerialize: optional data", e);
		} catch (IOException e) {
			throw new AlixiaException("DialogSerialization:deSerialize: IO exception", e);
		}
		LOGGER.debug("FINISHED DESERIALIZING");
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
		
		LOGGER.debug("IN SERIALIZE");
		SharedUtils.checkNotNull(header);
		SharedUtils.checkNotNull(dialog);
		if (dialog instanceof DialogRequest) {
			request = (DialogRequest) dialog;
			if (!request.isValid()) {
				LOGGER.error(request.toString());
				throw new AlixiaException("DialogSerialization:serialize: invalid dialog request");
			}
			LOGGER.debug("DEBUG DIALOGREQUEST");
			debugString = request.toString();
		} else {
			response = (DialogResponse) dialog;
			if (!response.isValid()) {
			    LOGGER.error(response.toString());
				throw new AlixiaException("DialogSerialization:serialize: invalid dialog response");
			}
			LOGGER.debug("DEBUG DIALOGRESPONSE");
			debugString = response.toString();
		}
		LOGGER.debug(debugString);
		LOGGER.debug("READY TO WRITE OBJECTS");
		byteOutputStream = new ByteArrayOutputStream();
		try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteOutputStream)) {
			objectOutputStream.writeObject(header);
			objectOutputStream.writeObject(dialog);
			objectOutputStream.flush();
			objectOutputStream.close();
		} catch (InvalidClassException e) {
			throw new AlixiaException("DialogSerialization:serialize: invalid class", e);
		} catch (NotSerializableException e) {
			throw new AlixiaException("DialogSerialization:serialize: not serializable", e);
		} catch (IOException e) {
			throw new AlixiaException("DialogSerialization:serialize: IO exception");
		}
		LOGGER.debug("WROTE OBJECTS");
		if (byteOutputStream.size() > MAXHEADROOM) {
			throw new AlixiaException("DialogSerialization: serialized size exceeds Redis max");
		}
		return byteOutputStream.toByteArray();
	}
}
