/**
 * Copyright (c) 2010-2012, Christopher J. Kucera
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Minecraft X-Ray team nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL VINCENT VOLLERS OR CJ KUCERA BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.apocalyptech.minecraft.xray;

import java.util.Vector;
import java.util.Properties;
import java.util.Enumeration;
import java.util.Collections;

/**
 * Class to provide a sorted properties list in our config file,
 * with a couple of extra functions that I want.
 *
 * The sorting functionality in here taken from
 * http://www.rgagnon.com/javadetails/java-0614.html
 */
public class XRayProperties extends Properties
{

	// Added at the behest of Eclipse (or, well, presumably Java itself)
	private static final long serialVersionUID = 2578311914423692774L;

	/**
	 * Overrides, called by the store method.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public synchronized Enumeration<Object> keys()
	{
		Enumeration keysEnum = super.keys();
		Vector keyList = new Vector();
		while (keysEnum.hasMoreElements())
		{
			keyList.add(keysEnum.nextElement());
		}
		Collections.sort(keyList);
		return keyList.elements();
	}

	/**
	 * Reads a boolean property, with the given default value
	 * in case it's not found.
	 */
	public boolean getBooleanProperty(String keyName, boolean defaultValue)
	{
		String propValue = this.getProperty(keyName);
		if (propValue != null && propValue.length() > 0)
		{
			String propLetter = propValue.substring(0, 1);
			return (propLetter.equalsIgnoreCase("y") ||
					propLetter.equalsIgnoreCase("t") ||
					propLetter.equals("1"));
		}
		else
		{
			return defaultValue;
		}
	}

	/**
	 * Sets a boolean property.  Basically just translates a
	 * boolean to "1" or "0"
	 */
	public void setBooleanProperty(String keyName, boolean propValue)
	{
		if (propValue)
		{
			this.setProperty(keyName, "1");
		}
		else
		{
			this.setProperty(keyName, "0");
		}
	}

	/**
	 * Gets an integer property, with a default vaule if it's not found.
	 */
	public int getIntProperty(String propName, int defaultValue)
	{
		String val = this.getProperty(propName);
		if (val == null)
		{
			return defaultValue;
		}
		else
		{
			return Integer.valueOf(val);
		}
	}

	/**
	 * Sets an integer property (basically just wraps Integer.toString()
	 */
	public void setIntProperty(String propName, int propVal)
	{
		this.setProperty(propName, Integer.toString(propVal));
	}
}
