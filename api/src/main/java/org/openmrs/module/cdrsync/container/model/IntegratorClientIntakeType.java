package org.openmrs.module.cdrsync.container.model;

import org.openmrs.module.cdrsync.model.IntegratorClientIntake;
import org.openmrs.util.Security;

import java.io.Serializable;
import java.util.Date;

public class IntegratorClientIntakeType implements Serializable {
	
	private Integer clientIntakeId;
	
	private Integer patientId;
	
	private String patientIdentifier;
	
	private String gender;
	
	private String phoneNumber;
	
	private String patientName;
	
	private String familyName;
	
	private Integer age;
	
	private String address;
	
	private String state;
	
	private String lga;
	
	private Date encounterDate;
	
	private Integer serviceAreaId;
	
	private String serviceAreaName;
	
	private Integer hivBloodTransfusion;
	
	private Integer hivUnprotected;
	
	private Integer hivSti;
	
	private Integer hivDiagnosed;
	
	private Integer hivIvDrug;
	
	private Integer hivForced;
	
	private Integer stiMaleGenital;
	
	private Integer stiMaleScrotal;
	
	private Integer stiMaleUrethral;
	
	private Integer stiFemaleGenital;
	
	private Integer stiFemaleAbdominal;
	
	private Integer stiFemaleVaginal;
	
	private Integer hivScore;
	
	private Integer tbCoughTwoWeek;
	
	private Integer tbFever;
	
	private Integer tbCoughHeamoptysis;
	
	private Integer tbCoughUnexplainedWeightLoss;
	
	private Integer tbUnexplainedWeightLoss;
	
	private Integer tbWeightLoss;
	
	private Integer tbNightSweat;
	
	private Integer tbCoughWeightLoss;
	
	private Integer tbCoughFever;
	
	private Integer tbCoughSputum;
	
	private Integer tbScore;
	
	private Double covidTemperature;
	
	private Integer covidDryCough;
	
	private Integer covidFever;
	
	private Integer covidNotVaccinated;
	
	private Integer covidLossSmell;
	
	private Integer covidHeadache;
	
	private Integer covidCloseContact;
	
	private Integer covidHealthCare;
	
	private Integer covidShortnessBreath;
	
	private Integer covidMuscleAche;
	
	private Integer covidLossTaste;
	
	private Integer covidSoreThroat;
	
	private Integer covidTravel;
	
	private Integer covidChronicNcd;
	
	private Integer covidScore;
	
	private Integer ncdHypertensive;
	
	private Integer ncdHtnMedication;
	
	private Integer ncdBpUpper;
	
	private Integer ncdBpLower;
	
	private Integer ncdDiabetic;
	
	private Integer ncdDmMedication;
	
	private Integer ncdRbs;
	
	private Integer ncdScore;
	
	private Integer tbTest;
	
	private Integer covidTest;
	
	private Integer hivTest;
	
	private Integer ncdTest;
	
	private Date dateCreated;
	
	private Double bmiWeight;
	
	private Double bmiHeight;
	
	private Double bmiValue;
	
	private String bmiRemark;
	
	private Integer covidHaveYouBeenVaccinated;
	
	private Integer covidVaccinationDose;
	
	private String covidNameOfVaccine;
	
	private Date covidDateOfVaccination;
	
	private Integer cervicalEverCervical;
	
	private String cervicalCervical;
	
	private String ncdOther;
	
	private String ncdComment;
	
	public IntegratorClientIntakeType(IntegratorClientIntake integratorClientIntake) {
		this.clientIntakeId = integratorClientIntake.getClientIntakeId();
		this.patientId = integratorClientIntake.getPatientId();
		this.patientIdentifier = integratorClientIntake.getPatientIdentifier();
		this.gender = integratorClientIntake.getGender();
		this.phoneNumber = integratorClientIntake.getPhoneNumber() != null ? Security.encrypt(integratorClientIntake
		        .getPhoneNumber()) : null;
		this.patientName = integratorClientIntake.getPatientName() != null ? Security.encrypt(integratorClientIntake
		        .getPatientName()) : null;
		this.familyName = integratorClientIntake.getFamilyName() != null ? Security.encrypt(integratorClientIntake
		        .getFamilyName()) : null;
		this.age = integratorClientIntake.getAge();
		this.address = integratorClientIntake.getAddress() != null ? Security.encrypt(integratorClientIntake.getAddress())
		        : null;
		this.state = integratorClientIntake.getState();
		this.lga = integratorClientIntake.getLga();
		this.encounterDate = integratorClientIntake.getEncounterDate();
		this.serviceAreaId = integratorClientIntake.getServiceAreaId();
		this.serviceAreaName = integratorClientIntake.getServiceAreaName();
		this.hivBloodTransfusion = integratorClientIntake.getHivBloodTransfusion();
		this.hivUnprotected = integratorClientIntake.getHivUnprotected();
		this.hivSti = integratorClientIntake.getHivSti();
		this.hivDiagnosed = integratorClientIntake.getHivDiagnosed();
		this.hivIvDrug = integratorClientIntake.getHivIvDrug();
		this.hivForced = integratorClientIntake.getHivForced();
		this.stiMaleGenital = integratorClientIntake.getStiMaleGenital();
		this.stiMaleScrotal = integratorClientIntake.getStiMaleScrotal();
		this.stiMaleUrethral = integratorClientIntake.getStiMaleUrethral();
		this.stiFemaleGenital = integratorClientIntake.getStiFemaleGenital();
		this.stiFemaleAbdominal = integratorClientIntake.getStiFemaleAbdominal();
		this.stiFemaleVaginal = integratorClientIntake.getStiFemaleVaginal();
		this.hivScore = integratorClientIntake.getHivScore();
		this.tbCoughTwoWeek = integratorClientIntake.getTbCoughTwoWeek();
		this.tbFever = integratorClientIntake.getTbFever();
		this.tbCoughHeamoptysis = integratorClientIntake.getTbCoughHeamoptysis();
		this.tbCoughUnexplainedWeightLoss = integratorClientIntake.getTbCoughUnexplainedWeightLoss();
		this.tbUnexplainedWeightLoss = integratorClientIntake.getTbUnexplainedWeightLoss();
		this.tbWeightLoss = integratorClientIntake.getTbWeightLoss();
		this.tbNightSweat = integratorClientIntake.getTbNightSweat();
		this.tbCoughWeightLoss = integratorClientIntake.getTbCoughWeightLoss();
		this.tbCoughFever = integratorClientIntake.getTbCoughFever();
		this.tbCoughSputum = integratorClientIntake.getTbCoughSputum();
		this.tbScore = integratorClientIntake.getTbScore();
		this.covidTemperature = integratorClientIntake.getCovidTemperature();
		this.covidDryCough = integratorClientIntake.getCovidDryCough();
		this.covidFever = integratorClientIntake.getCovidFever();
		this.covidNotVaccinated = integratorClientIntake.getCovidNotVaccinated();
		this.covidLossSmell = integratorClientIntake.getCovidLossSmell();
		this.covidHeadache = integratorClientIntake.getCovidHeadache();
		this.covidCloseContact = integratorClientIntake.getCovidCloseContact();
		this.covidHealthCare = integratorClientIntake.getCovidHealthCare();
		this.covidShortnessBreath = integratorClientIntake.getCovidShortnessBreath();
		this.covidMuscleAche = integratorClientIntake.getCovidMuscleAche();
		this.covidLossTaste = integratorClientIntake.getCovidLossTaste();
		this.covidSoreThroat = integratorClientIntake.getCovidSoreThroat();
		this.covidTravel = integratorClientIntake.getCovidTravel();
		this.covidChronicNcd = integratorClientIntake.getCovidChronicNcd();
		this.covidScore = integratorClientIntake.getCovidScore();
		this.ncdHypertensive = integratorClientIntake.getNcdHypertensive();
		this.ncdHtnMedication = integratorClientIntake.getNcdHtnMedication();
		this.ncdBpUpper = integratorClientIntake.getNcdBpUpper();
		this.ncdBpLower = integratorClientIntake.getNcdBpLower();
		this.ncdDiabetic = integratorClientIntake.getNcdDiabetic();
		this.ncdDmMedication = integratorClientIntake.getNcdDmMedication();
		this.ncdRbs = integratorClientIntake.getNcdRbs();
		this.ncdScore = integratorClientIntake.getNcdScore();
		this.tbTest = integratorClientIntake.getTbTest();
		this.covidTest = integratorClientIntake.getCovidTest();
		this.hivTest = integratorClientIntake.getHivTest();
		this.ncdTest = integratorClientIntake.getNcdTest();
		this.dateCreated = integratorClientIntake.getDateCreated();
		this.bmiWeight = integratorClientIntake.getBmiWeight();
		this.bmiHeight = integratorClientIntake.getBmiHeight();
		this.bmiValue = integratorClientIntake.getBmiValue();
		this.bmiRemark = integratorClientIntake.getBmiRemark();
		this.covidHaveYouBeenVaccinated = integratorClientIntake.getCovidHaveYouBeenVaccinated();
		this.covidVaccinationDose = integratorClientIntake.getCovidVaccinationDose();
		this.covidNameOfVaccine = integratorClientIntake.getCovidNameOfVaccine();
		this.covidDateOfVaccination = integratorClientIntake.getCovidDateOfVaccination();
		this.cervicalEverCervical = integratorClientIntake.getCervicalEverCervical();
		this.cervicalCervical = integratorClientIntake.getCervicalCervical();
		this.ncdOther = integratorClientIntake.getNcdOther();
		this.ncdComment = integratorClientIntake.getNcdComment();
	}
	
	public Integer getClientIntakeId() {
		return clientIntakeId;
	}
	
	public Integer getPatientId() {
		return patientId;
	}
	
	public String getPatientIdentifier() {
		return patientIdentifier;
	}
	
	public String getGender() {
		return gender;
	}
	
	public String getPhoneNumber() {
		return phoneNumber;
	}
	
	public String getPatientName() {
		return patientName;
	}
	
	public String getFamilyName() {
		return familyName;
	}
	
	public Integer getAge() {
		return age;
	}
	
	public String getAddress() {
		return address;
	}
	
	public String getState() {
		return state;
	}
	
	public String getLga() {
		return lga;
	}
	
	public Date getEncounterDate() {
		return encounterDate;
	}
	
	public Integer getServiceAreaId() {
		return serviceAreaId;
	}
	
	public String getServiceAreaName() {
		return serviceAreaName;
	}
	
	public Integer getHivBloodTransfusion() {
		return hivBloodTransfusion;
	}
	
	public Integer getHivUnprotected() {
		return hivUnprotected;
	}
	
	public Integer getHivSti() {
		return hivSti;
	}
	
	public Integer getHivDiagnosed() {
		return hivDiagnosed;
	}
	
	public Integer getHivIvDrug() {
		return hivIvDrug;
	}
	
	public Integer getHivForced() {
		return hivForced;
	}
	
	public Integer getStiMaleGenital() {
		return stiMaleGenital;
	}
	
	public Integer getStiMaleScrotal() {
		return stiMaleScrotal;
	}
	
	public Integer getStiMaleUrethral() {
		return stiMaleUrethral;
	}
	
	public Integer getStiFemaleGenital() {
		return stiFemaleGenital;
	}
	
	public Integer getStiFemaleAbdominal() {
		return stiFemaleAbdominal;
	}
	
	public Integer getStiFemaleVaginal() {
		return stiFemaleVaginal;
	}
	
	public Integer getHivScore() {
		return hivScore;
	}
	
	public Integer getTbCoughTwoWeek() {
		return tbCoughTwoWeek;
	}
	
	public Integer getTbFever() {
		return tbFever;
	}
	
	public Integer getTbCoughHeamoptysis() {
		return tbCoughHeamoptysis;
	}
	
	public Integer getTbCoughUnexplainedWeightLoss() {
		return tbCoughUnexplainedWeightLoss;
	}
	
	public Integer getTbUnexplainedWeightLoss() {
		return tbUnexplainedWeightLoss;
	}
	
	public Integer getTbWeightLoss() {
		return tbWeightLoss;
	}
	
	public Integer getTbNightSweat() {
		return tbNightSweat;
	}
	
	public Integer getTbCoughWeightLoss() {
		return tbCoughWeightLoss;
	}
	
	public Integer getTbCoughFever() {
		return tbCoughFever;
	}
	
	public Integer getTbCoughSputum() {
		return tbCoughSputum;
	}
	
	public Integer getTbScore() {
		return tbScore;
	}
	
	public Double getCovidTemperature() {
		return covidTemperature;
	}
	
	public Integer getCovidDryCough() {
		return covidDryCough;
	}
	
	public Integer getCovidFever() {
		return covidFever;
	}
	
	public Integer getCovidNotVaccinated() {
		return covidNotVaccinated;
	}
	
	public Integer getCovidLossSmell() {
		return covidLossSmell;
	}
	
	public Integer getCovidHeadache() {
		return covidHeadache;
	}
	
	public Integer getCovidCloseContact() {
		return covidCloseContact;
	}
	
	public Integer getCovidHealthCare() {
		return covidHealthCare;
	}
	
	public Integer getCovidShortnessBreath() {
		return covidShortnessBreath;
	}
	
	public Integer getCovidMuscleAche() {
		return covidMuscleAche;
	}
	
	public Integer getCovidLossTaste() {
		return covidLossTaste;
	}
	
	public Integer getCovidSoreThroat() {
		return covidSoreThroat;
	}
	
	public Integer getCovidTravel() {
		return covidTravel;
	}
	
	public Integer getCovidChronicNcd() {
		return covidChronicNcd;
	}
	
	public Integer getCovidScore() {
		return covidScore;
	}
	
	public Integer getNcdHypertensive() {
		return ncdHypertensive;
	}
	
	public Integer getNcdHtnMedication() {
		return ncdHtnMedication;
	}
	
	public Integer getNcdBpUpper() {
		return ncdBpUpper;
	}
	
	public Integer getNcdBpLower() {
		return ncdBpLower;
	}
	
	public Integer getNcdDiabetic() {
		return ncdDiabetic;
	}
	
	public Integer getNcdDmMedication() {
		return ncdDmMedication;
	}
	
	public Integer getNcdRbs() {
		return ncdRbs;
	}
	
	public Integer getNcdScore() {
		return ncdScore;
	}
	
	public Integer getTbTest() {
		return tbTest;
	}
	
	public Integer getCovidTest() {
		return covidTest;
	}
	
	public Integer getHivTest() {
		return hivTest;
	}
	
	public Integer getNcdTest() {
		return ncdTest;
	}
	
	public Date getDateCreated() {
		return dateCreated;
	}
	
	public Double getBmiWeight() {
		return bmiWeight;
	}
	
	public Double getBmiHeight() {
		return bmiHeight;
	}
	
	public Double getBmiValue() {
		return bmiValue;
	}
	
	public String getBmiRemark() {
		return bmiRemark;
	}
	
	public Integer getCovidHaveYouBeenVaccinated() {
		return covidHaveYouBeenVaccinated;
	}
	
	public Integer getCovidVaccinationDose() {
		return covidVaccinationDose;
	}
	
	public String getCovidNameOfVaccine() {
		return covidNameOfVaccine;
	}
	
	public Date getCovidDateOfVaccination() {
		return covidDateOfVaccination;
	}
	
	public Integer getCervicalEverCervical() {
		return cervicalEverCervical;
	}
	
	public String getCervicalCervical() {
		return cervicalCervical;
	}
	
	public String getNcdOther() {
		return ncdOther;
	}
	
	public String getNcdComment() {
		return ncdComment;
	}
	
	public void setClientIntakeId(Integer clientIntakeId) {
		this.clientIntakeId = clientIntakeId;
	}
	
	public void setPatientId(Integer patientId) {
		this.patientId = patientId;
	}
	
	public void setPatientIdentifier(String patientIdentifier) {
		this.patientIdentifier = patientIdentifier;
	}
	
	public void setGender(String gender) {
		this.gender = gender;
	}
	
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	
	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}
	
	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}
	
	public void setAge(Integer age) {
		this.age = age;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public void setState(String state) {
		this.state = state;
	}
	
	public void setLga(String lga) {
		this.lga = lga;
	}
	
	public void setEncounterDate(Date encounterDate) {
		this.encounterDate = encounterDate;
	}
	
	public void setServiceAreaId(Integer serviceAreaId) {
		this.serviceAreaId = serviceAreaId;
	}
	
	public void setServiceAreaName(String serviceAreaName) {
		this.serviceAreaName = serviceAreaName;
	}
	
	public void setHivBloodTransfusion(Integer hivBloodTransfusion) {
		this.hivBloodTransfusion = hivBloodTransfusion;
	}
	
	public void setHivUnprotected(Integer hivUnprotected) {
		this.hivUnprotected = hivUnprotected;
	}
	
	public void setHivSti(Integer hivSti) {
		this.hivSti = hivSti;
	}
	
	public void setHivDiagnosed(Integer hivDiagnosed) {
		this.hivDiagnosed = hivDiagnosed;
	}
	
	public void setHivIvDrug(Integer hivIvDrug) {
		this.hivIvDrug = hivIvDrug;
	}
	
	public void setHivForced(Integer hivForced) {
		this.hivForced = hivForced;
	}
	
	public void setStiMaleGenital(Integer stiMaleGenital) {
		this.stiMaleGenital = stiMaleGenital;
	}
	
	public void setStiMaleScrotal(Integer stiMaleScrotal) {
		this.stiMaleScrotal = stiMaleScrotal;
	}
	
	public void setStiMaleUrethral(Integer stiMaleUrethral) {
		this.stiMaleUrethral = stiMaleUrethral;
	}
	
	public void setStiFemaleGenital(Integer stiFemaleGenital) {
		this.stiFemaleGenital = stiFemaleGenital;
	}
	
	public void setStiFemaleAbdominal(Integer stiFemaleAbdominal) {
		this.stiFemaleAbdominal = stiFemaleAbdominal;
	}
	
	public void setStiFemaleVaginal(Integer stiFemaleVaginal) {
		this.stiFemaleVaginal = stiFemaleVaginal;
	}
	
	public void setHivScore(Integer hivScore) {
		this.hivScore = hivScore;
	}
	
	public void setTbCoughTwoWeek(Integer tbCoughTwoWeek) {
		this.tbCoughTwoWeek = tbCoughTwoWeek;
	}
	
	public void setTbFever(Integer tbFever) {
		this.tbFever = tbFever;
	}
	
	public void setTbCoughHeamoptysis(Integer tbCoughHeamoptysis) {
		this.tbCoughHeamoptysis = tbCoughHeamoptysis;
	}
	
	public void setTbCoughUnexplainedWeightLoss(Integer tbCoughUnexplainedWeightLoss) {
		this.tbCoughUnexplainedWeightLoss = tbCoughUnexplainedWeightLoss;
	}
	
	public void setTbUnexplainedWeightLoss(Integer tbUnexplainedWeightLoss) {
		this.tbUnexplainedWeightLoss = tbUnexplainedWeightLoss;
	}
	
	public void setTbWeightLoss(Integer tbWeightLoss) {
		this.tbWeightLoss = tbWeightLoss;
	}
	
	public void setTbNightSweat(Integer tbNightSweat) {
		this.tbNightSweat = tbNightSweat;
	}
	
	public void setTbCoughWeightLoss(Integer tbCoughWeightLoss) {
		this.tbCoughWeightLoss = tbCoughWeightLoss;
	}
	
	public void setTbCoughFever(Integer tbCoughFever) {
		this.tbCoughFever = tbCoughFever;
	}
	
	public void setTbCoughSputum(Integer tbCoughSputum) {
		this.tbCoughSputum = tbCoughSputum;
	}
	
	public void setTbScore(Integer tbScore) {
		this.tbScore = tbScore;
	}
	
	public void setCovidTemperature(Double covidTemperature) {
		this.covidTemperature = covidTemperature;
	}
	
	public void setCovidDryCough(Integer covidDryCough) {
		this.covidDryCough = covidDryCough;
	}
	
	public void setCovidFever(Integer covidFever) {
		this.covidFever = covidFever;
	}
	
	public void setCovidNotVaccinated(Integer covidNotVaccinated) {
		this.covidNotVaccinated = covidNotVaccinated;
	}
	
	public void setCovidLossSmell(Integer covidLossSmell) {
		this.covidLossSmell = covidLossSmell;
	}
	
	public void setCovidHeadache(Integer covidHeadache) {
		this.covidHeadache = covidHeadache;
	}
	
	public void setCovidCloseContact(Integer covidCloseContact) {
		this.covidCloseContact = covidCloseContact;
	}
	
	public void setCovidHealthCare(Integer covidHealthCare) {
		this.covidHealthCare = covidHealthCare;
	}
	
	public void setCovidShortnessBreath(Integer covidShortnessBreath) {
		this.covidShortnessBreath = covidShortnessBreath;
	}
	
	public void setCovidMuscleAche(Integer covidMuscleAche) {
		this.covidMuscleAche = covidMuscleAche;
	}
	
	public void setCovidLossTaste(Integer covidLossTaste) {
		this.covidLossTaste = covidLossTaste;
	}
	
	public void setCovidSoreThroat(Integer covidSoreThroat) {
		this.covidSoreThroat = covidSoreThroat;
	}
	
	public void setCovidTravel(Integer covidTravel) {
		this.covidTravel = covidTravel;
	}
	
	public void setCovidChronicNcd(Integer covidChronicNcd) {
		this.covidChronicNcd = covidChronicNcd;
	}
	
	public void setCovidScore(Integer covidScore) {
		this.covidScore = covidScore;
	}
	
	public void setNcdHypertensive(Integer ncdHypertensive) {
		this.ncdHypertensive = ncdHypertensive;
	}
	
	public void setNcdHtnMedication(Integer ncdHtnMedication) {
		this.ncdHtnMedication = ncdHtnMedication;
	}
	
	public void setNcdBpUpper(Integer ncdBpUpper) {
		this.ncdBpUpper = ncdBpUpper;
	}
	
	public void setNcdBpLower(Integer ncdBpLower) {
		this.ncdBpLower = ncdBpLower;
	}
	
	public void setNcdDiabetic(Integer ncdDiabetic) {
		this.ncdDiabetic = ncdDiabetic;
	}
	
	public void setNcdDmMedication(Integer ncdDmMedication) {
		this.ncdDmMedication = ncdDmMedication;
	}
	
	public void setNcdRbs(Integer ncdRbs) {
		this.ncdRbs = ncdRbs;
	}
	
	public void setNcdScore(Integer ncdScore) {
		this.ncdScore = ncdScore;
	}
	
	public void setTbTest(Integer tbTest) {
		this.tbTest = tbTest;
	}
	
	public void setCovidTest(Integer covidTest) {
		this.covidTest = covidTest;
	}
	
	public void setHivTest(Integer hivTest) {
		this.hivTest = hivTest;
	}
	
	public void setNcdTest(Integer ncdTest) {
		this.ncdTest = ncdTest;
	}
	
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	public void setBmiWeight(Double bmiWeight) {
		this.bmiWeight = bmiWeight;
	}
	
	public void setBmiHeight(Double bmiHeight) {
		this.bmiHeight = bmiHeight;
	}
	
	public void setBmiValue(Double bmiValue) {
		this.bmiValue = bmiValue;
	}
	
	public void setBmiRemark(String bmiRemark) {
		this.bmiRemark = bmiRemark;
	}
	
	public void setCovidHaveYouBeenVaccinated(Integer covidHaveYouBeenVaccinated) {
		this.covidHaveYouBeenVaccinated = covidHaveYouBeenVaccinated;
	}
	
	public void setCovidVaccinationDose(Integer covidVaccinationDose) {
		this.covidVaccinationDose = covidVaccinationDose;
	}
	
	public void setCovidNameOfVaccine(String covidNameOfVaccine) {
		this.covidNameOfVaccine = covidNameOfVaccine;
	}
	
	public void setCovidDateOfVaccination(Date covidDateOfVaccination) {
		this.covidDateOfVaccination = covidDateOfVaccination;
	}
	
	public void setCervicalEverCervical(Integer cervicalEverCervical) {
		this.cervicalEverCervical = cervicalEverCervical;
	}
	
	public void setCervicalCervical(String cervicalCervical) {
		this.cervicalCervical = cervicalCervical;
	}
	
	public void setNcdOther(String ncdOther) {
		this.ncdOther = ncdOther;
	}
	
	public void setNcdComment(String ncdComment) {
		this.ncdComment = ncdComment;
	}
}
