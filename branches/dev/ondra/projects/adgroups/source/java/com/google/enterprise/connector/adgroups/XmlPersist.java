// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.enterprise.connector.adgroups;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;

public class XmlPersist {
  public HashMap<String, LdapEntity> entities;

  public void save(File fileName) {
  	try {
  	FileWriter fw = new FileWriter(fileName);
	BufferedWriter bw = new BufferedWriter(fw);
	  for(LdapEntity e: entities.values())
	  {
		  bw.write(e.dn + "\t");

		  for (LdapEntity m : e.memberOf)
			  bw.write(m.dn+"|");
		  bw.write("\n");
	  }
	  bw.close();
	} catch (IOException ioe) {
		System.out.println(ioe);
	}
  }
}
