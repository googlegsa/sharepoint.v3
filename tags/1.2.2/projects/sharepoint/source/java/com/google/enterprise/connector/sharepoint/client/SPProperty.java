package com.google.enterprise.connector.sharepoint.client;

import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Value;

/**
 * @author amit_kagrawal
 * SharePoint implementation for {@link com.google.enterprise.connector.spi.Property}.
 * */
public class SPProperty  implements Property{

	private String name;
	private Value value;
	private boolean repeating=false;
	public SPProperty(String strName,Value inValue){
		this.name = strName;
		this.value = inValue;
	}
	
	public Value nextValue() throws RepositoryException {
		if(repeating){
			return null;
		}else{
			repeating=true;
			return value;
		}
	}
	
	public String getName(){
		return name;
	}

}
