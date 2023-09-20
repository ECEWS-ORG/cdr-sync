package org.openmrs.module.cdrsync.api.extractor.impl;

import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.cdrsync.api.extractor.*;
import org.openmrs.module.cdrsync.container.model.EncounterType;
import org.openmrs.module.cdrsync.container.model.PatientIdentifierType;
import org.openmrs.module.cdrsync.container.model.VisitType;
import org.openmrs.module.cdrsync.container.model.*;
import org.openmrs.module.cdrsync.model.extractor.BiometricInfo;
import org.openmrs.module.cdrsync.model.extractor.BiometricVerificationInfo;
import org.openmrs.module.cdrsync.model.extractor.IntegratorClientIntake;
import org.openmrs.module.cdrsync.model.extractor.DatimMap;
import org.openmrs.module.cdrsync.utils.AppUtil;
import org.openmrs.util.Security;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import static org.openmrs.module.cdrsync.utils.AppUtil.*;

public class ContainerServiceImpl implements ContainerService {
	
	private final Logger logger = Logger.getLogger(this.getClass().getName());
	
	private final CdrSyncAdminService cdrSyncAdminService;
	
	private final BiometricVerificationInfoService biometricVerificationInfoService;
	
	private final BiometricInfoService biometricInfoService;
	
	private final EncounterService encounterService;
	
	private final Covid19CaseService covid19CaseService;
	
	private final byte[] initVector;
	
	private final byte[] secretKey;
	
	private final String errorFileName = "error.txt";
	
	private String reportFolderGlobal;
	
	public ContainerServiceImpl() {
		this.cdrSyncAdminService = Context.getService(CdrSyncAdminService.class);
		this.biometricVerificationInfoService = Context.getService(BiometricVerificationInfoService.class);
		this.biometricInfoService = Context.getService(BiometricInfoService.class);
		this.encounterService = Context.getService(EncounterService.class);
		this.covid19CaseService = Context.getService(Covid19CaseService.class);
		String initVectorText = getInitVectorText();
		this.initVector = Base64.getDecoder().decode(initVectorText);
		String keyText = getEncryptionKeyText();
		this.secretKey = Base64.getDecoder().decode(keyText);
	}
	
	@Override
	public void createContainer(List<Container> containers, AtomicInteger count, Integer patientId, String reportFolder) {
		Patient patient = Context.getPatientService().getPatient(patientId);
		if (patient != null) {
			this.reportFolderGlobal = reportFolder;
			Container container = new Container();
			Date[] touchTimeDate = new Date[1];
			touchTimeDate[0] = patient.getDateChanged() != null ? patient.getDateChanged()
			        : patient.getDateCreated() != null ? patient.getDateCreated() : null;
			container.setMessageHeader(buildMessageHeader());
			container.setMessageData(buildMessageData(patient, touchTimeDate));
			setContainerTouchTimeAndFileName(containers, patient, touchTimeDate, container, reportFolder);
		}
	}
	
	private void setContainerTouchTimeAndFileName(List<Container> containers, Patient patient, Date[] touchTimes,
	        Container container, String reportFolder) {
		container.setId(patient.getUuid());
		container.getMessageHeader().setTouchTime(touchTimes[0]);
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		String touchTimeString = df.format(container.getMessageHeader().getTouchTime());
		String fileName = patient.getUuid() + "_" + touchTimeString + "_" + getDatimCode() + ".json";
		container.getMessageHeader().setFileName(fileName);
		
		writeObjectToFile(container, fileName, reportFolder);
		
		//		containers.add(container);
		//		if (containers.size() == 50) {
		//			try {
		//				syncContainersToCdr(containers);
		//				containers.clear();
		//			}
		//			catch (IOException e) {
		//				containers.clear();
		//				throw new RuntimeException(e);
		//			}
		//
		//		}
	}
	
	private MessageHeaderType buildMessageHeader() {
		MessageHeaderType messageHeaderType = new MessageHeaderType();
		messageHeaderType.setFacilityName(getFacilityName());
		messageHeaderType.setFacilityDatimCode(getDatimCode());
		DatimMap datimMap = getDatimMap();
		if (datimMap != null) {
			messageHeaderType.setFacilityLga(datimMap.getLgaName());
			messageHeaderType.setFacilityState(datimMap.getStateName());
		}
		messageHeaderType.setMessageCreationDateTime(new Date());
		messageHeaderType.setMessageSchemaVersion(new BigDecimal("2.1"));
		messageHeaderType.setMessageStatusCode("SYNCED");
		messageHeaderType.setMessageUniqueID(UUID.randomUUID().toString());
		messageHeaderType.setMessageSource("NMRS");
		return messageHeaderType;
	}
	
	private MessageDataType buildMessageData(Patient patient, Date[] touchTime) {
        MessageDataType messageDataType = new MessageDataType();
        List<EncounterProvider> providers = new ArrayList<>();
        List<Obs> obsList = new ArrayList<>();
        messageDataType.setDemographics(buildDemographics(patient, touchTime));
        messageDataType.setVisits(buildVisits(patient, touchTime));
        messageDataType.setEncounters(buildEncounters(patient, providers, touchTime, obsList));
        messageDataType.setObs(buildObs(patient, touchTime, obsList));
        messageDataType.setEncounterProviders(buildEncounterProviders(providers, patient, touchTime));
        messageDataType.setPatientBiometrics(buildPatientBiometrics(patient, touchTime));
        messageDataType.setPatientBiometricVerifications(buildPatientBiometricVerifications(patient, touchTime));
        messageDataType.setPatientPrograms(buildPatientProgram(patient, touchTime));
        messageDataType.setPatientIdentifiers(buildPatientIdentifier(patient, touchTime));
        messageDataType.setIntegratorClientIntakes(buildCovid19Cases(patient, touchTime));
        return messageDataType;
    }
	
	private List<IntegratorClientIntakeType> buildCovid19Cases(Patient patient, Date[] touchTime) {
        List<IntegratorClientIntakeType> integratorClientIntakeTypes = new ArrayList<>();
        List<IntegratorClientIntake> integratorClientIntakes = covid19CaseService.getCovid19CasesByPatientId(patient.getPatientId());
        if (integratorClientIntakes != null && !integratorClientIntakes.isEmpty()) {
            buildContainerCovid19CaseType(touchTime, integratorClientIntakeTypes, integratorClientIntakes);
        }
        return integratorClientIntakeTypes;
    }
	
	private void buildContainerCovid19CaseType(Date[] touchTime, List<IntegratorClientIntakeType> integratorClientIntakeTypes, List<IntegratorClientIntake> integratorClientIntakes) {
        integratorClientIntakes.forEach(covid19Case -> {
            IntegratorClientIntakeType covid19CaseType = new IntegratorClientIntakeType(covid19Case);

            integratorClientIntakeTypes.add(covid19CaseType);

            if (touchTime[0] == null || touchTime[0].before(covid19Case.getDateCreated()))
                touchTime[0] = covid19Case.getDateCreated();
        });
    }
	
	private DemographicsType buildDemographics(Patient patient, Date[] touchTime) {
		DemographicsType demographicsType = new DemographicsType();
		PersonAddress personAddress = patient.getPersonAddress();
		if (personAddress != null)
			setPatientTouchTime(touchTime, personAddress.getDateChanged(), personAddress.getDateCreated());
		
		PersonName personName = patient.getPersonName();
		if (personName != null)
			setPatientTouchTime(touchTime, personName.getDateChanged(), personName.getDateCreated());
		
		PersonAttribute personAttribute = patient.getAttribute(8);
		if (personAttribute != null)
			setPatientTouchTime(touchTime, personAttribute.getDateChanged(), personAttribute.getDateCreated());
		return setContainerDemographics(patient, touchTime, demographicsType, personAddress);
	}
	
	private void checkUpdatedDate(Date[] touchTimes, Date dateChanged, Date dateCreated) {
		if (dateChanged != null) {
			if (touchTimes[0].before(dateChanged))
				touchTimes[0] = dateChanged;
		} else if (dateCreated != null) {
			if (touchTimes[0].before(dateCreated))
				touchTimes[0] = dateCreated;
		}
	}
	
	private DemographicsType setContainerDemographics(Patient patient, Date[] touchTime, DemographicsType demographicsType,
	        PersonAddress personAddress) {
		if (personAddress != null) {
			if (personAddress.getAddress1() != null && !personAddress.getAddress1().isEmpty()) {
				demographicsType.setAddress1(Security.encrypt(personAddress.getAddress1(), initVector, secretKey));
			}
			if (personAddress.getAddress2() != null && !personAddress.getAddress2().isEmpty()) {
				demographicsType.setAddress2(Security.encrypt(personAddress.getAddress2(), initVector, secretKey));
			}
			demographicsType.setCityVillage(personAddress.getCityVillage());
			demographicsType.setStateProvince(personAddress.getStateProvince());
			demographicsType.setCountry(personAddress.getCountry());
		}
		if (patient.getVoided()) {
			demographicsType.setVoided(1);
			try {
				demographicsType.setVoidedBy(patient.getVoidedBy().getId());
			}
			catch (Exception e) {
				String message = "Error while getting voided by user id for patient with id: " + patient.getPatientId();
				//				throw new RuntimeException(message, e);
				logger.severe(message);
				writeErrorToFile(message, "patient_" + patient.getPatientId() + "_" + errorFileName, reportFolderGlobal);
			}
			demographicsType.setDateVoided(patient.getDateVoided());
			if (touchTime[0] == null || touchTime[0].before(patient.getDateVoided()))
				touchTime[0] = patient.getDateVoided();
			demographicsType.setVoidedReason(patient.getPersonVoidReason());
		} else
			demographicsType.setVoided(0);
		try {
			demographicsType.setCreator(patient.getCreator().getId());
		}
		catch (Exception e) {
			String message = "Error while getting creator user id for patient with id: " + patient.getPatientId();
			//			throw new RuntimeException(message, e);
			logger.severe(message);
			writeErrorToFile(message, "patient_" + patient.getPatientId() + "_" + errorFileName, reportFolderGlobal);
		}
		demographicsType.setDateCreated(patient.getDateCreated());
		
		demographicsType.setBirthdate(patient.getBirthdate());
		demographicsType.setBirthdateEstimated(patient.getBirthdateEstimated() ? 1 : 0);
		demographicsType.setChangedBy(patient.getChangedBy() != null ? patient.getChangedBy().getId() : 0);
		demographicsType.setDeathdateEstimated(patient.getDeathdateEstimated() ? 1 : 0);
		demographicsType.setDeathDate(patient.getDeathDate());
		demographicsType.setDead(patient.getDead() ? 1 : 0);
		try {
			demographicsType.setCauseOfDeath(patient.getCauseOfDeath() != null ? patient.getCauseOfDeath().getName()
			        .getName() : "");
		}
		catch (Exception e) {
			String message = "Error while getting cause of death for patient with id: " + patient.getPatientId();
			//			throw new RuntimeException(message, e);
			logger.severe(message);
			writeErrorToFile(message, "patient_" + patient.getPatientId() + "_" + errorFileName, reportFolderGlobal);
		}
		demographicsType.setFirstName(patient.getGivenName() != null ? Security.encrypt(patient.getGivenName(), initVector,
		    secretKey) : "");
		demographicsType.setLastName(patient.getFamilyName() != null ? Security.encrypt(patient.getFamilyName(), initVector,
		    secretKey) : "");
		demographicsType.setMiddleName(patient.getMiddleName() != null ? Security.encrypt(patient.getMiddleName(),
		    initVector, secretKey) : "");
		PersonAttribute personAttribute = patient.getAttribute(8);
		demographicsType.setPhoneNumber(personAttribute != null ? Security.encrypt(personAttribute.getValue(), initVector,
		    secretKey) : "");
		demographicsType.setGender(patient.getGender());
		demographicsType.setPatientUuid(patient.getUuid());
		demographicsType.setPatientId(patient.getPersonId());
		demographicsType.setDatimId(getDatimCode());
		demographicsType.setDateChanged(patient.getDateChanged());
		return demographicsType;
	}
	
	private void setPatientTouchTime(Date[] touchTime, Date dateChanged, Date dateCreated) {
		if (dateChanged == null && dateCreated == null)
			return;
		
		if (dateChanged != null) {
			if (touchTime[0] == null || touchTime[0].before(dateChanged))
				touchTime[0] = dateChanged;
			return;
		}
		if (touchTime[0] == null || touchTime[0].before(dateCreated))
			touchTime[0] = dateCreated;
		
	}
	
	private List<VisitType> buildVisits(Patient patient, Date[] touchTime) {
        List<VisitType> visitTypes = new ArrayList<>();
        List<Visit> visits = encounterService.getVisitsByPatientId(patient.getPatientId());
        if (visits != null && !visits.isEmpty()) {
            buildContainerVisitType(patient, touchTime, visitTypes, visits);
        }
        return visitTypes;
    }
	
	private void buildContainerVisitType(Patient patient, Date[] touchTime, List<VisitType> visitTypes, List<Visit> visits) {
        visits.forEach(visit -> {
            VisitType visitType = new VisitType();
            visitType.setVisitId(visit.getVisitId());
            visitType.setPatientId(visit.getPatient().getPatientId());
            visitType.setVisitTypeId(visit.getVisitType() != null ? visit.getVisitType().getVisitTypeId() : 0);
            visitType.setDateStarted(visit.getStartDatetime());
            try {
                visitType.setCreator(visit.getCreator().getId());
            } catch (Exception e) {
                String message = "Error while getting creator user id for visit with id: " + visit.getVisitId();
//                throw new RuntimeException(message, e);
                logger.severe(message);
                writeErrorToFile(message, "patient_" + patient.getPatientId() + "_" + errorFileName, reportFolderGlobal);
            }
            visitType.setDateStopped(visit.getStopDatetime());
            visitType.setDateCreated(visit.getDateCreated());
            try {
                visitType.setChangedBy(visit.getChangedBy() != null ? visit.getChangedBy().getId() : 0);
            } catch (Exception e) {
                String message = "Error while getting changed by user id for visit with id: " + visit.getVisitId();
//                throw new RuntimeException(message, e);
                logger.severe(message);
                writeErrorToFile(message, "patient_" + patient.getPatientId() + "_" + errorFileName, reportFolderGlobal);
            }
            visitType.setDateChanged(visit.getDateChanged());
            visitType.setVoided(visit.getVoided() ? 1 : 0);
            try {
                visitType.setVoidedBy(visit.getVoided() ? visit.getVoidedBy().getId() : 0);
            } catch (Exception e) {
                String message = "Error while getting voided by user id for visit with id: " + visit.getVisitId();
//                throw new RuntimeException(message, e);
                logger.severe(message);
                writeErrorToFile(message, "patient_" + patient.getPatientId() + "_" + errorFileName, reportFolderGlobal);
            }

            visitType.setDateVoided(visit.getDateVoided());
            visitType.setVisitUuid(visit.getUuid());
            visitType.setLocationId(visit.getLocation() != null ? visit.getLocation().getLocationId() : 0);
            visitType.setPatientUuid(patient.getPerson().getUuid());
            visitType.setDatimId(getDatimCode());
            visitTypes.add(visitType);
            if (visit.getDateChanged() != null) {
                if (touchTime[0] == null || touchTime[0].before(visit.getDateChanged()))
                    touchTime[0] = visit.getDateChanged();
            } else {
                if (visit.getDateCreated() != null && (touchTime[0] == null || touchTime[0].before(visit.getDateCreated())))
                    touchTime[0] = visit.getDateCreated();
            }
            if (visit.getDateVoided() != null && (touchTime[0] == null || touchTime[0].before(visit.getDateVoided()))) {
                touchTime[0] = visit.getDateVoided();
            }
        });
    }
	
	private List<PatientBiometricType> buildPatientBiometrics(Patient patient, Date[] touchTime) {
        List<PatientBiometricType> patientBiometricTypes = new ArrayList<>();
        List<BiometricInfo> biometricInfos = biometricInfoService.getBiometricInfoByPatientId(patient.getPatientId());
        if (biometricInfos != null && !biometricInfos.isEmpty()) {
            buildContainerBiometricType(patient, touchTime, patientBiometricTypes, biometricInfos);
        }
        return patientBiometricTypes;
    }
	
	private void buildContainerBiometricType(Patient patient, Date[] touchTime, List<PatientBiometricType> patientBiometricTypes, List<BiometricInfo> biometricInfos) {
        biometricInfos.forEach(biometricInfo -> {
            PatientBiometricType patientBiometricType = new PatientBiometricType();
            patientBiometricType.setBiometricInfoId(biometricInfo.getBiometricInfoId());
            patientBiometricType.setPatientId(patient.getPatientId());
            patientBiometricType.setCreator(biometricInfo.getCreator());
            patientBiometricType.setPatientUuid(patient.getPerson().getUuid());
            patientBiometricType.setDateCreated(biometricInfo.getDateCreated());
            patientBiometricType.setDatimId(getDatimCode());
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
            if (touchTime[0] == null || touchTime[0].before(biometricInfo.getDateCreated()))
                touchTime[0] = biometricInfo.getDateCreated();
        });
    }
	
	private List<PatientBiometricVerificationType> buildPatientBiometricVerifications(Patient patient, Date[] touchTime) {
        List<PatientBiometricVerificationType> patientBiometricTypes = new ArrayList<>();
        List<BiometricVerificationInfo> biometricInfos = biometricVerificationInfoService.getBiometricVerificationInfoByPatientId(patient.getPatientId());
        if (biometricInfos != null && !biometricInfos.isEmpty()) {
            buildContainerBiometricVerificationType(patient, touchTime, patientBiometricTypes, biometricInfos);
        }
        return patientBiometricTypes;
    }
	
	private void buildContainerBiometricVerificationType(Patient patient, Date[] touchTime, List<PatientBiometricVerificationType> patientBiometricTypes, List<BiometricVerificationInfo> biometricInfos) {
        biometricInfos.forEach(biometricInfo -> {
            PatientBiometricVerificationType patientBiometricType = new PatientBiometricVerificationType();
            patientBiometricType.setBiometricInfoId(biometricInfo.getBiometricInfoId());
            patientBiometricType.setPatientId(patient.getPatientId());
            patientBiometricType.setCreator(biometricInfo.getCreator());
            patientBiometricType.setPatientUuid(patient.getPerson().getUuid());
            patientBiometricType.setDateCreated(biometricInfo.getDateCreated());
            patientBiometricType.setDatimId(getDatimCode());
            patientBiometricType.setFingerPosition(biometricInfo.getFingerPosition());
            patientBiometricType.setImageDpi(biometricInfo.getImageDPI());
            patientBiometricType.setImageHeight(biometricInfo.getImageHeight());
            patientBiometricType.setImageQuality(biometricInfo.getImageQuality());
            patientBiometricType.setImageWidth(biometricInfo.getImageWidth());
            patientBiometricType.setManufacturer(biometricInfo.getManufacturer());
            patientBiometricType.setModel(biometricInfo.getModel());
            patientBiometricType.setSerialNumber(biometricInfo.getSerialNumber());
            patientBiometricType.setTemplate(biometricInfo.getTemplate());
            patientBiometricType.setEncodedTemplate(biometricInfo.getEncodedTemplate());
            patientBiometricType.setRecaptureCount(biometricInfo.getRecaptureCount());
            patientBiometricType.setHashed(biometricInfo.getHashed());
            patientBiometricTypes.add(patientBiometricType);
            if (touchTime[0] == null || touchTime[0].before(biometricInfo.getDateCreated()))
                touchTime[0] = biometricInfo.getDateCreated();
        });
    }
	
	private List<PatientProgramType> buildPatientProgram(Patient patient, Date[] touchTime) {
        List<PatientProgramType> patientProgramTypes = new ArrayList<>();
        List<PatientProgram> patientPrograms = encounterService.getPatientProgramsByPatientId(patient.getPatientId());
//        List<PatientProgram> patientPrograms = Context.getProgramWorkflowService().getPatientPrograms(
//                patient, null, null, null, null, null, true
//        );
        if (patientPrograms != null && !patientPrograms.isEmpty()) {
            buildContainerPatientProgramType(touchTime, patientProgramTypes, patientPrograms);
        }
        return patientProgramTypes;
    }
	
	private void buildContainerPatientProgramType(Date[] touchTime, List<PatientProgramType> patientProgramTypes, List<PatientProgram> patientPrograms) {
        patientPrograms.forEach(patientProgram -> {
            PatientProgramType patientProgramType = new PatientProgramType();
            patientProgramType.setPatientProgramId(patientProgram.getPatientProgramId());
            patientProgramType.setPatientId(patientProgram.getPatient().getPatientId());
            try {
                patientProgramType.setProgramId(patientProgram.getProgram().getProgramId());
                patientProgramType.setProgramName(patientProgram.getProgram().getName());
            } catch (Exception e) {
                String message = "Error while getting program id for patient program with id: " + patientProgram.getPatientProgramId();
//                throw new RuntimeException(message, e);
                logger.severe(message);
                writeErrorToFile(message, "patient_" + patientProgram.getPatient().getPatientId() + "_" + errorFileName, reportFolderGlobal);
            }
            patientProgramType.setDateEnrolled(patientProgram.getDateEnrolled());
            patientProgramType.setDateCompleted(patientProgram.getDateCompleted());
            try {
                patientProgramType.setOutcomeConceptId(patientProgram.getOutcome() == null ? 0 : patientProgram.getOutcome().getConceptId());
            } catch (Exception e) {
                String message = "Error while getting outcome concept id for patient program with id: " + patientProgram.getPatientProgramId();
//                throw new RuntimeException(message, e);
                logger.severe(message);
                writeErrorToFile(message, "patient_" + patientProgram.getPatient().getPatientId() + "_" + errorFileName, reportFolderGlobal);
            }
            try {
                patientProgramType.setCreator(patientProgram.getCreator().getId());
            } catch (Exception e) {
                String message = "Error while getting creator user id for patient program with id: " + patientProgram.getPatientProgramId();
//                throw new RuntimeException(message, e);
                logger.severe(message);
                writeErrorToFile(message, "patient_" + patientProgram.getPatient().getPatientId() + "_" + errorFileName, reportFolderGlobal);
            }
            patientProgramType.setDateCreated(patientProgram.getDateCreated());
            patientProgramType.setDateChanged(patientProgram.getDateChanged());
            patientProgramType.setChangedBy(patientProgram.getChangedBy() != null ? patientProgram.getChangedBy().getId() : 0);
            patientProgramType.setVoided(patientProgram.getVoided() ? 1 : 0);
            try {
                patientProgramType.setVoidedBy(patientProgram.getVoided() ? patientProgram.getVoidedBy().getId() : 0);
            } catch (Exception e) {
                String message = "Error while getting voided by user id for patient program with id: " + patientProgram.getPatientProgramId();
//                throw new RuntimeException(message, e);
                logger.severe(message);
                writeErrorToFile(message, "patient_" + patientProgram.getPatient().getPatientId() + "_" + errorFileName, reportFolderGlobal);
            }
            patientProgramType.setDateVoided(patientProgram.getDateVoided());
            patientProgramType.setPatientProgramUuid(patientProgram.getUuid());
            patientProgramType.setPatientUuid(patientProgram.getPatient().getPerson().getUuid());
            patientProgramType.setLocationId(patientProgram.getLocation() != null ?
                    patientProgram.getLocation().getLocationId() : 0);
            patientProgramType.setDatimId(getDatimCode());
            patientProgramTypes.add(patientProgramType);
            updatePatientTouchTime(touchTime, patientProgram.getDateChanged(), patientProgram.getDateCreated(), patientProgram.getDateVoided());
        });
    }
	
	private List<PatientIdentifierType> buildPatientIdentifier (Patient patient, Date[] touchTime) {
        List<PatientIdentifierType> patientIdentifierTypes = new ArrayList<>();
        Set<PatientIdentifier> patientIdentifiers = patient.getIdentifiers();
        if (patientIdentifiers != null && !patientIdentifiers.isEmpty()) {
            buildContainerPatientIdentifier(patient, touchTime, patientIdentifierTypes, patientIdentifiers);
        }
        return patientIdentifierTypes;
    }
	
	private void buildContainerPatientIdentifier(Patient patient, Date[] touchTime, List<PatientIdentifierType> patientIdentifierTypes, Set<PatientIdentifier> updatedPatientIdentifiers) {
        updatedPatientIdentifiers.forEach(patientIdentifier -> {
            PatientIdentifierType patientIdentifierType = new PatientIdentifierType();
            patientIdentifierType.setPatientIdentifierId(patientIdentifier.getPatientIdentifierId());
            patientIdentifierType.setPatientId(patientIdentifier.getPatient().getPatientId());
            patientIdentifierType.setIdentifier(patientIdentifier.getIdentifier());
            try {
                patientIdentifierType.setIdentifierType(patientIdentifier.getIdentifierType().getId());
            } catch (Exception e) {
                String message = "Error while getting identifier type id for patient identifier with id: " + patientIdentifier.getPatientIdentifierId();
//                throw new RuntimeException(message, e);
                logger.severe(message);
                writeErrorToFile(message, "patient_" + patient.getPatientId() + "_" + errorFileName, reportFolderGlobal);
            }
            patientIdentifierType.setPreferred(patientIdentifier.getPreferred() ? 1 : 0);
            try {
                patientIdentifierType.setCreator(patientIdentifier.getCreator().getId());
            } catch (Exception e) {
                String message = "Error while getting creator user id for patient identifier with id: " + patientIdentifier.getPatientIdentifierId();
//                throw new RuntimeException(message, e);
                logger.severe(message);
                writeErrorToFile(message, "patient_" + patient.getPatientId() + "_" + errorFileName, reportFolderGlobal);
            }
            patientIdentifierType.setPatientIdentifierUuid(patientIdentifier.getUuid());
            patientIdentifierType.setDateChanged(patientIdentifier.getDateChanged());
            patientIdentifierType.setDateCreated(patientIdentifier.getDateCreated());
            patientIdentifierType.setChangedBy(patientIdentifier.getChangedBy() != null ? patientIdentifier.getChangedBy().getId() : 0);
            patientIdentifierType.setVoided(patientIdentifier.getVoided() ? 1 : 0);
            try {
                patientIdentifierType.setVoidedBy(patientIdentifier.getVoided() ? patientIdentifier.getVoidedBy().getId() : 0);
            } catch (Exception e) {
                String message = "Error while getting voided by user id for patient identifier with id: " + patientIdentifier.getPatientIdentifierId();
//                throw new RuntimeException(message, e);
                logger.severe(message);
                writeErrorToFile(message, "patient_" + patient.getPatientId() + "_" + errorFileName, reportFolderGlobal);
            }
            patientIdentifierType.setDateVoided(patientIdentifier.getDateVoided());
            patientIdentifierType.setDatimId(getDatimCode());
            patientIdentifierType.setPatientUuid(patient.getUuid());
            patientIdentifierTypes.add(patientIdentifierType);
            updatePatientTouchTime(touchTime, patientIdentifier.getDateChanged(), patientIdentifier.getDateCreated(), patientIdentifier.getDateVoided());
        });
    }
	
	private List<EncounterType> buildEncounters(
            Patient patient, List<EncounterProvider> providers, Date[] touchTime, List<Obs> obsList
    ) {
        List<EncounterType> encounterTypes = new ArrayList<>();
//            List<Encounter> encounterList = Context.getEncounterService().getEncounters(patient, null, null, null, null, null, null, null, null, true);
        List<Encounter> encounterList = encounterService.getEncountersByPatientId(patient.getPatientId());
        if (encounterList != null && !encounterList.isEmpty())
            buildContainerEncounterType(patient, providers, touchTime, encounterTypes, encounterList, obsList);
        return encounterTypes;
    }
	
	private void buildContainerEncounterType(
            Patient patient, List<EncounterProvider> providers, Date[] touchTime,
            List<EncounterType> encounterTypes, List<Encounter> encounters, List<Obs> obsList
    ) {
        encounters.forEach(encounter -> {
            EncounterType encounterType = new EncounterType();
            encounterType.setPatientUuid(patient.getPerson().getUuid());
            encounterType.setDatimId(getDatimCode());
            if (encounter.getVisit() != null) {
                encounterType.setVisitId(encounter.getVisit().getVisitId());
                encounterType.setVisitUuid(encounter.getVisit().getUuid());
            }
            encounterType.setEncounterUuid(encounter.getUuid());
            encounterType.setEncounterId(encounter.getEncounterId());
            try {
                encounterType.setEncounterTypeId(encounter.getEncounterType().getEncounterTypeId());
            } catch (Exception e) {
                String message = "Error setting encounter type for encounter " + encounter.getEncounterId() + " for patient " + patient.getPatientId();
//                throw new RuntimeException(message, e);
                logger.severe(message);
                writeErrorToFile(message, "patient_" + patient.getPatientId() + "_" + errorFileName, reportFolderGlobal);
            }

            encounterType.setPatientId(patient.getPatientId());
            encounterType.setLocationId(encounter.getLocation() != null ? encounter.getLocation().getLocationId() : 0);
            try {
                encounterType.setFormId(encounter.getForm().getFormId());
                encounterType.setPmmForm(encounter.getForm().getName());
            } catch (Exception e) {
                String message = "Error setting form for encounter " + encounter.getEncounterId() + " for patient " + patient.getPatientId();
//                throw new RuntimeException(message, e);
                logger.severe(message);
                writeErrorToFile(message, "patient_" + patient.getPatientId() + "_" + errorFileName, reportFolderGlobal);
            }

            encounterType.setEncounterDatetime(encounter.getEncounterDatetime());
            try {
                encounterType.setCreator(encounter.getCreator().getId());
            } catch (Exception e) {
                String message = "Error while getting creator user id for encounter with id: " + encounter.getEncounterId();
//                throw new RuntimeException(message, e);
                logger.severe(message);
                writeErrorToFile(message, "patient_" + patient.getPatientId() + "_" + errorFileName, reportFolderGlobal);
            }
            encounterType.setDateCreated(encounter.getDateCreated());
            encounterType.setChangedBy(encounter.getChangedBy() != null ? encounter.getChangedBy().getId() : 0);
            encounterType.setDateChanged(encounter.getDateChanged());
            encounterType.setVoided(encounter.getVoided() ? 1 : 0);
            try {
                encounterType.setVoidedBy(encounter.getVoided() ? encounter.getVoidedBy().getId() : 0);
            } catch (Exception e) {
                String message = "Error while getting voided by user id for encounter with id: " + encounter.getEncounterId();
//                throw new RuntimeException(message, e);
                logger.severe(message);
                writeErrorToFile(message, "patient_" + patient.getPatientId() + "_" + errorFileName, reportFolderGlobal);
            }
            encounterType.setDateVoided(encounter.getDateVoided());
            encounterTypes.add(encounterType);
            Set<EncounterProvider> encounterProviders = encounter.getEncounterProviders();
            if (!encounterProviders.isEmpty()) {
                providers.addAll(encounterProviders);
            }
            updatePatientTouchTime(touchTime, encounter.getDateChanged(), encounter.getDateCreated(), encounter.getDateVoided());
            Set<Obs> obsSet = encounter.getAllObs(true);
            if (obsSet != null && !obsSet.isEmpty()) {
                obsList.addAll(obsSet);
            }
        });
    }
	
	private void updatePatientTouchTime(Date[] touchTime, Date dateChanged, Date dateCreated, Date dateVoided) {
		if (dateChanged == null && dateVoided == null && dateCreated == null)
			return;
		if (touchTime[0] == null) {
			if (dateChanged != null)
				touchTime[0] = dateChanged;
			else if (dateVoided != null)
				touchTime[0] = dateVoided;
			else
				touchTime[0] = dateCreated;
		}
		if (dateChanged != null && touchTime[0].before(dateChanged)) {
			touchTime[0] = dateChanged;
		} else {
			if (dateCreated != null && touchTime[0].before(dateCreated))
				touchTime[0] = dateCreated;
		}
		if (dateVoided != null && touchTime[0].before(dateVoided))
			touchTime[0] = dateVoided;
		
	}
	
	private List<ObsType> buildObs(Patient patient, Date[] touchTime, List<Obs> obsList) {
        List<ObsType> obsTypeList = new ArrayList<>();
        if (obsList != null && !obsList.isEmpty())
            buildContainerObsType(patient, touchTime, obsTypeList, obsList);
        return obsTypeList;
    }
	
	private void buildContainerObsType(Patient patient, Date[] touchTimes, List<ObsType> obsTypeList, List<Obs> obsList) {
        List<Integer> confidentialConcepts = AppUtil.getConfidentialConcepts(); //todo get from global property
        obsList.forEach(obs -> {
            ObsType obsType = new ObsType();
            obsType.setPatientUuid(patient.getUuid());
            obsType.setDatimId(getDatimCode());
            obsType.setObsUuid(obs.getUuid());
            obsType.setObsId(obs.getObsId());
            obsType.setPersonId(obs.getPersonId());
            try {
                obsType.setConceptId(obs.getConcept().getConceptId());
            } catch (Exception e) {
                String message = "Error setting concept id for obs " + obs.getObsId() + " for patient " + patient.getPatientId();
//                throw new RuntimeException(message, e);
                logger.severe(message);
                writeErrorToFile(message, "patient_" + patient.getPatientId() + "_" + errorFileName, reportFolderGlobal);
            }
            try {
                obsType.setVariableName(obs.getConcept().getName().getName());
            } catch (Exception e) {
                String message = "Error setting concept name for obs " + obs.getObsId() + " for patient " + patient.getPatientId();
//                throw new RuntimeException(message, e);
                logger.severe(message);
                writeErrorToFile(message, "patient_" + patient.getPatientId() + "_" + errorFileName, reportFolderGlobal);
            }
            try {
                obsType.setDatatype(obs.getConcept().getDatatype().getConceptDatatypeId());
            } catch (Exception e) {
                String message = "Error setting concept datatype for obs " + obs.getObsId() + " for patient " + patient.getPatientId();
//                throw new RuntimeException(message, e);
                logger.severe(message);
                writeErrorToFile(message, "patient_" + patient.getPatientId() + "_" + errorFileName, reportFolderGlobal);
            }
            obsType.setEncounterId(obs.getEncounter().getEncounterId());
            obsType.setEncounterUuid(obs.getEncounter().getUuid());
            try {
                obsType.setPmmForm(obs.getEncounter().getForm().getName());
                obsType.setFormId(obs.getEncounter().getForm().getFormId());
            } catch (Exception e) {
                String message = "Error setting form for obs " + obs.getObsId() + " for patient " + patient.getPatientId();
//                throw new RuntimeException(message, e);
                logger.severe(message);
                writeErrorToFile(message, "patient_" + patient.getPatientId() + "_" + errorFileName, reportFolderGlobal);
            }
            try {
                obsType.setEncounterType(obs.getEncounter().getEncounterType().getEncounterTypeId());
            } catch (Exception e) {
                String message = "Error setting encounter type for obs " + obs.getObsId() + " for patient " + patient.getPatientId();
//                throw new RuntimeException(message, e);
                logger.severe(message);
                writeErrorToFile(message, "patient_" + patient.getPatientId() + "_" + errorFileName, reportFolderGlobal);
            }
            obsType.setVisitUuid(obs.getEncounter().getVisit() != null ? obs.getEncounter().getVisit().getUuid() : "");
            obsType.setObsDatetime(obs.getObsDatetime());
            obsType.setObsGroupId(obs.getObsGroup() == null ? 0 : obs.getObsGroup().getObsId());
            try {
                obsType.setValueCoded(obs.getValueCoded() != null ? obs.getValueCoded().getConceptId() : 0);
            } catch (Exception e) {
                String message = "Error setting value coded for obs " + obs.getObsId() + " for patient " + patient.getPatientId();
//                throw new RuntimeException(message, e);
                logger.severe(message);
                writeErrorToFile(message, "patient_" + patient.getPatientId() + "_" + errorFileName, reportFolderGlobal);
            }
            obsType.setValueDatetime(obs.getValueDatetime());
            obsType.setValueNumeric(obs.getValueNumeric() != null ?
                    BigDecimal.valueOf(obs.getValueNumeric()) : null);
            if (confidentialConcepts.contains(obsType.getConceptId())) {
                obsType.setValueText(obs.getValueText() != null ? Security.encrypt(obs.getValueText(), initVector, secretKey) : "");
                String variableValue = "";
                if (obs.getValueCoded() != null) {
                    try {
                        variableValue = Security.encrypt(obs.getValueCoded().getName().getName(), initVector, secretKey);
                    } catch (Exception e) {
                        String message = "Error setting value coded name for obs " + obs.getObsId() + " for patient " + patient.getPatientId();
//                        throw new RuntimeException(message, e);
                        logger.severe(message);
                        writeErrorToFile(message, "patient_" + patient.getPatientId() + "_" + errorFileName, reportFolderGlobal);
                    }
                } else if (obs.getValueText() != null) {
                    variableValue = Security.encrypt(obs.getValueText(), initVector, secretKey);
                } else if (obs.getValueDatetime() != null) {
                    variableValue = Security.encrypt(String.valueOf(obs.getValueDatetime()), initVector, secretKey);
                } else if (obs.getValueNumeric() != null) {
                    variableValue = Security.encrypt(String.valueOf(obs.getValueNumeric()), initVector, secretKey);
                }
                obsType.setVariableValue(variableValue);
            } else {
                obsType.setValueText(obs.getValueText());
                String variableValue = "";
                if (obs.getValueCoded() != null) {
                    try {
                        variableValue = obs.getValueCoded().getName().getName();
                    } catch (Exception e) {
                        String message = "Error setting value coded name for obs " + obs.getObsId() + " for patient " + patient.getPatientId();
//                        throw new RuntimeException(message, e);
                        logger.severe(message);
                        writeErrorToFile(message, "patient_" + patient.getPatientId() + "_" + errorFileName, reportFolderGlobal);
                    }
                } else if (obs.getValueText() != null) {
                    variableValue = obs.getValueText();
                } else if (obs.getValueDatetime() != null) {
                    variableValue = String.valueOf(obs.getValueDatetime());
                } else if (obs.getValueNumeric() != null) {
                    variableValue = String.valueOf(obs.getValueNumeric());
                }
                obsType.setVariableValue(variableValue);
            }
            try {
                obsType.setCreator(obs.getCreator().getId());
            } catch (Exception e) {
                String message = "Error while getting creator user id for obs with id: " + obs.getObsId();
//                throw new RuntimeException(message, e);
                logger.severe(message);
                writeErrorToFile(message, "patient_" + patient.getPatientId() + "_" + errorFileName, reportFolderGlobal);
            }
            obsType.setDateCreated(obs.getDateCreated());

            obsType.setLocationId(obs.getLocation() != null ? obs.getLocation().getLocationId() : 0);
            obsType.setVoided(obs.getVoided() ? 1 : 0);
            try {
                obsType.setVoidedBy(obs.getVoided() ? obs.getVoidedBy().getId() : 0);
            } catch (Exception e) {
                String message = "Error while getting voided by user id for obs with id: " + obs.getObsId();
//                throw new RuntimeException(message, e);
                logger.severe(message);
                writeErrorToFile(message, "patient_" + patient.getPatientId() + "_" + errorFileName, reportFolderGlobal);
            }
            obsType.setDateVoided(obs.getDateVoided());
            obsTypeList.add(obsType);
            updatePatientTouchTime(touchTimes, obs.getDateChanged(), obs.getDateCreated(), obs.getDateVoided());
        });
    }
	
	private List<EncounterProviderType> buildEncounterProviders(List<EncounterProvider> providers, Patient patient, Date[] touchTimes) {
        List<EncounterProviderType> encounterProviderTypes = new ArrayList<>();
        if (providers != null && !providers.isEmpty()) {
            providers.forEach(encounterProvider -> {
                EncounterProviderType providerType = new EncounterProviderType();
                providerType.setEncounterProviderId(encounterProvider.getEncounterProviderId());
                providerType.setEncounterId(encounterProvider.getEncounter().getEncounterId());
                try {
                    providerType.setProviderId(encounterProvider.getProvider().getProviderId());
                } catch (Exception e) {
                    String message = "Error while getting provider id for encounter provider with id: " + encounterProvider.getEncounterProviderId();
//                    throw new RuntimeException(message, e);
                    logger.severe(message);
                    writeErrorToFile(message, "patient_" + patient.getPatientId() + "_" + errorFileName, reportFolderGlobal);
                }
                try {
                    providerType.setEncounterRoleId(encounterProvider.getEncounterRole().getEncounterRoleId());
                } catch (Exception e) {
                    String message = "Error while getting encounter role id for encounter provider with id: " + encounterProvider.getEncounterProviderId();
//                    throw new RuntimeException(message, e);
                    logger.severe(message);
                    writeErrorToFile(message, "patient_" + patient.getPatientId() + "_" + errorFileName, reportFolderGlobal);
                }
                try {
                    providerType.setCreator(encounterProvider.getCreator().getId());
                } catch (Exception e) {
                    String message = "Error while getting creator user id for encounter provider with id: " + encounterProvider.getEncounterProviderId();
//                    throw new RuntimeException(message, e);
                    logger.severe(message);
                    writeErrorToFile(message, "patient_" + patient.getPatientId() + "_" + errorFileName, reportFolderGlobal);
                }
                providerType.setDateCreated(encounterProvider.getDateCreated());
                try {
                    providerType.setChangedBy(encounterProvider.getChangedBy() != null ? encounterProvider.getChangedBy().getId() : 0);
                } catch (Exception e) {
                    String message = "Error while getting changed by user id for encounter provider with id: " + encounterProvider.getEncounterProviderId();
//                    throw new RuntimeException(message, e);
                    logger.severe(message);
                    writeErrorToFile(message, "patient_" + patient.getPatientId() + "_" + errorFileName, reportFolderGlobal);
                }
                providerType.setDateChanged(encounterProvider.getDateChanged());
                providerType.setVoided(encounterProvider.getVoided() ? 1 : 0);
                providerType.setDateVoided(encounterProvider.getDateVoided());
                try {
                    providerType.setVoidedBy(encounterProvider.getVoided() ? encounterProvider.getVoidedBy().getId() : 0);
                } catch (Exception e) {
                    String message = "Error while getting voided by user id for encounter provider with id: " + encounterProvider.getEncounterProviderId();
//                    throw new RuntimeException(message, e);
                    logger.severe(message);
                    writeErrorToFile(message, "patient_" + patient.getPatientId() + "_" + errorFileName, reportFolderGlobal);
                }
                providerType.setVoidedReason(encounterProvider.getVoidReason());
                providerType.setEncounterProviderUuid(encounterProvider.getUuid());
                providerType.setEncounterUuid(encounterProvider.getEncounter().getUuid());
                providerType.setVisitUuid(encounterProvider.getEncounter().getVisit() != null ? encounterProvider.getEncounter().getVisit().getUuid() : null);
                providerType.setLocationId(encounterProvider.getEncounter().getLocation() != null ? encounterProvider.getEncounter().getLocation().getLocationId() : 0);
                providerType.setPatientUuid(patient.getUuid());
                providerType.setDatimId(getDatimCode());
                encounterProviderTypes.add(providerType);
                updatePatientTouchTime(touchTimes, encounterProvider.getDateChanged(), encounterProvider.getDateCreated(), encounterProvider.getDateVoided());
            });
        }
        return encounterProviderTypes;
    }
}
