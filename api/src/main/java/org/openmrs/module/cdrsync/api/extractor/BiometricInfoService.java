package org.openmrs.module.cdrsync.api.extractor;

import org.openmrs.module.cdrsync.model.extractor.BiometricInfo;

import java.util.List;

public interface BiometricInfoService {
	
	List<BiometricInfo> getBiometricInfoByPatientId(Integer patientId);
	
}
