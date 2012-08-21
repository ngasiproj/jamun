/*
 Copyright (c) 2011-2012, the Shaft project (http://www.shaftserver.org). All rights reserved.
Apache Software License 2.0
Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

  1. Redistributions of source code must retain the above copyright notice,
     this list of conditions and the following disclaimer.

  2. Redistributions in binary form must reproduce the above copyright
     notice, this list of conditions and the following disclaimer in
     the documentation and/or other materials provided with the distribution.

  3. The names of the authors may not be used to endorse or promote products
     derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL WebAppShowCase
OR ANY CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

Version 0.3


*/
package org.shaft.server.objs.db;
import org.shaft.server.auth.ShaftAuthException;
import org.shaft.server.objs.*;
import org.shaft.server.utils.*;

import java.io.*;
import java.util.*;
import tools.util.*;
//import org.json.simple.parser.*;
//import org.json.simple.*;


public class DBServerObjsParser extends ServerObjsParser{
		static Vector<String> datatypes = null;
		static String modelName = "model";
		static String controlName = "control";

	static{
				datatypes = SharedMethods.toVector(ShaftRestConfig.dataTypes,",");

	}
	public Vector<DBField> attributes = new Vector<DBField>();
	public String shaftid = ShaftRestConfig.shaftid;
	public Vector<DBAccess> access = new Vector<DBAccess>();
	public String shaftowner = ShaftRestConfig.shaftowner;
	public String shafttimestamp = ShaftRestConfig.shafttimestamp;
	public String shaftrealm = ShaftRestConfig.shaftrealm;
	static Hashtable<String,Hashtable<String,Vector>> cache = new Hashtable<String,Hashtable<String,Vector>>();

	public DBField getDBField(String name){
		DBField dbf = null;
		for (int ct = 0; ct < attributes.size();ct++){
			dbf = attributes.elementAt(ct);
			if (dbf.name.equals(name))
				return dbf;
		}
		return null;
	}

	public DBAccess getAccess(String ac){
		DBAccess dbf = null;
		for (int ct = 0; ct < access.size();ct++){
			dbf = access.elementAt(ct);
			if (dbf.getAction().equals(ac))
				return dbf;
		}
		return null;
	}

	public void parse(String app,String path,String type)throws Exception{
		if(ShaftRestConfig.getAppConfig(app).getString("deployment.mode").equals(ShaftRestConfig.production))
		{
			Hashtable<String,Vector> co = cache.get(app + "_" + path + "-" + type);
			if (co != null){
				access = co.get("access");
				attributes = co.get("attributes");
				System.out.println(" SOB FROM CACHE " + path);
				return;
			}
		}
		String 	pf = ShaftRestConfig.shaftroot + ShaftRestConfig.shaftapps + File.separator + app +  File.separator + "server" + File.separator + path + "." + type;





						FileInputStream fi = new FileInputStream(pf);
			try{

			InputStreamReader inr = new InputStreamReader(fi);

			LineNumberReader lr = new LineNumberReader(inr);
           String s = null;
           int i = 0;
           boolean attrS = false;
           boolean attrC = false;
           boolean attrOP = false;
           boolean attrCP = false;

           boolean accessS = false;
           boolean accessC = false;
           boolean accessOP = false;
           boolean accessCP = false;

           DBAccess dba = null;

           DBField dbf = null;
           while ((s = lr.readLine()) != null)
	        {
		        if(s != null )
		        {
		        	s = s.trim();

					//begin access
		        	if (!accessC){

		        	if (s.startsWith(controlName) && !accessOP)
		        	{
						accessS = true;
						if (s.endsWith("{"))
							accessOP = true;
						continue;
		        	}
		        	if (!accessOP && accessS){
							if (s.equals("{"))
							accessOP = true;

		        	}

		        	if (accessOP && accessS){
							if (s.equals("}"))
							accessC = true;
							else{
								dba = new DBAccess(app);
								dba.setServerObjs(getServerObjs());
								int idf = s.indexOf(":");
								if (idf > 0){

									dba.setAction (s.substring(0,idf).trim());
									dba.setRoles(s.substring(idf + 1,s.length()).trim());

								}
								else
									throw new ShaftRestException("role_required_for_action:" + s );
								access.add(dba);

							}

		        	}

		        	}
		        	//end access




		        	if (!attrC){

		        	if (s.startsWith(modelName) && !attrOP)
		        	{
						attrS = true;
						if (s.endsWith("{"))
							attrOP = true;
						continue;
		        	}
		        	if (!attrOP && attrS){
							if (s.equals("{"))
							attrOP = true;

		        	}

		        	if (attrOP && attrS){
							if (s.equals("}"))
							attrC = true;
							else{
								dbf = new DBField();
								dbf.dateformat = getServerObjs().getDateFormat();
								int idf = s.indexOf(":");
								if (idf > 0){

									dbf.setName(s.substring(0,idf).trim());
									dbf.setDataType(s.substring(idf + 1,s.length()).trim());
									if (ShaftRestConfig.forceNamesToLowerCase)
										dbf.dataType = dbf.dataType.toLowerCase();
									if (!datatypes.contains(dbf.dataType))
										throw new ShaftRestException("invalid_datatype:" + dbf.dataType );
										if (dbf.dataType.equals(ShaftRestConfig.shaftid))
											shaftid = dbf.name;
										else if (dbf.dataType.equals(ShaftRestConfig.shaftowner))
											shaftowner = dbf.name;
										else if (dbf.dataType.equals(ShaftRestConfig.shaftrealm))
											shaftrealm = dbf.name;

										else if (dbf.dataType.equals(ShaftRestConfig.shafttimestamp))
											shafttimestamp = dbf.name;

								}
								else
								dbf.setName(s);
								
								if (!ShaftServerUtil.validName(dbf.name))
									new ShaftAuthException("invalid_name").throwIt();
								if (!dbf.dataType.equals(ShaftRestConfig.shaftid) && !dbf.dataType.equals(ShaftRestConfig.shaftrealm)&& !dbf.dataType.equals(ShaftRestConfig.shaftowner)&& !dbf.dataType.equals(ShaftRestConfig.shafttimestamp))
									attributes.add(dbf);
							}

		        	}

		        	}
		        }
	        }
	        if (!attrC)
	        	throw new ShaftRestException("invalid_db_object_syntax");
	 	        if (attributes.size() < 1)
	        	throw new ShaftRestException("db_object_must_have_atleast_1_field_attribute");

			if (accessS){
	        if (!accessC)
	        	throw new ShaftRestException("invalid_db_access_syntax");
			 	        if (access.size() < 1)
	        	throw new ShaftRestException("db_access_must_have_atleast_1_access_rule");

			}
			
			if(ShaftRestConfig.getAppConfig(app).getString("deployment.mode").equals(ShaftRestConfig.production))
			{
				Hashtable<String,Vector> co = new Hashtable<String,Vector>();
					cache.put(app + "_" + path + "-" + type,co);
				
					co.put("access",access);
					co.put("attributes",attributes);
				
			}
			
	        			}finally{
				fi.close();
			}






	}
}