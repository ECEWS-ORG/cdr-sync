package org.openmrs.module.cdrsync.fragment.controller;

import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.module.cdrsync.api.CdrContainerService;
import org.openmrs.module.cdrsync.api.impl.CdrContainerServiceImpl;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ContainerFragmentController {
	
	CdrContainerService containerService = new CdrContainerServiceImpl();
	
	//	CdrContainerService containerService = Context.getService(CdrContainerService.class);
	
	public void controller(FragmentModel model, @SpringBean("userService") UserService service) {
		model.addAttribute("users", service.getAllUsers());
	}
	
	public String getPatientsFromInitial() throws IOException {
		return containerService.getAllPatients();
	}
	
	public String getPatientsFromLastSync() throws IOException {
		String lastSync = Context.getAdministrationService().getGlobalProperty("last.cdr.sync");
		System.out.println("From db::" + lastSync);
		if (lastSync != null && !lastSync.isEmpty()) {
			try {
				DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy'T'hh:mm:ss");
				Date lastSyncDate = dateFormat.parse(lastSync);
				System.out.println("Last sync date::" + lastSyncDate);
				return containerService.getPatientsByEncounterDateTime(lastSyncDate, new Date());
			}
			catch (ParseException e) {
				System.out.println("parse exception::" + e.getMessage());
				e.printStackTrace();
				return e.getMessage();
			}
			catch (IOException e) {
				System.out.println("Io exception::" + e.getMessage());
				e.printStackTrace();
				return e.getMessage();
			}
			
		} else {
			return containerService.getAllPatients();
		}
	}
	
	public String getPatientsFromCustomDate(@RequestParam(value = "from") String from, @RequestParam(value = "to") String to)
	        throws IOException, ParseException {
		System.out.println(from + ":::" + to);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date startDate = dateFormat.parse(from);
		Date endDate = dateFormat.parse(to);
		return containerService.getPatientsByEncounterDateTime(startDate, endDate);
	}
	
	public void saveLastSync() {
		containerService.saveLastSyncDate();
	}
}
