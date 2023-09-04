package org.openmrs.module.cdrsync.model.nfc_card;

import javax.persistence.*;
import java.util.Date;

@Entity(name = "cdrsync.NfcCardMapper")
@Table(name = "nfc_card_mapper")
public class NfcCardMapper {
	
	@Id
	@Column(name = "nfc_card_mapper_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(name = "nfc_card_id")
	private String nfcCardId;
	
	@Column(name = "patient_identifier")
	private String patientIdentifier;
	
	@Column(name = "patient_uuid")
	private String patientUuid;
	
	@Column(name = "patient_phone_no")
	private String patientPhoneNo;
	
	@Column(name = "date_created")
	private Date dateCreated;
	
	@Column(name = "creator")
	private String creator;
	
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getNfcCardId() {
		return nfcCardId;
	}
	
	public void setNfcCardId(String nfcCardId) {
		this.nfcCardId = nfcCardId;
	}
	
	public String getPatientIdentifier() {
		return patientIdentifier;
	}
	
	public void setPatientIdentifier(String patientIdentifier) {
		this.patientIdentifier = patientIdentifier;
	}
	
	public String getPatientUuid() {
		return patientUuid;
	}
	
	public void setPatientUuid(String patientUuid) {
		this.patientUuid = patientUuid;
	}
	
	public String getPatientPhoneNo() {
		return patientPhoneNo;
	}
	
	public void setPatientPhoneNo(String patientPhoneNo) {
		this.patientPhoneNo = patientPhoneNo;
	}
	
	public Date getDateCreated() {
		return dateCreated;
	}
	
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	public String getCreator() {
		return creator;
	}
	
	public void setCreator(String creator) {
		this.creator = creator;
	}
}
