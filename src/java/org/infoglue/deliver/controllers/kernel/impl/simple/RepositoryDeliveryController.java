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

import org.infoglue.cms.entities.management.*;
import org.infoglue.cms.util.*;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.deliver.util.CacheController;

import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;

import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class RepositoryDeliveryController extends BaseDeliveryController
{

	/**
	 * Private constructor to enforce factory-use
	 */

	private RepositoryDeliveryController()
	{
	}

	/**
	 * Factory method
	 */

	public static RepositoryDeliveryController getRepositoryDeliveryController()
	{
		return new RepositoryDeliveryController();
	}


	/**
	 * This method returns the master repository.
	 */

	public RepositoryVO getMasterRepository() throws SystemException, Exception
	{
		RepositoryVO repositoryVO = (RepositoryVO)CacheController.getCachedObject("masterRepository", "masterRepository");
		if(repositoryVO != null)
			return repositoryVO;

		Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        beginTransaction(db);

        try
        {
         	OQLQuery oql = db.getOQLQuery( "SELECT r FROM org.infoglue.cms.entities.management.impl.simple.RepositoryImpl r ORDER BY r.repositoryId");

        	QueryResults results = oql.execute(Database.ReadOnly);

			if (results.hasMore())
            {
            	Repository repository = (Repository)results.next();
            	repositoryVO = repository.getValueObject();
            }

            //If any of the validations or setMethods reported an error, we throw them up now before create.
            ceb.throwIfNotEmpty();

			commitTransaction(db);
        }
        catch(Exception e)
        {
            CmsLogger.logSevere("An error occurred so we should not complete the transaction:" + e, e);
			rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }

		if(repositoryVO != null)
			CacheController.cacheObject("masterRepository", "masterRepository", repositoryVO);

        return repositoryVO;
	}


	public RepositoryVO getRepositoryFromServerName(String serverName, String portNumber) throws SystemException, Exception
    {
        Database db = CastorDatabaseService.getDatabase();
        beginTransaction(db);
        try
		{
            OQLQuery oql = db.getOQLQuery( "SELECT r FROM org.infoglue.cms.entities.management.impl.simple.RepositoryImpl r WHERE is_defined(r.dnsName)");
            QueryResults results = oql.execute(Database.ReadOnly);
            while (results.hasMore())
            {
                Repository repository = (Repository) results.next();
                CmsLogger.logInfo("repository:" + repository.getDnsName());
                String[] dnsNames = splitStrings(repository.getDnsName());
                CmsLogger.logInfo("dnsNames:" + dnsNames);
                for (int i=0;i<dnsNames.length;i++)
                {
                	CmsLogger.logInfo("dnsNames[i]:" + dnsNames[i]);
                    if((dnsNames[i].indexOf(":") == -1 && dnsNames[i].indexOf(serverName) != -1) || dnsNames[i].indexOf(serverName + ":" + portNumber) != -1)
                    {
                    	commitTransaction(db);
                        return repository.getValueObject();
                    }
                }
            }

            commitTransaction(db);
        }
        catch (Exception e)
		{
            CmsLogger.logSevere("An error occured - Unable to map server-name to repository",e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }

        return null;
    }

    private String[] splitStrings(String str)
    {
        List list = new ArrayList();
        StringTokenizer st = new StringTokenizer(str, ",");
        while (st.hasMoreTokens())
        {
            String token = st.nextToken().trim();
            list.add(token);
        }

        return (String[]) list.toArray(new String[0]);
    }

	/**
	 * This method returns all the repositories.
	 */

	public List getRepositoryVOList() throws SystemException, Exception
	{
		List repositoryVOList = new ArrayList();

		Database db = CastorDatabaseService.getDatabase();
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

		beginTransaction(db);

		try
		{
			OQLQuery oql = db.getOQLQuery( "SELECT r FROM org.infoglue.cms.entities.management.impl.simple.RepositoryImpl r ORDER BY r.repositoryId");

			QueryResults results = oql.execute(Database.ReadOnly);

			if (results.hasMore())
			{
				Repository repository = (Repository)results.next();
				RepositoryVO repositoryVO = repository.getValueObject();
				repositoryVOList.add(repositoryVO);
			}

			//If any of the validations or setMethods reported an error, we throw them up now before create.
			ceb.throwIfNotEmpty();

			commitTransaction(db);
		}
		catch(Exception e)
		{
			CmsLogger.logSevere("An error occurred so we should not complete the transaction:" + e, e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}

		return repositoryVOList;
	}



}