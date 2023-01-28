package org.openmrs.module.cdrsync.api.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.api.db.hibernate.HibernateObsDAO;
import org.openmrs.module.cdrsync.api.dao.CdrSyncObsDao;

import java.util.Date;
import java.util.List;

public class CdrSyncObsDaoImpl extends HibernateObsDAO implements CdrSyncObsDao {
	
	DbSessionFactory sessionFactory;
	
	public void setSessionFactory(DbSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	public List<Obs> getObsByPatientAndLastSyncDate(Patient patient, Date lastSyncDate, Date endDate) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Obs.class);
		criteria.add(Restrictions.eq("person", patient.getPerson()));
		if (lastSyncDate != null) {
			criteria.add(Restrictions.ge("dateCreated", lastSyncDate));
		}
		if (endDate == null)
			endDate = new Date();
		criteria.add(Restrictions.le("dateCreated", endDate));
		return criteria.list();
	}
}
