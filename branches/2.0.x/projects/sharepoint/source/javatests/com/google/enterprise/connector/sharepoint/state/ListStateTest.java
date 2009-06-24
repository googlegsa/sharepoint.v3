package com.google.enterprise.connector.sharepoint.state;

import java.util.ArrayList;
import java.util.GregorianCalendar;

import org.joda.time.DateTime;

import com.google.enterprise.connector.sharepoint.Util;
import com.google.enterprise.connector.sharepoint.client.SPDocument;

import junit.framework.TestCase;
/**
 * @author amit_kagrawal
 * */
public class ListStateTest extends TestCase{
	//set the logging 
	
	public void setup(){
		
	}
	static{
		System.setProperty("java.util.logging.config.file","logging.properties");
	}
	public void testCrawlQueue(){
		SPDocument doc1 = new SPDocument("id1", "url1",new GregorianCalendar(2007, 1, 1));
		SPDocument doc2 = new SPDocument("id2", "url2",new GregorianCalendar(2007, 1, 2));
		SPDocument doc3 = new SPDocument("id3", "url3",new GregorianCalendar(2007, 1, 3));
		SPDocument doc4 = new SPDocument("id4", "url4",new GregorianCalendar(2007, 1, 4));

		DateTime time1 = Util.parseDate("20080702T140520.411+0000");
		DateTime time2 = Util.parseDate("20080702T140516.411+0000");
		DateTime time3 = Util.parseDate("20080702T140508.411+0000");
		DateTime time4 = Util.parseDate("20080702T140509.411+0000");

 
		ListState ls1 =new ListState("{001-002-003}", time3);//foo
		ls1.setUrl("http://dummyList.com/1");
		
		ArrayList crawlQueueList1 = new ArrayList();
		crawlQueueList1.add(doc1);
		crawlQueueList1.add(doc2);
		crawlQueueList1.add(doc3);
		
		ls1.setCrawlQueue(crawlQueueList1);		
		
		ls1.dumpCrawlQueue();

		/*ListState ls2 =new ListState("{001-003-003}", time4);
		ArrayList crawlQueueList2 = new ArrayList();
		ls2.setCrawlQueue(crawlQueueList2);*/
		
		
	}
}
