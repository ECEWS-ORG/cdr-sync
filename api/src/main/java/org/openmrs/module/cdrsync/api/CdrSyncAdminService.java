package org.openmrs.module.cdrsync.api;

import org.openmrs.api.AdministrationService;

public interface CdrSyncAdminService extends AdministrationService {
	
	void updateLastSyncGlobalProperty(String propertyName, String propertyValue);
}
