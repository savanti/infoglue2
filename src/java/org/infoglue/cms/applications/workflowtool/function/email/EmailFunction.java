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
package org.infoglue.cms.applications.workflowtool.function.email;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.SendFailedException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.infoglue.cms.applications.workflowtool.function.ContentFunction;
import org.infoglue.cms.applications.workflowtool.function.InfoglueFunction;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.content.DigitalAsset;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.mail.ByteDataSource;
import org.infoglue.cms.util.mail.MailService;
import org.infoglue.cms.util.mail.MailServiceFactory;
import org.infoglue.cms.util.mail.StringDataSource;

import com.opensymphony.workflow.WorkflowException;

/**
 * 
 */
public class EmailFunction extends InfoglueFunction 
{
	/**
	 * 
	 */
	private static final String ADDRESS_DELIMITER = ",";
	
	/**
	 * 
	 */
	private static final String EMAIL_PARAMETER_PREFIX = "email_";
	
	/**
	 * 
	 */
	public static final String ILLEGAL_ADDRESSES_PARAMETER = EMAIL_PARAMETER_PREFIX + "illegal";
	
	/**
	 * 
	 */
	public static final String TO_PARAMETER = EMAIL_PARAMETER_PREFIX + "to";
	
	/**
	 * 
	 */
	public static final String FROM_PARAMETER = EMAIL_PARAMETER_PREFIX + "from";
	
	/**
	 * 
	 */
	private static final String TO_ARGUMENT = "to";

	/**
	 * 
	 */
	private static final String FROM_ARGUMENT = "from";

	/**
	 * 
	 */
	private static final String SUBJECT_ARGUMENT = "subject";

	/**
	 * 
	 */
	private static final String BODY_ARGUMENT = "body";

	/**
	 * 
	 */
	private static final String BODY_TYPE_ARGUMENT = "type";
	
	/**
	 * 
	 */
	private static final String SILENT_MODE_ARGUMENT = "silent";
	
	/**
	 * 
	 */
	private static final String STATUS_OK = "status.email.ok";
	
	/**
	 * 
	 */
	private static final String STATUS_NOK = "status.email.nok";
	
	/**
	 * 
	 */
	private MailService service;
	
	/**
	 * 
	 */
	private MimeMessage message;
	
	/**
	 * 
	 */
	private MimeMultipart multipart;

	/**
	 * 
	 */
	private Collection attachments = new ArrayList();
	
	/**
	 * The illegal addresses.
	 */
	private Collection illegalAddresses; // type: <String>

	/**
	 * Indicates if a failure should be ignored.
	 */
	private boolean silentMode;
	
	/**
	 * 
	 */
	public EmailFunction() 
	{
		super();
	}

	/**
	 * 
	 */
	protected void execute() throws WorkflowException 
	{
		setFunctionStatus(silentMode ? STATUS_OK : STATUS_NOK);
		try
		{
			process();
		}
		catch(Exception e)
		{
			if(!silentMode)
			{
				throwException(e);
			}
			getLogger().warn("[silent mode]", e);
		}
		if(silentMode)
		{
			setParameter(ILLEGAL_ADDRESSES_PARAMETER, new ArrayList());
		}
		for(Iterator i=illegalAddresses.iterator(); i.hasNext(); )
			System.out.println("illegal [" + i.next().toString() + "]");
	}
	
	/**
	 * 
	 */
	private void process() throws WorkflowException
	{
		if(illegalAddresses.isEmpty())
		{
			initializeMailService();
			getAttachments();
			createMessage();
			sendMessage();
		}
	}
	
	/**
	 * 
	 */
	private void createMessage() throws WorkflowException
	{
		if(attachments.isEmpty())
		{
			createSimpleMessage();
		}
		else
		{
			createMultipartMessage();
		}
	}
	
	/**
	 * 
	 */
	private void createSimpleMessage() throws WorkflowException
	{
		getLogger().debug("Creating simple message.");
		initializeMessage();
		initializeSimpleBody();
	}

	/**
	 * 
	 */
	private void createMultipartMessage() throws WorkflowException
	{
		getLogger().debug("Creating message.");
		initializeMessage();
		initializeMultipart();
		createMainBodyPart();
		createAttachments();
	}
	
	/**
	 * 
	 */
	private void initializeMessage() throws WorkflowException
	{
		getLogger().debug("Initializing message.");
		message = service.createMessage();
		initializeTo();
		initializeFrom();
		initializeSubject();
	}
	
	/**
	 * 
	 */
	private void initializeSimpleBody() throws WorkflowException
	{
		getLogger().debug("Initializing simple body.");
		try
		{
	    	message.setDataHandler(getDataHandler(getArgument(BODY_ARGUMENT), getArgument(BODY_TYPE_ARGUMENT)));
		}
		catch(Exception e)
		{
			throwException(e);
		}
	}
	
	/**
	 * 
	 */
	private void initializeMultipart() throws WorkflowException
	{
		getLogger().debug("Initializing multipart.");
		try 
		{
			multipart = new MimeMultipart();
			message.setContent(multipart);
		}
		catch(Exception e)
		{
			throwException(e);
		}
	}
	
	/**
	 * 
	 */
	private void initializeTo() throws WorkflowException
	{
		getLogger().debug("Initializing to.");
		try 
		{
			if(argumentExists(TO_ARGUMENT))
			{
				getLogger().debug("Adding 'to' from argument [" + getArgument(TO_ARGUMENT) + "].");
		    	message.addRecipients(Message.RecipientType.TO, createAddresses(getArgument(TO_ARGUMENT)));
			}
			if(parameterExists(TO_PARAMETER))
			{
				getLogger().debug("Adding 'to' from parameters");
		    	message.addRecipients(Message.RecipientType.TO, addressesToArray((List) getParameter(TO_PARAMETER)));
			}
		}
		catch(Exception e)
		{
			throwException(e);
		}
	}
	
	/**
	 * 
	 */
	private void initializeFrom() throws WorkflowException
	{
		getLogger().debug("Initializing from.");
		try 
		{
			if(argumentExists(FROM_ARGUMENT))
			{
				getLogger().debug("Adding 'from' from argument [" + getArgument(FROM_ARGUMENT) + "].");
				message.addFrom(createAddresses(getArgument(FROM_ARGUMENT)));
			}
			if(parameterExists(FROM_PARAMETER))
			{
				getLogger().debug("Adding 'from' from parameter.");
		    	message.addFrom(addressesToArray((List) getParameter(FROM_PARAMETER)));
			}
		}
		catch(Exception e)
		{
			throwException(e);
		}
	}
	
	/**
	 * 
	 */
	private void initializeSubject() throws WorkflowException
	{
		getLogger().debug("Initializing subject.");
		try 
		{
			message.setSubject(getArgument(SUBJECT_ARGUMENT), UTF8_ENCODING);
		}
		catch(Exception e)
		{
			throwException(e);
		}
	}
	
	/**
	 * 
	 */
	private void createMainBodyPart() throws WorkflowException
	{
		getLogger().debug("Initializing main body part.");
		try 
		{
			final BodyPart part = new MimeBodyPart();
	    	part.setDataHandler(getDataHandler(getArgument(BODY_ARGUMENT), getArgument(BODY_TYPE_ARGUMENT)));
			multipart.addBodyPart(part);
		}
		catch(Exception e)
		{
			throwException(e);
		}
	}
	
	/**
	 * 
	 */
	private void createAttachments() throws WorkflowException
	{
		getLogger().debug("Found " + attachments.size() + " attachments.");
		for(final Iterator i = attachments.iterator(); i.hasNext(); )
		{
			createAttachment((DigitalAsset) i.next());
		}
	}
	
	/**
	 * 
	 */
	private void createAttachment(final DigitalAsset digitalAsset) throws WorkflowException
	{
		try
		{
			final BodyPart part = new MimeBodyPart();
	    	part.setDataHandler(getDataHandler(digitalAsset.getAssetBytes(), digitalAsset.getValueObject().getAssetContentType()));
			part.setFileName(digitalAsset.getAssetFileName());
	    	multipart.addBodyPart(part);
		}
		catch(Exception e)
		{
			throwException(e);
		}
	}

	/**
	 * 
	 */
	private void getAttachments() throws WorkflowException
	{
		try
		{
			final ContentVersionVO contentVersionVO = (ContentVersionVO) getParameter(ContentFunction.CONTENT_VERSION_PARAMETER, false);
			if(contentVersionVO != null)
			{
				final ContentVersion contentVersion = ContentVersionController.getContentVersionController().getReadOnlyContentVersionWithId(contentVersionVO.getContentVersionId(), getDatabase()); 
				if(contentVersion != null)
				{
					attachments = contentVersion.getDigitalAssets();
				}
			}
		}
		catch(Exception e)
		{
			throwException(e);
		}
	}
	
	/**
	 * 
	 */
	private InternetAddress[] createAddresses(final String s) throws WorkflowException
	{
		final List addresses = new ArrayList();
		for(final StringTokenizer st = new StringTokenizer(s, ADDRESS_DELIMITER); st.hasMoreTokens(); )
		{
			final Address address = createAddress(st.nextToken());
			if(address != null) // illegal address?
			{
				addresses.add(address);
			}
		}
		return addressesToArray(addresses);
	}
	
	/**
	 * 
	 */
	private InternetAddress[] addressesToArray(final List list)
	{
		final InternetAddress[] addresses = new InternetAddress[list.size()];
		for(int i = 0; i < list.size(); ++i)
		{
			addresses[i] = (InternetAddress) list.get(i);
		}
		return addresses;
	}
	
	/**
	 * 
	 */
	private InternetAddress createAddress(final String email)
	{
		try 
		{
			return new InternetAddress(email);
		}
		catch(Exception e)
		{
			illegalAddresses.add(email);
		}
		return null;
	}
	
	/**
	 * 
	 */
	private void initializeMailService() throws WorkflowException
	{
		getLogger().debug("Initializing mail service.");
		try 
		{
			service = MailServiceFactory.getService();
		}
		catch(Exception e)
		{
			throwException(e);
		}
	}
	
	/**
	 * 
	 */
	private void sendMessage() throws WorkflowException
	{
		getLogger().debug("Sending message.");
		if(illegalAddresses.isEmpty())
		{
			try 
			{
				service.send(message);
				setFunctionStatus(STATUS_OK);
			}
			catch(SystemException e)
			{
				handleSendException(e);
			}
		}
	}

	/**
	 * 
	 */
	private void handleSendException(final SystemException e) throws WorkflowException
	{
		if(e.getCause() instanceof SendFailedException)
		{
			populateIllegalAddresses((SendFailedException) e.getCause());
		}
		else
		{
			throwException(e);
		}
	}
	
	/**
	 * 
	 */
	private void populateIllegalAddresses(final SendFailedException e)
	{
		final Address[] invalidAddresses = (e.getInvalidAddresses() == null) ? new Address[0] : e.getInvalidAddresses();
		for(int i=0; i<invalidAddresses.length; ++i)
		{
			illegalAddresses.add(invalidAddresses[i].toString());
		}
	}
	
	/**
	 * 
	 */
	private DataHandler getDataHandler(final String content, final String type)
	{
    	return new DataHandler(new StringDataSource(content, getContentType(type)));
	}

	/**
	 * 
	 */
	private DataHandler getDataHandler(final byte[] content, final String type)
	{
    	return new DataHandler(new ByteDataSource(content, getContentType(type)));
	}
	
	/**
	 * 
	 */
	private String getContentType(final String type)
	{
		return type + ";charset=" + UTF8_ENCODING;		
	}
	
	/**
	 * Method used for initializing the function; will be called before <code>execute</code> is called.
	 * <p><strong>Note</strong>! You must call <code>super.initialize()</code> first.</p>
	 * 
	 * @throws WorkflowException if an error occurs during the initialization.
	 */
	protected void initialize() throws WorkflowException 
	{
		super.initialize();
		this.illegalAddresses = (Collection) getParameter(EmailFunction.ILLEGAL_ADDRESSES_PARAMETER, new ArrayList());
		this.silentMode       = getArgument(SILENT_MODE_ARGUMENT, "false").equalsIgnoreCase("true");
	}
}
