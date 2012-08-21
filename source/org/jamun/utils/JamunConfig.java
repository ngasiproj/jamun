/*
 Copyright (c) 2011-2012, the Jamun project (http://www.jamunapp.org). All rights reserved.
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

Version 0.1


*/
package org.jamun.utils;

import org.json.simple.parser.*;
import org.json.simple.*;

import tools.util.FileUtil;
import tools.util.NameValuePairs;
import java.util.Map;

public class JamunConfig{
		public static String jamunroot = null;

		public static long requestTimeout = 5 * 60 * 1000;
		public static NameValuePairs defconfigmodnvp = null;
		public static String jamunconfigd = "WEB-INF/jamun/";	
		public static String version = "0.1";
		public static String ePath = "/_errorpages/";
		public static void init(String t){
			jamunroot = t;
			try{

				requestTimeout = getDefaultConfig().getLong("request.timeout")  * 60 * 1000;

			}catch (Exception e){
				e.printStackTrace();
			}
		}

	


				public static NameValuePairs getDefaultConfig()throws Exception{
			if (defconfigmodnvp == null || defconfigmodnvp.getString("deployment.mode").equals("development") ){
	try{
				String rts = FileUtil.getStringFromFile(jamunroot + jamunconfigd + "server.json");
				  JSONParser parser=new JSONParser();
				  Object obj=parser.parse(rts);
				  defconfigmodnvp = new NameValuePairs((Map)obj);

				}catch (Exception e){
					e.printStackTrace();
					new JamunException(e).throwIt();
				}

						}
			return defconfigmodnvp;
		}

		


}
