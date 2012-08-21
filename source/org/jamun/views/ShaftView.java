/*
 Copyright (c) 2011-2012, the Jamun project (http://www.jamunapp.org). All rights reserved.
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

Version 0.1


*/

package org.jamun.views;
import java.util.*;

import tools.util.*;

import java.io.*;

import javax.servlet.http.HttpServletRequest;

import org.jamun.utils.JamunConfig;
import org.jamun.utils.JamunUtil;
import org.shaft.server.ShaftRestServer;
import org.shaft.utils.ShaftConfig;
import org.json.simple.parser.*;
import org.json.simple.*;


public class ShaftView implements ViewInterf
     {

	public String getRequestRoute(HttpServletRequest request) throws Exception{
		String app = (String)request.getAttribute("shaftapp");
		if (app == null)return null;
		String route = "/" + ShaftConfig.shaftapps + "/" + app + "/public/jamun.html" ;
	
		if (!new File(ShaftConfig.shaftroot +  route ).exists())
		{
			Hashtable<String,String> sobs = ShaftRestServer.getShaftRestServer().getServerObjs(app);
			if (sobs.size() < 1)return null;
	
			String src = JamunConfig.jamunroot + JamunConfig.jamunconfigd + "views/shaft/templates/" + JamunConfig.getDefaultConfig().getString("view.engine.template") + "/";
			String dest = ShaftConfig.shaftroot +   "/" + ShaftConfig.shaftapps + "/" + app + "/public/";
			FileUtil.deleteAll(dest);
			new File(dest).mkdirs();
			FileUtil.copyFilesInDir(src + "public/", dest);
			Enumeration<String> en = sobs.keys();
			String k = null, v = null;
			HashMap m = new HashMap();
			m.put("shaftusermgr", true);
			while (en.hasMoreElements())
			{
				k = en.nextElement();
				v = sobs.get(k);
				//(k + " RENDERING SERVER OBJS " + v);
				if (v.equals("db")){
					FileUtil.copy(src + "view.db", dest + "views/" + k + ".html");
					m.put(k, true);
					FileUtil.writeToFile("{}", dest + "properties/" + k + ".json");
				}
			}
	
			String rts = FileUtil.getStringFromFile(dest + "properties/jamun.json");
			  JSONParser parser=new JSONParser();
			  JSONObject obj= (JSONObject)parser.parse(rts);
			  obj.put("deployment.mode", JamunConfig.getDefaultConfig().getString("deployment.mode"));
			  obj.put("pageList",m);
			  
			  FileUtil.writeToFile(obj.toString(), dest + "properties/jamun.json");
			//		"deployment.mode":"development",
	//		"pageList":{"testpages":false,"shaftusermgr":true,"pathprivileges":true,"publicpaths":["member","enduser"]}
			
		}
		
		return route;
		
	}



}
