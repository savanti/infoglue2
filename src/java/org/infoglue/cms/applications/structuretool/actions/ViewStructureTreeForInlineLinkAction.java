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

package org.infoglue.cms.applications.structuretool.actions;

import org.infoglue.cms.controllers.kernel.impl.simple.*;
import org.infoglue.cms.applications.common.actions.WebworkAbstractAction;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

import java.util.List;

/**
 * This action shows the Content-tree when binding stuff.
 */ 

public class ViewStructureTreeForInlineLinkAction extends WebworkAbstractAction
{
    private Integer repositoryId;
    private ConstraintExceptionBuffer ceb;
	private String tree;	
	private List repositories;
	private String textAreaId = "";
	private Integer oldSiteNodeId;
	
	
	public ViewStructureTreeForInlineLinkAction()
	{
		this.ceb = new ConstraintExceptionBuffer();			
	}	

	public void setRepositoryId(Integer repositoryId)
	{
		this.repositoryId = repositoryId;
	}

	public Integer getRepositoryId()
	{
		return this.repositoryId;
	}
   
	public String getTree()
	{
		return tree;
	}

	public void setTree(String string)
	{
		tree = string;
	}
	
	public String getCurrentAction()
	{
		return "ViewStructureTreeForInlineLink.action";
	}
	
    public String doExecute() throws Exception
    {
		this.repositories = RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal());

		if(this.repositoryId == null)
		{
			this.repositoryId = (Integer)getHttpSession().getAttribute("repositoryId");
			if(this.repositoryId == null)
				this.repositoryId = RepositoryController.getController().getFirstRepositoryVO().getRepositoryId();		
		}
		
		return "success";					
    }
      
	public List getRepositories()
	{
		return repositories;
	}  
	
	public String getTextAreaId()
	{
		return textAreaId;
	}

	public void setTextAreaId(String string)
	{
		textAreaId = string;
	}

	public Integer getOldSiteNodeId() 
	{
		return oldSiteNodeId;
	}
	
	public void setOldSiteNodeId(Integer oldSiteNodeId) 
	{
		this.oldSiteNodeId = oldSiteNodeId;
	}
}
