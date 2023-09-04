package org.openmrs.module.cdrsync.api.extractor.dao;

import org.openmrs.module.cdrsync.model.extractor.BiometricVerificationInfo;

import java.util.List;

public interface BiometricVerificationInfoDao {
	
	List<BiometricVerificationInfo> getBiometricVerificationInfoByPatientId(Integer patientId);
}
