package org.openmrs.module.cdrsync.api.extractor;

import org.openmrs.module.cdrsync.model.extractor.DatimMap;

public interface DatimMapService {
	
	DatimMap getDatimMapByDatimCode(String datimCode);
}
