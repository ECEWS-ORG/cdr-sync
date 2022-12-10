package org.openmrs.module.cdrsync.api.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Patient;
import org.openmrs.api.db.hibernate.DbSession;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.cdrsync.api.dao.BiometricInfoDao;
import org.openmrs.module.cdrsync.model.BiometricInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

//@Repository
public class BiometricInfoDaoImpl implements BiometricInfoDao {
	
	//	@Autowired
	private SessionFactory sessionFactory;
	
	//	private SessionFactory sessionFactory;
	
	private Session getSession() {
		return sessionFactory.getCurrentSession();
	}
	
	/**
	 * @param sessionFactory the sessionFactory to set
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	public List<BiometricInfo> getBiometricInfoByPatient(Patient patient) {
		Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(BiometricInfo.class)
		        .add(Restrictions.eq("patientId", patient.getPatientId()));
		return criteria.list();
	}
}
