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
package com.hulles.alixia.cayenne;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.ObjectSelect;
import org.apache.cayenne.query.Query;

import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.Gender;
import com.hulles.alixia.api.shared.SerialPerson;
import com.hulles.alixia.api.shared.SerialUUID;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.api.shared.UserType;
import com.hulles.alixia.cayenne.auto._Person;
import com.hulles.alixia.crypto.AlixiaCrypto;
import com.hulles.alixia.media.MediaUtils;

public class Person extends _Person {
    private static final long serialVersionUID = 1L;
	private static final Integer SYSTEMPERSONID = 1;
    private static final String DEFAULT_PASSWORD = "xx";
    private static final String DEFAULT_EMAIL = "NewPerson@example.com";
    private static final DateFormat dateToString = DateFormat.getDateInstance();
    
    public Person() {
    	
    }
    
    public static Person getSystemPerson() {
    	
    	return findPerson(SYSTEMPERSONID);
    }
    
    public static Person findPerson(Integer personID) {

		SharedUtils.checkNotNull(personID);
		return Cayenne.objectForPK(AlixiaApplication.getEntityContext(), Person.class, personID);
    }
    public static Person findPerson(SerialUUID<SerialPerson> uuid) {
		Query query;
		ObjectContext context;

		SharedUtils.checkNotNull(uuid);
		context = AlixiaApplication.getEntityContext();
		query = ObjectSelect
				.query(Person.class)
				.where(_Person.PERSON_UUID.eq(uuid.getUUIDString()));
		return (Person) Cayenne.objectForQuery(context, query);
    }
    public static Person findPerson(String userName) {
		Query query;
		ObjectContext context;

		SharedUtils.checkNotNull(userName);
		context = AlixiaApplication.getEntityContext();
		query = ObjectSelect
				.query(Person.class)
				.where(_Person.USERNAME.likeIgnoreCase(userName));
		return (Person) Cayenne.objectForQuery(context, query);
    }
    
    public static Person findPersonByEmail(String email) {
		Query query;
		ObjectContext context;

		SharedUtils.checkNotNull(email);
		context = AlixiaApplication.getEntityContext();
		query = ObjectSelect
				.query(Person.class)
				.where(_Person.EMAIL.likeIgnoreCase(email));
		return (Person) Cayenne.objectForQuery(context, query);
    }

	public static List<Person> getAllPersons() {
		ObjectContext context;
		List<Person> persons;

		context = AlixiaApplication.getEntityContext();
		persons = ObjectSelect
				.query(Person.class)
				.orderBy("lastName")
				.orderBy("firstName")
				.select(context);
		return persons;
    }

	public static List<Person> getPersonsByFirstName(String name) {
		ObjectContext context;
		List<Person> persons;

		SharedUtils.checkNotNull(name);
		context = AlixiaApplication.getEntityContext();
		persons = ObjectSelect
				.query(Person.class)
				.where(_Person.FIRST_NAME.likeIgnoreCase(name))
				.select(context);
		return persons;
	}
	
	public Boolean hasAvatar() {
		byte[] imageBytes;
		
		imageBytes = this.getAvatarBytes();
		return imageBytes != null;
	}
    
    public BufferedImage getAvatar() {
		byte[] imageBytes = null;
		BufferedImage image = null;

		imageBytes = this.getAvatarBytes();
		if (imageBytes != null) {
			try {
				image = MediaUtils.byteArrayToImage(imageBytes);
			} catch (IOException e) {
				throw new AlixiaException("Person: can't restore image from bytes", e);
			}
		}
		return image;
    }

	public void setAvatar(Image image) {
        byte[] imageBytes;

        SharedUtils.nullsOkay(image);
        if (image == null) {
        	this.setAvatarBytes(null);
        } else {
            try {
				imageBytes = MediaUtils.imageToByteArray(image);
			} catch (IOException e) {
				throw new AlixiaException("Person: can't convert image to bytes", e);
			}
            this.setAvatarBytes(imageBytes);
        }
    }
	
	public Gender getGender() {
		Character code;
		
		code = this.getGenderCode();
		if (code == null) {
			return null;
		}
		return Gender.findGender(code);
	}
	
	public void setGender(Gender gender) {
		
		SharedUtils.nullsOkay(gender);
		if (gender == null) {
			this.setGender(null);
			return;
		}
		this.setGenderCode(gender.getStoreName());
	}
	
    public UserType getUserType() {
    	Integer userTypeID;
    	
    	userTypeID = this.getUserTypeId();
    	if (userTypeID == null) {
    		return null;
    	}
    	return UserType.findUserType(userTypeID);
    }

    public void setUserType(UserType type) {
    	Integer userTypeID;
    	
    	SharedUtils.checkNotNull(type);
    	userTypeID = type.getStoreID();
    	this.setUserTypeId(userTypeID);
    }
    
    public SerialUUID<SerialPerson> getUUID() {
    	String uuidStr;

		uuidStr = this.getPersonUuid();
		return new SerialUUID<>(uuidStr);
    }

    public String getLastAccessString() {
        LocalDateTime ldt;

        ldt = getLastAccess();
        return getDateString(ldt);
    }

    public void setLastAccess() {
        LocalDateTime ldt;
    	
    	ldt = LocalDateTime.now();
    	this.setLastAccess(ldt);
    }
    
    public String getFullNameLF() {
		StringBuffer buf;
		String fname;
		String lname;
		
		fname = getFirstName();
		lname = getLastName();
		
		buf = new StringBuffer();
		if ((lname != null) && (!lname.isEmpty())) {
			buf.append(lname);
		}
		if ((fname != null) && (!fname.isEmpty())) {
			if (lname != null) {
				buf.append(", ");
			}
			buf.append(fname);
		}
		return buf.toString();
    }
    
    public String getFullNameFL() {
		StringBuffer buf;
		String fname;
		String lname;
		
		fname = getFirstName();
		lname = getLastName();
		
		buf = new StringBuffer();
		if ((fname != null) && (!fname.isEmpty())) {
			buf.append(fname);
		}
		if ((lname != null) && (!lname.isEmpty())) {
			if (fname != null) {
				buf.append(" ");
			}
			buf.append(lname);
		}
		return buf.toString();
    }
    
    public Boolean verifyPassword(String pword) {
    	String hashedPassword;
    	boolean okay;

    	SharedUtils.checkNotNull(pword);
    	hashedPassword = this.getPassword();
    	okay = AlixiaCrypto.checkPassword(pword, hashedPassword);
    	return okay;
    }

    @Override
	public void setPassword(String password) {
    	String hashedPassword;
    	LocalDateTime ldt;
    	
    	SharedUtils.checkNotNull(password);
    	hashedPassword = AlixiaCrypto.hashPassword(password);
    	super.setPassword(hashedPassword);
    	ldt = LocalDateTime.now();
    	this.setPasswordChanged(ldt);
    }
    
	public static Person createNew() {
		String uuidStr;
		Person person;
		ObjectContext context;
		
		context = AlixiaApplication.getEntityContext();
		person = context.newObject(Person.class);
		uuidStr = UUID.randomUUID().toString();
		person.setPersonUuid(uuidStr);
		person.setUsername("New Person");
		person.setEmail(DEFAULT_EMAIL);
		person.setPassword(DEFAULT_PASSWORD);
		person.setUserType(UserType.NORMAL);
		context.commitChanges();
		return person;
	}

    public String getDOBString() {
        LocalDate ld;

        ld = getBirthDate();
        if (ld == null) {
        	return null;
        }
        return getDateString(ld);
    }

    public Long getAgeInDays() {
        LocalDate birthLD;
    	LocalDate today;
    	
        birthLD = getBirthDate();
        if (birthLD == null) {
        	return null;
        }
        today = LocalDate.now();
        return ChronoUnit.DAYS.between(birthLD, today);
    }
    
    public Integer getAgeInYears() {
        LocalDate birthLD;
    	LocalDate today;
    	Period p;
    	
        birthLD = getBirthDate();
        if (birthLD == null) {
        	return null;
        }
        today = LocalDate.now();
        p = Period.between(birthLD, today);
        return p.getYears();
    }
    
    public void delete() {
    	ObjectContext context;
    	
    	context = this.getObjectContext();
		context.deleteObjects(this);
    	context.commitChanges();
    }
    
    public void commit() {
    	ObjectContext context;
    	
    	context = this.getObjectContext();
    	context.commitChanges();
    }

    public boolean iAmTheCentralScrutinizer() {
    	
    	return this.equals(getSystemPerson());
    }
    
    public static String getDateString(LocalDate date) {
        String result;

        SharedUtils.checkNotNull(date);
        result = dateToString.format(date);
        return result;
    }
    
    public static String getDateString(LocalDateTime date) {
        String result;

        SharedUtils.checkNotNull(date);
        result = dateToString.format(date);
        return result;
    }
}
