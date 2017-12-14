/*******************************************************************************
 * Copyright © 2017 Hulles Industries LLC
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
 *******************************************************************************/
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
