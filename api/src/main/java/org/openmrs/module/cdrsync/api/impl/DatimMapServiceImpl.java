package org.openmrs.module.cdrsync.api.impl;

import org.openmrs.module.cdrsync.api.DatimMapService;
import org.openmrs.module.cdrsync.api.dao.DatimMapDao;
import org.openmrs.module.cdrsync.model.DatimMap;

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
