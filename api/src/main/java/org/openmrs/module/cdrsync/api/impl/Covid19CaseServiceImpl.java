package org.openmrs.module.cdrsync.api.impl;

import org.openmrs.module.cdrsync.api.Covid19CaseService;
import org.openmrs.module.cdrsync.api.dao.Covid19CaseDao;
import org.openmrs.module.cdrsync.model.IntegratorClientIntake;

import java.util.List;

public class Covid19CaseServiceImpl implements Covid19CaseService {
	
	private Covid19CaseDao covid19CaseDao;
	
	public void setDao(Covid19CaseDao covid19CaseDao) {
		this.covid19CaseDao = covid19CaseDao;
	}
	
	@Override
	public List<IntegratorClientIntake> getCovid19CasesByPatientId(Integer patientId) {
		return covid19CaseDao.getCovid19CasesByPatientId(patientId);
	}
}
