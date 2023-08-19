package org.openmrs.module.cdrsync.api.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.openmrs.api.db.hibernate.DbSession;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.cdrsync.api.dao.BiometricVerificationInfoDao;
import org.openmrs.module.cdrsync.model.BiometricVerificationInfo;

import java.util.List;

@SuppressWarnings("unchecked")
public class BiometricVerificationInfoDaoImpl implements BiometricVerificationInfoDao {
	
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
	public List<BiometricVerificationInfo> getBiometricVerificationInfoByPatientId(Integer patientId) {
		Criteria criteria = getSession().createCriteria(BiometricVerificationInfo.class);
		criteria.add(Restrictions.eq("patientId", patientId));
		return criteria.list();
	}
}
