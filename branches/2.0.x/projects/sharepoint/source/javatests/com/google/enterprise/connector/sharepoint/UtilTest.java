package com.google.enterprise.connector.sharepoint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;
/**
 * @author amit_kagrawal
 * */
public class UtilTest extends TestCase{
	
	public void testListItemChangesStringToCalendar() throws ParseException{
		//Date in UTC format
		String listItemChangesString = "2008-07-16T05:30:35Z";
		//UTC format to Calender
		Calendar cal = Util.listItemChangesStringToCalendar(listItemChangesString);
		
		assertNotNull(cal);
		//cal.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date dt = cal.getTime();
		final String TIMEFORMAT3 = "yyyy-MM-dd'T'HH:mm:ss'Z'";
		final SimpleDateFormat SIMPLE_DATE_FORMATTER3 =new SimpleDateFormat(TIMEFORMAT3);
		System.out.println("Time: "+SIMPLE_DATE_FORMATTER3.format(dt));
		
	}
}
