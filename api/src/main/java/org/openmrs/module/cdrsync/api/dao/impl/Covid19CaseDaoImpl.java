package org.openmrs.module.cdrsync.api.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.openmrs.api.db.hibernate.DbSession;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.cdrsync.api.dao.Covid19CaseDao;
import org.openmrs.module.cdrsync.model.Covid19Case;

import java.util.List;

@SuppressWarnings("unchecked")
public class Covid19CaseDaoImpl implements Covid19CaseDao {
	
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
	public List<Covid19Case> getCovid19CasesByPatientId(Integer patientId) {
		Criteria criteria = getSession().createCriteria(Covid19Case.class);
		criteria.add(Restrictions.eq("patientId", patientId));
		return criteria.list();
	}
}
