package org.openmrs.module.cdrsync.api.extractor.dao;

import org.openmrs.module.cdrsync.model.extractor.BiometricInfo;

import java.util.Date;
import java.util.List;

public interface BiometricInfoDao {
	
	List<BiometricInfo> getBiometricInfoByPatientId(Integer patientId);
	
	List<BiometricInfo> getBiometricInfoByPatientIdAndDateCaptured(Integer patientId, Date dateCaptured);
}
