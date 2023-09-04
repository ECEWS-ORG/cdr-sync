package org.openmrs.module.cdrsync.api.nfc_card.services;

import org.openmrs.module.cdrsync.model.dto.PatientDto;
import org.openmrs.module.cdrsync.model.nfc_card.NfcCardMapper;

import java.util.List;

public interface NfcCardMapperService {
	
	NfcCardMapper getNfcCardMapperByNfcCardId(String nfcCardId);
	
	NfcCardMapper getNfcCardMapperByPatientIdentifier(String patientIdentifier);
	
	NfcCardMapper saveNfcCardMapper(NfcCardMapper nfcCardMapper);
	
	List<PatientDto> getPatientDetails(String patientIdentifier, int identifierType);
}
