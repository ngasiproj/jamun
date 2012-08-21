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

package org.shaft.compiler;
import java.sql.*;
import java.util.*;
import tools.util.*;
import java.io.*;
import javax.net.ssl.HttpsURLConnection;
import java.net.*;
import org.shaft.utils.*;

public class HtmlConverter extends DartCompiler
     {

     	void addPath(String cpl,StringBuffer cp){
     		String[] fcpl = new File(cpl).list();
for (int ct = 0;ct < fcpl.length;ct++)
{
	if (cp.length() > 0)
		cp.append(File.pathSeparator);
		cp.append(FileUtil.pathToOS(cpl + fcpl[ct]));

		//FileUtil.pathToOS
		//(cpl + fcpl[ct] + " CHEC KLIB " + new File(cpl + fcpl[ct]).isDirectory() + ":" + new File(FileUtil.pathToOS(cpl + fcpl[ct])).length());
		if (new File(cpl + fcpl[ct]).isDirectory())// && new File(FileUtil.pathToOS(cpl + fcpl[ct] + File.separator)).length() > 0)
			addPath(FileUtil.pathToOS(cpl + fcpl[ct] + File.separator),cp);
}
     	}
public  void doCompile(String app,String ds,String outd,String entr,String wdir)throws Exception{



String cpl = ShaftConfig.getBuildDir(app,"dartc") + "compiler/lib/";
StringBuffer cp = new StringBuffer();
addPath(cpl,cp);
/*String[] fcpl = new File(cpl).list();
for (int ct = 0;ct < fcpl.length;ct++)
{
	if (cp.length() > 0)
		cp.append(File.pathSeparator);
		cp.append(cpl + fcpl[ct]);
		if (new File(cpl + fcpl[ct]).isDirectory() && new File(cpl + fcpl[ct]).length() > 0)
			addPath(cpl + fcpl[ct],cp);
}*/

String jvm = System.getProperty("java.home");
if (jvm.endsWith("jre"))
jvm = jvm.substring(0,jvm.length() - 3);
jvm = jvm + "bin" + File.separator + "java";
if (SharedMethods.windows())
jvm = jvm + ".exe";



String cmd = jvm + " -ea -classpath " + cp + " com.google.dart.compiler.DartCompiler -noincremental --work " + wdir + " --out " + outd + " " + entr;
System.out.println(SharedMethods.windows() + " DARTC Compile "+ cmd);
	 ProcessBuilder pb = null;
	 if (ShaftConfig.getAppConfig(app).getBoolean("dart.compiler.dartc.optimize") && !SharedMethods.windows())
	 {
	/*if (SharedMethods.windows()){
			 pb = new ProcessBuilder(ShaftConfig.shafthome + "bin" + File.separator + ShaftConfig.getDartVersion(app) + ".bat",  jvm,wdir,outd,entr,"--optimize");
			 System.out.println(ShaftConfig.shafthome + "bin" + File.separator + ShaftConfig.getDartVersion(app) + ".bat" + " " +   jvm + " " +wdir + " " +outd + " " +entr+ " " +"--optimize");

	}else{
		*/

	 pb = new ProcessBuilder(jvm,"-ea","-classpath",cp.toString(),"com.google.dart.compiler.DartCompiler","-noincremental","--optimize","--work",wdir,"--out",outd,entr);
	//}
	 }
	else{
		/*		if (SharedMethods.windows()){
			 pb = new ProcessBuilder(ShaftConfig.shafthome + "bin" + File.separator + ShaftConfig.getDartVersion(app) + ".bat",  jvm,wdir,outd,entr);
			 System.out.println(ShaftConfig.shafthome + "bin" + File.separator + ShaftConfig.getDartVersion(app) + ".bat" + " " +   jvm + " " +wdir + " " +outd + " " +entr);

	}else*/
	 pb = new ProcessBuilder(jvm,"-ea","-classpath",cp.toString(),"com.google.dart.compiler.DartCompiler","-noincremental","--work",wdir,"--out",outd,entr);
	}
	 pb = pb.redirectErrorStream(true);
	 Process pp = pb.start();
	NProcess p = new NProcess(pp);
					String ret = p.getStreamsString(30);
					p.waitFor();
					String ret2 = p.getStreamsString(30);
					if (ret2 != null)
					{
						if (ret == null)ret = "";
						ret = ret + ret2;
					}
					if (StringUtil.isRealString(ret) && ! new File(outd).exists())
						throw new ShaftException(ret);


		}




}
