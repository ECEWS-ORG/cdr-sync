package org.openmrs.module.cdrsync.api.extractor.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Encounter;
import org.openmrs.PatientProgram;
import org.openmrs.Visit;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.hibernate.DbSession;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.api.db.hibernate.HibernateEncounterDAO;
import org.openmrs.module.cdrsync.api.extractor.dao.EncounterDao;

import java.util.List;
import java.util.logging.Logger;

@SuppressWarnings("unchecked")
public class EncounterDaoImpl extends HibernateEncounterDAO implements EncounterDao {
	
	private final Logger log = Logger.getLogger(this.getClass().getName());
	
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
	public List<Encounter> getEncountersByPatientId(Integer patientId) throws DAOException {
		Criteria crit = getSession().createCriteria(Encounter.class).createAlias("patient", "p")
		        .add(Restrictions.eq("p.patientId", patientId)).addOrder(Order.desc("encounterDatetime"));
		return crit.list();
	}
	
	@Override
	public List<Visit> getVisitsByPatientId(Integer patientId) {
		Criteria crit = getSession().createCriteria(Visit.class).createAlias("patient", "p")
		        .add(Restrictions.eq("p.patientId", patientId)).addOrder(Order.desc("dateCreated"));
		return crit.list();
	}
	
	@Override
	public List<PatientProgram> getPatientProgramsByPatientId(Integer patientId) {
		Criteria crit = getSession().createCriteria(PatientProgram.class).createAlias("patient", "p")
		        .add(Restrictions.eq("p.patientId", patientId)).addOrder(Order.desc("dateCreated"));
		return crit.list();
	}
}
