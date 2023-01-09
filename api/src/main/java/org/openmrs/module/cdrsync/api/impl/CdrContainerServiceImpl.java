package org.openmrs.module.cdrsync.api.impl;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.*;
import org.openmrs.api.*;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.cdrsync.api.BiometricInfoService;
import org.openmrs.module.cdrsync.api.CdrContainerService;
import org.openmrs.module.cdrsync.api.CdrSyncAdminService;
import org.openmrs.module.cdrsync.api.CdrSyncEncounterService;
import org.openmrs.module.cdrsync.container.model.EncounterType;
import org.openmrs.module.cdrsync.container.model.PatientIdentifierType;
import org.openmrs.module.cdrsync.container.model.VisitType;
import org.openmrs.module.cdrsync.container.model.*;
import org.openmrs.module.cdrsync.model.BiometricInfo;
import org.openmrs.module.cdrsync.model.ContainerWrapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.openmrs.module.cdrsync.utils.AppUtils.encrypt;

public class CdrContainerServiceImpl extends BaseOpenmrsService implements CdrContainerService {
	
	private final PatientService patientService = Context.getPatientService();
	
	private final VisitService visitService = Context.getVisitService();
	
	private final ProgramWorkflowService programWorkflowService = Context.getProgramWorkflowService();
	
	private final ObsService obsService = Context.getObsService();
	
	private final EncounterService encounterService = Context.getEncounterService();
	
	private final CdrSyncAdminService administrationService = Context.getService(CdrSyncAdminService.class);
	
	private final BiometricInfoService biometricInfoService = Context.getService(BiometricInfoService.class);
	
	private final CdrSyncEncounterService cdrSyncEncounterService = Context.getService(CdrSyncEncounterService.class);
	
	ObjectMapper objectMapper = new ObjectMapper();
	
	@Override
	public String getAllPatients() throws IOException {
		List<Patient> patients = patientService.getAllPatients(false);
		System.out.println("Total no of patients:: " + patients.size());
		List<Patient> newPatients = patients.subList(0, 4);
		System.out.println("---------" + newPatients.size());
		return buildContainer(newPatients);
	}
	
	@Override
	public String getPatientsByEncounterDateTime(Date from, Date to) {
        List<Encounter> encounters = cdrSyncEncounterService.getEncountersByEncounterDateTime(from, to);
        if (encounters != null && !encounters.isEmpty()) {
            System.out.println("No of encounters since last sync::"+encounters.size());
            List<Patient> patientList = encounters.stream()
                    .map(Encounter::getPatient)
                    .distinct()
                    .collect(Collectors.toList());
            System.out.println("No of patients that have encounters since last sync date::" + patientList.size());
            return buildContainer(patientList.subList(0,4));
        }
        return "No new encounter to sync";
	}
	
	private String buildContainer(List<Patient> patients) {
        List<Container> containers = new ArrayList<>();
        String datimCode = Context.getAdministrationService().getGlobalProperty("facility_datim_code");
        String facilityName = Context.getAdministrationService().getGlobalProperty("Facility_Name");
        String resp = "";
        AtomicInteger count = new AtomicInteger();
        try {
            patients.forEach(patient -> {
                System.out.println(count.getAndIncrement());
                Container container = new Container();
                container.setMessageHeader(buildMessageHeader(datimCode, facilityName));
                container.setMessageData(buildMessageData(patient, datimCode));
                container.setId(patient.getUuid());
//                container.getMessageHeader().setTouchTime();
                containers.add(container);
                if (containers.size() == 2) {
                    try {
                        syncContainersToCdr(containers);
                        containers.clear();
                    } catch (IOException e) {
                        containers.clear();
                        throw new RuntimeException(e);
                    }

                }
            });
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            resp = "Can't sync at the moment, try again later!";
        }
        if (resp.equals("Can't sync at the moment, try again later!")) {
            return resp;
        }
        if (!containers.isEmpty()) {
            try {
                syncContainersToCdr(containers);
                containers.clear();
            } catch (IOException e) {
                containers.clear();
                resp = "Incomplete syncing, try again later!";
                return resp;
            }
        }
        return "Syncing successful";
    }
	
	private void syncContainersToCdr(List<Container> containers) throws IOException {
		ContainerWrapper containerWrapper = new ContainerWrapper(containers);
		String url = "http://localhost:8484/sync-containers";
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()){
            HttpPost post = new HttpPost(url);
            post.setHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity(objectMapper.writeValueAsString(containerWrapper)));
            try (CloseableHttpResponse response = httpClient.execute(post)){
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    System.out.println("After successfully sending request::" + responseBody);
                } else
                    System.out.println("error sending request");
            }
        }
    }
	
	@Override
	public void saveLastSyncDate() {
		System.out.println("Saving last sync date-----");
		Date syncDate = new Date();
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy'T'hh:mm:ss");
		String syncDateString = dateFormat.format(syncDate);
		Context.getAdministrationService().updateGlobalProperty("last.cdr.sync", syncDateString);
		//		administrationService.updateLastSyncGlobalProperty("last.cdr.sync", syncDateString);
	}
	
	private MessageHeaderType buildMessageHeader(String datimCode, String facilityName) {
		MessageHeaderType messageHeaderType = new MessageHeaderType();
		messageHeaderType.setFacilityName(facilityName);
		messageHeaderType.setFacilityDatimCode(datimCode);
		messageHeaderType.setMessageCreationDateTime(new Date());
		messageHeaderType.setMessageSchemaVersion(new BigDecimal("1.0"));
		messageHeaderType.setMessageStatusCode("SYNCED");
		messageHeaderType.setMessageUniqueID(UUID.randomUUID().toString());
		messageHeaderType.setMessageSource("NMRS");
		//        messageHeaderType.setTouchTime(); todo confirm how to get the touch time from visit
		return messageHeaderType;
	}
	
	private MessageDataType buildMessageData(Patient patient, String datimCode) {
		MessageDataType messageDataType = new MessageDataType();
		messageDataType.setDemographics(buildDemographics(patient, datimCode));
		messageDataType.setVisits(buildVisits(patient, datimCode));
		messageDataType.setPatientBiometrics(buildPatientBiometrics(patient, datimCode));
		messageDataType.setPatientPrograms(buildPatientProgram(patient));
		messageDataType.setPatientIdentifiers(buildPatientIdentifier(patient));
		messageDataType.setEncounters(buildEncounters(patient, datimCode));
		messageDataType.setObs(buildObs(patient, datimCode));
		messageDataType.setEncounterProviders(buildEncounterProviders(patient));
		return messageDataType;
	}
	
	private DemographicsType buildDemographics(Patient patient, String datimCode) {
		DemographicsType demographicsType = new DemographicsType();
		PersonAddress personAddress = patient.getPersonAddress();
		if (personAddress.getAddress1() != null && !personAddress.getAddress1().isEmpty()) {
			demographicsType.setAddress1(encrypt(personAddress.getAddress1()));
		}
		if (personAddress.getAddress2() != null && !personAddress.getAddress2().isEmpty()) {
			demographicsType.setAddress2(encrypt(personAddress.getAddress2()));
		}
		demographicsType.setCityVillage(personAddress.getCityVillage());
		demographicsType.setStateProvince(personAddress.getStateProvince());
		demographicsType.setCountry(personAddress.getCountry());
		
		if (patient.getVoided()) {
			demographicsType.setVoided(1);
			demographicsType.setVoidedBy(patient.getVoidedBy().getId());
			demographicsType.setDateVoided(patient.getDateVoided());
			demographicsType.setVoidedReason(patient.getPersonVoidReason());
		}
		if (patient.getCreator() != null)
			demographicsType.setCreator(patient.getCreator().getId());
		if (patient.getDateCreated() != null)
			demographicsType.setDateCreated(patient.getDateCreated());
		
		demographicsType.setBirthdate(patient.getBirthdate());
		demographicsType.setBirthdateEstimated(patient.getBirthdateEstimated() ? 1 : 0);
		demographicsType.setChangedBy(patient.getChangedBy() != null ? patient.getChangedBy().getId() : 0);
		demographicsType.setDeathdateEstimated(patient.getDeathdateEstimated() ? 1 : 0);
		demographicsType.setDeathDate(patient.getDeathDate());
		demographicsType.setDead(patient.getDead() ? 1 : 0);
		//        if (patient.get)
		//        demographicsType.setCauseOfDeath(patient.getCauseOfDeath().); todo confirm how to get cause of death
		demographicsType.setFirstName(encrypt(patient.getGivenName() != null ? patient.getGivenName() : ""));
		demographicsType.setLastName(encrypt(patient.getFamilyName() != null ? patient.getFamilyName() : ""));
		demographicsType.setMiddleName(encrypt(patient.getMiddleName() != null ? patient.getMiddleName() : ""));
		PersonAttribute personAttribute = patient.getAttribute(8);
		demographicsType.setPhoneNumber(encrypt(personAttribute != null ? personAttribute.getValue() : ""));
		demographicsType.setGender(patient.getGender());
		demographicsType.setPatientUuid(patient.getUuid());
		demographicsType.setPatientId(patient.getPersonId());
		demographicsType.setDatimId(datimCode);
		demographicsType.setDateChanged(patient.getDateChanged());
		return demographicsType;
	}
	
	private List<VisitType> buildVisits(Patient patient, String datimCode) {
        List<VisitType> visitTypes = new ArrayList<>();
        List<Visit> visits = visitService.getVisitsByPatient(patient);
        if (visits != null && !visits.isEmpty()) {
            System.out.println("No of visits::"+visits.size());
            visits.forEach(visit -> {
                VisitType visitType = new VisitType();
                visitType.setVisitId(visit.getVisitId());
                visitType.setPatientId(visit.getPatient().getPatientId());
                visitType.setVisitTypeId(visit.getVisitType() != null ? visit.getVisitType().getVisitTypeId() : 0);
                visitType.setDateStarted(visit.getStartDatetime());
                visitType.setCreator(visit.getCreator() != null ? visit.getCreator().getId() : 0);
                visitType.setDateStopped(visit.getStopDatetime());
                visitType.setDateCreated(visit.getDateCreated());
                visitType.setChangedBy(visit.getChangedBy() != null ? visit.getChangedBy().getId() : 0);
                visitType.setDateChanged(visit.getDateChanged());
                visitType.setVoided(visit.getVoided() ? 1 : 0);
                visitType.setVoidedBy(visit.getVoided() ? visit.getVoidedBy().getId() : 0);
                visitType.setDateVoided(visit.getDateVoided());
                visitType.setVisitUuid(visit.getUuid());
                visitType.setLocationId(visit.getLocation() != null ? visit.getLocation().getLocationId() : 0);
                visitType.setPatientUuid(patient.getPerson().getUuid());
                visitType.setDatimId(datimCode);
                visitTypes.add(visitType);
            });
        } else
            System.out.println("No visits for this patient");
        return visitTypes;
    }
	
	private List<PatientBiometricType> buildPatientBiometrics(Patient patient, String datimCode) {
        List<PatientBiometricType> patientBiometricTypes = new ArrayList<>();
        List<BiometricInfo> biometricInfos = biometricInfoService.getBiometricInfoByPatientId(patient.getPatientId());
        if (biometricInfos != null && !biometricInfos.isEmpty()) {
            System.out.println("No of biometrics::"+biometricInfos.size());
            biometricInfos.forEach(biometricInfo -> {
                PatientBiometricType patientBiometricType = new PatientBiometricType();
                patientBiometricType.setBiometricInfoId(biometricInfo.getBiometricInfoId());
                patientBiometricType.setPatientId(patient.getPatientId());
                patientBiometricType.setCreator(biometricInfo.getCreator());
                patientBiometricType.setPatientUuid(patient.getPerson().getUuid());
                patientBiometricType.setDateCreated(biometricInfo.getDateCreated());
                patientBiometricType.setDatimId(datimCode);
                patientBiometricType.setFingerPosition(biometricInfo.getFingerPosition());
                patientBiometricType.setImageDpi(biometricInfo.getImageDPI());
                patientBiometricType.setImageHeight(biometricInfo.getImageHeight());
                patientBiometricType.setImageQuality(biometricInfo.getImageQuality());
                patientBiometricType.setImageWidth(biometricInfo.getImageWidth());
                patientBiometricType.setManufacturer(biometricInfo.getManufacturer());
                patientBiometricType.setModel(biometricInfo.getModel());
                patientBiometricType.setSerialNumber(biometricInfo.getSerialNumber());
                patientBiometricType.setTemplate(biometricInfo.getTemplate()); //todo convert blob to utf-8 format string
                patientBiometricTypes.add(patientBiometricType);
            });
        } else {
            System.out.println(patientBiometricTypes);
            System.out.println("No biometrics found");
        }
        return patientBiometricTypes;
    }
	
	private List<PatientProgramType> buildPatientProgram(Patient patient) {
        List<PatientProgramType> patientProgramTypes = new ArrayList<>();
        List<PatientProgram> patientPrograms = programWorkflowService.getPatientPrograms(
                patient, null, null, null, null, null, false
        );
        if (patientPrograms != null && !patientPrograms.isEmpty()) {
            System.out.println("No of patient programs::"+patientPrograms.size());
            patientPrograms.forEach(patientProgram -> {
                PatientProgramType patientProgramType = new PatientProgramType();
                patientProgramType.setPatientProgramId(patientProgram.getPatientProgramId());
                patientProgramType.setProgramId(patientProgram.getProgram() != null ?
                        patientProgram.getProgram().getProgramId() : 0);
                patientProgramType.setProgramName(patientProgram.getProgram() != null ?
                        patientProgram.getProgram().getName() : "");
                patientProgramType.setDateEnrolled(patientProgram.getDateEnrolled());
                patientProgramType.setDateCompleted(patientProgram.getDateCompleted());
                patientProgramType.setOutcomeConceptId(patientProgram.getOutcome() != null ?
                        patientProgram.getOutcome().getConceptId() : 0);
                patientProgramType.setCreator(patientProgram.getCreator() != null ?
                        patientProgram.getCreator().getId() : 0);
                patientProgramType.setDateCreated(patientProgram.getDateCreated());
                patientProgramType.setDateChanged(patientProgram.getDateChanged());
                patientProgramType.setChangedBy(patientProgram.getChangedBy() != null ?
                        patientProgram.getChangedBy().getId() : 0);
                patientProgramType.setVoided(patientProgram.getVoided() ? 1 : 0);
                patientProgramType.setVoidedBy(patientProgram.getVoided() ? patientProgram.getVoidedBy().getId() : 0);
                patientProgramType.setDateVoided(patientProgram.getDateVoided());
                patientProgramType.setPatientProgramUuid(patientProgram.getUuid());
                patientProgramTypes.add(patientProgramType);
            });
        } else
            System.out.println("No patient programs");
        return patientProgramTypes;
    }
	
	private List<PatientIdentifierType> buildPatientIdentifier (Patient patient) {
        List<PatientIdentifierType> patientIdentifierTypes = new ArrayList<>();
        List<PatientIdentifier> patientIdentifiers = patient.getActiveIdentifiers();
        if (patientIdentifiers != null && !patientIdentifiers.isEmpty()) {
            System.out.println("No of patient identifiers::" + patientIdentifiers.size());
            patientIdentifiers.forEach(patientIdentifier -> {
                PatientIdentifierType patientIdentifierType = new PatientIdentifierType();
                patientIdentifierType.setPatientIdentifierId(patientIdentifier.getPatientIdentifierId());
                patientIdentifierType.setPatientId(patientIdentifier.getPatient().getPatientId());
                patientIdentifierType.setIdentifier(patientIdentifier.getIdentifier());
                patientIdentifierType.setIdentifierType(patientIdentifier.getIdentifierType() != null ? patientIdentifier.getIdentifierType().getId() : 0);
                patientIdentifierType.setPreferred(patientIdentifier.getPreferred() ? 1 : 0);
                patientIdentifierType.setCreator(patientIdentifier.getCreator() != null ? patientIdentifier.getCreator().getId() : 0);
                patientIdentifierType.setPatientIdentifierUuid(patientIdentifier.getUuid());
                patientIdentifierType.setDateChanged(patientIdentifier.getDateChanged());
                patientIdentifierType.setDateCreated(patientIdentifier.getDateCreated());
                patientIdentifierType.setChangedBy(patientIdentifier.getChangedBy() != null ? patientIdentifier.getChangedBy().getId() : 0);
                patientIdentifierType.setVoided(patientIdentifier.getVoided() ? 1 : 0);
                patientIdentifierType.setVoidedBy(patientIdentifier.getVoided() ? patientIdentifier.getVoidedBy().getId() : 0);
                patientIdentifierType.setDateVoided(patientIdentifier.getDateVoided());
                patientIdentifierTypes.add(patientIdentifierType);
            });
        } else
            System.out.println("No patient identifiers");
        return patientIdentifierTypes;
    }
	
	private List<EncounterType> buildEncounters(Patient patient, String datimCode) {
        List<EncounterType> encounterTypes = new ArrayList<>();
        List<Encounter> encounters = encounterService.getEncountersByPatientId(patient.getPatientId());
        if (encounters != null && !encounters.isEmpty()) {
            System.out.println("No of enconters::"+encounters.size());
            encounters.forEach(encounter -> {
                EncounterType encounterType = new EncounterType();
                encounterType.setPatientUuid(patient.getPerson().getUuid());
                encounterType.setDatimId(datimCode);
                if (encounter.getVisit() != null) {
                    encounterType.setVisitId(encounter.getVisit().getVisitId());
                    encounterType.setVisitUuid(encounter.getVisit().getUuid());
                }
                encounterType.setEncounterUuid(encounter.getUuid());
                encounterType.setEncounterId(encounter.getEncounterId());
                encounterType.setEncounterTypeId(encounter.getEncounterType() != null ?
                        encounter.getEncounterType().getEncounterTypeId() : 0);
                encounterType.setPatientId(patient.getPatientId());
                encounterType.setFormId(encounter.getForm() != null ? encounter.getForm().getFormId() : 0);
                encounterType.setLocationId(encounter.getLocation() != null ? encounter.getLocation().getLocationId() : 0);
                encounterType.setPmmForm(encounter.getForm() != null ? encounter.getForm().getName() : "");
                encounterType.setEncounterDatetime(encounter.getEncounterDatetime());
                encounterType.setCreator(encounter.getCreator() != null ? encounter.getCreator().getId() : 0);
                encounterType.setDateCreated(encounter.getDateCreated());
                encounterType.setChangedBy(encounter.getChangedBy() != null ? encounter.getChangedBy().getId() : 0);
                encounterType.setDateChanged(encounter.getDateChanged());
                encounterType.setVoided(encounter.getVoided() ? 1 : 0);
                encounterType.setVoidedBy(encounter.getVoided() ? encounter.getVoidedBy().getId() : 0);
                encounterType.setDateVoided(encounter.getDateVoided());
                encounterTypes.add(encounterType);
            });
        } else System.out.println("No encounters");
        return encounterTypes;
    }
	
	private List<ObsType> buildObs(Patient patient, String datimCode) {
        List<ObsType> obsTypeList = new ArrayList<>();
        List<Obs> obsList = obsService.getObservationsByPerson(patient.getPerson());
        List<Integer> confidentialConcepts = new ArrayList<>(Collections.singletonList(159635));
        if (obsList != null && !obsList.isEmpty()) {
            System.out.println("No of observations::"+obsList.size());
            obsList.forEach(obs -> {
                ObsType obsType = new ObsType();
                obsType.setPatientUuid(patient.getPerson().getUuid());
                obsType.setDatimId(datimCode);
                obsType.setObsUuid(obs.getUuid());
                obsType.setObsId(obs.getObsId());
                obsType.setPersonId(obs.getPersonId());
                obsType.setConceptId(obs.getConcept() != null ? obs.getConcept().getConceptId() : 0);
                if (obs.getEncounter() != null) {
                    obsType.setEncounterId(obs.getEncounter().getEncounterId());
                    obsType.setEncounterUuid(obs.getEncounter().getUuid());
                    obsType.setPmmForm(obs.getEncounter().getForm() != null ?
                            obs.getEncounter().getForm().getName() : "");
                    obsType.setFormId(obs.getEncounter().getForm() != null ?
                            obs.getEncounter().getForm().getFormId() : 0);
                    obsType.setEncounterType(obs.getEncounter().getEncounterType() != null ?
                            obs.getEncounter().getEncounterType().getEncounterTypeId() : 0);
                    obsType.setVisitUuid(obs.getEncounter().getVisit() != null ?
                            obs.getEncounter().getVisit().getUuid() : "");
                }
                obsType.setObsDatetime(obs.getObsDatetime());
                obsType.setObsGroupId(obs.getObsGroup() != null ? obs.getObsGroup().getObsId() : 0);
                obsType.setValueCoded(obs.getValueCoded() != null ? obs.getValueCoded().getConceptId() : 0);
                obsType.setValueDatetime(obs.getValueDatetime());
                obsType.setValueNumeric(obs.getValueNumeric() != null ?
                        BigDecimal.valueOf(obs.getValueNumeric()) : null);
                if (confidentialConcepts.contains(obsType.getConceptId())) {
                    obsType.setValueText(encrypt(obs.getValueText() != null ? obs.getValueText() : ""));
//                obsType.setVariableValue(obs.getValueCoded().g); todo
                } else {
                    obsType.setValueText(obs.getValueText());
                }
                obsType.setCreator(obs.getCreator() != null ? obs.getCreator().getId() : 0);
                obsType.setDateCreated(obs.getDateCreated());
//            obsType.setVariableName(obs.getConcept().getName().getName()); todo
                obsType.setDatatype(obs.getConcept() != null ?
                        obs.getConcept().getDatatype().getConceptDatatypeId() : 0);
                obsType.setLocationId(obs.getLocation() != null ? obs.getLocation().getLocationId() : 0);
                obsType.setVoided(obs.getVoided() ? 1 : 0);
                obsType.setVoidedBy(obs.getVoided() ? obs.getVoidedBy().getId() : 0);
                obsType.setDateVoided(obs.getDateVoided());
                obsTypeList.add(obsType);
            });
        } else System.out.println("No observations");
        return obsTypeList;
    }
	
	private List<EncounterProviderType> buildEncounterProviders(Patient patient) {
        List<EncounterProviderType> encounterProviderTypes = new ArrayList<>();
//        List<EncounterProvider> encounterProviders = Context.getProviderService(). todo confirm how to get encounter providers
        return encounterProviderTypes;
    }
}
