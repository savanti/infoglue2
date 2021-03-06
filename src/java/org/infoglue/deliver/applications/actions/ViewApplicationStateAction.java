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

package org.infoglue.deliver.applications.actions;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.net.InetAddress;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.pluto.PortletContainerServices;
import org.apache.pluto.portalImpl.services.ServiceManager;
import org.apache.pluto.portalImpl.services.portletentityregistry.PortletEntityRegistry;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.ServerNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.WorkflowController;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.AuthenticationModule;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.CmsSessionContextListener;
import org.infoglue.cms.util.sorters.AverageInvokingTimeComparator;
import org.infoglue.deliver.applications.databeans.CacheEvictionBean;
import org.infoglue.deliver.cache.PageCacheHelper;
import org.infoglue.deliver.controllers.kernel.impl.simple.ExtranetController;
import org.infoglue.deliver.portal.ServletConfigContainer;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.CompressionHelper;
import org.infoglue.deliver.util.RequestAnalyser;

import com.opensymphony.oscache.base.Cache;
import com.opensymphony.oscache.base.CacheEntry;
import com.opensymphony.oscache.base.OSCacheUtility;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;
import com.opensymphony.oscache.web.ServletCache;
import com.opensymphony.oscache.web.ServletCacheAdministrator;

import webwork.action.ActionContext;

/**
 * This is the action that shows the application state and also can be used to set up surveilence.
 * The idea is to have one command which allways returns a known resultpage if it's ok. Otherwise it prints
 * an error-statement. This action is then called every x minutes by the surveilence and an alarm is raised if something is wrong.
 * We also have a command which can list more status about the application.
 *
 * @author Mattias Bogeblad
 */

public class ViewApplicationStateAction extends InfoGlueAbstractAction 
{
    private final static Logger logger = Logger.getLogger(ViewApplicationStateAction.class.getName());

    private List states 					= new ArrayList();
    private Map applicationMap 				= new HashMap();
    private Object cache					= null;
    private Object cacheMap					= null;
    private String cacheKey					= null;
    
	private boolean databaseConnectionOk 	= false;
	private boolean applicationSettingsOk 	= false;
	private boolean testQueriesOk			= false;
	private boolean diskPermissionOk 		= false;
	
	private String cacheName				= "";
	private boolean clearFileCache			= false;

	private String className				= "";
	private String logLevel					= "";

	private String attributeName			= "";
	private String returnAddress			= null;

	private static VisualFormatter formatter = new VisualFormatter();
	
	/**
	 * The constructor for this action - contains nothing right now.
	 */
    
    public ViewApplicationStateAction() 
    {
	
    }
    
    private Category getDeliverCategory()
    {
        Enumeration enumeration = Logger.getCurrentCategories();
        while(enumeration.hasMoreElements())
        {
            Category category = (Category)enumeration.nextElement();
            if(category.getName().equalsIgnoreCase("org.infoglue.deliver"))
                return category;
        }
        
        return null;
    }

    private Category getCastorJDOCategory()
    {
        Enumeration enumeration = Logger.getCurrentCategories();
        while(enumeration.hasMoreElements())
        {
            Category category = (Category)enumeration.nextElement();
            if(category.getName().equalsIgnoreCase("org.exolab.castor.jdo"))
                return category;
        }
        
        return null;
    }

    private Category getCategory(String className)
    {
        Enumeration enumeration = Logger.getCurrentCategories();
        while(enumeration.hasMoreElements())
        {
            Category category = (Category)enumeration.nextElement();
            if(category.getName().equalsIgnoreCase(className))
                return category;
        }
        
        Category category = Category.getInstance(className);
       
        return category;
    }

	private String handleAccess(HttpServletRequest request) throws Exception
	{
		String returnValue = null;
		
        boolean allowAccess = false;
        try
        {
	        if(!ServerNodeController.getController().getIsIPAllowed(this.getRequest()))
	        {
	        	Principal principal = getPrincipal();
	        	logger.info("principal:" + principal);
	        	if(principal == null)
	        		principal = (Principal)this.getHttpSession().getAttribute("infogluePrincipal");
	        	
	        	logger.info("principal:" + principal);
	        	Principal anonymousPrincipal = this.getAnonymousPrincipal();
	        	if(principal == null || principal.getName().equals(anonymousPrincipal.getName()))
	        	{
	            	this.getHttpSession().removeAttribute("infogluePrincipal");
	    		    this.getHttpSession().removeAttribute("infoglueRemoteUser");
	    		    this.getHttpSession().removeAttribute("cmsUserName");
	
	    		    String redirectUrl = getRedirectUrl(getRequest(), getResponse());								
	    			getResponse().sendRedirect(redirectUrl);
	    			returnValue = NONE;
	        	}
	        	else
	        	{
					if(AccessRightController.getController().getIsPrincipalAuthorized((InfoGluePrincipal)principal, "ViewApplicationState.Read", false, true))
					{
						allowAccess = true;
					}
					else
					{
			        	logger.warn("A user from an IP(" + this.getRequest().getRemoteAddr() + ") and username [" + principal + "] was denied access to ViewApplicationState.");
	
			        	this.getHttpSession().removeAttribute("infogluePrincipal");
		    		    this.getHttpSession().removeAttribute("infoglueRemoteUser");
		    		    this.getHttpSession().removeAttribute("cmsUserName");
	
		    		    this.getResponse().setContentType("text/plain");
			            this.getResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
			            this.getResponse().getWriter().println("You have no access to this view as you don't have ViewApplicationState.Read-rights.");				
			            returnValue = NONE;
					}
	        	}
	        	
			    if(!allowAccess)
		        {
		        	logger.warn("A user from an IP(" + this.getRequest().getRemoteAddr() + ") and username [" + principal + "] was denied access to ViewApplicationState.");
			    	this.getResponse().setContentType("text/plain");
			        this.getResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
		            this.getResponse().getWriter().println("You have no access to this view - talk to your administrator if you should.");				
		            returnValue = NONE;
		        }
	        }
        }
        catch (Exception e) 
        {
        	logger.warn("Error checking for access: " + e.getMessage());
		}
		return returnValue;
	}

	
	/**
	 * This method validates that the current page is accessible to the requesting user.
	 * It fetches information from the page metainfo about if the page is protected and if it is 
	 * validates the users credentials against the extranet database,
	 */
	
	public Principal getPrincipal() throws SystemException, Exception
	{
		Principal principal = (Principal)this.getHttpSession().getAttribute("infogluePrincipal");
		logger.info("principal:" + principal);

		try
		{
			if(principal == null || CmsPropertyHandler.getAnonymousUser().equalsIgnoreCase(principal.getName()))
			{
				if(logger.isInfoEnabled())
					logger.info("Principal in session was:" + principal + " - we clear it as only cms-users are allowed.");
				//System.out.println("principal1:" + principal);
				if(principal != null)
				{
					principal = null;
					this.getHttpSession().removeAttribute("infogluePrincipal");
				    this.getHttpSession().removeAttribute("infoglueRemoteUser");
				    this.getHttpSession().removeAttribute("cmsUserName");

					Map status = new HashMap();
					status.put("redirected", new Boolean(false));
					principal = AuthenticationModule.getAuthenticationModule(null, this.getOriginalFullURL(), getRequest(), false).loginUser(getRequest(), getResponse(), status);
					Boolean redirected = (Boolean)status.get("redirected");
					if(redirected != null && redirected.booleanValue())
					{
					    this.getHttpSession().removeAttribute("infogluePrincipal");
					    principal = null;
					    return principal;
					}
					else if(principal != null)
					{
					    this.getHttpSession().setAttribute("infogluePrincipal", principal);
						this.getHttpSession().setAttribute("infoglueRemoteUser", principal.getName());
						this.getHttpSession().setAttribute("cmsUserName", principal.getName());
					}
				}
				//System.out.println("principal2:" + principal);
				
			    if(principal == null)
			        principal = loginWithRequestArguments();
			    //System.out.println("principal3:" + principal);

			    if(principal == null || CmsPropertyHandler.getAnonymousUser().equalsIgnoreCase(principal.getName()))
			    {
					String ssoUserName = AuthenticationModule.getAuthenticationModule(null, this.getOriginalFullURL(), this.getRequest(), false).getSSOUserName(getRequest());
					//System.out.println("ssoUserName:" + ssoUserName);
					if(ssoUserName != null)
					{
						principal = UserControllerProxy.getController().getUser(ssoUserName);
						if(principal != null)
						{
						    this.getHttpSession().setAttribute("infogluePrincipal", principal);
							this.getHttpSession().setAttribute("infoglueRemoteUser", principal.getName());
							this.getHttpSession().setAttribute("cmsUserName", principal.getName());
						}
					}
			    }							
			}
		}
		catch(Exception e)
		{
			logger.error("An error occurred:" + e.getMessage(), e);
		}
		
		return principal;
	}
	
	/**
	 * This method (if enabled in deliver.properties) checks for arguments in the request
	 * and logs the user in if available.
	 * 
	 * @return Principal
	 * @throws Exception
	 */
	private Principal loginWithRequestArguments() throws Exception
	{
	    Principal principal = null;
	    
        String userName = this.getRequest().getParameter("j_username");
	    String password = this.getRequest().getParameter("j_password");
	    String ticket 	= null; //this.getRequest().getParameter("ticket");
	    //System.out.println("userName:" + userName);
	    //System.out.println("password:" + password);
	    //System.out.println("ticket:" + ticket);
		
		if(ticket != null)
	    {
			//System.out.println("ticket used in loginWithRequestArguments:" + ticket);
		    Map arguments = new HashMap();
		    arguments.put("ticket", ticket);
		    
			principal = ExtranetController.getController().getAuthenticatedPrincipal(arguments, this.getRequest());
			if(principal != null)
			{
			    this.getHttpSession().setAttribute("infogluePrincipal", principal);
				this.getHttpSession().setAttribute("infoglueRemoteUser", principal.getName());
				this.getHttpSession().setAttribute("cmsUserName", principal.getName());
			}
	    }		    
	    else if(userName != null && password != null)
	    {
		    Map arguments = new HashMap();
		    arguments.put("j_username", userName);
		    arguments.put("j_password", password);
		    
			principal = ExtranetController.getController().getAuthenticatedPrincipal(arguments, this.getRequest());
			if(principal != null)
			{
			    this.getHttpSession().setAttribute("infogluePrincipal", principal);
				this.getHttpSession().setAttribute("infoglueRemoteUser", principal.getName());
				this.getHttpSession().setAttribute("cmsUserName", principal.getName());
			}
	    }
	    
	    return principal;
	}

	
    /**
     * This action allows clearing of the given cache manually.
     */
    public String doClearCache() throws Exception
    {
    	String returnValue = handleAccess(this.getRequest());
    	if(returnValue != null)
    		return returnValue;
        
        CacheController.clearCache(cacheName);
        if(cacheName.equals("pageCache"))
			PageCacheHelper.getInstance().clearPageCache();
        else if(clearFileCache)
        	CacheController.clearFileCaches(cacheName);
        
        //this.getHttpSession().invalidate();
        if(this.returnAddress != null && !this.returnAddress.equals(""))
        {
            this.getResponse().sendRedirect(this.returnAddress);
            
            return NONE;
        }
 
        return "cleared";
    }

    /**
     * This action allows clearing of the given cache manually.
     */
    public String doClearStringPool() throws Exception
    {
    	String returnValue = handleAccess(this.getRequest());
    	if(returnValue != null)
    		return returnValue;
        
        CacheController.clearPooledString();
        
        //this.getHttpSession().invalidate();
        if(this.returnAddress != null && !this.returnAddress.equals(""))
        {
            this.getResponse().sendRedirect(this.returnAddress);
            
            return NONE;
        }
 
        return "cleared";
    }

    /**
     * This action allows clearing of the given cache manually.
     */
    /*
    public String doFlushCache() throws Exception
    {
    	String returnValue = handleAccess(this.getRequest());
    	if(returnValue != null)
    		return returnValue;
        
        CacheController.flushCache(cacheName);
        
        //this.getHttpSession().invalidate();
        if(this.returnAddress != null && !this.returnAddress.equals(""))
        {
            this.getResponse().sendRedirect(this.returnAddress);
            
            return NONE;
        }
 
        return "cleared";
    }
    */


    /**
     * This action allows clearing of the given cache manually.
     */
    public String doClearCacheStartingWith() throws Exception
    {
    	String returnValue = handleAccess(this.getRequest());
    	if(returnValue != null)
    		return returnValue;
        
        Map caches = getCaches();
        Iterator cachesIterator = caches.keySet().iterator();
        while(cachesIterator.hasNext())
        {
        	String cacheName = (String)cachesIterator.next();
        	if(cacheName.startsWith(getRequest().getParameter("cacheNamePrefix")))
        		CacheController.clearCache(cacheName);
        }
        
        if(this.returnAddress != null && !this.returnAddress.equals(""))
        {
            this.getResponse().sendRedirect(this.returnAddress);
            
            return NONE;
        }

        
        return "cleared";
    }

    /**
     * This action allows clearing of the given cache manually.
     */
    public String doClearPageCache() throws Exception
    {
    	String returnValue = handleAccess(this.getRequest());
    	if(returnValue != null)
    		return returnValue;
        
        //CacheController.clearFileCaches("pageCache");
		PageCacheHelper.getInstance().clearPageCache();
        
        if(this.returnAddress != null && !this.returnAddress.equals(""))
        {
            this.getResponse().sendRedirect(this.returnAddress);
            
            return NONE;
        }

        
        return "cleared";
    }
	

    
    /**
     * This action allows clearing of the given cache manually.
     */
    public String doClearCastorCache() throws Exception
    {
    	String returnValue = handleAccess(this.getRequest());
    	if(returnValue != null)
    		return returnValue;
        
        CacheController.clearCache(Class.forName(className));
        
        return "cleared";
    }

    /**
     * This action allows clearing of the given cache manually.
     */
    public String doClearApplicationCache() throws Exception
    {
    	String returnValue = handleAccess(this.getRequest());
    	if(returnValue != null)
    		return returnValue;
        
        if(attributeName != null && attributeName.equals("all"))
        {
        	getApplicationAttributes();
        	Iterator applicationAttributesIterator = this.applicationMap.keySet().iterator();
        	while(applicationAttributesIterator.hasNext())
        	{
        		String attributeName = (String)applicationAttributesIterator.next();
        		ActionContext.getServletContext().removeAttribute(attributeName);        	
        	}
        }
        else if(attributeName != null)
        {
        	ActionContext.getServletContext().removeAttribute(attributeName);
        }
        
        return "cleared";
    }

    /**
     * This action allows clearing of the given cache manually.
     */
    public String doRestoreWorkflows() throws Exception
    {
    	String returnValue = handleAccess(this.getRequest());
    	if(returnValue != null)
    		return returnValue;
        
        WorkflowController.restoreSessionFactory(null);
        
        //this.getHttpSession().invalidate();
        
        return "cleared";
    }

    /**
     * This action allows clearing of the given cache manually.
     */
    public String doDecreaseActiveCount() throws Exception
    {
        if(CmsPropertyHandler.getOperatingMode().equalsIgnoreCase("3"))
        {
            this.getResponse().setContentType("text/plain");
            this.getResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
            this.getResponse().getWriter().println("You have no access to this view - talk to your administrator if you should.");
            
            return NONE;
        }
        
    	String returnValue = handleAccess(this.getRequest());
    	if(returnValue != null)
    		return returnValue;
        
        RequestAnalyser.getRequestAnalyser().decNumberOfCurrentRequests(1000);
        
        return "cleared";
    }

    /**
     * This action allows setting of the loglevel on any class.
     */
    public String doSetLogLevel() throws Exception
    {
    	Level newLevel = Level.ERROR;
    	if(this.logLevel.equalsIgnoreCase("debug"))
    		newLevel = Level.DEBUG;
    	if(this.logLevel.equalsIgnoreCase("info"))
    		newLevel = Level.INFO;
    	else if(this.logLevel.equalsIgnoreCase("warn"))
    		newLevel = Level.WARN;
    	else if(this.logLevel.equalsIgnoreCase("error"))
    		newLevel = Level.ERROR;
    	
    	Category category = getCategory(this.className);
    	if(category != null)
    	{
    		category.setLevel(newLevel);
    		
			Enumeration enumeration = Logger.getLogger("org.infoglue.console-debug-dummy").getAllAppenders();
	        while(enumeration.hasMoreElements())
	        {
	        	Appender appender = (Appender)enumeration.nextElement();
	           	category.addAppender(appender);
	            break;
	        }

    	}
    	
        return "cleared";
    }

    /**
     * This action allows setting of the loglevel on some basic classes.
     */
    public String doSetLogInfo() throws Exception
    {
        //ViewPageFilter.logger.setLevel(Level.INFO);
        //ViewPageAction.logger.setLevel(Level.INFO);
        //RedirectFilter.logger.setLevel(Level.INFO);
        CastorDatabaseService.logger.setLevel(Level.INFO);
        CacheController.logger.setLevel(Level.INFO);
        getDeliverCategory().setLevel(Level.INFO);
        getCastorJDOCategory().setLevel(Level.INFO);
        
        return "cleared";
    }

    /**
     * This action allows setting of the loglevel on some basic classes.
     */
    public String doSetLogWarning() throws Exception
    {
        //ViewPageFilter.logger.setLevel(Level.WARN);
        //ViewPageAction.logger.setLevel(Level.WARN);
        //RedirectFilter.logger.setLevel(Level.WARN);
        CastorDatabaseService.logger.setLevel(Level.WARN);
        CacheController.logger.setLevel(Level.WARN);
        getDeliverCategory().setLevel(Level.WARN);
        getCastorJDOCategory().setLevel(Level.WARN);
        
        return "cleared";
    }

    /**
     * This action allows setting of the loglevel on some basic classes.
     */
    public String doSetLogError() throws Exception
    {
        //ViewPageFilter.logger.setLevel(Level.ERROR);
        //ViewPageAction.logger.setLevel(Level.ERROR);
        //RedirectFilter.logger.setLevel(Level.ERROR);
        CastorDatabaseService.logger.setLevel(Level.ERROR);
        CacheController.logger.setLevel(Level.ERROR);
        getDeliverCategory().setLevel(Level.ERROR);
        getCastorJDOCategory().setLevel(Level.ERROR);

        return "cleared";
    }

    /**
     * This action allows clearing of the caches manually.
     */
    public String doClearCaches() throws Exception
    {
    	String returnValue = handleAccess(this.getRequest());
    	if(returnValue != null)
    		return returnValue;
        
        CacheController.clearServerNodeProperty(true);
        CacheController.clearCastorCaches();
        CacheController.clearCaches(null, null, null);

        PageCacheHelper.getInstance().clearPageCache();
        //CacheController.clearFileCaches("pageCache");
        CacheController.clearPooledString();
        //CacheController.resetSpecial();
        if(clearFileCache)
        	CacheController.clearFileCaches();
        
        if(this.returnAddress != null && !this.returnAddress.equals(""))
        {
            this.getResponse().sendRedirect(this.returnAddress);
            
            return NONE;
        }
        	
        return "cleared";
    }

    /**
     * This action allows clearing of the castor caches manually.
     */
    public String doClearCastorCaches() throws Exception
    {
    	String returnValue = handleAccess(this.getRequest());
    	if(returnValue != null)
    		return returnValue;
        
        CacheController.clearCastorCaches();
        
        return "cleared";
    }

    public String doClearOSCaches() throws Exception
	{
    	String returnValue = handleAccess(this.getRequest());
    	if(returnValue != null)
    		return returnValue;

		ServletCacheAdministrator servletCacheAdministrator = ServletCacheAdministrator.getInstance(ActionContext.getServletContext());
		servletCacheAdministrator.flushAll();
		Cache cache = servletCacheAdministrator.getAppScopeCache(ActionContext.getServletContext());
		
		OSCacheUtility.clear(cache);
		
		return "cleared";
	}
    /**
     * This action allows recaching of some parts of the caches manually.
     */
    public String doClearPortlets() throws Exception
    {
        try 
        {
        	
     		//run registry services to load new portlet info from the registry files
        	String[] svcs = {
     				"org.apache.pluto.portalImpl.services.portletdefinitionregistry.PortletDefinitionRegistryService",
     				"org.apache.pluto.portalImpl.services.portletentityregistry.PortletEntityRegistryService"};
     		int len = svcs.length;
     		for (int i = 0; i < len; i++) {				
	 			try {
					ServiceManager.hotInit(ServletConfigContainer.getContainer().getServletConfig(), svcs[i]);
	 			} catch (Throwable e) {
	 				String svc = svcs[i].substring(svcs[i].lastIndexOf('.') + 1);
	 				String msg = "Initialization of " + svc + " service for hot deployment failed!"; 
	 				logger.error(msg);
	 				break;
	 			}
	 	
	 			try {
	 				logger.error("ServletConfigContainer.getContainer().getServletConfig():" + ServletConfigContainer.getContainer().getServletConfig());
	 				logger.error("ServletConfigContainer.getContainer().getServletConfig().getServletContext():" + ServletConfigContainer.getContainer().getServletConfig().getServletContext());
	 				logger.error("svcs[i]:" + svcs[i]);
					ServiceManager.postHotInit(ServletConfigContainer.getContainer().getServletConfig(), svcs[i]);
	 			} catch (Throwable e) {
	 				String svc = svcs[i].substring(svcs[i].lastIndexOf('.') + 1);
	 				String msg = "Post initialization of " + svc + " service for hot deployment failed!"; 
	 				logger.error(msg);
	 				break;
	 			}
			}

        	
            PortletContainerServices.prepare("infoglueAVote");
            ServiceManager.init(ServletConfigContainer.getContainer().getServletConfig());
        
            PortletEntityRegistry.load();

        } catch (Exception e) {
        	e.printStackTrace();
        }
        
        return "cleared";
    }
    
    /**
     * This action allows recaching of some parts of the caches manually.
     */
    public String doReCache() throws Exception
    {
    	String returnValue = handleAccess(this.getRequest());
    	if(returnValue != null)
    		return returnValue;
        
        CacheController.cacheCentralCastorCaches();
        
        return "cleared";        
    }
    
    private List getList(String key, Object value)
    {
        List list = new ArrayList();
        list.add(key);
        list.add(value);

        return list;
    }
    private List getList(String key, Object value, Object value2)
    {
        List list = new ArrayList();
        list.add(key);
        list.add(value);
        list.add(value2);

        return list;
    }
    
    public String doGC() throws Exception
    {
    	String returnValue = handleAccess(this.getRequest());
    	if(returnValue != null)
    		return returnValue;
        
        Runtime.getRuntime().gc();
        
        return doExecute();
    }

    public String doResetAverageResponseTimeStatistics() throws Exception
    {
    	String returnValue = handleAccess(this.getRequest());
    	if(returnValue != null)
    		return returnValue;
        
        RequestAnalyser.resetAverageResponseTimeStatistics();
        
        return "cleared";
    }

    public String doResetComponentStatistics() throws Exception
    {
    	String returnValue = handleAccess(this.getRequest());
    	if(returnValue != null)
    		return returnValue;
        
        RequestAnalyser.resetComponentStatistics();
        
        return "cleared";
    }

    public String doResetPageStatistics() throws Exception
    {
    	String returnValue = handleAccess(this.getRequest());
    	if(returnValue != null)
    		return returnValue;
        
        RequestAnalyser.resetPageStatistics();
        
        return "cleared";
    }

    public String doOngoingPublicationDetails() throws Exception
    {
    	String returnValue = handleAccess(this.getRequest());
    	if(returnValue != null)
    		return returnValue;

        return "successOngoingPublications";
    }

    public List<CacheEvictionBean> getOngoingPublications()
    {
    	return RequestAnalyser.getRequestAnalyser().getOngoingPublications();
    }

    public String doComponentStatistics() throws Exception
    {
    	String returnValue = handleAccess(this.getRequest());
    	if(returnValue != null)
    		return returnValue;
        
        states.add(getList("Average processing time per request (ms)", "&nbsp;", "" + RequestAnalyser.getRequestAnalyser().getAverageElapsedTime()));
		
        states.add(getList("<br/><strong>Individual components (in milliseconds)</strong>", "&nbsp;", "&nbsp;"));
        
        List unsortedComponents = new ArrayList();
        Set componentNames = RequestAnalyser.getAllComponentNames();
        Iterator componentNamesIterator = componentNames.iterator();
        while(componentNamesIterator.hasNext())
        {
        	String componentName = (String)componentNamesIterator.next();
        	long componentAverageElapsedTime = RequestAnalyser.getComponentAverageElapsedTime(componentName);
        	int componentNumberOfHits = RequestAnalyser.getComponentNumberOfHits(componentName);
        	//states.add(getList("" + componentName + " - " + componentNumberOfHits + " hits", "" + componentAverageElapsedTime));
        	if(componentName.indexOf("micro") > -1)
        	{
        		componentAverageElapsedTime = componentAverageElapsedTime / 1000;
        		componentName = componentName.replaceFirst("micro", "now milli");
        	}
        	unsortedComponents.add(getList("" + componentName + " - " + componentNumberOfHits + " hits", new Long(componentNumberOfHits * componentAverageElapsedTime), new Long(componentAverageElapsedTime)));
        }

        Collections.sort(unsortedComponents, new AverageInvokingTimeComparator());
        
    	states.addAll(unsortedComponents);
    	
        return "successComponentStatistics";
    }

    public String doPageStatistics() throws Exception
    {
    	String returnValue = handleAccess(this.getRequest());
    	if(returnValue != null)
    		return returnValue;
        states.add(getList("Average processing time per request (ms)", "" + RequestAnalyser.getRequestAnalyser().getAverageElapsedTime()));
		
        states.add(getList("<br/><strong>Individual pages (in milliseconds)</strong>", "&nbsp;"));
        
        VisualFormatter vf = new VisualFormatter();
        
        List unsortedPageUrls = new ArrayList();
        Set pageUrls = RequestAnalyser.getAllPageUrls();
        Iterator pageUrlsIterator = pageUrls.iterator();
        while(pageUrlsIterator.hasNext())
        {
        	String pageUrl = (String)pageUrlsIterator.next();
        	long pageAverageElapsedTime = RequestAnalyser.getPageAverageElapsedTime(pageUrl);
        	int pageNumberOfHits = RequestAnalyser.getPageNumberOfHits(pageUrl);
        	Date date = RequestAnalyser.getLastEventDate(pageUrl);
        	unsortedPageUrls.add(getList("" + pageUrl + " (last accessed " + vf.formatDate(date,"yyyy-MM-dd HH:mm") + ") - " + pageNumberOfHits + " hits - total " + (pageNumberOfHits * pageAverageElapsedTime), new Long(pageAverageElapsedTime)));
        }

        Collections.sort(unsortedPageUrls, new AverageInvokingTimeComparator());
        
    	states.addAll(unsortedPageUrls);

        return "successPageStatistics";
    }

    public String doLatestPageStatistics() throws Exception
    {
    	String returnValue = handleAccess(this.getRequest());
    	if(returnValue != null)
    		return returnValue;
        //states.add(getList("Average processing time per request (ms)", "" + RequestAnalyser.getRequestAnalyser().getAverageElapsedTime()));
		
        states.add(getList("<br/><strong>Last requested pages</strong>", "&nbsp;"));
        
        VisualFormatter vf = new VisualFormatter();
        
        List unsortedPageUrls = new ArrayList();
        List<String> pageUrls = RequestAnalyser.getRequestAnalyser().getLatestPageStatistics();
        Iterator pageUrlsIterator = pageUrls.iterator();
        while(pageUrlsIterator.hasNext())
        {
        	String pageUrl = (String)pageUrlsIterator.next();
        	unsortedPageUrls.add(getList("" + pageUrl, ""));
        }

    	states.addAll(unsortedPageUrls);

        return "successPageStatistics";
    }

    public String doCacheStatistics() throws Exception
    {
    	String returnValue = handleAccess(this.getRequest());
    	if(returnValue != null)
    		return returnValue;
        
        return "successCacheStatistics";
    }

    public String doCacheDetailsStatistics() throws Exception
    {
    	String returnValue = handleAccess(this.getRequest());
    	if(returnValue != null)
    		return returnValue;
        
        if(this.cacheName != null && !this.cacheName.equals(""))
        {
        	this.cache = CacheController.getCaches().get(this.cacheName);
        	if(this.cache instanceof GeneralCacheAdministrator)
        	{
        		this.cacheMap = ((GeneralCacheAdministrator)this.cache).getCache().cacheMap;
        	}
        }
        
        return "successCacheDetailsStatistics";
    }
    
    public String doClearPublications() throws Exception
    {
    	String returnValue = handleAccess(this.getRequest());
    	if(returnValue != null)
    		return returnValue;
        
    	RequestAnalyser.getRequestAnalyser().resetLatestPublications();
    	
        return "cleared";
    }

    private String getRedirectUrl(HttpServletRequest request, HttpServletResponse response) throws ServletException, Exception 
  	{
		String url = AuthenticationModule.getAuthenticationModule(null, this.getOriginalFullURL(), request, false).getLoginDialogUrl(request, response);
		return url;
  	}
    
    /**
     * This method is the application entry-point. The method does a lot of checks to see if infoglue
     * is installed correctly and if all resources needed are available.
     */
         
    public String doExecute() throws Exception
    {
    	String returnValue = handleAccess(this.getRequest());
    	if(returnValue != null)
    		return returnValue;
        
        String sessionTimeout = CmsPropertyHandler.getSessionTimeout();
		if(sessionTimeout == null)
		    sessionTimeout = "1800";
		
        states.add(getList("Application started", "" + formatter.formatDate(CmsPropertyHandler.getStartupTime(), "yyyy-MM-dd HH:mm")));
        states.add(getList("Maximum memory (MB)", "" + Runtime.getRuntime().maxMemory() / 1024 / 1024));
        states.add(getList("Used memory (MB)", "" + ((Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory()) / 1024 / 1024)));
        states.add(getList("Free memory (MB)", "" + Runtime.getRuntime().freeMemory() / 1024 / 1024));
        states.add(getList("Total memory (MB)", "" + Runtime.getRuntime().totalMemory() / 1024 / 1024));
        addPermGenStatistics(states);
        states.add(getList("Number of sessions <br/>(remains for " + (Integer.parseInt(sessionTimeout) / 60) + " minutes after last request)", "" + CmsSessionContextListener.getActiveSessions()));
        states.add(getList("Number of request being handled now", "" + RequestAnalyser.getRequestAnalyser().getNumberOfCurrentRequests()));
        states.add(getList("Number of active request being handled now", "" + RequestAnalyser.getRequestAnalyser().getNumberOfActiveRequests() + " <a href=\"ViewApplicationState!decreaseActiveCount.action\">Decrease (experts)</a>"));
        states.add(getList("Total number of requests handled", "" + RequestAnalyser.getRequestAnalyser().getTotalNumberOfRequests()));
        states.add(getList("Average processing time per request (ms)", "" + RequestAnalyser.getRequestAnalyser().getAverageElapsedTime() + " <a href=\"ViewApplicationState!resetAverageResponseTimeStatistics.action\">(Reset)</a>"));
        states.add(getList("Slowest request (ms)", "" + RequestAnalyser.getRequestAnalyser().getMaxElapsedTime()));
        states.add(getList("Number of pages in the statistics", RequestAnalyser.getAllPageUrls().size()));

        states.add(getList("<br/><strong>Ongoing publications (handling in process)</strong>", "&nbsp;"));
    	states.add(getList("Number of publications in process", "" + RequestAnalyser.getRequestAnalyser().getOngoingPublications().size() + " <a href=\"ViewApplicationState!ongoingPublicationDetails.action\">(Details)</a>"));
        List<CacheEvictionBean> publications = RequestAnalyser.getRequestAnalyser().getLatestPublications();

        states.add(getList("<br/><strong>Latest publications (Finished, Timestamp, user, object name)</strong>", "&nbsp;"));
        
        List<CacheEvictionBean> publicationsToShow = new ArrayList<CacheEvictionBean>();
        publicationsToShow.addAll(publications);
    	Collections.reverse(publicationsToShow);
        if(publications.size() > 20)
        	publicationsToShow = publicationsToShow.subList(0, 20);
        
        Iterator<CacheEvictionBean> publicationsIterator = publicationsToShow.iterator();
        while(publicationsIterator.hasNext())
        {
        	CacheEvictionBean publication = publicationsIterator.next();
        	states.add(getList("<a href=\"javascript:void(0);\" onclick=\"window.open('" + CmsPropertyHandler.getCmsFullBaseUrl() + "/ViewPublications!showPublicationDetails.action?publicationId=" + publication.getPublicationId() + "', 'Publication details', 'width=900,height=600');\">PublicationId: " + publication.getPublicationId() + ", User: " + publication.getUserName() + ", Finished: " + formatter.formatDate(publication.getProcessedTimestamp(), "yyyy-MM-dd HH:mm:ss") + ", Initiated: " + formatter.formatDate(publication.getTimestamp(), "yyyy-MM-dd HH:mm:ss") + ", Received: " + formatter.formatDate(publication.getReceivedTimestamp(), "yyyy-MM-dd HH:mm:ss") + ", Entity: " + publication.getClassName().replaceAll(".*\\.", "") + "</a>", ""));
        }

        states.add(getList("<a href=\"ViewApplicationState!clearPublications.action\">Clear (" + publications.size() + " publications done since last reset)</a>", "&nbsp;"));

    	getApplicationAttributes();
    	
		//this.getHttpSession().invalidate();

        return "success";
    }

    private void addPermGenStatistics(List states)
    {
    	Iterator iter1 = ManagementFactory.getMemoryPoolMXBeans().iterator();
    	while (iter1.hasNext()) 
    	{
    	    MemoryPoolMXBean item = (MemoryPoolMXBean) iter1.next();
    	    long used = item.getUsage().getUsed() / 1024 / 1024;
    	    long max = item.getUsage().getMax() / 1024 / 1024;
    	    long usedDivided = used;
    	    long maxDivided = max;
    	    if(max > 100)
    	    {
    	    	usedDivided = used / 10;
    	    	maxDivided = max / 10;
    	    }
    	    
    	    states.add(getList("" + item.getName(), "" + used + " / " + max + "<div style='border: 1px solid #ccc; background-color: green; height: 10px; width: " + maxDivided + "px;'><div style='margin-top: 2px; background-color: red; height: 6px; width: " + usedDivided + "px;'></div></div>"));
    	}	
    	
	    long max = Runtime.getRuntime().maxMemory() / 1024 / 1024;
	    long used = ((Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory()) / 1024 / 1024);
	    long usedDivided = used;
	    long maxDivided = max;
	    if(max > 100)
	    {
	    	usedDivided = used / 10;
	    	maxDivided = max / 10;
	    }

        states.add(getList("Heap summary", "" + used + " / " + max + "<div style='border: 1px solid #ccc; background-color: green; height: 10px; width: " + maxDivided + "px;'><div style='margin-top: 2px; background-color: red; height: 6px; width: " + usedDivided + "px;'></div></div>"));

    }

	private void getApplicationAttributes()
	{
		Enumeration attributesEnumeration = ActionContext.getServletContext().getAttributeNames();
    	while(attributesEnumeration.hasMoreElements())
    	{
    		String attributeName = (String)attributesEnumeration.nextElement();
    		if(attributeName.indexOf("org.apache.catalina") == -1 && attributeName.indexOf("__oscache") == -1 && attributeName.indexOf("javax.servlet") == -1)
    		{
    			applicationMap.put(attributeName, ActionContext.getServletContext().getAttribute(attributeName).getClass().getName());
    		}
    	}
	}
        
	public String doAsXML() throws Exception
	{
		doExecute();

		getResponse().setContentType("text/xml; charset=utf-8");
		
		return "successAsXML";
	}

    public String doCacheEntryGroups() throws Exception
    {
    	if(this.cacheName != null && !this.cacheName.equals(""))
        {
        	this.cache = CacheController.getCaches().get(this.cacheName);
        	if(this.cache instanceof GeneralCacheAdministrator)
        	{
        		this.cacheMap = ((GeneralCacheAdministrator)this.cache).getCache().cacheMap;
        	}
        }
    	
    	CacheEntry ce = null;

    	Iterator ceIterator = ((GeneralCacheAdministrator)cache).getCache().cacheMap.values().iterator();
    	while(ceIterator.hasNext())
    	{
    		ce = (CacheEntry)ceIterator.next();
    		if(ce.getKey().equals(this.cacheKey))
    		{
    			break;
    		}
    	}
    	getResponse().setContentType("text/plain");

    	StringBuilder sb = new StringBuilder();
    	
    	if(ce != null && ce.getGroups() != null)
    	{
    		List<String> groups = new ArrayList<String>();
    		groups.addAll(ce.getGroups());
    		Collections.sort(groups);
	    	for(Object group : groups)
	    	{
	    		sb.append("" + group + "\n");
	    	}
    	}

    	getResponse().getWriter().println(sb.toString());
    	
    	return NONE;
    }

    public String doCacheEntryValue() throws Exception
    {
    	Object value = null;
    	if(this.cacheName != null && !this.cacheName.equals(""))
        {
        	this.cache = CacheController.getCaches().get(this.cacheName);
        	if(this.cache instanceof GeneralCacheAdministrator)
        	{
        		value = CacheController.getCachedObjectFromAdvancedCache(cacheName, cacheKey);
        	}
        	else
        	{
        		value = CacheController.getCachedObject(cacheName, cacheKey);        		
        	}
        }
    	
    	if(value != null && value instanceof byte[])
    	{
    	    CompressionHelper compressionHelper = new CompressionHelper();
	    	value = compressionHelper.decompress((byte[])value);
    	}
    	getResponse().setContentType("text/plain");

    	StringBuilder sb = new StringBuilder();
    	
    	sb.append("" + value + "\n");
    	getResponse().getWriter().println(sb.toString());
    	
    	return NONE;
    }

	public boolean getIsApplicationSettingsOk()
	{
		return applicationSettingsOk;
	}

	public boolean getIsDatabaseConnectionOk()
	{
		return databaseConnectionOk;
	}

	public boolean getIsDiskPermissionOk()
	{
		return diskPermissionOk;
	}

	public boolean getIsTestQueriesOk()
	{
		return testQueriesOk;
	}

	public Map getCaches()
	{
		return CacheController.getCaches();
	}

	public List getSortedCaches()
	{
		List cacheNames = new ArrayList();
		cacheNames.addAll(CacheController.getCaches().keySet());
		Collections.sort(cacheNames);
		
		return cacheNames;
	}

	public ServletCache getOSCache()
	{
		ServletCacheAdministrator servletCacheAdministrator = ServletCacheAdministrator.getInstance(ActionContext.getServletContext());
		ServletCache applicationCache = (ServletCache)servletCacheAdministrator.getAppScopeCache(ActionContext.getServletContext());
        
		return applicationCache;
	}

    public List getStates()
    {
        return states;
    }
    
    public void setCacheName(String cacheName)
    {
        this.cacheName = cacheName;
    }

    public String getCacheName()
    {
        return this.cacheName;
    }

    public void setClearFileCache(boolean clearFileCache)
    {
        this.clearFileCache = clearFileCache;
    }

    public int getActiveNumberOfSessions() throws Exception
    {
    	return CmsSessionContextListener.getActiveSessions();
    }
    
    public String getServerName()
    {
    	String serverName = "Unknown";

    	try
    	{
		    InetAddress localhost = InetAddress.getLocalHost();
		    serverName = localhost.getHostName(); 
    	}
    	catch(Exception e)
    	{
    		
    	}
    	
	    return serverName;
    }

    public Map parseData(String originalSummary, String originalStats)
    {
    	Map data = new HashMap();
    	try
    	{
	    	data.put("originalSummary", originalSummary);
	    	data.put("originalStats", originalStats);
	    	
	    	if(originalSummary != null && originalStats != null)
	    	{
		    	int startLocation = originalSummary.indexOf("Approximate size");
		    	//System.out.println("startLocation:" + startLocation);
		    	//String size = originalSummary.substring(startLocation + 17, originalSummary.indexOf("KB") + 2);
		    	//System.out.println("size:" + size);
		    	data.put("estimatedSize", "Unknown");
		
		    	int startHitLocation = originalStats.indexOf("Hit count");
		    	String hits = originalStats.substring(startHitLocation + 12, originalStats.indexOf(",", startHitLocation));
		    	
		    	//System.out.println("originalStats:" + originalStats);
		    	int startMissLocation = originalStats.indexOf("miss count");
		    	//System.out.println("startMissLocation:" + startMissLocation);
		    	String miss = originalStats.substring(startMissLocation + 13);
		    	
		    	//System.out.println("hits:" + hits);
		    	//System.out.println("miss:" + miss);
		    	
		    	try
		    	{
		    		int missInt = Integer.parseInt(miss.trim());
		    		int hitInt = Integer.parseInt(hits.trim());
		    		if(missInt > 0 && hitInt > 0)
		    			data.put("hitMissRatio", "" + (int)((float)hitInt / (float)((int)missInt + (int)hitInt) * 100));
		    		else if(missInt == 0)
		    			data.put("hitMissRatio", "100");
		    		else if(hitInt == 0)
		    			data.put("hitMissRatio", "0");
		    	}
		    	catch (Exception e) 
		    	{
		    		logger.error("Could not parse hits / miss:" + e.getMessage() + " (" + miss.trim() + " / " + hits.trim() + ")");
				}
	    	}
    	}
    	catch (Exception e) 
    	{
    		logger.error("Problem parsing data:" + e.getMessage(), e);
		}
    	return data;
    }

    public Integer getStringPoolSize()
    {
    	return CacheController.getPooledStringSize();
    }

    public Integer getStringPoolHits()
    {
    	return CacheController.getPooledStringHits();
    }

	public void setClassName(String className) 
	{
		this.className = className;
	}

	public void setLogLevel(String logLevel) 
	{
		this.logLevel = logLevel;
	}

    public Map getApplicationMap()
    {
    	return this.applicationMap;
    }

    public Object getCache()
    {
    	return this.cache;
    }

    public Object getCacheMap()
    {
    	return this.cacheMap;
    }
    
    public void setCacheKey(String cacheKey)
    {
    	this.cacheKey = cacheKey;
    }

    public int getLength(Object o)
    {
    	logger.info("o:" + o);
    	if(o == null)
    		return -1;
    	if(o instanceof byte[])
    	{
    		byte[] array = (byte[])o;
    		logger.info("array:" + array.length);
    		return array.length;
    	}
    	else if(o instanceof String)
    	{
    		logger.info("object string:" + (String)o.toString());
    		return o.toString().length();
    	}
    	else
    		return o.toString().length();
    }

	public void setAttributeName(String attributeName)
	{
		this.attributeName = attributeName;
	}
	
	public String getReturnAddress()
	{
		return returnAddress;
	}

	public void setReturnAddress(String returnAddress)
	{
		this.returnAddress = returnAddress;
	}

}
