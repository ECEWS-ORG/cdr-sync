package org.openmrs.module.cdrsync.api.dao;

import org.openmrs.module.cdrsync.model.BiometricVerificationInfo;

import java.util.List;

public interface BiometricVerificationInfoDao {
	
	List<BiometricVerificationInfo> getBiometricVerificationInfoByPatientId(Integer patientId);
}
