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

public class DBAccess extends ServerAccess {

	public  DBAccess(String a){
		super(a);
	}

	/*String app = null;
	public  DBAccess(String a){
		app = a;
	}

	public String action = null;*/
	public Vector<String> roles = null;
	//ShaftRestConfig.getDefaultRoles(app);

	public void setRoles(String l)throws ShaftRestException{
		roles = SharedMethods.toVector(l,",");
		if (roles.contains(ShaftRestConfig.ownerRole) && !roles.contains(ShaftRestConfig.memberRole))
			roles.add(ShaftRestConfig.memberRole);
	}
	public Vector<String> getRoles()throws Exception{
		return roles;
	}

/*		public void setAction(String ac)throws ShaftRestException{
				if (!DBServerObjs.actions.contains(ac))
			throw new ShaftRestException("unsupported_action:" + action);
			action = ac;

	}*/





}