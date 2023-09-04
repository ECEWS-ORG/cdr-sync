package org.openmrs.module.cdrsync.api.extractor;

import org.openmrs.Encounter;
import org.openmrs.PatientProgram;
import org.openmrs.Visit;

import java.util.List;

public interface EncounterService {
	
	List<Encounter> getEncountersByPatientId(Integer patientId);
	
	List<Visit> getVisitsByPatientId(Integer patientId);
	
	List<PatientProgram> getPatientProgramsByPatientId(Integer patientId);
	
}
