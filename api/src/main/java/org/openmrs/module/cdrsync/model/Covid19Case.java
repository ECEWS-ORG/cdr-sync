package org.openmrs.module.cdrsync.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "cv19_case_inv")
public class Covid19Case {
	
	@Id
	@Column(name = "cv19_case_id")
	private Integer covid19CaseId;
	
	@Column(name = "patient_id")
	private Integer patientId;
	
	@Column(name = "case_id")
	private String caseId;
	
	@Column(name = "pepfar_id")
	private String pepfarId;
	
	@Column(name = "gender")
	private String gender;
	
	@Column(name = "dob")
	private Date dateOfBirth;
	
	@Column(name = "hiv_diagnosis_date")
	private Date hivDiagnosisDate;
	
	@Column(name = "art_start_date")
	private Date artStartDate;
	
	@Column(name = "case_status")
	private Integer caseStatus;
	
	@Column(name = "case_status_date")
	private Date caseStatusDate;
	
	@Column(name = "poe_screened")
	private Integer poeScreened;
	
	@Column(name = "on_admission")
	private Integer onAdmission;
	
	@Column(name = "status_d_a")
	private Integer statusDA;
	
	@Column(name = "occupation")
	private Integer occupation;
	
	@Column(name = "typ_respiratory")
	private Integer typRespiratory;
	
	@Column(name = "typ_base_serum")
	private Integer typBaseSerum;
	
	@Column(name = "typ_others")
	private Integer typOthers;
	
	@Column(name = "symp_asymp")
	private Integer sympAsymp;
	
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
}
