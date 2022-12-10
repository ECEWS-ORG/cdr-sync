/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openmrs.module.cdrsync.api.dao;

import groovyjarjarcommonscli.ParseException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Tony
 */
public class DateFormatter {
	
	/**
	 * @param myDate
	 * @return
	 */
	public static String formatDate(String myDate) throws java.text.ParseException {
		
		Date newDate = new SimpleDateFormat("yyyy-MM-dd").parse(myDate);
		
		SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM, yyyy");
		String strDate = formatter.format(newDate);
		
		return strDate;
	}
	
	public static boolean isNumeric(String strNum) {
		if (strNum == null) {
			return false;
		}
		try {
			double d = Double.parseDouble(strNum);
		}
		catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}
	
}
