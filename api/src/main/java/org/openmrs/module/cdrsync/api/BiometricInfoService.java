package org.openmrs.module.cdrsync.api;

import org.openmrs.Patient;
import org.openmrs.module.cdrsync.model.BiometricInfo;

import java.util.List;

public interface BiometricInfoService {
	
	List<BiometricInfo> getBiometricInfoByPatient(Patient patient);
}
