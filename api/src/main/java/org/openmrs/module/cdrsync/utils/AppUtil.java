package org.openmrs.module.cdrsync.utils;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.api.context.Context;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.cdrsync.api.DatimMapService;
import org.openmrs.module.cdrsync.model.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class AppUtil {
	
	static Logger logger = Logger.getLogger(AppUtil.class.getName());
	
	private static String datimCode;
	
	private static String facilityName;
	
	private static String partnerShortName;
	
	private static ObjectMapper objectMapper;
	
	private static DatimMap datimMap;
	
	private AppUtil() {
	}
	
	public static String getDatimCode() {
		if (datimCode == null || datimCode.isEmpty())
			datimCode = Context.getAdministrationService().getGlobalProperty("facility_datim_code");
		return datimCode;
	}
	
	public static String getFacilityName() {
		if (facilityName == null || facilityName.isEmpty())
			facilityName = Context.getAdministrationService().getGlobalProperty("Facility_Name");
		return facilityName;
	}
	
	private static ObjectMapper getObjectMapper() {
		if (objectMapper == null) {
			objectMapper = new ObjectMapper();
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			objectMapper.setDateFormat(df);
		}
		return objectMapper;
	}
	
	public static DatimMap getDatimMap() {
		if (datimMap == null)
			datimMap = Context.getService(DatimMapService.class).getDatimMapByDatimCode(getDatimCode());
		return datimMap;
	}
	
	private static String getPartnerShortName() {
		if (partnerShortName == null || partnerShortName.isEmpty())
			partnerShortName = Context.getAdministrationService().getGlobalProperty("partner_short_name");
		return partnerShortName;
	}
	
	public static String ensureDownloadDirectoryExists(String contextPath) {
		String downloadDirectory = Paths.get(new File(contextPath).getParentFile().toString(), "downloads").toString();
		File file = new File(downloadDirectory);
		if (!file.exists() && !file.mkdirs()) {
			throw new RuntimeException("Unable to create download directory");
		}
		return downloadDirectory;
	}
	
	public static String ensureReportDirectoryExists(String contextPath, String reportName, int start) {
		String downloadDirectory = ensureDownloadDirectoryExists(contextPath);
		String reportDirectory = Paths.get(downloadDirectory, reportName).toString();
		File file = new File(reportDirectory);
		if (!file.exists() && !file.mkdirs()) {
			throw new RuntimeException("Unable to create report directory");
		} else if (file.exists()) {
			if (start == 0) {
				try {
					logger.info("Cleaning report directory");
					FileUtils.cleanDirectory(file);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return reportDirectory;
	}
	
	public static List<Integer> getConfidentialConcepts() {
		return new ArrayList<>(Arrays.asList(159635, 162729, 160638, 160641, 160642));
	}
	
	public static void writeObjectToFile(Object object, String fileName, String reportFolder) throws IOException {
		
		File folder = new File(reportFolder);
		
		File dir = new File(folder, "jsonFiles");
		if (!dir.exists() && !dir.mkdirs()) {
			throw new RuntimeException("Unable to create directory " + dir.getAbsolutePath());
		}
		String json = getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(object);
		File file = new File(dir, fileName);
		FileUtils.writeStringToFile(file, json, "UTF-8");
		
		if (dir.listFiles() != null && Objects.requireNonNull(dir.listFiles()).length == 10000) {
			try {
				String facility = getFacilityName().replaceAll(" ", "_");
				String dateString = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
				ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(Paths.get(
				    folder.getAbsolutePath(),
				    getPartnerShortName() + "_" + getDatimCode() + "_" + facility + "_" + dateString + "_"
				            + new Date().getTime() + ".zip")));
				zipDirectory(dir, dir.getName(), zos);
				zos.close();
				FileUtils.cleanDirectory(dir);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	public static String zipFolder(String type, String reportFolder, String contextPath) {
		File folder = new File(reportFolder);
		File dir = new File(folder, "jsonFiles");
		StringBuilder result = new StringBuilder();
		String facility = getFacilityName().replaceAll(" ", "_");
		if (dir.listFiles() != null) {
			ZipOutputStream zipOutputStream;
			try {
				String dateString = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
				zipOutputStream = new ZipOutputStream(Files.newOutputStream(Paths.get(
				    folder.getAbsolutePath(),
				    getPartnerShortName() + "_" + getDatimCode() + "_" + facility + "_" + dateString + "_"
				            + new Date().getTime() + ".zip")));
				zipDirectory(dir, dir.getName(), zipOutputStream);
				zipOutputStream.close();
				FileUtils.deleteDirectory(dir);
				
				File[] files = folder.listFiles();
				if (files != null) {
					for (File file : files) {
						if (file.getName().endsWith(".zip")) {
							String filePath = file.getAbsolutePath();
							int index = filePath.lastIndexOf(contextPath.substring(1));
							filePath = filePath.substring(index);
							filePath = filePath.replace("\\", "\\\\");
							filePath = "\\\\" + filePath;
							result.append(filePath).append("&&");
						}
					}
				} else {
					System.out.println("No files found in the folder");
				}
			}
			catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		return "Sync complete!," + result.toString().trim();
	}
	
	private static void zipDirectory(File directory, String baseName, ZipOutputStream zos) throws IOException {
		File[] files = directory.listFiles();
		if (files != null) {
			byte[] buffer = new byte[1024];
			for (File file : files) {
				if (file.isDirectory()) {
					String name = baseName + "/" + file.getName();
					zipDirectory(file, name, zos);
				} else {
					FileInputStream fis = new FileInputStream(file);
					zos.putNextEntry(new ZipEntry(baseName + "/" + file.getName()));
					int length;
					while ((length = fis.read(buffer)) > 0) {
						zos.write(buffer, 0, length);
					}
					zos.closeEntry();
					fis.close();
				}
			}
		}
	}
	
	//	public static void syncContainersToCdr(List<Container> containers) throws IOException {
	//		ContainerWrapper containerWrapper = new ContainerWrapper(containers);
	//		if (Context.getRuntimeProperties().getProperty("cdr.sync.url") == null) {
	//			System.out.println("Setting sync url");
	//			Context.getRuntimeProperties().setProperty("cdr.sync.url", "http://localhost:8484/sync-containers");
	//		}
	//		String url = Context.getRuntimeProperties().getProperty("cdr.sync.url");
	//		System.out.println("Syncing to CDR::" + url);
	//		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()){
	//			String json = objectMapper.writeValueAsString(containerWrapper);
	//			String encryptedJson = Security.encrypt(json);
	//			EncryptedBody encryptedBody = new EncryptedBody(encryptedJson);
	//			HttpPost post = new HttpPost(url);
	//			HttpGet get = new HttpGet(url);
	//			get.setHeader("Content-Type", "application/json");
	//			post.setHeader("Content-Type", "application/json");
	//			post.setEntity(new StringEntity(objectMapper.writeValueAsString(encryptedBody)));
	//			try (CloseableHttpResponse response = httpClient.execute(post)){
	//				int statusCode = response.getStatusLine().getStatusCode();
	//				if (statusCode == 200) {
	//					String responseBody = EntityUtils.toString(response.getEntity());
	//					System.out.println("After successfully sending request::" + responseBody);
	//				} else {
	//					System.out.println("error sending request");
	//					System.out.println("status code::" + statusCode);
	//					throw new IOException("error sending request to cdr");
	//				}
	//			}
	//		}
	//	}
	
	public static void getFacilityMetaData(String reportFolder) {
		try {
			Collection<Module> loadedModules = ModuleFactory.getLoadedModules();
			List<ModuleInfo> moduleInfos = loadedModules.stream().map(ModuleInfo::new).collect(Collectors.toList());

			SystemProperty systemProperty = new SystemProperty();
			systemProperty.setOsName(System.getProperty("os.name"));
			systemProperty.setOsVersion(System.getProperty("os.version"));
			systemProperty.setOsArch(System.getProperty("os.arch"));
			systemProperty.setJavaVersion(System.getProperty("java.version"));
			systemProperty.setHostName(InetAddress.getLocalHost().getHostName());
			systemProperty.setDiskSpace(new File("/").getTotalSpace() / 1024 / 1024 / 1024);
			systemProperty.setUserName(System.getProperty("user.name"));
			systemProperty.setRamSize(((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean())
					.getTotalPhysicalMemorySize() / 1024 / 1024 / 1024);

			FacilityMetaData facilityMetaData = new FacilityMetaData(moduleInfos);
			facilityMetaData.setSystemProperty(systemProperty);
			facilityMetaData.setId(getDatimCode());
			facilityMetaData.setFacilityName(getFacilityName());
			facilityMetaData.setFacilityDatimCode(getDatimCode());
			String fileName = getPartnerShortName() + "_" + getDatimCode() + "_facilityMetaData.json";
			writeObjectToFile(facilityMetaData, fileName, reportFolder);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
