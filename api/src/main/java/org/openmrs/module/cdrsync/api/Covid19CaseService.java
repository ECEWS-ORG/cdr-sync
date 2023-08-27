package org.openmrs.module.cdrsync.api;

import org.openmrs.module.cdrsync.model.IntegratorClientIntake;

import java.util.List;

public interface Covid19CaseService {
	
	List<IntegratorClientIntake> getCovid19CasesByPatientId(Integer patientId);
}
