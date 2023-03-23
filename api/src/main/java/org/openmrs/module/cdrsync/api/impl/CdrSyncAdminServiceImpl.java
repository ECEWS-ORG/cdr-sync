package org.openmrs.module.cdrsync.api.impl;

import org.openmrs.api.impl.AdministrationServiceImpl;
import org.openmrs.module.cdrsync.api.CdrSyncAdminService;
import org.openmrs.module.cdrsync.api.dao.CdrSyncAdminDao;
import org.openmrs.module.cdrsync.model.CdrSyncBatch;

import java.util.List;

public class CdrSyncAdminServiceImpl extends AdministrationServiceImpl implements CdrSyncAdminService {
	
	private CdrSyncAdminDao syncAdminDao;
	
	public void setDao(CdrSyncAdminDao dao) {
		this.syncAdminDao = dao;
	}
	
	@Override
	public void updateLastSyncGlobalProperty(String propertyName, String propertyValue) {
		this.syncAdminDao.updateLastSyncGlobalProperty(propertyName, propertyValue);
	}
	
	@Override
	public CdrSyncBatch saveCdrSyncBatch(CdrSyncBatch cdrSyncBatch) {
		return syncAdminDao.saveCdrSyncBatch(cdrSyncBatch);
	}
	
	@Override
	public CdrSyncBatch getCdrSyncBatchByStatusAndOwner(String status, String owner, String syncType) {
		return syncAdminDao.getCdrSyncBatchByStatusAndOwner(status, owner, syncType);
	}
	
	@Override
	public List<CdrSyncBatch> getRecentSyncBatches() {
		return syncAdminDao.getRecentSyncBatches();
	}
	
}
