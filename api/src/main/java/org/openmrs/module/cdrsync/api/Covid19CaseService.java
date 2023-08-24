package org.openmrs.module.cdrsync.api;

import org.openmrs.module.cdrsync.model.Covid19Case;

import java.util.List;

public interface Covid19CaseService {
	
	List<Covid19Case> getCovid19CasesByPatientId(Integer patientId);
}
