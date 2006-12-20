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

package org.infoglue.cms.applications.managementtool.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.DigitalAssetController;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.FileUploadHelper;

import webwork.action.ActionContext;

/**
 * This class represents the optimization tools of the system
 * It offers the user a few options in which to optimize the database and model.
 * 
 * @author mattias
 */

public class ViewArchiveToolAction extends InfoGlueAbstractAction
{
	private static final long serialVersionUID = 1L;
	
	private int numberOfVersionsToKeep = 3;
	private int assetFileSizeLimit = 500000;
	private List optimizationBeanList = new ArrayList();
	
	private String[] digitalAssetId;
	private String archiveUrl;
	private StringBuffer archiveFileSize = new StringBuffer();
	
    public String doInput() throws Exception
    {
    	return "input";
    }

	public String doInputArchiveOldAssets() throws Exception
    {
		optimizationBeanList = ContentVersionController.getContentVersionController().getHeavyContentVersions(numberOfVersionsToKeep, assetFileSizeLimit);
        		
        return "inputArchiveOldAssets";
    }

	public String doInputRestoreAssetArchive() throws Exception
    {        		
        return "inputRestoreAssetArchive";
    }
	
	public String doArchiveOldAssets() throws Exception
    {
		archiveUrl = DigitalAssetController.getController().archiveDigitalAssets(digitalAssetId, archiveFileSize);
		
        return "successArchive";
    }

	public String doRestoreAssetArchive() throws Exception
    {
		File file = FileUploadHelper.getUploadedFile(ActionContext.getContext().getMultiPartRequest());
		if(file == null || !file.exists())
			throw new SystemException("The file upload must have gone bad as no file reached the restore utility.");

		DigitalAssetController.getController().restoreAssetArchive(file);
		
        return "successRestoreArchive";
    }

    public String doExecute() throws Exception
    {
        return "success";
    }

	public int getNumberOfVersionsToKeep() 
	{
		return numberOfVersionsToKeep;
	}

	public void setNumberOfVersionsToKeep(int numberOfVersionsToKeep) 
	{
		this.numberOfVersionsToKeep = numberOfVersionsToKeep;
	}

	public List getOptimizationBeanList() 
	{
		return optimizationBeanList;
	}

	public void setDigitalAssetId(String[] digitalAssetId) 
	{
		this.digitalAssetId = digitalAssetId;
	}

	public String getArchiveUrl() 
	{
		return archiveUrl;
	}

	public String getArchiveFileSize() 
	{
		return archiveFileSize.toString();
	}

	public int getAssetFileSizeLimit() 
	{
		return assetFileSizeLimit;
	}

	public void setAssetFileSizeLimit(int assetFileSizeLimit) 
	{
		this.assetFileSizeLimit = assetFileSizeLimit;
	}

}