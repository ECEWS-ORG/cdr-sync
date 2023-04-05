package org.openmrs.module.cdrsync.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AppUtil {
	
	private AppUtil() {
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
		System.out.println("downloadDirectory = " + downloadDirectory);
		String reportDirectory = Paths.get(downloadDirectory, reportName).toString();
		System.out.println("reportDirectory = " + reportDirectory);
		File file = new File(reportDirectory);
		if (!file.exists() && !file.mkdirs()) {
			throw new RuntimeException("Unable to create report directory");
		} else if (file.exists()) {
			if (start == 0) {
				try {
					System.out.println("Cleaning report directory");
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
}
