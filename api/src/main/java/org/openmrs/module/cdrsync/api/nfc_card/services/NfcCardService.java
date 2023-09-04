package org.openmrs.module.cdrsync.api.nfc_card.services;

import org.openmrs.module.cdrsync.model.dto.ApiResponse;
import org.openmrs.module.cdrsync.model.dto.PatientDto;

import java.util.List;

public interface NfcCardService {
	
	ApiResponse<?> getNfcCardByPatientIdentifier(String patientIdentifier);
	
	ApiResponse<?> getNfcCardMapperByNfcCardId(String nfcCardId, String hostName, int port);
	
	ApiResponse<String> saveNfcCardMapper(String nfcCardId, String patientIdentifier, String patientUuid,
	        String patientPhoneNo, String hostName, int port);
	
	ApiResponse<List<PatientDto>> getPatientDetails(String patientIdentifier, int identifierType);
}
