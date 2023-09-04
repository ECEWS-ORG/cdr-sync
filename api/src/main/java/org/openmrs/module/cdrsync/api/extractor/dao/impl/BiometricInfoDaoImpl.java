package org.openmrs.module.cdrsync.api.extractor.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.openmrs.api.db.hibernate.DbSession;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.cdrsync.api.extractor.dao.BiometricInfoDao;
import org.openmrs.module.cdrsync.model.extractor.BiometricInfo;

import java.util.Date;
import java.util.List;

//@Repository
@SuppressWarnings("unchecked")
public class BiometricInfoDaoImpl implements BiometricInfoDao {
	
	private DbSessionFactory sessionFactory;
	
	private DbSession getSession() {
		return sessionFactory.getCurrentSession();
	}
	
	/**
	 * @param sessionFactory the sessionFactory to set
	 */
	public void setSessionFactory(DbSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	public List<BiometricInfo> getBiometricInfoByPatientId(Integer patientId) {
		Criteria criteria = getSession().createCriteria(BiometricInfo.class);
		criteria.add(Restrictions.eq("patientId", patientId));
		return criteria.list();
	}
	
	@Override
	public List<BiometricInfo> getBiometricInfoByPatientIdAndDateCaptured(Integer patientId, Date dateCaptured) {
		Criteria criteria = getSession().createCriteria(BiometricInfo.class);
		criteria.add(Restrictions.eq("patientId", patientId));
		criteria.add(Restrictions.ge("dateCreated", dateCaptured));
		return criteria.list();
	}
}
