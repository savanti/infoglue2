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
 
package org.infoglue.deliver.util.charts;

import org.infoglue.cms.util.*;
import org.infoglue.deliver.controllers.kernel.impl.simple.TemplateController;

import org.jfree.chart.ChartUtilities;

import java.io.File;

/**
 * @author Mattias Bogeblad
 *
 * This is the helper class for charts. It gives the user a possibility to 
 * invoke different charts and supply data.
 */

public class ChartHelper 
{
	private TemplateController templateController = null;
	
	/**
	 * The default constructor.
	 */

	public ChartHelper(TemplateController templateController)
	{
		this.templateController = templateController;
	}
	
	/**
	 * This method invokes a named chart with the data supplied in xml-format.
	 */	
	
	public String renderGraph(String chartName, String dataAsXML, int width, int height)
	{
		String assetUrl = "";

		try
		{
			XMLDataDiagram demo = null;
			
			if(chartName.equals("TimeSeriesDiagram"))
			{
				demo = new TimeSeriesDiagram();
				demo.setDiagramData(dataAsXML);
				demo.renderChart();
			}
			else
			{
				//Todo - for now...
				demo = new TimeSeriesDiagram();
				demo.setDiagramData(dataAsXML);
				demo.renderChart();
			}
			
			String uniqueId = chartName + width + height + dataAsXML.length();
			String fileName = uniqueId + ".png";
			
			String filePath = CmsPropertyHandler.getProperty("digitalAssetPath");
			File file = new File(filePath + java.io.File.separator + fileName);
			ChartUtilities.saveChartAsPNG(file, demo.getChart(), width, height);
		
			assetUrl = this.templateController.getDigitalAssetBaseUrl() + "/" + file.getName();
		}
		catch(Exception e)
		{
			CmsLogger.logWarning("An error occurred when we tried to generate a graph:" + e.getMessage(), e);
		}
		
		return assetUrl;
	}
	
}
