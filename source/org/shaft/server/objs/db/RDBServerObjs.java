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
import java.sql.Connection;
import java.sql.ResultSet;
import tools.util.*;

import org.shaft.server.utils.*;

import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;

import java.util.Enumeration;
import java.util.Vector;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;


public class RDBServerObjs extends DBServerObjs{
/*
	public Object doProcess(String app,String path,String action,HttpServletRequest request)throws Exception{
		return getServerObjsParser().attributes;

		CREATE TABLE shaftuser (
 shaftuser varchar(50) NOT NULL,
 app varchar(50) NOT null,
 shaftpassword varchar(150) NOT null,
 administrator varchar(50),
  digest varchar(50),
 PRIMARY KEY  (shaftuser,app)
) ;
	String sql = "UPDATE appvh set jvmid = ?,accountid = ? where accountid = ? and jvmid = ? AND appid = ?";

	}*/
static boolean dbtypeC = false;
static String dbtype = null;


	void addVariable(SPQueryAndUpdate spq,DBField dbf,Object val)throws Exception{
						if (val == null)
							val = dbf.defaultValue;
						//if (dbf.dataType.equals("string") || dbf.dataType.equals("text") || dbf.dataType.equals("shaftid"))
						if (dbf.isString())
							{
						//(" ADCRITVAR " + val);
					spq.addVariable((String)dbf.getValue(val) );
						}
						else if (dbf.isNumber())
							{
					spq.addVariable((Integer)dbf.getValue(val) );
						}
								else if (dbf.isBoolean())
							{
					spq.addVariable((Boolean)dbf.getValue(val) );
						}
						else if (dbf.isDateTime())
							{
					spq.addVariable((Date)dbf.getValue(val) );
						}
					else
						throw new ShaftRestException("invalid_datatype:" + dbf.name + ":" + dbf.dataType );

	}
		public  Object update(String app,String path, MultiValuePair params)throws Exception
	{
		String oid = null;
						DBField dbf = null;
					DBServerObjsParser sop =	getServerObjsParser();
				//	shaftid = sop.shaftid;
					String shaftid = (String)params.remove(sop.shaftid);
					if (shaftid != null)
						params.put(sop.shaftid + ".eq",shaftid);
					Vector<DBField> dbfs = sop.attributes;


		String table = getTableName(app,path);
		SPQueryAndUpdate spq = new SPQueryAndUpdate(DBConnectionMgr.getConnection(app));
		try{
			if (ShaftRestConfig.forceNamesToLowerCase)
			spq.forceTableNameToLowerCase();

				StringBuffer sql = new  StringBuffer( "UPDATE  " + table  + " set ");
				StringBuffer sqlv = new  StringBuffer();
				dbf = null;

				for (int ct = 0;ct < dbfs.size();ct++){
					dbf = dbfs.elementAt(ct);
					if (params.get(dbf.name) != null)
					{

					if (sqlv.length() > 1){

					sqlv.append(" , ");
					}

					sqlv.append(dbf.name );
					sqlv.append(" = ? ");


					}

				}
				sql.append(sqlv);
				sqlv = new  StringBuffer();
				Vector<DBField> critv = getCriteria(params,dbfs,sqlv,sop,"update");

				if (sqlv.length() > 1){
				sql.append(" WHERE ");
				sql.append(sqlv);
				}
				sql.append(";");
				//("update DB TABLE " + sql);
				spq.setTemplate(sql);

					for (int ct = 0;ct < dbfs.size();ct++){
					dbf = dbfs.elementAt(ct);
					if (params.get(dbf.name) != null)
					{
					addVariable(spq,dbf,params.get(dbf.name));

					/*(dbf.name + " update val " + params.get(dbf.name));
					if (dbf.dataType.equals("string") || dbf.dataType.equals("text"))
					spq.addVariable((String)params.get(dbf.name) );
					else if (dbf.dataType.equals("number") || dbf.dataType.equals("integer") || dbf.dataType.equals("int") )
					spq.addVariable(Integer.parseInt((String)params.get(dbf.name)) );

					else
						throw new ShaftRestException("invalid_datatype:" + dbf.name + ":" + dbf.dataType );*/
					}

				}

			for (int ct = 0;ct < critv.size();ct++){
					dbf = critv.elementAt(ct);

					//(dbf.name + " update crit " + dbf.defaultValue);
					addVariable(spq,dbf,null);
				}


				spq.execute();
				return true;

		}finally{
			//spq.close();
			DBConnectionMgr.close(spq);
		}

	}

	public  Object delete(String app,String path, MultiValuePair params)throws Exception
	{
		String oid = null;
						DBField dbf = null;
						DBServerObjsParser sop =	getServerObjsParser();
				Vector<DBField> dbfs = sop.attributes;


		String table = getTableName(app,path);
		SPQueryAndUpdate spq = new SPQueryAndUpdate(DBConnectionMgr.getConnection(app));
		try{
			if (ShaftRestConfig.forceNamesToLowerCase)
			spq.forceTableNameToLowerCase();

				StringBuffer sql = new  StringBuffer( "DELETE from  " + table );
			//	StringBuffer sqlv = new  StringBuffer();
				dbf = null;
/*
				for (int ct = 0;ct < dbfs.size();ct++){
					dbf = dbfs.elementAt(ct);
					if (params.get(dbf.name) != null)
					{

					if (sqlv.length() > 1){

					sqlv.append(" AND ");
					}

					sqlv.append(dbf.name );
					sqlv.append(" = ? ");


					}

				}*/

				StringBuffer	sqlv = new  StringBuffer();
				Vector<DBField> critv = getCriteria(params,dbfs,sqlv,sop,"delete");
				//Access deletea = sop.getAccess
				if (sqlv.length() > 1){
				sql.append(" WHERE ");
				sql.append(sqlv);
				}



			/*	if (sqlv.length() > 1){
				sql.append(" WHERE ");
				sql.append(sqlv);
				}*/
				sql.append(";");
				//("DELETE DB TABLE 1 " + sql);
				spq.setTemplate(sql);

					for (int ct = 0;ct < critv.size();ct++){
					dbf = critv.elementAt(ct);
				//	if (params.get(dbf.name) != null)
					{
					addVariable(spq,dbf,null);

					/*(dbf.name + " LIST HC " + params.get(dbf.name));
					if (dbf.dataType.equals("string"))
					spq.addVariable((String)params.get(dbf.name) );
						else if (dbf.dataType.equals("number") || dbf.dataType.equals("integer") || dbf.dataType.equals("int") )
					spq.addVariable(Integer.parseInt((String)params.get(dbf.name)) );

					else
						throw new ShaftRestException("invalid_datatype:" + dbf.name + ":" + dbf.dataType );*/
					}

				}


				spq.execute();
				return true;

		}finally{
			//spq.close();
			DBConnectionMgr.close(spq);
		}

	}

	public  Object list(String app,String path, MultiValuePair params)throws Exception
	{
		String oid = null;
						DBField dbf = null;
						DBServerObjsParser sop =	getServerObjsParser();
				Vector<DBField> dbfs = sop.attributes;

Vector<String> cn = new Vector<String>(dbfs.size() + 1);
for (int ct = 0; ct < dbfs.size();ct++)
	cn.add(dbfs.elementAt(ct).name);
NameValuePairs appcfg = ShaftRestConfig.getAppConfig(app);
if (appcfg.getBoolean("show." + sop.shaftowner))
	cn.add(sop.shaftowner);
if (appcfg.getBoolean("show." + sop.shaftrealm))
	cn.add(sop.shaftrealm);
if (appcfg.getBoolean("show." + sop.shafttimestamp))
	cn.add(sop.shafttimestamp);

cn.add(sop.shaftid);
String table = getTableName(app,path);
		SPQueryAndUpdate spq = new SPQueryAndUpdate(DBConnectionMgr.getRConnection(app));
		try{
			if (ShaftRestConfig.forceNamesToLowerCase)
			spq.forceTableNameToLowerCase();

			String pn = (String)params.get("results.page");
			int ps = ShaftRestConfig.getDefaultConfig().getInt("results.page.max.size");
			Map m = new HashMap();
			//Vector<DBField> critv = getCriteria(params,dbfs,sqlv,sop,"read");
			if (pn == null)
			{
				
				StringBuffer sql = new  StringBuffer( "select count(*) from  " + table );
				StringBuffer sqlv = new  StringBuffer();
				dbf = null;
				//(" SEL LIST 1 " + dbfs);


								sqlv = new  StringBuffer();
				Vector<DBField> critv = getCriteria(params,dbfs,sqlv,sop,"read");

				if (sqlv.length() > 1){
				sql.append(" WHERE ");
				sql.append(sqlv);
				}

					//(" SEL LIST 2 " + sqlv);


				sql.append(";");
				//("list DB TABLE " + sql);
				spq.setTemplate(sql);



							for (int ct = 0;ct < critv.size();ct++){
					dbf = critv.elementAt(ct);

					//(dbf.name + " list crit " + dbf.defaultValue);
					addVariable(spq,dbf,null);
				}



				ResultSet res = spq.query();
				res.next();
				int dbc = res.getInt(1);
				m.put("results.page.max.size", ps);
				int pgs = dbc/ps;
				if ((pgs * ps ) < dbc)pgs = pgs + 1;
				if (pgs < 1)pgs = 1;
				//(pgs + " pgsss " + dbc);
				m.put("results.pages", pgs);
				pn = "1";
				
				
			}
			
			
				StringBuffer sql = new  StringBuffer( "select * from  " + table );
				StringBuffer sqlv = new  StringBuffer();
				dbf = null;
				//(" SEL LIST 1 " + dbfs);


								sqlv = new  StringBuffer();
				Vector<DBField> critv = getCriteria(params,dbfs,sqlv,sop,"read");

				if (sqlv.length() > 1){
				sql.append(" WHERE ");
				sql.append(sqlv);
				}

					//(" SEL LIST 2 " + sqlv);

				int pl = Integer.parseInt(pn);
				pl = ((pl - 1)* ps);
				if (!dbtypeC){
					dbtypeC = true;
					dbtype = spq.getDBType();
					
				}
				if (dbtype.equals("derby"))
				{
					
	//				OFFSET 10 ROWS FETCH NEXT 10 ROWS ONLY
					sql.append(" OFFSET " + pl + " ROWS FETCH NEXT " + ps + " ROWS ONLY ");
				}
				else
				sql.append(" LIMIT " + pl + "," + ps);
				sql.append( ";");
				//("list DB TABLE " + sql);
				spq.setTemplate(sql);



							for (int ct = 0;ct < critv.size();ct++){
					dbf = critv.elementAt(ct);

					//(dbf.name + " list crit " + dbf.defaultValue);
					addVariable(spq,dbf,null);
				}



				Vector v = spq.listResults(cn);
				

				m.put("columns", v.elementAt(0));

				v.setElementAt(m, 0);
				//v.remove(0);
				//("DB LIST RESULT " + v);
				return v;
				//Hashtable h2 = new Hashtable();
				//h2.put("metadata", m);
				//h2.put("results", v);
				//return h2;

		}finally{
			//spq.close();
			DBConnectionMgr.closeR(spq);
		}

	}


	public  Object add(String app,String path, MultiValuePair params)throws Exception
	{
		String oid = null;
						DBField dbf = null;
						DBServerObjsParser sop = getServerObjsParser();
				Vector<DBField> dbfs = sop.attributes;

							for (int ct = 0;ct < dbfs.size();ct++){
					dbf = dbfs.elementAt(ct);
					if (params.get(dbf.name) == null)
						throw new ShaftRestException("missing_field_value:" + dbf.name );
							}
		for (int cta = 0;cta < ShaftRestConfig.maxAddRowAttempts;cta++)
		try{

		String table = getTableName(app,path);
		SPQueryAndUpdate spq = new SPQueryAndUpdate(DBConnectionMgr.getConnection(app));
		try{
			if (ShaftRestConfig.forceNamesToLowerCase)
			spq.forceTableNameToLowerCase();

				StringBuffer sql = new  StringBuffer( "INSERT INTO  " + table + "(");
				StringBuffer sqlv = new  StringBuffer( " ) VALUES (");
				dbf = null;

				sql.append(sop.shaftid + ",");
				sql.append(sop.shaftowner + ",");
				sql.append(sop.shaftrealm + ",");

				sqlv.append("?,");
				sqlv.append("?,");
				sqlv.append("?,");

				for (int ct = 0;ct < dbfs.size();ct++){
					dbf = dbfs.elementAt(ct);
					sql.append(dbf.name );
					sqlv.append("?");

					if (dbfs.size() - ct > 1){

					sql.append(",");
					sqlv.append(",");
					}

				}
				sql.append(sqlv);
				sql.append(");");
				//(oid + ":" + sop.getUser() + " inseert DB TABLE " + sql + ":" + dbfs.size() + ":" + dbfs);

				spq.setTemplate(sql);
				oid = generateID(app,path);
				spq.addVariable(oid);
				spq.addVariable(sop.getUser());
				spq.addVariable(sop.getRealm());

				//(oid + ":" + sop.getUser() + " inseert DB TABLE " + sql + ":" + dbfs.size() + ":" + dbfs);

					for (int ct = 0;ct < dbfs.size();ct++){
						dbf = dbfs.elementAt(ct);
											addVariable(spq,dbf,params.get(dbf.name));



				}


				spq.execute();
		}finally{
			//spq.close();
			DBConnectionMgr.close(spq);
		}



			return oid;

		}catch (Exception e){
			e.printStackTrace();
		}
		throw new ShaftRestException("unable_to_add_row" );

	}

	protected String  generateID(String app,String path)throws Exception{
Vector<String> suv = listIDs(app,path);
String tid = AlphaNumeric.generateRandomAlphNumeric(ShaftRestConfig.defaultRowIDLength);

while (true){
if (!suv.contains(tid) )
{
	break;
}
tid = AlphaNumeric.generateRandomAlphNumeric(ShaftRestConfig.defaultRowIDLength);
}
return tid;
	}


		protected Vector<String> listIDs(String app,String path)throws Exception{
		String table = getTableName(app,path);
		SPQueryAndUpdate spq = new SPQueryAndUpdate(DBConnectionMgr.getRConnection(app));
			try{
		String sql = "SELECT DISTINCT " + getServerObjsParser().shaftid + " from " + table;

			spq.setTemplate(sql);
			return spq.toVector();

			}

			finally{
				//if (spq != null)spq.close();
				DBConnectionMgr.closeR(spq);
			}

	}

		
	void checkForUniquePerField(DBField dbf ,Vector uni){
		Enumeration en = dbf.params.keys();
		while (en.hasMoreElements()){
			String k = en.nextElement().toString();
			if (k.endsWith(".unique"))
			{
				if (dbf.params.getBoolean(k))
				{
				uni.add(k.substring(0,k.length() - 7));
				uni.add(dbf.name);
				}
			}
		}
	}

	public void createTable(String app,String path)throws Exception
	{
		//prependAppNameToTable
		String table = getTableName(app,path);
		SPQueryAndUpdate spq = new SPQueryAndUpdate(DBConnectionMgr.getConnection(app));
		try{
			if (ShaftRestConfig.forceNamesToLowerCase)
			spq.forceTableNameToLowerCase();
			Vector lt = spq.listTables();
			//("TABLE LST " + lt);
			if (!lt.contains(table))
			{
				StringBuffer sql = new  StringBuffer( "create table " + table + "(");

				Vector<DBField> dbfs = getServerObjsParser().attributes;
				DBField dbf = null;
				DBServerObjsParser sop = getServerObjsParser();
				sql.append(sop.shaftid + " varchar(50) NOT NULL,");
				sql.append(sop.shaftowner + " varchar(" + ShaftRestConfig.shaftownerlength + ") NOT NULL,");
				sql.append(sop.shaftrealm + " varchar(" + ShaftRestConfig.shaftrealmlength + ") NOT NULL,");

				sql.append(sop.shafttimestamp + " TIMESTAMP default CURRENT_TIMESTAMP,");
				EZArrayList uniqs = new EZArrayList();
				for (int ct = 0;ct < dbfs.size();ct++){
					dbf = dbfs.elementAt(ct);
					if (dbf.isString())
					sql.append(dbf.name + " varchar(" +  dbf.dataLength + ")");
					else if (dbf.isNumber() )
					sql.append(dbf.name + " int");
					else if (dbf.isDateTime() )
					sql.append(dbf.name + " TIMESTAMP");

					else if (dbf.isBoolean())
					{
						sql.append(dbf.name + " boolean");

						//getDBType
					}
					else
						throw new ShaftRestException("invalid_datatype:" + dbf.name + ":" + dbf.dataType );
					if (dbf.notNull)
					sql.append(" NOT NULL");
					if (dbf.params.getBoolean("unique"))
						sql.append(" UNIQUE");
					//else if (dbf.params.getBoolean("ownerUnique"))
					//	uniqs.add(dbf.name);
					checkForUniquePerField(dbf,uniqs);
					if (dbfs.size() - ct > 1)
					sql.append(",");

				}
				if (uniqs.size() > 0)
				{
					sql.append(", PRIMARY KEY (" + sop.shaftowner + "," + uniqs.toDelimitedString(",") + ")");

				}
				sql.append(");");
				//("CREATE DB TABLE " + sql);
				spq.setTemplate(sql);
				spq.execute();
			}
		}finally{
			//spq.close();
			DBConnectionMgr.close(spq);
		}
	}


}