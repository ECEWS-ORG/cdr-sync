package org.openmrs.module.cdrsync.api.extractor.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.openmrs.api.db.hibernate.DbSession;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.cdrsync.api.extractor.dao.BiometricVerificationInfoDao;
import org.openmrs.module.cdrsync.model.extractor.BiometricVerificationInfo;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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
		AtomicBoolean tableExists = new AtomicBoolean(false);
		getSession().doWork(connection -> {
			DatabaseMetaData dbm = connection.getMetaData();
			ResultSet tables = dbm.getTables(null, null, "biometricverificationinfo", null);
			if (tables.next()) {
				tableExists.set(true);
			}
		});
		if (tableExists.get()) {
			Criteria criteria = getSession().createCriteria(BiometricVerificationInfo.class);
			criteria.add(Restrictions.eq("patientId", patientId));
			return criteria.list();
		}
		return new ArrayList<>();
	}
}
