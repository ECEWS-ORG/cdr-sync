package org.openmrs.module.cdrsync.model;

import java.util.List;

public class FacilityMetaData {
	
	private String id;
	
	private String facilityName;
	
	private String facilityDatimCode;
	
	private String operatingSystem;
	
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
	
	public String getOperatingSystem() {
		return operatingSystem;
	}
	
	public void setOperatingSystem(String operatingSystem) {
		this.operatingSystem = operatingSystem;
	}
	
	public List<ModuleInfo> getInstalledModules() {
		return installedModules;
	}
	
	public void setInstalledModules(List<ModuleInfo> installedModules) {
		this.installedModules = installedModules;
	}
}
