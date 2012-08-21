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
package org.shaft.server.rest;

import com.sun.jersey.api.container.MappableContainerException;
import java.io.IOException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.Map;
import java.util.Hashtable;
import javax.ws.rs.core.MediaType;
import tools.util.*;
import com.google.gson.*;
import java.io.File;
import javax.servlet.http.HttpServletRequest;
import org.shaft.server.objs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.*;
import com.sun.jersey.*;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.*;
import org.shaft.server.utils.*;

@Path("/")
public class RestAPI {

		static Hashtable<String,SimpleDateFormat> sdfs = new Hashtable<String,SimpleDateFormat>();
		public static SimpleDateFormat getDateFormat(String df){
						SimpleDateFormat sdf = sdfs.get(df);
			if (sdf == null){
				sdf = new SimpleDateFormat(df);
				sdfs.put(df,sdf);
			}
			return sdf;
		}

	    static private class sqlTimestampConverter implements JsonSerializer<Timestamp> {
        //static SimpleDateFormat sdf = new SimpleDateFormat("'Date('yyyy-MM-dd'T'HH:mm:ss.SSSZ')'");
		String df = null;
		sqlTimestampConverter(String d){
			df = d;
		}

        //@Override
        public JsonElement serialize(Timestamp src, Type srcType,
JsonSerializationContext context) {
			SimpleDateFormat sdf = RestAPI.getDateFormat(df);
				/*sdfs.get(df);
			if (sdf == null){
				sdf = new SimpleDateFormat(df);
				sdfs.put(df,sdf);
			}*/
            return new JsonPrimitive(sdf.format(src));
        }
    }

//        GsonBuilder gson = new GsonBuilder().setDateFormat("'Date('yyyy-MM-
//dd'T'HH:mm:ss.SSSZ')'");
  //      gson.registerTypeAdapter(Timestamp.class, new sqlTimestampConverter());


    public Response generateResponse(String entity, MediaType mediaType) {
        return Response.ok(entity, mediaType).build();
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response process(@Context HttpServletRequest request)throws Exception
	 {
	  	        	String sot = (String)request.getAttribute("shaftobjtype");
	ServerObjsInterf sob = ServerObjFactory.create(sot);
					Hashtable re = new Hashtable();
					try{
					Object resp = null;
				//	try{
					/*
					public static String restStatus = "status";
	public static String restResponse = "response";
	public static String restOK = "ok";
	public static String restFAIL = "fail";
	public static String restErrorCode = "code";
	public static String restStackTrace = "stacktrace";
	public static boolean showRestStackTrace = true;


					*/
					resp = sob.process(sot,request);
					re.put(ShaftRestConfig.restStatus,ShaftRestConfig.restOK);
					re.put(ShaftRestConfig.restResponse,resp);

					}catch (Exception e){
						e.printStackTrace();
					re.put(ShaftRestConfig.restStatus,ShaftRestConfig.restFAIL);
					String ec = e.getMessage();
					if(ec != null)
						re.put(ShaftRestConfig.restErrorCode,ec);
					if (ShaftRestConfig.showRestStackTrace)
					{
						re.put(ShaftRestConfig.restStackTrace,SharedMethods.getTraceString(e));
					}

					}
				/*	}catch (org.shaft.server.auth.ShaftAuthException e){
		  e.printStackTrace();
		        return Response.status(Response.Status.FORBIDDEN).build();

					}*/
  			Gson gson = new GsonBuilder().registerTypeAdapter(Timestamp.class, new sqlTimestampConverter(sob.getDateFormat(request))).create();
 //gson.registerTypeAdapter(Timestamp.class, new sqlTimestampConverter());
          return generateResponse(gson.toJson(re),
                MediaType.APPLICATION_JSON_TYPE);
               }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response processP(@Context HttpServletRequest request)throws Exception
	 {
	  	        	return process(request);
}

}