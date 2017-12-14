package com.hulles.a1icia.api.shared;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Serialization {
	
	public static Serializable deSerialize(byte[] bytes) throws IOException, ClassNotFoundException {
		ByteArrayInputStream byteInputStream;
		ObjectInputStream objectInputStream;
		Object object;
		
		SharedUtils.checkNotNull(bytes);
		byteInputStream = new ByteArrayInputStream(bytes);
		objectInputStream = new ObjectInputStream(byteInputStream);
		object = objectInputStream.readObject();
		objectInputStream.close();
		return (Serializable) object;
	}

	public static byte[] serialize(Serializable object) throws IOException {
		ByteArrayOutputStream byteOutputStream;
		ObjectOutputStream objectOutputStream;
		
		SharedUtils.checkNotNull(object);
		byteOutputStream = new ByteArrayOutputStream();
		objectOutputStream = new ObjectOutputStream(byteOutputStream);
		objectOutputStream.writeObject(object);
		objectOutputStream.close();
		return byteOutputStream.toByteArray();
	}
}
