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
package org.shaft.server.objs;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

import org.shaft.server.ShaftRestServer;
import org.shaft.server.utils.*;
import org.shaft.server.objs.*;
import tools.util.*;
import org.shaft.server.auth.*;
import org.json.simple.parser.*;
import org.json.simple.*;

public class BatchMgrServerObjs extends ServerObjsInterf{
	BatchMgrServerObjsParser dbp = new BatchMgrServerObjsParser();

	public static Vector<String> actions = new Vector<String>();

	static {

		actions.add("process");

	}

		public  Vector<String> supportedActions()throws Exception{
			return actions;
		}

		public BatchMgrServerObjsParser getServerObjsParser()throws Exception{
			return dbp;
		}

	public Object doProcess(String app,String path,String action, HttpServletRequest request)throws Exception{
		//if (action.equals("add") )
		//return RealmMgr.getRealmMgr(app).add(app,request);
		//if (action.equals("list") )
		//	return RealmMgr.getRealmMgr(app).getRealms(app,request);
		if (action.equals("process") ){
			List res = new ArrayList();
		byte[] rt = StreamUtil.readStream(request.getInputStream(), request.getContentLength());
		String rts = new String(rt);
		  JSONParser parser=new JSONParser();
		  Object obj=parser.parse(rts);
		  JSONArray obj2=(JSONArray)obj;
		  ShaftRestServer srs = ShaftRestServer.getShaftRestServer();
		  Hashtable<String,String> som = srs.getServerObjs(app);
	        if (obj2.size() > ShaftRestConfig.getDefaultConfig().getInt("max_batch_size"))
	        	throw new ShaftRestException("exceeded_max_batch_size");
		  
		  DBConnectionMgr.startBatch();
		  try{
		  for (int ct = 0; ct < obj2.size();ct++)
		  {
			  JSONObject jo = (JSONObject)obj2.get(ct);
			  
			  String mpath = (String)jo.get("serverobj");
			  String maction = (String)jo.get("action");
			  String sot = som.get(mpath);
			  ServerObjsInterf sob = ServerObjFactory.create(sot);
				if (!sob.supportedActions().contains(maction))
					throw new ShaftRestException("unsupported_action:" + maction);
		//			String app = (String)request.getAttribute("shaftapp");
		//			setApp(app);
		//		String path = (String)request.getAttribute("shaftobj");

					ServerObjsParser sop = sob.getServerObjsParser();
					sop.setServerObjs(sob);
					sop.parse(app,mpath,sot);
					//JSONObject jbp = (JSONObject)jo.get("params");
					//Hashtable bp = new Hashtable();
					
					
					request.setAttribute("batch.params", jo.get("params"));
					sob.checkAccess(app,maction,sop,request);

					res.add(sob.doProcess(app,mpath,maction,request));
			  
			  
		  }
		  DBConnectionMgr.release(false);
		  }catch (Exception e){
			  DBConnectionMgr.release(true);
			  throw e;
		  }
		
		
		return res;
		}

		return null;
	}








}
