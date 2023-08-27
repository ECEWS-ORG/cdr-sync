package org.openmrs.module.cdrsync.model;

import org.codehaus.jackson.annotate.JsonProperty;
import org.openmrs.module.Module;

import java.io.Serializable;
import java.util.List;

public class ModuleInfo implements Serializable {
	
	private String name;
	
	private String moduleId;
	
	private String packageName;
	
	private String description;
	
	private String author;
	
	private String version;
	
	public ModuleInfo(Module module) {
		this.name = module.getName();
		this.moduleId = module.getModuleId();
		this.packageName = module.getPackageName();
		this.description = module.getDescription();
		this.author = module.getAuthor();
		this.version = module.getVersion();
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getModuleId() {
		return moduleId;
	}
	
	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}
	
	public String getPackageName() {
		return packageName;
	}
	
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public void setAuthor(String author) {
		this.author = author;
	}
	
	public String getVersion() {
		return version;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}
}
