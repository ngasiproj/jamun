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

package org.shaft.utils;
import tools.util.*;
import java.io.*;
import java.util.*;

public class ShaftConfig{
		public static String shaftversion = "v.4-b08142012A";
		public static String shaftroot = null;
	public static String shaftconfigd = "WEB-INF/shaft/";

	public static String shaftapps = "";
	public static String apptconfigd =  "config";
	public static String dartjsextention = ".dart.js";
	public static String dartextention = "-js.html";
	public static String htmlextention = ".html";
	public static String shafthome = "/usr/shaft/";
	//public static String defaultdartversion = "b12062011-2012";
	public static int compilewait = 120;
	public static long defconfigmod = 0;
	public static NameValuePairs defconfigmodnvp = null;
	public static NameValuePairs appconfigmod = new NameValuePairs();
	public static Hashtable<String,NameValuePairs> appconfigmodnvp = new Hashtable<String,NameValuePairs>();
	public static String ePath = "/_errorpages/";

	static{
		if (SharedMethods.windows())
			shafthome = "C:\\usr\\shaft\\";
	}

	public static String getTempDir(){
		return System.getProperty("java.io.tmpdir") + File.separator;
	}
		public static String getTempDir(String app){
		String atmp =  getTempDir() + app + File.separator;
		new File(atmp).mkdirs();
		return atmp;
	}

		public static String getBuildDir(String app,String c)throws ShaftException{
		String	bd = shafthome + "dart/" + getDartVersion(app) + File.separator + "out/" + getAppConfig(app).get("dart.compiler." + c + ".build.type") + "/";
//if (!new File(bd).exists())
//	bd = shafthome + "dart/" + getDartVersion(app) + File.separator + "out/Debug_ia32/";
	return bd;
		}
	public static String getDartVersion(String app)throws ShaftException{
		return (String)(getAppConfig(app)).get("dart.version");
	}
	public static boolean isDefaultConfigModified(){
		return (new File(shaftroot + shaftconfigd + "app.properties").lastModified() > defconfigmod);
	}

		public static String getDefaultConfigFile(){
		return shaftroot + shaftconfigd + "app.properties";
	}
		public static String getAppConfigFile(String app){
		return shaftroot + shaftapps + File.separator + app  + File.separator + apptconfigd + File.separator + "app.properties";
	}
		public static boolean isAppConfigModified(String app){
		return (new File(getAppConfigFile(app)).lastModified() > appconfigmod.getLong(app));
	}
	public static NameValuePairs getAppConfig(String app)throws ShaftException{
		if (!new File(getAppConfigFile(app)).exists()){
			return getDefaultConfig();
		}
		NameValuePairs appnvp = null;
		if (isAppConfigModified(app) || isDefaultConfigModified()){
try{
			NameValuePairs dnvp = getDefaultConfig();
			appnvp = (NameValuePairs)dnvp.clone();

			appnvp.merge(new NameValuePairs(getAppConfigFile(app)));
			long appmod = new File(getAppConfigFile(app)).lastModified();

			appconfigmod.put(app,appmod);
			appconfigmodnvp.put(app,appnvp);

			}catch (Exception e){
				e.printStackTrace();
				ShaftException.throwException(e);
			}

					}
		return appconfigmodnvp.get(app);
	}

		public static NameValuePairs getDefaultConfig()throws ShaftException{
		if (defconfigmodnvp == null || isDefaultConfigModified()){
try{
			defconfigmodnvp = new NameValuePairs(shaftroot + shaftconfigd + "app.properties");


			defconfigmod = new File(shaftroot + shaftconfigd + "app.properties").lastModified();
			}catch (Exception e){
				e.printStackTrace();
				ShaftException.throwException(e);
			}

					}
		return defconfigmodnvp;
	}

}