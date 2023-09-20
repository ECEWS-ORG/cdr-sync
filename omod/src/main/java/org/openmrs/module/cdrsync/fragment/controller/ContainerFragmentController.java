package org.openmrs.module.cdrsync.fragment.controller;

import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.module.cdrsync.api.extractor.CdrContainerService;
import org.openmrs.module.cdrsync.api.extractor.CdrSyncAdminService;
import org.openmrs.module.cdrsync.api.extractor.CdrSyncPatientService;
import org.openmrs.module.cdrsync.api.extractor.impl.CdrContainerServiceImpl;
import org.openmrs.module.cdrsync.api.nfc_card.services.NfcCardMapperService;
import org.openmrs.module.cdrsync.model.extractor.CdrSyncBatch;
import org.openmrs.module.cdrsync.model.extractor.enums.SyncStatus;
import org.openmrs.module.cdrsync.model.extractor.enums.SyncType;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class ContainerFragmentController {
	
	private final Logger logger = Logger.getLogger(this.getClass().getName());
	
	private CdrContainerService containerService;
	
	private CdrContainerService getContainerService() {
		if (containerService == null)
			containerService = new CdrContainerServiceImpl();
		return containerService;
	}
	
	public void controller(FragmentModel model, @SpringBean("userService") UserService service) {
		String lastSyncDate = Context.getAdministrationService().getGlobalProperty("last.cdr.sync");
		List<CdrSyncBatch> recentSyncBatches = Context.getService(CdrSyncAdminService.class).getRecentSyncBatches();
		Long patientsOnArt = Context.getService(CdrSyncPatientService.class).getCountOfPatientsOnArt(false);
		Long patientsMappedToNfc = Context.getService(NfcCardMapperService.class).getNumberOfMappedPatients();
		double percentageMapped = (double) patientsMappedToNfc * 100 / patientsOnArt;
		percentageMapped = Math.round(percentageMapped * 100);
		percentageMapped = percentageMapped / 100;
		if (lastSyncDate == null || lastSyncDate.isEmpty()) {
			lastSyncDate = "N/A";
		}
		model.addAttribute("users", service.getAllUsers());
		model.addAttribute("lastSyncDate", lastSyncDate);
		model.addAttribute("recentSyncBatches", recentSyncBatches);
		model.addAttribute("patientsOnArt", patientsOnArt);
		model.addAttribute("patientsMappedToNfc", patientsMappedToNfc);
		model.addAttribute("percentageMapped", percentageMapped);
	}
	
	public ResponseEntity<List<CdrSyncBatch>> getRecentSyncBatches() {
		List<CdrSyncBatch> recentSyncBatches = Context.getService(CdrSyncAdminService.class).getRecentSyncBatches();
		return new ResponseEntity<List<CdrSyncBatch>>(recentSyncBatches, HttpStatus.OK);
	}
	
	public ResponseEntity<String> getLastSyncDate() {
		String lastSyncDate = Context.getAdministrationService().getGlobalProperty("last.cdr.sync");
		if (lastSyncDate == null || lastSyncDate.isEmpty()) {
			lastSyncDate = "N/A";
		}
		return new ResponseEntity<String>(lastSyncDate, HttpStatus.OK);
	}
	
	public ResponseEntity<Long> getPatientsCount() throws IOException {
		Long response = getContainerService().getPatientsCount(true);
		logger.info("Total count from db::" + response);
		return new ResponseEntity<Long>(response, HttpStatus.OK);
	}
	
	public ResponseEntity<String> getPatientsCountFromLastSync() throws IOException {
		String lastSync = Context.getAdministrationService().getGlobalProperty("last.cdr.sync");
		logger.info("Last sync date from db::" + lastSync);
		String response;
		if (lastSync != null && !lastSync.isEmpty()) {
			try {
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				//				DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy'T'hh:mm:ss");
				Date lastSyncDate = dateFormat.parse(lastSync);
				logger.info("Last sync date::" + lastSyncDate);
				response = lastSync + "/" + getContainerService().getPatientsCount(lastSyncDate, new Date(), true);
			}
			catch (ParseException e) {
				e.printStackTrace();
				return new ResponseEntity<String>(lastSync + "/0", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			response = lastSync + "/" + getContainerService().getPatientsCount(true);
		}
		return new ResponseEntity<String>(response, HttpStatus.OK);
	}
	
	public ResponseEntity<Long> getPatientsCountFromCustomDate(@RequestParam(value = "from") String from,
	        @RequestParam(value = "to") String to) throws IOException {
		logger.info("From custom::" + from + " to " + to);
		long response;
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date fromDate = dateFormat.parse(from);
			Date toDate = dateFormat.parse(to);
			response = getContainerService().getPatientsCount(fromDate, toDate, true);
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
		    SyncStatus.IN_PROGRESS.name(), Context.getAuthenticatedUser().getUsername(), type);
		if (cdrSyncBatch == null) {
			cdrSyncBatch = new CdrSyncBatch();
			cdrSyncBatch.setPatientsProcessed(0);
			cdrSyncBatch.setPatients(Integer.parseInt(total));
			cdrSyncBatch.setOwnerUsername(Context.getAuthenticatedUser().getUsername());
			cdrSyncBatch.setDateStarted(new Date());
			cdrSyncBatch.setStatus(SyncStatus.IN_PROGRESS.name());
			cdrSyncBatch.setSyncType(type);
			Context.getService(CdrSyncAdminService.class).saveCdrSyncBatch(cdrSyncBatch);
		}
		Integer id = cdrSyncBatch.getId();
		String resp;
		if (id != null) {
			resp = cdrSyncBatch.getPatientsProcessed() + "/" + id + "/" + cdrSyncBatch.getSyncType() + "/"
			        + cdrSyncBatch.getPatients();
		} else {
			resp = cdrSyncBatch.getPatientsProcessed() + "/" + 0;
		}
		return new ResponseEntity<String>(resp, HttpStatus.OK);
	}
	
	public void updateCdrSyncBatch(@RequestParam(value = "type") String type,
	        @RequestParam(value = "processed") String processed, @RequestParam(value = "id") String id,
	        @RequestParam(value = "total") String total) {
		int processedPatients = Integer.parseInt(processed);
		int batchId = Integer.parseInt(id);
		Context.getService(CdrSyncAdminService.class).updateCdrSyncBatchStatus(batchId, SyncStatus.IN_PROGRESS.name(),
		    processedPatients, false);
	}
	
	public void updateCdrSyncBatchToCancelled(@RequestParam(value = "id") String id) {
		int batchId = Integer.parseInt(id);
		Context.getService(CdrSyncAdminService.class).updateCdrSyncBatchStatus(batchId, SyncStatus.CANCELLED.name(), null,
		    true);
	}
	
	public ResponseEntity<String> getPatientsFromInitial(HttpServletRequest request,
	        @RequestParam(value = "start") String start, @RequestParam(value = "length") String length,
	        @RequestParam(value = "total") String total, @RequestParam(value = "id") String id) throws IOException {
		logger.info("From initial::" + start + " " + length);
		String response;
		int startPoint = Integer.parseInt(start);
		long totalPatients = Long.parseLong(total);
		int lengthOfPatients = Integer.parseInt(length);
		int batchId = Integer.parseInt(id);
		String contextPath = request.getContextPath();
		logger.info("context path: " + contextPath);
		String fullContextPath = request.getSession().getServletContext().getRealPath(contextPath);
		logger.info("full context path: " + fullContextPath);
		String host = request.getServerName();
		logger.info("Host::" + host);
		int port = request.getServerPort();
		logger.info("Port::" + port);
		String url = "http://" + host + ":" + port + contextPath + "/ws/rest/v1/module?v=full";
		response = getContainerService().getAllPatients(totalPatients, startPoint, lengthOfPatients,
		    SyncType.INITIAL.name(), fullContextPath, contextPath, url, Integer.parseInt(id));
		if (response.contains("Sync complete!")) {
			Context.getService(CdrSyncAdminService.class).updateCdrSyncBatchStatus(batchId, SyncStatus.COMPLETED.name(),
			    startPoint, true);
		} else if (response.contains("Cannot resume sync, kindly start a new sync!")) {
			Context.getService(CdrSyncAdminService.class).updateCdrSyncBatchStatus(batchId, SyncStatus.CANCELLED.name(),
			    startPoint, true);
		}
		return new ResponseEntity<String>(response, HttpStatus.OK);
	}
	
	public ResponseEntity<String> getPatientsFromLastSync(HttpServletRequest request,
	        @RequestParam(value = "start") String start, @RequestParam(value = "length") String length,
	        @RequestParam(value = "total") String total, @RequestParam(value = "id") String id) throws IOException {
		String lastSync = Context.getAdministrationService().getGlobalProperty("last.cdr.sync");
		logger.info("From db::" + lastSync);
		String response;
		String contextPath = request.getContextPath();
		String fullContextPath = request.getSession().getServletContext().getRealPath(contextPath);
		String host = request.getServerName();
		logger.info("Host::" + host);
		int port = request.getServerPort();
		logger.info("Port::" + port);
		String url = "http://" + host + ":" + port + contextPath + "/ws/rest/v1/module?v=full";
		if (lastSync != null && !lastSync.isEmpty()) {
			try {
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				//				DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy'T'hh:mm:ss");
				Date lastSyncDate = dateFormat.parse(lastSync);
				logger.info("Last sync date::" + lastSyncDate);
				response = getContainerService().getAllPatients(Long.valueOf(total), lastSyncDate, new Date(),
				    Integer.parseInt(start), Integer.parseInt(length), SyncType.INCREMENTAL.name(), fullContextPath,
				    contextPath, url, Integer.parseInt(id));
				return checkIfSyncHasCompletedAndUpdateSyncBatch(start, total, id, response);
			}
			catch (ParseException e) {
				logger.severe("parse exception::" + e.getMessage());
				e.printStackTrace();
				return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
			catch (IOException e) {
				logger.severe("IO exception::" + e.getMessage());
				e.printStackTrace();
				return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
			
		} else {
			response = getContainerService().getAllPatients(Long.parseLong(total), Integer.parseInt(start),
			    Integer.parseInt(length), SyncType.INCREMENTAL.name(), fullContextPath, contextPath, url,
			    Integer.parseInt(id));
			return checkIfSyncHasCompletedAndUpdateSyncBatch(start, total, id, response);
		}
	}
	
	public void updateCdrSyncBatchStartAndEndDate(@RequestParam(value = "id") String id,
	        @RequestParam(value = "startDate") String startDate, @RequestParam(value = "endDate") String endDate)
	        throws ParseException {
		logger.info("Updating start and end date range");
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date lastSyncDate = dateFormat.parse(startDate);
		Date syncEndDate = dateFormat.parse(endDate);
		Context.getService(CdrSyncAdminService.class).updateCdrSyncBatchStartAndEndDateRange(Integer.parseInt(id),
		    lastSyncDate, syncEndDate);
	}
	
	private ResponseEntity<String> checkIfSyncHasCompletedAndUpdateSyncBatch(String start, String total, String id,
	        String response) {
		if (response.contains("Sync complete!")) {
			Context.getService(CdrSyncAdminService.class).updateCdrSyncBatchStatus(Integer.parseInt(id),
			    SyncStatus.COMPLETED.name(), Integer.parseInt(start), true);
		} else if (response.contains("Cannot resume sync, kindly start a new sync!")) {
			Context.getService(CdrSyncAdminService.class).updateCdrSyncBatchStatus(Integer.parseInt(id),
			    SyncStatus.CANCELLED.name(), Integer.parseInt(start), true);
		}
		return new ResponseEntity<String>(response, HttpStatus.OK);
	}
	
	public ResponseEntity<String> getPatientsFromCustomDate(HttpServletRequest request,
	        @RequestParam(value = "from") String from, @RequestParam(value = "to") String to,
	        @RequestParam(value = "start") String start, @RequestParam(value = "length") String length,
	        @RequestParam(value = "total") String total, @RequestParam(value = "id") String id) throws ParseException,
	        IOException {
		logger.info(from + ":::" + to);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date startDate = dateFormat.parse(from);
		Date endDate = dateFormat.parse(to);
		String contextPath = request.getContextPath();
		String fullContextPath = request.getSession().getServletContext().getRealPath(contextPath);
		String host = request.getServerName();
		logger.info("Host::" + host);
		int port = request.getServerPort();
		logger.info("Port::" + port);
		String url = "http://" + host + ":" + port + contextPath + "/ws/rest/v1/module?v=full";
		String response = getContainerService().getAllPatients(Long.parseLong(total), startDate, endDate,
		    Integer.parseInt(start), Integer.parseInt(length), SyncType.CUSTOM.name(), fullContextPath, contextPath, url,
		    Integer.parseInt(id));
		if (response.contains("Sync complete!")) {
			Context.getService(CdrSyncAdminService.class).updateCdrSyncBatchStatus(Integer.parseInt(id),
			    SyncStatus.COMPLETED.name(), Integer.parseInt(start), true);
		} else if (response.contains("Cannot resume sync, kindly start a new sync!")) {
			Context.getService(CdrSyncAdminService.class).updateCdrSyncBatchStatus(Integer.parseInt(id),
			    SyncStatus.CANCELLED.name(), Integer.parseInt(start), true);
		}
		return new ResponseEntity<String>(response, HttpStatus.OK);
	}
	
	public void saveLastSync() {
		getContainerService().saveLastSyncDate();
	}
	
	public ResponseEntity<String> deleteCdrSyncBatch(@RequestParam(value = "id") String id) {
		Context.getService(CdrSyncAdminService.class).deleteCdrSyncBatch(Integer.parseInt(id));
		return new ResponseEntity<String>("Batch deleted successfully!", HttpStatus.OK);
	}
}
