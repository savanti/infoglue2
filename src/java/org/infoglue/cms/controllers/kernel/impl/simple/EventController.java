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

package org.infoglue.cms.controllers.kernel.impl.simple;

import org.exolab.castor.jdo.Database;

import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.kernel.*;
import org.infoglue.cms.entities.structure.SiteNodeVersion;
import org.infoglue.cms.entities.workflow.*;
import org.infoglue.cms.entities.workflow.EventVO;
import org.infoglue.cms.entities.workflow.impl.simple.EventImpl;
import org.infoglue.cms.entities.management.*;
import org.infoglue.cms.entities.management.impl.simple.*;

import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.CmsLogger;

import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Mattias Bogeblad
 *
 * This class implements all operations we can do on the cmEvent-entity.
 */

public class EventController extends BaseController 
{

    /**
     * Gets the eventVO in a readonly transaction.
     */
    
    public static EventVO getEventVOWithId(Integer eventId) throws SystemException, Bug
    {
		return (EventVO) getVOWithId(EventImpl.class, eventId);
    }
   	
    /**
     * Gets the event in the given transaction.
     */
	
    public static Event getEventWithId(Integer eventId, Database db) throws SystemException, Bug
    {
		return (Event) getObjectWithId(EventImpl.class, eventId, db);
    }

    /**
     * Gets all events in a read only transaction.
     */

    public List getEventVOList() throws SystemException, Bug
    {
        return getAllVOObjects(EventImpl.class, "eventId");
    }

	/**
	 * Creates a new Event with the values in the eventVO sent in. 
	 */
	
    public static EventVO create(EventVO eventVO, Integer repositoryId, InfoGluePrincipal infoGluePrincipal, Database db) throws ConstraintException, SystemException
    {
        //Fetch related entities here if they should be referenced        
     	Repository repository = RepositoryController.getController().getRepositoryWithId(repositoryId, db);
     	
        Event event = new EventImpl();
        event.setValueObject(eventVO);				
        event.setRepository((RepositoryImpl)repository);
        event.setCreator(infoGluePrincipal.getName());
        
        try
        {
            db.create(event);
        }
        catch(Exception e)
        {
            CmsLogger.logSevere("An error occurred so we should not complete the transaction:" + e, e);
            throw new SystemException(e.getMessage());
        }
        
        return event.getValueObject();
    }  


	/**
	 * Creates a new Event with the values in the eventVO sent in in a new transaction. 
	 */
	
    public static EventVO create(EventVO eventVO, Integer repositoryId, InfoGluePrincipal infoGluePrincipal) throws ConstraintException, SystemException
    {
        Event event = null;
		
        Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
		
		beginTransaction(db);
		try
        {
	        //Fetch related entities here if they should be referenced        
	     	Repository repository = RepositoryController.getController().getRepositoryWithId(repositoryId, db);
	     	
	        event = new EventImpl();
	        event.setValueObject(eventVO);				
	        event.setRepository((RepositoryImpl)repository);
            event.setCreator(infoGluePrincipal.getName());
            db.create(event);
    
            commitTransaction(db);
        }
        catch(Exception e)
        {
        	CmsLogger.logSevere("An error occurred so we should not completes the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
        
        return event.getValueObject();
    }  
	

    
    /**
     * This method removes an event from the database.
     */
                       
    public static void delete(EventVO eventVO) throws ConstraintException, SystemException
    {
	    deleteEntity(EventImpl.class, eventVO.getEventId());
    }        


    /**
     * This method removes an event from the database.
     */
                       
    public static void delete(Event event, Database db) throws ConstraintException, SystemException, Exception
    {
	    db.remove(event);
    }  

	/**
	 * This method updates an event.
	 */
	
    public static EventVO update(EventVO eventVO) throws ConstraintException, SystemException
    {
    	return (EventVO) updateEntity(EventImpl.class, eventVO);
    }        





	/**
	 * Returns a list of events currently available for the certain entity.
	 */
	
	public static List getEventVOListForEntity(String entityClass, Integer entityId) throws SystemException, Bug
	{
		List events = new ArrayList();
		
		Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        beginTransaction(db);
		try
        {
            OQLQuery oql = db.getOQLQuery( "SELECT e FROM org.infoglue.cms.entities.workflow.impl.simple.EventImpl e WHERE e.entityClass = $1 AND e.entityId = $2");
        	oql.bind(entityClass);
        	oql.bind(entityId);
        	
        	QueryResults results = oql.execute(Database.ReadOnly);
			
			while (results.hasMore()) 
            {
            	Event event = (Event)results.next();
            	events.add(event.getValueObject());
            }
            
            commitTransaction(db);
        }
        catch(Exception e)
        {
        	CmsLogger.logSevere("An error occurred so we should not completes the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
		
		return events;	
	}
	
	/**
	 * Returns a list of events with either publish or unpublish-state currently available for the repository stated.
	 */
	
	public static List getPublicationEventVOListForRepository(Integer repositoryId) throws SystemException, Bug
	{
		List events = new ArrayList();
		
		Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        beginTransaction(db);
		try
        {
            OQLQuery oql = db.getOQLQuery( "SELECT e FROM org.infoglue.cms.entities.workflow.impl.simple.EventImpl e WHERE (e.typeId = $1 OR e.typeId = $2) AND e.repository.repositoryId = $3 ORDER BY e.eventId desc");
        	oql.bind(EventVO.PUBLISH);
        	oql.bind(EventVO.UNPUBLISH_LATEST);
        	oql.bind(repositoryId);
        	
        	QueryResults results = oql.execute(Database.ReadOnly);
			
			while (results.hasMore()) 
            {
            	Event event = (Event)results.next();

            	boolean isValid = true;
            	try
            	{
	            	if(event.getEntityClass().equalsIgnoreCase(ContentVersion.class.getName()))
	            	{
	            		ContentVersion contentVersion = ContentVersionController.getContentVersionController().getContentVersionWithId(event.getEntityId(), db);
	            		if(contentVersion == null)
							isValid = false;
	            	}
					else if(event.getEntityClass().equalsIgnoreCase(SiteNodeVersion.class.getName()))
					{
						SiteNodeVersion siteNodeVersion = SiteNodeVersionController.getController().getSiteNodeVersionWithId(event.getEntityId(), db);
						if(siteNodeVersion == null)
							isValid = false;
					}
				}
				catch(Exception e)
				{
					isValid = false;
					//delete(event, db);
				}
					
				if(isValid)
	            	events.add(event.getValueObject());
            }
            
            commitTransaction(db);
        }
        catch(Exception e)
        {
        	CmsLogger.logSevere("An error occurred so we should not completes the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
		
		return events;	
	}

	/**
	 * This is a method that gives the user back an newly initialized ValueObject for this entity that the controller
	 * is handling.
	 */

	public BaseEntityVO getNewVO()
	{
		return new EventVO();
	}

}
