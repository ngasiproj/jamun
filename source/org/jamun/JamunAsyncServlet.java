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

package org.jamun;


import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.servlet.AsyncContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletContext;
import javax.servlet.UnavailableException;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;

import org.jamun.utils.JamunConfig;
import org.jamun.utils.JamunUtil;
import org.jamun.views.ViewFactory;

import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Vector;
import java.io.File;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Hashtable;
import tools.util.*;


public class JamunAsyncServlet
extends HttpServlet
{

    public void init()
    throws ServletException {

    super.init();
   
    
  		String rootPath = getServletConfig().getServletContext().getRealPath("/");
  		JamunConfig.init(rootPath);
  		JamunUtil.log(" Jamun Version  " + JamunConfig.version);
		//CaimitoConfig.init();

}
    
    protected void renderError(HttpServletRequest req,int c){
    //	HttpServletResponse response = (HttpServletResponse)req.getAsyncContext().getResponse();
    //	response.setStatus( c );
    //	response.setContentType("text/html" );
    	renderPage(req,c,JamunConfig.ePath + c + ".html");  	
    }
    
    protected void renderPage(HttpServletRequest req,int c,String p){
    	HttpServletResponse response = (HttpServletResponse)req.getAsyncContext().getResponse();
    	response.setStatus( c );
    	response.setContentType("text/html" );
		req.getAsyncContext().dispatch(p);  	
    }
    protected void performService(final HttpServletRequest req, final HttpServletResponse resp )
    throws ServletException, IOException {
	try{

    //final String path = getRelativePath(req);

		String r = ViewFactory.getView().getRequestRoute(req);
		
	//	((HttpServletResponse)req.getAsyncContext().getResponse()).sendError(404,"BAD REW");
		//HttpServletResponse response = (HttpServletResponse)req.getAsyncContext().getResponse();
		//response.setStatus( 404 );
		//response.getOutputStream().write( "BAD REQUEST".getBytes() );
		
		//req.getAsyncContext().dispatch("/_errorpages/404.html");
		//response.getOutputStream().flush();
		
		if (r != null)
			renderPage(req,200,r);
		//resp.getWriter().println("APP " + r);
		else
			renderError(req,404);
			//resp.getWriter().println("NOAPP " + r);*/
    } catch (Exception e) {
  	  e.printStackTrace();
  	renderError(req,500);
  	  
    } 



}
    
    
    protected void service(final HttpServletRequest req, final HttpServletResponse resp)
    throws ServletException, IOException {

  //final  AsyncContext ac = req.getAsyncContext(); 
  //("hello 1 " + req.isAsyncStarted());
  if (req.isAsyncStarted()){
	  performService(req,resp);	
	  return;
  }
  //( " hello 2" + req.isAsyncStarted());   
  
  final  AsyncContext ac = req.startAsync();
  ac.setTimeout(JamunConfig.requestTimeout);
  ac.start(new Runnable() { 
	
        public void run() {  
            try { 
            	//("rHELLO 2 ");
            	performService(req,resp);
            } catch (Exception e) {
          	  e.printStackTrace();
          	  
            } 
            finally{
            	if (req.isAsyncStarted() )
            	  ac.complete();
              }
        }
            
            });
}





}

