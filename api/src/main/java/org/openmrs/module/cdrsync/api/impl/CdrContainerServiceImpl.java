package org.openmrs.module.cdrsync.api.impl;

import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.cdrsync.api.CdrContainerService;
import org.openmrs.module.cdrsync.api.CdrSyncPatientService;
import org.openmrs.module.cdrsync.api.ContainerService;
import org.openmrs.module.cdrsync.container.model.Container;
import org.openmrs.module.cdrsync.model.enums.SyncType;
import org.openmrs.module.cdrsync.utils.AppUtil;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import static org.openmrs.module.cdrsync.utils.AppUtil.getFacilityMetaData;
import static org.openmrs.module.cdrsync.utils.AppUtil.zipFolder;

public class CdrContainerServiceImpl extends BaseOpenmrsService implements CdrContainerService {
	
	Logger logger = Logger.getLogger(this.getClass().getName());
	
	private final ContainerService containerService;
	
	private final CdrSyncPatientService cdrSyncPatientService;
	
	private final AdministrationService administrationService;
	
	public CdrContainerServiceImpl() {
		this.containerService = new ContainerServiceImpl();
		this.cdrSyncPatientService = Context.getService(CdrSyncPatientService.class);
		this.administrationService = Context.getAdministrationService();
	}
	
	@Override
	@Transactional(readOnly = true)
	public String getAllPatients(long patientCount, int start, int length, String type, String fullContextPath,
	        String contextPath, String url) throws IOException {
		String result;
		String reportType = "CDR";
		String reportFolder = AppUtil.ensureReportDirectoryExists(fullContextPath, reportType, start);
		if (start == 0) {
			getFacilityMetaData(reportFolder);
		}
		if (start < patientCount) {
			List<Integer> patients = cdrSyncPatientService.getPatientIds(start, length, true);
			result = buildContainer(patients, reportFolder);
			return result;
		} else {
			if (!type.equals(SyncType.CUSTOM.name())) {
				saveLastSyncDate();
			}
			return zipFolder(type, reportFolder, contextPath);
		}
	}
	
	@Override
	public List<Integer> getAllPatients(boolean includeVoided) {
		return cdrSyncPatientService.getPatientIds(includeVoided);
	}
	
	@Override
	public String getAllPatients(Long patientCount, Date startDate, Date endDate, Integer start, Integer length,
	        String type, String fullContextPath, String contextPath, String url) throws IOException {
		String result;
		String reportType = "CDR";
		String reportFolder = AppUtil.ensureReportDirectoryExists(fullContextPath, reportType, start);
		if (start == 0) {
			getFacilityMetaData(reportFolder);
		}
		if (start < patientCount) {
			List<Integer> patients = cdrSyncPatientService.getPatientsByLastSyncDate(startDate, endDate, null, true, start,
			    length);
			//			result = buildContainer(patients, startDate, endDate);
			result = buildContainer(patients, reportFolder);
			return result;
		} else {
			if (!type.equals(SyncType.CUSTOM.name())) {
				saveLastSyncDate();
			}
			return zipFolder(type, reportFolder, contextPath);
		}
	}
	
	private String buildContainer(List<Integer> patientIds, String reportFolder) {
		List<Container> containers = new ArrayList<>();
		String resp;
		AtomicInteger count = new AtomicInteger();
		try {
			patientIds.forEach(patientId -> {
				try {
					containerService.createContainer(containers, count, patientId, reportFolder);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
		} catch (RuntimeException e) {
			logger.severe(e.getMessage());
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
	
	@Override
	public void saveLastSyncDate() {
		Date syncDate = new Date();
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy'T'hh:mm:ss");
		String syncDateString = dateFormat.format(syncDate);
		if (administrationService.getGlobalProperty("last.cdr.sync") == null) {
			GlobalProperty globalProperty = new GlobalProperty("last.cdr.sync", syncDateString, "Last sync date to CDR");
			administrationService.saveGlobalProperty(globalProperty);
		} else
			administrationService.updateGlobalProperty("last.cdr.sync", syncDateString);
		logger.info("Last sync date to CDR: " + syncDateString);
	}
	
	@Override
	public long getPatientsCount(boolean includeVoided) {
		return cdrSyncPatientService.getPatientsCount(includeVoided);
	}
	
	@Override
	public long getPatientsCount(Date startDate, Date endDate, boolean includeVoided) {
		//		getFacilityMetaData();
		return cdrSyncPatientService.getPatientCountFromLastSyncDate(startDate, endDate, null, includeVoided);
	}
	
}
