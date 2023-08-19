package org.openmrs.module.cdrsync.container.model;

import java.io.Serializable;
import java.util.Date;

public class PatientBiometricVerificationType implements Serializable {
	
	private int biometricInfoId;
	
	private int patientId;
	
	private String template;
	
	private int imageHeight;
	
	private int imageWidth;
	
	private int imageDpi;
	
	private int imageQuality;
	
	private String fingerPosition;
	
	private String serialNumber;
	
	private String model;
	
	private String manufacturer;
	
	private int creator;
	
	private Date dateCreated;
	
	private String patientUuid;
	
	private String datimId;
	
	private String encodedTemplate;
	
	private String hashed;
	
	private Integer recaptureCount;
	
	public int getBiometricInfoId() {
		return biometricInfoId;
	}
	
	public void setBiometricInfoId(int biometricInfoId) {
		this.biometricInfoId = biometricInfoId;
	}
	
	public int getPatientId() {
		return patientId;
	}
	
	public void setPatientId(int patientId) {
		this.patientId = patientId;
	}
	
	public String getTemplate() {
		return template;
	}
	
	public void setTemplate(String template) {
		this.template = template;
	}
	
	public int getImageHeight() {
		return imageHeight;
	}
	
	public void setImageHeight(int imageHeight) {
		this.imageHeight = imageHeight;
	}
	
	public int getImageWidth() {
		return imageWidth;
	}
	
	public void setImageWidth(int imageWidth) {
		this.imageWidth = imageWidth;
	}
	
	public int getImageDpi() {
		return imageDpi;
	}
	
	public void setImageDpi(int imageDpi) {
		this.imageDpi = imageDpi;
	}
	
	public int getImageQuality() {
		return imageQuality;
	}
	
	public void setImageQuality(int imageQuality) {
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
	
	public int getCreator() {
		return creator;
	}
	
	public void setCreator(int creator) {
		this.creator = creator;
	}
	
	public Date getDateCreated() {
		return dateCreated;
	}
	
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	public String getPatientUuid() {
		return patientUuid;
	}
	
	public void setPatientUuid(String patientUuid) {
		this.patientUuid = patientUuid;
	}
	
	public String getDatimId() {
		return datimId;
	}
	
	public void setDatimId(String datimId) {
		this.datimId = datimId;
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
