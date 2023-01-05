package org.openmrs.module.cdrsync.api.dao.impl;

import org.hibernate.Query;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.api.db.hibernate.HibernateAdministrationDAO;
import org.openmrs.module.cdrsync.api.dao.CdrSyncAdminDao;

public class CdrSyncAdminDaoImpl extends HibernateAdministrationDAO implements CdrSyncAdminDao {
	
	DbSessionFactory sessionFactory;
	
	public void setSessionFactory(DbSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	public void updateLastSyncGlobalProperty(String propertyName, String propertyValue) {
		System.out.println("Custom Setting the global property");
		Query query = sessionFactory.getCurrentSession()
		        .createQuery("UPDATE GlobalProperty SET propertyValue = :propertyValue WHERE property = :propertyName")
		        .setParameter("propertyName", propertyName).setParameter("propertyValue", propertyValue);
		int s = query.executeUpdate();
		System.out.println("Finished updating::" + s);
	}
}
