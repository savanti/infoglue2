/* ===============================================================================
 *
 * Part of the InfoGlue SiteNode Management Platform (www.infoglue.org)
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.infoglue.cms.controllers.kernel.impl.simple.*;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.util.CmsLogger;
import org.infoglue.cms.applications.common.actions.WebworkAbstractAction;

/**
 * This class implements submit to publish on many sitenode versions at once.
 *  
 * @author Mattias Bogeblad
 */

public class ChangeMultiSiteNodeVersionStatePublishAction extends WebworkAbstractAction
{
	private Integer siteNodeId;
	private List siteNodeVersionId = new ArrayList();
	private Integer stateId;
	private String versionComment;
			    
	/**
	 * This method gets called when calling this action. 
	 * If the stateId is 2 which equals that the user tries to prepublish the page. If so we
	 * ask the user for a comment as this is to be regarded as a new version. 
	 */
	   
    public String doExecute() throws Exception
    {      
		setSiteNodeVersionId( getRequest().getParameterValues("sel") );
		Iterator it = siteNodeVersionId.iterator();
		
		while(it.hasNext())
		{
			Integer siteNodeVersionId = (Integer)it.next();
			CmsLogger.logInfo("Publishing:" + siteNodeVersionId);
			SiteNodeStateController.changeState(siteNodeVersionId, SiteNodeVersionVO.PUBLISH_STATE, getVersionComment(), this.getInfoGluePrincipal(), null);
		}

       	return "success";
    }
        
    public java.lang.Integer getSiteNodeId()
    {
        return this.siteNodeId;
    }
        
    public void setSiteNodeId(java.lang.Integer siteNodeId)
    {
	    this.siteNodeId = siteNodeId;
    }
                	
	public void setStateId(Integer stateId)
	{
		this.stateId = stateId;
	}

	public void setVersionComment(String versionComment)
	{
		this.versionComment = versionComment;
	}
	
	public String getVersionComment()
	{
		return this.versionComment;
	}
	
	public Integer getStateId()
	{
		return this.stateId;
	}
    /*        
	public List getSiteNodeVersionId() 
	{
		return siteNodeVersionId;
	}
	*/

	private void setSiteNodeVersionId(String[] list) 
	{
		siteNodeVersionId = new ArrayList();
		for(int i=0; i < list.length; i++)
		{
			siteNodeVersionId.add(new Integer(list[i]));
		}		
	}
}
