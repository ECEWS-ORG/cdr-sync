package org.openmrs.module.cdrsync.api;

import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.cdrsync.CdrsyncConfig;

import java.io.IOException;
import java.util.Date;

public interface CdrContainerService extends OpenmrsService {
	
	@Authorized(CdrsyncConfig.MODULE_PRIVILEGE)
	String getAllPatients(Long patientCount, int start, int length) throws IOException;
	
	@Authorized(CdrsyncConfig.MODULE_PRIVILEGE)
	String getAllPatients(Long patientCount, Date startDate, Date endDate, Integer start, Integer length) throws IOException;
	
	//	String getPatientsByEncounterDateTime(Date from, Date to) throws IOException;
	@Authorized(CdrsyncConfig.MODULE_PRIVILEGE)
	void saveLastSyncDate();
	
	@Authorized(CdrsyncConfig.MODULE_PRIVILEGE)
	long getPatientsCount(boolean includeVoided) throws IOException;
	
	@Authorized(CdrsyncConfig.MODULE_PRIVILEGE)
	long getPatientsCount(Date startDate, Date endDate, boolean includeVoided) throws IOException;
}
