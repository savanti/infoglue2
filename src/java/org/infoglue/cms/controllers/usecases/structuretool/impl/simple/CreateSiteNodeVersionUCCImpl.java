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

package org.infoglue.cms.controllers.usecases.structuretool.impl.simple;

import org.infoglue.cms.controllers.usecases.structuretool.CreateSiteNodeVersionUCC;

import org.infoglue.cms.controllers.kernel.impl.simple.BaseUCCController;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;

import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.entities.structure.SiteNodeVersion;
import org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl;

import org.infoglue.cms.exception.*;
import org.infoglue.cms.util.*;

import org.exolab.castor.jdo.Database;

public class CreateSiteNodeVersionUCCImpl extends BaseUCCController implements CreateSiteNodeVersionUCC
{
        
    public SiteNodeVersionVO createSiteNodeVersion(SiteNodeVersionVO siteNodeVersionVO) throws ConstraintException, SystemException
    {
        Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        SiteNodeVersion siteNodeVersion = null;

        beginTransaction(db);

        try
        {
            //Here you might want to add some validate functonality?
            siteNodeVersion = createSiteNodeVersionEntity(db, siteNodeVersionVO);
             
            //If any of the validations or setMethods reported an error, we throw them up now before create.
            ceb.throwIfNotEmpty();
            
            commitTransaction(db);
        }
        catch(ConstraintException ce)
        {
            CmsLogger.logWarning("An error occurred so we should not complete the transaction:" + ce, ce);
            rollbackTransaction(db);
            throw ce;
        }
        catch(Exception e)
        {
            CmsLogger.logSevere("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }

        return siteNodeVersion.getValueObject();
    }     
    
    
    /**
     * Creates the entity SiteNodeVersion sent in and references the correct other entities
     */
     
    private SiteNodeVersion createSiteNodeVersionEntity(Database db, SiteNodeVersionVO siteNodeVersionVO) throws SystemException
    {
        SiteNodeVersion siteNodeVersion = null;

        try
        {
            //Fetch related entities here if they should be referenced        
            
            siteNodeVersion = new SiteNodeVersionImpl();
            siteNodeVersion.setValueObject(siteNodeVersionVO);
            //Refrence other entities through setters here
            db.create(siteNodeVersion);
        }
        catch(Exception e)
        {
            throw new SystemException("An error occurred when we tried to create the SiteNodeVersion in the database. Reason:" + e.getMessage(), e);    
        }
        
        return siteNodeVersion;
        
    }

}
        
