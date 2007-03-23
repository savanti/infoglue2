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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class SearchController extends BaseController 
{
    private final static Logger logger = Logger.getLogger(SearchController.class.getName());

	public static String getAttributeValue(String xml,String key)
	{
		String value = "";
			try
	        {
		        InputSource inputSource = new InputSource(new StringReader(xml));
				DOMParser parser = new DOMParser();
				parser.parse(inputSource);
				Document document = parser.getDocument();
				NodeList nl = document.getDocumentElement().getChildNodes();
				Node n = nl.item(0);
				nl = n.getChildNodes();

				for(int i=0; i<nl.getLength(); i++)
				{
					n = nl.item(i);
					if(n.getNodeName().equalsIgnoreCase(key))
					{
						value = n.getFirstChild().getNodeValue();
						break;
					}
				}		        	
	        }
	        catch(Exception e)
	        {
	        	e.printStackTrace();
	        }
		if(value.equalsIgnoreCase(""))value="This Content is Unititled";
		return value;
	}

	public static String setScoreImg(double score)
	{
		if( 2.0 <  score){
			return "5star.gif";					
		}
		else if( 1.0 <  score){
			return "4star.gif";					
		}
		else if( 0.6 <  score){
			return "3star.gif";					
		}
		else if( 0.4 <  score){
			return "2star.gif";					
		}
		else{
			return "1star.gif";	
		}
	}

   	public static List getContentVersions(Integer repositoryId, String searchString, int maxRows, String name, Integer languageId, Integer contentTypeDefinitionId, Integer caseSensitive, Integer stateId) throws SystemException, Bug
   	{
		List matchingContents = getContentVersions(new Integer[]{repositoryId}, searchString, maxRows, name, languageId, contentTypeDefinitionId, caseSensitive, stateId);
			
		return matchingContents;
		
   	}

	
   	public static List getContentVersions(Integer[] repositoryId, String searchString, int maxRows, String name, Integer languageId, Integer contentTypeDefinitionId, Integer caseSensitive, Integer stateId) throws SystemException, Bug
   	{
		List matchingContents = new ArrayList();

		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
		Database db = CastorDatabaseService.getDatabase();
		try
		{
			beginTransaction(db);

			String repositoryArgument = " AND (";
			
			int index = 3;
			List repArguments = new ArrayList();
			
			for(int i=0; i<repositoryId.length; i++)
			{
				if(i > 0)
					repositoryArgument += " OR ";
				
				repositoryArgument += "cv.owningContent.repository.repositoryId = $" + index;
			    repArguments.add(repositoryId[i]);
				index++;
			}
			repositoryArgument += ")";
				
			//int index = 4;
			String extraArguments = "";
			String inverse = "";
			List arguments = new ArrayList();
						
			if(name != null && !name.equalsIgnoreCase(""))
			{
			    extraArguments += " AND cv.versionModifier = $" + index;
			    arguments.add(name);
				index++;
			}
			if(languageId != null)
			{
			    extraArguments += " AND cv.language = $" + index;
			    arguments.add(languageId);
				index++;
			}
			if(contentTypeDefinitionId != null)
			{
			    extraArguments += " AND cv.owningContent.contentTypeDefinition = $" + index;
			    arguments.add(contentTypeDefinitionId);
				index++;
			}
			if(stateId != null)
			{
			    extraArguments += " AND cv.stateId = $" + index;
			    arguments.add(stateId);
				index++;
			}
			    
			String sql = "SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl cv WHERE cv.isActive = $1 AND cv.versionValue LIKE $2 " + repositoryArgument + extraArguments + " ORDER BY cv.owningContent asc, cv.language, cv.contentVersionId desc";
			logger.info("sql:" + sql);
			//System.out.println("sql:" + sql);
			OQLQuery oql = db.getOQLQuery(sql);
			oql.bind(new Boolean(true));
			oql.bind("%" + searchString + "%");
			//oql.bind(repositoryId);
			Iterator repIterator = repArguments.iterator();
			while(repIterator.hasNext())
			{
				Integer repositoryIdAsInteger = (Integer)repIterator.next();
				oql.bind(repositoryIdAsInteger);
			    //System.out.println("repositoryIdAsInteger:" + repositoryIdAsInteger);
			}
	        
			Iterator iterator = arguments.iterator();
			while(iterator.hasNext())
			{
			    oql.bind(iterator.next());
			}
			
			QueryResults results = oql.execute(Database.ReadOnly);
			
			Integer previousContentId  = new Integer(-1);
			Integer previousLanguageId = new Integer(-1);  	
			int currentCount = 0;
			while(results.hasMore() && currentCount < maxRows) 
			{
				ContentVersion contentVersion = (ContentVersion)results.next();
				logger.info("Found a version matching " + searchString + ":" + contentVersion.getId() + "=" + contentVersion.getOwningContent().getName());
				if(contentVersion.getOwningContent().getId().intValue() != previousContentId.intValue() || contentVersion.getLanguage().getId().intValue() != previousLanguageId.intValue())
				{
				    ContentVersion latestContentVersion = ContentVersionController.getContentVersionController().getLatestActiveContentVersion(contentVersion.getOwningContent().getId(), contentVersion.getLanguage().getId(), db);
					if(latestContentVersion.getId().intValue() == contentVersion.getId().intValue() && (caseSensitive == null || contentVersion.getVersionValue().indexOf(searchString) > -1))
					{
					    matchingContents.add(contentVersion.getValueObject());
					    previousContentId = contentVersion.getOwningContent().getId();
					    previousLanguageId = contentVersion.getLanguage().getId();
					    currentCount++;
					}
				}
			}

			results.close();
			oql.close();

			commitTransaction(db);
		}
		catch ( Exception e )
		{
			rollbackTransaction(db);
			throw new SystemException("An error occurred when we tried to search. Reason:" + e.getMessage(), e);			
		}
		
		return matchingContents;
		
   	}
   	
   	/**
   	 * Gets all content versions last changed by a certain user.
   	 * 
   	 * @param userName
   	 * @return
   	 * @throws SystemException
   	 * @throws Bug
   	 */
   	
   	public static Set getContentVersions(Integer contentTypeDefinitionId, String userName, Date publishStartDate, Date publishEndDate, Date unpublishStartDate, Date unpublishEndDate) throws SystemException, Bug
   	{
		Set matchingContentVersions = new HashSet();

		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
		Database db = CastorDatabaseService.getDatabase();
		try
		{
			beginTransaction(db);
			
			int index = 2;
			/*
			String repositoryArgument = ""; //" AND (";
			List repArguments = new ArrayList();
			
			for(int i=0; i<repositoryId.length; i++)
			{
				if(i > 0)
					repositoryArgument += " OR ";
				
				repositoryArgument += "cv.owningContent.repository.repositoryId = $" + index;
			    repArguments.add(repositoryId[i]);
				index++;
			}
			//repositoryArgument += ")";
			*/
			
			String extraArguments = "";
			String inverse = "";
			List arguments = new ArrayList();
						
			if(userName != null && !userName.equalsIgnoreCase(""))
			{
			    extraArguments += " cv.versionModifier = $" + index;
			    //extraArguments += " AND cv.versionModifier = $" + index;
			    arguments.add(userName);
				index++;
			}
			/*
			if(languageId != null)
			{
			    extraArguments += " AND cv.language = $" + index;
			    arguments.add(languageId);
				index++;
			}
			*/
			
			if(contentTypeDefinitionId != null)
			{
			    extraArguments += " AND cv.owningContent.contentTypeDefinition = $" + index;
			    arguments.add(contentTypeDefinitionId);
				index++;
			}
			
			/*
			if(stateId != null)
			{
			    extraArguments += " AND cv.stateId = $" + index;
			    arguments.add(stateId);
				index++;
			}
			*/
			if(publishStartDate != null)
			{
			    extraArguments += " AND cv.owningContent.publishDateTime > $" + index;
			    arguments.add(publishStartDate);
				index++;
			}
			if(publishEndDate != null)
			{
			    extraArguments += " AND cv.owningContent.publishDateTime < $" + index;
			    arguments.add(publishEndDate);
				index++;
			}
			if(unpublishStartDate != null)
			{
			    extraArguments += " AND cv.owningContent.expireDateTime > $" + index;
			    arguments.add(unpublishStartDate);
				index++;
			}
			if(unpublishEndDate != null)
			{
			    extraArguments += " AND cv.owningContent.expireDateTime < $" + index;
			    arguments.add(unpublishEndDate);
				index++;
			}
			    
			String sql = "SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.FullContentVersionImpl cv WHERE cv.isActive = $1 AND " /*+ repositoryArgument*/ + extraArguments + " ORDER BY cv.contentId asc, cv.language, cv.contentVersionId desc";
			if(logger.isInfoEnabled())
				logger.info("sql:" + sql);
			System.out.println("sql:" + sql);
			OQLQuery oql = db.getOQLQuery(sql);
			oql.bind(new Boolean(true));
			//oql.bind(repositoryId);
			/*
			Iterator repIterator = repArguments.iterator();
			while(repIterator.hasNext())
			{
				Integer repositoryIdAsInteger = (Integer)repIterator.next();
				oql.bind(repositoryIdAsInteger);
			    //System.out.println("repositoryIdAsInteger:" + repositoryIdAsInteger);
			}
	        */
			
			Iterator iterator = arguments.iterator();
			while(iterator.hasNext())
			{
			    oql.bind(iterator.next());
			}
			
			QueryResults results = oql.execute(Database.ReadOnly);
			
			while(results.hasMore()) 
			{
				ContentVersion contentVersion = (ContentVersion)results.next();
				if(logger.isInfoEnabled())
					logger.info("Found a version matching:" + contentVersion.getId() + ":" + contentVersion.getOwningContent().getExpireDateTime());
				
				ContentVersion latestContentVersion = ContentVersionController.getContentVersionController().getLatestActiveContentVersion(contentVersion.getValueObject().getContentId(), contentVersion.getValueObject().getLanguageId(), db);
				if(latestContentVersion.getId().intValue() == contentVersion.getId().intValue())
				{
					matchingContentVersions.add(contentVersion.getValueObject());
				}
			}

			results.close();
			oql.close();

			commitTransaction(db);
		}
		catch ( Exception e )
		{
			rollbackTransaction(db);
			throw new SystemException("An error occurred when we tried to search. Reason:" + e.getMessage(), e);			
		}
		
		return matchingContentVersions;
		
   	}
   	
   	public static int replaceString(String searchString, String replaceString, String[] contentVersionIds, InfoGluePrincipal infoGluePrincipal)throws SystemException, Bug
   	{
		int replacements = 0;
		
   	    ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
		Database db = CastorDatabaseService.getDatabase();
		try
		{
			beginTransaction(db);

			for(int i=0; i<contentVersionIds.length; i++)
			{
			    String contentVersionId = contentVersionIds[i];
			    logger.info("contentVersionId:" + contentVersionId);
			    ContentVersion contentVersion = ContentVersionController.getContentVersionController().getContentVersionWithId(new Integer(contentVersionIds[i]), db);
			    if(contentVersion.getStateId().intValue() != ContentVersionVO.WORKING_STATE.intValue())
			    {
		            List events = new ArrayList();
			        contentVersion = ContentStateController.changeState(contentVersion.getId(), ContentVersionVO.WORKING_STATE, "Automatic by the replace function", true, null, infoGluePrincipal, null, db, events);
			        logger.info("Setting the version to working before replacing string...");
			    }
			    
			    String value = contentVersion.getVersionValue();
			    value = value.replaceAll(searchString, replaceString);
			    
			    contentVersion.setVersionValue(value);

			    replacements++;
			}
			
			commitTransaction(db);
		}
		catch ( Exception e )
		{
			rollbackTransaction(db);
			throw new SystemException("An error occurred when we tried to fetch a list of users in this role. Reason:" + e.getMessage(), e);			
		}
		
		return replacements;
		
   	}
   	
	/**
	 * This is a method that never should be called.
	 */

	public BaseEntityVO getNewVO()
	{
		return null;
	}
   	
}