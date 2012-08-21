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
import org.shaft.server.objs.*;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import org.shaft.server.utils.*;
import tools.util.*;
import org.shaft.server.auth.*;


public abstract class DBServerObjs extends ServerObjsInterf{
	public static Vector<String> actions = new Vector<String>();
	//	static Vector<String> datatypes = null;
		//new Vector<String>();

	DBServerObjsParser dbp = new DBServerObjsParser();
	static {
		actions.add("add");
		actions.add("create");
		actions.add("view");
		actions.add("read");
		actions.add("list");
		actions.add("delete");
		actions.add("update");
////		datatypes = SharedMethods.toVector(ShaftRestConfig.dataTypes,",");

	}

		public Vector<DBField> getCriteria(MultiValuePair params,Vector<DBField> dbfs,StringBuffer sqlv,DBServerObjsParser sop,String action)throws Exception{
			Vector<DBField> critv = new Vector<DBField>();
					DBField dbf = null;
					DBField crit = null;
					Object val = null;
					//DBServerObjsParser sop = getServerObjsParser();
					if (!action.equals("create") && !action.equals("add"))
					{

					sqlv.append(sop.shaftrealm );
					sqlv.append(" = ? ");
					crit = new DBField();
					crit.defaultValue = sop.getRealm();
					critv.add(crit);

					DBAccess deletea = sop.getAccess(action);

					if (deletea.getRoles().size() > 0){
					if (deletea.getRoles().contains(ShaftRestConfig.ownerRole))
					{
					sqlv.append(" AND " + sop.shaftowner );
					sqlv.append(" = ? ");
					crit = new DBField();
					//crit.dateformat = getDateFormat();
					crit.defaultValue = sop.getUser();
					critv.add(crit);
					}
					}
					}
					String comp = null;
					if (action.equals("read") || action.equals("list")){
					val = params.get(sop.shaftowner + ".eq");
					if (val != null)
					{
					if (sqlv.length() > 1)
					sqlv.append(" AND ");
						sqlv.append(sop.shaftowner );
					sqlv.append(" = '" + val + "' ");
					}
					else{
					val = params.get(sop.shaftowner + ".ne");
					if (val != null)
					{
					if (sqlv.length() > 1)
					sqlv.append(" AND ");
						sqlv.append(sop.shaftowner );
					sqlv.append(" != '" + val + "' ");
					}
					}
					}
					for (int ct = 0;ct < dbfs.size();ct++){
					dbf = dbfs.elementAt(ct);
					val = params.get(dbf.name + ".eq");

					if (val != null)
						comp = "=";
					else{
					val = params.get(dbf.name + ".lt");
					if (val != null)
						comp = "<";
						else{
					val = params.get(dbf.name + ".gt");
					if (val != null)
						comp = ">";
					else{
					val = params.get(dbf.name + ".ne");
					if (val != null)
						comp = "!=";
					}
					}
					}

					if (val != null)
					{
					if (sqlv.length() > 1){
					sqlv.append(" AND ");
					}
					sqlv.append(dbf.name );
					sqlv.append(" " + comp + " ? ");

					crit = new DBField();
					crit.dateformat = getDateFormat();

					crit.dataType = dbf.dataType;
					crit.defaultValue = val;
					critv.add(crit);
					}

				}

					if (sop.shaftid != null){

					val = params.get(sop.shaftid + ".eq");
					if (val == null)
					val = params.get(sop.shaftid );

					if (val != null)
					{
					if (sqlv.length() > 1){
					sqlv.append(" AND ");
					}
					sqlv.append(sop.shaftid );
					sqlv.append(" = ? ");

					crit = new DBField();
					crit.dateformat = getDateFormat();
					crit.dataType = ShaftRestConfig.shaftid;
					crit.defaultValue = val;

					critv.add(crit);
					}
					}
					if (sop.shafttimestamp != null){

					val = params.get(sop.shafttimestamp + ".eq");

					if (val != null)
						comp = "=";
					else{
					val = params.get(sop.shafttimestamp + ".lt");
					if (val != null)
						comp = "<";
						else{
					val = params.get(sop.shafttimestamp + ".gt");
					if (val != null)
						comp = ">";
					}
					}


					if (val == null){
					val = params.get(sop.shafttimestamp );
					comp = "=";
					}
					if (val != null)
					{
					if (sqlv.length() > 1){
					sqlv.append(" AND ");
					}
					sqlv.append(sop.shafttimestamp );
					sqlv.append(" " + comp + " ? ");

					crit = new DBField();
					crit.dateformat = getDateFormat();
					crit.dataType = ShaftRestConfig.shafttimestamp;
					crit.defaultValue = val;

					critv.add(crit);
					}
					}

			return critv;
		}
		public  Vector<String> supportedActions()throws Exception{
			return actions;
		}

		public DBServerObjsParser getServerObjsParser()throws Exception{
			return dbp;
		}
/*	public Object process(HttpServletRequest request)throws Exception{
		String action = (String)request.getAttribute("shaftaction");
		if (!supportedActions().contains(action))
			throw new ShaftRestException("unsupported_action:" + action);
		return doProcess(action,request);
	}

	public abstract Object doProcess(String action, HttpServletRequest request)throws Exception;

	public Object process(HttpServletRequest request)throws Exception{
		return "isredbib";
	}*/


	/*	public void checkAccess(String app,String action,ServerObjsParser sop, HttpServletRequest request)throws Exception{
			DBServerObjsParser dop = (DBServerObjsParser)sop;
			DBAccess dba = dop.getDBAccess(action);
			if (dba == null)return;
			String u = UserMgr. Mgr(app).auth(app,request,dba.roles);
			dop.setUser(u);

		}*/


		public Object doProcess(String app,String path,String action,HttpServletRequest request)throws Exception{
		createTable(app,path);
		MultiValuePair mvp = new MultiValuePair( request.getParameterMap());
		HashMap bp = (HashMap)request.getAttribute("batch.params");
		if (bp != null)
		{
			mvp.putAll(bp);
			mvp.put("is.batch",true);
			
		}
		if (action.equals("create") || action.equals("add"))
		return add(app,path,mvp);
		if (action.equals("delete"))
		return delete(app,path,mvp);
		if (action.equals("update"))
		return update(app,path,mvp);

		else if (action.equals("read") || action.equals("list"))
		return list(app,path,mvp);

		return getServerObjsParser().attributes;

	}

		public String getTableName(String app,String path)throws Exception
	{
		//prependAppNameToTable
		String table = path;
		if (ShaftRestConfig.prependAppNameToTable)
			table = app + "_" + table;
		return table;

				//Vector listTables
	}
//java.util.Map getParameterMap()
	public abstract Object update(String app,String path, MultiValuePair params)throws Exception;

	public abstract Object delete(String app,String path, MultiValuePair params)throws Exception;

	public abstract Object add(String app,String path, MultiValuePair params)throws Exception;
	public abstract Object list(String app,String path, MultiValuePair params)throws Exception;


	public abstract void createTable(String app, String path)throws Exception;
}