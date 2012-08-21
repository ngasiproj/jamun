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

package org.shaft.server;


import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletContext;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import java.io.PrintWriter;
import java.util.Vector;
import java.io.File;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Hashtable;

import org.shaft.server.auth.ShaftAuthException;
import org.shaft.server.utils.*;

import tools.util.*;
import org.shaft.utils.*;


public class ShaftRestServer
{
	static ShaftRestServer srs = null;
	
	public static ShaftRestServer getShaftRestServer(){
		return srs;
	}
	public ShaftRestServer(String r){
		ShaftRestConfig.shaftroot = r;

					try{
						if (ShaftRestConfig.getDefaultConfig().getString("db.source").equals("datasource"))
						{
									if (new File(ShaftConfig.shaftroot + ShaftConfig.shaftconfigd + "load_db.sql").exists())
									{
						SPQueryAndUpdate spq = new SPQueryAndUpdate(DBConMgr.getConnection(ShaftRestConfig.datasourceName));
							try{

						spq.executeScript(ShaftConfig.shaftroot + ShaftConfig.shaftconfigd + "load_db.sql",true,false);
							}finally{
								if (spq != null)spq.close();
							}
									}
						}

			}
			catch (Exception e){
				tools.util.LogMgr.err("load_db.sql err " + e.toString());
			}
			srs = this;

	}

	public Vector<String> listApps(){
				String ipl[] = new File(ShaftRestConfig.shaftroot + ShaftRestConfig.shaftapps).list();

		Vector shaftapps = new Vector();
		if (ipl != null){
			for (int ct = 0; ct < ipl.length;ct++){
					if (new File(ShaftRestConfig.shaftroot + ShaftRestConfig.shaftapps + "/" + ipl[ct]).isDirectory() && !ShaftRestConfig.shaftrest.equals("/" + ipl[ct] + "/") && !ipl[ct].equalsIgnoreCase("WEB-INF"))
					shaftapps.add(ipl[ct]);
			}
		}
		return shaftapps;
	}

		public Hashtable<String,String> getServerObjs(String app){
				String ipl[] = new File(ShaftRestConfig.shaftroot + ShaftRestConfig.shaftapps + File.separator + app +  File.separator + "server").list();

		Hashtable<String,String> sobs = new Hashtable<String,String>();

		if (ipl != null){
			//int i = 0;
			for (int ct = 0; ct < ipl.length;ct++){
				int i = ipl[ct].indexOf(".");
				String ext = ipl[ct].substring(i + 1,ipl[ct].length());
				String son = ipl[ct].substring(0,ipl[ct].length() - (ext.length() + 1));
				try{
				if (ShaftServerUtil.validName(son))
					//new ShaftAuthException("invalid_name").throwIt();
					sobs.put(son,ipl[ct].substring(ipl[ct].length() - ext.length(),ipl[ct].length()));
				}catch (Exception ve){
					ve.printStackTrace();
				}
				}
		}
		sobs.put(ShaftRestConfig.shaftusermgrpath,ShaftRestConfig.shaftusermgrtype);
		sobs.put(ShaftRestConfig.shaftrealmmgrpath,ShaftRestConfig.shaftrealmmgrtype);
		sobs.put(ShaftRestConfig.batchmgrpath,ShaftRestConfig.shaftbatchmgrtype);
		return sobs;
	}

	public String getRequestRoute(String path,String vhost,HttpServletRequest request){
	//	if (!("/" + path).startsWith(ShaftRestConfig.shaftrest))
	//		return null;
		//(ShaftRestConfig.shaftrest + " CHECK REST PATH 1 " + path);
	if (( path).indexOf(ShaftRestConfig.shaftrest) < 0)
			return null;
	path = StringUtil.replaceSubstring(path,ShaftRestConfig.shaftrest,"/");
//	path = StringUtil.replaceSubstring(path,ShaftRestConfig.shaftrest + "/","");
//	path = StringUtil.replaceSubstring(path,ShaftRestConfig.shaftrest,"");

		//path = path.substring(ShaftRestConfig.shaftrest.length() - 1,path.length());
		//("REST PATH 1 " + path);
	//("CHECK REST PATH 2 " + path);

		String app = org.shaft.VHostMgr.getApp(vhost);
		if (app == null){
			int i = path.indexOf("/");
			if (i > 0){
				app = path.substring(0,i);
				path = path.substring(i + 1,path.length());
			}
			}
	//	null CHECK REST PATH 2 /default/testpages/list

		//(app + " CHECK REST PATH 2 " + path);

			if (app == null)
				return null;
			//("CHECK REST PATH 3 " + listApps());

			if (!listApps().contains(app))
				return null;
			int i = path.indexOf("/");
			String action = null;
			if (i > 0){
				action = path.substring(i + 1,path.length());
				path = path.substring(0,i);

			}

			//(path + " CHECK REST PATH 4 " + action);


			String sos = getServerObjs(app).get(path);
	//(path + " CHECK REST PATH 5 " + sos);


			if (sos == null)return null;
			request.setAttribute("shaftapp",app);
			request.setAttribute("shaftobj",path);
			request.setAttribute("shaftaction",action);
			request.setAttribute("shaftobjtype",sos);

//("HELLO 2 " + ShaftRestConfig.shaftrest);

		return ShaftRestConfig.shaftrest;
	}

}

