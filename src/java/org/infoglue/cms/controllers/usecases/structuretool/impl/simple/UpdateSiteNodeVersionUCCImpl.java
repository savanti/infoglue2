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

import org.infoglue.cms.controllers.usecases.structuretool.UpdateSiteNodeVersionUCC;

import org.infoglue.cms.controllers.kernel.impl.simple.*;
import org.infoglue.cms.controllers.kernel.impl.simple.BaseUCCController;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;

import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.entities.structure.SiteNodeVersion;

import org.infoglue.cms.exception.*;
import org.infoglue.cms.util.*;

import org.exolab.castor.jdo.Database;

public class UpdateSiteNodeVersionUCCImpl extends BaseUCCController implements UpdateSiteNodeVersionUCC
{
        
    public SiteNodeVersionVO updateSiteNodeVersion(Integer siteNodeVersionId, SiteNodeVersionVO siteNodeVersionVO) throws ConstraintException, SystemException
    {
        Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        SiteNodeVersion siteNodeVersion = null;

        beginTransaction(db);

        try
        {
            //add validation here if needed
            siteNodeVersion = SiteNodeVersionController.getController().getSiteNodeVersionWithId(siteNodeVersionId, db);
            siteNodeVersion.setValueObject(siteNodeVersionVO);

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
}
        
