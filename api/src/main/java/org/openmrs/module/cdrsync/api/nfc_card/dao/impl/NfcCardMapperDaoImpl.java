package org.openmrs.module.cdrsync.api.nfc_card.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.openmrs.api.db.hibernate.DbSession;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.cdrsync.api.nfc_card.dao.NfcCardMapperDao;
import org.openmrs.module.cdrsync.model.dto.PatientDto;
import org.openmrs.module.cdrsync.model.nfc_card.NfcCardMapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NfcCardMapperDaoImpl implements NfcCardMapperDao {
	
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
	public NfcCardMapper getNfcCardMapperByNfcCardId(String nfcCardId) {
		Criteria criteria = getSession().createCriteria(NfcCardMapper.class);
		criteria.add(Restrictions.eq("nfcCardId", nfcCardId));
		return (NfcCardMapper) criteria.uniqueResult();
	}
	
	@Override
	public NfcCardMapper getNfcCardMapperByPatientIdentifier(String patientIdentifier) {
		Criteria criteria = getSession().createCriteria(NfcCardMapper.class);
		criteria.add(Restrictions.eq("patientIdentifier", patientIdentifier));
		return (NfcCardMapper) criteria.uniqueResult();
	}
	
	@Override
	public NfcCardMapper saveNfcCardMapper(NfcCardMapper nfcCardMapper) {
		DbSession session = getSession();
		nfcCardMapper.setDateCreated(new Date());
		session.getTransaction().begin();
		session.save(nfcCardMapper);
		session.getTransaction().commit();
		session.flush();
		session.refresh(nfcCardMapper);
		return nfcCardMapper;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<PatientDto> getPatientDetails(String patientIdentifier, int identifierType) {
		String patientIdentifierparametrized = "%"+patientIdentifier+"%";
		String query = "SELECT patient_identifier.identifier as patient_identifier, person_attribute.value AS patient_phone_no, person.uuid as patient_uuid, "
				+ "person_name.given_name, person_name.family_name, patient_identifier.identifier_type, patient_identifier.voided \n"
		        + "FROM patient_identifier\n"
		        + "LEFT JOIN person ON patient_identifier.patient_id = person.person_id and person.voided = 0\n"
		        + "LEFT JOIN person_name ON patient_identifier.patient_id = person_name.person_id and person_name.voided = 0\n"
		        + "LEFT JOIN person_attribute ON patient_identifier.patient_id = person_attribute.person_id AND person_attribute.person_attribute_type_id = 8 and person_attribute.voided = 0\n"
		        + "WHERE (patient_identifier.identifier_type = :identifierType AND patient_identifier.identifier like :patientIdentifier AND patient_identifier.preferred = 1)"
				+ "OR (person_name.given_name like :patientIdentifier)"
				+ "OR (person_name.family_name like :patientIdentifier) LIMIT 10;";
		Query q = getSession().createSQLQuery(query);
		q.setParameter("patientIdentifier", patientIdentifierparametrized);
		q.setParameter("identifierType", identifierType);
		List<Object[]> result = q.list();
		if (result == null)
			return null;
		List<PatientDto> patientDtos = new ArrayList<>();
		for (Object[] objects : result) {
			PatientDto patientDto = new PatientDto();
			patientDto.setPatientIdentifier((String) objects[0]);
			patientDto.setPatientPhoneNumber((String) objects[1]);
			patientDto.setPatientUuid((String) objects[2]);
			patientDto.setPatientName(objects[3] + " " + objects[4]);
			patientDto.setIdentifierType((Integer) objects[5]);
			patientDto.setVoided((Boolean) objects[6]);
			patientDtos.add(patientDto);
		}
		return patientDtos;
	}
	
	@Override
	public Long getNumberOfMappedPatients() {
		String query = "SELECT COUNT(*) FROM nfc_card_mapper";
		Query q = getSession().createSQLQuery(query);
		return ((Number) q.uniqueResult()).longValue();
	}
}
