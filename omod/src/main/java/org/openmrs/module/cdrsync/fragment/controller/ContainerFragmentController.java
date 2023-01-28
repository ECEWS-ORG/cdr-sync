package org.openmrs.module.cdrsync.fragment.controller;

import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.module.cdrsync.api.CdrContainerService;
import org.openmrs.module.cdrsync.api.impl.CdrContainerServiceImpl;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
		String lastSyncDate = Context.getAdministrationService().getGlobalProperty("last.cdr.sync");
		
		model.addAttribute("users", service.getAllUsers());
		model.addAttribute("lastSyncDate", lastSyncDate);
	}
	
	public ResponseEntity<String> getPatientsFromInitial() throws IOException {
		return new ResponseEntity<String>(containerService.getAllPatients(), HttpStatus.OK);
	}
	
	public ResponseEntity<String> getPatientsFromLastSync() throws IOException {
		String lastSync = Context.getAdministrationService().getGlobalProperty("last.cdr.sync");
		System.out.println("From db::" + lastSync);
		if (lastSync != null && !lastSync.isEmpty()) {
			try {
				DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy'T'hh:mm:ss");
				Date lastSyncDate = dateFormat.parse(lastSync);
				System.out.println("Last sync date::" + lastSyncDate);
				return new ResponseEntity<String>(containerService.getAllPatients(lastSyncDate, new Date()), HttpStatus.OK);
			}
			catch (ParseException e) {
				System.out.println("parse exception::" + e.getMessage());
				e.printStackTrace();
				return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
			catch (IOException e) {
				System.out.println("Io exception::" + e.getMessage());
				e.printStackTrace();
				return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
			
		} else {
			return new ResponseEntity<String>(containerService.getAllPatients(), HttpStatus.OK);
		}
	}
	
	public ResponseEntity<String> getPatientsFromCustomDate(@RequestParam(value = "from") String from,
	        @RequestParam(value = "to") String to) throws IOException, ParseException {
		System.out.println(from + ":::" + to);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date startDate = dateFormat.parse(from);
		Date endDate = dateFormat.parse(to);
		return new ResponseEntity<String>(containerService.getAllPatients(startDate, endDate), HttpStatus.OK);
	}
	
	public void saveLastSync() {
		containerService.saveLastSyncDate();
	}
}
