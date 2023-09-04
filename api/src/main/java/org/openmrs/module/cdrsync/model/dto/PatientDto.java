package org.openmrs.module.cdrsync.model.dto;

public class PatientDto {
	
	private String patientIdentifier;
	
	private String patientPhoneNumber;
	
	private String patientUuid;
	
	private String patientName;
	
	private int identifierType;
	
	private boolean voided;
	
	public PatientDto() {
	}
	
	public PatientDto(String patientIdentifier, String patientPhoneNumber, String patientUuid, String patientName,
	    int identifierType, boolean voided) {
		this.patientIdentifier = patientIdentifier;
		this.patientPhoneNumber = patientPhoneNumber;
		this.patientUuid = patientUuid;
		this.patientName = patientName;
		this.identifierType = identifierType;
		this.voided = voided;
	}
	
	public String getPatientIdentifier() {
		return this.patientIdentifier;
	}
	
	public String getPatientPhoneNumber() {
		return this.patientPhoneNumber;
	}
	
	public void setPatientIdentifier(String patientIdentifier) {
		this.patientIdentifier = patientIdentifier;
	}
	
	public void setPatientPhoneNumber(String patientPhoneNumber) {
		this.patientPhoneNumber = patientPhoneNumber;
	}
	
	public String getPatientUuid() {
		return this.patientUuid;
	}
	
	public void setPatientUuid(String patientUuid) {
		this.patientUuid = patientUuid;
	}
	
	public String getPatientName() {
		return this.patientName;
	}
	
	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}
	
	public int getIdentifierType() {
		return identifierType;
	}
	
	public void setIdentifierType(int identifierType) {
		this.identifierType = identifierType;
	}
	
	public boolean isVoided() {
		return voided;
	}
	
	public void setVoided(boolean voided) {
		this.voided = voided;
	}
}
