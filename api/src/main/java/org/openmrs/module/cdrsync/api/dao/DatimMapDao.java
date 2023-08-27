package org.openmrs.module.cdrsync.api.dao;

import org.openmrs.api.db.DAOException;
import org.openmrs.module.cdrsync.model.DatimMap;

public interface DatimMapDao {
	
	DatimMap getDatimMapByDatimCode(String datimCode) throws DAOException;
}
