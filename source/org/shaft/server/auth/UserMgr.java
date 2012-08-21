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

package org.shaft.server.auth;


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
import java.util.HashMap;
import org.shaft.server.utils.*;
import tools.util.*;

public abstract class UserMgr
{

	NameValuePairs appmaxage = new NameValuePairs();

	public long getMaxAge(String app)throws Exception{
		long maxage = appmaxage.getLong(app);
		if (maxage < 0){
			maxage = ShaftRestConfig.getAppConfig(app).getLong("shaftsession.maxage");
			if (maxage < 0)maxage = 0;
			maxage = maxage * 60 * 1000L;
			appmaxage.put(app,maxage);
		}
		return maxage;
	}

	public String auth(String app,HttpServletRequest request,Vector<String> roles,String realm)throws Exception{
		String u = getUser(app,request);
		if ((roles == null || roles.size() < 1 || realm == null ) && u != null)
		{
			if (roles != null && roles.size() > 0)
			{
			RealmMgr rm = RealmMgr.getRealmMgr(app);

				if (!rm.isMember(app,u,roles))
			new ShaftAuthException("user_not_in_roles").throwIt();

			}
			return u;
		}
		if (u == null && roles.contains(ShaftRestConfig.guestRole))
			return ShaftRestConfig.guestUser;
		if (u == null)
			new ShaftAuthException("user_not_in_roles").throwIt();

		RealmMgr rm = RealmMgr.getRealmMgr(app);

		if (rm.isAdmin(app,u))
			return u;

		if (realm == null)
			realm = rm.getDefaultRealm(app,getOwner(app,u));
		if (rm.isCreator(app,u,realm))
			return u;

		//Vector<String> ur = rm.getRealms(app,u);
		//if (!ur.contains(realm))
		//			new ShaftAuthException("user_not_in_realm").throwIt();

		if (roles.contains(ShaftRestConfig.memberRole) && rm.isMember(app,u,realm))
			return u;

		if (roles.contains(ShaftRestConfig.allRole))
			return u;


		new ShaftAuthException("user_not_in_roles").throwIt();
		return null;
		}


/*public boolean isAdmin(String app,HttpServletRequest request)throws Exception{
	return isAdmin(app,getUser(app,request));
}
public boolean isAdmin(String app,String u)throws Exception{
		Vector<String> ur = getRoles(app,u);
		return ur.contains(getAdminRole(app));
}*/

	public String getUser(String app,HttpServletRequest request)throws Exception{
		return getUser(app,request,false);
	}

	public String getUser(String app,HttpServletRequest request,boolean silent)throws Exception{
		String u = request.getParameter(ShaftRestConfig.userobjname);
		if (u != null)
		{
					String p = request.getParameter(ShaftRestConfig.userobjpassword);
					if (p != null)
						if (login(app,u,p) != null)return u;
		}
		String s = request.getParameter(ShaftRestConfig.userobjsessionid);
		if (s != null){
			u = getUserFromSessionId(app,s);
			if (u != null)return u;
		}
		if (!silent)
		new ShaftAuthException("unable_to_authenticate").throwIt();
		return null;
		}

			public String login(String app,HttpServletRequest request)throws Exception{
		String u = request.getParameter(ShaftRestConfig.userobjname);
		if (u != null)
		{
					String p = request.getParameter(ShaftRestConfig.userobjpassword);
					if (p != null){
						String s = login(app,u,p);
					if (s != null)
						return s;
					}
		}
		new ShaftAuthException("unable_to_authenticate").throwIt();
		return null;
		}

		public boolean add(String app,HttpServletRequest request)throws Exception{
		String u = request.getParameter(ShaftRestConfig.userobjname + ShaftRestConfig.newExt);
		if (StringUtil.isRealString(u))
		{
			if (exists(app,u))
		new ShaftAuthException("unable_to_create_user").throwIt();

					String p = request.getParameter(ShaftRestConfig.userobjpassword + ShaftRestConfig.newExt);
					if (StringUtil.isRealString(p)){
						String logintype = request.getParameter(ShaftRestConfig.logintype );
						if (!StringUtil.isRealString(logintype))
							logintype = getDefaultUserType(app);
						String digest = request.getParameter(ShaftRestConfig.digest );
						if (!StringUtil.isRealString(digest))
							digest = getDefaultDigest(app);
		String o = getUser(app,request,true);
		addUser(app,u,p,digest,logintype,o);

				RealmMgr.getRealmMgr(app).addUserDefaultRealm(app,u,o);



					return true;
					}
		}
		new ShaftAuthException("unable_to_create_user").throwIt();
		return false;
		}

		public boolean isAdminOrOwnerOf(String app,String ao,String u)throws Exception
		{
			if (ao == null)return false;
				if (RealmMgr.getRealmMgr(app).isAdmin(app,ao))
					return true;

			String username = u;
			while (true){
			username = getOwner(app,username);
			if (username == null)
				break;
			if (username.equals(ao))
				return true;
			if (RealmMgr.getRealmMgr(app).isAdmin(app,username))
				return true;
			}
			return false;
}

		public boolean delete(String app,HttpServletRequest request)throws Exception{
	//	String u = request.getParameter(ShaftRestConfig.userobjname + ShaftRestConfig.newExt);
	//	if (u != null)
	//	{
	//		if (exists(app,u))
	//	new ShaftAuthException("unable_to_create_user").throwIt();

					String p = request.getParameter(ShaftRestConfig.userobjname + ".eq");
					if (p == null){
						HashMap bp = (HashMap)request.getAttribute("batch.params");
						if (bp != null)
							p = (String)bp.get(ShaftRestConfig.userobjname + ".eq");
						
					}
					if (p != null){
		String o = getUser(app,request,true);
		if (!isAdminOrOwnerOf(app,o,p))
		new ShaftAuthException("unable_to_delete_user").throwIt();
		

		deleteUser(app,p);

		Vector<String> ol = list(app,p);
		for (int ct = 0; ct < ol.size();ct++){
			deleteUser(app,ol.elementAt(ct));
			
		}
		
		//		RealmMgr.getRealmMgr(app).addUserDefaultRealm(app,u,o);



					return true;
					}
	//	}
		new ShaftAuthException("unable_to_update_user").throwIt();
		return false;
		}

		
		public Vector<String> list(String app,HttpServletRequest request)throws Exception{

				String o = getUser(app,request);
				return list(app,o);

				}
		

		public boolean update(String app,HttpServletRequest request)throws Exception{
	//	String u = request.getParameter(ShaftRestConfig.userobjname + ShaftRestConfig.newExt);
	//	if (u != null)
	//	{
	//		if (exists(app,u))
	//	new ShaftAuthException("unable_to_create_user").throwIt();

					String p = request.getParameter(ShaftRestConfig.userobjpassword + ShaftRestConfig.newExt);
					if (p != null){
		String o = getUser(app,request,true);
		String u = request.getParameter(ShaftRestConfig.userobjname + ".eq");
		if (u != null){
			if (!isAdminOrOwnerOf(app,o,u))
				new ShaftAuthException("unable_to_update_user").throwIt();
			o = u;
			
		}
		updatePasswd(app,o,p);

		//		RealmMgr.getRealmMgr(app).addUserDefaultRealm(app,u,o);



					return true;
					}
	//	}
		new ShaftAuthException("unable_to_update_user").throwIt();
		return false;
		}

public  String login(String app,String user,String pass)throws Exception{
	return login(app,user,pass,true);
}

		public abstract String login(String app,String user,String pass,boolean cs)throws Exception;
		public abstract String getUserFromSessionId(String app,String s)throws Exception;
		public abstract void addSessionId(String app,String user,String sid)throws Exception;
		public abstract void deleteSessionId(String app,String user)throws Exception;

		//public abstract void addRole(String app,String user,String role)throws Exception;
		//public abstract void deleteRole(String app,String user,String role)throws Exception;
		public abstract void addUser(String app,String user,String passwd,String digest,String utype,String owner)throws Exception;
		public abstract void deleteUser(String app,String user)throws Exception;
		public abstract void updatePasswd(String app,String user,String passwd)throws Exception;
		public abstract boolean exists(String app,String user)throws Exception;
		public	abstract String getPasswd(String app,String user)throws Exception;
		public	abstract String getOwner(String app,String user)throws Exception;
		public	abstract String getMD5Digest(String app,String user)throws Exception;
		public	abstract Vector<String> list(String app,String owner)throws Exception;
//	public abstract Vector<String> getRoles(String app,String user)throws Exception;
public abstract Vector<String> listIDs(String app)throws Exception;

public String createSessionId(String app,String user)throws Exception{
		synchronized(this){

			deleteSessionId(app,user);
		String oid = null;
		for (int cta = 0;cta < ShaftRestConfig.maxAddRowAttempts;cta++)
		try{


				oid = generateID(user,app);
			addSessionId(app,user,oid);
			return oid;

		}catch (Exception e){
			e.printStackTrace();
		}
		}
		new ShaftAuthException("unable_to_add_user_session").throwIt();
		return null;
}

		public String getDefaultDigest(String app)throws Exception{
				String digest = ShaftRestConfig.getAppConfig(app).getString("defaultdigest");
				return digest;
		}

		/*public String getAdminRole(String app)throws Exception{
				String shaftadminrole = ShaftRestConfig.getAppConfig(app).getString("shaftadminrole");
				return shaftadminrole;
		}*/

		/*public String getDefaultRole(String app)throws Exception{
				String defaultrole = ShaftRestConfig.getAppConfig(app).getString("defaultrole");
				return defaultrole;
		}*/

			public String getDefaultUserType(String app)throws Exception{
				String defaultusertype = ShaftRestConfig.getAppConfig(app).getString("defaultusertype");
				return defaultusertype;
		}


public void addUser(String app,String user,String passwd,String owner)throws Exception{
		addUser(app,user,passwd,getDefaultDigest(app),getDefaultUserType(app),owner);
}


			protected String  generateID(String user,String app)throws Exception{
Vector<String> suv = listIDs(app);
String tid = AlphaNumeric.generateRandomAlphNumeric(ShaftRestConfig.defaultRowIDLength);
String dig = ShaftRestConfig.defaultSessionDigest;
if (dig != null)
tid = Crypto.digest(tid,dig);
tid = user + "_" + tid;
while (true){
if (!suv.contains(tid) )
{
	break;
}
tid = AlphaNumeric.generateRandomAlphNumeric(ShaftRestConfig.defaultRowIDLength);
if (dig != null)
tid = Crypto.digest(tid,dig);
tid = user + "_" + tid;
}
return tid;
	}


	static NameValuePairs initapps = new NameValuePairs();
	public static UserMgr getUserMgr(String app){
		UserMgr umr = new GenericUserMgr();
		if (!initapps.getBoolean(app))
		{
			try{
				String au = ShaftRestConfig.getAppConfig(app).getString("shaftadmin");
				if (!umr.exists(app,au))
				{
				String ap = ShaftRestConfig.getAppConfig(app).getString("shaftadminpassword");
				umr.addUser(app,au,ap,null);
				//umr.addRole(app,au,umr.getAdminRole(app));

				}
				initapps.put(app,true);
			}catch (Exception e){
			e.printStackTrace();
			}
		}
		return umr;
	}

}

