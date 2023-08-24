package org.openmrs.module.cdrsync.api.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Patient;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.api.db.hibernate.HibernateAdministrationDAO;
import org.openmrs.module.cdrsync.api.dao.CdrSyncAdminDao;
import org.openmrs.module.cdrsync.model.*;

import java.text.SimpleDateFormat;
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
	public void updateLastSyncGlobalProperty(String propertyName, String propertyValue) {
		Query query = sessionFactory.getCurrentSession()
		        .createQuery("UPDATE GlobalProperty SET propertyValue = :propertyValue WHERE property = :propertyName")
		        .setParameter("propertyName", propertyName).setParameter("propertyValue", propertyValue);
		int s = query.executeUpdate();
		log.info("Finished updating::" + s);
	}
	
	@Override
	public void saveCdrSyncBatch(CdrSyncBatch cdrSyncBatch) {
		//		this.sessionFactory.getCurrentSession().saveOrUpdate(cdrSyncBatch);
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
	public void updateCdrSyncBatchStatus(int batchId, String status, int patientsProcessed, boolean done) {
		StringBuilder query = new StringBuilder("update cdr_sync_batch set status = :status, "
		        + "total_number_of_patients_processed = :patientsProcessed");
		if (done) {
			query.append(", date_completed = :dateCompleted");
		}
		query.append(" where cdr_sync_batch_id = :batchId");
		Query q = sessionFactory.getCurrentSession().createSQLQuery(query.toString());
		q.setParameter("status", status);
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
		criteria.add(Restrictions.eq("ownerUsername", owner));
		criteria.add(Restrictions.eq("syncType", syncType));
		return (CdrSyncBatch) criteria.uniqueResult();
	}
	
	public List<CdrSyncBatch> getRecentSyncBatches() {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(CdrSyncBatch.class);
		criteria.addOrder(Order.desc("dateStarted"));
		criteria.setMaxResults(10);
		return criteria.list();
	}
	
	@Override
	public List<Patient> getPatients(Integer start, Integer length, boolean includeVoided) throws DAOException {
		String queryStr;
		if (includeVoided) {
			queryStr = "from Patient";
		} else {
			queryStr = "from Patient p where p.voided = :voided";
		}
		Query query = sessionFactory.getCurrentSession().createQuery(queryStr);
		query.setFirstResult(start);
		query.setMaxResults(length);
		return query.list();
	}
	
	@Override
	public Long getPatientsCount(boolean includeVoided) throws DAOException {
		String queryStr;
		if (includeVoided) {
			queryStr = "select count(*) from Patient";
		} else {
			queryStr = "select count(*) from Patient p where p.voided = :voided";
		}
		Query query = sessionFactory.getCurrentSession().createQuery(queryStr);
		return (Long) query.uniqueResult();
	}
	
	@Override
	public List<Integer> getPatientIds(Integer start, Integer length, boolean includeVoided) throws DAOException {
		String queryStr;
		if (includeVoided) {
			queryStr = "select p.patientId from Patient p";
		} else {
			queryStr = "select p.patientId from Patient p where p.voided = :voided";
		}
		Query query = sessionFactory.getCurrentSession().createQuery(queryStr);
		query.setFirstResult(start);
		query.setMaxResults(length);
		return query.list();
	}
	
	@Override
	public List<Integer> getPatientIds(boolean includeVoided) throws DAOException {
		String queryStr;
		if (includeVoided) {
			queryStr = "select p.patientId from Patient p";
		} else {
			queryStr = "select p.patientId from Patient p where p.voided = :voided";
		}
		Query query = sessionFactory.getCurrentSession().createQuery(queryStr);
		return query.list();
	}
	
	@Override
	public List<Integer> getPatientsByLastSyncDate(Date from, Date to, List<String> patientIds, boolean includeVoided,
	        Integer start, Integer length) throws DAOException {
		
		SQLQuery sql = buildQuery(from, to, patientIds, includeVoided);
		if (start != null)
			sql.setFirstResult(start);
		if (length != null)
			sql.setMaxResults(length);
		
		return (List<Integer>) sql.list();
	}
	
	@Override
	public Long getPatientCountFromLastSyncDate(Date from, Date to, List<String> patientIds, boolean includeVoided)
	        throws DAOException {
		SQLQuery sql = buildQuery(from, to, patientIds, includeVoided);
		
		List<Integer> ret = sql.list();
		
		return (long) ret.size();
	}
	
	private SQLQuery buildQuery(Date from, Date to, List<String> patientIds, boolean includeVoided) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String fromStr = sdf.format(from);
		String toStr = sdf.format(to);
		String query = "SELECT DISTINCT patient_id FROM ("
		        + getQueryString(fromStr, toStr, patientIds, includeVoided, "patient", "patient_id", false) + " UNION ALL "
		        + getQueryString(fromStr, toStr, patientIds, includeVoided, "person", "person_id", false) + "  UNION ALL "
		        + getQueryString(fromStr, toStr, patientIds, includeVoided, "person_address", "person_id", false)
		        + "  UNION ALL "
		        + getQueryString(fromStr, toStr, patientIds, includeVoided, "patient_identifier", "patient_id", false)
		        + "  UNION ALL "
		        + getQueryString(fromStr, toStr, patientIds, includeVoided, "patient_program", "patient_id", false)
		        + "  UNION ALL "
		        + getQueryString(fromStr, toStr, patientIds, includeVoided, "person_attribute", "person_id", false)
		        + "  UNION ALL "
		        + getQueryString(fromStr, toStr, patientIds, includeVoided, "person_name", "person_id", false)
		        + "  UNION ALL " + getQueryString(fromStr, toStr, patientIds, includeVoided, "obs", "person_id", true)
		        + "  UNION ALL "
		        + getQueryString(fromStr, toStr, patientIds, includeVoided, "encounter", "patient_id", false)
		        + "  UNION ALL " + getQueryString(fromStr, toStr, patientIds, includeVoided, "visit", "patient_id", false)
		        + " UNION ALL "
		        + getQueryString(fromStr, toStr, patientIds, includeVoided, "biometricinfo", "patient_id", true)
		        + " UNION ALL "
		        + getQueryString(fromStr, toStr, patientIds, includeVoided, "biometricverificationinfo", "patient_id", true)
		        + ") AS patient_id";
		SQLQuery sql = sessionFactory.getCurrentSession().createSQLQuery(query);
		if (patientIds != null && patientIds.size() > 0)
			sql.setParameterList("patientIds", patientIds);
		
		return sql;
	}
	
	private String getQueryString(String from, String to, List<String> patientIds, boolean includeVoided, String tableName,
	        String fieldName, boolean noDateChanged) {
		StringBuilder query = new StringBuilder();
		if (tableName.equals("person") || tableName.equals("person_address") || tableName.equals("person_attribute")
		        || tableName.equals("person_name")) {
			query.append("  SELECT ").append(tableName).append(".").append(fieldName).append(" AS patient_id FROM ")
			        .append(tableName).append(" INNER JOIN patient ON ").append(tableName)
			        .append(".person_id = patient.patient_id WHERE TRUE");
			setRangeQuery(from, to, tableName, query);
		} else {
			if (noDateChanged) {
				query.append("  SELECT ").append(tableName).append(".").append(fieldName).append(" AS patient_id FROM ")
				        .append(tableName).append(" WHERE TRUE");
				if (from != null)
					query.append(" AND ").append(tableName).append(".date_created >= '").append(from).append("' ");
				if (to != null)
					query.append(" AND ").append(tableName).append(".date_created <= '").append(to).append("' ");
			} else {
				query.append("  SELECT ").append(tableName).append(".").append(fieldName).append(" AS patient_id FROM ")
				        .append(tableName).append(" WHERE TRUE");
				setRangeQuery(from, to, tableName, query);
			}
		}
		if (!includeVoided)
			query.append(" AND ").append(tableName).append(".voided = FALSE ");
		if (patientIds != null && patientIds.size() > 0)
			query.append(" AND ").append(tableName).append(".").append(fieldName).append(" IN (:patientIds)  ");
		return query.toString();
	}
	
	private void setRangeQuery(String from, String to, String tableName, StringBuilder query) {
		if (from != null)
			query.append(" AND (").append(tableName).append(".date_created >= '").append(from).append("' OR ")
			        .append(tableName).append(".date_changed >= '").append(from).append("' ) ");
		if (to != null)
			query.append(" AND (").append(tableName).append(".date_created <= '").append(to).append("' OR ")
			        .append(tableName).append(".date_changed <= '").append(to).append("' ) ");
	}
	
	@Override
	public DatimMap getDatimMapByDatimCode(String datimCode) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(DatimMap.class);
		criteria.add(Restrictions.eq("datimCode", datimCode));
		return (DatimMap) criteria.uniqueResult();
	}
	
	@Override
	public List<BiometricVerificationInfo> getBiometricVerificationInfoByPatientId(Integer patientId) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(BiometricVerificationInfo.class);
		criteria.add(Restrictions.eq("patientId", patientId));
		return criteria.list();
	}
	
	@Override
	public List<BiometricInfo> getBiometricInfoByPatientId(Integer patientId) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(BiometricInfo.class);
		criteria.add(Restrictions.eq("patientId", patientId));
		return criteria.list();
	}
	
	@Override
	public List<Covid19Case> getCovid19CasesByPatientId(Integer patientId) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Covid19Case.class);
		criteria.add(Restrictions.eq("patientId", patientId));
		return criteria.list();
	}
}
