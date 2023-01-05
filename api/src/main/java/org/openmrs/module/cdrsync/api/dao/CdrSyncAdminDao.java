package org.openmrs.module.cdrsync.api.dao;

import org.openmrs.api.db.AdministrationDAO;

public interface CdrSyncAdminDao extends AdministrationDAO {
	
	void updateLastSyncGlobalProperty(String propertyName, String propertyValue);
}
