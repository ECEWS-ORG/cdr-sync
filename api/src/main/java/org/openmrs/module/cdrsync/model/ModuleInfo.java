package org.openmrs.module.cdrsync.model;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

public class ModuleInfo {
	
	@JsonProperty("uuid")
	public String uuid;
	
	@JsonProperty("display")
	public String display;
	
	@JsonProperty("name")
	public String name;
	
	@JsonProperty("description")
	public String description;
	
	@JsonProperty("packageName")
	public String packageName;
	
	@JsonProperty("author")
	public String author;
	
	@JsonProperty("version")
	public String version;
	
	@JsonProperty("started")
	public Boolean started;
	
	@JsonProperty("startupErrorMessage")
	public Object startupErrorMessage;
	
	@JsonProperty("requireOpenmrsVersion")
	public String requireOpenmrsVersion;
	
	@JsonProperty("awareOfModules")
	public List<String> awareOfModules;
	
	@JsonProperty("requiredModules")
	public List<String> requiredModules;
	
	@JsonProperty("links")
	public List<Link> links;
	
	@JsonProperty("resourceVersion")
	public String resourceVersion;
	
	static class Link {
		
		@JsonProperty("rel")
		public String rel;
		
		@JsonProperty("uri")
		public String uri;
	}
}
