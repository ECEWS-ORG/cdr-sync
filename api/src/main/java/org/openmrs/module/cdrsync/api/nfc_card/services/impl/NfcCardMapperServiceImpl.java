package org.openmrs.module.cdrsync.api.nfc_card.services.impl;

import org.openmrs.module.cdrsync.api.nfc_card.dao.NfcCardMapperDao;
import org.openmrs.module.cdrsync.api.nfc_card.services.NfcCardMapperService;
import org.openmrs.module.cdrsync.model.dto.PatientDto;
import org.openmrs.module.cdrsync.model.nfc_card.NfcCardMapper;

import java.util.List;

public class NfcCardMapperServiceImpl implements NfcCardMapperService {
	
	private NfcCardMapperDao nfcCardMapperDao;
	
	/**
	 * @param nfcCardMapperDao the nfcCardMapperDao to set
	 */
	public void setDao(NfcCardMapperDao nfcCardMapperDao) {
		this.nfcCardMapperDao = nfcCardMapperDao;
	}
	
	@Override
	public NfcCardMapper getNfcCardMapperByNfcCardId(String nfcCardId) {
		return nfcCardMapperDao.getNfcCardMapperByNfcCardId(nfcCardId);
	}
	
	@Override
	public NfcCardMapper getNfcCardMapperByPatientIdentifier(String patientIdentifier) {
		return nfcCardMapperDao.getNfcCardMapperByPatientIdentifier(patientIdentifier);
	}
	
	@Override
	public NfcCardMapper saveNfcCardMapper(NfcCardMapper nfcCardMapper) {
		return nfcCardMapperDao.saveNfcCardMapper(nfcCardMapper);
	}
	
	@Override
	public List<PatientDto> getPatientDetails(String patientIdentifier, int identifierType) {
		return nfcCardMapperDao.getPatientDetails(patientIdentifier, identifierType);
	}
	
	@Override
	public Long getNumberOfMappedPatients() {
		return nfcCardMapperDao.getNumberOfMappedPatients();
	}
}
