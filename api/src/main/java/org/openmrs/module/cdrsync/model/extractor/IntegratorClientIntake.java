package org.openmrs.module.cdrsync.model.extractor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "integrator_client_intake")
public class IntegratorClientIntake {
	
	@Id
	@Column(name = "client_intake_id")
	private Integer clientIntakeId;
	
	@Column(name = "patient_id")
	private Integer patientId;
	
	@Column(name = "patient_identifier")
	private String patientIdentifier;
	
	@Column(name = "gender")
	private String gender;
	
	@Column(name = "phone_number")
	private String phoneNumber;
	
	@Column(name = "patient_name")
	private String patientName;
	
	@Column(name = "family_name")
	private String familyName;
	
	@Column(name = "age")
	private Integer age;
	
	@Column(name = "address")
	private String address;
	
	@Column(name = "state")
	private String state;
	
	@Column(name = "lga")
	private String lga;
	
	@Column(name = "encounter_date")
	private Date encounterDate;
	
	@Column(name = "service_area_id")
	private Integer serviceAreaId;
	
	@Column(name = "service_area_name")
	private String serviceAreaName;
	
	@Column(name = "hiv_blood_transfussion")
	private Integer hivBloodTransfusion;
	
	@Column(name = "hiv_unprotected")
	private Integer hivUnprotected;
	
	@Column(name = "hiv_sti")
	private Integer hivSti;
	
	@Column(name = "hiv_diagnosed")
	private Integer hivDiagnosed;
	
	@Column(name = "hiv_iv_drug")
	private Integer hivIvDrug;
	
	@Column(name = "hiv_forced")
	private Integer hivForced;
	
	@Column(name = "sti_male_genital")
	private Integer stiMaleGenital;
	
	@Column(name = "sti_male_scrotal")
	private Integer stiMaleScrotal;
	
	@Column(name = "sti_male_urethral")
	private Integer stiMaleUrethral;
	
	@Column(name = "sti_female_genital")
	private Integer stiFemaleGenital;
	
	@Column(name = "sti_female_abdominal")
	private Integer stiFemaleAbdominal;
	
	@Column(name = "sti_female_vaginal")
	private Integer stiFemaleVaginal;
	
	@Column(name = "hiv_score")
	private Integer hivScore;
	
	@Column(name = "tb_cough_two_week")
	private Integer tbCoughTwoWeek;
	
	@Column(name = "tb_fever")
	private Integer tbFever;
	
	@Column(name = "tb_cough_heamoptysis")
	private Integer tbCoughHeamoptysis;
	
	@Column(name = "tb_cough_unexplained_weightloss")
	private Integer tbCoughUnexplainedWeightLoss;
	
	@Column(name = "tb_unexplained_weightloss")
	private Integer tbUnexplainedWeightLoss;
	
	@Column(name = "tb_weight_loss")
	private Integer tbWeightLoss;
	
	@Column(name = "tb_night_sweat")
	private Integer tbNightSweat;
	
	@Column(name = "tb_cough_weightloss")
	private Integer tbCoughWeightLoss;
	
	@Column(name = "tb_cough_fever")
	private Integer tbCoughFever;
	
	@Column(name = "tb_cough_sputum")
	private Integer tbCoughSputum;
	
	@Column(name = "tb_score")
	private Integer tbScore;
	
	@Column(name = "covid_temperature")
	private Double covidTemperature;
	
	@Column(name = "covid_dry_cough")
	private Integer covidDryCough;
	
	@Column(name = "covid_fever")
	private Integer covidFever;
	
	@Column(name = "covid_not_vaccinated")
	private Integer covidNotVaccinated;
	
	@Column(name = "covid_loss_smell")
	private Integer covidLossSmell;
	
	@Column(name = "covid_headache")
	private Integer covidHeadache;
	
	@Column(name = "covid_close_contact")
	private Integer covidCloseContact;
	
	@Column(name = "covid_health_care")
	private Integer covidHealthCare;
	
	@Column(name = "covid_shortness_breath")
	private Integer covidShortnessBreath;
	
	@Column(name = "covid_muscle_ache")
	private Integer covidMuscleAche;
	
	@Column(name = "covid_loss_taste")
	private Integer covidLossTaste;
	
	@Column(name = "covid_sore_throat")
	private Integer covidSoreThroat;
	
	@Column(name = "covid_travel")
	private Integer covidTravel;
	
	@Column(name = "covid_chronic_ncd")
	private Integer covidChronicNcd;
	
	@Column(name = "covid_score")
	private Integer covidScore;
	
	@Column(name = "ncd_hypertensive")
	private Integer ncdHypertensive;
	
	@Column(name = "ncd_htn_medication")
	private Integer ncdHtnMedication;
	
	@Column(name = "ncd_bp_upper")
	private Integer ncdBpUpper;
	
	@Column(name = "ncd_bp_lower")
	private Integer ncdBpLower;
	
	@Column(name = "ncd_diabetic")
	private Integer ncdDiabetic;
	
	@Column(name = "ncd_dm_medication")
	private Integer ncdDmMedication;
	
	@Column(name = "ncd_rbs")
	private Integer ncdRbs;
	
	@Column(name = "ncd_score")
	private Integer ncdScore;
	
	@Column(name = "tb_test")
	private Integer tbTest;
	
	@Column(name = "covid_test")
	private Integer covidTest;
	
	@Column(name = "hiv_test")
	private Integer hivTest;
	
	@Column(name = "ncd_test")
	private Integer ncdTest;
	
	@Column(name = "date_created")
	private Date dateCreated;
	
	@Column(name = "bmi_weight")
	private Double bmiWeight;
	
	@Column(name = "bmi_height")
	private Double bmiHeight;
	
	@Column(name = "bmi_value")
	private Double bmiValue;
	
	@Column(name = "bmi_remark")
	private String bmiRemark;
	
	@Column(name = "covid_have_you_been_vaccinated")
	private Integer covidHaveYouBeenVaccinated;
	
	@Column(name = "covid_vaccination_dose")
	private Integer covidVaccinationDose;
	
	@Column(name = "covid_name_of_vaccine")
	private String covidNameOfVaccine;
	
	@Column(name = "covid_date_of_vaccination")
	private Date covidDateOfVaccination;
	
	@Column(name = "cervical_ever_cervical")
	private Integer cervicalEverCervical;
	
	@Column(name = "cervical_cervical")
	private String cervicalCervical;
	
	@Column(name = "ncd_other")
	private String ncdOther;
	
	@Column(name = "ncd_comment")
	private String ncdComment;
	
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
}
