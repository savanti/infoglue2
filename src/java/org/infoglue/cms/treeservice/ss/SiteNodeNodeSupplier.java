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

package org.infoglue.cms.treeservice.ss;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.infoglue.cms.controllers.usecases.structuretool.ViewSiteNodeTreeUCC;
import org.infoglue.cms.controllers.usecases.structuretool.ViewSiteNodeTreeUCCFactory;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsLogger;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.sorters.ReflectionComparator;

import com.frovi.ss.Tree.BaseNode;
import com.frovi.ss.Tree.BaseNodeSupplier;

/**
 * ContentNodeSupplier.java
 * Created on 2002-sep-30 
 * @author Stefan Sik, ss@frovi.com 
 * ss
 */
public class SiteNodeNodeSupplier extends BaseNodeSupplier
{

	private ViewSiteNodeTreeUCC ucc;
	private ArrayList cacheLeafs;

	
	public SiteNodeNodeSupplier(Integer repositoryId, InfoGluePrincipal infoGluePrincipal) throws SystemException
	{
		SiteNodeVO vo =null;
		ucc = ViewSiteNodeTreeUCCFactory.newViewSiteNodeTreeUCC();	
		try
		{
			vo = ucc.getRootSiteNode(repositoryId, infoGluePrincipal);
			BaseNode rootNode =  new SiteNodeNodeImpl();
			rootNode.setChildren(true);
			rootNode.setId(vo.getId());
			rootNode.setTitle(vo.getName());
			rootNode.setContainer(vo.getIsBranch().booleanValue());	
			
			setRootNode(rootNode);
		}
		catch (ConstraintException e)
		{
			e.printStackTrace();
		}
			
	}
	/**
	 * @see com.frovi.ss.Tree.BaseNodeSupplier#hasChildren()
	 */
	public boolean hasChildren()
	{
		return false;
	}

	/**
	 * @see com.frovi.ss.Tree.INodeSupplier#getChildContainerNodes(Integer)
	 */
	public Collection getChildContainerNodes(Integer parentNode)
	{
		ArrayList ret = new ArrayList();
		cacheLeafs = new ArrayList();
		
		List children = null;
		try
		{
			children = ucc.getSiteNodeChildren(parentNode);
		}
		catch (ConstraintException e)
		{
			CmsLogger.logWarning("Error getting SiteNode Children", e);
		}
		catch (SystemException e)
		{
			CmsLogger.logWarning("Error getting SiteNode Children", e);
		}
		
		//Sort the tree nodes if setup to do so
		String sortProperty = CmsPropertyHandler.getProperty("structure.tree.sort");
		if(sortProperty != null)
			Collections.sort(children, new ReflectionComparator(sortProperty));
		
		Iterator i = children.iterator();
		while(i.hasNext())
		{
			SiteNodeVO vo = (SiteNodeVO) i.next();
			
			BaseNode node =  new SiteNodeNodeImpl();
			node.setId(vo.getId());
			node.setTitle(vo.getName());
			
			if (vo.getIsBranch().booleanValue())
			{
				node.setContainer(true);
				node.setChildren((vo.getChildCount().intValue() > 0)); // 
				ret.add(node);
			}
			else
			{
				node.setContainer(false);
				cacheLeafs.add(node);				
			}
			
		}
		
		return ret;
	}

	/**
	 * @see com.frovi.ss.Tree.INodeSupplier#getChildLeafNodes(Integer)
	 */
	public Collection getChildLeafNodes(Integer parentNode)
	{
		return cacheLeafs;
	}

}
