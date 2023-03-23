package org.openmrs.module.cdrsync.api.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.api.db.hibernate.HibernateAdministrationDAO;
import org.openmrs.module.cdrsync.api.dao.CdrSyncAdminDao;
import org.openmrs.module.cdrsync.model.CdrSyncBatch;

import java.util.List;

@SuppressWarnings("unchecked")
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
	
	public CdrSyncBatch saveCdrSyncBatch(CdrSyncBatch cdrSyncBatch) {
		System.out.println("Custom Saving the CDR Sync Batch");
		this.sessionFactory.getCurrentSession().saveOrUpdate(cdrSyncBatch);
		return cdrSyncBatch;
	}
	
	public CdrSyncBatch getCdrSyncBatchByStatusAndOwner(String status, String owner, String syncType) {
		System.out.println("Custom Getting the CDR Sync Batch");
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(CdrSyncBatch.class);
		criteria.add(Restrictions.eq("status", status));
		criteria.add(Restrictions.eq("ownerUsername", owner));
		criteria.add(Restrictions.eq("syncType", syncType));
		return (CdrSyncBatch) criteria.uniqueResult();
	}
	
	public List<CdrSyncBatch> getRecentSyncBatches() {
		System.out.println("Custom Getting the CDR Sync Batch");
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(CdrSyncBatch.class);
		criteria.addOrder(Order.desc("dateStarted"));
		criteria.setMaxResults(10);
		return criteria.list();
	}
}
