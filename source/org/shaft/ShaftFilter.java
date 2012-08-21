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

package org.shaft;


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

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Hashtable;
import org.shaft.server.*;

import tools.util.*;
import org.shaft.utils.*;
import org.apache.catalina.comet.*;

public class ShaftFilter
  implements CometFilter,Filter
{

	ShaftServer shaftServer = null;
	ShaftRestServer restServer = null;

static Vector<String> forbidden = new  Vector<String>();
	static {
		forbidden.add("WEB-INF");
		forbidden.add("shaftapps");

	}

	Vector<String> ignorepaths = new  Vector<String>();


	  public void init(FilterConfig config)
    throws ServletException
  {

  		System.out.println("INIT SHAFT FILTER " + ShaftConfig.shaftversion);
  		try{
  		String ip = config.getInitParameter("ignorepaths");
  		if (ip != null)
  			ignorepaths = SharedMethods.toVector(ip,",",true);
/*
		ip = config.getServletContext().getRealPath("/");
		String ipl[] = new File(ip).list();
		if (ipl != null){
			for (int ct = 0; ct < ipl.length;ct++){
				if (!forbidden.contains(ipl[ct]) && !ignorepaths.contains(ipl[ct]))
					ignorepaths.add(ipl[ct]);
			}
		}*/

		String rootPath = config.getServletContext().getRealPath("/");
		shaftServer  = new ShaftServer(rootPath);
		restServer  = new ShaftRestServer(rootPath);

	}catch (Exception e){
		e.printStackTrace();
	}
  }





public void destroy(){
}


		String getTrimmedRequestURI(HttpServletRequest request)
	{

		String trimmedrequesturi = request.getRequestURI();
		if (trimmedrequesturi == null)return trimmedrequesturi;
		trimmedrequesturi = trimmedrequesturi.trim();
		int i = trimmedrequesturi.indexOf("?");
		if (i > -1)
			trimmedrequesturi = trimmedrequesturi.substring(0,i);
		i = trimmedrequesturi.indexOf(";jsessionid=");
		if (i > -1)
			trimmedrequesturi = trimmedrequesturi.substring(0,i);

		return trimmedrequesturi;
	}
		
		
public void doFilterEvent(CometEvent event,
        CometFilterChain chain)
throws java.io.IOException,
       javax.servlet.ServletException
       {
		HttpServletRequest request = event.getHttpServletRequest();
	  		HttpServletResponse response = event.getHttpServletResponse();

			String tr = getTrimmedRequestURI(request);
			String cp = (request).getContextPath();

  	  	

			if (tr.startsWith(cp))
			{
				tr = tr.substring(cp.length(),tr.length());
			}
			if (tr.startsWith("/"))
				tr = tr.substring(1,tr.length());


			for (int ct = 0; ct < forbidden.size();ct++)
			{
				if(tr.startsWith(forbidden.elementAt(ct) + "/") || tr.equals(forbidden.elementAt(ct)) )
				{
					response.sendError(403);
					event.close();
					return;
				}
			}
			for (int ct = 0; ct < ignorepaths.size();ct++)
			{
				if(tr.startsWith(ignorepaths.elementAt(ct) + "/") || tr.equals(ignorepaths.elementAt(ct)) )
				{
		  	  		chain.doFilterEvent(event);
					return;
				}
			}



String route = restServer.getRequestRoute(tr,request.getHeader("Host"),request);
//("SHAFT ROUTE 1 " + route);
if (route == null)
route = shaftServer.getRequestRoute(tr,request.getHeader("Host"),request);
//("SHAFT ROUTE 2 " + route);

if (route != null){
 		String qq = request.getQueryString();
		if (qq != null)qq =  "?" + qq;
		else
			qq = "";
  	  			request.getRequestDispatcher(route + qq).forward(request,response);
  	  		event.close();
				return;
}


  	  		chain.doFilterEvent(event);	  		
	  		
	  		
	  		
       }

  public void doFilter(ServletRequest request,
                      ServletResponse response,FilterChain chain)
    throws ServletException, IOException
  {

   	  		HttpServletRequest hrequest = (HttpServletRequest)request;
  	  		HttpServletResponse hresponse = (HttpServletResponse)response;

			String tr = getTrimmedRequestURI(hrequest);
			String cp = (hrequest).getContextPath();

  	  	

			if (tr.startsWith(cp))
			{
				tr = tr.substring(cp.length(),tr.length());
			}
			if (tr.startsWith("/"))
				tr = tr.substring(1,tr.length());


			for (int ct = 0; ct < forbidden.size();ct++)
			{
				if(tr.startsWith(forbidden.elementAt(ct) + "/") || tr.equals(forbidden.elementAt(ct)) )
				{
					hresponse.sendError(403);
					return;
				}
			}
			for (int ct = 0; ct < ignorepaths.size();ct++)
			{
				if(tr.startsWith(ignorepaths.elementAt(ct) + "/") || tr.equals(ignorepaths.elementAt(ct)) )
				{
		  	  		chain.doFilter(request,response);
					return;
				}
			}



String route = restServer.getRequestRoute(tr,hrequest.getHeader("Host"),hrequest);
//("SHAFT ROUTE 1 " + route);
if (route == null)
route = shaftServer.getRequestRoute(tr,hrequest.getHeader("Host"),hrequest);
//("SHAFT ROUTE 2 " + route);

if (route != null){
 		String qq = hrequest.getQueryString();
		if (qq != null)qq =  "?" + qq;
		else
			qq = "";
  	  			request.getRequestDispatcher(route + qq).forward(request,response);
				return;
}






  	  		chain.doFilter(request,response);

  }








}

