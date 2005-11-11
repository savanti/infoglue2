/* ===============================================================================
 *
 * Part of the InfoGlue Content Management Platform (www.infoglue.org)
 *
 * ===============================================================================
 *
 *  Copyright (C)
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2, as published by the
 * Free Software Foundation. See the file LICENSE.html for more information.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
 * Place, Suite 330 / Boston, MA 02111-1307 / USA.
 *
 * ===============================================================================
 */

package org.infoglue.deliver.util;

import java.io.Serializable;
import javax.servlet.http.HttpServletRequest;

public final class BrowserBean implements Serializable
{
	private HttpServletRequest request = null;
	private String useragent           = null;
	private String languages           = null;
	private boolean netEnabled        = false;

	private boolean ie  	= false;
	private boolean ns7 	= false;
	private boolean ns6 	= false;
	private boolean ns4 	= false;
	private boolean opera  	= false;
	private boolean mozilla = false;
	private boolean safari  = false;
	private boolean firefox = false;
	
	private String os;
	private boolean isWindows = false;
	private boolean isMac = false;
	private boolean isLinux = false;
	
	
	public void setRequest(HttpServletRequest req)
	{
		this.request = req;
		this.useragent = request.getHeader("User-Agent");
		this.languages = request.getHeader("Accept-Language");

		if(this.languages != null)
			this.languages = this.languages.toLowerCase();

		if(useragent != null)
		{
			inituseragent();
			initOs();
		}
	}

	private void inituseragent()
	{
		String user = useragent.toLowerCase();
		
		if(user.indexOf("opera") != -1)
		{
			this.opera = true;
		}
		else if(user.indexOf("msie") != -1) 
		{
			this.ie = true;
		}
		else if(user.indexOf("safari") != -1)
		{
			this.safari = true;
		}
		else if(user.indexOf("gecko") != -1)
		{
			this.mozilla = true;
			if(user.indexOf("firefox") != -1)
			    this.firefox = true;
		}
		else if(user.indexOf("netscape/7") != -1)
		{
			this.ns7 = true;
		}
		else if(user.indexOf("netscape6") != -1)
		{
			this.ns6 = true;
		}
		else if(user.indexOf("mozilla") != -1)
		{
			this.ns4 = true;
		}

		if(user.indexOf(".net clr") != -1)
			this.netEnabled = true;
	}

	private void initOs() 
	{
	    String user = useragent.toLowerCase();
        if (user.indexOf("win") > -1)
        {
            this.isWindows = true;
            
            if (user.indexOf("windows 95") > -1 || user.indexOf("win95") > -1)
            {
                this.os = "Windows 95";
            } 
            else if (user.indexOf("windows 98") > -1 || user.indexOf("win98") > -1)
            {
                this.os = "Windows 98";
            } 
            else if (user.indexOf("windows nt 5.1") > -1 || user.indexOf("winnt") > -1)
            {
                this.os = "Windows XP";
            } 
            else if (user.indexOf("windows nt") > -1 || user.indexOf("winnt") > -1)
            {
                this.os = "Windows NT";
            } 
            else if (user.indexOf("win16") > -1 || user.indexOf("windows 3.") > -1)
            {
                this.os = "Windows 3.x";
            }
            else
                this.os = "Windows";

        } 
        else if (user.indexOf("mac") > -1)
        {
            this.isMac = true;
            
            if (user.indexOf("mac_powerpc") > -1 || user.indexOf("mac_ppc") > -1)
            {
                this.os = "macintosh power pc";
            } 
            else if (user.indexOf("macintosh") > -1)
            {
                this.os = "Macintosh";
            } 
            else
            {
                this.os = "Mac";
            }
        }
        else if (user.indexOf("inux") > -1)
        {
            this.isLinux = true;
            this.os = "Linux";
        }
        else
        {
            this.os = "Other";
        }
    }	
	
	public String getUseragent()
	{
		return useragent;
	}

	public String getLanguageCode()
	{
		return this.languages;
	}

	public boolean isNetEnabled()
	{
		return netEnabled;
	}

	public boolean isIE()
	{
		return ie;
	}

	public boolean isNS7()
	{
		return ns7;
	}

 	public boolean isNS6()
	{
		return ns6;
	}

	public boolean isNS4()
	{
		return ns4;
	}
	
    public String getOs()
    {
        return os;
    }
    
    public boolean isMozilla()
    {
        return mozilla;
    }
    
    public boolean isNs4()
    {
        return ns4;
    }
    
    public boolean isNs6()
    {
        return ns6;
    }
    
    public boolean isNs7()
    {
        return ns7;
    }
    
    public boolean isOpera()
    {
        return opera;
    }
    
    public boolean isSafari()
    {
        return safari;
    }
    
    public boolean isLinux()
    {
        return isLinux;
    }
    
    public boolean isMac()
    {
        return isMac;
    }
    
    public boolean isWindows()
    {
        return isWindows;
    }
    
    public boolean isFirefox()
    {
        return firefox;
    }
}