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

public class Frog extends DartCompiler
     {


		public  void doCompile(String app,String ds,String outd,String entr,String wdir)throws Exception{
	String pyt = (String)(ShaftConfig.getAppConfig(app)).get("dart.compiler.frog.python");
	if (pyt == null || !new File(pyt).exists())
		ShaftException.throwException("python_not_found");
	 ProcessBuilder pb = new ProcessBuilder(pyt,ShaftConfig.getBuildDir(app,"frog") + "frog/bin/frog","--compile-only","--out=" + outd,ds);
	 pb.directory(new File(ShaftConfig.shafthome + "dart/" + ShaftConfig.getDartVersion(app)));
	 pb = pb.redirectErrorStream(true);
	 Process pp = pb.start();
	NProcess p = new NProcess(pp);
					String ret = p.getStreamsString(10);
					p.waitFor();
					if (StringUtil.isRealString(ret) && ret.indexOf("Compilation succeded") < 0)
						ShaftException.throwException("frog_compile_error",ret);


		}




}
