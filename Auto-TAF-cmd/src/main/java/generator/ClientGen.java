package generator;

//1.login to esb
//2.upload a proxy to esb
//3.check whether it was deployed successfully
//4.send a payload to the proxy
//5.Get the response 
//6.Check the response is correct/incorrect

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wso2.carbon.proxyadmin.stub.types.carbon.ProxyData;

import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.Operation;
import com.predic8.wsdl.PortType;
import com.predic8.wsdl.WSDLParser;

public class ClientGen {
	static Logger log = Logger.getLogger(ClientGen.class.getName());
	static ArrayList<String> operations;

	public static void main(String args){
//		AuthenticationLibrary au = new AuthenticationLibrary();
//		au.LoginAs("admin", "admin", "admin");

//		ProxyServiceAdminLibrary li = new ProxyServiceAdminLibrary();
		
		String[] transport = { "http", "https" };
		ProxyData data = new ProxyData();
		data.setName("Cal112");
		data.setWsdlURI("http://localhost:8082/axis2/services/CalculatorService?wsdl");
		data.setTransports(transport);
		data.setStartOnLoad(true);
		String serviceEndPoint = "https://localhost:8243/services";
		data.setEndpointXML("<endpoint xmlns=\"http://ws.apache.org/ns/synapse\"><address uri=\""
				+ serviceEndPoint + "\" /></endpoint>");
		data.setEnableSecurity(true);
		
		try {
//			li.initProxyServiceAdmin();
//			String d=li.addProxy(data);
//			System.out.println(d);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
	}

	public static void main(String[] args) {
		ArrayList<String> libList = new ArrayList<String>();
		// lib.add("StatisticsAdminLibrary");
		// lib.add("UserAdminLibrary");
		// lib.add("ServiceAdminLibrary");
		// lib.add("DiscoveryAdminLibrary");
		// lib.add("AdminManagementServiceLibrary");
		// for (String nm : lib) {
		// generate(nm);
		// }
		File test = new File("src/test/resources/robotframework/tests");
		File[] txtFiles = test.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".txt");
			}
		});

		if (txtFiles == null || txtFiles.length == 0) {
			System.out.println("error: no Files");
			return;
		}

		System.out.println("info: files= " + txtFiles.length);
		BufferedReader read = null;
		for (File file : txtFiles) {
			try {
				read = new BufferedReader(new FileReader(file));
				String str;
				while ((str = read.readLine()) != null) {
					if (str.startsWith("Library")) {
						String lib = str.replace("Library ", "").trim();
						if (lib.startsWith("robotlib")) {
							String lib1 = lib.replace("robotlib.", "").trim();
							System.out.println(lib1);
							libList.add(lib1);
						}
					}
				}
			} catch (FileNotFoundException e) {
				System.out.println(e.getMessage());
			} catch (IOException e) {
				System.out.println(e.getMessage());
			} finally {
				if (read != null) {
					try {
						read.close();
					} catch (IOException e) {
						System.out.println(e.getMessage());
					}
				}
			}
		}

		// generate
		for (String nm : libList) {
			generate(nm);
			System.out.println("info: gen " + nm);
		}

	}

	public static void generate(String library) {
		Set<String> importLib = new HashSet<String>();
		STGroup group = new STGroupFile(
				"src/main/resources/template/templateR.stg");
		try {
			log.info("Hello this is an info message");
			// "org.wso2.carbon.service.mgt.stub.ServiceAdminStub";
			// String wsdl="/ServiceAdmin.wsdl";
			String[] res = getServiceInfor(library);
			if (res == null) {
				System.out.println("error: No Service named " + library);
				return;
			}

			// String servicestub
			// ="org.wso2.carbon.statistics.stub.StatisticsAdminStub";
			// String wsdl="/StatisticsAdmin.wsdl";
			String servicestub = res[0];
			String wsdl = "/" + res[1] + ".wsdl";

			Class<?> c = Class.forName(servicestub);

			System.out.println(c.getPackage().getName());
			importLib.add(servicestub);

			String methods = "";

			operations = getOperations(c, wsdl);

			for (Method m : c.getMethods()) {
				if (!isMethodNameValid(m.getName())) {
					continue;
				}

				ST methodTem = group.getInstanceOf("method");
				methodTem.add("returnType", m.getReturnType().getSimpleName());
				methodTem.add("methodName", m.getName());
				String retType = m.getReturnType().getSimpleName();
				retType = retType.replace("[]", "");
				if (isNameValid(retType)) {
					importLib.add(m.getReturnType().getCanonicalName()
							.replace("[]", ""));
				}
				String paras = "";
				String parasRet = "";
				int i = 0;
				for (Class<?> pc : m.getParameterTypes()) {
					String retType1 = pc.getSimpleName();
					// System.out.println(retType1);
					retType1 = retType1.replace("[]", "");
					if (isNameValid(retType1)) {
						importLib.add(pc.getCanonicalName().replace("[]", ""));
					}
					paras += pc.getSimpleName() + " " + "arg" + (i) + ",";
					parasRet += "arg" + (i++) + ",";
				}
				if (!paras.equals("")) {
					methodTem.add("paras",
							paras.substring(0, paras.length() - 2));
					methodTem.add("parasRet",
							parasRet.substring(0, parasRet.length() - 2));

				}

				methodTem.add("cond", !m.getReturnType().getSimpleName()
						.equals("void"));
				methods += methodTem.render();
			}

			ST classTem = group.getInstanceOf("class");

			String serviceName = getServiceName(c, wsdl);
			System.out.println("className " + serviceName);

			classTem.add("name", serviceName);
			classTem.add("namestub", serviceName + "Stub");
			classTem.add("methods", methods);

			String imports = "";
			for (String s : importLib) {
				imports += "import " + s + ";\n";
			}

			classTem.add("clsimport", imports);

			save(serviceName, classTem.render());
			System.out.println("info: generated " + serviceName);
		} catch (ClassNotFoundException e) {
			System.out.println("error " + e.getMessage());
		}
	}

	private static void save(String className, String result) {
		try {
			BufferedWriter wri = new BufferedWriter(new FileWriter(new File(
					"src/main/java/robotlib/" + className + "Library.java")));
			wri.write(result);
			wri.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	private static boolean isMethodNameValid(String name) {

		if (operations.contains(name)) {
			return true;
		} else {
			return false;
		}

	}

	private static boolean isNameValid(String name) {
		// System.out.println(name);
		String[] type = { "int", "boolean", "String", "Class", "Object",
				"void", "java.lang.String", "double", "long" };
		for (String s : type) {
			if (name.equals(s)) {
				return false;
			}
		}

		return true;
	}

	private static ArrayList<String> getOperations(Class<?> c, String wsdl) {

		ArrayList<String> li = new ArrayList<String>();
		try {
			InputStream input = c.getResourceAsStream(wsdl);
			WSDLParser parser = new WSDLParser();
			Definitions defs = parser.parse(input);
			for (PortType pt : defs.getPortTypes()) {
				for (Operation op : pt.getOperations()) {
					li.add(op.getName());
				}
			}
		} catch (Exception x) {
			System.out.println(x.getMessage());
		}

		return li;

	}

	private static String getServiceName(Class<?> c, String wsdl) {

		InputStream input = c.getResourceAsStream(wsdl);
		WSDLParser parser = new WSDLParser();
		Definitions defs = parser.parse(input);

		try {
			return defs.getServices().get(0).getName();
		} catch (Exception e) {
			return "newLib";
		}

	}

	public static String[] getServiceInfor(String lib) {
		File pomfile = new File("service.xml");
		String[] res;
		try {

			DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			Document doc = dBuilder.parse(pomfile);
			System.out.println("Root element :"
					+ doc.getDocumentElement().getNodeName());

			NodeList service = doc.getElementsByTagName("service");
			for (int i = 0; i < service.getLength(); i++) {
				Element ele = (Element) service.item(i);
				if (ele.getAttribute("lib").equals(lib)) {
					res = new String[2];
					res[0] = ele.getAttribute("stub");
					res[1] = ele.getAttribute("wsdl");
					return res;
				}

			}
			return null;

		} catch (Exception e) {
			System.out.println(e.getMessage());
			return null;
		}
	}

}
