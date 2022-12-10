package org.openmrs.module.cdrsync.api;

import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.cdrsync.CdrsyncConfig;

public interface CdrContainerService extends OpenmrsService {
	
	@Authorized(CdrsyncConfig.MODULE_PRIVILEGE)
	void getAllPatients();
}
