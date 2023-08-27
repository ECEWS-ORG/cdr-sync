package org.openmrs.module.cdrsync.api;

import org.openmrs.module.cdrsync.model.DatimMap;

public interface DatimMapService {
	
	DatimMap getDatimMapByDatimCode(String datimCode);
}
