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
package com.hulles.alixia.api.shared;

import java.util.Date;

final public class SerialPerson extends SerialEntity {
	private static final long serialVersionUID = 7983119219364774032L;
	private String userName;
	private UserType userType;
	private String firstName;
	private String lastName;
	private String email;
	private String notes;
	private String mobile;
	private String fullNameLF; // lastname, firstname
	private String fullNameFL; // firstname lastname
	private Locale locale;
	private Date dob;
	private String dobString;
	private Integer ageInYears;
	private Integer heightCm;
	private Gender gender;
    private Boolean checkIn;
    private Boolean hasAvatar;
    private SerialUUID<SerialPerson> uuid;
	private transient String password;

	public SerialPerson() {
		// need no-arg constructor
	}

	public Boolean getCheckIn() {
		
		return checkIn;
	}

	public void setCheckIn(Boolean checkIn) {
		
		SharedUtils.nullsOkay(checkIn);
		this.checkIn = checkIn;
	}

	public Boolean hasAvatar() {
		
		return hasAvatar;
	}

	public void setHasAvatar(Boolean hasAvatar) {
		
		SharedUtils.checkNotNull(hasAvatar);
		this.hasAvatar = hasAvatar;
	}

	public Gender getGender() {
		
		return gender;
	}

	public void setGender(Gender gender) {
		
		SharedUtils.nullsOkay(gender);
		this.gender = gender;
	}

	public Integer getHeightCm() {
		
		return heightCm;
	}

	public void setHeightCm(Integer heightCm) {
		
		SharedUtils.nullsOkay(heightCm);
		this.heightCm = heightCm;
	}

	public Date getDOB() {
		
		return dob;
	}

	public void setDOB(Date dob) {
		
		SharedUtils.nullsOkay(dob);
		this.dob = dob;
	}

	public String getDOBString() {
		
		return dobString;
	}

	public void setDOBString(String dobString) {

		SharedUtils.nullsOkay(dobString);
		this.dobString = dobString;
	}

	public Integer getAgeInYears() {
		
		return ageInYears;
	}

	public void setAgeInYears(Integer ageInYears) {
		
		SharedUtils.nullsOkay(ageInYears);
		this.ageInYears = ageInYears;
	}

	public String getFullNameLF() {
		
		return fullNameLF;
	}

	public void setFullNameLF(String fullNameLF) {
		
		SharedUtils.checkNotNull(fullNameLF);
		this.fullNameLF = fullNameLF;
	}

	public String getFullNameFL() {
		
		return fullNameFL;
	}

	public void setFullNameFL(String fullNameFL) {
		
		SharedUtils.checkNotNull(fullNameFL);
		this.fullNameFL = fullNameFL;
	}

	public String getFirstName() {
		
		return firstName;
	}

	public void setFirstName(String firstName) {
		
		SharedUtils.checkNotNull(firstName);
		this.firstName = firstName;
	}

	public String getLastName() {
		
		return lastName;
	}

	public void setLastName(String lastName) {
		
		SharedUtils.checkNotNull(lastName);
		this.lastName = lastName;
	}

	public String getEmail() {
		
		return email;
	}

	public void setEmail(String email) {
		
		SharedUtils.checkNotNull(email);
		this.email = email;
	}

	public String getNotes() {
		
		return notes;
	}

	public void setNotes(String notes) {
		
		SharedUtils.nullsOkay(notes);
		this.notes = notes;
	}

	public String getUserName() {
		
		return userName;
	}

	public void setUserName(String userName) {
		
		SharedUtils.checkNotNull(userName);
		this.userName = userName;
	}

	public UserType getUserType() {
		
		return userType;
	}

	public void setUserType(UserType userType) {
		
		SharedUtils.checkNotNull(userType);
		this.userType = userType;
	}

	public boolean isSystemAdmin() {
		
		return (userType == UserType.SYSADMIN);
	}

	public boolean isAdmin() {
		
		switch (userType) {
		case ADMIN:
		case SYSADMIN:  // sys admins are also admins
			return true;
		default:
			return false;
		}		
	}

	public boolean isNormal() {
		
		switch (userType) {
		case NORMAL:
		case ADMIN:		// company admins are also normal
		case SYSADMIN:  // sys admins are also normal
			return true;
		default:
			return false;
		}		
	}

	public SerialUUID<SerialPerson> getUUID() {
		
		return uuid;
	}

	public void setUUID(SerialUUID<SerialPerson> uuid) {
		
		SharedUtils.checkNotNull(uuid);
		this.uuid = uuid;
	}

	public String getPassword() {
		
		return password;
	}

	public void setPassword(String password) {
		
		SharedUtils.checkNotNull(password);
		this.password = password;
	}

	public String getMobile() {
		
		return mobile;
	}

	public void setMobile(String mobile) {
		
		SharedUtils.nullsOkay(mobile);
		this.mobile = mobile;
	}

	public Locale getLocale() {
		
		return locale;
	}

	public void setLocale(Locale locale) {
		
		SharedUtils.checkNotNull(locale);
		this.locale = locale;
	}

	@Override
	public SerialUUID<SerialPerson> getKey() {

		return uuid;
	}

}
