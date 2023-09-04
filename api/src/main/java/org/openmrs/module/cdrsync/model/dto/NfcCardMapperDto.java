package org.openmrs.module.cdrsync.model.dto;

import org.openmrs.module.cdrsync.model.nfc_card.NfcCardMapper;

public class NfcCardMapperDto {
	
	private String nfcCardId;
	
	private String patientIdentifier;
	
	private String patientPhoneNo;
	
	public NfcCardMapperDto() {
	}
	
	public NfcCardMapperDto(NfcCardMapper nfcCardMapper) {
		this.nfcCardId = nfcCardMapper.getNfcCardId();
		this.patientIdentifier = nfcCardMapper.getPatientIdentifier();
		this.patientPhoneNo = nfcCardMapper.getPatientPhoneNo();
	}
	
	public String getNfcCardId() {
		return this.nfcCardId;
	}
	
	public String getPatientIdentifier() {
		return this.patientIdentifier;
	}
	
	public void setNfcCardId(String nfcCardId) {
		this.nfcCardId = nfcCardId;
	}
	
	public void setPatientIdentifier(String patientIdentifier) {
		this.patientIdentifier = patientIdentifier;
	}
	
	public String getPatientPhoneNo() {
		return this.patientPhoneNo;
	}
	
	public void setPatientPhoneNo(String patientPhoneNo) {
		this.patientPhoneNo = patientPhoneNo;
	}
}
