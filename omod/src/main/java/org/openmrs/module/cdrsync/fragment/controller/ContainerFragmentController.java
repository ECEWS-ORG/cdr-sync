package org.openmrs.module.cdrsync.fragment.controller;

import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.module.cdrsync.api.CdrContainerService;
import org.openmrs.module.cdrsync.api.CdrSyncAdminService;
import org.openmrs.module.cdrsync.api.impl.CdrContainerServiceImpl;
import org.openmrs.module.cdrsync.model.CdrSyncBatch;
import org.openmrs.module.cdrsync.model.enums.SyncStatus;
import org.openmrs.module.cdrsync.model.enums.SyncType;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ContainerFragmentController {
	
	CdrContainerService containerService = new CdrContainerServiceImpl();
	
	User user = Context.getAuthenticatedUser();
	
	CdrSyncBatch cdrSyncBatch1;
	
	//	CdrContainerService containerService = Context.getService(CdrContainerService.class);
	
	public void controller(FragmentModel model, @SpringBean("userService") UserService service) {
		String lastSyncDate = Context.getAdministrationService().getGlobalProperty("last.cdr.sync");
		List<CdrSyncBatch> recentSyncBatches = Context.getService(CdrSyncAdminService.class).getRecentSyncBatches();
		System.out.println("recent sync batches::" + recentSyncBatches.size());
		model.addAttribute("users", service.getAllUsers());
		model.addAttribute("lastSyncDate", lastSyncDate);
		model.addAttribute("recentSyncBatches", recentSyncBatches);
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
	
	public ResponseEntity<String> getPatientsProcessed(@RequestParam(value = "total") String total,
	        @RequestParam(value = "type") String type) {
		CdrSyncBatch cdrSyncBatch = Context.getService(CdrSyncAdminService.class).getCdrSyncBatchByStatusAndOwner(
		    SyncStatus.IN_PROGRESS.name(), user.getUsername(), type);
		if (cdrSyncBatch == null) {
			cdrSyncBatch = new CdrSyncBatch();
			cdrSyncBatch.setPatientsProcessed(0);
			cdrSyncBatch.setPatients(Integer.parseInt(total));
			cdrSyncBatch.setOwnerUsername(user.getUsername());
			cdrSyncBatch.setDateStarted(new Date());
			cdrSyncBatch.setStatus(SyncStatus.IN_PROGRESS.name());
			cdrSyncBatch.setSyncType(type);
			Context.getService(CdrSyncAdminService.class).saveCdrSyncBatch(cdrSyncBatch);
		}
		cdrSyncBatch1 = cdrSyncBatch;
		Integer id = cdrSyncBatch.getId();
		String resp;
		if (id != null) {
			resp = cdrSyncBatch.getPatientsProcessed() + "/" + cdrSyncBatch.getId();
		} else {
			resp = cdrSyncBatch.getPatientsProcessed() + "/" + 0;
		}
		//		System.out.println("Batch id::" + cdrSyncBatch1.getId());
		return new ResponseEntity<String>(resp, HttpStatus.OK);
	}
	
	public void updateCdrSyncBatch(@RequestParam(value = "type") String type,
	        @RequestParam(value = "processed") String processed, @RequestParam(value = "id") String id,
	        @RequestParam(value = "total") String total) {
		int processedPatients = Integer.parseInt(processed);
		int batchId = Integer.parseInt(id);
		int totalPatients = Integer.parseInt(total);
		cdrSyncBatch1 = new CdrSyncBatch();
		cdrSyncBatch1.setId(batchId);
		cdrSyncBatch1.setPatientsProcessed(processedPatients);
		cdrSyncBatch1.setPatients(totalPatients);
		cdrSyncBatch1.setOwnerUsername(user.getUsername());
		cdrSyncBatch1.setStatus(SyncStatus.IN_PROGRESS.name());
		cdrSyncBatch1.setSyncType(type);
		cdrSyncBatch1 = Context.getService(CdrSyncAdminService.class).saveCdrSyncBatch(cdrSyncBatch1);
		System.out.println("Updated batch::" + cdrSyncBatch1.getPatientsProcessed());
	}
	
	public ResponseEntity<String> getPatientsFromInitial(HttpServletRequest request,
	        @RequestParam(value = "start") String start, @RequestParam(value = "length") String length,
	        @RequestParam(value = "total") String total, @RequestParam(value = "id") String id) throws IOException {
		System.out.println("From initial::" + start + " " + length);
		String response;
		int startPoint = Integer.parseInt(start);
		long totalPatients = Long.parseLong(total);
		int lengthOfPatients = Integer.parseInt(length);
		int batchId = Integer.parseInt(id);
		String contextPath = request.getContextPath();
		System.out.println("context path: " + contextPath);
		String fullContextPath = request.getSession().getServletContext().getRealPath(contextPath);
		System.out.println("full context path: " + fullContextPath);
		response = containerService.getAllPatients(totalPatients, startPoint, lengthOfPatients, SyncType.INITIAL.name(),
		    fullContextPath);
		if (response.contains("Sync complete!")) {
			cdrSyncBatch1 = new CdrSyncBatch();
			cdrSyncBatch1.setId(batchId);
			cdrSyncBatch1.setDateCompleted(new Date());
			cdrSyncBatch1.setStatus(SyncStatus.COMPLETED.name());
			cdrSyncBatch1.setPatientsProcessed(startPoint);
			cdrSyncBatch1.setPatients((int) totalPatients);
			cdrSyncBatch1.setOwnerUsername(user.getUsername());
			cdrSyncBatch1.setSyncType(SyncType.INITIAL.name());
			Context.getService(CdrSyncAdminService.class).saveCdrSyncBatch(cdrSyncBatch1);
		}
		return new ResponseEntity<String>(response, HttpStatus.OK);
	}
	
	public ResponseEntity<String> getPatientsFromLastSync(HttpServletRequest request,
	        @RequestParam(value = "start") String start, @RequestParam(value = "length") String length,
	        @RequestParam(value = "total") String total, @RequestParam(value = "id") String id) throws IOException {
		String lastSync = Context.getAdministrationService().getGlobalProperty("last.cdr.sync");
		System.out.println("From db::" + lastSync);
		String response;
		String contextPath = request.getContextPath();
		String fullContextPath = request.getSession().getServletContext().getRealPath(contextPath);
		if (lastSync != null && !lastSync.isEmpty()) {
			try {
				DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy'T'hh:mm:ss");
				Date lastSyncDate = dateFormat.parse(lastSync);
				System.out.println("Last sync date::" + lastSyncDate);
				response = containerService.getAllPatients(Long.valueOf(total), lastSyncDate, new Date(),
				    Integer.parseInt(start), Integer.parseInt(length), SyncType.INCREMENTAL.name(), fullContextPath);
				return checkIfSyncHasCompletedAndUpdateSyncBatch(start, total, id, response);
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
			    Integer.parseInt(length), SyncType.INCREMENTAL.name(), fullContextPath);
			return checkIfSyncHasCompletedAndUpdateSyncBatch(start, total, id, response);
		}
	}
	
	private ResponseEntity<String> checkIfSyncHasCompletedAndUpdateSyncBatch(String start, String total, String id,
	        String response) {
		if (response.contains("Sync complete!")) {
			updateSyncBatch(start, total, id);
			cdrSyncBatch1.setSyncType(SyncType.INCREMENTAL.name());
			Context.getService(CdrSyncAdminService.class).saveCdrSyncBatch(cdrSyncBatch1);
		}
		return new ResponseEntity<String>(response, HttpStatus.OK);
	}
	
	public ResponseEntity<String> getPatientsFromCustomDate(HttpServletRequest request,
	        @RequestParam(value = "from") String from, @RequestParam(value = "to") String to,
	        @RequestParam(value = "start") String start, @RequestParam(value = "length") String length,
	        @RequestParam(value = "total") String total, @RequestParam(value = "id") String id) throws ParseException,
	        IOException {
		System.out.println(from + ":::" + to);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date startDate = dateFormat.parse(from);
		Date endDate = dateFormat.parse(to);
		String contextPath = request.getContextPath();
		String fullContextPath = request.getSession().getServletContext().getRealPath(contextPath);
		String response = containerService.getAllPatients(Long.parseLong(total), startDate, endDate,
		    Integer.parseInt(start), Integer.parseInt(length), SyncType.CUSTOM.name(), fullContextPath);
		if (response.contains("Sync complete!")) {
			updateSyncBatch(start, total, id);
			cdrSyncBatch1.setSyncType(SyncType.CUSTOM.name());
			Context.getService(CdrSyncAdminService.class).saveCdrSyncBatch(cdrSyncBatch1);
		}
		return new ResponseEntity<String>(response, HttpStatus.OK);
	}
	
	private void updateSyncBatch(String start, String total, String id) {
		cdrSyncBatch1 = new CdrSyncBatch();
		cdrSyncBatch1.setId(Integer.parseInt(id));
		cdrSyncBatch1.setDateCompleted(new Date());
		cdrSyncBatch1.setStatus(SyncStatus.COMPLETED.name());
		cdrSyncBatch1.setPatientsProcessed(Integer.parseInt(start));
		cdrSyncBatch1.setPatients(Integer.parseInt(total));
		cdrSyncBatch1.setOwnerUsername(user.getUsername());
	}
	
	public void saveLastSync() {
		containerService.saveLastSyncDate();
	}
	
	public void downloadFile(@RequestParam(value = "filePath") String filePath, HttpServletResponse response) {
		System.out.println("File path::" + filePath);
		File file = new File(filePath);
		if (!file.exists()) {
			String errorMessage = "Sorry. The file you are looking for does not exist";
			System.out.println(errorMessage);
			OutputStream outputStream;
			try {
				outputStream = response.getOutputStream();
				outputStream.write(errorMessage.getBytes(Charset.forName("UTF-8")));
				outputStream.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			//			return new ResponseEntity<String>(errorMessage, HttpStatus.NOT_FOUND);
		}
		
		// Set the response headers
		response.setContentType("application/zip");
		response.setHeader("Content-Disposition", "attachment;filename=" + file.getName());
		System.out.println("File name::" + file.getName());
		
		// Create a buffer for reading the file
		byte[] buffer = new byte[1024];
		
		try {
			//			InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
			// Read the file contents and write them to the response output stream
			FileInputStream fileIn = new FileInputStream(filePath);
			OutputStream outStream = response.getOutputStream();
			
			int len;
			while ((len = fileIn.read(buffer)) > 0) {
				outStream.write(buffer, 0, len);
			}
			
			fileIn.close();
			outStream.flush();
			//			return new ResponseEntity<Resource>(resource, HttpStatus.OK);
		}
		catch (IOException e) {
			e.printStackTrace();
			//			return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
