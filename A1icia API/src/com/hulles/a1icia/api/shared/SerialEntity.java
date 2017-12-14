package com.hulles.a1icia.api.shared;

import java.io.Serializable;

public abstract class SerialEntity implements Serializable {
	private static final long serialVersionUID = -4704550566143714188L;

	public SerialEntity() {
	}
	
	abstract public SerialUUID<? extends SerialEntity> getKey();

	@Override
	public boolean equals(Object obj) {
		String uuidStr;
		String otherUUIDStr;
		
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SerialEntity)) {
			return false;
		}
		SerialEntity other = (SerialEntity) obj;
		uuidStr = this.getKey().getUUIDString();
		otherUUIDStr = other.getKey().getUUIDString();
		if (uuidStr == null) {
			if (otherUUIDStr != null) {
				return false;
			}
		} else if (!uuidStr.equals(otherUUIDStr)) {
			return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		
		result = prime * result + ((getKey() == null) ? 0 : getKey().getUUIDString().hashCode());
		return result;
	}

}
