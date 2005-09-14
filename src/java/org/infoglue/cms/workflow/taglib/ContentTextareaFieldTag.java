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

package org.infoglue.cms.workflow.taglib;


/**
 * 
 */
public class ContentTextareaFieldTag extends ElementTag 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7262153925857546242L;

	/**
	 * 
	 */
	public ContentTextareaFieldTag() 
	{
		super();
	}

	/**
	 * 
	 */
	protected Element createElement()
	{
		return new Element("textarea");
	}
	
	/**
	 * 
	 */
	protected void process() 
	{
		super.process();
	}
	
	/**
	 * 
	 */
	public void setName(final String name) 
	{
		getElement().attribute("name", name);
		getElement().text(getElementValue(name));
	}
	
	/**
	 * 
	 */
	public void setRows(final String rows) 
	{
		getElement().attribute("rows", rows);
	}

	/**
	 * 
	 */
	public void setColumns(final String columns) 
	{
		getElement().attribute("cols", columns);
	}
}
