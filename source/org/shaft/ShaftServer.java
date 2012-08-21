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

package org.shaft;


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
import org.shaft.utils.*;
import org.shaft.compiler.*;
import org.shaft.server.utils.*;
import tools.io.*;
import tools.util.FileUtil;


public class ShaftServer
{
	public ShaftServer(String r){
		ShaftConfig.shaftroot = r;
	}

	public Vector<String> listApps(){
				String ipl[] = new File(ShaftConfig.shaftroot + ShaftConfig.shaftapps).list();

		Vector shaftapps = new Vector();
		if (ipl != null){
			for (int ct = 0; ct < ipl.length;ct++){
					if (new File(ShaftConfig.shaftroot + ShaftConfig.shaftapps + "/" + ipl[ct]).isDirectory() && !ShaftRestConfig.shaftrest.equals("/" + ipl[ct] + "/") && !ipl[ct].equalsIgnoreCase("WEB-INF"))
					shaftapps.add(ipl[ct]);
			}
		}
		return shaftapps;
	}


	public String getRequestRoute(String path,String vhost,HttpServletRequest request)throws ShaftException{
		String app = VHostMgr.getApp(vhost);
		if (app == null){
			int i = path.indexOf("/");
			if (i > 0){
				app = path.substring(0,i);
				path = path.substring(i + 1,path.length());
			}
			}
			if (app == null)
				return null;

			if (!listApps().contains(app))
				return null;


			String route = "/" + ShaftConfig.shaftapps + "/" + app + "/public/"  + path;
			//if (!new File(ShaftConfig.shaftroot +  "/" + ShaftConfig.shaftapps + "/" + app + "/public/"  ).exists())
				//return  "/" + ShaftConfig.shaftapps + "/" + app + "/"  + path;
			request.setAttribute("shaftapp",app);
			
			return route;
	}

	Hashtable<String,Boolean> compiling = new Hashtable<String,Boolean>();
	void checkDart(String app,String route)throws ShaftException{
		if (!route.endsWith(ShaftConfig.dartextention) && !route.endsWith(ShaftConfig.dartjsextention))
			return;
				if (ShaftConfig.getAppConfig(app).getString("mode").equals("no_compile"))
				return;

			String js = ShaftConfig.shaftroot + route;
		File jsf = new File(js);



		if (jsf.exists())
		{
			if (ShaftConfig.getAppConfig(app).getString("mode").equals("production"))
				return;
		}

				if (route.endsWith(ShaftConfig.dartjsextention)){
			String src = js.substring(0,js.length() - ShaftConfig.dartjsextention.length());
			src = src + ".dart";
			File srcf = new File(src);
			//(js + " JS SRC " + src + ":" + srcf.exists() + ":" + (srcf.lastModified() > jsf.lastModified()));

			if (! srcf.exists())
				return;

				if (!(srcf.lastModified() > jsf.lastModified())){
							if (!ShaftConfig.isDefaultConfigModified() && (new File(ShaftConfig.getDefaultConfigFile()).lastModified() <=  jsf.lastModified()) && (new File(ShaftConfig.getAppConfigFile(app)).lastModified() <=  jsf.lastModified()))
			{
					return;
			}
				}

		if (compiling.get(app) != null)
		{
			for (int ct = 0; ct < ShaftConfig.compilewait;ct++)
				try{

				Thread.sleep(1000);
				if (compiling.get(app) == null)
					break;
				}catch (Exception e)
				{
					e.printStackTrace();
				}
		}
		else{
		compiling.put(app,(true));
			try{
			//(js + " JS SRC COMPILE " + src + ":" + srcf.exists() + ":" + (srcf.lastModified() > jsf.lastModified()));

		DartCompiler.getCompiler(app).compile(app,FileUtil.pathToOS(src),FileUtil.pathToOS(js),true);

			}finally{
				compiling.remove(app);
			}
		}

		return;
		}

		String db = ShaftConfig.shaftroot + route.substring(0,route.length() - ShaftConfig.dartextention.length()  );
		String html = db + ShaftConfig.htmlextention;
		File htmlf = new File(html);
		DartCompiler dc = DartCompiler.getCompiler(app);
		String ds = dc.getDartSource(app, js, html);
		if (jsf.lastModified() >= new File(ds).lastModified())
		{

		if (jsf.lastModified() >= htmlf.lastModified())
		{
			if (!ShaftConfig.isDefaultConfigModified() && (new File(ShaftConfig.getDefaultConfigFile()).lastModified() <=  jsf.lastModified()) && (new File(ShaftConfig.getAppConfigFile(app)).lastModified() <=  jsf.lastModified()))
			{
				String fn = ShaftConfig.shaftroot +  "/" + ShaftConfig.shaftapps + "/" + app + "/public/";
				File lst = new File(fn  );
							String[] names = lst.list(new ExtensionFileNameFilter(".dart"));
				boolean isnwer = false;
				if (names != null)
			for (int ct = 0;ct < names.length;ct++)
			{
				if (new File(fn + names[ct]).lastModified() > jsf.lastModified()){
					//( " isnwer " + names[ct] );
					isnwer = true;
					break;
				}
			}
				if (!isnwer)
				return;
			}
		}
		}

		if (compiling.get(app) != null)
		{
			for (int ct = 0; ct < ShaftConfig.compilewait;ct++)
				try{

				Thread.sleep(1000);
				if (compiling.get(app) == null)
					break;
				}catch (Exception e)
				{
					e.printStackTrace();
				}
		}
		else{
		compiling.put(app,(true));
			try{

		dc.compile(app,FileUtil.pathToOS(js),FileUtil.pathToOS(html),false);

			}finally{
				compiling.remove(app);
			}

		}

	}

}

