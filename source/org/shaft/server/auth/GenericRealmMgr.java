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
import java.util.Date;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Hashtable;
import org.shaft.server.utils.*;

import tools.util.*;

import java.sql.Connection;
import java.sql.ResultSet;
import tools.util.*;


public class GenericRealmMgr extends RealmMgr
{



	public  boolean canInvite(String app,String realm)throws Exception
	{
				SPQueryAndUpdate spq = new SPQueryAndUpdate(DBConnectionMgr.getRConnection(app));
			try{
		String sql = "SELECT memberscaninvite  from shaftrealm where app = ? AND shaftrealmid = ?";
			spq.setTemplate(sql);

			spq.addVariable(app);
			spq.addVariable(realm);

			ResultSet res = spq.query();
			if (res.next())
				return res.getBoolean(1);

			}

			finally{
				DBConnectionMgr.closeR(spq);
			}

		return false;
	}

	public  boolean secretMembership(String app,String realm)throws Exception
	{

				SPQueryAndUpdate spq = new SPQueryAndUpdate(DBConnectionMgr.getRConnection(app));
			try{
		String sql = "SELECT secretmembership  from shaftrealm where app = ? AND shaftrealmid = ?";
			spq.setTemplate(sql);

			spq.addVariable(app);
			spq.addVariable(realm);

			ResultSet res = spq.query();
			if (res.next())
				return res.getBoolean(1);

			}

			finally{
				DBConnectionMgr.closeR(spq);
			}

		return false;
		}




	public  void addRealm(String app,String u,String realm,boolean canInvite,boolean secret)throws Exception
	{
					SPQueryAndUpdate spq = new SPQueryAndUpdate(DBConnectionMgr.getConnection(app));
			try{
		String sql = "INSERT INTO shaftrealm (shaftuser,app,shaftrealmid,memberscaninvite,secretmembership) values (?,?,?,?,?)";

			spq.setTemplate(sql);
			spq.addVariable(u);
			spq.addVariable(app);
			spq.addVariable(realm);
			spq.addVariable(canInvite);
			spq.addVariable(secret);

			 spq.execute();

			}

			finally{
				DBConnectionMgr.close(spq);
			}
		}


	public  Vector<String> getRealms(String app,String user)throws Exception
		{
		SPQueryAndUpdate spq = new SPQueryAndUpdate(DBConnectionMgr.getRConnection(app));
			try{
		String sql = "SELECT DISTINCT shaftrealmid from shaftrealmmembers where app = ? AND shaftuser = ?";
			spq.setTemplate(sql);

			spq.addVariable(app);
			spq.addVariable(user);

			return spq.toVector();

			}

			finally{
				DBConnectionMgr.closeR(spq);
			}
		}
	
	public  void deleteRealms(String app,String user)throws Exception
	{
		SPQueryAndUpdate spq = new SPQueryAndUpdate(DBConnectionMgr.getConnection(app));
		try{
	String sql = "DELETE FROM shaftrealmmembers where shaftuser = ? AND app = ?";

		spq.setTemplate(sql);
		spq.addVariable(user);
		spq.addVariable(app);
		 spq.execute();

		}

		finally{
			DBConnectionMgr.close(spq);
		}
	}

	public  Vector<String> listRealmsCreated(String app,String user)throws Exception
		{
		SPQueryAndUpdate spq = new SPQueryAndUpdate(DBConnectionMgr.getRConnection(app));
			try{
		String sql = "SELECT DISTINCT shaftrealmid from shaftrealm where app = ? AND shaftuser = ?";
			spq.setTemplate(sql);

			spq.addVariable(app);
			spq.addVariable(user);

			return spq.toVector();

			}

			finally{
				DBConnectionMgr.closeR(spq);
			}
		}



	public  Vector<String> listMembers(String app,String realm)throws Exception
	{

		SPQueryAndUpdate spq = new SPQueryAndUpdate(DBConnectionMgr.getRConnection(app));
			try{
		String sql = "SELECT DISTINCT shaftuser from shaftrealmmembers where app = ? AND shaftrealmid = ?";
			spq.setTemplate(sql);

			spq.addVariable(app);
			spq.addVariable(realm);

			return spq.toVector();

			}

			finally{
				DBConnectionMgr.closeR(spq);
			}
		}



		public  void addUser(String app,String user,String realm)throws Exception
		{
			SPQueryAndUpdate spq = new SPQueryAndUpdate(DBConnectionMgr.getConnection(app));
			try{
		String sql = "INSERT INTO shaftrealmmembers (shaftuser,app,shaftrealmid) values (?,?,?)";

			spq.setTemplate(sql);
			spq.addVariable(user);
			spq.addVariable(app);
			spq.addVariable(realm);

			 spq.execute();

			}

			finally{
				DBConnectionMgr.close(spq);
			}
		}


		public Vector<String> listIDs(String app)throws Exception{
		SPQueryAndUpdate spq = new SPQueryAndUpdate(DBConnectionMgr.getRConnection(app));
			try{
		String sql = "SELECT DISTINCT shaftrealmid from shaftrealm where app = ?";
			spq.setTemplate(sql);
			//(" GRM.lisiyd app " + app);
			spq.addVariable(app);
			return spq.toVector();

			}

			finally{
				DBConnectionMgr.closeR(spq);
			}

	}
//----------------------------------------------------------
/*
		protected Connection getConnection(String app)throws Exception{
		return DBConMgr.getConnection(ShaftRestConfig.datasourceName);
	}*/




}

