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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Iterator;

import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;

import org.infoglue.cms.entities.kernel.*;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.CmsLogger;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.validators.Constants;
import org.infoglue.cms.util.validators.ConstraintRule;
import org.infoglue.cms.util.validators.EmailValidator;
import org.infoglue.cms.util.validators.StringValidator;

/**
 * BaseController.java
 * Created on 2002-aug-28 
 * @author Stefan Sik, ss@frovi.com 
 * @author Mattias Bogeblad, mattias.bogeblad@sprawlsolutions.se
 * 
 * Baseclass for ControllerClasses.
 * Various methods to load, create and delete entities
 * 
 * TODO:
 * Now that all entities implements BaseEntity clear all reflection and simplify
 * arguments...
 * 
 * -matbog 2002-09-15: Added and modified new read-only methods for fetching a VO-object. 
 * 					   These method must be called instead of the old ones when just fetching a entity
 * 					   or all entities from a table.
 */

public abstract class BaseController
{

	private static Integer getEntityId(Object entity) throws Bug
	{
		Integer entityId = new Integer(-1);
		
		try 
		{
			entityId = ((IBaseEntity) entity).getId();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Bug("Unable to retrieve object id");
		}
		
		/*
		try {
			entityId = (Integer) entity.getClass().getDeclaredMethod("getId", new Class[0]).invoke(entity, new Object[0]);
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		} catch (NoSuchMethodException e) {
		}

		*/		
		return entityId;
	}

	/*************************************************** 
	 * Create, Delete & Update operations
	 ***************************************************/

	// Create entity
	// The validation belongs here
    protected static Object createEntity(Object entity) throws SystemException, Bug
    {
        Database db = CastorDatabaseService.getDatabase();
        beginTransaction(db);

        try
        {
            db.create(entity);
            commitTransaction(db);
            //CmsSystem.log(entity,"Created object", CmsSystem.DBG_NORMAL);
			//CmsSystem.transactionLogEntry(entity.getClass().getName(), CmsSystem.TRANS_CREATE, getEntityId(entity), entity.toString());
            
        }
        catch(Exception e)
        {
            CmsLogger.logSevere("An error occurred so we should not complete the transaction:" + e, e);
            //CmsSystem.log(entity,"Failed to create object", CmsSystem.DBG_LOW);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
        return entity;
    }     


	// Create entity inside an existing transaction
    protected static Object createEntity(Object entity, Database db) throws SystemException, Bug
    {
        try
        {
            db.create(entity);
            commitTransaction(db);
            //CmsSystem.log(entity,"Created object", CmsSystem.DBG_NORMAL);
			//CmsSystem.transactionLogEntry(entity.getClass().getName(), CmsSystem.TRANS_CREATE, getEntityId(entity), entity.toString());   
        }
        catch(Exception e)
        {
            CmsLogger.logSevere("An error occurred so we should not complete the transaction:" + e, e);
            //CmsSystem.log(entity,"Failed to create object", CmsSystem.DBG_LOW);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
        return entity;
    }     


	// Delete entity
    public static void deleteEntity(Class entClass, Integer id) throws Bug, SystemException
    {
        Database db = CastorDatabaseService.getDatabase();
        Object entity = null;

        beginTransaction(db);

        try
        {
            entity = getObjectWithId(entClass, id, db);
            
            // Delete the entity
            db.remove(entity);
            commitTransaction(db);
            //CmsSystem.log(entity,"Deleted object", CmsSystem.DBG_NORMAL);           
			//CmsSystem.transactionLogEntry(entClass.getName(), CmsSystem.TRANS_DELETE, id, entity.toString());            
        }
        catch(Exception e)
        {
            CmsLogger.logSevere("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
    }        


	// Delete entity
	public static void deleteEntity(Class entClass, String id) throws Bug, SystemException
	{
		Database db = CastorDatabaseService.getDatabase();
		Object entity = null;

		beginTransaction(db);

		try
		{
			entity = getObjectWithId(entClass, id, db);
            
			// Delete the entity
			db.remove(entity);
			commitTransaction(db);
			//CmsSystem.log(entity,"Deleted object", CmsSystem.DBG_NORMAL);           
			//CmsSystem.transactionLogEntry(entClass.getName(), CmsSystem.TRANS_DELETE, id, entity.toString());            
		}
		catch(Exception e)
		{
			CmsLogger.logSevere("An error occurred so we should not complete the transaction:" + e, e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
	}    

	// Delete entity
    public static void deleteEntity(Class entClass, Integer id, Database db) throws Bug, SystemException
    {
        Object entity = null;

        try
        {
            entity = getObjectWithId(entClass, id, db);
            // Delete the entity
            db.remove(entity);
        }
        catch(Exception e)
        {
            CmsLogger.logSevere("An error occurred so we should not complete the transaction:" + e, e);
            throw new SystemException(e.getMessage());
        }
    }        


    public static BaseEntityVO updateEntity(Class arg, BaseEntityVO vo) throws Bug, SystemException
    {
        Database db = CastorDatabaseService.getDatabase();

        IBaseEntity entity = null;

        beginTransaction(db);

        try
        {
            entity = (IBaseEntity) getObjectWithId(arg, vo.getId(), db);
            entity.setVO(vo);

            commitTransaction(db);
        }
        catch(Exception e)
        {
            CmsLogger.logSevere("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }

        return entity.getVO();
    }        

    
	public static BaseEntityVO updateEntity(Class arg, BaseEntityVO vo, Database db) throws Bug, SystemException
	{
		IBaseEntity entity = null;

		entity = (IBaseEntity) getObjectWithId(arg, vo.getId(), db);
		entity.setVO(vo);

		return entity.getVO();
	}        

	
	/* Update entity and a collection with other entities
	 * Experimental, use with caution
	 * 
	 */
    public static BaseEntityVO updateEntity(Class entClass, BaseEntityVO vo, String collectionMethod, Class manyClass, String[] manyIds) throws ConstraintException, SystemException
    {
        Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        IBaseEntity entity = null;

        beginTransaction(db);

        try
        {
            //add validation here if needed
            List manyList = new ArrayList();
            if(manyIds != null)
			{
		        for (int i=0; i < manyIds.length; i++)
	            {
	            	IBaseEntity manyEntity = (IBaseEntity) getObjectWithId(manyClass, new Integer(manyIds[i]), db);
	            	CmsLogger.logInfo("!!Using experimental code: BaseController::update. getting " + manyEntity.toString());
	            	manyList.add(manyEntity);
	            }
			}
			
		
            entity = (IBaseEntity) getObjectWithId(entClass, vo.getId(), db);
            entity.setVO(vo);
            
            // Now reflect to set the collection
            Object[] arg = {manyList};
            Class[] parm = {Collection.class};
            entity.getClass().getDeclaredMethod(collectionMethod, parm).invoke(entity, arg);
						
			// DONE
			
            //If any of the validations or setMethods reported an error, we throw them up now before create.
            ceb.throwIfNotEmpty();
            
            commitTransaction(db);
            //CmsSystem.transactionLogEntry(entity.getClass().getName(), CmsSystem.TRANS_UPDATE, vo.getId(), entity.toString());

        }
        catch(ConstraintException ce)
        {
            CmsLogger.logWarning("An error occurred so we should not complete the transaction:" + ce, ce);
            rollbackTransaction(db);
            throw ce;
        }
        catch(Exception e)
        {
            CmsLogger.logSevere("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }

        return entity.getVO();
    }        
	

	/*
	protected static Object getObjectWithId(Class arg, Integer id) throws SystemException, Bug
	{
		Database db = CastorDatabaseService.getDatabase();		
		Object ret = null;
		try
		{
			beginTransaction(db);
			ret = getObjectWithId(arg, id, db);
			commitTransaction(db);
		}
		catch (Exception e)
		{
			rollbackTransaction(db);
            throw new SystemException("An error occurred when we tried to fetch the object " + arg.getName() + ". Reason:" + e.getMessage(), e);    
		}
		return ret;
	}
	*/

	/**
	 * This method fetches one object / entity within a transaction.
	 **/
	
    protected static Object getObjectWithId(Class arg, Integer id, Database db) throws SystemException, Bug
    {
        Object object = null;
        try
        {
            object = db.load(arg, id);
        	//CmsSystem.log(object, "BaseController:getObjectWithId", CmsSystem.DBG_HIGH);
        }
        catch(Exception e)
        {
            throw new SystemException("An error occurred when we tried to fetch the object " + arg.getName() + ". Reason:" + e.getMessage(), e);    
        }
    
        if(object == null)
        {
            throw new Bug("The object with id [" + id + "] was not found. This should never happen.");
        }
    	return object;
    }


	/**
	 * This method fetches one object / entity within a transaction.
	 **/
	
    protected static Object getObjectWithIdAsReadOnly(Class arg, Integer id, Database db) throws SystemException, Bug
    {
        Object object = null;
        try
        {
            object = db.load(arg, id, Database.ReadOnly);    			
        	//CmsSystem.log(object, "BaseController:getObjectWithId", CmsSystem.DBG_HIGH);
        }
        catch(Exception e)
        {
            throw new SystemException("An error occurred when we tried to fetch the object " + arg.getName() + ". Reason:" + e.getMessage(), e);    
        }
    
        if(object == null)
        {
            throw new Bug("The object with id [" + id + "] was not found. This should never happen.");
        }
    	return object;
    }

	/**
	 * This method fetches one object / entity within a transaction.
	 **/
	
	protected static Object getObjectWithId(Class arg, String id, Database db) throws SystemException, Bug
	{
		Object object = null;
		try
		{
			object = db.load(arg, id);
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to fetch the object " + arg.getName() + ". Reason:" + e.getMessage(), e);    
		}
    
		if(object == null)
		{
			throw new Bug("The object with id [" + id + "] was not found. This should never happen.");
		}
		return object;
	}

	    
  	/**
	 * This method converts a List of entities to a list of value-objects.
	 */
	
	public static List toVOList(Collection baseEntities) throws SystemException, Bug
    {
		List resultVOList = new ArrayList();
		
        if(baseEntities != null)
    	{
			Object o = null;
		    try
			{
				Iterator iterator = baseEntities.iterator();
				while (iterator.hasNext()) 
		        {
					o = (Object)iterator.next();
					// Om metoden getValueObject saknas, kastas ett undantag.            	
	                resultVOList.add(o.getClass().getDeclaredMethod("getValueObject", new Class[0]).invoke(o, new Object[0]));
		        }
	    	}
		    catch(NoSuchMethodException e)
	        {
	            throw new Bug("The object in list was of the wrong type: " + o.getClass().getName() + ". This should never happen.", e);
	        }
	        catch(Exception e)
	        {
	            throw new SystemException("An error occurred when we tried to convert the collection to a valueList. Reason:" + e.getMessage(), e);    
	        }    
		}
		    
        return resultVOList;
    }

  	/**
	 * This method converts a List of entities to a list of value-objects.
	 */
	
	public static List toModifiableVOList(Collection baseEntities) throws SystemException, Bug
    {
		List resultVOList = new ArrayList();
		
        if(baseEntities != null)
    	{
			Object o = null;
		    try
			{
				Iterator iterator = baseEntities.iterator();
				while (iterator.hasNext()) 
		        {
					o = (Object)iterator.next();
					// Om metoden getValueObject saknas, kastas ett undantag.            	
	                resultVOList.add(o.getClass().getDeclaredMethod("getValueObject", new Class[0]).invoke(o, new Object[0]));
		        }
	    	}
		    catch(NoSuchMethodException e)
	        {
	            throw new Bug("The object in list was of the wrong type: " + o.getClass().getName() + ". This should never happen.", e);
	        }
	        catch(Exception e)
	        {
	            throw new SystemException("An error occurred when we tried to convert the collection to a valueList. Reason:" + e.getMessage(), e);    
	        }    
		}
		    
        return resultVOList;
    }

	/*************************************************** 
	 * Read only operations
	 ***************************************************/

	/**
	 * This method is used to fetch a ValueObject from the database.
	 */

	public static Object getVOWithId(Class arg, Integer id) throws SystemException, Bug
	{
		Database db = CastorDatabaseService.getDatabase();		
		Object ret = null;
		try
		{
			beginTransaction(db);
			ret = getVOWithId(arg, id, db);
			commitTransaction(db);
		}
		catch (Exception e)
		{
			rollbackTransaction(db);
            throw new SystemException("An error occurred when we tried to fetch the object " + arg.getName() + ". Reason:" + e.getMessage(), e);    
		}
		return ret;
	}

	/**
	 * This method fetches one object in read only mode and returns it's value object.
	 */
	
    private static BaseEntityVO getVOWithId(Class arg, Integer id, Database db) throws SystemException, Bug
    {
        IBaseEntity vo = null;
        try
        {
    		vo = (IBaseEntity)db.load(arg, id, Database.ReadOnly);
        }
        catch(Exception e)
        {
            throw new SystemException("An error occurred when we tried to fetch the object " + arg.getName() + ". Reason:" + e.getMessage(), e);    
        }
    
        if(vo == null)
        {
            throw new Bug("The object with id [" + id + "] was not found. This should never happen.");
        }
        
    	return vo.getVO();
    }
    
    
	/**
	 * This method is used to fetch a ValueObject from the database.
	 */

	public static Object getVOWithId(Class arg, String id) throws SystemException, Bug
	{
		Database db = CastorDatabaseService.getDatabase();		
		Object ret = null;
		try
		{
			beginTransaction(db);
			ret = getVOWithId(arg, id, db);
			commitTransaction(db);
		}
		catch (Exception e)
		{
			rollbackTransaction(db);
			throw new SystemException("An error occurred when we tried to fetch the object " + arg.getName() + ". Reason:" + e.getMessage(), e);    
		}
		return ret;
	}
	
	/**
	 * This method fetches one object in read only mode and returns it's value object.
	 */
	
	private static BaseEntityVO getVOWithId(Class arg, String id, Database db) throws SystemException, Bug
	{
		IBaseEntity vo = null;
		try
		{
			vo = (IBaseEntity)db.load(arg, id, Database.ReadOnly);
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to fetch the object " + arg.getName() + ". Reason:" + e.getMessage(), e);    
		}
    
		if(vo == null)
		{
			throw new Bug("The object with id [" + id + "] was not found. This should never happen.");
		}
        
		return vo.getVO();
	}


	/**
	 * This method fetches all object in read only mode and returns a list of value objects.
	 */
    /*
    public static List getAllVOObjects(Class arg) throws SystemException, Bug
    {
		Database db = CastorDatabaseService.getDatabase();
		List ret = null;
        try
        {
			beginTransaction(db);
			ret = getAllVOObjects(arg, db);
			commitTransaction(db);
        }
        catch(Exception e)
        {
			rollbackTransaction(db);
            throw new SystemException("An error occurred when we tried to fetch " + arg.getName() + " Reason:" + e.getMessage(), e);    
        }    
        return ret;
    }
    */
    
	/**
	 * This method fetches all object in read only mode and returns a list of value objects.
	 */
    
	public static List getAllVOObjects(Class arg, String orderByAttribute, String direction) throws SystemException, Bug
	{
		Database db = CastorDatabaseService.getDatabase();
		List ret = null;
		try
		{
			beginTransaction(db);
			ret = getAllVOObjects(arg, orderByAttribute, direction, db);
			commitTransaction(db);
		}
		catch(Exception e)
		{
			rollbackTransaction(db);
			throw new SystemException("An error occurred when we tried to fetch " + arg.getName() + " Reason:" + e.getMessage(), e);    
		}    
		return ret;
	}


    
	/**
	 * This method fetches all object in read only mode and returns a list of value objects.
	 */

	public static List getAllVOObjects(Class arg, String orderByField, String direction, Database db) throws SystemException, Bug
	{
		ArrayList resultList = new ArrayList();
		OQLQuery	oql;
		try
		{
        	
			CmsLogger.logInfo("BaseHelper::GetAllObjects for " + arg.getName());
			oql = db.getOQLQuery( "SELECT u FROM " + arg.getName() + " u ORDER BY u." + orderByField + " " + direction);
			QueryResults results = oql.execute(Database.ReadOnly);
			
			while (results.hasMore()) 
			{
				Object o = results.next();

				// Om metoden getValueObject saknas, kastas ett undantag.            	
				resultList.add(o.getClass().getDeclaredMethod("getValueObject", new Class[0]).invoke(o, new Object[0]));
			}
		}
		catch(NoSuchMethodException e)
		{
			throw new Bug("The object [" + arg.getName() + "] is of the wrong type. This should never happen.", e);
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to fetch " + arg.getName() + " Reason:" + e.getMessage(), e);    
		}    

		return resultList;
	}
    
	/**
	 * This method fetches all object in read only mode and returns a list of value objects sorted on primary Key.
	 */
	 
	public List getAllVOObjects(Class arg, String primaryKey) throws SystemException, Bug
	{
		Database db = CastorDatabaseService.getDatabase();
		List ret = null;
		try
		{
			beginTransaction(db);
			ret = getAllVOObjects(arg, primaryKey, db);
			commitTransaction(db);
		}
		catch(Exception e)
		{
			rollbackTransaction(db);
			throw new SystemException("An error occurred when we tried to fetch " + arg.getName() + " Reason:" + e.getMessage(), e);    
		}    
		return ret;
	}


	/**
	 * This method fetches all object in read only mode and returns a list of value objects.
	 */

	public List getAllVOObjects(Class arg, String primaryKey, Database db) throws SystemException, Bug
	{
		ArrayList resultList = new ArrayList();
		OQLQuery	oql;
		try
		{
			oql = db.getOQLQuery( "SELECT u FROM " + arg.getName() + " u ORDER BY u." + primaryKey);
			QueryResults results = oql.execute(Database.ReadOnly);
			
			while (results.hasMore()) 
			{
				IBaseEntity baseEntity = (IBaseEntity)results.next();
				resultList.add(baseEntity.getVO());
			}
		}
		catch(ClassCastException e)
		{
			throw new Bug("The object [" + arg.getName() + "] is of the wrong type. This should never happen.", e);
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to fetch " + arg.getName() + " Reason:" + e.getMessage(), e);    
		}    

		return resultList;
	}
	
	/***************************************************
	 * Validation and integrity check of entities - cre 2002-09-18 / SS
	 * *************************************************/

    public static ConstraintExceptionBuffer validateEntity(ValidatableEntityVO vo)
    {
    	// This method loops through the rulelist and creates
    	// validators according to the settings in each rule.
    	// The old validators are used to do the actual validation
    	// but I have changed them to use less constructor
    	// parameter passing in favour for setters.
    	    	
    	//CmsSystem.log("ValidationController::validate()", CmsSystem.DBG_HIGH);
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
		
		// Prepare the object for validation
		vo.PrepareValidation();
		
		// Loop through rules and create validators
    	Iterator iterator = vo.getConstraintRules().iterator();
    	while (iterator.hasNext()) 
    	{
    		ConstraintRule cr = (ConstraintRule) iterator.next();
    		Integer intId = vo.getId();
    		CmsLogger.logInfo("Validating object id: " + intId);

			// an ugly switch for now.    		
    		switch (cr.getConstraintType())
    		{
    			case Constants.EMAIL:
    			{
					if (cr.getValue() != null)
					{
						// Create validator
	    				EmailValidator v = new EmailValidator(cr.getFieldName());
	    				
	    				// Set properties
	    				v.setObjectClass(vo.getConstraintRuleList().getEntityClass());
	    				v.setRange(cr.getValidRange());
	    				v.setIsRequired(cr.required);
	    				v.setMustBeUnique(cr.unique);
	    				v.setExcludeId(intId);

						// Do the limbo
	    				v.validate((String) cr.getValue(), ceb);
	    				
	    				// <todo>
	    				// Note: the actual value validated should be extracted
	    				// from the vo using the fieldname with reflection.
	    				// </todo>
	    				
					}		 	    	 
    				break;
    			}
				case Constants.STRING:
    			{
					if (cr.getValue() != null)
					{    				
	    				StringValidator v = new StringValidator(cr.getFieldName());
	    				v.setObjectClass(vo.getConstraintRuleList().getEntityClass());
	    				v.setRange(cr.getValidRange());
	    				v.setIsRequired(cr.required);
	    				v.setMustBeUnique(cr.unique);
	    				v.setExcludeId(intId);

	    				v.validate((String) cr.getValue(), ceb);
					}		 	    	 
    				break;
    			}
    			case Constants.FLOAT:
    			{
    				break;
    			}
    			case Constants.INTEGER:
    			{
    				break;
    			}
    			case Constants.PROPERNOUN:
    			{
    				break;
    			}
    			
    		} // switch
    		    		
    	} // while
				
		return ceb;
    }



	/*************************************************** 
	 * Transaction specifik operations
	 ***************************************************/

    /**
     * Begins a transaction on the named database
     */
         
    protected static void beginTransaction(Database db) throws SystemException
    {
        try
        {
            db.begin();
        }
        catch(Exception e)
        {
			e.printStackTrace();
        	throw new SystemException("An error occurred when we tried to begin an transaction. Reason:" + e.getMessage(), e);    
        }
    }
       
    /**
     * Ends a transaction on the named database
     */
     
    protected static void commitTransaction(Database db) throws SystemException
    {
        try
        {
            db.commit();
			db.close();
        }
        catch(Exception e)
        {
			e.printStackTrace();
            throw new SystemException("An error occurred when we tried to commit an transaction. Reason:" + e.getMessage(), e);    
        }
    }
 
 
    /**
     * Rollbacks a transaction on the named database
     */
     
    protected static void rollbackTransaction(Database db) throws SystemException
    {
        try
        {
        	if (db.isActive())
        	{
	            db.rollback();
				db.close();
        	}
        }
        catch(Exception e)
        {
			//e.printStackTrace();
            CmsLogger.logInfo("An error occurred when we tried to rollback an transaction. Reason:" + e.getMessage());
            //throw new SystemException("An error occurred when we tried to rollback an transaction. Reason:" + e.getMessage(), e);    
        }
    }

	public abstract BaseEntityVO getNewVO();
}