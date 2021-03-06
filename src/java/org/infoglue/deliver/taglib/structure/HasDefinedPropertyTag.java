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

package org.infoglue.deliver.taglib.structure;

import javax.servlet.jsp.JspException;

import org.infoglue.deliver.taglib.component.ComponentLogicTag;

public class HasDefinedPropertyTag extends ComponentLogicTag 
{
	private static final long serialVersionUID = 4050206323348354355L;

	private Integer siteNodeId;
	private Integer languageId;
	private String propertyName;
	private boolean useInheritance = true;
	
    public HasDefinedPropertyTag()
    {
        super();
    }

	public int doEndTag() throws JspException
    {
		setResultAttribute(new Boolean(this.getComponentLogic().getHasDefinedProperty(siteNodeId, languageId, propertyName, useInheritance)));
        return EVAL_PAGE;
    }

    public void setSiteNodeId(String siteNodeId) throws JspException
    {
        this.siteNodeId = evaluateInteger("HasDefinedPropertyTag", "siteNodeId", siteNodeId);
    }

    public void setLanguageId(String languageId) throws JspException
    {
        this.languageId = evaluateInteger("HasDefinedPropertyTag", "languageId", languageId);
    }

    public void setPropertyName(String propertyName) throws JspException
    {
        this.propertyName = evaluateString("hasDefinedProperty", "propertyName", propertyName);
    }

    public void setUseInheritance(boolean useInheritance)
    {
        this.useInheritance = useInheritance;
    }
    
}
