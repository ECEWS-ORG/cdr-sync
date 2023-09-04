package org.openmrs.module.cdrsync.model.extractor;

import java.io.Serializable;

public class SystemProperty implements Serializable {
	
	private String osName;
	
	private String osVersion;
	
	private String osArch;
	
	private String javaVersion;
	
	private String hostName;
	
	private Long diskSpace;
	
	private String userName;
	
	private Long ramSize;
	
	public String getOsName() {
		return osName;
	}
	
	public void setOsName(String osName) {
		this.osName = osName;
	}
	
	public String getOsVersion() {
		return osVersion;
	}
	
	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}
	
	public String getOsArch() {
		return osArch;
	}
	
	public void setOsArch(String osArch) {
		this.osArch = osArch;
	}
	
	public String getJavaVersion() {
		return javaVersion;
	}
	
	public void setJavaVersion(String javaVersion) {
		this.javaVersion = javaVersion;
	}
	
	public String getHostName() {
		return hostName;
	}
	
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	
	public Long getDiskSpace() {
		return diskSpace;
	}
	
	public void setDiskSpace(Long diskSpace) {
		this.diskSpace = diskSpace;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public Long getRamSize() {
		return ramSize;
	}
	
	public void setRamSize(Long ramSize) {
		this.ramSize = ramSize;
	}
}
