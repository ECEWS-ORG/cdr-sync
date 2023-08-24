package org.openmrs.module.cdrsync.container.model;

import org.openmrs.module.cdrsync.model.Covid19Case;

import javax.persistence.Column;
import java.util.Date;

public class Covid19CaseType {
	
	private Integer covid19CaseId;
	
	private Integer patientId;
	
	private String caseId;
	
	private String pepfarId;
	
	private String gender;
	
	private Date dateOfBirth;
	
	private Date hivDiagnosisDate;
	
	private Date artStartDate;
	
	private Integer caseStatus;
	
	private Date caseStatusDate;
	
	private Integer poeScreened;
	
	private Integer onAdmission;
	
	private Integer statusDA;
	
	private Integer occupation;
	
	private Integer typRespiratory;
	
	private Integer typBaseSerum;
	
	private Integer typOthers;
	
	private Integer sympAsymp;
	
	private String patientUuid;
	
	private String datimCode;
	
	public Covid19CaseType(Covid19Case covid19Case) {
		this.covid19CaseId = covid19Case.getCovid19CaseId();
		this.patientId = covid19Case.getPatientId();
		this.caseId = covid19Case.getCaseId();
		this.pepfarId = covid19Case.getPepfarId();
		this.gender = covid19Case.getGender();
		this.dateOfBirth = covid19Case.getDateOfBirth();
		this.hivDiagnosisDate = covid19Case.getHivDiagnosisDate();
		this.artStartDate = covid19Case.getArtStartDate();
		this.caseStatus = covid19Case.getCaseStatus();
		this.caseStatusDate = covid19Case.getCaseStatusDate();
		this.poeScreened = covid19Case.getPoeScreened();
		this.onAdmission = covid19Case.getOnAdmission();
		this.statusDA = covid19Case.getStatusDA();
		this.occupation = covid19Case.getOccupation();
		this.typRespiratory = covid19Case.getTypRespiratory();
		this.typBaseSerum = covid19Case.getTypBaseSerum();
		this.typOthers = covid19Case.getTypOthers();
		this.sympAsymp = covid19Case.getSympAsymp();
	}
	
	public Integer getCovid19CaseId() {
		return covid19CaseId;
	}
	
	public void setCovid19CaseId(Integer covid19CaseId) {
		this.covid19CaseId = covid19CaseId;
	}
	
	public Integer getPatientId() {
		return patientId;
	}
	
	public void setPatientId(Integer patientId) {
		this.patientId = patientId;
	}
	
	public String getCaseId() {
		return caseId;
	}
	
	public void setCaseId(String caseId) {
		this.caseId = caseId;
	}
	
	public String getPepfarId() {
		return pepfarId;
	}
	
	public void setPepfarId(String pepfarId) {
		this.pepfarId = pepfarId;
	}
	
	public String getGender() {
		return gender;
	}
	
	public void setGender(String gender) {
		this.gender = gender;
	}
	
	public Date getDateOfBirth() {
		return dateOfBirth;
	}
	
	public void setDateOfBirth(Date dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}
	
	public Date getHivDiagnosisDate() {
		return hivDiagnosisDate;
	}
	
	public void setHivDiagnosisDate(Date hivDiagnosisDate) {
		this.hivDiagnosisDate = hivDiagnosisDate;
	}
	
	public Date getArtStartDate() {
		return artStartDate;
	}
	
	public void setArtStartDate(Date artStartDate) {
		this.artStartDate = artStartDate;
	}
	
	public Integer getCaseStatus() {
		return caseStatus;
	}
	
	public void setCaseStatus(Integer caseStatus) {
		this.caseStatus = caseStatus;
	}
	
	public Date getCaseStatusDate() {
		return caseStatusDate;
	}
	
	public void setCaseStatusDate(Date caseStatusDate) {
		this.caseStatusDate = caseStatusDate;
	}
	
	public Integer getPoeScreened() {
		return poeScreened;
	}
	
	public void setPoeScreened(Integer poeScreened) {
		this.poeScreened = poeScreened;
	}
	
	public Integer getOnAdmission() {
		return onAdmission;
	}
	
	public void setOnAdmission(Integer onAdmission) {
		this.onAdmission = onAdmission;
	}
	
	public Integer getStatusDA() {
		return statusDA;
	}
	
	public void setStatusDA(Integer statusDA) {
		this.statusDA = statusDA;
	}
	
	public Integer getOccupation() {
		return occupation;
	}
	
	public void setOccupation(Integer occupation) {
		this.occupation = occupation;
	}
	
	public Integer getTypRespiratory() {
		return typRespiratory;
	}
	
	public void setTypRespiratory(Integer typRespiratory) {
		this.typRespiratory = typRespiratory;
	}
	
	public Integer getTypBaseSerum() {
		return typBaseSerum;
	}
	
	public void setTypBaseSerum(Integer typBaseSerum) {
		this.typBaseSerum = typBaseSerum;
	}
	
	public Integer getTypOthers() {
		return typOthers;
	}
	
	public void setTypOthers(Integer typOthers) {
		this.typOthers = typOthers;
	}
	
	public Integer getSympAsymp() {
		return sympAsymp;
	}
	
	public void setSympAsymp(Integer sympAsymp) {
		this.sympAsymp = sympAsymp;
	}
	
	public String getPatientUuid() {
		return patientUuid;
	}
	
	public void setPatientUuid(String patientUuid) {
		this.patientUuid = patientUuid;
	}
	
	public String getDatimCode() {
		return datimCode;
	}
	
	public void setDatimCode(String datimCode) {
		this.datimCode = datimCode;
	}
}
