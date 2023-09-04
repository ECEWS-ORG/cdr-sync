package org.openmrs.module.cdrsync.api.extractor.dao.impl;

import org.openmrs.api.db.hibernate.DbSession;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.cdrsync.api.extractor.dao.Covid19CaseDao;
import org.openmrs.module.cdrsync.model.extractor.IntegratorClientIntake;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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
	public List<IntegratorClientIntake> getCovid19CasesByPatientId(Integer patientId) {
		List<IntegratorClientIntake> integratorClientIntakes = new ArrayList<>();
		AtomicBoolean tableExists = new AtomicBoolean(false);
		getSession().doWork(connection -> {
			DatabaseMetaData dbm = connection.getMetaData();
			ResultSet tables = dbm.getTables(null, null, "integrator_client_intake", null);
			if (tables.next()) {
				tableExists.set(true);
			}
		});
		if (tableExists.get()) {
			String sql = "SELECT *, service_area_name FROM integrator_client_intake a JOIN integrator_service_area b ON a.service_area_id = b.service_area_id WHERE a.patient_id = :patientId";
			integratorClientIntakes = getSession().createSQLQuery(sql)
					.addEntity(IntegratorClientIntake.class)
					.setParameter("patientId", patientId)
					.list();
		}
		return integratorClientIntakes;
	}
}
