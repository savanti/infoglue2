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

package org.infoglue.cms.applications.contenttool.actions;

import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.RolePropertiesVO;
import org.infoglue.cms.entities.management.UserPropertiesVO;
import org.infoglue.cms.entities.content.DigitalAssetVO;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.controllers.kernel.impl.simple.*;
import org.infoglue.cms.util.CmsLogger;
import org.infoglue.cms.util.CmsPropertyHandler;

import webwork.action.Action;
import webwork.action.ActionContext;
import webwork.config.Configuration;
import webwork.multipart.MultiPartRequestWrapper;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.io.*;
import java.util.List;


public class CreateDigitalAssetAction extends ViewDigitalAssetAction
{
	private String entity;
	private Integer entityId;

	private Integer contentVersionId = null;
	private String digitalAssetKey   = "";
	private Integer uploadedFilesCounter = new Integer(0);
	private ContentVersionVO contentVersionVO;
	private ContentTypeDefinitionVO contentTypeDefinitionVO;
	private String reasonKey;
	private DigitalAssetVO digitalAssetVO = null;
	private String closeOnLoad;

    public CreateDigitalAssetAction()
    {
    }
        
    public void setContentVersionId(Integer contentVersionId)
	{
		this.contentVersionId = contentVersionId;
	}    
     
    public Integer getContentVersionId()
	{
		return this.contentVersionId;
	}
	
    public void setDigitalAssetKey(String digitalAssetKey)
	{
		this.digitalAssetKey = digitalAssetKey;
	}

    public String getDigitalAssetKey()
    {
        return digitalAssetKey;
    }

	public void setUploadedFilesCounter(Integer uploadedFilesCounter)
	{
		this.uploadedFilesCounter = uploadedFilesCounter;
	}

	public Integer getUploadedFilesCounter()
	{
		return this.uploadedFilesCounter;
	}
	   
	public List getDefinedAssetKeys()
	{
		return ContentTypeDefinitionController.getController().getDefinedAssetKeys(this.contentTypeDefinitionVO.getSchemaValue());
	}
	
    public String doExecute() //throws Exception
    {
        try
        {
            MultiPartRequestWrapper mpr = ActionContext.getMultiPartRequest();
            if(mpr == null)
            {
                this.reasonKey = "tool.contenttool.fileUpload.fileUploadFailedOnSizeText";
                return "uploadFailed";
            }
            
	        if(this.contentVersionId != null)
	        {
		    	this.contentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(this.contentVersionId);
		        this.contentTypeDefinitionVO = ContentController.getContentController().getContentTypeDefinition(contentVersionVO.getContentId());
	        }
	        else
	        {
	            if(this.entity.equalsIgnoreCase("UserProperties"))
	            {
	                UserPropertiesVO userPropertiesVO = UserPropertiesController.getController().getUserPropertiesVOWithId(this.entityId);
	                this.contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(userPropertiesVO.getContentTypeDefinitionId());            
	            }
	            else if(this.entity.equalsIgnoreCase("RoleProperties"))
	            {
	                RolePropertiesVO rolePropertiesVO = RolePropertiesController.getController().getRolePropertiesVOWithId(this.entityId);
	                this.contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(rolePropertiesVO.getContentTypeDefinitionId());            
	            }
	        }
	        
	
	    	InputStream is = null;
			//File renamedFile = null;
			File file = null;
			
	    	try 
	    	{
	    		if(mpr != null)
	    		{ 
		    		Enumeration names = mpr.getFileNames();
		         	while (names.hasMoreElements()) 
		         	{
		            	String name 		  = (String)names.nextElement();
						String contentType    = mpr.getContentType(name);
						String fileSystemName = mpr.getFilesystemName(name);
						
		            	CmsLogger.logInfo("digitalAssetKey:" + digitalAssetKey);
		            	CmsLogger.logInfo("name:" + name);
		            	CmsLogger.logInfo("contentType:" + contentType);
		            	CmsLogger.logInfo("fileSystemName:" + fileSystemName);
		            	
		            	file = mpr.getFile(name);
		            	//String fileName = this.contentVersionId + "_" + System.currentTimeMillis() + "_" + fileSystemName;
						String fileName = fileSystemName;
						
						fileName = new VisualFormatter().replaceNonAscii(fileName, '_');
						
						String tempFileName = "tmp_" + System.currentTimeMillis() + "_" + fileName;
		            	//String filePath = file.getParentFile().getPath();
		            	String filePath = CmsPropertyHandler.getProperty("digitalAssetPath");
		            	fileSystemName = filePath + File.separator + tempFileName;
		            	
						//CmsLogger.logInfo("New fileSystemName:" + fileSystemName);
		            	//renamedFile = new File(fileSystemName);
						//boolean isRenamed = file.renameTo(renamedFile);
						//CmsLogger.logInfo("isRenamed:" + isRenamed);
						
		            	DigitalAssetVO newAsset = new DigitalAssetVO();
						newAsset.setAssetContentType(contentType);
						newAsset.setAssetKey(digitalAssetKey);
						newAsset.setAssetFileName(fileName);
						newAsset.setAssetFilePath(filePath);
						newAsset.setAssetFileSize(new Integer(new Long(file.length()).intValue()));
						//is = new FileInputStream(renamedFile);
						is = new FileInputStream(file);
						
						if(this.contentVersionId != null)
						    digitalAssetVO = DigitalAssetController.create(newAsset, is, this.contentVersionId);
		         		else
		         		    digitalAssetVO = DigitalAssetController.create(newAsset, is, this.entity, this.entityId);
		         		    
						this.uploadedFilesCounter = new Integer(this.uploadedFilesCounter.intValue() + 1);
		         	}
	    		}
	    		else
	    		{
	    			CmsLogger.logSevere("File upload failed for some reason.");
	    		}
	      	} 
	      	catch (Throwable e) 
	      	{
	      	    e.printStackTrace();
	      		//CmsLogger.logSevere("An error occurred when we tried to upload a new asset:" + e.getMessage(), e);
	      	}
			finally
			{
				try
				{
					is.close();
					file.delete();
				}
				catch(Throwable e){ e.printStackTrace(); }
			}
        }
        catch(Throwable e){ e.printStackTrace(); }
        
        return "success";
    }

	/**
	 * This method fetches the blob from the database and saves it on the disk.
	 * Then it returnes a url for it
	 */
	
	public String getDigitalAssetUrl() throws Exception
	{
		String imageHref = null;
		try
		{
       		imageHref = DigitalAssetController.getDigitalAssetUrl(digitalAssetVO.getDigitalAssetId());
		}
		catch(Exception e)
		{
			CmsLogger.logWarning("We could not get the url of the digitalAsset: " + e.getMessage(), e);
		}
		
		return imageHref;
	}
	
    public String getAssetThumbnailUrl()
    {
        String imageHref = null;
		try
		{
       		imageHref = DigitalAssetController.getDigitalAssetThumbnailUrl(digitalAssetVO.getDigitalAssetId());
		}
		catch(Exception e)
		{
			CmsLogger.logWarning("We could not get the url of the thumbnail: " + e.getMessage(), e);
		}
		
		return imageHref;
    }
    

    public String getEntity()
    {
        return entity;
    }
    
    public void setEntity(String entity)
    {
        this.entity = entity;
    }
    
    public Integer getEntityId()
    {
        return entityId;
    }
    
    public void setEntityId(Integer entityId)
    {
        this.entityId = entityId;
    }
    
    public String getReasonKey()
    {
        return reasonKey;
    }
    
    public String getCloseOnLoad()
    {
        return closeOnLoad;
    }
    
    public void setCloseOnLoad(String closeOnLoad)
    {
        this.closeOnLoad = closeOnLoad;
    }
}