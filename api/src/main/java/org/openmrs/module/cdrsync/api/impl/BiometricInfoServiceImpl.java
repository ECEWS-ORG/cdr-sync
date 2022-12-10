package org.openmrs.module.cdrsync.api.impl;

import org.openmrs.Patient;
import org.openmrs.module.cdrsync.api.BiometricInfoService;
import org.openmrs.module.cdrsync.api.dao.BiometricInfoDao;
import org.openmrs.module.cdrsync.model.BiometricInfo;
import org.springframework.stereotype.Service;

import java.util.List;

//@Service
public class BiometricInfoServiceImpl implements BiometricInfoService {
	
	private BiometricInfoDao biometricInfoDao;
	
	public BiometricInfoServiceImpl(BiometricInfoDao biometricInfoDao) {
		this.biometricInfoDao = biometricInfoDao;
	}
	
	@Override
	public List<BiometricInfo> getBiometricInfoByPatient(Patient patient) {
		return biometricInfoDao.getBiometricInfoByPatient(patient);
	}
}
