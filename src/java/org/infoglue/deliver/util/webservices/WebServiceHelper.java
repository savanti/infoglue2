package org.infoglue.deliver.util.webservices;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

/**
 * This class helps in requesting information from an webservice.
 * @author Mattias Bogeblad
 */

public class WebServiceHelper
{
	private String serviceUrl = "";

	private boolean isSuccessfull;
	private String message;

	/**
	 * The constructor for this class.
	 */

	public WebServiceHelper()
	{
	}

	/**
	 * A method to set the serviceUrl which is the endpoint of this call.
	 */

	public String getServiceUrl()
	{
		return serviceUrl;
	}

	/**
	 * A method to get the serviceUrl which is the endpoint of this call.
	 */

	public void setServiceUrl(String serviceUrl)
	{
		this.serviceUrl = serviceUrl;
	}

	/**
	 * This is the method that lets you call the endpoint and get a single string-value back.
	 */

	public String getString(String method)
	{
		String response = "";

		try
		{
			Service service = new Service();
			Call call = (Call)service.createCall();

			String endpoint = this.serviceUrl;

			call.setTargetEndpointAddress(endpoint); //Set the target service host and service location,
			call.setOperationName(new QName("http://soapinterop.org/", method)); //This is the target services method to invoke.
			call.setEncodingStyle( "http://schemas.xmlsoap.org/soap/encoding/" );

			QName qnameAttachment = new QName("urn:EchoAttachmentsService", "DataHandler");

			call.setReturnType(qnameAttachment);

			response = (String)call.invoke(new Object[0]); //Add the attachment.
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * This is the method that lets you call the endpoint and get a list of values back.
	 */

	public Collection getCollection(String method)
	{
		Collection response = null;

		try
		{
			Service service = new Service();
			Call call = (Call)service.createCall();

			String endpoint = this.serviceUrl;

			call.setTargetEndpointAddress(endpoint); //Set the target service host and service location,
			call.setOperationName(new QName("http://soapinterop.org/", method)); //This is the target services method to invoke.
			call.setEncodingStyle( "http://schemas.xmlsoap.org/soap/encoding/" );

			QName qnameAttachment = new QName("urn:EchoAttachmentsService", "DataHandler");

			call.setReturnType(qnameAttachment);

			response = (Collection)call.invoke(new Object[0]); //Add the attachment.
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * This is the method that lets you call the endpoint and get a map of values back.
	 */

	public Map getMap(String method)
	{
		Map response = null;

		try
		{
			Service service = new Service();
			Call call = (Call)service.createCall();

			String endpoint = this.serviceUrl;

			call.setTargetEndpointAddress(endpoint); //Set the target service host and service location,
			call.setOperationName(new QName("http://soapinterop.org/", method)); //This is the target services method to invoke.
			call.setEncodingStyle( "http://schemas.xmlsoap.org/soap/encoding/" );

			QName qnameAttachment = new QName("urn:EchoAttachmentsService", "DataHandler");

			call.setReturnType(qnameAttachment);

			response = (Map)call.invoke(new Object[0]); //Add the attachment.
		}
		catch (Exception e)
		{
			response.put("errorStatus", new Integer(1));
			response.put("errorMessage", e.getMessage());
			e.printStackTrace();
		}
		return response;
	}


	/**
	 * This is the method that lets you call the endpoint and get a map of values back.
	 */

	public Map getMap(String method, Map argument)
	{
		Map response = new HashMap();

		try
		{
			Service service = new Service();
			Call call = (Call)service.createCall();

			String endpoint = this.serviceUrl;

			call.setTargetEndpointAddress(endpoint); //Set the target service host and service location,
			call.setOperationName(new QName("http://soapinterop.org/", method)); //This is the target services method to invoke.
			call.setEncodingStyle( "http://schemas.xmlsoap.org/soap/encoding/" );

			QName qnameAttachment = new QName("urn:EchoAttachmentsService", "DataHandler");

			call.addParameter("data", XMLType.XSD_STRING, ParameterMode.IN);

			call.setReturnType(qnameAttachment);
			Object[] args = {argument};
			response = (Map)call.invoke(args); //Add the attachment.
		}
		catch (Exception e)
		{
			response.put("errorStatus", new Integer(1));
			response.put("errorMessage", e.getMessage());
			e.printStackTrace();
		}

		return response;
	}


	/**
	 * A helper method that lets the template get hold of a Map-object to populate.
	 */

	public Map getMap()
	{
		return new HashMap();
	}


	/**
	 * This method returns true if the request to the webservice returned successfully.
	 */

	public boolean getIsSuccessfull()
	{
		return this.isSuccessfull;
	}

	/**
	 * This method sets if the request to the webservice returned successfully.
	 */

	public void setIsSuccessfull(boolean isSuccessfull)
	{
		this.isSuccessfull = isSuccessfull;
	}

	/**
	 * This method returns any message coming from the webservice.
	 */

	public String getMessage()
	{
		return this.message;
	}

	/**
	 * This method sets a message from the webservice.
	 */

	public void setMessage(String message)
	{
		this.message = message;
	}
}