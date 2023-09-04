package org.openmrs.module.cdrsync.model.extractor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Date;

@Entity
@Table(name = "biometricverificationinfo")
public class BiometricVerificationInfo {
	
	@Id
	@Column(name = "biometricInfo_Id")
	private Integer biometricInfoId;
	
	@Column(name = "patient_Id")
	private Integer patientId;
	
	@Column(name = "template")
	private String template;
	
	@Column(name = "new_template")
	private byte[] newTemplate;
	
	@Column(name = "imageWidth")
	private Integer imageWidth;
	
	@Column(name = "imageHeight")
	private Integer imageHeight;
	
	@Column(name = "imageDPI")
	private Integer imageDPI;
	
	@Column(name = "imageQuality")
	private Integer imageQuality;
	
	@Column(name = "fingerPosition")
	private String fingerPosition;
	
	@Column(name = "serialNumber")
	private String serialNumber;
	
	@Column(name = "model")
	private String model;
	
	@Column(name = "manufacturer")
	private String manufacturer;
	
	@Column(name = "creator")
	private Integer creator;
	
	@Column(name = "date_created")
	private Date dateCreated;
	
	@Column(name = "encoded_template")
	private String encodedTemplate;
	
	@Column(name = "hashed")
	private String hashed;
	
	@Column(name = "recapture_count")
	private Integer recaptureCount;
	
	public Integer getBiometricInfoId() {
		return biometricInfoId;
	}
	
	public void setBiometricInfoId(Integer biometricInfoId) {
		this.biometricInfoId = biometricInfoId;
	}
	
	public Integer getPatientId() {
		return patientId;
	}
	
	public void setPatientId(Integer patientId) {
		this.patientId = patientId;
	}
	
	public String getTemplate() {
		if (getNewTemplate() != null) {
			try {
				byte[] blobData = getNewTemplate();
				setNewTemplate(null);
				return new String(blobData);
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return template;
	}
	
	public void setTemplate(String template) {
		this.template = template;
	}
	
	public byte[] getNewTemplate() {
		return newTemplate;
	}
	
	public void setNewTemplate(byte[] newTemplate) {
		this.newTemplate = newTemplate;
	}
	
	public Integer getImageWidth() {
		return imageWidth;
	}
	
	public void setImageWidth(Integer imageWidth) {
		this.imageWidth = imageWidth;
	}
	
	public Integer getImageHeight() {
		return imageHeight;
	}
	
	public void setImageHeight(Integer imageHeight) {
		this.imageHeight = imageHeight;
	}
	
	public Integer getImageDPI() {
		return imageDPI;
	}
	
	public void setImageDPI(Integer imageDPI) {
		this.imageDPI = imageDPI;
	}
	
	public Integer getImageQuality() {
		return imageQuality;
	}
	
	public void setImageQuality(Integer imageQuality) {
		this.imageQuality = imageQuality;
	}
	
	public String getFingerPosition() {
		return fingerPosition;
	}
	
	public void setFingerPosition(String fingerPosition) {
		this.fingerPosition = fingerPosition;
	}
	
	public String getSerialNumber() {
		return serialNumber;
	}
	
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}
	
	public String getModel() {
		return model;
	}
	
	public void setModel(String model) {
		this.model = model;
	}
	
	public String getManufacturer() {
		return manufacturer;
	}
	
	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}
	
	public Integer getCreator() {
		return creator;
	}
	
	public void setCreator(Integer creator) {
		this.creator = creator;
	}
	
	public Date getDateCreated() {
		return dateCreated;
	}
	
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	public String getEncodedTemplate() {
		return encodedTemplate;
	}
	
	public void setEncodedTemplate(String encodedTemplate) {
		this.encodedTemplate = encodedTemplate;
	}
	
	public String getHashed() {
		return hashed;
	}
	
	public void setHashed(String hashed) {
		this.hashed = hashed;
	}
	
	public Integer getRecaptureCount() {
		return recaptureCount;
	}
	
	public void setRecaptureCount(Integer recaptureCount) {
		this.recaptureCount = recaptureCount;
	}
}
