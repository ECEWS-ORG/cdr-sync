package org.openmrs.module.cdrsync.api;

import org.openmrs.Encounter;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.EncounterService;
import org.openmrs.module.cdrsync.CdrsyncConfig;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public interface CdrSyncEncounterService extends EncounterService {
	
	@Authorized(CdrsyncConfig.MODULE_PRIVILEGE)
	List<Encounter> getEncountersByEncounterDateTime(Date from, Date to);
}
