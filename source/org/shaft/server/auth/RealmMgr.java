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
import org.shaft.server.utils.*;
import tools.util.*;

public abstract class RealmMgr
{

	NameValuePairs appmaxage = new NameValuePairs();

	public long getMaxInviteAge(String app)throws Exception{
		long maxage = appmaxage.getLong(app);
		if (maxage < 0){
			maxage = ShaftRestConfig.getAppConfig(app).getLong("user.realm.invite.maxage");
			if (maxage < 0)maxage = 0;
			maxage = maxage * 60 * 1000L;
			appmaxage.put(app,maxage);
		}
		return maxage;
	}

	public boolean auth(String app,String u,String realm)throws Exception{
			if (isMember(app,u,realm))
				return true;
		new ShaftAuthException("user_not_in_roles").throwIt();
		return false;
		}


	public boolean isMember(String app,String u,String realm)throws Exception{
		Vector<String> ur = getRealms(app,u);
		return (ur.contains(realm) || realm.equals(ShaftRestConfig.ownerRole));

		}


	public boolean isMember(String app,String u,Vector<String> ur)throws Exception{
		String realm = null;
		for (int ct = 0; ct < ur.size();ct++)
		{
			realm = ur.elementAt(ct);
			if (isMember(app,u,realm))
				return true;
		}
		return false;

		}


	public boolean isCreator(String app,String u,String realm)throws Exception{
		Vector<String> ur = listRealmsCreated(app,u);
		return ur.contains(realm);

		}

public boolean isAdmin(String app,String u)throws Exception{
		Vector<String> ur = getRealms(app,u);
		return ur.contains(getAdminRealm(app));
}



		public boolean add(String app,HttpServletRequest request)throws Exception{
		String realm = request.getParameter(ShaftRestConfig.realmobjname + ShaftRestConfig.newExt);
		if (realm != null)
		{
			if (!ShaftServerUtil.validName(realm))
						new ShaftAuthException("invalid_name").throwIt();
			if (!ShaftRestConfig.getAppConfig(app).getBoolean("allow_mix_case"))
				if (!realm.equals(realm.toLowerCase()))
					new ShaftAuthException("invalid_name").throwIt();

			if (exists(app,realm))
		new ShaftAuthException("unable_to_create_realm").throwIt();

	boolean caninvite = false;
	 String caninviteS = request.getParameter(ShaftRestConfig.caninvite);
	 if (caninviteS != null && caninviteS.equals("true"))
	 	caninvite = true;

	boolean secretmembers = false;
	 String secretmembersS = request.getParameter(ShaftRestConfig.secretmembers);
	 if (secretmembersS != null && secretmembersS.equals("true"))
	 	secretmembers = true;

	addRealm(app,UserMgr.getUserMgr(app).getUser(app,request),realm, caninvite,secretmembers);
return true;

					}
		return false;
		}


public void createRealm(String app,String user,String realm,boolean canInvite,boolean secret)throws Exception{
				if (!isAdmin(app,user))
				{
				if (!canCreate(app))
					new ShaftAuthException("create_realm_not_allowed").throwIt();
				if (maxCreate(app) <= listRealmsCreated(app,user).size())
					new ShaftAuthException("create_realm_max_exceeded").throwIt();

				if (maxMembership(app) <= getRealms(app,user).size())
					new ShaftAuthException("realm_membership_max_exceeded").throwIt();
				}

				if (exists(app,realm))
					new ShaftAuthException("realm_exists").throwIt();

			addRealm(app,user,realm,canInvite,secret);


}

		public int maxMembership(String app)throws Exception{
				return ShaftRestConfig.getAppConfig(app).getInt("max.user.realm.membership");
		}

		public int maxCreate(String app)throws Exception{
				return ShaftRestConfig.getAppConfig(app).getInt("max.user.realms");
		}

		public boolean canCreate(String app)throws Exception{
				return ShaftRestConfig.getAppConfig(app).getBoolean("user.can.create.realms");
		}


/*
 user.can.create.realms=true
defaultrealm.members.can.invite=true
defaultrealm.secret.realm.memberships=true
allow.secret.realm.memberships=true
max.user.realms=5
max.user.realm.invites=5
max.user.realm.members=25
max.user.realm.membership=25
#minutes
user.realm.invite.maxage=1440
*/

		public String getAdminRealm(String app)throws Exception{
				String shaftadminrole = ShaftRestConfig.getAppConfig(app).getString("shaftadminrealm");
				return shaftadminrole;
		}

		public String getDefaultRealm(String app,String o)throws Exception{
				if (o != null && !isAdmin(app,o)){
					Vector<String> v = getRealms( app,o);
					String dr = null;
					for (int ct = 0; ct < v.size();ct++)
					{
						dr = ShaftRestConfig.getAppConfig(app).getString("defaultrealm." + v.elementAt(ct));
						if (StringUtil.isRealString(dr))
							return dr;
					}
					new ShaftAuthException("create_realm_not_allowed").throwIt();

				}
					
				String defaultrole = ShaftRestConfig.getAppConfig(app).getString("defaultrealm");
				return defaultrole;
		}

			public String getDefaultUserType(String app)throws Exception{
				String defaultusertype = ShaftRestConfig.getAppConfig(app).getString("defaultusertype");
				return defaultusertype;
		}





	static NameValuePairs initapps = new NameValuePairs();
	public static RealmMgr getRealmMgr(String app){
		RealmMgr umr = new GenericRealmMgr();
		if (!initapps.getBoolean(app))
		{
			try{
				String au = ShaftRestConfig.getAppConfig(app).getString("shaftadmin");
				if (!umr.exists(app,umr.getAdminRealm(app)))
				{
				boolean sm = true;
				if (ShaftRestConfig.getAppConfig(app).getBoolean("no_shaftadmin.secret.memberships"))
					sm = false;
				umr.addRealm(app,au,umr.getAdminRealm(app),ShaftRestConfig.getAppConfig(app).getBoolean("shaftadmin.can.invite"),sm);
				umr.addUser(app,au,umr.getAdminRealm(app));

				}
				initapps.put(app,true);
			}catch (Exception e){
			e.printStackTrace();
			}
		}
		return umr;
	}

	public void addUserDefaultRealm(String app,String u,String o)throws Exception{
				String dr = getDefaultRealm(app,o);
				if (!exists(app,dr))
				{
				String au = ShaftRestConfig.getAppConfig(app).getString("shaftadmin");


				addRealm(app,au,dr,ShaftRestConfig.getAppConfig(app).getBoolean("defaultrealm.members.can.invite"),ShaftRestConfig.getAppConfig(app).getBoolean("defaultrealm.secret.realm.memberships"));

				}

				if (!isMember(app,u,dr))
				{
				addUser(app,u,dr);

				}

}


		public boolean exists(String app,String realm)throws Exception
	{
		Vector<String> ms = listIDs(app);
		return ms.contains(realm);
	}
	
		public  Vector<String> getRealms(String app,HttpServletRequest request)throws Exception{
		
		String user = UserMgr.getUserMgr(app).getUser(app,request);
		return getRealms(app,user);
		}

	public abstract void addRealm(String app,String u,String realm,boolean canInvite,boolean secret)throws Exception;
	public abstract boolean canInvite(String app,String realm)throws Exception;
	public abstract boolean secretMembership(String app,String realm)throws Exception;
	public abstract void addUser(String app,String u,String realm)throws Exception;
	public abstract Vector<String> getRealms(String app,String user)throws Exception;
	public abstract Vector<String> listRealmsCreated(String app,String user)throws Exception;
	public abstract Vector<String> listIDs(String app)throws Exception;
	public abstract Vector<String> listMembers(String app,String realm)throws Exception;
	public abstract void deleteRealms(String app,String user)throws Exception;

/*
	public abstract void invite(String app,String u,String invitee,String realm)throws Exception;
	public abstract void accept(String app,String u,String realm)throws Exception;
	public abstract void decline(String app,String u,String realm)throws Exception;
	public abstract void discontinue(String app,String u,String realm)throws Exception;
	public abstract void deleteUser(String app,String u,String realm)throws Exception;
	public abstract Vector<String> listInviteSent(String app,String u)throws Exception;
	public abstract Vector<String> listInviteReceived(String app,String u)throws Exception;
	public abstract Vector<String> listInvitees(String app,String u,String realm)throws Exception;
	*/

					/*
					 *
					 *
	invite
	accept
		decline
			discontinue
			deleteUser
				create - secret memberships
				listmemberships
				listinvites
				ismember

				listmemberships
				if (empty)
					create
				ismember
				if not
				listinvites
				if not invite.
				iscreator
		public abstract String login(String app,String user,String pass)throws Exception;
		public abstract String getUserFromSessionId(String app,String s)throws Exception;
		public abstract void addSessionId(String app,String user,String sid)throws Exception;
		public abstract void deleteSessionId(String app,String user)throws Exception;

		public abstract void addRole(String app,String user,String role)throws Exception;
		public abstract void deleteRole(String app,String user,String role)throws Exception;
		public abstract void addUser(String app,String user,String passwd,String digest,String utype)throws Exception;
		public abstract void deleteUser(String app,String user)throws Exception;
		public abstract void updatePasswd(String app,String user,String passwd)throws Exception;
		public abstract boolean exists(String app,String user)throws Exception;

			*/


}

