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
import tools.util.NameValuePairs;
public class DBField {
	public String dateformat = null;
	public String name = null;
	public String dataType = ShaftRestConfig.defaultDataType;
		public  int  dataLength = ShaftRestConfig.defaultDataLength;
	public  boolean  notNull = ShaftRestConfig.defaultNotNull;
	public Object defaultValue = null;
	//public String shaftid = ShaftRestConfig.shaftid;
	//public Object getDataTypeValue
	
	NameValuePairs params = new NameValuePairs();
	
	public void setName(String n){
		name = n;
		int i = n.indexOf("(");
		if (i > 0){
			name = n.substring(0,i).trim();
			n = n.substring(i + 1,n.length()).trim();
			n = n.substring(0,n.length() -1);
			//NameValuePairs fp = new NameValuePairs();
			params.fromDelimitedString(n,"=",",");
			//params.put(name, fp);
		
		}
	}
	
	public void setDataType(String n){
		dataType = n;
		int i = n.indexOf("(");
		if (i > 0){
			dataType = n.substring(0,i).trim();
			n = n.substring(i + 1,n.length()).trim();
			n = n.substring(0,n.length() -1);
			//NameValuePairs fp = new NameValuePairs();
			params.fromDelimitedString(n,"=",",");
			//params.put(name, fp);
		
		}
	}
	
	public boolean isString(){
		if (dataType == null)return false;
		if (dataType.equals("string") || dataType.equals("text") || dataType.equals(ShaftRestConfig.userobjname) || dataType.equals(ShaftRestConfig.shaftid) || dataType.equals(ShaftRestConfig.shaftrealm) || dataType.equals(ShaftRestConfig.shaftowner) || dataType.equals("email") || dataType.equals("phone"))
			return true;
		return false;
	}

		public boolean isDateTime(){
		if (dataType == null)return false;
		if (dataType.equals("date") || dataType.equals("time") || dataType.equals(ShaftRestConfig.shafttimestamp) ||  dataType.equals("datetime") || dataType.equals("timestamp"))
			return true;
		return false;
	}

		public boolean isNumber(){
		if (dataType == null)return false;
		if (dataType.equals("number") || dataType.equals("num") || dataType.equals("int") || dataType.equals("integer"))
			return true;
		return false;
	}

			public boolean isBoolean(){
		if (dataType == null)return false;
		if (dataType.equals("boolean") || dataType.equals("bool") )
			return true;
		return false;
	}
	public Object getValue()throws Exception{
		return getValue(defaultValue);
	}

		public Object getValue(Object val)throws Exception{
		if (val != null){
			if (val.toString().equalsIgnoreCase("null"))
				return null;
		if (isNumber())
			return new Integer(val.toString());
		else if (isDateTime())
			return org.shaft.server.rest.RestAPI.getDateFormat(dateformat).parse(val.toString());
				//new Date(val.toString());

		else if (isBoolean()){
			if (val.toString().equals("true") || val.toString().equals("1"))
				return true;
			return false;
		}

		}

		return val;
	}


}