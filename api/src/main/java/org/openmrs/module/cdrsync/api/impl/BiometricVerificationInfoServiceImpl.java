package org.openmrs.module.cdrsync.api.impl;

import org.openmrs.module.cdrsync.api.BiometricVerificationInfoService;
import org.openmrs.module.cdrsync.api.dao.BiometricVerificationInfoDao;
import org.openmrs.module.cdrsync.model.BiometricVerificationInfo;

import java.util.List;

public class BiometricVerificationInfoServiceImpl implements BiometricVerificationInfoService {
	
	private BiometricVerificationInfoDao biometricVerificationInfoDao;
	
	public void setDao(BiometricVerificationInfoDao biometricVerificationInfoDao) {
		this.biometricVerificationInfoDao = biometricVerificationInfoDao;
	}
	
	@Override
	public List<BiometricVerificationInfo> getBiometricVerificationInfoByPatientId(Integer patientId) {
		return biometricVerificationInfoDao.getBiometricVerificationInfoByPatientId(patientId);
	}
}
