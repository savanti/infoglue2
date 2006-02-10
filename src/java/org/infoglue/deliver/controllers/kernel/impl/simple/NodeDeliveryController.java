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

package org.infoglue.deliver.controllers.kernel.impl.simple;

import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.impl.simple.ContentImpl;
import org.infoglue.cms.entities.structure.ServiceBinding;
import org.infoglue.cms.entities.structure.Qualifyer;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl;
import org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeImpl;
import org.infoglue.cms.entities.structure.SiteNodeVersion;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.entities.management.*;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.services.*;
import org.infoglue.cms.util.*;

import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;

import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.deliver.applications.databeans.DeliveryContext;
import org.infoglue.deliver.applications.databeans.NullObject;
import org.infoglue.deliver.applications.databeans.WebPage;
import org.infoglue.deliver.applications.filters.URIMapperCache;
import org.infoglue.deliver.applications.filters.ViewPageFilter;
import org.infoglue.deliver.controllers.kernel.URLComposer;
import org.infoglue.deliver.util.CacheController;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;

import java.net.URLEncoder;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Date;

import javax.servlet.http.HttpSession;


public class NodeDeliveryController extends BaseDeliveryController
{
    private final static Logger logger = Logger.getLogger(NodeDeliveryController.class.getName());

	private URLComposer urlComposer = null; 

    protected static final String META_INFO_BINDING_NAME 				= "Meta information";
    protected static final String NAV_TITLE_ATTRIBUTE_NAME 		 		= "NavigationTitle";

	private static final boolean USE_INHERITANCE = true;
	private static final boolean DO_NOT_USE_INHERITANCE = false;	
	
	protected static final Integer NO 			= new Integer(0);
	protected static final Integer YES 			= new Integer(1);
	protected static final Integer INHERITED 	= new Integer(2);

	private Integer siteNodeId = null;
	private Integer languageId = null;
	private Integer contentId 	= null;
	
	/**
	 * Private constructor to enforce factory-use
	 */
	
	private NodeDeliveryController(Integer siteNodeId, Integer languageId, Integer contentId) throws SystemException, Exception
	{
		this.siteNodeId = siteNodeId;
		this.languageId = languageId;
		this.contentId  = contentId;
		this.urlComposer = URLComposer.getURLComposer(); 
	}
	
	/**
	 * Factory method
	 */
	
	public static NodeDeliveryController getNodeDeliveryController(Integer siteNodeId, Integer languageId, Integer contentId) throws SystemException, Exception
	{
		return new NodeDeliveryController(siteNodeId, languageId, contentId);
	}

	/**
	 * Factory method
	 */
	
	public static NodeDeliveryController getNodeDeliveryController(DeliveryContext deliveryContext) throws SystemException, Exception
	{
		return new NodeDeliveryController(deliveryContext.getSiteNodeId(), deliveryContext.getLanguageId(), deliveryContext.getContentId());
	}

	/**
	 * This method returns which mode the delivery-engine is running in.
	 * The mode is important to be able to show working, preview and published data separate.
	 */
	
	private Integer getOperatingMode()
	{
		Integer operatingMode = new Integer(0); //Default is working
		try
		{
			operatingMode = new Integer(CmsPropertyHandler.getProperty("operatingMode"));
			//getLogger().info("Operating mode is:" + operatingMode);
		}
		catch(Exception e)
		{
			getLogger().warn("We could not get the operating mode from the propertyFile:" + e.getMessage(), e);
		}
		return operatingMode;
	}
	
	/**
	 * This method gets the appropriate siteNodeVersion
	 */

	public SiteNodeVersionVO getActiveSiteNodeVersionVO(Database db, Integer siteNodeId) throws Exception
	{
		SiteNodeVersionVO siteNodeVersionVO = null;
		
		SiteNodeVersion siteNodeVersion = getActiveSiteNodeVersion(siteNodeId, db);
		if(siteNodeVersion != null)
			siteNodeVersionVO = siteNodeVersion.getValueObject();
		
		return siteNodeVersionVO;
	}
	
	/**
	 * This method gets the appropriate siteNodeVersion
	 */
	
	public SiteNodeVersion getActiveSiteNodeVersion(Integer siteNodeId, Database db) throws Exception
	{
		SiteNodeVersion siteNodeVersion = null;
		
		SiteNode siteNode = (SiteNode)this.getObjectWithId(SiteNodeImpl.class, siteNodeId, db);
		getLogger().info("Loaded siteNode " + siteNode.getName());
		Collection siteNodeVersions = siteNode.getSiteNodeVersions();
		getLogger().info("Loaded versions " + siteNodeVersions.size());
		
		Iterator versionIterator = siteNodeVersions.iterator();
		while(versionIterator.hasNext())
		{
			SiteNodeVersion siteNodeVersionCandidate = (SiteNodeVersion)versionIterator.next();	
			getLogger().info("SiteNodeVersionCandidate " + siteNodeVersionCandidate.getId());
			if(siteNodeVersionCandidate.getIsActive().booleanValue() && siteNodeVersionCandidate.getStateId().intValue() >= getOperatingMode().intValue())
			{
				if(siteNodeVersionCandidate.getOwningSiteNode().getSiteNodeId().intValue() == siteNodeId.intValue())
				{
					if(siteNodeVersion == null || siteNodeVersion.getSiteNodeVersionId().intValue() < siteNodeVersionCandidate.getId().intValue())
					{
						siteNodeVersion = siteNodeVersionCandidate;
					}
				}
			}
		}
		
		return siteNodeVersion;
	}
	
	/**
	 * This method checks if there is a serviceBinding with the name on this or any parent node.
	 */
	
	public ServiceDefinitionVO getInheritedServiceDefinition(List qualifyerList, Integer siteNodeId, AvailableServiceBindingVO availableServiceBindingVO, Database db, boolean inheritParentBindings) throws SystemException, Exception
	{
		getLogger().info("Trying to find binding " + availableServiceBindingVO.getName() + " on siteNodeId:" + siteNodeId);
		ServiceDefinitionVO serviceDefinitionVO = null;
		
		//SiteNode siteNode = (SiteNode)this.getObjectWithId(SiteNodeImpl.class, siteNodeId, db);
		SiteNode siteNode = (SiteNode)this.getObjectWithId(SmallSiteNodeImpl.class, siteNodeId, db);
		getLogger().info("Loaded siteNode " + siteNode.getName());
		
		//serviceDefinitionVO = getServiceDefinitionVO(siteNode, availableServiceBindingVO, db);
		serviceDefinitionVO = getServiceDefinitionVO(qualifyerList, siteNode, availableServiceBindingVO, db);
		
		getLogger().info("Loaded serviceDefinitionVO " + serviceDefinitionVO);
		
		if(serviceDefinitionVO == null)
		{
			//We check if the available service definition state that this is a inheritable binding            	
			//AvailableServiceBinding availableServiceBinding = getAvailableServiceBindingRecursive(siteNodeVersion.getOwningSiteNode(), availableServiceBindingName, inheritParentBindings);
			if(availableServiceBindingVO != null && availableServiceBindingVO.getIsInheritable().booleanValue() && inheritParentBindings)
        	{
            	getLogger().info("No binding found - lets try the parent.");
            	SiteNode parent = siteNode.getParentSiteNode();
            	if(parent != null)
            	    serviceDefinitionVO = getInheritedServiceDefinition(qualifyerList, parent.getSiteNodeId(), availableServiceBindingVO, db, inheritParentBindings);
        	}
		}
		
		try{ throw new Exception("APA"); } catch(Exception e){e.printStackTrace();}
		
		getLogger().info("Loaded serviceDefinitionVO end...");
		
		return serviceDefinitionVO;
	}
	
	private ServiceDefinitionVO getServiceDefinitionVO(List qualifyerList, SiteNode siteNode, AvailableServiceBindingVO availableServiceBindingVO, Database db) throws Exception
	{
	    ServiceDefinitionVO serviceDefinitionVO = null;
	    //ServiceBinding serviceBinding = null;
		
	    String key = "" + siteNode.getSiteNodeId() + "_" + availableServiceBindingVO.getId();
		getLogger().info("key:" + key);
		Object object = CacheController.getCachedObject("serviceDefinitionCache", key);
		Object object2 = CacheController.getCachedObject("qualifyerListCache", key);
		if(object != null && object2 != null)
		{
			getLogger().info("There was an cached ServiceDefinitionVO:" + object);
			if(object instanceof ServiceDefinitionVO)
			    serviceDefinitionVO = (ServiceDefinitionVO)object;
			if(object2 instanceof List)
			    qualifyerList.addAll((List)object2);
		}
		else
		{
			/*
			Collection siteNodeVersions = siteNode.getSiteNodeVersions();
			SiteNodeVersion siteNodeVersion = null;
			
			Iterator versionIterator = siteNodeVersions.iterator();
			while(versionIterator.hasNext())
			{
				SiteNodeVersion siteNodeVersionCandidate = (SiteNodeVersion)versionIterator.next();	
				if(siteNodeVersionCandidate.getIsActive().booleanValue() && siteNodeVersionCandidate.getStateId().intValue() >= getOperatingMode().intValue())
				{
					if(siteNodeVersionCandidate.getOwningSiteNode().getSiteNodeId().intValue() == siteNode.getId().intValue())
					{
						if(siteNodeVersion == null || siteNodeVersion.getSiteNodeVersionId().intValue() < siteNodeVersionCandidate.getId().intValue())
						{
							siteNodeVersion = siteNodeVersionCandidate;
						}
					}
				}
			}
			
			ServiceBinding serviceBinding = null;
			
			if(siteNodeVersion != null)
			{
			    Timer timer2 = new Timer();

				Collection serviceBindings = siteNodeVersion.getServiceBindings();
				Iterator serviceBindingIterator = serviceBindings.iterator();
				while(serviceBindingIterator.hasNext())
				{
					ServiceBinding serviceBindingCandidate = (ServiceBinding)serviceBindingIterator.next();
					if(serviceBindingCandidate.getAvailableServiceBinding().getAvailableServiceBindingId().intValue() == availableServiceBindingVO.getId().intValue())
					//if(serviceBindingCandidate.getValueObject().getAvailableServiceBindingId().intValue() == availableServiceBindingVO.getId().intValue())
					{
						serviceBinding = serviceBindingCandidate;
						break;
					}
				}
			}
			
			if (serviceBinding != null) 
	        {
		        serviceBindingVO = serviceBinding.getValueObject();
		        object = serviceBindingVO;
	        }
			else
			    object = new NullObject();
			*/
			
		    OQLQuery oql = db.getOQLQuery( "SELECT sb FROM org.infoglue.cms.entities.structure.impl.simple.ServiceBindingImpl sb WHERE sb.siteNodeVersion.owningSiteNode = $1 AND sb.availableServiceBinding = $2 AND sb.siteNodeVersion.isActive = $3 AND sb.siteNodeVersion.stateId >= $4 order by sb.siteNodeVersion.siteNodeVersionId DESC");
			oql.bind(siteNode);
			oql.bind(availableServiceBindingVO.getId());
			oql.bind(new Boolean(true));
			oql.bind(getOperatingMode());
			
	    	QueryResults results = oql.execute(Database.ReadOnly);
			
			if (results.hasMore()) 
	        {
			    ServiceBinding serviceBinding = (ServiceBinding)results.next();
		        //serviceBindingVO = serviceBinding.getValueObject();
		        serviceDefinitionVO = serviceBinding.getServiceDefinition().getValueObject();
		        Collection qualifyers = serviceBinding.getBindingQualifyers();
				
				qualifyers = sortQualifyers(qualifyers);
				
				Iterator iterator = qualifyers.iterator();
				while(iterator.hasNext())
				{
					Qualifyer qualifyer = (Qualifyer)iterator.next();
					HashMap argument = new HashMap();
					argument.put(qualifyer.getName(), qualifyer.getValue());
					qualifyerList.add(argument);
				}
		        
		        object = serviceDefinitionVO;
		        object2 = qualifyerList;
	        }
			else
			{
			    object = new NullObject();
		    	object2 = new NullObject();
			}
			

			CacheController.cacheObject("serviceDefinitionCache", key, object);
			CacheController.cacheObject("qualifyerListCache", key, object2);

		}
			
			
		/*
	    Collection serviceBindings = siteNodeVersion.getServiceBindings();
		Iterator serviceBindingIterator = serviceBindings.iterator();
		while(serviceBindingIterator.hasNext())
		{
			ServiceBinding serviceBindingCandidate = (ServiceBinding)serviceBindingIterator.next();
			//getLogger().warn("siteNodeVersion " + siteNodeVersion.getId());
			if(serviceBindingCandidate.getAvailableServiceBinding().getAvailableServiceBindingId().intValue() == availableServiceBindingVO.getId().intValue())
			//if(serviceBindingCandidate.getValueObject().getAvailableServiceBindingId().intValue() == availableServiceBindingVO.getId().intValue())
			{
				serviceBinding = serviceBindingCandidate;
			}
		}
		*/
		
		return serviceDefinitionVO;
	}
	
	/**
	 * This method fetches an available service binding as long as there is one associated with this site nodes type definition or any
	 * of the parent site node type definitions.
	 */
	
	private AvailableServiceBinding getAvailableServiceBindingRecursive(SiteNode siteNode, String availableServiceBindingName, boolean inheritParentBindings)
	{
		if(siteNode == null || availableServiceBindingName == null)
			return null;
		
		AvailableServiceBinding availableServiceBinding = null;
		
		SiteNodeTypeDefinition siteNodeTypeDefinition = siteNode.getSiteNodeTypeDefinition();
		if(siteNodeTypeDefinition != null)
		{
			Collection availableServiceBindings = siteNodeTypeDefinition.getAvailableServiceBindings();
			
			Iterator availableServiceBindingsIterator = availableServiceBindings.iterator();
			while(availableServiceBindingsIterator.hasNext())
			{
				AvailableServiceBinding currentAvailableServiceBinding = (AvailableServiceBinding)availableServiceBindingsIterator.next();
				if(currentAvailableServiceBinding.getName().equalsIgnoreCase(availableServiceBindingName))
				{
					availableServiceBinding = currentAvailableServiceBinding;
				}
			}
		}
				
		if(availableServiceBinding == null)
			availableServiceBinding = getAvailableServiceBindingRecursive(siteNode.getParentSiteNode(), availableServiceBindingName, inheritParentBindings);
			
		return availableServiceBinding;
	}	


	/**
	 * This method returns the SiteNodeVO that is sent in.
	 */
	
	public SiteNode getSiteNode(Database db, Integer siteNodeId) throws SystemException
	{
		SiteNode siteNode = null;
		
		siteNode = (SiteNode)getObjectWithId(SiteNodeImpl.class, siteNodeId, db);

		return siteNode;
	}

	/**
	 * This method returns the latest sitenodeVersion there is for the given siteNode.
	 */
	
	public SiteNodeVersionVO getLatestActiveSiteNodeVersionVO(Database db, Integer siteNodeId) throws SystemException, Exception
	{
		String key = "" + siteNodeId;
		getLogger().info("key:" + key);
		SiteNodeVersionVO siteNodeVersionVO = (SiteNodeVersionVO)CacheController.getCachedObject("latestSiteNodeVersionCache", key);
		if(siteNodeVersionVO != null)
		{
			getLogger().info("There was an cached siteNodeVersionVO:" + siteNodeVersionVO);
		}
		else
		{
			SiteNodeVersion siteNodeVersion = getLatestActiveSiteNodeVersion(siteNodeId, db);
			if(siteNodeVersion != null)
				siteNodeVersionVO = siteNodeVersion.getValueObject();
			
			CacheController.cacheObject("latestSiteNodeVersionCache", key, siteNodeVersionVO);
		}
				
		return siteNodeVersionVO;
	}

	/**
	 * This method returns the latest sitenodeVersion there is for the given siteNode and stores it in a cache special to the deliver page cache.
	 */
	
	public SiteNodeVersionVO getLatestActiveSiteNodeVersionVOForPageCache(Database db, Integer siteNodeId) throws SystemException, Exception
	{
		String key = "" + siteNodeId;
		getLogger().info("key:" + key);
		SiteNodeVersionVO siteNodeVersionVO = (SiteNodeVersionVO)CacheController.getCachedObject("pageCacheLatestSiteNodeVersions", key);
		if(siteNodeVersionVO != null)
		{
			getLogger().info("There was an cached siteNodeVersionVO:" + siteNodeVersionVO);
		}
		else
		{
			SiteNodeVersion siteNodeVersion = getLatestActiveSiteNodeVersion(siteNodeId, db);
			if(siteNodeVersion != null)
				siteNodeVersionVO = siteNodeVersion.getValueObject();
			
			CacheController.cacheObject("pageCacheLatestSiteNodeVersions", key, siteNodeVersionVO);
		}
				
		return siteNodeVersionVO;
	}


	/**
	 * This method returns the latest sitenodeVersion there is for the given siteNode.
	 */
	
	public SiteNodeVersion getLatestActiveSiteNodeVersion(Integer siteNodeId, Database db) throws SystemException, Exception
	{
		SiteNodeVersion siteNodeVersion = null;
		
		getLogger().info("Loading siteNode " + siteNodeId);
		SiteNode siteNode = (SiteNode)this.getObjectWithId(SiteNodeImpl.class, siteNodeId, db);
		getLogger().info("siteNode " + siteNode.getName());
		Collection siteNodeVersions = siteNode.getSiteNodeVersions();
		getLogger().info("siteNodeVersions " + siteNodeVersions);
		
		Iterator versionIterator = siteNodeVersions.iterator();
		while(versionIterator.hasNext())
		{
			SiteNodeVersion siteNodeVersionCandidate = (SiteNodeVersion)versionIterator.next();	
			getLogger().info("SiteNodeVersionCandidate " + siteNodeVersionCandidate.getId());
			if(siteNodeVersionCandidate.getIsActive().booleanValue() && siteNodeVersionCandidate.getStateId().intValue() >= getOperatingMode().intValue())
			{
				if(siteNodeVersionCandidate.getOwningSiteNode().getSiteNodeId().intValue() == siteNodeId.intValue())
				{
					if(siteNodeVersion == null || siteNodeVersion.getSiteNodeVersionId().intValue() < siteNodeVersionCandidate.getId().intValue())
					{
						siteNodeVersion = siteNodeVersionCandidate;
					}
				}
			}
		}

		return siteNodeVersion;
	}
	

	/**
	 * This method returns the SiteNodeVO that is the parent to the one sent in.
	 */
	
	public SiteNodeVO getParentSiteNode(Database db, Integer siteNodeId) throws SystemException
	{
		String key = "" + siteNodeId;
		getLogger().info("key getParentSiteNode:" + key);
		Object object = CacheController.getCachedObject("parentSiteNodeCache", key);
		SiteNodeVO parentSiteNodeVO = null;
		getLogger().info("object:" + object);
		if(object instanceof NullObject)
		{
			getLogger().info("There was an cached parentSiteNodeVO but it was null:" + object);
		}
		else if(object != null)
		{
			getLogger().info("There was an cached parentSiteNodeVO:" + parentSiteNodeVO);
			parentSiteNodeVO = (SiteNodeVO)object;
		}
		else
		{
			getLogger().info("There was no cached parentSiteNodeVO:" + parentSiteNodeVO);
			
			SiteNode siteNode = (SiteNode)getObjectWithId(SmallSiteNodeImpl.class, siteNodeId, db);
            SiteNode parentSiteNode = siteNode.getParentSiteNode();
            if(parentSiteNode != null)		
            {
                parentSiteNodeVO = parentSiteNode.getValueObject();
            	CacheController.cacheObject("parentSiteNodeCache", key, parentSiteNodeVO);
    			getLogger().info("Caching parentSiteNodeVO:" + parentSiteNodeVO);
            }
            else
            {
                getLogger().info("Caching parentSiteNodeVO: NullObject");
                CacheController.cacheObject("parentSiteNodeCache", key, new NullObject());
            }
            
			
		}
		
		return parentSiteNodeVO;
	}

	/**
	 * This method returns the SiteNodeVO that is the parent to the one sent in.
	 */
	
	public SiteNodeVO getParentSiteNodeForPageCache(Database db, Integer siteNodeId) throws SystemException
	{
		String key = "" + siteNodeId;
		getLogger().info("key getParentSiteNode:" + key);
		Object object = CacheController.getCachedObject("pageCacheParentSiteNodeCache", key);
		SiteNodeVO parentSiteNodeVO = null;
		getLogger().info("object:" + object);
		if(object instanceof NullObject)
		{
			getLogger().info("There was an cached parentSiteNodeVO but it was null:" + object);
		}
		else if(object != null)
		{
			getLogger().info("There was an cached parentSiteNodeVO:" + parentSiteNodeVO);
			parentSiteNodeVO = (SiteNodeVO)object;
		}
		else
		{
			getLogger().info("There was no cached parentSiteNodeVO:" + parentSiteNodeVO);
			
			SiteNode siteNode = (SiteNode)getObjectWithId(SmallSiteNodeImpl.class, siteNodeId, db);
            SiteNode parentSiteNode = siteNode.getParentSiteNode();
            if(parentSiteNode != null)		
            {
                parentSiteNodeVO = parentSiteNode.getValueObject();
            	CacheController.cacheObject("pageCacheParentSiteNodeCache", key, parentSiteNodeVO);
    			getLogger().info("Caching parentSiteNodeVO:" + parentSiteNodeVO);
            }
            else
            {
                getLogger().info("Caching parentSiteNodeVO: NullObject");
                CacheController.cacheObject("pageCacheParentSiteNodeCache", key, new NullObject());
            }
            
			
		}
		
		return parentSiteNodeVO;
	}


	/**
	 * This method returns true if the if the page in question (ie sitenode) has page-caching disabled.
	 * This is essential to turn off when you have a dynamic page like an external application or searchresult.
	 */
	
	public boolean getIsPageCacheDisabled(Database db, Integer siteNodeId)
	{
		boolean isPageCacheDisabled = false;
		
		try
		{
			SiteNodeVersionVO latestSiteNodeVersionVO = getLatestActiveSiteNodeVersionVO(db, siteNodeId);
			if(latestSiteNodeVersionVO != null && latestSiteNodeVersionVO.getDisablePageCache() != null)
			{	
				if(latestSiteNodeVersionVO.getDisablePageCache().intValue() == NO.intValue())
					isPageCacheDisabled = false;
				else if(latestSiteNodeVersionVO.getDisablePageCache().intValue() == YES.intValue())
					isPageCacheDisabled = true;
				else if(latestSiteNodeVersionVO.getDisablePageCache().intValue() == INHERITED.intValue())
				{
					SiteNodeVO parentSiteNode = this.getParentSiteNode(db, siteNodeId);
					if(parentSiteNode != null)
						isPageCacheDisabled = getIsPageCacheDisabled(db, parentSiteNode.getSiteNodeId()); 
				}
			}

		}
		catch(Exception e)
		{
			getLogger().warn("An error occurred trying to get if the siteNodeVersion has disabled pageCache:" + e.getMessage(), e);
		}
				
		return isPageCacheDisabled;
	}

	/**
	 * This method returns the pageCacheKey for the page.
	 */
	
	public String getPageCacheKey(Database db, HttpSession session, TemplateController templateController, Integer siteNodeId, Integer languageId, Integer contentId, String userAgent, String queryString, String extra)
	{
	    String pageKey = CacheController.getPageCacheKey(session, templateController, siteNodeId, languageId, contentId, userAgent, queryString, extra);

		try
		{
			SiteNodeVersionVO latestSiteNodeVersionVO = getLatestActiveSiteNodeVersionVOForPageCache(db, siteNodeId);
			if(latestSiteNodeVersionVO != null && latestSiteNodeVersionVO.getPageCacheKey() != null && latestSiteNodeVersionVO.getPageCacheKey().length() > 0 && !latestSiteNodeVersionVO.getPageCacheKey().equalsIgnoreCase("default"))
			{
			    pageKey = latestSiteNodeVersionVO.getPageCacheKey();
			    pageKey = pageKey.replaceAll("\\$siteNodeId", "" + siteNodeId);
			    pageKey = pageKey.replaceAll("\\$languageId", "" + languageId);
			    pageKey = pageKey.replaceAll("\\$contentId", "" + contentId);
			    pageKey = pageKey.replaceAll("\\$useragent", "" + userAgent);
			    pageKey = pageKey.replaceAll("\\$queryString", "" + queryString);
	    	
	    	    int sessionAttributeStartIndex = pageKey.indexOf("$session.");
	    	    while(sessionAttributeStartIndex > -1)
	    	    {
	        	    int sessionAttributeEndIndex = pageKey.indexOf("_", sessionAttributeStartIndex);
	        	    String sessionAttribute = null;
	        	    if(sessionAttributeEndIndex > -1)
	        	        sessionAttribute = pageKey.substring(sessionAttributeStartIndex + 9, sessionAttributeEndIndex);
	        	    else
	        	        sessionAttribute = pageKey.substring(sessionAttributeStartIndex + 9);

	        	    pageKey = pageKey.replaceAll("\\$session." + sessionAttribute, "" + session.getAttribute(sessionAttribute));    	    
	    	    
	        	    sessionAttributeStartIndex = pageKey.indexOf("$session.");
	    	    }

	    	    int cookieAttributeStartIndex = pageKey.indexOf("$cookie.");
	    	    while(cookieAttributeStartIndex > -1)
	    	    {
	        	    int cookieAttributeEndIndex = pageKey.indexOf("_", cookieAttributeStartIndex);
		    	    String cookieAttribute = null;
	        	    if(cookieAttributeEndIndex > -1)
	        	        cookieAttribute = pageKey.substring(cookieAttributeStartIndex + 8, cookieAttributeEndIndex);
	        	    else
	        	        cookieAttribute = pageKey.substring(cookieAttributeStartIndex + 8);

	        	    pageKey = pageKey.replaceAll("\\$cookie." + cookieAttribute, "" + templateController.getCookie(cookieAttribute));    	    
	    	    
	        	    cookieAttributeStartIndex = pageKey.indexOf("$cookie.");
	    	    }

			}

		}
		catch(Exception e)
		{
			getLogger().warn("An error occurred trying to get if the siteNodeVersion had a different pageCacheKey:" + e.getMessage(), e);
			e.printStackTrace();
		}

		return pageKey;
	}

	/**
	 * This method returns true if the if the page in question (ie sitenode) has editOnSight disabled.
	 */
	
	public boolean getIsEditOnSightDisabled(Database db, Integer siteNodeId)
	{
		getLogger().info("getIsEditOnSightDisabled:" + siteNodeId);
		
		boolean isEditOnSightDisabled = false;
		
		try
		{
			SiteNodeVersionVO latestSiteNodeVersionVO = getLatestActiveSiteNodeVersionVO(db, siteNodeId);
			if(latestSiteNodeVersionVO != null && latestSiteNodeVersionVO.getDisableEditOnSight() != null)
			{	
				if(latestSiteNodeVersionVO.getDisableEditOnSight().intValue() == NO.intValue())
					isEditOnSightDisabled = false;
				else if(latestSiteNodeVersionVO.getDisableEditOnSight().intValue() == YES.intValue())
					isEditOnSightDisabled = true;
				else if(latestSiteNodeVersionVO.getDisableEditOnSight().intValue() == INHERITED.intValue())
				{
					SiteNodeVO parentSiteNode = this.getParentSiteNode(db, siteNodeId);
					if(parentSiteNode != null)
						isEditOnSightDisabled = getIsEditOnSightDisabled(db, parentSiteNode.getSiteNodeId()); 
				}
			}

		}
		catch(Exception e)
		{
			getLogger().warn("An error occurred trying to get if the siteNodeVersion has disabled pageCache:" + e.getMessage(), e);
		}
			
		getLogger().info("getIsEditOnSightDisabled:" + isEditOnSightDisabled);
		
		return isEditOnSightDisabled;
	}
	
	/**
	 * This method returns true if the if the page in question (ie sitenode) is protected byt the exctranet fnctionality.
	 * This is essential to turn off when you have a dynamic page like an external application or searchresult.
	 */
	
	public boolean getIsPageProtected(Database db, Integer siteNodeId)
	{
		boolean isPageProtected = false;
		
		try
		{
			SiteNodeVersionVO latestSiteNodeVersionVO = getLatestActiveSiteNodeVersionVO(db, siteNodeId);
			if(latestSiteNodeVersionVO != null && latestSiteNodeVersionVO.getIsProtected() != null)
			{	
				if(latestSiteNodeVersionVO.getIsProtected().intValue() == NO.intValue())
					isPageProtected = false;
				else if(latestSiteNodeVersionVO.getIsProtected().intValue() == YES.intValue())
					isPageProtected = true;
				else if(latestSiteNodeVersionVO.getIsProtected().intValue() == INHERITED.intValue())
				{
					SiteNodeVO parentSiteNode = this.getParentSiteNode(db, siteNodeId);
					if(parentSiteNode != null)
						isPageProtected = getIsPageProtected(db, parentSiteNode.getSiteNodeId()); 
				}
			}

		}
		catch(Exception e)
		{
			getLogger().warn("An error occurred trying to get if the siteNodeVersion has disabled pageCache:" + e.getMessage(), e);
		}
				
		return isPageProtected;
	}
	
	
	/**
	 * This method returns the id of the siteNodeVersion that is protected if any.
	 */
	
	public Integer getProtectedSiteNodeVersionId(Database db, Integer siteNodeId)
	{
		Integer protectedSiteNodeVersionId = null;
		
		try
		{
			SiteNodeVersionVO siteNodeVersionVO = getLatestActiveSiteNodeVersionVO(db, siteNodeId);
			getLogger().info("siteNodeId:" + siteNodeId);
			if(siteNodeVersionVO != null && siteNodeVersionVO.getIsProtected() != null)
			{	
				getLogger().info("siteNodeVersionVO:" + siteNodeVersionVO.getId() + ":" + siteNodeVersionVO.getIsProtected());
				if(siteNodeVersionVO.getIsProtected().intValue() == NO.intValue())
					protectedSiteNodeVersionId = null;
				else if(siteNodeVersionVO.getIsProtected().intValue() == YES.intValue())
					protectedSiteNodeVersionId = siteNodeVersionVO.getId();
				else if(siteNodeVersionVO.getIsProtected().intValue() == INHERITED.intValue())
				{
					SiteNodeVO parentSiteNode = this.getParentSiteNode(db, siteNodeId);
					if(parentSiteNode != null)
						protectedSiteNodeVersionId = getProtectedSiteNodeVersionId(db, parentSiteNode.getSiteNodeId()); 
				}
			}

		}
		catch(Exception e)
		{
			getLogger().warn("An error occurred trying to get if the siteNodeVersion has disabled pageCache:" + e.getMessage(), e);
		}
				
		return protectedSiteNodeVersionId;
	}


	/**
	 * This method returns the id of the siteNodeVersion that is protected if any.
	 */
	
	public Integer getProtectedSiteNodeVersionIdForPageCache(Database db, Integer siteNodeId)
	{
		Integer protectedSiteNodeVersionId = null;
		
		try
		{
			SiteNodeVersionVO siteNodeVersionVO = this.getLatestActiveSiteNodeVersionVOForPageCache(db, siteNodeId);
			getLogger().info("siteNodeId:" + siteNodeId);
			if(siteNodeVersionVO != null && siteNodeVersionVO.getIsProtected() != null)
			{	
				getLogger().info("siteNodeVersionVO:" + siteNodeVersionVO.getId() + ":" + siteNodeVersionVO.getIsProtected());
				if(siteNodeVersionVO.getIsProtected().intValue() == NO.intValue())
					protectedSiteNodeVersionId = null;
				else if(siteNodeVersionVO.getIsProtected().intValue() == YES.intValue())
					protectedSiteNodeVersionId = siteNodeVersionVO.getId();
				else if(siteNodeVersionVO.getIsProtected().intValue() == INHERITED.intValue())
				{
					SiteNodeVO parentSiteNode = this.getParentSiteNodeForPageCache(db, siteNodeId);
					if(parentSiteNode != null)
						protectedSiteNodeVersionId = getProtectedSiteNodeVersionIdForPageCache(db, parentSiteNode.getSiteNodeId()); 
				}
			}

		}
		catch(Exception e)
		{
			getLogger().warn("An error occurred trying to get if the siteNodeVersion has disabled pageCache:" + e.getMessage(), e);
		}
				
		return protectedSiteNodeVersionId;
	}

	
	/**
	 * This method returns the id of the siteNodeVersion that has disabled languages if any.
	 */
	
	public Integer getDisabledLanguagesSiteNodeVersionId(Database db, Integer siteNodeId)
	{
		Integer disabledLanguagesSiteNodeVersionId = null;
		
		try
		{
			SiteNodeVersionVO siteNodeVersionVO = this.getLatestActiveSiteNodeVersionVOForPageCache(db, siteNodeId);
			getLogger().info("siteNodeId:" + siteNodeId);
			if(siteNodeVersionVO != null && siteNodeVersionVO.getDisableLanguages() != null)
			{	
				getLogger().info("siteNodeVersionVO:" + siteNodeVersionVO.getId() + ":" + siteNodeVersionVO.getDisableLanguages());
				if(siteNodeVersionVO.getDisableLanguages().intValue() == NO.intValue())
				    disabledLanguagesSiteNodeVersionId = null;
				else if(siteNodeVersionVO.getDisableLanguages().intValue() == YES.intValue())
				    disabledLanguagesSiteNodeVersionId = siteNodeVersionVO.getId();
				else if(siteNodeVersionVO.getDisableLanguages().intValue() == INHERITED.intValue())
				{
					SiteNodeVO parentSiteNode = this.getParentSiteNodeForPageCache(db, siteNodeId);
					if(parentSiteNode != null)
					    disabledLanguagesSiteNodeVersionId = getDisabledLanguagesSiteNodeVersionId(db, parentSiteNode.getSiteNodeId()); 
				}
			}

		}
		catch(Exception e)
		{
			getLogger().warn("An error occurred trying to get if the siteNodeVersion has disabled languages:" + e.getMessage(), e);
		}
				
		return disabledLanguagesSiteNodeVersionId;
	}

	/**
	 * This method return a single content bound. 
	 */
	
	public ContentVO getBoundContent(Database db, InfoGluePrincipal infoGluePrincipal, Integer siteNodeId, Integer languageId, boolean useLanguageFallback, String availableServiceBindingName, boolean inheritParentBindings, DeliveryContext deliveryContext) throws SystemException, Exception
	{
		List contents = getBoundContents(db, infoGluePrincipal, siteNodeId, languageId, useLanguageFallback, availableServiceBindingName, inheritParentBindings, false, deliveryContext);
		return (contents != null && contents.size() > 0) ? (ContentVO)contents.get(0) : null;
	}


	/**
	 * This method return a single content bound. 
	 */
	
	public ContentVO getBoundContent(Database db, InfoGluePrincipal infoGluePrincipal, Integer siteNodeId, Integer languageId, boolean useLanguageFallback, String availableServiceBindingName, boolean inheritParentBindings, boolean includeFolders, DeliveryContext deliveryContext) throws SystemException, Exception
	{
		List contents = getBoundContents(db, infoGluePrincipal, siteNodeId, languageId, useLanguageFallback, availableServiceBindingName, inheritParentBindings, includeFolders, deliveryContext);
		return (contents != null && contents.size() > 0) ? (ContentVO)contents.get(0) : null;
	}

	/**
	 * This method return a single content bound. 
	 */
	
	public ContentVO getBoundContent(Database db, InfoGluePrincipal infoGluePrincipal, Integer siteNodeId, Integer languageId, boolean useLanguageFallback, String availableServiceBindingName, DeliveryContext deliveryContext) throws SystemException, Exception
	{
		getLogger().info("siteNodeId:" + siteNodeId);
		getLogger().info("availableServiceBindingName:" + availableServiceBindingName);
		List contents = getBoundContents(db, infoGluePrincipal, siteNodeId, languageId, useLanguageFallback, availableServiceBindingName, USE_INHERITANCE, true, deliveryContext);
		return (contents != null && contents.size() > 0) ? (ContentVO)contents.get(0) : null;
	}
	
	/**
	 * This method return a single content bound. 
	 */
	
	public ContentVO getBoundContent(InfoGluePrincipal infoGluePrincipal, Integer siteNodeId, Integer languageId, boolean useLanguageFallback, String availableServiceBindingName, DeliveryContext deliveryContext) throws SystemException, Exception
	{
	    List contents = null;
	    
	    Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

		beginTransaction(db);

        try
        {
            contents = getBoundContents(db, infoGluePrincipal, siteNodeId, languageId, useLanguageFallback, availableServiceBindingName, USE_INHERITANCE, true, deliveryContext);
        
            closeTransaction(db);
	    }
	    catch(Exception e)
	    {
	        getLogger().error("An error occurred so we should not complete the transaction:" + e, e);
			rollbackTransaction(db);
	        throw new SystemException(e.getMessage());
	    }

        return (contents != null && contents.size() > 0) ? (ContentVO)contents.get(0) : null;
	}


	/**
	 * This method returns a list of contents bound to the named availableServiceBinding.
	 */
	
	public List getBoundContents(Database db, InfoGluePrincipal infoGluePrincipal, Integer siteNodeId, Integer languageId, boolean useLanguageFallback, String availableServiceBindingName, boolean inheritParentBindings, boolean includeFolders, DeliveryContext deliveryContext) throws SystemException, Exception
	{
		String boundContentsKey = "" + infoGluePrincipal.getName() + "_" + siteNodeId + "_" + languageId + "_" + useLanguageFallback + "_" + includeFolders + "_" + availableServiceBindingName + "_" + USE_INHERITANCE;
		//String boundContentsKey = "" + siteNodeId + "_" + availableServiceBindingName + "_" + USE_INHERITANCE;
		getLogger().info("boundContentsKey:" + boundContentsKey);
		List boundContentVOList = (List)CacheController.getCachedObject("boundContentCache", boundContentsKey);
		if(boundContentVOList != null)
		{
			getLogger().info("There was an cached content boundContentVOList:" + boundContentVOList.size());
		}
		else
		{
		    boundContentVOList = new ArrayList();
			
			getLogger().info("Coming in with:" + siteNodeId + " and " + availableServiceBindingName);

		    AvailableServiceBindingVO availableServiceBindingVO = AvailableServiceBindingDeliveryController.getAvailableServiceBindingDeliveryController().getAvailableServiceBindingVO(availableServiceBindingName, db);
        
		    List qualifyerList = new ArrayList();
	    	ServiceDefinitionVO serviceDefinitionVO = getInheritedServiceDefinition(qualifyerList, siteNodeId, availableServiceBindingVO, db, inheritParentBindings);
			if(serviceDefinitionVO != null)
			{
				String serviceClassName = serviceDefinitionVO.getClassName();
				BaseService service = (BaseService)Class.forName(serviceClassName).newInstance();
    		 
				HashMap arguments = new HashMap();
				arguments.put("method", "selectContentListOnIdList");
        		
				arguments.put("arguments", qualifyerList);
				
				List contents = service.selectMatchingEntities(arguments, db);
				
				getLogger().info("Found bound contents:" + contents.size());	        		
				if(contents != null)
				{
					Iterator i = contents.iterator();
					while(i.hasNext())
					{
						ContentVO candidate = (ContentVO)i.next();
						getLogger().info("candidate:" + candidate.getName());
						//Checking to see that now is between the contents publish and expire-date. 
						//if(ContentDeliveryController.getContentDeliveryController().isValidContent(candidate.getId(), languageId, useLanguageFallback, infoGluePrincipal))
						//	boundContentVOList.add(candidate);        		
						Content candidateContent = (Content)getObjectWithId(ContentImpl.class, candidate.getId(), db); 
						
						getLogger().info("candidateContent:" + candidateContent.getName());
						if(ContentDeliveryController.getContentDeliveryController().isValidContent(infoGluePrincipal, candidateContent, languageId, useLanguageFallback, includeFolders, db, deliveryContext))
						{
							deliveryContext.addUsedContent("content_" + candidate.getId());

						    boundContentVOList.add(candidate);    
						}
					}
				}
			}
			
			CacheController.cacheObject("boundContentCache", boundContentsKey, boundContentVOList);
		}
		
		return boundContentVOList;

	}

	

	/**
	 * This method returns a list of children to the bound content with the named availableServiceBindingName.
	 * The collection of contents are also sorted on given arguments.
	 */
	
	public List getBoundFolderContents(Database db, InfoGluePrincipal infoGluePrincipal, Integer siteNodeId, Integer languageId, String availableServiceBindingName, boolean searchRecursive, Integer maximumNumberOfLevels, String sortAttribute, String sortOrder, boolean useLanguageFallback, boolean includeFolders, DeliveryContext deliveryContext) throws SystemException, Exception
	{
		List folderContents = new ArrayList();
		
		deliveryContext.addUsedContent("selectiveCacheUpdateNonApplicable");
		
    	getLogger().info("Coming in with:" + siteNodeId + " and " + availableServiceBindingName + " and " + searchRecursive + " and " + maximumNumberOfLevels + " and " + sortAttribute + " and " + sortOrder);
        
        ContentVO contentVO = getBoundContent(db, infoGluePrincipal, siteNodeId, languageId, useLanguageFallback, availableServiceBindingName, includeFolders, deliveryContext);
        getLogger().info("contentVO:" + contentVO);
        
        if(contentVO != null)
        {
           	folderContents = ContentDeliveryController.getContentDeliveryController().getSortedChildContents(infoGluePrincipal, languageId, contentVO.getContentId(), siteNodeId, db, searchRecursive, maximumNumberOfLevels, sortAttribute, sortOrder, useLanguageFallback, includeFolders, deliveryContext);
        }
        
		return folderContents;
	}


	/**
	 * This method returns a list of children to the bound content with the named availableServiceBindingName.
	 * The collection of contents are also sorted on given arguments.
	 */
	
	public List getBoundFolderContents(Database db, InfoGluePrincipal infoGluePrincipal, Integer contentId, Integer languageId, boolean searchRecursive, Integer maximumNumberOfLevels, String sortAttribute, String sortOrder, boolean useLanguageFallback, boolean includeFolders, DeliveryContext deliveryContext) throws SystemException, Exception
	{
		List folderContents = new ArrayList();
		        
		folderContents = ContentDeliveryController.getContentDeliveryController().getSortedChildContents(infoGluePrincipal, languageId, contentId, siteNodeId, db, searchRecursive, maximumNumberOfLevels, sortAttribute, sortOrder, useLanguageFallback, includeFolders, deliveryContext);
		
		return folderContents;
	}


	/**
	 * This method return a single siteNode bound. 
	 */
	
	public SiteNodeVO getBoundSiteNode(Database db, Integer siteNodeId, String availableServiceBindingName) throws SystemException, Exception
	{
		List siteNodes = getBoundSiteNodes(db, siteNodeId, availableServiceBindingName);
		return (siteNodes != null && siteNodes.size() > 0) ? (SiteNodeVO)siteNodes.get(0) : null;
	}
	

	/**
	 * This method return a single siteNode bound. 
	 */
	
	public SiteNodeVO getBoundSiteNode(Database db, Integer siteNodeId, String availableServiceBindingName, int position) throws SystemException, Exception
	{
		List siteNodes = getBoundSiteNodes(db, siteNodeId, availableServiceBindingName);
		return (siteNodes != null && siteNodes.size() > position) ? (SiteNodeVO)siteNodes.get(position) : null;
	}


	/**
	 * This method should be rewritten later....
	 * The concept is to fetch the bound siteNode
	 */
	
	public List getBoundSiteNodes(Database db, Integer siteNodeId, String availableServiceBindingName) throws SystemException, Exception
	{
		String boundSiteNodesKey = "" + siteNodeId + "_" + availableServiceBindingName + "_" + USE_INHERITANCE;
		getLogger().info("boundSiteNodesKey:" + boundSiteNodesKey);
		List boundSiteNodeVOList = (List)CacheController.getCachedObject("boundSiteNodeCache", boundSiteNodesKey);
		if(boundSiteNodeVOList != null)
		{
			getLogger().info("There was an cached content boundSiteNodeVOList:" + boundSiteNodeVOList.size());
		}
		else
		{
			boundSiteNodeVOList = new ArrayList();

		    AvailableServiceBindingVO availableServiceBindingVO = AvailableServiceBindingDeliveryController.getAvailableServiceBindingDeliveryController().getAvailableServiceBindingVO(availableServiceBindingName, db);

		    List qualifyerList = new ArrayList();
	    	ServiceDefinitionVO serviceDefinitionVO = getInheritedServiceDefinition(qualifyerList, siteNodeId, availableServiceBindingVO, db, USE_INHERITANCE);
			
		    if(serviceDefinitionVO != null)
			{
				String serviceClassName = serviceDefinitionVO.getClassName();
				BaseService service = (BaseService)Class.forName(serviceClassName).newInstance();
    		 	
				HashMap arguments = new HashMap();
				arguments.put("method", "selectSiteNodeListOnIdList");
        	
				arguments.put("arguments", qualifyerList);
    		
				List siteNodes = service.selectMatchingEntities(arguments, db);
    		
				getLogger().info("Found bound siteNodes:" + siteNodes.size());
				if(siteNodes != null)
				{
					Iterator i = siteNodes.iterator();
					while(i.hasNext())
					{
						SiteNodeVO candidate = (SiteNodeVO)i.next();
						//Checking to see that now is between the contents publish and expire-date. 
						if(isValidSiteNode(db, candidate.getId()))
							boundSiteNodeVOList.add(candidate);
					}
				}
			}
	       	        						
			CacheController.cacheObject("boundSiteNodeCache", boundSiteNodesKey, boundSiteNodeVOList);
		}
		
		return boundSiteNodeVOList;
	}


	
	/**
	 * This method returns a url to the given page. The url is composed of siteNode, language and content
	 */

	public String getPageUrl(Database db, InfoGluePrincipal infoGluePrincipal, Integer siteNodeId, Integer languageId, Integer contentId, DeliveryContext deliveryContext) throws SystemException
	{
		String pageUrl = "";
		
		/*
		if(siteNodeId == null)
			siteNodeId = new Integer(-1);

		if(languageId == null)
			languageId = new Integer(-1);

		if(contentId == null)
			contentId = new Integer(-1);
		
		//String arguments = "siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId;
		
		SiteNode siteNode = getSiteNode(db, siteNodeId);
		String dnsName = CmsPropertyHandler.getProperty("webServerAddress");
		if(siteNode != null && siteNode.getRepository().getDnsName() != null && !siteNode.getRepository().getDnsName().equals(""))
			dnsName = siteNode.getRepository().getDnsName();
			
		//pageUrl = dnsName + "/" + CmsPropertyHandler.getProperty("applicationBaseAction") + "?" + arguments;
		*/

		pageUrl = urlComposer.composePageUrl(db, infoGluePrincipal, siteNodeId, languageId, contentId, deliveryContext); 
		
		return pageUrl;
	}


	public String getPageUrlAfterLanguageChange(Database db, InfoGluePrincipal infoGluePrincipal, Integer siteNodeId, Integer languageId, Integer contentId, DeliveryContext deliveryContext) throws SystemException
    {
		/*
	    SiteNode siteNode = getSiteNode(db, siteNodeId);
		String dnsName = CmsPropertyHandler.getProperty("webServerAddress");
		if(siteNode != null && siteNode.getRepository().getDnsName() != null && !siteNode.getRepository().getDnsName().equals(""))
			dnsName = siteNode.getRepository().getDnsName();
		*/
        return urlComposer.composePageUrlAfterLanguageChange(db, infoGluePrincipal, siteNodeId, languageId, contentId, deliveryContext);
    } 
	
	/**
	 * This method constructs a string representing the path to the page with respect to where in the
	 * structure the page is. It also takes the page title into consideration. It is done by recursively going
	 * up in the structure until the root is reached. On each node we collect the pageTitle.
	 */

	public String getPagePath(Database db, InfoGluePrincipal infoGluePrincipal, Integer siteNodeId, Integer languageId, Integer contentId, String bindingName, String attributeName, boolean useLanguageFallBack, DeliveryContext deliveryContext) throws SystemException, Exception
	{
		String pagePath = "/";
		
		SiteNodeVO parentSiteNode = this.getParentSiteNode(db, siteNodeId);
		if(parentSiteNode != null)
		{
			pagePath = getPagePath(db, infoGluePrincipal, parentSiteNode.getId(), languageId, null, bindingName, attributeName, useLanguageFallBack, deliveryContext) + "/"; 
		}
		
		pagePath += this.getPageNavigationTitle(db, infoGluePrincipal, siteNodeId, languageId, contentId, bindingName, attributeName, useLanguageFallBack, deliveryContext, false);
		pagePath = pagePath.replaceAll(" ", "_");
		
		return pagePath;
	}


	/**
	 * This method returns a url to the delivery engine
	 */
	public String getPageBaseUrl(Database db) throws SystemException
	{
		String pageUrl = "";
		
		SiteNode siteNode = this.getSiteNode(db, this.siteNodeId);
		String dnsName = CmsPropertyHandler.getProperty("webServerAddress");
		if(siteNode != null && siteNode.getRepository().getDnsName() != null && !siteNode.getRepository().getDnsName().equals(""))
			dnsName = siteNode.getRepository().getDnsName();
						
		//pageUrl = dnsName + "/" + CmsPropertyHandler.getProperty("applicationBaseAction");
		pageUrl = urlComposer.composePageBaseUrl(dnsName); 
		
		return pageUrl;
	}


	/**
	 * This method returns the navigation-title to the given page. 
	 * The title is based on the content sent in firstly, secondly the siteNode. 
	 * The actual text is fetched from either the content or the metacontent bound to the sitenode. 
	 */
	public String getPageNavigationTitle(Database db, InfoGluePrincipal infoGluePrincipal, Integer siteNodeId, Integer languageId, Integer contentId, String metaBindingName, String attributeName, boolean useLanguageFallback, DeliveryContext deliveryContext, boolean escapeHTML) throws SystemException, Exception
	{
		String navTitle = "";
		
		if(contentId == null || contentId.intValue() == -1)
		{
			ContentVO content = getBoundContent(db, infoGluePrincipal, siteNodeId, languageId, useLanguageFallback, metaBindingName, deliveryContext);
			if(content != null)
				navTitle = ContentDeliveryController.getContentDeliveryController().getContentAttribute(db, content.getContentId(), languageId, attributeName, siteNodeId, useLanguageFallback, deliveryContext, infoGluePrincipal, escapeHTML);
		}
		else
		{
			navTitle = ContentDeliveryController.getContentDeliveryController().getContentAttribute(db, contentId, languageId, attributeName, siteNodeId, useLanguageFallback, deliveryContext, infoGluePrincipal, escapeHTML);
		}
		
		return navTitle;
	}


    public Integer getSiteNodeId(Database db, InfoGluePrincipal infogluePrincipal, Integer repositoryId, String path, String attributeName, Integer parentSiteNodeId, Integer languageId, DeliveryContext deliveryContext) throws SystemException, Exception
    {
        /*
        getLogger().info("repositoryId:" + repositoryId);
        getLogger().info("navigationTitle:" + navigationTitle);
        getLogger().info("parentSiteNodeId:" + parentSiteNodeId);
        getLogger().info("languageId:" + languageId);
        */
    	if (repositoryId == null || repositoryId.intValue() == -1) 
        {
            repositoryId = RepositoryDeliveryController.getRepositoryDeliveryController().getMasterRepository(db).getRepositoryId();
            getLogger().info("RepositoryId not specifed - Resolved master repository to "+repositoryId);
        }
        
        if (repositoryId == null)
            throw new SystemException("No repository given and unable to resolve master repository");

        List languages = LanguageDeliveryController.getLanguageDeliveryController().getAvailableLanguagesForRepository(db, repositoryId);

        List bindings = new ArrayList();
        StringBuffer sb = new StringBuffer(256);
        sb.append("SELECT s FROM ");
        sb.append("org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl s ");
        sb.append("WHERE ");
        if (parentSiteNodeId == null || parentSiteNodeId.intValue() == -1) 
        {
            sb.append("is_undefined(s.parentSiteNode) ");
        } 
        else 
        {
            sb.append("s.parentSiteNode.siteNodeId = $").append((bindings.size()+1)).append(" ");
            bindings.add(parentSiteNodeId);
        }
        
        sb.append("and s.repository.repositoryId = $").append((bindings.size()+1)).append(" ");
        bindings.add(repositoryId);

        getLogger().info("OQL ["+sb.toString()+"]");
        OQLQuery oql = db.getOQLQuery( sb.toString() );
        for (int i=0;i<bindings.size();i++) 
        {
            oql.bind(bindings.get(i));
        }
        
        QueryResults results = oql.execute(Database.ReadOnly);
        while (results.hasMore()) 
        {
            SiteNode siteNode = (SiteNode) results.next();
            if (path == null || path.length() == 0) 
            {
                logger.info("Returning siteNode:" + siteNode.getName());
                return siteNode.getSiteNodeId();
            }
            
            logger.info("Continued with siteNode: " + siteNode.getName());
            ContentVO content = getBoundContent(db, infogluePrincipal, siteNode.getSiteNodeId(), languageId, true, META_INFO_BINDING_NAME, deliveryContext);
            if(content != null) 
            {
                //getLogger().info("Content "+content.getContentId());
                String pathCandidate = null;
                for (int i=0;i<languages.size();i++) 
                {
                    LanguageVO language = (LanguageVO) languages.get(i);
                    //getLogger().info("Language : "+language.getLanguageCode());
                    
                    if(attributeName.equals("SiteNode.name"))
                        pathCandidate = siteNode.getName();
                    else
                    {
	                    pathCandidate = ContentDeliveryController.getContentDeliveryController().getContentAttribute(db, content.getContentId(), language.getLanguageId(), attributeName, siteNode.getSiteNodeId(), true, deliveryContext, infogluePrincipal, false);
	                    
	                    if((pathCandidate == null || pathCandidate.equals("")) && !attributeName.equals(NAV_TITLE_ATTRIBUTE_NAME))
	                        pathCandidate = ContentDeliveryController.getContentDeliveryController().getContentAttribute(db, content.getContentId(), language.getLanguageId(), NAV_TITLE_ATTRIBUTE_NAME, siteNode.getSiteNodeId(), true, deliveryContext, infogluePrincipal, false);
                    }
                    
                    getLogger().info(attributeName + " ["+pathCandidate.trim()+"]==[" + path + "]");
                    if (pathCandidate != null && pathCandidate.toLowerCase().trim().equals(path.toLowerCase())) 
                    {
                        return siteNode.getSiteNodeId();
                    }
                }
            }
        }

        return null;
    }

    public String getPageNavigationPath(Database db, InfoGluePrincipal infogluePrincipal, Integer siteNodeId, Integer languageId, Integer contentId, DeliveryContext deliveryContext) throws SystemException, Exception
    {
        String path = "/";

        SiteNodeVO parentSiteNode = this.getParentSiteNode(db, siteNodeId);
        if (parentSiteNode != null)
        {
            path = getPageNavigationPath(db, infogluePrincipal, parentSiteNode.getId(), languageId, null, deliveryContext) + "/";
        } 
        else 
        {
            return "";
        }
        
        String niceURIEncoding = CmsPropertyHandler.getProperty("niceURIEncoding");
        if(niceURIEncoding == null || niceURIEncoding.length() == 0)
            niceURIEncoding = "UTF-8";
        
        String attributeName = ViewPageFilter.attributeName;
        
        String pathPart;
        
        if(attributeName.equals("SiteNode.name"))
        {
            SiteNode siteNode = this.getSiteNode(db, siteNodeId);
            pathPart = siteNode.getName();
        }
        else
        {
	        pathPart = this.getPageNavigationTitle(db, infogluePrincipal, siteNodeId, languageId, null, META_INFO_BINDING_NAME, attributeName, true, deliveryContext, false);
	        if((pathPart == null || pathPart.equals("")) && !attributeName.equals(NAV_TITLE_ATTRIBUTE_NAME))
	            pathPart = this.getPageNavigationTitle(db, infogluePrincipal, siteNodeId, languageId, null, META_INFO_BINDING_NAME, NAV_TITLE_ATTRIBUTE_NAME, true, deliveryContext, false);
        }
        
        path += URLEncoder.encode(pathPart, niceURIEncoding);
     
        return path;
    }


    public static Integer getSiteNodeIdFromPath(Database db, InfoGluePrincipal infogluePrincipal, Integer repositoryId, String[] path, String attributeName, Integer languageId, DeliveryContext deliveryContext) throws SystemException, Exception
    {
        Integer siteNodeId = null;
        URIMapperCache uriCache = URIMapperCache.getInstance();

        int idx = path.length;
        while (idx >= 0) 
        {
        	//getLogger().info("Looking for cache nodeName at index "+idx);
            siteNodeId = uriCache.getCachedSiteNodeId(repositoryId, path, idx);
            if (siteNodeId != null)
                break;
            idx = idx - 1;
        }
        //getLogger().info("Idx = "+idx);
        for (int i = idx;i < path.length; i++) 
        {
            if (i < 0) 
            {
                siteNodeId = NodeDeliveryController.getNodeDeliveryController(null, null, null).getSiteNodeId(db, infogluePrincipal, repositoryId, null, attributeName, null, languageId, deliveryContext);
            } 
            else 
            {
                siteNodeId = NodeDeliveryController.getNodeDeliveryController(null, null, null).getSiteNodeId(db, infogluePrincipal, repositoryId, path[i], attributeName, siteNodeId, languageId, deliveryContext);
            }
            
            if (siteNodeId != null)
                uriCache.addCachedSiteNodeId(repositoryId, path, i+1, siteNodeId);
        }

        return siteNodeId;
    }
    
    public static Integer getSiteNodeIdFromPath(InfoGluePrincipal infogluePrincipal, Integer repositoryId, String[] path, String attributeName, Integer languageId, DeliveryContext deliveryContext) throws SystemException, Exception
    {
        Integer siteNodeId = null;

        Database db = CastorDatabaseService.getDatabase();
		
		beginTransaction(db);

		try
		{
	        URIMapperCache uriCache = URIMapperCache.getInstance();
	
	        int numberOfPaths = path.length;
	        while (numberOfPaths >= 0) 
	        {
	        	//logger.info("Looking for cached nodeName at index "+idx);
	            siteNodeId = uriCache.getCachedSiteNodeId(repositoryId, path, numberOfPaths);

	            if (siteNodeId != null)
	                break;

	            numberOfPaths = numberOfPaths - 1;
	        }
	        logger.info("numberOfPaths = "+numberOfPaths);
	        for (int i = numberOfPaths;i < path.length; i++) 
	        {
	            if (i < 0) 
	            {
	    	        logger.info("Getting root node");
	                siteNodeId = NodeDeliveryController.getNodeDeliveryController(null, null, null).getSiteNodeId(db, infogluePrincipal, repositoryId, null, attributeName, null, languageId, deliveryContext);
	            } 
	            else 
	            {
	    	        logger.info("Getting normal");
	                siteNodeId = NodeDeliveryController.getNodeDeliveryController(null, null, null).getSiteNodeId(db, infogluePrincipal, repositoryId, path[i], attributeName, siteNodeId, languageId, deliveryContext);
	            }
	            
	            if (siteNodeId != null)
	                uriCache.addCachedSiteNodeId(repositoryId, path, i+1, siteNodeId);
	        }
	     
	        commitTransaction(db);
	    }
		catch(Exception e)
		{
			logger.error("An error occurred so we should not complete the transaction:" + e, e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
		
        return siteNodeId;
    }
	
	/**
	 * This method returns the contentId of the bound metainfo-content to the given page. 
	 */

	public Integer getMetaInfoContentId(Database db, InfoGluePrincipal infoGluePrincipal, Integer siteNodeId, String metaBindingName, boolean inheritParentBindings, DeliveryContext deliveryContext) throws SystemException, Exception
	{
		ContentVO content = getBoundContent(db, infoGluePrincipal, siteNodeId, languageId, true, metaBindingName, inheritParentBindings, deliveryContext);
		if(content != null)
			return content.getContentId();
		
		return null;
	}



	/**
	 * This method returns the root siteNodeVO for the specified repository.
	 * If the repositoryName is null we fetch the name of the master repository.
	 */
	
	public static SiteNodeVO getRootSiteNode(Database db, String repositoryName) throws SystemException, Exception
	{
		if(repositoryName == null)
		{
			repositoryName = RepositoryDeliveryController.getRepositoryDeliveryController().getMasterRepository(db).getName();
			logger.info("Fetched name of master repository as none were given:" + repositoryName);
		}
		 
        SiteNode siteNode = null;

        logger.info("Fetching the root siteNode for the repository " + repositoryName);
		OQLQuery oql = db.getOQLQuery( "SELECT c FROM org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl c WHERE is_undefined(c.parentSiteNode) AND c.repository.name = $1");
		oql.bind(repositoryName);
		
    	QueryResults results = oql.execute(Database.ReadOnly);

		if (results.hasMore()) 
        {
        	siteNode = (SiteNode)results.next();
        	logger.info("The root node was found:" + siteNode.getName());
        }
        
		logger.info("siteNode:" + siteNode);
		
        return (siteNode == null) ? null : siteNode.getValueObject();	
	}
	

	/**
	 * This method returns the root siteNodeVO for the specified repository.
	 * If the repositoryName is null we fetch the name of the master repository.
	 */
	
	public SiteNodeVO getRootSiteNode(Database db, Integer repositoryId) throws SystemException, Exception
	{
	    SiteNodeVO siteNodeVO = null;

        String key = "" + repositoryId;
		getLogger().info("key in getRootSiteNode:" + key);
		siteNodeVO = (SiteNodeVO)CacheController.getCachedObject("rootSiteNodeCache", key);
		if(siteNodeVO != null)
		{
		    getLogger().info("There was an cached master root siteNode:" + siteNodeVO.getName());
		}
		else
		{
			if(repositoryId == null)
			{
				repositoryId = RepositoryDeliveryController.getRepositoryDeliveryController().getMasterRepository(db).getRepositoryId();
				getLogger().info("Fetched name of master repository as none were given:" + repositoryId);
			}
			
	        getLogger().info("Fetching the root siteNode for the repository " + repositoryId);
			OQLQuery oql = db.getOQLQuery( "SELECT c FROM org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl c WHERE is_undefined(c.parentSiteNode) AND c.repository = $1");
			oql.bind(repositoryId);
			
	    	QueryResults results = oql.execute(Database.ReadOnly);
			
	    	if (results.hasMore()) 
	        {
	        	siteNodeVO = ((SiteNode)results.next()).getValueObject();
				getLogger().info("The root node was found:" + siteNodeVO.getName());
	        }

			getLogger().info("siteNodeVO:" + siteNodeVO);

			CacheController.cacheObject("rootSiteNodeCache", key, siteNodeVO);
		}

        return siteNodeVO;	
	}


	
	/**
	 * This method returns the list of siteNodeVO which is children to this one.
	 */
	
	public List getChildSiteNodes(Database db, Integer siteNodeId) throws SystemException, Exception
	{
    	logger.warn("getChildSiteNodes:" + siteNodeId);

    	if(siteNodeId == null)
		{
			return null;
		}

        String key = "" + siteNodeId;
		getLogger().info("key in getChildSiteNodes:" + key);
		List siteNodeVOList = (List)CacheController.getCachedObject("childSiteNodesCache", key);
		if(siteNodeVOList != null)
		{
		    getLogger().info("There was a cached list of child sitenodes:" + siteNodeVOList.size());
		}
		else
		{
	        siteNodeVOList = new ArrayList();
	
	        OQLQuery oql = db.getOQLQuery( "SELECT s FROM org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl s WHERE s.parentSiteNode = $1 ORDER BY s.siteNodeId");
			oql.bind(siteNodeId);
			
	    	QueryResults results = oql.execute(Database.ReadOnly);
			
			while (results.hasMore()) 
	        {
	        	SiteNode siteNode = (SiteNode)results.next();
				
	        	if(isValidSiteNode(siteNode, db))
	        	    siteNodeVOList.add(siteNode.getValueObject());
			}

			CacheController.cacheObject("childSiteNodesCache", key, siteNodeVOList);
		}

    	logger.warn("getChildSiteNodes end:" + siteNodeId);

		return siteNodeVOList;	
	}

	
	/**
	 * This method returns a sorted list of qualifyers.
	 */
	
	private List getBindingQualifyers(Integer serviceBindingId, Database db) throws SystemException, Bug, Exception
	{
		List qualifyers = new ArrayList();
		
		OQLQuery oql = db.getOQLQuery( "SELECT q FROM org.infoglue.cms.entities.structure.impl.simple.QualifyerImpl q WHERE q.serviceBinding.serviceBindingId = $1 ORDER BY q.sortOrder");
		oql.bind(serviceBindingId);
		
    	QueryResults results = oql.execute(Database.ReadOnly);
		while(results.hasMore()) 
        {
        	Qualifyer qualifyer = (Qualifyer)results.next();
			qualifyers.add(qualifyer);
		}
		
		return qualifyers;
	}
	
	
	/**
	 * This method validates that right now is between publishdate and expiredate.
	 */
	
	private boolean isValidOnDates(Date publishDate, Date expireDate)
	{
		boolean isValid = true;
		Date now = new Date();
		
		if(publishDate.after(now) || expireDate.before(now))
			isValid = false;
		
		return isValid;
	}		
	
	/**
	 * Returns if a siteNode is between dates and has a siteNode version suitable for this delivery mode.
	 * @throws Exception
	 */
	
	public boolean isValidSiteNode(Database db, Integer siteNodeId) throws Exception
	{
		boolean isValidSiteNode = false;
		
		SiteNode siteNode = (SiteNode)getObjectWithId(SiteNodeImpl.class, siteNodeId, db); 
		isValidSiteNode = isValidSiteNode(siteNode, db);
    			
		return isValidSiteNode;					
	}

	/**
	 * Returns if a siteNode is between dates and has a SiteNode version suitable for this delivery mode.
	 * @throws Exception
	 */

	public boolean isValidSiteNode(SiteNode siteNode, Database db) throws Exception
	{
		boolean isValidContent = false;
		
		if(isValidOnDates(siteNode.getPublishDateTime(), siteNode.getExpireDateTime()))
		{
			Collection versions = siteNode.getSiteNodeVersions();
			Iterator versionsIterator = versions.iterator();
			while(versionsIterator.hasNext())
			{
				SiteNodeVersion siteNodeVersion = (SiteNodeVersion)versionsIterator.next();
				if(siteNodeVersion.getIsActive().booleanValue() && siteNodeVersion.getStateId().intValue() >= getOperatingMode().intValue())
					isValidContent = true;
			}
		}
		
		if(isValidContent && !siteNode.getExpireDateTime().before(new Date()))
		{
		    Date expireDateTimeCandidate = siteNode.getExpireDateTime();
		    if(CacheController.expireDateTime == null || expireDateTimeCandidate.before(CacheController.expireDateTime))
			{
			    CacheController.expireDateTime = expireDateTimeCandidate;
			}
		}
		else if(siteNode.getPublishDateTime().after(new Date())) //If it's a publish date to come we consider it
		{
		    Date publishDateTimeCandidate = siteNode.getPublishDateTime();
		    if(CacheController.publishDateTime == null || publishDateTimeCandidate.after(CacheController.publishDateTime))
			{
			    CacheController.publishDateTime = publishDateTimeCandidate;
			}
		}    	
		
		return isValidContent;					
	}
	
	/**
	 * This method just sorts the list of qualifyers on sortOrder.
	 */
	
	private List sortQualifyers(Collection qualifyers)
	{
		List sortedQualifyers = new ArrayList();

		try
		{		
			Iterator iterator = qualifyers.iterator();
			while(iterator.hasNext())
			{
				Qualifyer qualifyer = (Qualifyer)iterator.next();
				int index = 0;
				Iterator sortedListIterator = sortedQualifyers.iterator();
				while(sortedListIterator.hasNext())
				{
					Qualifyer sortedQualifyer = (Qualifyer)sortedListIterator.next();
					if(sortedQualifyer.getSortOrder().intValue() > qualifyer.getSortOrder().intValue())
			    	{
			    		break;
			    	}
			    	index++;
				}
				sortedQualifyers.add(index, qualifyer);
			    					
			}
		}
		catch(Exception e)
		{
			getLogger().warn("The sorting of qualifyers failed:" + e.getMessage(), e);
		}
			
		return sortedQualifyers;
	}

}