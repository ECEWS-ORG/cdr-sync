package org.openmrs.module.cdrsync.api.extractor;

import org.openmrs.api.AdministrationService;
import org.openmrs.module.cdrsync.model.extractor.CdrSyncBatch;

import java.util.Date;
import java.util.List;

public interface CdrSyncAdminService extends AdministrationService {
	
	void saveCdrSyncBatch(CdrSyncBatch cdrSyncBatch);
	
	void updateCdrSyncBatchStatus(int batchId, String status, Integer patientsProcessed, boolean done);
	
	CdrSyncBatch getCdrSyncBatchByStatusAndOwner(String status, String owner, String syncType);
	
	List<CdrSyncBatch> getRecentSyncBatches();
	
	void updateCdrSyncBatchStartAndEndDateRange(int batchId, Date startDate, Date endDate);
	
	void updateCdrSyncBatchDownloadUrls(int batchId, String downloadUrls);
	
	void deleteCdrSyncBatch(int batchId);
}
