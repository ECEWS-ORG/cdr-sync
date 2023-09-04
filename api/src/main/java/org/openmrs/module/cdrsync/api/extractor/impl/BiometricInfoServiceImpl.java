package org.openmrs.module.cdrsync.api.extractor.impl;

import org.openmrs.module.cdrsync.api.extractor.BiometricInfoService;
import org.openmrs.module.cdrsync.api.extractor.dao.BiometricInfoDao;
import org.openmrs.module.cdrsync.model.extractor.BiometricInfo;

import java.util.List;

public class BiometricInfoServiceImpl implements BiometricInfoService {
	
	private BiometricInfoDao biometricInfoDao;
	
	public void setDao(BiometricInfoDao biometricInfoDao) {
		this.biometricInfoDao = biometricInfoDao;
	}
	
	@Override
	public List<BiometricInfo> getBiometricInfoByPatientId(Integer patientId) {
		return biometricInfoDao.getBiometricInfoByPatientId(patientId);
	}
	
}
