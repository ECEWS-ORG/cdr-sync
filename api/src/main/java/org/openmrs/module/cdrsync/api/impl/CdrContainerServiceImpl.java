package org.openmrs.module.cdrsync.api.impl;

import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.*;
import org.openmrs.api.*;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.cdrsync.api.BiometricInfoService;
import org.openmrs.module.cdrsync.api.CdrContainerService;
import org.openmrs.module.cdrsync.api.dao.impl.BiometricInfoDaoImpl;
import org.openmrs.module.cdrsync.container.model.*;
import org.openmrs.module.cdrsync.container.model.EncounterType;
import org.openmrs.module.cdrsync.container.model.PatientIdentifierType;
import org.openmrs.module.cdrsync.container.model.VisitType;
import org.openmrs.module.cdrsync.model.*;
import org.openmrs.util.HttpClient;
import org.openmrs.util.HttpUrl;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.client.RestTemplate;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CdrContainerServiceImpl extends BaseOpenmrsService implements CdrContainerService {
	
	//	PersonService personService = Context.getPersonService();
	
	PatientService patientService = Context.getPatientService();
	
	VisitService visitService = Context.getVisitService();
	
	ProgramWorkflowService programWorkflowService = Context.getProgramWorkflowService();
	
	ObsService obsService = Context.getObsService();
	
	EncounterService encounterService = Context.getEncounterService();
	
	AdministrationService administrationService = Context.getAdministrationService();
	
	BiometricInfoService biometricInfoService = new BiometricInfoServiceImpl(new BiometricInfoDaoImpl());
	
	//	@Autowired
	//	private final RestTemplate restTemplate = new RestTemplate();
	ObjectMapper objectMapper = new ObjectMapper();
	
	private static SecretKeySpec secretKey;
	
	private final static String SECRET = "IHVNPass1word";
	
	@Override
	public void getAllPatients() {
		List<Patient> patients = patientService.getAllPatients(false);
		System.out.println("Total no of patients:: " + patients.size());
		List<Patient> newPatients = patients.subList(0, 2);
		System.out.println(newPatients.size());
		buildContainer(newPatients);
	}
	
	private void buildContainer(List<Patient> patients) {
        List<Container> containers = new ArrayList<>();
        String datimCode = administrationService.getGlobalProperty("facility_datim_code");
        System.out.println(datimCode);
        String facilityName = administrationService.getGlobalProperty("Facility_Name");
        System.out.println(facilityName);
        AtomicInteger count = new AtomicInteger();
        patients.forEach(patient -> {
            System.out.println(count.getAndIncrement());
            Container container = new Container();
            container.setMessageHeader(buildMessageHeader(datimCode, facilityName));
            container.setMessageData(buildMessageData(patient, datimCode));
            containers.add(container);
            if (containers.size() == 2) {
                ContainerWrapper containerWrapper = new ContainerWrapper(containers);
                String url = "http://localhost:8484/sync-containers";
                OutputStreamWriter wr;
                BufferedReader rd;
                String response = "";
                try {
//                    HttpClient client = new HttpClient(url);
////                    HttpPost
//                    Map<String, String> params = new HashMap<>();
//                    params.put("containers", new ObjectMapper().writeValueAsString(containerWrapper.getContainers()));
//                    String resp = client.post(params);
//                    System.out.println(resp);

                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
                    df.setTimeZone(TimeZone.getTimeZone("Africa/Lagos"));
                    objectMapper.setDateFormat(df);
                    String data = objectMapper.writeValueAsString(containerWrapper);

                    HttpUrl httpUrl = new HttpUrl(url);
                    HttpURLConnection connection = httpUrl.openConnection();
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Length", String.valueOf(data.length()));
                    connection.setRequestProperty("Content-Type", "application/json");
                    wr = new OutputStreamWriter(connection.getOutputStream());
                    wr.write(data);
                    wr.flush();
                    wr.close();

                    String line;
                    for(rd = new BufferedReader(new InputStreamReader(connection.getInputStream())); (line = rd.readLine()) != null; response = String.format("%s%s\n", response, line)) {
                    }
                    System.out.println(response);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
//                ResponseEntity<String> response = restTemplate.postForEntity(url, containerWrapper, String.class);
//                if (response != null) {
//                    String message = response.getBody();
//                    System.out.println(message);
//                }
            }
        });
        Date syncDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyy'T'HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Africa/Lagos"));
        String syncDateString = dateFormat.format(syncDate);
        GlobalProperty globalProperty = new GlobalProperty("Sync_Date", syncDateString, "Last sync date to CDR");
        administrationService.saveGlobalProperty(globalProperty);
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
		//		messageDataType.setPatientBiometrics(buildPatientBiometrics(patient, datimCode));
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
        List<BiometricInfo> biometricInfos = biometricInfoService.getBiometricInfoByPatient(patient);
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
//                patientBiometricType.setTemplate(biometricInfo.getNewTemplate());
                patientBiometricTypes.add(patientBiometricType);
            });
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
            System.out.println("No of patient identifiers::"+patientIdentifiers.size());
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
//                obsType.setVariableValue(obs.getValueCoded().g);
                } else {
                    obsType.setValueText(obs.getValueText());
                }
                obsType.setCreator(obs.getCreator() != null ? obs.getCreator().getId() : 0);
                obsType.setDateCreated(obs.getDateCreated());
//            obsType.setVariableName(obs.getConcept().getName().getName());
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
//        List<EncounterProvider> encounterProviders = Context.getProviderService().
        return encounterProviderTypes;
    }
	
	public static void setKey(String myKey) {
		MessageDigest sha;
		try {
			byte[] key = myKey.getBytes(StandardCharsets.UTF_8);
			sha = MessageDigest.getInstance("SHA-1");
			key = sha.digest(key);
			key = Arrays.copyOf(key, 16);
			secretKey = new SecretKeySpec(key, "AES");
		}
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	public static String encrypt(String strToEncrypt) {
        try
        {
            setKey(SECRET);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
        }
        catch (InvalidKeyException | NoSuchAlgorithmException | BadPaddingException |
               IllegalBlockSizeException | NoSuchPaddingException e)
        {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }
	
	public static String decrypt(String strToDecrypt) {
        try
        {
            setKey(SECRET);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        }
        catch (InvalidKeyException | NoSuchAlgorithmException | BadPaddingException |
               IllegalBlockSizeException | NoSuchPaddingException e)
        {
            System.out.println("Error while decrypting: " + e.getMessage());
        }
        return null;
    }
}
