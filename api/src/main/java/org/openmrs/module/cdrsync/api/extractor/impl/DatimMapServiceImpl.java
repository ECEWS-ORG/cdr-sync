package org.openmrs.module.cdrsync.api.extractor.impl;

import org.openmrs.module.cdrsync.api.extractor.DatimMapService;
import org.openmrs.module.cdrsync.api.extractor.dao.DatimMapDao;
import org.openmrs.module.cdrsync.model.extractor.DatimMap;

public class DatimMapServiceImpl implements DatimMapService {
	
	private DatimMapDao datimMapDao;
	
	public void setDao(DatimMapDao datimMapDao) {
		this.datimMapDao = datimMapDao;
	}
	
	@Override
	public DatimMap getDatimMapByDatimCode(String datimCode) {
		return datimMapDao.getDatimMapByDatimCode(datimCode);
	}
}
