package org.openmrs.module.cdrsync.api.extractor.dao;

import org.openmrs.api.db.AdministrationDAO;
import org.openmrs.module.cdrsync.model.extractor.CdrSyncBatch;

import java.util.List;

public interface CdrSyncAdminDao extends AdministrationDAO {
	
	void saveCdrSyncBatch(CdrSyncBatch cdrSyncBatch);
	
	void updateCdrSyncBatchStatus(int batchId, String status, int patientsProcessed, boolean done);
	
	CdrSyncBatch getCdrSyncBatchByStatusAndOwner(String status, String owner, String syncType);
	
	List<CdrSyncBatch> getRecentSyncBatches();
	
}
