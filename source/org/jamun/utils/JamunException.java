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

import org.shaft.utils.ShaftException;

public class JamunException extends Exception{

	
	
	public JamunException(String e){
		super(e);

	}
		public JamunException(Exception e){
		super(e);
	}
	public static void throwException(String e)throws JamunException{
		throwException(new JamunException(e));
	}
		public static void throwException(String e,String msg)throws JamunException{
		throwException(new JamunException(msg));
	}
	public static void throwException(Exception e)throws JamunException{
		if (e instanceof JamunException)
			throw (JamunException)e;
		throw new JamunException(e.toString());
	}

		public void throwIt()throws JamunException{
			throw this;
	}
	
	
}
