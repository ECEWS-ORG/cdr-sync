package org.openmrs.module.cdrsync.api.extractor.dao;

import org.openmrs.module.cdrsync.model.extractor.IntegratorClientIntake;

import java.util.List;

public interface Covid19CaseDao {
	
	List<IntegratorClientIntake> getCovid19CasesByPatientId(Integer patientId);
}
