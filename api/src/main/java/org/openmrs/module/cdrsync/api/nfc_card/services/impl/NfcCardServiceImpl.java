package org.openmrs.module.cdrsync.api.nfc_card.services.impl;

import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.cdrsync.model.dto.ApiResponse;
import org.openmrs.module.cdrsync.api.nfc_card.services.NfcCardMapperService;
import org.openmrs.module.cdrsync.api.nfc_card.services.NfcCardService;
import org.openmrs.module.cdrsync.model.dto.PatientDto;
import org.openmrs.module.cdrsync.model.nfc_card.NfcCardMapper;

import java.util.List;

import static org.apache.log4j.varia.ExternallyRolledFileAppender.OK;

public class NfcCardServiceImpl implements NfcCardService {
	
	private final NfcCardMapperService nfcCardMapperService;
	
	private final User user;
	
	public NfcCardServiceImpl() {
		this.nfcCardMapperService = Context.getService(NfcCardMapperService.class);
		this.user = Context.getAuthenticatedUser();
	}
	
	@Override
    public ApiResponse<?> getNfcCardByPatientIdentifier(String patientIdentifier) {
        NfcCardMapper nfcCardMapper = nfcCardMapperService.getNfcCardMapperByPatientIdentifier(patientIdentifier);
        return new ApiResponse<>(true, "Successful", nfcCardMapper);
    }
	
	@Override
    public ApiResponse<?> getNfcCardMapperByNfcCardId(String nfcCardId, String hostName, int port) {
        NfcCardMapper nfcCardMapper = nfcCardMapperService.getNfcCardMapperByNfcCardId(nfcCardId);
        if (nfcCardMapper != null) {
            String url = "http://" + hostName + ":" + port + "/openmrs/coreapps/clinicianfacing/patient.page?patientId=" + nfcCardMapper.getPatientUuid();
            return new ApiResponse<>(true, "Successful", url);
        }
        return new ApiResponse<>(false, "Card not mapped yet", null);
    }
	
	@Override
    public ApiResponse<String> saveNfcCardMapper(String nfcCardId, String patientIdentifier, String patientUuid,
                                                 String patientPhoneNo, String hostName, int port) {
        NfcCardMapper nfcCard = new NfcCardMapper();
        nfcCard.setPatientIdentifier(patientIdentifier);
        nfcCard.setPatientUuid(patientUuid);
        nfcCard.setPatientPhoneNo(patientPhoneNo);
        nfcCard.setNfcCardId(nfcCardId);
        nfcCard.setCreator(user.getUsername());
        nfcCardMapperService.saveNfcCardMapper(nfcCard);

        String url = "http://" + hostName + ":" + port + "/openmrs/coreapps/clinicianfacing/patient.page?patientId=" + patientUuid;
        return new ApiResponse<>(true, "Successful", url);
    }
	
	@Override
    public ApiResponse<List<PatientDto>> getPatientDetails(String patientIdentifier, int identifierType) {
        List<PatientDto> patientDetails = nfcCardMapperService.getPatientDetails(patientIdentifier, identifierType);
        return new ApiResponse<>(true, "Successful", patientDetails);
    }
}
