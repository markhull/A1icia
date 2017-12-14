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

public class DialogSerialization {
	final static Logger LOGGER = Logger.getLogger("A1iciaApi.DialogSerialization");
	final static Level LOGLEVEL = A1iciaConstants.getA1iciaLogLevel();
	private static final boolean DEBUG = true;
	private static final int MAXHEADROOM = JebusApiHub.getMaxHardOutputBufferLimit();
	
	public static Dialog deSerialize(A1icianID a1icianID, byte[] bytes) {
		ByteArrayInputStream byteInputStream;
		Object object;
		DialogHeader header;
		Dialog dialog = null;
		Dialog notOurDialog = null;
		A1icianID dialogA1icianID;
		A1icianID bcastID = A1iciaConstants.getBroadcastA1icianID();
		String debugString;
		
		SharedUtils.checkNotNull(a1icianID);
		SharedUtils.checkNotNull(bytes);
		LOGGER.log(LOGLEVEL, "DESERIALIZING");
		byteInputStream = new ByteArrayInputStream(bytes);
		try (ObjectInputStream objectInputStream = new ObjectInputStream(byteInputStream)) {
			object = objectInputStream.readObject();
			header = (DialogHeader) object;
			dialogA1icianID = header.getToA1icianID();
			if (dialogA1icianID.equals(bcastID) || dialogA1icianID.equals(a1icianID)) {
				object = objectInputStream.readObject();
				dialog = (Dialog) object;
				if (DEBUG) {
					if (dialog instanceof DialogRequest) {
						debugString = ((DialogRequest)dialog).toString();
					} else {
						debugString = ((DialogResponse)dialog).toString();
					}
					LOGGER.log(LOGLEVEL, debugString);
				}
			} else if (DEBUG) {
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
