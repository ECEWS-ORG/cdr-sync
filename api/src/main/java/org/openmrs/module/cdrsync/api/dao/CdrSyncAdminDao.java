package org.openmrs.module.cdrsync.api.dao;

import org.openmrs.Patient;
import org.openmrs.api.db.AdministrationDAO;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.cdrsync.model.*;

import java.util.Date;
import java.util.List;

public interface CdrSyncAdminDao extends AdministrationDAO {
	
	void saveCdrSyncBatch(CdrSyncBatch cdrSyncBatch);
	
	void updateCdrSyncBatchStatus(int batchId, String status, int patientsProcessed, boolean done);
	
	CdrSyncBatch getCdrSyncBatchByStatusAndOwner(String status, String owner, String syncType);
	
	List<CdrSyncBatch> getRecentSyncBatches();
	
}
