package org.openmrs.module.cdrsync.api;

import org.openmrs.module.cdrsync.model.BiometricVerificationInfo;

import java.util.List;

public interface BiometricVerificationInfoService {
	
	List<BiometricVerificationInfo> getBiometricVerificationInfoByPatientId(Integer patientId);
}
