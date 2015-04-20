package com.brightcove.consulting.alj.models;

import java.io.Serializable;

/**
 * Base class for Gigya data store backed objects.
 * 
 * @author ssayles
 */
public abstract class DataStoreObject implements Serializable {

	private static final long serialVersionUID = -5625586426856808480L;

	private String oid;

	public String getOid() {
		return oid;
	}

	public void setOid(String oid) {
		this.oid = oid;
	}

	
}
