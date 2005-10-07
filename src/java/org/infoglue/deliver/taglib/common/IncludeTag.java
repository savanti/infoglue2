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

package org.infoglue.deliver.taglib.common;

import java.util.Iterator;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.deliver.taglib.TemplateControllerTag;

public class IncludeTag extends TemplateControllerTag 
{
	private static final long serialVersionUID = 4050206323348354355L;
	
	private Integer contentId;
	private String relationAttributeName;
	private String contentName;
	private String template;
	
    public IncludeTag()
    {
        super();
    }

	public int doEndTag() throws JspException
    {
		try
        {
		    if(contentId == null)
		    {
			    Integer componentContentId = this.getController().getComponentLogic().getInfoGlueComponent().getContentId();
	
			    List relatedContents = this.getController().getRelatedContents(componentContentId, relationAttributeName);
			    Iterator i = relatedContents.iterator();
			    while(i.hasNext())
			    {
			        ContentVO contentVO = (ContentVO)i.next();
			        if(contentVO.getName().equalsIgnoreCase("Referer JSP"))
	                {
			            contentId = contentVO.getId();
			            break;
	                }
			    }
	
			    template = this.getController().getContentAttribute(contentId, "Template");
		    }
		    else
		        template = this.getController().getContentAttribute(contentId, "Template");
		    
		    produceResult(this.getController().renderString(template));
        } 
		catch (Exception e)
        {
            e.printStackTrace();
        }
		
        return EVAL_PAGE;
    }

    public void setTemplate(String template) throws JspException
    {
        this.template = evaluateString("includeTag", "template", template);
    }
    
    public void setContentId(String contentId) throws JspException
    {
        this.contentId = evaluateInteger("includeTag", "contentId", contentId);
    }
    
    public void setRelationAttributeName(String relationAttributeName) throws JspException
    {
        this.relationAttributeName = evaluateString("includeTag", "relationAttributeName", relationAttributeName);
    }

    public void setContentName(String contentName) throws JspException
    {
        this.contentName = evaluateString("includeTag", "contentName", contentName);
    }
}
