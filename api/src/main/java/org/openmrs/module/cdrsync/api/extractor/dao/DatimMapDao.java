package org.openmrs.module.cdrsync.api.extractor.dao;

import org.openmrs.api.db.DAOException;
import org.openmrs.module.cdrsync.model.extractor.DatimMap;

public interface DatimMapDao {
	
	DatimMap getDatimMapByDatimCode(String datimCode) throws DAOException;
}
