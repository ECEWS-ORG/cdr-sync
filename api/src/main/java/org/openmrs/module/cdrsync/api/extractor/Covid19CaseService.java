package org.openmrs.module.cdrsync.api.extractor;

import org.openmrs.module.cdrsync.model.extractor.IntegratorClientIntake;

import java.util.List;

public interface Covid19CaseService {
	
	List<IntegratorClientIntake> getCovid19CasesByPatientId(Integer patientId);
}
