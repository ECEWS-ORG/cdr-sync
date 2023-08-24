package org.openmrs.module.cdrsync.api;

import org.openmrs.Patient;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.cdrsync.model.*;

import java.util.Date;
import java.util.List;

public interface CdrSyncAdminService extends AdministrationService {
	
	void saveCdrSyncBatch(CdrSyncBatch cdrSyncBatch);
	
	void updateCdrSyncBatchStatus(int batchId, String status, int patientsProcessed, boolean done);
	
	CdrSyncBatch getCdrSyncBatchByStatusAndOwner(String status, String owner, String syncType);
	
	List<CdrSyncBatch> getRecentSyncBatches();
	
	Long getPatientsCount(boolean includeVoided) throws DAOException;
	
	List<Patient> getPatients(Integer start, Integer length, boolean includeVoided) throws DAOException;
	
	List<Integer> getPatientIds(Integer start, Integer length, boolean includeVoided) throws DAOException;
	
	List<Integer> getPatientIds(boolean includeVoided) throws DAOException;
	
	List<Integer> getPatientsByLastSyncDate(Date from, Date to, List<String> patientIds, boolean includeVoided,
	        Integer start, Integer length) throws DAOException;
	
	Long getPatientCountFromLastSyncDate(Date from, Date to, List<String> patientIds, boolean includeVoided)
	        throws DAOException;
	
	DatimMap getDatimMapByDatimCode(String datimCode) throws DAOException;
	
	List<Covid19Case> getCovid19CasesByPatientId(Integer patientId);
}
