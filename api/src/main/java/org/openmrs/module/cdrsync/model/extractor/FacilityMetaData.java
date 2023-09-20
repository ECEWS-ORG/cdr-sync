package org.openmrs.module.cdrsync.model.extractor;

import java.io.Serializable;
import java.util.List;

public class FacilityMetaData implements Serializable {
	
	private String id;
	
	private String facilityName;
	
	private String facilityDatimCode;
	
	private String initVectorText;
	
	private String encryptionKeyText;
	
	private SystemProperty systemProperty;
	
	private List<ModuleInfo> installedModules;
	
	public FacilityMetaData(List<ModuleInfo> installedModules) {
		this.installedModules = installedModules;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getFacilityName() {
		return facilityName;
	}
	
	public void setFacilityName(String facilityName) {
		this.facilityName = facilityName;
	}
	
	public String getFacilityDatimCode() {
		return facilityDatimCode;
	}
	
	public void setFacilityDatimCode(String facilityDatimCode) {
		this.facilityDatimCode = facilityDatimCode;
	}
	
	public List<ModuleInfo> getInstalledModules() {
		return installedModules;
	}
	
	public void setInstalledModules(List<ModuleInfo> installedModules) {
		this.installedModules = installedModules;
	}
	
	public SystemProperty getSystemProperty() {
		return systemProperty;
	}
	
	public void setSystemProperty(SystemProperty systemProperty) {
		this.systemProperty = systemProperty;
	}
	
	public String getInitVectorText() {
		return initVectorText;
	}
	
	public void setInitVectorText(String initVectorText) {
		this.initVectorText = initVectorText;
	}
	
	public String getEncryptionKeyText() {
		return encryptionKeyText;
	}
	
	public void setEncryptionKeyText(String encryptionKeyText) {
		this.encryptionKeyText = encryptionKeyText;
	}
	
}
