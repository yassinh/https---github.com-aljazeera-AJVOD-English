package com.brightcove.consulting.alj.services;


public class ServiceException extends Exception {

	protected int errorCode;
	protected int statusCode;
	protected String errorMessage;
	protected String errorDetails;
	protected String statusReason;



	public ServiceException(int errorCode, int statusCode, String errorMessage, String errorDetails, String statusReason) {
		super(errorMessage);
		this.errorCode = errorCode;
		this.statusCode = statusCode;
		this.errorMessage = errorMessage;
		this.errorDetails = errorDetails;
		this.statusReason = statusReason;
	}

	public ServiceException(String arg0) {
		super(arg0);
		this.errorMessage = arg0;
	}


	public int getErrorCode() {
		return errorCode;
	}
	
	public int getStatusCode() {
		return statusCode;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}

	public String getErrorDetails() {
		return errorDetails;
	}

	public String getStatusReason() {
		return statusReason;
	}

}
