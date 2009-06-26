package com.google.enterprise.connector.sharepoint.client;

import java.io.Serializable;
/**
 * Attribute class.
 * @author amit_kagrawal
 */
public class Attribute implements Serializable
{
 private static final long serialVersionUID = 0x1L;
 private String name;
 private Object value;

 public Attribute(String s, Object obj)
 {
     if(s == null)
     {
         throw new IllegalArgumentException("Attribute name cannot be null ");
     }else{
         name = s;
         if(obj!=null){ //ignore null values
        	 value = obj;
         }
         return;
     }
 }

 public String getName()
 {
     return name;
 }

 public Object getValue()
 {
     return value;
 }

 public boolean equals(Object obj)
 {
     if(!(obj instanceof Attribute)){
         return false;
     }
     Attribute attribute = (Attribute)obj;
     if(value == null)
     {
         if(attribute.getValue() == null){
             return name.equals(attribute.getName());
         }else{
             return false;
         }
     } else{
         return name.equals(attribute.getName()) && value.equals(attribute.getValue());
     }
 }

/**
 * @return
 * @see java.lang.String#hashCode()
 */
public int hashCode() {
	return super.hashCode();
}

}
