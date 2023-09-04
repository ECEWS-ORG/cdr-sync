package org.openmrs.module.cdrsync.api.extractor;

import org.openmrs.module.cdrsync.model.extractor.BiometricVerificationInfo;

import java.util.List;

public interface BiometricVerificationInfoService {
	
	List<BiometricVerificationInfo> getBiometricVerificationInfoByPatientId(Integer patientId);
}
