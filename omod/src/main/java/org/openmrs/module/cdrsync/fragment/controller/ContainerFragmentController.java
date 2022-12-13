package org.openmrs.module.cdrsync.fragment.controller;

import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.module.cdrsync.api.CdrContainerService;
import org.openmrs.module.cdrsync.api.impl.CdrContainerServiceImpl;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentModel;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class ContainerFragmentController {
	
	CdrContainerService containerService = new CdrContainerServiceImpl();
	
	//	CdrContainerService containerService = Context.getService(CdrContainerService.class);
	
	public void controller(FragmentModel model, @SpringBean("userService") UserService service) {
		model.addAttribute("users", service.getAllUsers());
	}
	
	public void getPatients() throws IOException {
		containerService.getAllPatients();
	}
	
	public void getPatientsFromLastSync() throws IOException {
		String lastSync = Context.getAdministrationService().getGlobalProperty("last.cdr.sync");
		System.out.println("From db::" + lastSync);
		if (lastSync != null && !lastSync.isEmpty()) {
			try {
				DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
				Date lastSyncDate = dateFormat.parse(lastSync);
				System.out.println("Last sync date::" + lastSyncDate);
				containerService.getPatientsByEncounterDateTime(lastSyncDate, new Date());
			}
			catch (ParseException e) {
				System.out.println("parse exception::" + e.getMessage());
				e.printStackTrace();
			}
			catch (IOException e) {
				System.out.println("Io exception::" + e.getMessage());
				e.printStackTrace();
			}
			
		} else {
			containerService.getAllPatients();
		}
	}
	
	public void getPatientsFromCustomDate(Date from, Date to) throws IOException {
		containerService.getPatientsByEncounterDateTime(from, to);
	}
}
