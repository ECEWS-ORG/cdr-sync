package org.openmrs.module.cdrsync.api.extractor;

import org.openmrs.api.AdministrationService;
import org.openmrs.module.cdrsync.model.extractor.CdrSyncBatch;

import java.util.List;

public interface CdrSyncAdminService extends AdministrationService {
	
	void saveCdrSyncBatch(CdrSyncBatch cdrSyncBatch);
	
	void updateCdrSyncBatchStatus(int batchId, String status, int patientsProcessed, boolean done);
	
	CdrSyncBatch getCdrSyncBatchByStatusAndOwner(String status, String owner, String syncType);
	
	List<CdrSyncBatch> getRecentSyncBatches();
	
}
