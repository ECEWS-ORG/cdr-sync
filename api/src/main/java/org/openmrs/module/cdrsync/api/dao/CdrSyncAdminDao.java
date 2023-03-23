package org.openmrs.module.cdrsync.api.dao;

import org.openmrs.api.db.AdministrationDAO;
import org.openmrs.module.cdrsync.model.CdrSyncBatch;

import java.util.List;

public interface CdrSyncAdminDao extends AdministrationDAO {
	
	void updateLastSyncGlobalProperty(String propertyName, String propertyValue);
	
	CdrSyncBatch saveCdrSyncBatch(CdrSyncBatch cdrSyncBatch);
	
	CdrSyncBatch getCdrSyncBatchByStatusAndOwner(String status, String owner, String syncType);
	
	List<CdrSyncBatch> getRecentSyncBatches();
}
