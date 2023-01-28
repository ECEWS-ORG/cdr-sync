package org.openmrs.module.cdrsync.api;

import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.cdrsync.CdrsyncConfig;

import java.io.IOException;
import java.util.Date;

public interface CdrContainerService extends OpenmrsService {
	
	@Authorized(CdrsyncConfig.MODULE_PRIVILEGE)
	String getAllPatients() throws IOException;
	
	String getAllPatients(Date startDate, Date endDate) throws IOException;
	
	String getPatientsByEncounterDateTime(Date from, Date to) throws IOException;
	
	void saveLastSyncDate();
}
