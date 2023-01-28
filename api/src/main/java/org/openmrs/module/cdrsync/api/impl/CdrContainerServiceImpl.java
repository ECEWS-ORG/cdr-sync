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
import org.openmrs.module.cdrsync.api.*;
import org.openmrs.module.cdrsync.container.model.EncounterType;
import org.openmrs.module.cdrsync.container.model.PatientIdentifierType;
import org.openmrs.module.cdrsync.container.model.VisitType;
import org.openmrs.module.cdrsync.container.model.*;
import org.openmrs.module.cdrsync.model.BiometricInfo;
import org.openmrs.module.cdrsync.model.ContainerWrapper;
import org.openmrs.module.cdrsync.model.EncryptedBody;
import org.openmrs.util.Security;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class CdrContainerServiceImpl extends BaseOpenmrsService implements CdrContainerService {
	
	private final PatientService patientService = Context.getPatientService();
	
	private final VisitService visitService = Context.getVisitService();
	
	private final ProgramWorkflowService programWorkflowService = Context.getProgramWorkflowService();
	
	private final ObsService obsService = Context.getObsService();
	
	private final EncounterService encounterService = Context.getEncounterService();
	
	private final BiometricInfoService biometricInfoService = Context.getService(BiometricInfoService.class);
	
	private final CdrSyncEncounterService cdrSyncEncounterService = Context.getService(CdrSyncEncounterService.class);
	
	private final String datimCode = Context.getAdministrationService().getGlobalProperty("facility_datim_code");
	
	private final String facilityName = Context.getAdministrationService().getGlobalProperty("Facility_Name");
	
	private final User user = Context.getAuthenticatedUser();
	
	ObjectMapper objectMapper = new ObjectMapper();
	
	@Override
	public String getAllPatients() {
		String query = "SELECT p FROM Patient p";
		List<Patient> patients = patientService.getPatients("", true, null, 1000);
		//		List<Patient> patients = patientService.getAllPatients(true);
		System.out.println("Total no of patients:: " + patients.size());
		//		List<Patient> newPatients = new ArrayList<>(patients.subList(0, 20));
		//		System.out.println("---------" + newPatients.size());
		//		patients.clear();
		return buildContainer(patients);
	}
	
	@Override
	public String getAllPatients(Date startDate, Date endDate) {
		String query = "SELECT * FROM Patient p";
		List<Patient> patients = patientService.getPatients(query, true, 16502, 1000);
		//		List<Patient> patients = patientService.getAllPatients(true);
		System.out.println("Total no of patients:: " + patients.size());
		//		List<Patient> newPatients = new ArrayList<>(patients.subList(0, 20));
		//		System.out.println("---------" + newPatients.size());
		//		patients.clear();
		return buildContainer(patients, startDate, endDate);
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
	
	private String buildContainer(List<Patient> patients, Date from, Date to) {
        List<Container> containers = new ArrayList<>();
        String resp = "";
        AtomicInteger count = new AtomicInteger();
        try {
            patients.forEach(patient ->
                    createContainerFromLastSyncDate(containers, count, patient, from, to));
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            resp = "There's a problem connecting to the server. Please, check your connection and try again.";
            return resp;
        }
        if (!containers.isEmpty()) {
            try {
                syncContainersToCdr(containers);
                containers.clear();
                resp = "Sync successful!";
            } catch (IOException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
                resp = "Can't sync at the moment, try again later!";
            }
        } else
            resp = "Sync successful!";
        return resp;
    }
	
	private String buildContainer(List<Patient> patients) {
        List<Container> containers = new ArrayList<>();
        String resp;
        AtomicInteger count = new AtomicInteger();
        try {
            patients.forEach(patient -> createContainer(containers, count, patient));
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            resp = "There's a problem connecting to the server. Please, check your connection and try again.";
            return resp;
        }
        if (!containers.isEmpty()) {
            try {
                syncContainersToCdr(containers);
                containers.clear();
                resp = "Sync successful!";
            } catch (IOException e) {
                containers.clear();
                resp = "Incomplete syncing, try again later!";
            }
        } else
            resp = "Sync successful!";
        return resp;
    }
	
	private void createContainer(List<Container> containers, AtomicInteger count, Patient patient) {
		System.out.println(count.getAndIncrement());
		Container container = new Container();
		Date[] touchTimeDate = new Date[1];
		touchTimeDate[0] = patient.getDateChanged() != null ? patient.getDateChanged() : patient.getDateCreated();
		container.setMessageHeader(buildMessageHeader());
		container.setMessageData(buildMessageData(patient, touchTimeDate));
		setContainerTouchTimeAndFileName(containers, patient, touchTimeDate, container);
	}
	
	private void createContainerFromLastSyncDate(List<Container> containers, AtomicInteger count, Patient patient,
	        Date from, Date to) {
		System.out.println(count.getAndIncrement());
		Date[] touchTimeDate = new Date[1];
		touchTimeDate[0] = patient.getDateChanged() != null ? patient.getDateChanged() : patient.getDateCreated(); // todo handle touch time for patient efficiently
		Container container = new Container();
		boolean[] hasUpdate = new boolean[1];
		container.setMessageData(buildMessageDataFromLastSync(patient, hasUpdate, touchTimeDate, from, to));
		if (hasUpdate[0]) {
			container.setMessageHeader(buildMessageHeader());
			setContainerTouchTimeAndFileName(containers, patient, touchTimeDate, container);
		}
	}
	
	private void setContainerTouchTimeAndFileName(List<Container> containers, Patient patient, Date[] touchTimes,
	        Container container) {
		container.setId(patient.getUuid());
		container.getMessageHeader().setTouchTime(touchTimes[0]);
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		String touchTimeString = df.format(container.getMessageHeader().getTouchTime());
		String fileName = patient.getUuid() + "_" + touchTimeString + "_" + datimCode;
		container.getMessageHeader().setFileName(fileName);
		containers.add(container);
		if (containers.size() == 10) {
			try {
				syncContainersToCdr(containers);
				containers.clear();
			}
			catch (IOException e) {
				containers.clear();
				throw new RuntimeException(e);
			}
			
		}
	}
	
	private void syncContainersToCdr(List<Container> containers) throws IOException {
		ContainerWrapper containerWrapper = new ContainerWrapper(containers);
        if (Context.getRuntimeProperties().getProperty("cdr.sync.url") == null) {
            System.out.println("Setting sync url");
            Context.getRuntimeProperties().setProperty("cdr.sync.url", "http://localhost:8484/sync-containers");
        }
		String url = Context.getRuntimeProperties().getProperty("cdr.sync.url");
        System.out.println("Syncing to CDR::" + url);
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()){
            String json = objectMapper.writeValueAsString(containerWrapper);
            String encryptedJson = Security.encrypt(json);
            EncryptedBody encryptedBody = new EncryptedBody(encryptedJson);
            HttpPost post = new HttpPost(url);
            post.setHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity(objectMapper.writeValueAsString(encryptedBody)));
            try (CloseableHttpResponse response = httpClient.execute(post)){
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    System.out.println("After successfully sending request::" + responseBody);
                } else {
                    System.out.println("error sending request");
                    System.out.println("status code::" + statusCode);
                    throw new IOException("error sending request to cdr");
                }
            }
        }
    }
	
	@Override
	public void saveLastSyncDate() {
		Date syncDate = new Date();
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy'T'hh:mm:ss");
		String syncDateString = dateFormat.format(syncDate);
		if (Context.getAdministrationService().getGlobalProperty("last.cdr.sync") == null) {
			GlobalProperty globalProperty = new GlobalProperty("last.cdr.sync", syncDateString, "Last sync date to CDR");
			Context.getAdministrationService().saveGlobalProperty(globalProperty);
		} else
			Context.getAdministrationService().updateGlobalProperty("last.cdr.sync", syncDateString);
	}
	
	private MessageHeaderType buildMessageHeader() {
		MessageHeaderType messageHeaderType = new MessageHeaderType();
		messageHeaderType.setFacilityName(facilityName);
		messageHeaderType.setFacilityDatimCode(datimCode);
		messageHeaderType.setMessageCreationDateTime(new Date());
		messageHeaderType.setMessageSchemaVersion(new BigDecimal("1.0"));
		messageHeaderType.setMessageStatusCode("SYNCED");
		messageHeaderType.setMessageUniqueID(UUID.randomUUID().toString());
		messageHeaderType.setMessageSource("NMRS");
		return messageHeaderType;
	}
	
	private MessageDataType buildMessageData(Patient patient, Date[] touchTimes) {
		MessageDataType messageDataType = new MessageDataType();
        List<EncounterProvider> providers = new ArrayList<>();
		messageDataType.setDemographics(buildDemographics(patient, touchTimes));
		messageDataType.setVisits(buildVisits(patient, touchTimes));
		messageDataType.setPatientBiometrics(buildPatientBiometrics(patient, touchTimes));
		messageDataType.setPatientPrograms(buildPatientProgram(patient, touchTimes));
		messageDataType.setPatientIdentifiers(buildPatientIdentifier(patient, touchTimes));
		messageDataType.setEncounters(buildEncounters(patient, providers, touchTimes));
		messageDataType.setObs(buildObs(patient, touchTimes));
		messageDataType.setEncounterProviders(buildEncounterProviders(providers, patient, touchTimes));
		return messageDataType;
	}
	
	private MessageDataType buildMessageDataFromLastSync(Patient patient, boolean[] hasUpdate, Date[] touchTimes, Date from, Date to) {
        MessageDataType messageData = new MessageDataType();
        List<EncounterProvider> providers = new ArrayList<>();
        messageData.setDemographics(buildDemographics(patient, touchTimes, hasUpdate, from, to));
        messageData.setVisits(buildVisits(patient, hasUpdate, touchTimes, from, to));
        messageData.setPatientBiometrics(buildPatientBiometrics(patient, touchTimes, hasUpdate, from));
        messageData.setPatientPrograms(buildPatientProgram(patient, touchTimes, hasUpdate, from, to));
        messageData.setPatientIdentifiers(buildPatientIdentifier(patient, touchTimes, hasUpdate, from));
        messageData.setEncounters(buildEncounters(patient, providers, touchTimes, hasUpdate, from, to));
        messageData.setObs(buildObs(patient, touchTimes, hasUpdate, from, to));
        messageData.setEncounterProviders(buildEncounterProviders(providers, patient, touchTimes));
        return messageData;
    }
	
	private DemographicsType buildDemographics(Patient patient, Date[] touchTimes, boolean[] hasUpdate, Date from, Date to) {
		DemographicsType demographicsType = new DemographicsType();
		PersonAddress personAddress = patient.getPersonAddress();
		checkUpdatedDate(touchTimes, hasUpdate, from, to, personAddress.getDateChanged(), personAddress.getDateCreated());
		PersonName personName = patient.getPersonName();
		checkUpdatedDate(touchTimes, hasUpdate, from, to, personName.getDateChanged(), personName.getDateCreated());
		return setContainerDemographics(patient, touchTimes, demographicsType, personAddress);
	}
	
	private void checkUpdatedDate(Date[] touchTimes, boolean[] hasUpdate, Date from, Date to, Date dateChanged,
	        Date dateCreated) {
		if (dateChanged != null) {
			if (dateChanged.after(from) && dateChanged.before(to)) {
				hasUpdate[0] = true;
			}
			if (touchTimes[0].before(dateChanged))
				touchTimes[0] = dateChanged;
		} else if (dateCreated != null) {
			if (dateCreated.after(from) && dateCreated.before(to)) {
				hasUpdate[0] = true;
			}
			if (touchTimes[0].before(dateCreated))
				touchTimes[0] = dateCreated;
		}
	}
	
	private DemographicsType setContainerDemographics(Patient patient, Date[] touchTimes, DemographicsType demographicsType,
	        PersonAddress personAddress) {
		if (personAddress.getAddress1() != null && !personAddress.getAddress1().isEmpty()) {
			demographicsType.setAddress1(Security.encrypt(personAddress.getAddress1()));
		}
		if (personAddress.getAddress2() != null && !personAddress.getAddress2().isEmpty()) {
			demographicsType.setAddress2(Security.encrypt(personAddress.getAddress2()));
		}
		demographicsType.setCityVillage(personAddress.getCityVillage());
		demographicsType.setStateProvince(personAddress.getStateProvince());
		demographicsType.setCountry(personAddress.getCountry());
		
		if (patient.getVoided()) {
			demographicsType.setVoided(1);
			demographicsType.setVoidedBy(patient.getVoidedBy().getId());
			demographicsType.setDateVoided(patient.getDateVoided());
			if (touchTimes[0].before(patient.getDateVoided()))
				touchTimes[0] = patient.getDateVoided();
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
		demographicsType.setCauseOfDeath(patient.getCauseOfDeath() != null ? patient.getCauseOfDeath().getName().getName()
		        : null);
		demographicsType.setFirstName(patient.getGivenName() != null ? Security.encrypt(patient.getGivenName()) : "");
		demographicsType.setLastName(patient.getFamilyName() != null ? Security.encrypt(patient.getFamilyName()) : "");
		demographicsType.setMiddleName(patient.getMiddleName() != null ? Security.encrypt(patient.getMiddleName()) : "");
		PersonAttribute personAttribute = patient.getAttribute(8);
		demographicsType.setPhoneNumber(personAttribute != null ? Security.encrypt(personAttribute.getValue()) : "");
		demographicsType.setGender(patient.getGender());
		demographicsType.setPatientUuid(patient.getUuid());
		demographicsType.setPatientId(patient.getPersonId());
		demographicsType.setDatimId(datimCode);
		demographicsType.setDateChanged(patient.getDateChanged());
		return demographicsType;
	}
	
	private DemographicsType buildDemographics(Patient patient, Date[] touchTimes) {
		DemographicsType demographicsType = new DemographicsType();
		PersonAddress personAddress = patient.getPersonAddress();
		if (personAddress.getDateChanged() != null) {
			if (touchTimes[0].before(personAddress.getDateChanged()))
				touchTimes[0] = personAddress.getDateChanged();
		} else if (personAddress.getDateCreated() != null) {
			if (touchTimes[0].before(personAddress.getDateCreated()))
				touchTimes[0] = personAddress.getDateCreated();
		}
		PersonName personName = patient.getPersonName();
		if (personName.getDateChanged() != null) {
			if (touchTimes[0].before(personName.getDateChanged()))
				touchTimes[0] = personName.getDateChanged();
		} else if (personName.getDateCreated() != null) {
			if (touchTimes[0].before(personName.getDateCreated()))
				touchTimes[0] = personName.getDateCreated();
		}
		return setContainerDemographics(patient, touchTimes, demographicsType, personAddress);
	}
	
	private List<VisitType> buildVisits(Patient patient, Date[] touchTimes) {
        List<VisitType> visitTypes = new ArrayList<>();
        List<Visit> visits = visitService.getVisits(
                null, Collections.singletonList(patient), null, null, null,
                null, null, null, null, true, true
        );
        if (visits != null && !visits.isEmpty()) {
            buildContainerVisitType(patient, touchTimes, visitTypes, visits);
        } else
            System.out.println("No visits for this patient");
        return visitTypes;
    }
	
	private List<VisitType> buildVisits(Patient patient, boolean[] hasUpdate, Date[] touchTimes, Date startDate, Date endDate) {
        List<VisitType> visitTypes = new ArrayList<>();
        List<Visit> visits = Context.getService(CdrSyncVisitService.class).getVisitsByPatientAndDateChanged(patient, startDate, endDate);
        if (visits != null && !visits.isEmpty()) {
            hasUpdate[0] = true;
            buildContainerVisitType(patient, touchTimes, visitTypes, visits);
        } else
            System.out.println("No visits for this patient");
        return visitTypes;
    }
	
	private void buildContainerVisitType(Patient patient, Date[] touchTimes, List<VisitType> visitTypes, List<Visit> visits) {
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
            if (visit.getDateChanged() != null) {
                if (touchTimes[0].before(visit.getDateChanged()))
                    touchTimes[0] = visit.getDateChanged();
            } else {
                if (touchTimes[0].before(visit.getDateCreated()))
                    touchTimes[0] = visit.getDateCreated();
            }
            visitTypes.add(visitType);
            if (visit.getDateVoided() != null) {
                if (touchTimes[0].before(visit.getDateVoided()))
                    touchTimes[0] = visit.getDateVoided();
            }
        });
    }
	
	private List<PatientBiometricType> buildPatientBiometrics(Patient patient, Date[] touchTimes) {
        List<PatientBiometricType> patientBiometricTypes = new ArrayList<>();
        List<BiometricInfo> biometricInfos = biometricInfoService.getBiometricInfoByPatientId(patient.getPatientId());
        if (biometricInfos != null && !biometricInfos.isEmpty()) {
            buildContainerBiometricType(patient, touchTimes, patientBiometricTypes, biometricInfos);
        } else {
            System.out.println("No biometrics found");
        }
        return patientBiometricTypes;
    }
	
	private List<PatientBiometricType> buildPatientBiometrics(Patient patient, Date[] touchTimes, boolean[] hasUpdate, Date startDate) {
        List<PatientBiometricType> patientBiometricTypes = new ArrayList<>();
        List<BiometricInfo> biometricInfos = biometricInfoService.getBiometricInfoByPatientIdAndDateCaptured(patient.getPatientId(), startDate);
        if (biometricInfos != null && !biometricInfos.isEmpty()) {
            hasUpdate[0] = true;
            buildContainerBiometricType(patient, touchTimes, patientBiometricTypes, biometricInfos);
        } else {
            System.out.println("No biometrics found");
        }
        return patientBiometricTypes;
    }
	
	private void buildContainerBiometricType(Patient patient, Date[] touchTimes, List<PatientBiometricType> patientBiometricTypes, List<BiometricInfo> biometricInfos) {
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
            patientBiometricType.setTemplate(biometricInfo.getTemplate());
            patientBiometricTypes.add(patientBiometricType);
            if (touchTimes[0].before(biometricInfo.getDateCreated()))
                touchTimes[0] = biometricInfo.getDateCreated();
        });
    }
	
	private List<PatientProgramType> buildPatientProgram(Patient patient, Date[] touchTimes) {
        List<PatientProgramType> patientProgramTypes = new ArrayList<>();
        List<PatientProgram> patientPrograms = programWorkflowService.getPatientPrograms(
                patient, null, null, null, null, null, true
        );
        if (patientPrograms != null && !patientPrograms.isEmpty()) {
            buildContainerPatientProgramType(touchTimes, patientProgramTypes, patientPrograms);
        } else
            System.out.println("No patient programs");
        return patientProgramTypes;
    }
	
	private List<PatientProgramType> buildPatientProgram(Patient patient, Date[] touchTimes, boolean[] hasUpdate, Date from, Date to) {
        List<PatientProgramType> patientProgramTypes = new ArrayList<>();
        List<PatientProgram> patientPrograms = Context.getService(CdrSyncPatientProgramService.class)
                .getPatientProgramsByPatientAndLastSyncDate(patient, from, to);
        if (patientPrograms != null && !patientPrograms.isEmpty()) {
            hasUpdate[0] = true;
            buildContainerPatientProgramType(touchTimes, patientProgramTypes, patientPrograms);
        } else
            System.out.println("No patient programs");
        return patientProgramTypes;
    }
	
	private void buildContainerPatientProgramType(Date[] touchTimes, List<PatientProgramType> patientProgramTypes, List<PatientProgram> patientPrograms) {
        System.out.println("No of patient programs::"+patientPrograms.size());
        patientPrograms.forEach(patientProgram -> {
            PatientProgramType patientProgramType = new PatientProgramType();
            patientProgramType.setPatientProgramId(patientProgram.getPatientProgramId());
            patientProgramType.setPatientId(patientProgram.getPatient().getPatientId());
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
            patientProgramType.setPatientUuid(patientProgram.getPatient().getPerson().getUuid());
            patientProgramType.setLocationId(patientProgram.getLocation() != null ?
                    patientProgram.getLocation().getLocationId() : 0);
            patientProgramType.setDatimId(datimCode);
            patientProgramTypes.add(patientProgramType);
            if (patientProgram.getDateChanged() != null) {
                if (touchTimes[0].before(patientProgram.getDateChanged()))
                    touchTimes[0] = patientProgram.getDateChanged();
            } else {
                if (touchTimes[0].before(patientProgram.getDateCreated()))
                    touchTimes[0] = patientProgram.getDateCreated();
            }
            if (patientProgram.getDateVoided() != null && touchTimes[0].before(patientProgram.getDateVoided()))
                touchTimes[0] = patientProgram.getDateVoided();
        });
    }
	
	private List<PatientIdentifierType> buildPatientIdentifier (Patient patient, Date[] touchTimes) {
        List<PatientIdentifierType> patientIdentifierTypes = new ArrayList<>();
        Set<PatientIdentifier> patientIdentifiers = patient.getIdentifiers();
        if (patientIdentifiers != null && !patientIdentifiers.isEmpty()) {
            buildContainerPatientIdentifier(patient, touchTimes, patientIdentifierTypes, patientIdentifiers);
        } else
            System.out.println("No patient identifiers");
        return patientIdentifierTypes;
    }
	
	private List<PatientIdentifierType> buildPatientIdentifier (Patient patient, Date[] touchTimes, boolean[] hasUpdate, Date lastSyncDate) {
        List<PatientIdentifierType> patientIdentifierTypes = new ArrayList<>();
        Set<PatientIdentifier> patientIdentifiers = patient.getIdentifiers();
        if (patientIdentifiers != null && !patientIdentifiers.isEmpty()) {
            Set<PatientIdentifier> updatedPatientIdentifiers = patientIdentifiers.stream()
                    .filter(patientIdentifier -> patientIdentifier.getDateChanged() != null &&
                            patientIdentifier.getDateChanged().after(lastSyncDate))
                    .collect(Collectors.toSet());
            if (!updatedPatientIdentifiers.isEmpty()) {
                hasUpdate[0] = true;
                buildContainerPatientIdentifier(patient, touchTimes, patientIdentifierTypes, updatedPatientIdentifiers);
            } else
                System.out.println("No updated patient identifiers");
        } else
            System.out.println("No patient identifiers");
        return patientIdentifierTypes;
    }
	
	private void buildContainerPatientIdentifier(Patient patient, Date[] touchTimes, List<PatientIdentifierType> patientIdentifierTypes, Set<PatientIdentifier> updatedPatientIdentifiers) {
        System.out.println("No of patient identifiers::" + updatedPatientIdentifiers.size());
        updatedPatientIdentifiers.forEach(patientIdentifier -> {
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
            patientIdentifierType.setDatimId(datimCode);
            patientIdentifierType.setPatientUuid(patient.getUuid());
            patientIdentifierTypes.add(patientIdentifierType);
            if (patientIdentifier.getDateChanged() != null) {
                if (touchTimes[0].before(patientIdentifier.getDateChanged()))
                    touchTimes[0] = patientIdentifier.getDateChanged();
            } else {
                if (touchTimes[0].before(patientIdentifier.getDateCreated()))
                    touchTimes[0] = patientIdentifier.getDateCreated();
            }
            if (patientIdentifier.getDateVoided() != null && touchTimes[0].before(patientIdentifier.getDateVoided()))
                touchTimes[0] = patientIdentifier.getDateVoided();
        });
    }
	
	private List<EncounterType> buildEncounters(Patient patient, List<EncounterProvider> providers, Date[] touchTimes) {
        List<EncounterType> encounterTypes = new ArrayList<>();
//        List<Encounter> encounters = encounterService.getEncountersByPatientId(patient.getPatientId());
        List<Encounter> encounters = encounterService.getEncounters(
                patient, null, null, null, null, null, null,
                null, null, true
        );
        if (encounters != null && !encounters.isEmpty()) {
            buildContainerEncounterType(patient, providers, touchTimes, encounterTypes, encounters);
        } else System.out.println("No encounters");
        return encounterTypes;
    }
	
	private List<EncounterType> buildEncounters(Patient patient, List<EncounterProvider> providers, Date[] touchTimes, boolean[] hasUpdate, Date startDate, Date endDate) {
        List<EncounterType> encounterTypes = new ArrayList<>();
//        List<Encounter> encounters = encounterService.getEncountersByPatientId(patient.getPatientId());
        List<Encounter> encounters = Context.getService(CdrSyncEncounterService.class)
                .getEncountersByLastSyncDateAndPatient(startDate, endDate, patient);
        if (encounters != null && !encounters.isEmpty()) {
            hasUpdate[0] = true;
            buildContainerEncounterType(patient, providers, touchTimes, encounterTypes, encounters);
        } else System.out.println("No encounters");
        return encounterTypes;
    }
	
	private void buildContainerEncounterType(Patient patient, List<EncounterProvider> providers, Date[] touchTimes, List<EncounterType> encounterTypes, List<Encounter> encounters) {
        System.out.println("No of encounters::"+encounters.size());
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
            Set<EncounterProvider> encounterProviders = encounter.getEncounterProviders();
            if (!encounterProviders.isEmpty()) {
                providers.addAll(encounterProviders);
            }
            if (encounter.getDateChanged() != null) {
                if (touchTimes[0].before(encounter.getDateChanged()))
                    touchTimes[0] = encounter.getDateChanged();
            } else {
                if (touchTimes[0].before(encounter.getDateCreated()))
                    touchTimes[0] = encounter.getDateCreated();
            }
            if (encounter.getDateVoided() != null && touchTimes[0].before(encounter.getDateVoided()))
                touchTimes[0] = encounter.getDateVoided();
        });
    }
	
	private List<ObsType> buildObs(Patient patient, Date[] touchTimes) {
        List<ObsType> obsTypeList = new ArrayList<>();
        List<Obs> obsList = obsService.getObservations(
                Collections.singletonList(patient.getPerson()), null, null, null, null, null,
                null, null, null, null, null, true
        );
        if (obsList != null && !obsList.isEmpty()) {
            buildContainerObsType(patient, touchTimes, obsTypeList, obsList);
        } else System.out.println("No observations");
        return obsTypeList;
    }
	
	private List<ObsType> buildObs(Patient patient, Date[] touchTimes, boolean[] hasUpdate, Date lastSyncDate, Date lastSyncDate2) {
        List<ObsType> obsTypeList = new ArrayList<>();
        List<Obs> obsList = Context.getService(CdrSyncObsService.class).getObsByPatientAndLastSyncDate(patient, lastSyncDate, lastSyncDate2);
        if (obsList != null && !obsList.isEmpty()) {
            hasUpdate[0] = true;
            buildContainerObsType(patient, touchTimes, obsTypeList, obsList);
        } else System.out.println("No observations");
        return obsTypeList;
    }
	
	private void buildContainerObsType(Patient patient, Date[] touchTimes, List<ObsType> obsTypeList, List<Obs> obsList) {
        System.out.println("No of observations::"+obsList.size());
        List<Integer> confidentialConcepts = new ArrayList<>(Arrays.asList(159635, 162729, 160638, 160641, 160642));
        obsList.forEach(obs -> {
            ObsType obsType = new ObsType();
            obsType.setPatientUuid(patient.getUuid());
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
                obsType.setValueText(obs.getValueText() != null ? Security.encrypt(obs.getValueText()) : "");
                obsType.setVariableValue(obs.getValueCoded() != null ?
                        Security.encrypt(obs.getValueCoded().getName().getName()) : obs.getValueText() != null ?
                        Security.encrypt(obs.getValueText()) : obs.getValueDatetime() != null ?
                        Security.encrypt(String.valueOf(obs.getValueDatetime())) : obs.getValueNumeric() != null ?
                        Security.encrypt(String.valueOf(obs.getValueNumeric())) : ""); //todo
            } else {
                obsType.setValueText(obs.getValueText());
                obsType.setVariableValue(obs.getValueCoded() != null ?
                        obs.getValueCoded().getName().getName() : obs.getValueText() != null ?
                        obs.getValueText() : obs.getValueDatetime() != null ?
                        String.valueOf(obs.getValueDatetime()) : obs.getValueNumeric() != null ?
                        String.valueOf(obs.getValueNumeric()) : ""); //todo
            }
            obsType.setCreator(obs.getCreator() != null ? obs.getCreator().getId() : 0);
            obsType.setDateCreated(obs.getDateCreated());
            obsType.setVariableName(obs.getConcept() != null ? obs.getConcept().getName().getName() : ""); //todo
            obsType.setDatatype(obs.getConcept() != null ? obs.getConcept().getDatatype().getConceptDatatypeId() : 0);
            obsType.setLocationId(obs.getLocation() != null ? obs.getLocation().getLocationId() : 0);
            obsType.setVoided(obs.getVoided() ? 1 : 0);
            obsType.setVoidedBy(obs.getVoided() ? obs.getVoidedBy().getId() : 0);
            obsType.setDateVoided(obs.getDateVoided());
            obsTypeList.add(obsType);
            if (obs.getDateChanged() != null) {
                if (touchTimes[0].before(obs.getDateChanged()))
                    touchTimes[0] = obs.getDateChanged();
            } else {
                if (touchTimes[0].before(obs.getDateCreated()))
                    touchTimes[0] = obs.getDateCreated();
            }
            if (obs.getDateVoided() != null && touchTimes[0].before(obs.getDateVoided()))
                touchTimes[0] = obs.getDateVoided();
        });
    }
	
	private List<EncounterProviderType> buildEncounterProviders(List<EncounterProvider> providers, Patient patient, Date[] touchTimes) {
        List<EncounterProviderType> encounterProviderTypes = new ArrayList<>();
        if (!providers.isEmpty()) {
            System.out.println("Providers::" + providers.size());
            providers.forEach(encounterProvider -> {
                EncounterProviderType providerType = new EncounterProviderType();
                providerType.setEncounterProviderId(encounterProvider.getEncounterProviderId());
                providerType.setEncounterId(encounterProvider.getEncounter() != null ? encounterProvider.getEncounter().getEncounterId() : 0);
                providerType.setProviderId(encounterProvider.getProvider() != null ? encounterProvider.getProvider().getProviderId() : 0);
                providerType.setEncounterRoleId(encounterProvider.getEncounterRole() != null ? encounterProvider.getEncounterRole().getEncounterRoleId() : 0);
                providerType.setCreator(encounterProvider.getCreator() != null ? encounterProvider.getCreator().getId() : 0);
                providerType.setDateCreated(encounterProvider.getDateCreated());
                providerType.setChangedBy(encounterProvider.getChangedBy() != null ? encounterProvider.getChangedBy().getId() : 0);
                providerType.setDateChanged(encounterProvider.getDateChanged());
                providerType.setVoided(encounterProvider.getVoided() ? 1 : 0);
                providerType.setDateVoided(encounterProvider.getDateVoided());
                providerType.setVoidedBy(encounterProvider.getVoidedBy() != null ? encounterProvider.getVoidedBy().getId() : 0);
                providerType.setVoidedReason(encounterProvider.getVoidReason());
                providerType.setEncounterProviderUuid(encounterProvider.getUuid());
                providerType.setEncounterUuid(encounterProvider.getEncounter() != null ? encounterProvider.getEncounter().getUuid() : null);
                providerType.setVisitUuid(encounterProvider.getEncounter() != null ? encounterProvider.getEncounter().getVisit() != null ? encounterProvider.getEncounter().getVisit().getUuid() : null : null);
                providerType.setLocationId(encounterProvider.getEncounter() != null ? encounterProvider.getEncounter().getLocation() != null ? encounterProvider.getEncounter().getLocation().getLocationId() : 0 : 0);
                providerType.setPatientUuid(patient.getUuid());
                providerType.setDatimId(datimCode);
                encounterProviderTypes.add(providerType);
                if (encounterProvider.getDateChanged() != null) {
                    if (touchTimes[0].before(encounterProvider.getDateChanged()))
                        touchTimes[0] = encounterProvider.getDateChanged();
                } else {
                    if (touchTimes[0].before(encounterProvider.getDateCreated()))
                        touchTimes[0] = encounterProvider.getDateCreated();
                }
                if (encounterProvider.getDateVoided() != null && touchTimes[0].before(encounterProvider.getDateVoided()))
                    touchTimes[0] = encounterProvider.getDateVoided();
            });
        }
        return encounterProviderTypes;
    }
}
