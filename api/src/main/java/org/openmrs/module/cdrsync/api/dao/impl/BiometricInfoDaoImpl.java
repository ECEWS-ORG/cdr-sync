package org.openmrs.module.cdrsync.api.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.openmrs.api.db.hibernate.DbSession;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.cdrsync.api.dao.BiometricInfoDao;
import org.openmrs.module.cdrsync.model.BiometricInfo;

import java.util.List;

//@Repository
public class BiometricInfoDaoImpl implements BiometricInfoDao {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
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
		Query criteria = getSession().createQuery("from BiometricInfo b where b.patientId = :patientId").setParameter(
		    "patientId", patientId);
		return criteria.list();
	}
}
