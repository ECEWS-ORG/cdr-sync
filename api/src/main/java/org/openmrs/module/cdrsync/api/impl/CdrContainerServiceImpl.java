package org.openmrs.module.cdrsync.api.impl;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.*;
import org.openmrs.api.*;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.cdrsync.CdrsyncConfig;
import org.openmrs.module.cdrsync.api.*;
import org.openmrs.module.cdrsync.container.model.EncounterType;
import org.openmrs.module.cdrsync.container.model.PatientIdentifierType;
import org.openmrs.module.cdrsync.container.model.VisitType;
import org.openmrs.module.cdrsync.container.model.*;
import org.openmrs.module.cdrsync.model.*;
import org.openmrs.module.cdrsync.model.enums.SyncType;
import org.openmrs.module.cdrsync.utils.AppUtil;
import org.openmrs.util.Security;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CdrContainerServiceImpl extends BaseOpenmrsService implements CdrContainerService {
	
	private final PatientService patientService = Context.getPatientService();
	
	private final VisitService visitService = Context.getVisitService();
	
	private final ProgramWorkflowService programWorkflowService = Context.getProgramWorkflowService();
	
	private final ObsService obsService = Context.getObsService();
	
	private final EncounterService encounterService = Context.getEncounterService();
	
	private final BiometricInfoService biometricInfoService = Context.getService(BiometricInfoService.class);
	
	private final CdrSyncEncounterService cdrSyncEncounterService = Context.getService(CdrSyncEncounterService.class);
	
	private static final String datimCode = Context.getAdministrationService().getGlobalProperty("facility_datim_code");
	
	private final String facilityName = Context.getAdministrationService().getGlobalProperty("Facility_Name");
	
	private final User user = Context.getAuthenticatedUser();
	
	private final String partnerShortName = Context.getAdministrationService().getGlobalProperty("partner_short_name");
	
	private static final ObjectMapper objectMapper = new ObjectMapper();
	
	private static final String operatingSystem = System.getProperty("os.name").toLowerCase();
	
	private static final String desktopPath;
	
	private static final String folderName = "Sync Module Folder";
	
	private static final String folderPath;
	
	private static final DatimMap datimMap;
	
	static {
		if (operatingSystem.contains("windows")) {
			desktopPath = System.getenv("USERPROFILE") + "\\Desktop\\";
		} else {
			desktopPath = System.getProperty("user.home") + "/Desktop/";
		}
		System.out.println("desktop path: " + desktopPath);
		folderPath = desktopPath + folderName;
		File dir1 = new File(desktopPath, folderName);
		if (!dir1.exists() && !dir1.mkdirs()) {
			throw new RuntimeException("Unable to create directory " + dir1.getAbsolutePath());
		}
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		objectMapper.setDateFormat(df);
		
		datimMap = Context.getService(CdrSyncPatientService.class).getDatimMapByDatimCode(datimCode);
	}
	
	public static void initialize(UserContext userContext) {
		Context.openSession();
		if (userContext != null) {
			Context.setUserContext(userContext);
		} else {
			Context.setUserContext(Context.getUserContext());
		}
		Context.openSessionWithCurrentUser();
		Context.addProxyPrivilege(CdrsyncConfig.MODULE_PRIVILEGE);
		Context.addProxyPrivilege(CdrsyncConfig.MODULE_PRIVILEGE);
		Context.addProxyPrivilege(CdrsyncConfig.MODULE_PRIVILEGE);
		Context.addProxyPrivilege("Get Patients");
		Context.addProxyPrivilege("Get Observations");
		Context.addProxyPrivilege("Get Encounters");
		Context.addProxyPrivilege("Get Concepts");
		Context.addProxyPrivilege("Get Users");
		Context.addProxyPrivilege("Get Identifier Types");
		Context.addProxyPrivilege("Manage Global Properties");
	}
	
	@Override
	public String getAllPatients(Long patientCount, int start, int length, String type, String fullContextPath) {
		String result;
		System.out.println("Total no of patients:: " + patientCount);
		String reportType = "CDR";
		String reportFolder = AppUtil.ensureReportDirectoryExists(fullContextPath, reportType, start);
		if (start < patientCount) {
			List<Integer> patients = Context.getService(CdrSyncPatientService.class).getPatientIds(start, length, true);
			System.out.println("Total no of patients processing:: " + patients.size());
			result = buildContainer(patients, reportFolder);
			return result;
		} else {
			return zipFolder(type, reportFolder);
		}
	}
	
	@Override
	public List<Integer> getAllPatients(boolean includeVoided) {
		return Context.getService(CdrSyncPatientService.class).getPatientIds(includeVoided);
	}
	
	@Override
	public String getAllPatients(Long patientCount, Date startDate, Date endDate, Integer start, Integer length,
	        String type, String fullContextPath) {
		String result;
		String reportType = "CDR";
		String reportFolder = AppUtil.ensureReportDirectoryExists(fullContextPath, reportType, start);
		if (start < patientCount) {
			List<Integer> patients = Context.getService(CdrSyncPatientService.class).getPatientsByLastSyncDate(startDate,
			    endDate, null, true, start, length);
			//			result = buildContainer(patients, startDate, endDate);
			result = buildContainer(patients, reportFolder);
			return result;
		} else {
			return zipFolder(type, reportFolder);
		}
	}
	
	private String zipFolder(String type, String reportFolder) {
		File folder = new File(reportFolder);
		File dir = new File(folder, "jsonFiles");
		StringBuilder result = new StringBuilder();
		String facility = facilityName.replaceAll(" ", "_");
		if (dir.listFiles() != null) {
			ZipOutputStream zipOutputStream;
			try {
				String dateString = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
				zipOutputStream = new ZipOutputStream(Files.newOutputStream(Paths.get(folder.getAbsolutePath(),
				    partnerShortName + "_" + datimCode + "_" + facility + "_" + dateString + "_" + new Date().getTime()
				            + ".zip")));
				zipDirectory(dir, dir.getName(), zipOutputStream);
				zipOutputStream.close();
				FileUtils.deleteDirectory(dir);
				if (!type.equals(SyncType.CUSTOM.name())) {
					saveLastSyncDate();
				}
				File[] files = folder.listFiles();
				if (files != null) {
					for (File file : files) {
						System.out.println(file.getAbsolutePath());
						if (file.getName().endsWith(".zip")) {
							String filePath = file.getAbsolutePath();
							filePath = filePath.replace("\\", "\\\\");
							result.append(filePath).append("&&");
						}
					}
				} else {
					System.out.println("No files found in the folder");
				}
			}
			catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		return "Sync complete!," + result.toString().trim();
	}
	
	private String buildContainer(List<Integer> patients, Date from, Date to) {
        List<Container> containers = new ArrayList<>();
        String resp;
        AtomicInteger count = new AtomicInteger();
        try {
            patients.forEach(patient ->
			{
				try {
					createContainerFromLastSyncDate(containers, count, patient, from, to);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            resp = "There's a problem connecting to the server. Please, check your connection and try again.";
            return resp;
        }
//        if (!containers.isEmpty()) {
//            try {
//                syncContainersToCdr(containers);
//                containers.clear();
//                resp = "Sync successful!";
//            } catch (IOException e) {
//                System.out.println(e.getMessage());
//                e.printStackTrace();
//                resp = "Incomplete syncing, try again later!";
//            }
//        } else
            resp = "Sync successful!";
        return resp;
    }
	
	private String buildContainer(List<Integer> patientIds, String reportFolder) {
		List<Container> containers = new ArrayList<>();
		String resp;
		AtomicInteger count = new AtomicInteger();
		try {
			patientIds.forEach(patientId -> {
				try {
					createContainer(containers, count, patientId, reportFolder);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
		} catch (RuntimeException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			resp = "There's a problem connecting to the server. Please, check your connection and try again.";
			return resp;
		}
//		if (!containers.isEmpty()) {
//			try {
//				syncContainersToCdr(containers);
//				containers.clear();
//				resp = "Sync successful!";
//			} catch (IOException e) {
//				containers.clear();
//				resp = "Incomplete syncing, try again later!";
//			}
//		} else
			resp = "Sync successful!";
		return resp;
	}
	
	private void createContainer(List<Container> containers, AtomicInteger count, Integer patientId, String reportFolder)
	        throws IOException {
		Patient patient = patientService.getPatient(patientId);
		if (patient != null) {
			System.out.println(count.getAndIncrement());
			Container container = new Container();
			Date[] touchTimeDate = new Date[1];
			touchTimeDate[0] = patient.getDateChanged() != null ? patient.getDateChanged() : patient.getDateCreated();
			container.setMessageHeader(buildMessageHeader());
			container.setMessageData(buildMessageData(patient, touchTimeDate));
			setContainerTouchTimeAndFileName(containers, patient, touchTimeDate, container, reportFolder);
		}
	}
	
	private void createContainerFromLastSyncDate(List<Container> containers, AtomicInteger count, Integer patientId,
	        Date from, Date to) throws IOException {
		System.out.println(count.getAndIncrement());
		Patient patient = patientService.getPatient(patientId);
		Date[] touchTimeDate = new Date[1];
		touchTimeDate[0] = patient.getDateChanged() != null ? patient.getDateChanged() : patient.getDateCreated();
		Container container = new Container();
		container.setMessageData(buildMessageDataFromLastSync(patient, touchTimeDate, from, to));
		container.setMessageHeader(buildMessageHeader());
		//		setContainerTouchTimeAndFileName(containers, patient, touchTimeDate, container, reportFolder); //todo
	}
	
	private void setContainerTouchTimeAndFileName(List<Container> containers, Patient patient, Date[] touchTimes,
	        Container container, String reportFolder) throws IOException {
		container.setId(patient.getUuid());
		container.getMessageHeader().setTouchTime(touchTimes[0]);
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		String touchTimeString = df.format(container.getMessageHeader().getTouchTime());
		String fileName = patient.getUuid() + "_" + touchTimeString + "_" + datimCode + ".json";
		container.getMessageHeader().setFileName(fileName);
		System.out.println("Touch time: " + touchTimeString);
		
		writeContainerToFile(container, fileName, reportFolder);
		
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
	
	private void writeContainerToFile(Container container, String fileName, String reportFolder) throws IOException {
		
		File folder = new File(reportFolder);
		
		File dir = new File(folder, "jsonFiles");
		if (!dir.exists() && !dir.mkdirs()) {
			throw new RuntimeException("Unable to create directory " + dir.getAbsolutePath());
		}
		String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(container);
		File file = new File(dir, fileName);
		FileUtils.writeStringToFile(file, json, "UTF-8");
		
		if (dir.listFiles() != null && Objects.requireNonNull(dir.listFiles()).length == 10000) {
			try {
				String facility = facilityName.replaceAll(" ", "_");
				String dateString = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
				ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(Paths.get(folder.getAbsolutePath(),
				    partnerShortName + "_" + datimCode + "_" + facility + "_" + dateString + "_" + new Date().getTime()
				            + ".zip")));
				zipDirectory(dir, dir.getName(), zos);
				zos.close();
				FileUtils.cleanDirectory(dir);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	private void zipDirectory(File directory, String baseName, ZipOutputStream zos) throws IOException {
		File[] files = directory.listFiles();
		if (files != null) {
			byte[] buffer = new byte[1024];
			for (File file : files) {
				if (file.isDirectory()) {
					String name = baseName + "/" + file.getName();
					zipDirectory(file, name, zos);
				} else {
					FileInputStream fis = new FileInputStream(file);
					zos.putNextEntry(new ZipEntry(baseName + "/" + file.getName()));
					int length;
					while ((length = fis.read(buffer)) > 0) {
						zos.write(buffer, 0, length);
					}
					zos.closeEntry();
					fis.close();
				}
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
			HttpGet get = new HttpGet(url);
			get.setHeader("Content-Type", "application/json");
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
	
	@Override
	public long getPatientsCount(boolean includeVoided) {
		return Context.getService(CdrSyncPatientService.class).getPatientsCount(includeVoided);
	}
	
	@Override
	public long getPatientsCount(Date startDate, Date endDate, boolean includeVoided) {
		return Context.getService(CdrSyncPatientService.class).getPatientCountFromLastSyncDate(startDate, endDate, null,
		    includeVoided);
	}
	
	private MessageHeaderType buildMessageHeader() {
		MessageHeaderType messageHeaderType = new MessageHeaderType();
		messageHeaderType.setFacilityName(facilityName);
		messageHeaderType.setFacilityDatimCode(datimCode);
		if (datimMap != null) {
			messageHeaderType.setFacilityLga(datimMap.getLgaName());
			messageHeaderType.setFacilityState(datimMap.getStateName());
		}
		messageHeaderType.setMessageCreationDateTime(new Date());
		messageHeaderType.setMessageSchemaVersion(new BigDecimal("1.1"));
		messageHeaderType.setMessageStatusCode("SYNCED");
		messageHeaderType.setMessageUniqueID(UUID.randomUUID().toString());
		messageHeaderType.setMessageSource("NMRS");
		return messageHeaderType;
	}
	
	private MessageDataType buildMessageData(Patient patient, Date[] touchTimes) {
		MessageDataType messageDataType = new MessageDataType();
        List<EncounterProvider> providers = new ArrayList<>();
		List<Obs> obsList = new ArrayList<>();
		messageDataType.setDemographics(buildDemographics(patient, touchTimes));
		messageDataType.setVisits(buildVisits(patient, touchTimes));
		messageDataType.setEncounters(buildEncounters(patient, providers, touchTimes, obsList));
		messageDataType.setObs(buildObs(patient, touchTimes, obsList));
		messageDataType.setEncounterProviders(buildEncounterProviders(providers, patient, touchTimes));
		messageDataType.setPatientBiometrics(buildPatientBiometrics(patient, touchTimes));
		messageDataType.setPatientPrograms(buildPatientProgram(patient, touchTimes));
		messageDataType.setPatientIdentifiers(buildPatientIdentifier(patient, touchTimes));
		return messageDataType;
	}
	
	private MessageDataType buildMessageDataFromLastSync(Patient patient, Date[] touchTimes, Date from, Date to) {
        MessageDataType messageData = new MessageDataType();
        List<EncounterProvider> providers = new ArrayList<>();
		List<Obs> obsList = new ArrayList<>();
        messageData.setDemographics(buildDemographics(patient, touchTimes));
        messageData.setVisits(buildVisits(patient, touchTimes, from, to));
		messageData.setEncounters(buildEncounters(patient, providers, touchTimes, obsList, from, to));
		messageData.setObs(buildObs(patient, touchTimes, obsList));
		messageData.setEncounterProviders(buildEncounterProviders(providers, patient, touchTimes));
        messageData.setPatientBiometrics(buildPatientBiometrics(patient, touchTimes, from));
        messageData.setPatientPrograms(buildPatientProgram(patient, touchTimes, from, to));
        messageData.setPatientIdentifiers(buildPatientIdentifier(patient, touchTimes, from));
        return messageData;
    }
	
	private DemographicsType buildDemographics(Patient patient, Date[] touchTimes) {
		DemographicsType demographicsType = new DemographicsType();
		PersonAddress personAddress = patient.getPersonAddress();
		if (personAddress != null)
			setPersonTouchTime(touchTimes, personAddress.getDateChanged(), personAddress.getDateCreated());
		
		PersonName personName = patient.getPersonName();
		if (personName != null)
			setPersonTouchTime(touchTimes, personName.getDateChanged(), personName.getDateCreated());
		
		PersonAttribute personAttribute = patient.getAttribute(8);
		if (personAttribute != null)
			setPersonTouchTime(touchTimes, personAttribute.getDateChanged(), personAttribute.getDateCreated());
		return setContainerDemographics(patient, touchTimes, demographicsType, personAddress);
	}
	
	//	private DemographicsType buildDemographics(Patient patient, Date[] touchTimes) {
	//		DemographicsType demographicsType = new DemographicsType();
	//		PersonAddress personAddress = patient.getPersonAddress();
	//		if (personAddress != null)
	//			checkUpdatedDate(touchTimes, personAddress.getDateChanged(), personAddress.getDateCreated());
	//		PersonName personName = patient.getPersonName();
	//		if (personName != null)
	//			checkUpdatedDate(touchTimes, personName.getDateChanged(), personName.getDateCreated());
	//		PersonAttribute personAttribute = patient.getAttribute(8);
	//		if (personAttribute != null)
	//			checkUpdatedDate(touchTimes, personAttribute.getDateChanged(),
	//			    personAttribute.getDateCreated());
	//		return setContainerDemographics(patient, touchTimes, demographicsType, personAddress);
	//	}
	
	private void checkUpdatedDate(Date[] touchTimes, Date dateChanged, Date dateCreated) {
		if (dateChanged != null) {
			if (touchTimes[0].before(dateChanged))
				touchTimes[0] = dateChanged;
		} else if (dateCreated != null) {
			if (touchTimes[0].before(dateCreated))
				touchTimes[0] = dateCreated;
		}
	}
	
	private DemographicsType setContainerDemographics(Patient patient, Date[] touchTimes, DemographicsType demographicsType,
	        PersonAddress personAddress) {
		if (personAddress != null) {
			if (personAddress.getAddress1() != null && !personAddress.getAddress1().isEmpty()) {
				demographicsType.setAddress1(Security.encrypt(personAddress.getAddress1()));
			}
			if (personAddress.getAddress2() != null && !personAddress.getAddress2().isEmpty()) {
				demographicsType.setAddress2(Security.encrypt(personAddress.getAddress2()));
			}
			demographicsType.setCityVillage(personAddress.getCityVillage());
			demographicsType.setStateProvince(personAddress.getStateProvince());
			demographicsType.setCountry(personAddress.getCountry());
		}
		if (patient.getVoided()) {
			demographicsType.setVoided(1);
			demographicsType.setVoidedBy(patient.getVoidedBy() != null ? patient.getVoidedBy().getId() : 0);
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
		try {
			demographicsType.setChangedBy(patient.getChangedBy() != null ? patient.getChangedBy().getId() : 0);
		}
		catch (Exception e) {
			demographicsType.setChangedBy(0);
		}
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
	
	private void setPersonTouchTime(Date[] touchTimes, Date dateChanged, Date dateCreated) {
		
		if (dateChanged != null) {
			if (touchTimes[0].before(dateChanged))
				touchTimes[0] = dateChanged;
		} else if (dateCreated != null) {
			if (touchTimes[0].before(dateCreated))
				touchTimes[0] = dateCreated;
		}
		
	}
	
	private List<VisitType> buildVisits(Patient patient, Date[] touchTimes) {
        List<VisitType> visitTypes = new ArrayList<>();
        List<Visit> visits = visitService.getVisits(
                null, Collections.singletonList(patient), null, null, null,
                null, null, null, null, true, true
        );
        if (visits != null && !visits.isEmpty()) {
            buildContainerVisitType(patient, touchTimes, visitTypes, visits);
        }
        return visitTypes;
    }
	
	private List<VisitType> buildVisits(Patient patient, Date[] touchTimes, Date startDate, Date endDate) {
        List<VisitType> visitTypes = new ArrayList<>();
        List<Visit> visits = Context.getService(CdrSyncVisitService.class).getVisitsByPatientAndDateChanged(patient, startDate, endDate);
        if (visits != null && !visits.isEmpty()) {
            buildContainerVisitType(patient, touchTimes, visitTypes, visits);
        }
        return visitTypes;
    }
	
	private void buildContainerVisitType(Patient patient, Date[] touchTimes, List<VisitType> visitTypes, List<Visit> visits) {
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
            visitType.setVoidedBy(visit.getVoided() ? visit.getVoidedBy() != null ? visit.getVoidedBy().getId() : 0 : 0);
            visitType.setDateVoided(visit.getDateVoided());
            visitType.setVisitUuid(visit.getUuid());
            visitType.setLocationId(visit.getLocation() != null ? visit.getLocation().getLocationId() : 0);
            visitType.setPatientUuid(patient.getPerson().getUuid());
            visitType.setDatimId(datimCode);
			visitTypes.add(visitType);
            if (visit.getDateChanged() != null) {
                if (touchTimes[0].before(visit.getDateChanged()))
                    touchTimes[0] = visit.getDateChanged();
            } else {
                if (visit.getDateCreated() != null && touchTimes[0].before(visit.getDateCreated()))
                    touchTimes[0] = visit.getDateCreated();
            }
            if (visit.getDateVoided() != null && touchTimes[0].before(visit.getDateVoided())) {
                    touchTimes[0] = visit.getDateVoided();
            }
//			Set<Encounter> visitEncounters = visit.getEncounters();
//			if (visitEncounters != null && !visitEncounters.isEmpty()) {
//				encounters.addAll(visitEncounters);
//			} //todo refactor this
        });
    }
	
	private List<PatientBiometricType> buildPatientBiometrics(Patient patient, Date[] touchTimes) {
        List<PatientBiometricType> patientBiometricTypes = new ArrayList<>();
        List<BiometricInfo> biometricInfos = biometricInfoService.getBiometricInfoByPatientId(patient.getPatientId());
        if (biometricInfos != null && !biometricInfos.isEmpty()) {
            buildContainerBiometricType(patient, touchTimes, patientBiometricTypes, biometricInfos);
        }
        return patientBiometricTypes;
    }
	
	private List<PatientBiometricType> buildPatientBiometrics(Patient patient, Date[] touchTimes, Date startDate) {
        List<PatientBiometricType> patientBiometricTypes = new ArrayList<>();
        List<BiometricInfo> biometricInfos = biometricInfoService.getBiometricInfoByPatientIdAndDateCaptured(patient.getPatientId(), startDate);
        if (biometricInfos != null && !biometricInfos.isEmpty()) {
            buildContainerBiometricType(patient, touchTimes, patientBiometricTypes, biometricInfos);
        }
        return patientBiometricTypes;
    }
	
	private void buildContainerBiometricType(Patient patient, Date[] touchTimes, List<PatientBiometricType> patientBiometricTypes, List<BiometricInfo> biometricInfos) {
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
        }
        return patientProgramTypes;
    }
	
	private List<PatientProgramType> buildPatientProgram(Patient patient, Date[] touchTimes, Date from, Date to) {
        List<PatientProgramType> patientProgramTypes = new ArrayList<>();
        List<PatientProgram> patientPrograms = Context.getService(CdrSyncPatientProgramService.class)
                .getPatientProgramsByPatientAndLastSyncDate(patient, from, to);
        if (patientPrograms != null && !patientPrograms.isEmpty()) {
            buildContainerPatientProgramType(touchTimes, patientProgramTypes, patientPrograms);
        }
        return patientProgramTypes;
    }
	
	private void buildContainerPatientProgramType(Date[] touchTimes, List<PatientProgramType> patientProgramTypes, List<PatientProgram> patientPrograms) {
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
            patientProgramType.setVoidedBy(patientProgram.getVoided() ? patientProgram.getVoidedBy() != null ? patientProgram.getVoidedBy().getId() : 0 : 0);
            patientProgramType.setDateVoided(patientProgram.getDateVoided());
            patientProgramType.setPatientProgramUuid(patientProgram.getUuid());
            patientProgramType.setPatientUuid(patientProgram.getPatient().getPerson().getUuid());
            patientProgramType.setLocationId(patientProgram.getLocation() != null ?
                    patientProgram.getLocation().getLocationId() : 0);
            patientProgramType.setDatimId(datimCode);
            patientProgramTypes.add(patientProgramType);
			updatePatientTouchTime(touchTimes, patientProgram.getDateChanged(), patientProgram.getDateCreated(), patientProgram.getDateVoided());
		});
    }
	
	private List<PatientIdentifierType> buildPatientIdentifier (Patient patient, Date[] touchTimes) {
        List<PatientIdentifierType> patientIdentifierTypes = new ArrayList<>();
        Set<PatientIdentifier> patientIdentifiers = patient.getIdentifiers();
        if (patientIdentifiers != null && !patientIdentifiers.isEmpty()) {
            buildContainerPatientIdentifier(patient, touchTimes, patientIdentifierTypes, patientIdentifiers);
        }
        return patientIdentifierTypes;
    }
	
	private List<PatientIdentifierType> buildPatientIdentifier (Patient patient, Date[] touchTimes, Date lastSyncDate) {
        List<PatientIdentifierType> patientIdentifierTypes = new ArrayList<>();
        Set<PatientIdentifier> patientIdentifiers = patient.getIdentifiers();
        if (patientIdentifiers != null && !patientIdentifiers.isEmpty()) {
            Set<PatientIdentifier> updatedPatientIdentifiers = patientIdentifiers.stream()
                    .filter(patientIdentifier -> (patientIdentifier.getDateChanged() != null &&
                            patientIdentifier.getDateChanged().after(lastSyncDate)) ||
														(patientIdentifier.getDateVoided() != null &&
							patientIdentifier.getDateVoided().after(lastSyncDate)) ||
														(patientIdentifier.getDateCreated() != null &&
							patientIdentifier.getDateCreated().after(lastSyncDate)))
                    .collect(Collectors.toSet());
            if (!updatedPatientIdentifiers.isEmpty()) {
                buildContainerPatientIdentifier(patient, touchTimes, patientIdentifierTypes, updatedPatientIdentifiers);
            }
        }
        return patientIdentifierTypes;
    }
	
	private void buildContainerPatientIdentifier(Patient patient, Date[] touchTimes, List<PatientIdentifierType> patientIdentifierTypes, Set<PatientIdentifier> updatedPatientIdentifiers) {
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
            patientIdentifierType.setVoidedBy(patientIdentifier.getVoided() ? patientIdentifier.getVoidedBy() != null ? patientIdentifier.getVoidedBy().getId() : 0 : 0);
            patientIdentifierType.setDateVoided(patientIdentifier.getDateVoided());
            patientIdentifierType.setDatimId(datimCode);
            patientIdentifierType.setPatientUuid(patient.getUuid());
            patientIdentifierTypes.add(patientIdentifierType);
			updatePatientTouchTime(touchTimes, patientIdentifier.getDateChanged(), patientIdentifier.getDateCreated(), patientIdentifier.getDateVoided());
		});
    }
	
	private List<EncounterType> buildEncounters(
			Patient patient, List<EncounterProvider> providers, Date[] touchTimes, List<Obs> obsList
	) {
        List<EncounterType> encounterTypes = new ArrayList<>();
		List<Encounter> encounterList = encounterService.getEncounters(patient, null, null, null, null, null, null, null, null, true);
		if (encounterList != null && !encounterList.isEmpty())
            buildContainerEncounterType(patient, providers, touchTimes, encounterTypes, encounterList, obsList);
        return encounterTypes;
    }
	
	private List<EncounterType> buildEncounters(
			Patient patient, List<EncounterProvider> providers, Date[] touchTimes,
			List<Obs> obsList, Date fromDate, Date toDate) {
        List<EncounterType> encounterTypes = new ArrayList<>();
		List<Encounter> encounterList = Context.getService(CdrSyncEncounterService.class).getEncountersByLastSyncDateAndPatient(fromDate, toDate, patient);
        if (encounterList != null && !encounterList.isEmpty()) {
            buildContainerEncounterType(patient, providers, touchTimes, encounterTypes, encounterList, obsList);
        }
        return encounterTypes;
    }
	
	private void buildContainerEncounterType(
			Patient patient, List<EncounterProvider> providers, Date[] touchTimes,
			List<EncounterType> encounterTypes, List<Encounter> encounters, List<Obs> obsList
	) {
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
            encounterType.setLocationId(encounter.getLocation() != null ? encounter.getLocation().getLocationId() : 0);
			try {
				encounterType.setFormId(encounter.getForm() != null ? encounter.getForm().getFormId() : 0);
				encounterType.setPmmForm(encounter.getForm() != null ? encounter.getForm().getName() != null ? encounter.getForm().getName() : "" : "");
			} catch (Exception e) {
				System.out.println("Error getting form name");
			}
			encounterType.setEncounterDatetime(encounter.getEncounterDatetime());
            encounterType.setCreator(encounter.getCreator() != null ? encounter.getCreator().getId() : 0);
            encounterType.setDateCreated(encounter.getDateCreated());
			try {
				encounterType.setChangedBy(encounter.getChangedBy() != null ? encounter.getChangedBy().getId() : 0);
			} catch (Exception e) {
				System.out.println("Error getting changed by");
			}
            encounterType.setDateChanged(encounter.getDateChanged());
            encounterType.setVoided(encounter.getVoided() ? 1 : 0);
            encounterType.setVoidedBy(encounter.getVoided() ? encounter.getVoidedBy() != null ? encounter.getVoidedBy().getId() : 0 : 0);
            encounterType.setDateVoided(encounter.getDateVoided());
            encounterTypes.add(encounterType);
            Set<EncounterProvider> encounterProviders = encounter.getEncounterProviders();
            if (!encounterProviders.isEmpty()) {
                providers.addAll(encounterProviders);
            }
			updatePatientTouchTime(touchTimes, encounter.getDateChanged(), encounter.getDateCreated(), encounter.getDateVoided());
			Set<Obs> obsSet = encounter.getAllObs(true);
			if (obsSet != null && !obsSet.isEmpty()) {
				obsList.addAll(obsSet);
			}
        });
    }
	
	private void updatePatientTouchTime(Date[] touchTimes, Date dateChanged, Date dateCreated, Date dateVoided) {
		if (touchTimes[0] == null) {
			if (dateChanged != null)
				touchTimes[0] = dateChanged;
			else if (dateCreated != null)
				touchTimes[0] = dateCreated;
			else if (dateVoided != null)
				touchTimes[0] = dateVoided;
		} else {
			if (dateChanged != null) {
				if (touchTimes[0].before(dateChanged))
					touchTimes[0] = dateChanged;
			} else {
				if (dateCreated != null && touchTimes[0].before(dateCreated))
					touchTimes[0] = dateCreated;
			}
			if (dateVoided != null && touchTimes[0].before(dateVoided))
				touchTimes[0] = dateVoided;
		}
	}
	
	private List<ObsType> buildObs(Patient patient, Date[] touchTimes, List<Obs> obsList) {
        List<ObsType> obsTypeList = new ArrayList<>();
		if (obsList != null && !obsList.isEmpty())
            buildContainerObsType(patient, touchTimes, obsTypeList, obsList);
        return obsTypeList;
    }
	
	//	private List<ObsType> buildObs(Patient patient, Date[] touchTimes,
	//								   List<Obs> obsList) {
	//        List<ObsType> obsTypeList = new ArrayList<>();
	//        if (obsList != null && !obsList.isEmpty()) {
	//            buildContainerObsType(patient, touchTimes, obsTypeList, obsList);
	//        }
	//        return obsTypeList;
	//    }
	
	private void buildContainerObsType(Patient patient, Date[] touchTimes, List<ObsType> obsTypeList, List<Obs> obsList) {
        List<Integer> confidentialConcepts = AppUtil.getConfidentialConcepts(); //todo get from global property
        obsList.forEach(obs -> {
            ObsType obsType = new ObsType();
            obsType.setPatientUuid(patient.getUuid());
            obsType.setDatimId(datimCode);
            obsType.setObsUuid(obs.getUuid());
            obsType.setObsId(obs.getObsId());
            obsType.setPersonId(obs.getPersonId());
			try {
				obsType.setConceptId(obs.getConcept() != null ? obs.getConcept().getConceptId() : 0);
				obsType.setVariableName(obs.getConcept() != null ? obs.getConcept().getName() != null ? obs.getConcept().getName().getName() : "" : "");
				obsType.setDatatype(obs.getConcept() != null ? obs.getConcept().getDatatype() != null ? obs.getConcept().getDatatype().getConceptDatatypeId() : 0 : 0);
			} catch (Exception e) {
				System.out.println("Error getting concept name");
			}

            if (obs.getEncounter() != null) {
                obsType.setEncounterId(obs.getEncounter().getEncounterId());
                obsType.setEncounterUuid(obs.getEncounter().getUuid());
				try {
					obsType.setPmmForm(obs.getEncounter().getForm() != null ?
							obs.getEncounter().getForm().getName() : "");
					obsType.setFormId(obs.getEncounter().getForm() != null ?
							obs.getEncounter().getForm().getFormId() : 0);
				} catch (Exception e) {
					System.out.println("Error getting form name");
				}
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
                obsType.setVariableValue(obs.getValueCoded() != null ? obs.getValueCoded().getName() != null ?
                        Security.encrypt(obs.getValueCoded().getName().getName()) : obs.getValueText() != null ?
                        Security.encrypt(obs.getValueText()) : obs.getValueDatetime() != null ?
                        Security.encrypt(String.valueOf(obs.getValueDatetime())) : obs.getValueNumeric() != null ?
                        Security.encrypt(String.valueOf(obs.getValueNumeric())) : "" : "");
            } else {
                obsType.setValueText(obs.getValueText());
				try {
					obsType.setVariableValue(obs.getValueCoded() != null ?
							obs.getValueCoded().getName() != null ? obs.getValueCoded().getName().getName() : obs.getValueText() != null ?
									obs.getValueText() : obs.getValueDatetime() != null ?
									String.valueOf(obs.getValueDatetime()) : obs.getValueNumeric() != null ?
									String.valueOf(obs.getValueNumeric()) : "" : "");
				} catch (Exception e) {
					System.out.println("error get concept id: " + e.getMessage());
				}

			}
            obsType.setCreator(obs.getCreator() != null ? obs.getCreator().getId() : 0);
            obsType.setDateCreated(obs.getDateCreated());

            obsType.setLocationId(obs.getLocation() != null ? obs.getLocation().getLocationId() : 0);
            obsType.setVoided(obs.getVoided() ? 1 : 0);
            obsType.setVoidedBy(obs.getVoided() ? obs.getVoidedBy() != null ? obs.getVoidedBy().getId() : 0 : 0);
            obsType.setDateVoided(obs.getDateVoided());
            obsTypeList.add(obsType);
			updatePatientTouchTime(touchTimes, obs.getDateChanged(), obs.getDateCreated(), obs.getDateVoided());
		});
    }
	
	private List<EncounterProviderType> buildEncounterProviders(List<EncounterProvider> providers, Patient patient, Date[] touchTimes) {
        List<EncounterProviderType> encounterProviderTypes = new ArrayList<>();
        if (!providers.isEmpty()) {
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
				updatePatientTouchTime(touchTimes, encounterProvider.getDateChanged(), encounterProvider.getDateCreated(), encounterProvider.getDateVoided());
			});
        }
        return encounterProviderTypes;
    }
}
