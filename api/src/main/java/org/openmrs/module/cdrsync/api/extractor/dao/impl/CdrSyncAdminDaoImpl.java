package org.openmrs.module.cdrsync.api.extractor.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.api.db.hibernate.HibernateAdministrationDAO;
import org.openmrs.module.cdrsync.api.extractor.dao.CdrSyncAdminDao;
import org.openmrs.module.cdrsync.model.extractor.CdrSyncBatch;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@SuppressWarnings("unchecked")
public class CdrSyncAdminDaoImpl extends HibernateAdministrationDAO implements CdrSyncAdminDao {
	
	private final Logger log = Logger.getLogger(this.getClass().getName());
	
	DbSessionFactory sessionFactory;
	
	public void setSessionFactory(DbSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	public void saveCdrSyncBatch(CdrSyncBatch cdrSyncBatch) {
		String query = "insert into cdr_sync_batch (status, owner_username, sync_type, "
		        + "total_number_of_patients_processed, total_number_of_patients, date_started, date_completed) values (:status, :ownerUsername, "
		        + ":syncType, :totalNumberOfPatientsProcessed, :totalNumberOfPatients, :dateStarted, :dateCompleted)";
		Query q = sessionFactory.getCurrentSession().createSQLQuery(query);
		q.setParameter("status", cdrSyncBatch.getStatus());
		q.setParameter("ownerUsername", cdrSyncBatch.getOwnerUsername());
		q.setParameter("syncType", cdrSyncBatch.getSyncType());
		q.setParameter("totalNumberOfPatientsProcessed", cdrSyncBatch.getPatientsProcessed());
		q.setParameter("totalNumberOfPatients", cdrSyncBatch.getPatients());
		q.setParameter("dateStarted", cdrSyncBatch.getDateStarted());
		q.setParameter("dateCompleted", null);
		q.executeUpdate();
	}
	
	@Override
	public void updateCdrSyncBatchStatus(int batchId, String status, Integer patientsProcessed, boolean done) {
		StringBuilder query = new StringBuilder("update cdr_sync_batch set status = :status");
		if (done) {
			query.append(", date_completed = :dateCompleted");
		}
		if (patientsProcessed != null) {
			query.append(", total_number_of_patients_processed = :patientsProcessed");
		}
		query.append(" where cdr_sync_batch_id = :batchId");
		Query q = sessionFactory.getCurrentSession().createSQLQuery(query.toString());
		q.setParameter("status", status);
		if (patientsProcessed != null)
			q.setParameter("patientsProcessed", patientsProcessed);
		if (done) {
			q.setParameter("dateCompleted", new Date());
		}
		q.setParameter("batchId", batchId);
		int i = q.executeUpdate();
		log.info("Finished updating::" + i);
	}
	
	public CdrSyncBatch getCdrSyncBatchByStatusAndOwner(String status, String owner, String syncType) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(CdrSyncBatch.class);
		criteria.add(Restrictions.eq("status", status));
		//		criteria.add(Restrictions.eq("ownerUsername", owner));
		//		criteria.add(Restrictions.eq("syncType", syncType));
		return (CdrSyncBatch) criteria.uniqueResult();
	}
	
	public List<CdrSyncBatch> getRecentSyncBatches() {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(CdrSyncBatch.class);
		criteria.addOrder(Order.desc("dateStarted"));
		criteria.setMaxResults(10);
		return criteria.list();
	}
	
	@Override
	public void updateCdrSyncBatchStartAndEndDateRange(int batchId, Date startDate, Date endDate) {
		Query q = sessionFactory.getCurrentSession().createSQLQuery(
		    "update cdr_sync_batch set sync_start_date = :startDate, sync_end_date = :endDate"
		            + " where cdr_sync_batch_id = :batchId");
		q.setParameter("startDate", startDate);
		q.setParameter("endDate", endDate);
		q.setParameter("batchId", batchId);
		int i = q.executeUpdate();
		log.info("Finished updating::" + i);
	}
	
	@Override
	public void updateCdrSyncBatchDownloadUrls(int batchId, String downloadUrls) {
		Query q = sessionFactory.getCurrentSession().createSQLQuery(
		    "update cdr_sync_batch set download_urls = :downloadUrls" + " where cdr_sync_batch_id = :batchId");
		q.setParameter("downloadUrls", downloadUrls);
		q.setParameter("batchId", batchId);
		int i = q.executeUpdate();
		log.info("Finished updating::" + i);
	}
	
	@Override
	public void deleteCdrSyncBatch(int batchId) {
		Query q = sessionFactory.getCurrentSession().createSQLQuery(
		    "delete from cdr_sync_batch where cdr_sync_batch_id = :batchId");
		q.setParameter("batchId", batchId);
		int i = q.executeUpdate();
		log.info("Finished deleting::" + i);
	}
}
