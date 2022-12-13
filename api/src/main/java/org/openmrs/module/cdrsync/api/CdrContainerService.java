package org.openmrs.module.cdrsync.api;

import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.cdrsync.CdrsyncConfig;

import java.io.IOException;
import java.util.Date;

public interface CdrContainerService extends OpenmrsService {
	
	@Authorized(CdrsyncConfig.MODULE_PRIVILEGE)
	void getAllPatients() throws IOException;
	
	void getPatientsByEncounterDateTime(Date from, Date to) throws IOException;
}
