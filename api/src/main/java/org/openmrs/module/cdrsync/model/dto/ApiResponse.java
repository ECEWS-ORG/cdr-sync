package org.openmrs.module.cdrsync.model.dto;

import org.codehaus.jackson.annotate.JsonProperty;

public class ApiResponse<T> {
	
	@JsonProperty("Status")
	private boolean status;
	
	@JsonProperty("Message")
	private String message;
	
	@JsonProperty("Data")
	private T data;
	
	public ApiResponse() {
	}
	
	public ApiResponse(boolean status, String message, T data) {
		this.status = status;
		this.message = message;
		this.data = data;
	}
	
	public ApiResponse(boolean status, String message) {
		this.status = status;
		this.message = message;
	}
	
	public boolean getStatus() {
		return this.status;
	}
	
	public String getMessage() {
		return this.message;
	}
	
	public T getData() {
		return this.data;
	}
	
	public void setStatus(boolean status) {
		this.status = status;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public void setData(T data) {
		this.data = data;
	}
}
