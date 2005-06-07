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

package org.infoglue.cms.util.workflow;

import java.util.*;

import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.CmsLogger;
import org.infoglue.cms.entities.mydesktop.*;
import com.opensymphony.workflow.*;
import com.opensymphony.workflow.spi.*;
import com.opensymphony.workflow.query.*;
import com.opensymphony.workflow.basic.BasicWorkflow;
import com.opensymphony.workflow.loader.*;
import com.opensymphony.module.propertyset.PropertySet;
import net.sf.hibernate.*;
import net.sf.hibernate.cfg.Configuration;

/**
 * A facade to OSWorkflow that gives us a place to cache workflow data as we need it while interacting with it.
 * This class has kind of a strange interface due to the idiosyncracies of the OSWorkflow, particularly
 * the Workflow interface.  The idea is to encapsulate the interactions with OSWorkflow and eliminate the
 * need to pass a Workflow reference and the workflow ID all over the place when extracting data from OSWorkflow
 * @author <a href="mailto:jedprentice@gmail.com">Jed Prentice</a>
 * @version $Revision: 1.14 $ $Date: 2005/06/07 21:11:01 $
 */
public class WorkflowFacade
{
	private static SessionFactory hibernateSessionFactory;

	static
	{
		try
		{
			hibernateSessionFactory = new Configuration().configure().buildSessionFactory();
		}
		catch (HibernateException e)
		{
			e.printStackTrace();
			throw new ExceptionInInitializerError(e);
		}
	}

	private final AbstractWorkflow workflow;
	private long workflowId;

	private WorkflowDescriptor workflowDescriptor;

	/**
	 * Constructs a WorkflowFacade with the given user principal
	 * @param userPrincipal an InfoGluePrincipal representing a system user
	 */
	public WorkflowFacade(InfoGluePrincipal userPrincipal)
	{
		workflow = new BasicWorkflow(userPrincipal.getName());
		workflow.getConfiguration().getPersistenceArgs().put("sessionFactory", hibernateSessionFactory);
	}

	/**
	 * Constructs a WorkflowFacade with the given user principal representing an initialized instance of the workflow
	 * with the given name.  "Initialized" in this context means that the initial action has been executed and we have
	 * the workflow ID.
	 * @param userPrincipal an InfoGluePrincipal representing a system user
	 * @param name the name of the workflow to create
	 * @param initialAction the ID of the initial action to perform to get the workflow started.
	 */
	public WorkflowFacade(InfoGluePrincipal userPrincipal, String name, int initialAction) throws SystemException
	{
		this(userPrincipal, name, initialAction, new HashMap());
	}

	/**
	 * Constructs a WorkflowFacade with the given user principal representing an initialized instance of the workflow
	 * with the given name.  "Initialized" in this context means that the initial action has been executed and we have
	 * the workflow ID.
	 * @param userPrincipal an InfoGluePrincipal representing a system user
	 * @param name the name of the workflow to create
	 * @param initialAction the ID of the initial action to perform to get the workflow started.
	 * @param inputs a map of inputs to use to initialize the workflow.
	 */
	public WorkflowFacade(InfoGluePrincipal userPrincipal, String name, int initialAction, Map inputs) throws SystemException
	{
		this(userPrincipal);
		initialize(name, initialAction, inputs);
	}

	/**
	 * Constructs a WorkflowFacade for a user with the given workflow ID.
	 * @param userPrincipal an InfoGluePrincipal representing a system user
	 * @param workflowId the ID representing an instance of the desired workflow
	 */
	public WorkflowFacade(InfoGluePrincipal userPrincipal, long workflowId)
	{
		this(userPrincipal);
		setWorkflowIdAndDescriptor(workflowId);
	}

	/**
	 * Sets the workflow ID to the given value, and caches the associated workflow descriptor
	 * @param workflowId the desired workflow ID
	 */
	private void setWorkflowIdAndDescriptor(long workflowId)
	{
		this.workflowId = workflowId;
		workflowDescriptor = workflow.getWorkflowDescriptor(workflow.getWorkflowName(workflowId));
	}

	/**
	 * Returns the workflow ID
	 * @return the workflow ID
	 */
	public long getWorkflowId()
	{
		return workflowId;
	}

	/**
	 * Initializes the workflow, setting workflowId as a side-effect.
	 * @param name the name of the workflow to initialize
	 * @param initialAction the ID of the initial action to perform to get the workflow started.
	 * @param inputs a map of inputs to use to initialize the workflow.
	 * @throws SystemException if a workflow error occurs.
	 */
	private void initialize(String name, int initialAction, Map inputs) throws SystemException
	{
		try
		{
			setWorkflowIdAndDescriptor(workflow.initialize(name, initialAction, inputs));
		}
		catch (Exception e)
		{
			throw new SystemException(e);
		}
	}

	/**
	 * Performs an action using the given inputs
	 * @param actionId the ID of the action to perform
	 * @param inputs a map of inputs to the action
	 * @throws SystemException if a workflow error occurs, or if the underlying workflow is not active
	 */
	public void doAction(int actionId, Map inputs) throws SystemException
	{
		try
		{
			if (!isActive())
				throw new SystemException("Workflow " + workflowId + " is no longer active");

			workflow.doAction(workflowId, actionId, inputs);
		}
		catch (Exception e)
		{
			throw new SystemException(e);
		}
	}

	/**
	 * Returns the property set associated with the underlying workflow
	 * @return the property set associated with the underlying workflow
	 */
	public PropertySet getPropertySet()
	{
		return workflow.getPropertySet(workflowId);
	}

	/**
	 * Returns the state of the underlying workflow entry
	 * @return the state of the underlying workflow entry
	 */
	private int getEntryState()
	{
		return workflow.getEntryState(workflowId);
	}

	/**
	 * Indicates whether the underlying workflow is active
	 * @return true if the underlying workflow's state is WorkflowEntry.ACTIVATED, otherwise returns false.
	 */
	private boolean isActive()
	{
		return getEntryState() == WorkflowEntry.ACTIVATED;
	}

	/**
	 * Returns a list of all declared workflows, i.e., workflows defined in workflows.xml
	 * @return a list WorkflowVOs representing all declared workflows
	 */
	public List getDeclaredWorkflows()
	{
		String[] workflowNames = workflow.getWorkflowNames();
		List availableWorkflows = new ArrayList();

		for (int i = 0; i < workflowNames.length; i++)
			availableWorkflows.add(createWorkflowVO(workflowNames[i]));

		return availableWorkflows;
	}

	/**
	 * Returns a list of all active workflows.
	 * @return a list of WorkflowVOs representing all active workflows
	 * @throws SystemException if an error occurs finding the active workflows
	 */
	public List getActiveWorkflows() throws SystemException
	{
		List workflowVOs = new ArrayList();

		for (Iterator workflows = findActiveWorkflows().iterator(); workflows.hasNext();)
		{
			setWorkflowIdAndDescriptor(((Long)workflows.next()).longValue());
			CmsLogger.logInfo("workflowId:" + workflowId);
			workflowVOs.add(createWorkflowVO());
		}

		return workflowVOs;
	}

	/**
	 * Finds all active workflows
	 * @return A list of workflowIds representing workflows that match the hard-wored query expression.
	 * @throws SystemException if a workflow error occurs during the search
	 */
	private List findActiveWorkflows() throws SystemException
	{
		try
		{
			return workflow.query(new WorkflowExpressionQuery(new FieldExpression(FieldExpression.STATE,
						FieldExpression.ENTRY, FieldExpression.EQUALS, new Integer(WorkflowEntry.ACTIVATED))));
		}
		catch (WorkflowException e)
		{
			throw new SystemException(e);
		}
	}

	/**
	 * Returns all current steps for the workflow, i.e., steps that could be performed in the workflow's current state
	 * Steps are filtered according to ownership; if a step has an owner, it is only included if the ownser matches
	 * the caller or if the current user is an administrator.
	 * @return a list of WorkflowStepVOs representing the current steps of the workflow with workflowId
	 */
	public List getCurrentSteps()
	{
		return createStepVOs(workflow.getCurrentSteps(workflowId));
	}

	/**
	 * Returns all history steps for the workflow, i.e., all the steps that have already been performed.
	 * @return a list of WorkflowStepVOs representing all history steps for the workflow with workflowId
	 */
	public List getHistorySteps()
	{
		return createStepVOs(workflow.getHistorySteps(workflowId));
	}

	/**
	 * Returns all steps for a workflow definition.  These are the steps declared in the workfow descriptor; there is
	 * no knowledge of current or history steps at this point.
	 * @return a list of WorkflowStepVOs representing all steps in the workflow.
	 */
	public List getDeclaredSteps()
	{
		return getDeclaredSteps(workflowDescriptor);
	}

	/**
	 * Creates a list of WorkflowStepVOs from the given list of steps
	 * @param steps a list of Steps
	 * @return a list of WorkflowStepVOs corresponding to all steps that pass the filter
	 */
	private List createStepVOs(List steps)
	{
		List stepVOs = new ArrayList();
		for (Iterator i = steps.iterator(); i.hasNext();)
			stepVOs.add(createStepVO((Step)i.next()));

		return stepVOs;
	}

	/**
	 * Returns all steps for a workflow definition.  These are the steps declared in the workfow descriptor; there is
	 * no knowledge of current or history steps at this point.
	 * @param descriptor a workflow descriptor from which to get current steps
	 * @return a list of WorkflowStepVOs representing all steps in the workflow.
	 */
	private List getDeclaredSteps(WorkflowDescriptor descriptor)
	{
		List steps = new ArrayList();
		for (Iterator i = descriptor.getSteps().iterator(); i.hasNext();)
			steps.add(createStepVO((StepDescriptor)i.next()));

		return steps;
	}

	/**
	 * Returns a list of initial actions for the workflow
	 * @return a list of WorkflowActionVOs representing the global actions for the workflow with workflowId
	 */
	private List getInitialActions()
	{
		return createActionVOs(workflowDescriptor.getInitialActions());
	}

	/**
	 * Returns a list of global actions for the workflow
	 * @return a list of WorkflowActionVOs representing the global actions for the workflow with workflowId
	 */
	private List getGlobalActions()
	{
		return createActionVOs(workflowDescriptor.getGlobalActions());
	}

	/**
	 * Creates a list of WorkflowActionVOs from a list of action descriptors
	 * @param actionDescriptors a list of ActionDescriptors
	 * @return a list of WorkflowActionVOs representing actionDescriptors
	 */
	private List createActionVOs(List actionDescriptors)
	{
		List actions = new ArrayList();
		for (Iterator i = actionDescriptors.iterator(); i.hasNext();)
			actions.add(createActionVO((ActionDescriptor)i.next()));

		return actions;

	}

	/**
	 * Creates a new WorkflowVO.  This represents a pretty complete workflow; you get all the current steps, history
	 * steps, available actions, and global actions.
	 * @return a WorkflowVO representing workflow, with workflowId
	 */
	public WorkflowVO createWorkflowVO()
	{
		WorkflowVO workflowVO = new WorkflowVO(new Long(workflowId), workflow.getWorkflowName(workflowId));
		workflowVO.setCurrentSteps(getCurrentSteps());
		workflowVO.setHistorySteps(getHistorySteps());
		workflowVO.setInitialActions(getInitialActions());
		workflowVO.setGlobalActions(getGlobalActions());

		return workflowVO;
	}

	/**
	 * Creates a new WorkflowVO from workflow with the given name.  The resulting workflow VO contains only a
	 * minimal amount of data because we don't have the workflow ID.  Basically all you get is all the steps.
	 * @param name the name of the desired workflow
	 * @return a new WorkflowVO representing workflow
	 */
	private WorkflowVO createWorkflowVO(String name)
	{
		WorkflowVO workflowVO = new WorkflowVO(null, name);
		workflowVO.setDeclaredSteps(getDeclaredSteps(workflow.getWorkflowDescriptor(name)));
		return workflowVO;
	}

	/**
	 * Creates a WorkflowStepVO from the given step
	 * @param step the desired step
	 * @return a new WorkflowStepVO representing step.
	 */
	private WorkflowStepVO createStepVO(Step step)
	{
		CmsLogger.logInfo("step:" + step + ':' + step.getId());
		CmsLogger.logInfo("Owner:" + step.getOwner());

		WorkflowStepVO stepVO = new WorkflowStepVO();
		stepVO.setId(new Integer((int)step.getId()));// Hope it doesn't get too big; we are stuck with int thanks to BaseEntityVO
		stepVO.setStepId(new Integer(step.getStepId()));
		stepVO.setWorkflowId(new Long(workflowId));
		stepVO.setStatus(step.getStatus());
		stepVO.setStartDate(step.getStartDate());
		stepVO.setFinishDate(step.getFinishDate());
		stepVO.setOwner(step.getOwner());
		stepVO.setCaller(step.getCaller());

		StepDescriptor stepDescriptor = workflowDescriptor.getStep(step.getStepId());
		stepVO.setName(stepDescriptor.getName());
		for (Iterator i = stepDescriptor.getActions().iterator(); i.hasNext();)
			stepVO.addAction(createActionVO((ActionDescriptor)i.next()));

		return stepVO;
	}

	/**
	 * Creates a WorkflowStepVO from a step descriptor.  Some of the step data, e.g., status, startDate,
	 * finishDate, etc. cannot be populated here because the step descriptor does not know about these things.
	 * @param stepDescriptor a step descriptor
	 * @return a WorkflowStepVO representing stepDescriptor
	 */
	private WorkflowStepVO createStepVO(StepDescriptor stepDescriptor)
	{
		WorkflowStepVO step = new WorkflowStepVO();
		step.setStepId(new Integer(stepDescriptor.getId()));
		step.setName(stepDescriptor.getName());
		step.setStatus("Not started");

		for (Iterator i = stepDescriptor.getActions().iterator(); i.hasNext();)
			step.addAction(createActionVO((ActionDescriptor)i.next()));

		return step;
	}

	/**
	 * Creates a WorkflowActionVO for the given action descriptor
	 * @param actionDescriptor an action descriptor
	 * @return a WorkflowActionVO representing actionDescriptor
	 */
	private WorkflowActionVO createActionVO(ActionDescriptor actionDescriptor)
	{
		CmsLogger.logInfo("Action:" + actionDescriptor.getId() + ':' + actionDescriptor.getName()
					+ ':' + actionDescriptor.getParent().getClass());

		WorkflowActionVO actionVO = new WorkflowActionVO(new Integer(actionDescriptor.getId()));
		actionVO.setWorkflowId(new Long(workflowId));
		actionVO.setName(actionDescriptor.getName());
		actionVO.setView(actionDescriptor.getView());
		actionVO.setAutoExecute(actionDescriptor.getAutoExecute());
		actionVO.setMetaAttributes(actionDescriptor.getMetaAttributes());
		return actionVO;
	}
}
