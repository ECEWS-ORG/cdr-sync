package org.openmrs.module.cdrsync.api.dao;

import org.openmrs.module.cdrsync.model.IntegratorClientIntake;

import java.util.List;

public interface Covid19CaseDao {
	
	List<IntegratorClientIntake> getCovid19CasesByPatientId(Integer patientId);
}
