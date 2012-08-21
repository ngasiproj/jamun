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
package org.shaft.server.objs.usermgr;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import org.shaft.server.utils.*;
import org.shaft.server.objs.*;
import tools.util.*;
import org.shaft.server.auth.*;

public class UserMgrServerObjs extends ServerObjsInterf{
	UserMgrServerObjsParser dbp = new UserMgrServerObjsParser();

	public static Vector<String> actions = new Vector<String>();

	static {
		actions.add("login");
		actions.add("add");
		actions.add("delete");
		actions.add("update");
		actions.add("list");
	}

		public  Vector<String> supportedActions()throws Exception{
			return actions;
		}

		public UserMgrServerObjsParser getServerObjsParser()throws Exception{
			return dbp;
		}

	public Object doProcess(String app,String path,String action, HttpServletRequest request)throws Exception{
		if (action.equals("login") )
		return login(app,request);
		else if (action.equals("add") )
		return UserMgr.getUserMgr(app).add(app,request);
		else if (action.equals("update") )
		return UserMgr.getUserMgr(app).update(app,request);
		else if (action.equals("delete") )
		return UserMgr.getUserMgr(app).delete(app,request);
		else if (action.equals("list") ){
			Vector<String> usrs = UserMgr.getUserMgr(app).list(app,request);
			//usrs.insertElementAt(ShaftRestConfig.userobjname, 0);
			return usrs;
			}


		return null;
	}

	public  Object login(String app, HttpServletRequest request)throws Exception{
		String sess = UserMgr.getUserMgr(app).login(app,request);
		Hashtable h = new Hashtable();
		h.put("session", sess);
		if (!ShaftRestConfig.getAppConfig(app).getBoolean("no_disclose_roles")){
			h.put("roles", RealmMgr.getRealmMgr(app).getRealms(app,UserMgr.getUserMgr(app).getUserFromSessionId(app,sess)));
		}
		return h;
	}






}