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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.hulles.a1icia.november;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hulles.a1icia.api.A1iciaConstants;
import com.hulles.a1icia.api.shared.SerialMiniPerson;
import com.hulles.a1icia.api.shared.SerialPerson;
import com.hulles.a1icia.api.shared.SerialUUID;
import com.hulles.a1icia.cayenne.Person;
import com.hulles.a1icia.tools.A1iciaUtils;

/**
 * 
 * @author hulles
 */
final public class PersonUtils {
	private final static Logger logger = Logger.getLogger("A1iciaNovember.PersonUtils");
	private final static Level LOGLEVEL = A1iciaConstants.getA1iciaLogLevel();

	public PersonUtils() {

  	}

	public static SerialUUID<SerialPerson> login(String userName, String password) {
        Person dbPerson;
        SerialUUID<SerialPerson> uuid;
        
        // Should use SSL...
        if ((userName == null) || (password == null)) {
            return null;
        }
        dbPerson = Person.findPerson(userName);
        if (dbPerson == null) {
            return null;
        }
        if (!dbPerson.verifyPassword(password)) {
            return null;
        }
        uuid = dbPerson.getUUID();
        dbPerson.setLastAccess();
        dbPerson.commit();
		return uuid;
	}

	public static SerialUUID<SerialPerson> updatePerson(SerialPerson person) {
		Person dbPerson;
		String password;
		SerialUUID<SerialPerson> uuid;
		Date date;
		
		if (person == null) {
			A1iciaUtils.error("Null person in updatePerson"); //$NON-NLS-1$
			return null;
		}
		uuid = person.getUUID();
		if (uuid == null) {
			A1iciaUtils.error("Null UUID in updatePerson"); //$NON-NLS-1$
			return null;
		}
		dbPerson = Person.findPerson(uuid);
		if (dbPerson == null) {
			A1iciaUtils.error("Can't retrieve person in updatePerson"); //$NON-NLS-1$
			return null;
		}
		dbPerson.setUsername(person.getUserName());
		dbPerson.setFirstName(person.getFirstName());
		dbPerson.setLastName(person.getLastName());
		dbPerson.setEmail(person.getEmail());
		dbPerson.setMobile(person.getMobile());
		date = person.getDOB();
		dbPerson.setBirthDate(A1iciaUtils.ldFromUtilDate(date));
		dbPerson.setGender(person.getGender());
		dbPerson.setHeightCm(person.getHeightCm());
		password = person.getPassword();
		if (password != null && !password.isEmpty()) {
			// it could be null or blank coming in from EditProfile, form isn't
			// populated with old
			// field (s/b only field like that)
			if (!dbPerson.verifyPassword(password)) {
				dbPerson.setPassword(password);
			}
		}
		dbPerson.commit();
		return uuid;
	}

	public static List<SerialMiniPerson> getPeople() {
		List<SerialMiniPerson> sysPeople;
		List<Person> people;
		SerialMiniPerson user;

		sysPeople = new ArrayList<>();
		people = Person.getAllPersons();
		for (Person dbPerson : people) {
			user = buildSerialMiniPerson(dbPerson);
			sysPeople.add(user);
		}
		logger.log(LOGLEVEL, "getPeople: Count is " + sysPeople.size());
		return sysPeople;
	}

	public static void updatePassword(SerialUUID<SerialPerson> personUUID, String pword) {
		Person dbPerson;
		
		if ((personUUID == null) || (pword == null)) {
			A1iciaUtils.error("Null argument(s) passed to updatePassword"); //$NON-NLS-1$
			return;
		}
		dbPerson = Person.findPerson(personUUID);
		dbPerson.setPassword(pword);
		dbPerson.commit();
	}
  
    public static SerialPerson buildSerialPerson(Person dbPerson) {
    	SerialPerson person;
    	LocalDate ld;
    	
        person = new SerialPerson();
        person.setUUID(dbPerson.getUUID());
        person.setUserName(dbPerson.getUsername());
        person.setUserType(dbPerson.getUserType());
        person.setFirstName(dbPerson.getFirstName());
        person.setLastName(dbPerson.getLastName());
        person.setEmail(dbPerson.getEmail());
        person.setNotes(dbPerson.getNotes());
        person.setMobile(dbPerson.getMobile());
        person.setFullNameFL(dbPerson.getFullNameFL());
        person.setFullNameLF(dbPerson.getFullNameLF());
        ld = dbPerson.getBirthDate();
        person.setDOB(A1iciaUtils.utilDateFromLD(ld));
        person.setDOBString(dbPerson.getDOBString());
        person.setAgeInYears(dbPerson.getAgeInYears());
        person.setGender(dbPerson.getGender());
        person.setHeightCm(dbPerson.getHeightCm());
        person.setCheckIn(dbPerson.getCheckIn());
        person.setHasAvatar(dbPerson.hasAvatar());
        dbPerson.setLastAccess();
        dbPerson.commit();
        return person;
    }
    
    private static SerialMiniPerson buildSerialMiniPerson(Person dbPerson) {
    	SerialMiniPerson person;
    	
    	person = new SerialMiniPerson();
        person.setUUID(dbPerson.getUUID());
        person.setUserName(dbPerson.getUsername());
        person.setUserType(dbPerson.getUserType());
        person.setFullNameFL(dbPerson.getFullNameFL());
        person.setFullNameLF(dbPerson.getFullNameLF());
        return person;
    }

    public static List<ValidationError> validate(SerialPerson person) {
        List<ValidationError> errors;
        String userName;
        String email;
        Person dbPerson;
        SerialUUID<SerialPerson> uuid;

        errors = new ArrayList<>(ValidationError.values().length);
        userName = person.getUserName();
        if (userName == null) {
            errors.add(ValidationError.INVALID_NAME);
        } else {
            dbPerson = Person.findPerson(userName);
            if (dbPerson != null) {
            	uuid = dbPerson.getUUID();
            	if ((person.getUUID() == null) || (!uuid.equals(person.getUUID()))) {
            		// if it's not for the same person we already have it's an error
             		errors.add(ValidationError.NAME_EXISTS);
            	}
            }
        }
        email = person.getEmail();
        if (email == null) {
            errors.add(ValidationError.INVALID_EMAIL);
        } else {
            dbPerson = Person.findPersonByEmail(email);
            if (dbPerson != null) {
            	uuid = dbPerson.getUUID();
            	if ((person.getUUID() == null) || (!uuid.equals(person.getUUID()))) {
            		// if it's not for the same person we already have it's an error
            		errors.add(ValidationError.EMAIL_EXISTS);
            	}
            }
        }
        return errors;
    }

}
