package org.openmrs.module.cdrsync.fragment.controller;

import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.module.cdrsync.api.CdrContainerService;
import org.openmrs.module.cdrsync.api.impl.CdrContainerServiceImpl;
import org.openmrs.module.cdrsync.model.Partition;
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
import java.util.List;

public class ContainerFragmentController {
	
	CdrContainerService containerService = new CdrContainerServiceImpl();
	
	//	CdrContainerService containerService = Context.getService(CdrContainerService.class);
	
	public void controller(FragmentModel model, @SpringBean("userService") UserService service) {
		String lastSyncDate = Context.getAdministrationService().getGlobalProperty("last.cdr.sync");
		
		model.addAttribute("users", service.getAllUsers());
		model.addAttribute("lastSyncDate", lastSyncDate);
	}
	
	public ResponseEntity<Long> getPatientsCount() throws IOException {
		Long response = containerService.getPatientsCount(true);
		return new ResponseEntity<Long>(response, HttpStatus.OK);
	}
	
	public ResponseEntity<Long> getPatientsCountFromLastSync() throws IOException {
		String lastSync = Context.getAdministrationService().getGlobalProperty("last.cdr.sync");
		System.out.println("From db::" + lastSync);
		long response;
		if (lastSync != null && !lastSync.isEmpty()) {
			try {
				DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy'T'hh:mm:ss");
				Date lastSyncDate = dateFormat.parse(lastSync);
				System.out.println("Last sync date::" + lastSyncDate);
				response = containerService.getPatientsCount(lastSyncDate, new Date(), true);
			}
			catch (ParseException e) {
				e.printStackTrace();
				return new ResponseEntity<Long>(0L, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			response = containerService.getPatientsCount(true);
		}
		return new ResponseEntity<Long>(response, HttpStatus.OK);
	}
	
	public ResponseEntity<Long> getPatientsCountFromCustomDate(@RequestParam(value = "from") String from,
	        @RequestParam(value = "to") String to) throws IOException {
		System.out.println("From custom::" + from + " " + to);
		long response;
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date fromDate = dateFormat.parse(from);
			Date toDate = dateFormat.parse(to);
			response = containerService.getPatientsCount(fromDate, toDate, true);
		}
		catch (ParseException e) {
			e.printStackTrace();
			return new ResponseEntity<Long>(0L, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<Long>(response, HttpStatus.OK);
	}
	
	public ResponseEntity<String> getPatientsFromInitial(@RequestParam(value = "start") String start,
	        @RequestParam(value = "length") String length, @RequestParam(value = "total") String total) throws IOException {
		System.out.println("From initial::" + start + " " + length);
		String response;
		int count = Integer.parseInt(start);
		response = containerService.getAllPatients(Long.parseLong(total), count, Integer.parseInt(length));
		return new ResponseEntity<String>(response, HttpStatus.OK);
	}
	
	public ResponseEntity<String> getPatientsFromLastSync(@RequestParam(value = "start") String start,
	        @RequestParam(value = "length") String length, @RequestParam(value = "total") String total) throws IOException {
		String lastSync = Context.getAdministrationService().getGlobalProperty("last.cdr.sync");
		System.out.println("From db::" + lastSync);
		String response;
		if (lastSync != null && !lastSync.isEmpty()) {
			try {
				DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy'T'hh:mm:ss");
				Date lastSyncDate = dateFormat.parse(lastSync);
				System.out.println("Last sync date::" + lastSyncDate);
				response = containerService.getAllPatients(Long.valueOf(total), lastSyncDate, new Date(),
				    Integer.parseInt(start), Integer.parseInt(length));
				return new ResponseEntity<String>(response, HttpStatus.OK);
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
			response = containerService.getAllPatients(Long.parseLong(total), Integer.parseInt(start),
			    Integer.parseInt(length));
			return new ResponseEntity<String>(response, HttpStatus.OK);
		}
	}
	
	public ResponseEntity<String> getPatientsFromCustomDate(@RequestParam(value = "from") String from,
	        @RequestParam(value = "to") String to, @RequestParam(value = "start") String start,
	        @RequestParam(value = "length") String length, @RequestParam(value = "total") String total)
	        throws ParseException, IOException {
		System.out.println(from + ":::" + to);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date startDate = dateFormat.parse(from);
		Date endDate = dateFormat.parse(to);
		return new ResponseEntity<String>(containerService.getAllPatients(Long.parseLong(total), startDate, endDate,
		    Integer.parseInt(start), Integer.parseInt(length)), HttpStatus.OK);
	}
	
	public void saveLastSync() {
		containerService.saveLastSyncDate();
	}
}
