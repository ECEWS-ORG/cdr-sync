package org.openmrs.module.cdrsync.fragment.controller;

import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.module.cdrsync.model.dto.ApiResponse;
import org.openmrs.module.cdrsync.api.nfc_card.services.NfcCardService;
import org.openmrs.module.cdrsync.api.nfc_card.services.impl.NfcCardServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

import static org.openmrs.module.cdrsync.utils.AppUtil.getObjectMapper;
import static org.springframework.http.HttpStatus.OK;

public class NfcFragmentController {
	
	private final Logger logger = Logger.getLogger(this.getClass().getName());
	
	private NfcCardService nfcCardService;
	
	public NfcCardService getNfcCardService() {
		if (nfcCardService == null) {
			nfcCardService = new NfcCardServiceImpl();
		}
		return nfcCardService;
	}
	
	public ResponseEntity<ApiResponse<?>> getPatientDetails(
	        @RequestParam(value = "patientIdentifier") String patientIdentifier,
	        @RequestParam(value = "identifierType", required = false, defaultValue = "4") int identifierType) {
		return new ResponseEntity<ApiResponse<?>>(getNfcCardService().getPatientDetails(patientIdentifier, identifierType),
		        OK);
	}
	
	public ResponseEntity<ApiResponse<?>> getNfcCardMapperByNfcCardId(@RequestParam(value = "nfcCardId") String nfcCardId,
	        HttpServletRequest request) {
		String hostName = request.getServerName();
		logger.info("Host Name::" + hostName);
		int port = request.getServerPort();
		logger.info("Port::" + port);
		return new ResponseEntity<ApiResponse<?>>(
		        getNfcCardService().getNfcCardMapperByNfcCardId(nfcCardId, hostName, port), OK);
	}
	
	public ResponseEntity<ApiResponse<?>> getNfcCardByPatientIdentifier(
	        @RequestParam(value = "patientIdentifier") String patientIdentifier) {
		return new ResponseEntity<ApiResponse<?>>(getNfcCardService().getNfcCardByPatientIdentifier(patientIdentifier), OK);
	}
	
	public ResponseEntity<ApiResponse<String>> saveNfcCardMapper(@RequestParam(value = "nfcCardId") String nfcCardId,
	        @RequestParam(value = "patientIdentifier") String patientIdentifier,
	        @RequestParam(value = "patientUuid") String patientUuid,
	        @RequestParam(value = "patientPhoneNo", required = false) String patientPhoneNo, HttpServletRequest request) {
		String hostName = request.getServerName();
		int port = request.getServerPort();
		return new ResponseEntity<ApiResponse<String>>(getNfcCardService().saveNfcCardMapper(nfcCardId, patientIdentifier,
		    patientUuid, patientPhoneNo, hostName, port), OK);
	}
	
	public ResponseEntity<ApiResponse<String>> readNfcCard() {
		Socket socket = null;
		try {
			socket = new Socket("localhost", 12345);
			
			// Create input stream for data exchange
			
			InputStream inputStream = socket.getInputStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
			
			// Receive data from the server
			String serverResponse = in.readLine();
			logger.info("Server says: " + serverResponse);
			
			ObjectMapper mapper = getObjectMapper();
			ApiResponse apiResponse = mapper.readValue(serverResponse, ApiResponse.class);
			
			return new ResponseEntity<ApiResponse<String>>(apiResponse, OK);
		}
		catch (IOException e) {
			return new ResponseEntity<ApiResponse<String>>(new ApiResponse<String>(false, e.getMessage()), OK);
		}
		finally {
			if (socket != null) {
				try {
					socket.close();
				}
				catch (IOException e) {
					logger.warning(e.getMessage());
				}
			}
		}
	}
}
