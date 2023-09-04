package org.openmrs.module.cdrsync.model.dto;

public class ApiResponse<T> {
	
	private String status;
	
	private String message;
	
	private T data;
	
	public ApiResponse() {
	}
	
	public ApiResponse(String status, String message, T data) {
		this.status = status;
		this.message = message;
		this.data = data;
	}
	
	public ApiResponse(String status, String message) {
		this.status = status;
		this.message = message;
	}
	
	public String getStatus() {
		return this.status;
	}
	
	public String getMessage() {
		return this.message;
	}
	
	public T getData() {
		return this.data;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public void setData(T data) {
		this.data = data;
	}
}
