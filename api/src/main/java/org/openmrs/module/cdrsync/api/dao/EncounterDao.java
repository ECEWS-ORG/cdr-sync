package org.openmrs.module.cdrsync.api.dao;

import org.openmrs.Encounter;
import org.openmrs.PatientProgram;
import org.openmrs.Visit;

import java.util.List;

public interface EncounterDao {
	
	List<Encounter> getEncountersByPatientId(Integer patientId);
	
	List<Visit> getVisitsByPatientId(Integer patientId);
	
	List<PatientProgram> getPatientProgramsByPatientId(Integer patientId);
}
