/**
 * $Id: WorkflowVOTest.java,v 1.3 2005/01/14 15:48:39 jed Exp $
 * Created by jed on Dec 29, 2004
 */
package org.infoglue.cms.entities.mydesktop;

import java.util.Iterator;

import org.infoglue.cms.util.InfoGlueTestCase;
import org.infoglue.cms.util.workflow.*;
import org.infoglue.cms.security.InfoGluePrincipal;

/**
 * @author jed
 * @version $Revision: 1.3 $ $Date: 2005/01/14 15:48:39 $
 */
public class WorkflowVOTest extends InfoGlueTestCase
{
	private static final StepFilter adminFilter = new OwnerStepFilter(getAdminPrincipal());
	private static final StepFilter userFilter = new OwnerStepFilter(getCmsUserPrincipal());

	private WorkflowVO workflow = new WorkflowVO();

	protected void setUp() throws Exception
	{
		workflow.getCurrentSteps().add(createStep(getAdminPrincipal()));
		workflow.getCurrentSteps().add(createStep(getCmsUserPrincipal()));
		workflow.getCurrentSteps().add(new WorkflowStepVO());

		for (Iterator steps = workflow.getCurrentSteps().iterator(); steps.hasNext();)
			((WorkflowStepVO)steps.next()).addAction(new WorkflowActionVO());

		for (int i = 0; i < 2; ++i)
			workflow.getInitialActions().add(new WorkflowActionVO(new Integer(i)));
	}

	public void testGetAvailableActions() throws Exception
	{
		assertEquals("Wrong number of available actions:", 3, workflow.getAvailableActions().size());
	}

	public void testGetAvailableActionsFiltered() throws Exception
	{
		assertEquals("Wrong number of admin actions:", 3, workflow.getAvailableActions(adminFilter).size());
		assertEquals("Wrong number of user actions:", 2, workflow.getAvailableActions(userFilter).size());
	}

	public void testGetCurrentStepsFiltered() throws Exception
	{
		assertEquals("Wrong number of admin steps:", 3, workflow.getCurrentSteps(adminFilter).size());
		assertEquals("Wrong number of user steps:", 2, workflow.getCurrentSteps(userFilter).size());
	}

	public void testGetInitialAction() throws Exception
	{
		for (int i = 0; i < workflow.getInitialActions().size(); ++i)
		{
			WorkflowActionVO action= (WorkflowActionVO)workflow.getInitialActions().get(i);
			assertEquals("Wrong action at " + i + ": ", action, workflow.getInitialAction(action.getId()));
		}
	}

	public void testGetInitialActionNonexistent() throws Exception
	{
		try
		{
			workflow.getInitialAction(new Integer(-1));
			fail("IllegalArgumentException should have been thrown");
		}
		catch (IllegalArgumentException e)
		{
			// Expected
		}
	}

	public void testGetInitialActionNull() throws Exception
	{
		try
		{
			workflow.getInitialAction(null);
			fail("NullPointerException should have been thrown");
		}
		catch (NullPointerException e)
		{
			// Expected
		}
	}

	private static WorkflowStepVO createStep(InfoGluePrincipal owner)
	{
		WorkflowStepVO step = new WorkflowStepVO();
		step.setOwner(owner.getName());
		return step;
	}
}
