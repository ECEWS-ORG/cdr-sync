package org.openmrs.module.cdrsync.api.extractor.impl;

import org.openmrs.Encounter;
import org.openmrs.PatientProgram;
import org.openmrs.Visit;
import org.openmrs.module.cdrsync.api.extractor.EncounterService;
import org.openmrs.module.cdrsync.api.extractor.dao.EncounterDao;

import java.util.List;

public class EncounterServiceImpl implements EncounterService {
	
	private EncounterDao encounterDao;
	
	public void setDao(EncounterDao encounterDao) {
		this.encounterDao = encounterDao;
	}
	
	@Override
	public List<Encounter> getEncountersByPatientId(Integer patientId) {
		return encounterDao.getEncountersByPatientId(patientId);
	}
	
	@Override
	public List<Visit> getVisitsByPatientId(Integer patientId) {
		return encounterDao.getVisitsByPatientId(patientId);
	}
	
	@Override
	public List<PatientProgram> getPatientProgramsByPatientId(Integer patientId) {
		return encounterDao.getPatientProgramsByPatientId(patientId);
	}
}
