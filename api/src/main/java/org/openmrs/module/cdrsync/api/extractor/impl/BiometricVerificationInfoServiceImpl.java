package org.openmrs.module.cdrsync.api.extractor.impl;

import org.openmrs.module.cdrsync.api.extractor.BiometricVerificationInfoService;
import org.openmrs.module.cdrsync.api.extractor.dao.BiometricVerificationInfoDao;
import org.openmrs.module.cdrsync.model.extractor.BiometricVerificationInfo;

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
