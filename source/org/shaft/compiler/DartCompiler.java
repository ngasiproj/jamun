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
import org.shaft.utils.*;
import tools.util.*;
import java.util.*;
import java.io.*;

public abstract class DartCompiler
     {



		public static DartCompiler getCompiler(String app)throws ShaftException{
		String c = ShaftConfig.getAppConfig(app).getString("dart.compiler");
		if (c.equals("dartc"))
		return new HtmlConverter();
		else if (c.equals("frog"))
		return new Frog();

		else {
			ShaftException.throwException("dart_compiler_not_supported");
		}
		return null;
		}



static Hashtable<String,String> dsrces = new Hashtable<String,String>();

	public String getDartSource(String app,String js,String html)throws ShaftException{
try{

		String ds = dsrces.get(app);
		if (ds != null)
			return ds;
		js = FileUtil.pathToOS(js);
int i = js.lastIndexOf(File.separator);

String outd = js.substring(0,i+1);
Template t = new Template(html);
String st1 = "<script type=\"application/dart\" src=\"";
i = t.indexOf(st1);
if (i < 0)
throw new ShaftException("reference_to_dart_script_not_found");
String dn = t.toString().substring(i + st1.length(), t.toString().length() );
String st2 = "\"></script>";
i = dn.indexOf(st2);
if (i < 0)
throw new ShaftException("reference_to_end_dart_script_tag_not_found");
dn = dn.substring(0,i);
	dsrces.put(app,outd + dn);
	return outd + dn;
}catch (Exception e){

	e.printStackTrace();
	if (e instanceof ShaftException)
		throw (ShaftException)e;
		throw new ShaftException(e.toString());
}

	}

		public  void compile(String app,String js,String html,boolean isst)throws ShaftException{
try{

dsrces.remove(app);
String wdir = ShaftConfig.getTempDir(app);
		js = FileUtil.pathToOS(js);

int i = js.lastIndexOf(File.separator);
String outd = js.substring(0,i+1);
String das = null;
String ds = null;
Template t = null;
String dn = null;
if (!isst){
t = new Template(html);
String st1 = "<script type=\"application/dart\" src=\"";
i = t.indexOf(st1);
if (i < 0)
throw new ShaftException("reference_to_dart_script_not_found");
dn = t.toString().substring(i + st1.length(), t.toString().length() );

String st2 = "\"></script>";
i = dn.indexOf(st2);
if (i < 0)
throw new ShaftException("reference_to_end_dart_script_tag_not_found");
dn = dn.substring(0,i);

if (!new File(outd + dn).exists())
throw new ShaftException("dart_source_not_found");
das = outd + dn;
}
else
das = js;
Template te = new Template (ShaftConfig.shafthome + "dart/" +  File.separator + "entry.dart");

if (SharedMethods.windows())
das = new File(das).toURI().toURL().toString();
te.setTag("#DART_SRC#",das);
if (!isst)
ds = outd + dn;
else
ds = js;
String entr = wdir + "entry.dart";
te.store(entr);
wdir = wdir + "work";
FileUtil.deleteAll(wdir);
new File(wdir).mkdirs();
if (!isst)
outd = outd + dn.substring(0,dn.length() - 4) + "js";
else
outd = html;
//(" OUTDIFIL ADFRT COMPILE " + outd );
new File(outd).delete();
doCompile(app,ds,outd,entr,wdir);
if (!new File(outd).exists())
throw new ShaftException("dart_compile_failed");

if (!isst){
t.setTag(dn,dn.substring(0,dn.length() - 4) + "js");
t.setTag("type=\"application/dart\"","");
t.store(js);
dsrces.put(app,ds);
}

}catch (Exception e){

	e.printStackTrace();
	if (e instanceof ShaftException)
		throw (ShaftException)e;
		throw new ShaftException(e.toString());
}
		}





		public abstract  void doCompile(String app,String ds,String outd,String entr,String wdir)throws Exception;


}
