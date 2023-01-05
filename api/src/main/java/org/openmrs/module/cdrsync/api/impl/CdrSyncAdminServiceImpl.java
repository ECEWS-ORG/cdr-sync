package org.openmrs.module.cdrsync.api.impl;

import org.openmrs.api.impl.AdministrationServiceImpl;
import org.openmrs.module.cdrsync.api.CdrSyncAdminService;
import org.openmrs.module.cdrsync.api.dao.CdrSyncAdminDao;
import org.openmrs.module.cdrsync.api.dao.impl.CdrSyncEncounterDaoImpl;

public class CdrSyncAdminServiceImpl extends AdministrationServiceImpl implements CdrSyncAdminService {
	
	private CdrSyncAdminDao syncAdminDao;
	
	public void setDao(CdrSyncAdminDao dao) {
		this.syncAdminDao = dao;
	}
	
	@Override
	public void updateLastSyncGlobalProperty(String propertyName, String propertyValue) {
		this.syncAdminDao.updateLastSyncGlobalProperty(propertyName, propertyValue);
	}
	
}
