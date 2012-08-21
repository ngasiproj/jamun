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
package org.shaft.server.utils;

import java.sql.Connection;
import tools.util.*;
import java.util.Hashtable;

public class DBConnectionMgr{

	public static long getThreadID(){
		
		return Thread.currentThread().getId();
	}
	
	static Hashtable<Long,Boolean> batch = new Hashtable<Long,Boolean>();
	static Hashtable<Long,Connection> batchc = new Hashtable<Long,Connection>();
	
	public static Connection getConnection(String app)throws Exception{
		long tid = getThreadID();
		Connection c = batchc.get(tid);
		if (c != null){
			System.out.println("FROM CACHE ");
			return c;
		}
		c =  DBConMgr.getConnection(ShaftRestConfig.datasourceName);
		if (batch.remove(tid) != null){
			c.setAutoCommit(false);
			batchc.put(tid, c);
		}
		return c;
	}

	public static Connection getRConnection(String app)throws Exception{
		long tid = getThreadID();
		if (batch.get(tid) != null || batchc.get(tid) != null)
			return getConnection(app);
		return DBConMgr.getConnection(ShaftRestConfig.datasourceRName);
	}
	
	public static void close(SPQueryAndUpdate spq)throws Exception{
		if (spq != null){
			long tid = getThreadID();
			Connection c = batchc.get(tid);
			if (c != null && c.equals(spq.getConnection()))
			{
				System.out.println("NO CLOSE");
				spq.nofinalize = true;
				return;
			}
			spq.nofinalize = false;
			spq.close();
			
		}
	}
	public static void closeR(SPQueryAndUpdate spq)throws Exception{
		//if (spq != null)spq.close();
		close(spq);
	}
	
	public static void startBatch()throws Exception{
		batch.put(getThreadID(), true);
		
	}
	public static void release(boolean err)throws Exception{
		long tid = getThreadID();
		//batch.remove(tid);
		Connection c = batchc.remove(tid);
		if (c != null){
			if (!err)
				c.commit();
			else
				c.rollback();	
			c.close();
			System.out.println("RELEASED! " + err);
		}
		else
			batch.remove(tid);
	}
}