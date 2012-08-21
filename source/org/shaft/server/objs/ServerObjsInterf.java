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
import org.shaft.server.utils.*;
import org.shaft.server.auth.*;

public abstract class ServerObjsInterf{
	public abstract Vector<String> supportedActions()throws Exception;
	String app = null;
	public String getApp(){
	return app;
	}
	public void  setApp(String a){
	app = a;
	}
	public Object process(String sot,HttpServletRequest request)throws Exception{
		String action = (String)request.getAttribute("shaftaction");
		if (!supportedActions().contains(action))
			throw new ShaftRestException("unsupported_action:" + action);
			String app = (String)request.getAttribute("shaftapp");
			setApp(app);
		String path = (String)request.getAttribute("shaftobj");

			ServerObjsParser sop = getServerObjsParser();
			sop.setServerObjs(this);
			sop.parse(app,path,sot);
			checkAccess(app,action,sop,request);

		return doProcess(app,path,action,request);
	}
	public String getDateFormat(HttpServletRequest request)throws Exception{
		String app = (String)request.getAttribute("shaftapp");
		return getDateFormat(app);
	}

	public String getDateFormat()throws Exception{
		return getDateFormat(getApp());
	}
	public String getDateFormat(String app)throws Exception{
		return ShaftRestConfig.getAppConfig(app).getString("dateformat");
	}

	public abstract Object doProcess(String app,String path,String action, HttpServletRequest request)throws Exception;

		public void checkAccess(String app,String action,ServerObjsParser sop, HttpServletRequest request)throws Exception{
			//DBServerObjsParser dop = (DBServerObjsParser)sop;
			ServerAccess dba = sop.getAccess(action);
			String realm = request.getParameter(ShaftRestConfig.realmobjname);
			if (realm != null)
				sop.setRealm(realm);
			if (dba == null)return;
			String ar = ShaftRestConfig.getDefaultConfig().getString("anonymous.role");
			//(dba.getRoles() + " INTERF8ACE " + ar + ":" + action + ":" + app);
			if (dba.getRoles() != null && dba.getRoles().contains(ar))
			{
				
				sop.setUser(ShaftRestConfig.getDefaultConfig().getString("anonymous.user"));
				sop.setRealm(ar);
				return;
				
			}
			String u = UserMgr.getUserMgr(app).auth(app,request,dba.getRoles(),realm);
			if (u == null)
			throw new ShaftRestException("no_access");

			sop.setUser(u);
			if (realm == null){
				Vector<String> rm = RealmMgr.getRealmMgr(app).getRealms(app,u);
				if (rm != null && rm.size() > 0)
				sop.setRealm(rm.elementAt(0));
			}
		}
	public abstract ServerObjsParser getServerObjsParser()throws Exception;



}