package com.anwarelmakrahy.pwncore;

import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NmapXmlParser {

	public NmapXmlParser(InputStream in) {
		parseXml(in);
	}

	private void parseXml(InputStream in) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(in);
			doc.getDocumentElement().normalize();

			NodeList nodeLst = doc.getElementsByTagName("nmaprun");

			for (int s = 0; s < nodeLst.getLength(); s++) {

				Node fstNode = nodeLst.item(s);

				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {

					Element fstElmnt = (Element) fstNode;
					NodeList hostElementList = fstElmnt
							.getElementsByTagName("host");

					for (int i = 0; i < hostElementList.getLength(); i++) {
						mHostItems.add(new HostItem());
						getHost((Element) hostElementList.item(i),
								mHostItems.size() - 1);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void getHost(Element e, int index) {
		if (e.hasChildNodes()) {
			NodeList hostNodeList = e.getChildNodes();
			for (int i = 0; i < hostNodeList.getLength(); i++) {

				if (hostNodeList.item(i).getNodeName().equals("status")) {
					getHostStatus(hostNodeList.item(i), index);
				} else if (hostNodeList.item(i).getNodeName().equals("address")) {
					getHostAddress(hostNodeList.item(i), index);
				} else if (hostNodeList.item(i).getNodeName()
						.equals("hostnames")) {
					if (hostNodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {

						Element hostnameElement = (Element) hostNodeList
								.item(i);
						NodeList hostnameElementList = hostnameElement
								.getElementsByTagName("hostname");

						for (int j = 0; j < hostnameElementList.getLength(); j++) {
							getHostHostnames(
									(Element) hostnameElementList.item(j),
									index);
						}
					}
				} else if (hostNodeList.item(i).getNodeName().equals("ports")) {
					if (hostNodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {

						Element portElement = (Element) hostNodeList.item(i);
						NodeList portElementList = portElement
								.getElementsByTagName("port");

						for (int j = 0; j < portElementList.getLength(); j++) {
							getHostPorts((Element) portElementList.item(j),
									index);
						}
					}
				} else if (hostNodeList.item(i).getNodeName().equals("os")) {

				} else if (hostNodeList.item(i).getNodeName().equals("uptime")) {

				} else if (hostNodeList.item(i).getNodeName()
						.equals("distance")) {

				} else if (hostNodeList.item(i).getNodeName()
						.equals("tcpsequence")) {

				} else if (hostNodeList.item(i).getNodeName()
						.equals("ipidsequence")) {

				} else if (hostNodeList.item(i).getNodeName()
						.equals("tcptssequence")) {

				} else if (hostNodeList.item(i).getNodeName().equals("trace")) {

				} else if (hostNodeList.item(i).getNodeName().equals("times")) {

				}
			}
		}
	}

	private void getHostPorts(Node n, int index) {
		HostItem.HostPort tmp = new HostItem.HostPort();
		tmp.Protocol = n.getAttributes().getNamedItem("protocol")
				.getNodeValue();
		tmp.PortID = n.getAttributes().getNamedItem("portid").getNodeValue();
		mHostItems.get(index).mPorts.add(tmp);
	}

	private void getHostHostnames(Node n, int index) {
		HostItem.HostHostname tmp = new HostItem.HostHostname();
		tmp.Name = n.getAttributes().getNamedItem("name").getNodeValue();
		tmp.Type = n.getAttributes().getNamedItem("type").getNodeValue();
		mHostItems.get(index).mHostnames.add(tmp);
	}

	private void getHostAddress(Node n, int index) {
		HostItem.HostAddress tmp = new HostItem.HostAddress();
		tmp.Address = n.getAttributes().getNamedItem("addr").getNodeValue();
		tmp.AddressType = n.getAttributes().getNamedItem("addrtype")
				.getNodeValue();
		mHostItems.get(index).mAddresses.add(tmp);
	}

	private void getHostStatus(Node n, int index) {
		HostItem tmp = mHostItems.get(index);
		tmp.State = n.getAttributes().getNamedItem("state").getNodeValue();
		tmp.StateReason = n.getAttributes().getNamedItem("reason")
				.getNodeValue();
	}

	private ArrayList<HostItem> mHostItems = new ArrayList<HostItem>();

	public ArrayList<HostItem> getHostItems() {
		return mHostItems;
	}

	static class HostItem {
		static class HostPort {
			String Protocol;
			String PortID;
		}

		static class HostHostname {
			String Name;
			String Type;
		}

		static class HostAddress {
			String Address;
			String AddressType;
		}

		String State;
		String StateReason;

		ArrayList<HostPort> mPorts = new ArrayList<HostPort>();
		ArrayList<HostHostname> mHostnames = new ArrayList<HostHostname>();
		ArrayList<HostAddress> mAddresses = new ArrayList<HostAddress>();
	}
}
