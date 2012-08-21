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
package org.shaft.server.utils;
import java.util.Vector;
import tools.util.*;
import org.shaft.utils.*;

public class ShaftRestConfig{
	//auth
	public static String defaultSessionDigest = "SHA";

	public static String realmobjname = "shaftrealm";
	public static String shaftrealm = realmobjname;
	public static String userobjname = "shaftuser";
	public static String userobjpassword = "shaftpassword";
	public static String userobjsessionid = "shaftsessionid";
	public static String shaftusermgrpath = "shaftusermgr";
	public static String shaftbatchmgrtype = "bmr";
	public static String batchmgrpath = "batchmgr";
	public static String shaftusermgrtype = "umr";
	public static String shaftrealmmgrpath = "shaftrealmmgr";
	public static String shaftrealmmgrtype = "rmr";
	
	//public static String usersessionobjroles = "shaftroles";

	//db
	//attributes
	public static String shaftroot = null;
	public static String shaftapps = "";
	public static String shaftrest = "/shaftrest/";
	public static String defaultDataType = "string";
	public static int  defaultDataLength = 200;
	public static boolean  defaultNotNull = false;
	public static boolean  forceNamesToLowerCase = true;

	public static boolean prependAppNameToTable = true;
	public static String datasourceName = "shaft";
	public static String datasourceRName = "shaft";
	public static String shaftid = "shaftid";
	public static String dataTypes = "text,color,phone,email,string,shaftid,shaftuser,number,num,bool,boolean,date,time,datetime,timestamp,shaftowner,shafttimestamp,shaftrealm";
	public static int  defaultRowIDLength = 8;
	public static int  maxAddRowAttempts = 5;
	public static String shaftowner = "shaftowner";
	public static int shaftownerlength = 150;
	public static int shaftrealmlength = 150;

	public static String shafttimestamp = "shafttimestamp";




	//access
	public static String allRole = "all";
	public static String ownerRole = "owner";
	public static String memberRole = "member";
	public static String guestRole = "guest";
	public static String guestUser = "guest";


//	public static Vector<String> defaultRoles = new Vector<String>();

	public static String newExt = ".new";
	public static String digest = "digest";
	public static String logintype = "logintype";
	public static String role = "role";

	public static String caninvite = "caninvite";
	public static String secretmembers = "secretmembers";

	public static NameValuePairs defconfigmodnvp = null;
	public static String restStatus = "status";
	public static String restResponse = "response";
	public static String restOK = "ok";
	public static String restFAIL = "fail";
	public static String restErrorCode = "code";
	public static String restStackTrace = "stacktrace";
	public static boolean showRestStackTrace = true;
	public static String realmName = "Shaft";
	public static String production = "production";
	public static String development = "development"; 
	
	public static long requestTimeout = 5 * 60 * 1000;
	
	public static String ePath = "/_errorpages/";

//	public static Vector<String> getDefaultRoles(String app){
//		return defaultRoles;
//	}

	static{
		try{
			datasourceName = getDefaultConfig().getString("datasource.name");
			if (getDefaultConfig().get("datasource.read.name") != null)
			datasourceRName = getDefaultConfig().getString("datasource.read.name");
			else datasourceRName = datasourceName;
			realmName = getDefaultConfig().getString("realmName");
			requestTimeout = getDefaultConfig().getLong("request.timeout")  * 60 * 1000;
			if (getDefaultConfig().get("errorpages.path") != null)
				ePath = getDefaultConfig().getString("errorpages.path");
		}catch (Exception e){
			e.printStackTrace();
		}
	}

public static NameValuePairs getAppConfig(String app)throws Exception{
	return getDefaultConfig();
}


			public static NameValuePairs getDefaultConfig()throws Exception{
		if (defconfigmodnvp == null || defconfigmodnvp.getString("deployment.mode").equals("development") ){
try{
			defconfigmodnvp = new NameValuePairs(ShaftConfig.shaftroot + ShaftConfig.shaftconfigd + "server.properties");


			}catch (Exception e){
				e.printStackTrace();
				new ShaftRestException(e).throwIt();
			}

					}
		return defconfigmodnvp;
	}



}