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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.Charset;


public class GenericUserMgr extends UserMgr
{
		    protected static MessageDigest md5Helper = null;

  		static{
				
       try {
                md5Helper = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
  		}
  		
  	
  	 		            protected static String setDigest(String username,String passwd)throws Exception {
return setDigest(username,passwd,ShaftRestConfig.realmName);
  	 		            }
  		
  		            protected static String setDigest(String username,String passwd, String realmName)throws Exception {

try{

            
        String digestValue = username + ":" + realmName + ":"
            + passwd;

        byte[] valueBytes = null;
      //  try {
            valueBytes = digestValue.getBytes(Charset.defaultCharset());
      //  } catch (UnsupportedEncodingException uee) {
           // log.error("Illegal digestEncoding: " + getDigestEncoding(), uee);
       //     throw new IllegalArgumentException(uee.getMessage());
       // }

        byte[] digest = null;
        // Bugzilla 32137
        synchronized(md5Helper) {
            digest = md5Helper.digest(valueBytes);
        }

        return md5Encode(digest);
}catch (Exception e){
	e.printStackTrace();
	ShaftAuthException.throwException(e);
}
return null;
    }
    
    
            private static final char[] hexadecimal =
    {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
     'a', 'b', 'c', 'd', 'e', 'f'};


    // --------------------------------------------------------- Public Methods


    /**
     * Encodes the 128 bit (16 bytes) MD5 into a 32 character String.
     *
     * @param binaryData Array containing the digest
     * @return Encoded MD5, or null if encoding failed
     */
    protected static  String md5Encode( byte[] binaryData ) {

        if (binaryData.length != 16)
            return null;

        char[] buffer = new char[32];

        for (int i=0; i<16; i++) {
            int low = binaryData[i] & 0x0f;
            int high = (binaryData[i] & 0xf0) >> 4;
            buffer[i*2] = hexadecimal[high];
            buffer[i*2 + 1] = hexadecimal[low];
        }

        return new String(buffer);

    }
  		
  		
	/*	protected Connection getConnection(String app)throws Exception{
		return DBConMgr.getConnection(ShaftRestConfig.datasourceName);
	}*/

			public boolean exists(String app,String user)throws Exception{
				return getUserInfo(app,user).size() > 0;
			}


public String getOwner(String app,String user)throws Exception{
	NameValuePairs nvp = getUserInfo(app,user);
	return (String)nvp.get("shaftowner");
}

 public Vector<String> list(String app,String user)throws Exception{
	SPQueryAndUpdate spq = new SPQueryAndUpdate(DBConnectionMgr.getRConnection(app));
		try{
		spq.setTemplate("select shaftuser from shaftuser where shaftowner = ? AND app = ?");
		spq.addVariable(user);
		spq.addVariable(app);
		return spq.toVector();
		}finally{
			DBConnectionMgr.closeR(spq);
		}
}

	NameValuePairs getUserInfo(String app,String user)throws Exception{
		SPQueryAndUpdate spq = new SPQueryAndUpdate(DBConnectionMgr.getRConnection(app));
			try{
			spq.setTemplate("select * from shaftuser where shaftuser = ? AND app = ?");
			spq.addVariable(user);
			spq.addVariable(app);
			return spq.getHash();
			}finally{
				DBConnectionMgr.closeR(spq);
			}
	}
	
	
		public	String getPasswd(String app,String user)throws Exception{
		
			return (String)getUserPasswd(app,user).get("shaftpassword");
	}
	
			public	String getMD5Digest(String app,String user)throws Exception{
		
			return (String)getUserPasswd(app,user).get("md5");
	}

		NameValuePairs getUserPasswd(String app,String user)throws Exception{
		SPQueryAndUpdate spq = new SPQueryAndUpdate(DBConnectionMgr.getRConnection(app));
			try{
			spq.setTemplate("select * from shaftpasswd where shaftuser = ? AND app = ?");
			spq.addVariable(user);
			spq.addVariable(app);
			return spq.getHash();
			}finally{
				DBConnectionMgr.closeR(spq);
			}
	}

		protected String passwdDigest(String passwd,String digest)throws Exception{
			if (!"PLAIN".equals(digest)){
 					if (StringUtil.isRealString(digest))
 					{
 					if (	 digest.equals("SHA"))
		    		passwd = Crypto.digest(passwd,digest);
		    		else
		    			new ShaftAuthException("unable_to_authenticate").throwIt();

 					}

 				}
 				return passwd;
		}

		public String login(String app,String user,String pass,boolean cs)throws Exception{
		NameValuePairs ui = getUserInfo(app,user);
		if (ui.size() < 1)
			new ShaftAuthException("unable_to_authenticate").throwIt();
		if (ui.getString("shaftusertype").equals("db"))
		{
		NameValuePairs up = getUserPasswd(app,user);
		if (up.size() < 2)
			new ShaftAuthException("unable_to_authenticate").throwIt();
		String digest = ui.getString("digest");
		String credentials = up.getString("shaftpassword");
		//(credentials + ":" + digest +  " THE CRED q " + pass);
 			pass = passwdDigest(pass,digest);
 		//(credentials + ":" + digest +  " THE CRED 2 " + pass);

 			/*	if (!"PLAIN".equals(digest)){
 					if (StringUtil.isRealString(digest))
 					{
 					if (	 digest.equals("SHA"))
		    		pass = Crypto.digest(pass,digest);
		    		else
		    			new ShaftAuthException("unable_to_authenticate").throwIt();

 					}

 				}*/
 				if (!pass.equals(credentials))
					new ShaftAuthException("unable_to_authenticate").throwIt();
				if (!cs)return user;
				return createSessionId(app,user);

		}
			new ShaftAuthException("unable_to_authenticate").throwIt();

		return null;

		}


		public Vector<String> listIDs(String app)throws Exception{
		SPQueryAndUpdate spq = new SPQueryAndUpdate(DBConnectionMgr.getRConnection(app));
			try{
		String sql = "SELECT DISTINCT shaftsessionid from shaftsession";

			spq.setTemplate(sql);
			return spq.toVector();

			}

			finally{
				DBConnectionMgr.closeR(spq);
			}

	}



		public  void addSessionId(String app,String user,String sid)throws Exception{
			SPQueryAndUpdate spq = new SPQueryAndUpdate(DBConnectionMgr.getConnection(app));
			try{
		String sql = "INSERT INTO shaftsession (shaftuser,app,shaftsessionid) values (?,?,?)";

			spq.setTemplate(sql);
			spq.addVariable(user);
			spq.addVariable(app);
			spq.addVariable(sid);

			 spq.execute();

			}

			finally{
				DBConnectionMgr.close(spq);
			}
		}
		public  void deleteSessionId(String app,String user)throws Exception{
			SPQueryAndUpdate spq = new SPQueryAndUpdate(DBConnectionMgr.getConnection(app));
			try{
		String sql = "DELETE FROM shaftsession where shaftuser = ? AND app = ?";

			spq.setTemplate(sql);
			spq.addVariable(user);
			spq.addVariable(app);
			 spq.execute();

			}

			finally{
				DBConnectionMgr.close(spq);
			}
		}

		public void updateSessionAge(String app,String user)throws Exception
		{

						SPQueryAndUpdate spq = new SPQueryAndUpdate(DBConnectionMgr.getConnection(app));
			try{
		String sql = "UPDATE shaftsession SET sessionage = ? where shaftuser = ? AND app = ?";
			Date dbt = spq.getDBTime();
			spq.setTemplate(sql);
//new java.sql.Timestamp(spq.getDBTime().getTime())
			spq.addVariable(dbt);
			spq.addVariable(user);
			spq.addVariable(app);

			 spq.execute();

			}

			finally{
				DBConnectionMgr.close(spq);
			}
		}

		public String getUserFromSessionId(String app,String sid)throws Exception{

		SPQueryAndUpdate spq = new SPQueryAndUpdate(DBConnectionMgr.getRConnection(app));
			try{
		String sql = "SELECT shaftuser,sessionage from shaftsession where app = ? AND shaftsessionid = ?";

			spq.setTemplate(sql);
			spq.addVariable(app);
			spq.addVariable(sid);
			ResultSet res = spq.query();
			if (res.next()){
				long ma = getMaxAge(app);

				String u = res.getString(1);
				//(u + " SESSION AGE " + ma);
				if (ma > 0){
					Date sd = res.getTimestamp(2);
					long st = sd.getTime();

				if ((st + ma) < spq.getDBTime().getTime()){
					DBConnectionMgr.closeR(spq);
					deleteSessionId(app,u);
					return null;
				}
				}
				DBConnectionMgr.closeR(spq);
				updateSessionAge(app,u);
				return u;
			}


			}

			finally{
				DBConnectionMgr.closeR(spq);
			}

		return null;

		}



		public  void addUser(String app,String user,String passwd,String digest,String utype,String owner)throws Exception
		{
						SPQueryAndUpdate spq = new SPQueryAndUpdate(DBConnectionMgr.getConnection(app));
			try{
		String sql = "INSERT INTO shaftuser (shaftuser,app,shaftusertype,digest) values (?,?,?,?)";
		if (owner != null)
		sql = "INSERT INTO shaftuser (shaftuser,app,shaftusertype,digest,shaftowner) values (?,?,?,?,?)";
			spq.setTemplate(sql);
			spq.addVariable(user);
			spq.addVariable(app);
			spq.addVariable(utype);
			spq.addVariable(digest);
			if (owner != null)
			spq.addVariable(owner);

			 spq.execute();

			}

			finally{
				DBConnectionMgr.close(spq);
			}
			if (utype.equals("db") && passwd != null)
				addPasswd(app,user,passwd,digest);

			}

					protected  void addPasswd(String app,String user,String passwd,String digest)throws Exception
		{
						SPQueryAndUpdate spq = new SPQueryAndUpdate(DBConnectionMgr.getConnection(app));
			try{
		String sql = "INSERT INTO shaftpasswd (shaftuser,app,shaftpassword,md5) values (?,?,?,?)";

			spq.setTemplate(sql);
			spq.addVariable(user);
			spq.addVariable(app);
				String md5 = setDigest(user,passwd);

			 			passwd = passwdDigest(passwd,digest);

			spq.addVariable(passwd);
			spq.addVariable(md5);

			 spq.execute();

			}

			finally{
				DBConnectionMgr.close(spq);
			}

			}

		public  void deleteUser(String app,String user)throws Exception
		{
									SPQueryAndUpdate spq = new SPQueryAndUpdate(DBConnectionMgr.getConnection(app));
			try{
		String sql = "DELETE FROM shaftuser where shaftuser = ? AND app = ?";

			spq.setTemplate(sql);
			spq.addVariable(user);
			spq.addVariable(app);
			 spq.execute();

			}

			finally{
				DBConnectionMgr.close(spq);
			}
			deletePasswd(app,user);
			
			RealmMgr.getRealmMgr(app).deleteRealms(app,user);
		}

		public void updatePasswd(String app,String user,String passwd)throws Exception
		{

						SPQueryAndUpdate spq = new SPQueryAndUpdate(DBConnectionMgr.getConnection(app));
			try{
		String sql = "UPDATE shaftpasswd SET shaftpassword = ?, md5 = ? where shaftuser = ? AND app = ?";
			String md5 = setDigest(user,passwd);
			spq.setTemplate(sql);
					NameValuePairs ui = getUserInfo(app,user);
			String digest = ui.getString("digest");
			 passwd = passwdDigest(passwd,digest);
			spq.addVariable(passwd);
			spq.addVariable(md5);

			spq.addVariable(user);
			spq.addVariable(app);

			 spq.execute();

			}

			finally{
				DBConnectionMgr.close(spq);
			}
		}

		  void deletePasswd(String app,String user)throws Exception
		{
									SPQueryAndUpdate spq = new SPQueryAndUpdate(DBConnectionMgr.getConnection(app));
			try{
		String sql = "DELETE FROM shaftpasswd where shaftuser = ? AND app = ?";

			spq.setTemplate(sql);
			spq.addVariable(user);
			spq.addVariable(app);
			 spq.execute();

			}

			finally{
				DBConnectionMgr.close(spq);
			}
		}




}

