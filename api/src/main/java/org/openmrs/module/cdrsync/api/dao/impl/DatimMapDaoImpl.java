package org.openmrs.module.cdrsync.api.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.hibernate.DbSession;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.cdrsync.api.dao.DatimMapDao;
import org.openmrs.module.cdrsync.model.DatimMap;

public class DatimMapDaoImpl implements DatimMapDao {
	
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
	public DatimMap getDatimMapByDatimCode(String datimCode) throws DAOException {
		Criteria criteria = getSession().createCriteria(DatimMap.class);
		criteria.add(Restrictions.eq("datimCode", datimCode));
		return (DatimMap) criteria.uniqueResult();
	}
}
