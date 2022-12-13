package org.openmrs.module.cdrsync.api.dao;

import org.openmrs.module.cdrsync.model.BiometricInfo;

import java.util.List;

public interface BiometricInfoDao {
	
	List<BiometricInfo> getBiometricInfoByPatientId(Integer patientId);
}
