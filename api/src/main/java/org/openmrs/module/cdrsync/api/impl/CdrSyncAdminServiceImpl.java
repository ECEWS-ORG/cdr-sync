package org.openmrs.module.cdrsync.api.impl;

import org.openmrs.Patient;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.impl.AdministrationServiceImpl;
import org.openmrs.module.cdrsync.api.CdrSyncAdminService;
import org.openmrs.module.cdrsync.api.dao.CdrSyncAdminDao;
import org.openmrs.module.cdrsync.model.CdrSyncBatch;
import org.openmrs.module.cdrsync.model.DatimMap;

import java.util.Date;
import java.util.List;

public class CdrSyncAdminServiceImpl extends AdministrationServiceImpl implements CdrSyncAdminService {
	
	private CdrSyncAdminDao syncAdminDao;
	
	//	private BiometricInfoDao biometricInfoDao;
	//
	//	private BiometricVerificationInfoDao biometricVerificationInfoDao;
	
	public void setDao(CdrSyncAdminDao dao) {
		this.syncAdminDao = dao;
	}
	
	//	public void setBiometricInfoDao(BiometricInfoDao biometricInfoDao) {
	//		this.biometricInfoDao = biometricInfoDao;
	//	}
	//
	//	public void setBiometricVerificationInfoDao(BiometricVerificationInfoDao biometricVerificationInfoDao) {
	//		this.biometricVerificationInfoDao = biometricVerificationInfoDao;
	//	}
	
	@Override
	public void saveCdrSyncBatch(CdrSyncBatch cdrSyncBatch) {
		syncAdminDao.saveCdrSyncBatch(cdrSyncBatch);
	}
	
	@Override
	public void updateCdrSyncBatchStatus(int batchId, String status, int patientsProcessed, boolean done) {
		syncAdminDao.updateCdrSyncBatchStatus(batchId, status, patientsProcessed, done);
	}
	
	@Override
	public CdrSyncBatch getCdrSyncBatchByStatusAndOwner(String status, String owner, String syncType) {
		return syncAdminDao.getCdrSyncBatchByStatusAndOwner(status, owner, syncType);
	}
	
	@Override
	public List<CdrSyncBatch> getRecentSyncBatches() {
		return syncAdminDao.getRecentSyncBatches();
	}
	
	@Override
	public Long getPatientsCount(boolean includeVoided) throws DAOException {
		return syncAdminDao.getPatientsCount(includeVoided);
	}
	
	@Override
	public List<Patient> getPatients(Integer start, Integer length, boolean includeVoided) throws DAOException {
		return syncAdminDao.getPatients(start, length, includeVoided);
	}
	
	@Override
	public List<Integer> getPatientIds(Integer start, Integer length, boolean includeVoided) throws DAOException {
		return syncAdminDao.getPatientIds(start, length, includeVoided);
	}
	
	@Override
	public List<Integer> getPatientIds(boolean includeVoided) throws DAOException {
		return syncAdminDao.getPatientIds(includeVoided);
	}
	
	@Override
	public List<Integer> getPatientsByLastSyncDate(Date from, Date to, List<String> patientIds, boolean includeVoided,
	        Integer start, Integer length) throws DAOException {
		return syncAdminDao.getPatientsByLastSyncDate(from, to, patientIds, includeVoided, start, length);
	}
	
	@Override
	public Long getPatientCountFromLastSyncDate(Date from, Date to, List<String> patientIds, boolean includeVoided)
	        throws DAOException {
		return syncAdminDao.getPatientCountFromLastSyncDate(from, to, patientIds, includeVoided);
	}
	
	@Override
	public DatimMap getDatimMapByDatimCode(String datimCode) throws DAOException {
		return syncAdminDao.getDatimMapByDatimCode(datimCode);
	}
	
}
