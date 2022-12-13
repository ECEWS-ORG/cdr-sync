package org.openmrs.module.cdrsync.api.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Encounter;
import org.openmrs.api.db.hibernate.DbSession;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.api.db.hibernate.HibernateEncounterDAO;
import org.openmrs.module.cdrsync.api.dao.CdrSyncEncounterDao;

import java.util.Date;
import java.util.List;

public class CdrSyncEncounterDaoImpl extends HibernateEncounterDAO implements CdrSyncEncounterDao {
	
	DbSessionFactory sessionFactory;
	
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
	public List<Encounter> getEncountersByEncounterDateTime(Date from, Date to) {
		Criteria criteria = getSession().createCriteria(Encounter.class);
		
		if (from != null) {
			criteria.add(Restrictions.or(Restrictions.ge("dateCreated", from), Restrictions.ge("dateChanged", from)));
		}
		
		if (to != null) {
			criteria.add(Restrictions.or(Restrictions.le("dateCreated", to), Restrictions.le("dateChanged", to)));
		} else {
			criteria.add(Restrictions.or(Restrictions.le("dateCreated", new Date()),
			    Restrictions.le("dateChanged", new Date())));
		}
		criteria.add(Restrictions.eq("voided", false));
		
		criteria.addOrder(Order.asc("encounterDatetime"));
		return criteria.list();
	}
}
