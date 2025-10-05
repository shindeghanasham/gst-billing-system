package com.gst.billing.model;

public class OtpRequest {
	private String mobileNumber;
	private String otp;

	// Constructors
	public OtpRequest() {
	}

	public OtpRequest(String mobileNumber, String otp) {
		this.mobileNumber = mobileNumber;
		this.otp = otp;
	}

	// Getters and Setters
	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public String getOtp() {
		return otp;
	}

	public void setOtp(String otp) {
		this.otp = otp;
	}
}