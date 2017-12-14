package com.hulles.a1icia.api.shared;

/**
 * The A1icia API version of the RuntimeException, for possible expanded use later.
 * 
 * @author hulles
 *
 */
public final class A1iciaAPIException extends RuntimeException {
	private static final long serialVersionUID = -4744550566143714488L;
	
	public A1iciaAPIException() {
		super("500 The Bees They're In My Eyes");
	}
	public A1iciaAPIException(String desc) {
		super(desc);
	}
    public A1iciaAPIException(String desc, Throwable ex) {
        super(desc, ex);
        
        ex.printStackTrace();
    }
}
