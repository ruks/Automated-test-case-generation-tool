package lib;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.wso2.carbon.proxyadmin.stub.types.carbon.ProxyData;

import javax.xml.namespace.QName;

public class AxisServiceClient {
	private static final Log log = LogFactory.getLog(AxisServiceClient.class);

	public OMElement sendReceive(OMElement payload, String endPointReference,
			String operation) throws AxisFault {
		ServiceClient sender;
		Options options;
		OMElement response = null;
		if (log.isDebugEnabled()) {
			log.debug("Service Endpoint : " + endPointReference);
			log.debug("Service Operation : " + operation);
			log.debug("Payload : " + payload);
		}
		try {
			sender = new ServiceClient();
			options = new Options();
			options.setTo(new EndpointReference(endPointReference));
			options.setProperty(
					org.apache.axis2.transport.http.HTTPConstants.CHUNKED,
					Boolean.FALSE);
			options.setTimeOutInMilliSeconds(45000);
			options.setAction("urn:" + operation);
			sender.setOptions(options);
			response = sender.sendReceive(payload);
			if (log.isDebugEnabled()) {
				log.debug("Response Message : " + response);
			}
		} catch (AxisFault axisFault) {
			log.error(axisFault.getMessage());
			throw new AxisFault("AxisFault while getting response :"
					+ axisFault.getMessage(), axisFault);
		}
		Assert.assertNotNull(response);
		return response;
	}

	// axis2 client with header setting
	public OMElement sendReceive(OMElement payload, String endPointReference,
			String operation, String localName, String ns, String value)
			throws AxisFault {
		ServiceClient sender;
		Options options;
		OMElement response = null;
		if (log.isDebugEnabled()) {
			log.debug("Service Endpoint : " + endPointReference);
			log.debug("Service Operation : " + operation);
			log.debug("Payload : " + payload);
		}
		try {
			sender = new ServiceClient();
			sender.addStringHeader(new QName(ns, localName), value); // Set
																		// headers
			options = new Options();
			options.setTo(new EndpointReference(endPointReference));
			options.setProperty(
					org.apache.axis2.transport.http.HTTPConstants.CHUNKED,
					Boolean.FALSE);
			options.setAction("urn:" + operation);
			sender.setOptions(options);
			response = sender.sendReceive(payload);
			if (log.isDebugEnabled()) {
				log.debug("Response Message : " + response);
			}
		} catch (AxisFault axisFault) {
			log.error(axisFault.getMessage());
			throw new AxisFault("AxisFault while getting response :"
					+ axisFault.getMessage(), axisFault);
		}
		Assert.assertNotNull(response);
		return response;
	}

	public OMElement sendReceive(OMElement payload, String endPointReference,
			String operation, String contentType) throws AxisFault {
		ServiceClient sender;
		Options options;
		OMElement response = null;
		if (log.isDebugEnabled()) {
			log.debug("Service Endpoint : " + endPointReference);
			log.debug("Service Operation : " + operation);
			log.debug("Payload : " + payload);
		}
		try {
			sender = new ServiceClient();
			options = new Options();
			options.setTo(new EndpointReference(endPointReference));
			options.setProperty(
					org.apache.axis2.transport.http.HTTPConstants.CHUNKED,
					Boolean.FALSE);
			options.setTimeOutInMilliSeconds(45000);
			options.setAction("urn:" + operation);
			options.setProperty(Constants.Configuration.MESSAGE_TYPE,
					contentType);
			sender.setOptions(options);
			response = sender.sendReceive(payload);
			if (log.isDebugEnabled()) {
				log.debug("Response Message : " + response);
			}
		} catch (AxisFault axisFault) {
			log.error(axisFault.getMessage());
			throw new AxisFault("AxisFault while getting response :"
					+ axisFault.getMessage(), axisFault);
		}
		Assert.assertNotNull(response);
		return response;
	}

	// one way communication
	public void sendRobust(OMElement payload, String endPointReference,
			String operation) throws AxisFault {
		ServiceClient sender;
		Options options;
		if (log.isDebugEnabled()) {
			log.info("Service Endpoint : " + endPointReference);
			log.info("Service Operation : " + operation);
			log.debug("Payload : " + payload);
		}
		try {
			sender = new ServiceClient();
			options = new Options();
			options.setTo(new EndpointReference(endPointReference));
			options.setProperty(
					org.apache.axis2.transport.http.HTTPConstants.CHUNKED,
					Boolean.FALSE);
			options.setAction("urn:" + operation);
			sender.setOptions(options);
			sender.sendRobust(payload);
		} catch (AxisFault axisFault) {
			log.error(axisFault.getMessage());
			throw new AxisFault("AxisFault while getting response :"
					+ axisFault.getMessage(), axisFault);
		}
	}

	// one way communication
	public void fireAndForget(OMElement payload, String endPointReference,
			String operation) throws AxisFault {
		ServiceClient sender;
		Options options;
		if (log.isDebugEnabled()) {
			log.info("Service Endpoint : " + endPointReference);
			log.info("Service Operation : " + operation);
			log.debug("Payload : " + payload);
		}
		try {
			sender = new ServiceClient();
			options = new Options();
			options.setTo(new EndpointReference(endPointReference));
			options.setProperty(
					org.apache.axis2.transport.http.HTTPConstants.CHUNKED,
					Boolean.FALSE);
			options.setAction("urn:" + operation);
			sender.setOptions(options);
			sender.fireAndForget(payload);
		} catch (AxisFault axisFault) {
			log.error(axisFault.getMessage());
			throw new AxisFault("AxisFault while getting response :"
					+ axisFault.getMessage(), axisFault);
		}
	}

	// create OMElement for method
	public OMElement getMethod(String method, String namespace, String... paras) {
		int l = paras.length;
		if (l % 2 != 0) {
			System.out.println("para mismatched");
			return null;
		}

		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace omNs = fac.createOMNamespace(namespace, "tns");
		OMElement meth = fac.createOMElement("method", omNs);

		l /= 2;
		for (int i = 0; i < l; i++) {
			OMElement value = fac.createOMElement(paras[i], omNs);
			value.addChild(fac.createOMText(value, paras[i + 1]));
			meth.addChild(value);
		}

		return meth;
	}

	public String getValue(OMElement om) {
		return om.getFirstElement().getText();
	}

	public String invokeOperation1(String operationName, String paraName,
			String paraVal, String endPOint, String namespace) {
		invokeOperation = null;
		OMElement method = getMethod(operationName, paraName, paraVal,
				namespace);
		OMElement res = null;
		try {
			res = sendReceive(method, endPOint, operationName);
			String val = getValue(res);
			invokeOperation = val;
			return val;
		} catch (AxisFault e) {
			System.out.println(e.getMessage());
			return null;
		}

	}

	public String invokeOperation2(String operationName, String endPOint,
			String namespace) {
		OMElement method = getMethod(operationName, null, null, namespace);
		OMElement res = null;
		try {
			res = sendReceive(method, endPOint, operationName);
			String val = getValue(res);
			invokeOperation = val;
			return val;
		} catch (AxisFault e) {
			System.out.println(e.getFaultAction());
			return null;
		}

	}

	public String invokeOperation(String operationName, String endPOint,
			String namespace, String... paras) {
		OMElement method = getMethod(operationName,namespace,paras);
		OMElement res = null;
		try {
			res = sendReceive(method, endPOint, operationName);
			String val = getValue(res);
			invokeOperation = val;
			return val;
		} catch (AxisFault e) {
			System.out.println(e.getFaultAction());
			return null;
		}

	}

	private String invokeOperation = null;

	public void AssertInvokeOperation(String expected) {
		Assert.assertEquals(invokeOperation, expected);
	}
}
