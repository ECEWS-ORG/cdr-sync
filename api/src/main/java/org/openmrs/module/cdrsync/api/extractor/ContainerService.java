package org.openmrs.module.cdrsync.api.extractor;

import org.openmrs.module.cdrsync.container.model.Container;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public interface ContainerService {
	
	void createContainer(List<Container> containers, AtomicInteger count, Integer patientId, String reportFolder)
	        throws IOException;
	
}
