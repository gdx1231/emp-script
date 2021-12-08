package com.gdxsoft.easyweb.conf;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.utils.UPath;

/**
 * 文档转换服务的OpenOffice /LibreOffice配置
 * &lt;sOffice sofficePath="c:/Program Files/LibreOffice/" ports="8100,8101,8102" /&gt;
 *
 */
public class ConfSOffice {
	private static Logger LOGGER = LoggerFactory.getLogger(ConfSecurities.class);
	private static ConfSOffice INST = null;
	private static long PROP_TIME = 0;


	/**
	 * Return the instance of define
	 * 
	 * @return
	 */
	public static ConfSOffice getInstance() {
		if (INST != null) {
			if (UPath.getPropTime() == PROP_TIME) {
				return INST;
			}
		}
		initDefine();
		return INST;
	}

	private synchronized static void initDefine() {
		// <define value="true" />
		if (UPath.getCfgXmlDoc() == null) {
			return;
		}
		// the last modify time of the ewa_conf.xml
		PROP_TIME = UPath.getPropTime();
		INST = new ConfSOffice();

		NodeList nl = UPath.getCfgXmlDoc().getElementsByTagName("sOffice");
		if (nl.getLength() == 0) {
			// 老的方法
			INST.sofficePath =  UPath.getCVT_OPENOFFICE_HOME();
			return;
		}
		Element item = (Element) nl.item(0);

		// 文档转换服务的OpenOffice /LibreOffice安装路径
		INST.sofficePath = item.getAttribute("sofficePath");
		setPorts(item);

	}

	private static void setPorts(Element item) {
		if (!item.hasAttribute("ports")) {
			return;
		}
		String list[] = item.getAttribute("ports").split(",");
		ArrayList<Integer> tempPorts = new ArrayList<>();
		for (int i = 0; i < list.length; i++) {
			String tempPort = list[i].trim();
			if (tempPort.length() == 0) {
				continue;
			}
			try {
				int port = Integer.parseInt(tempPort);

				if (port <= 3000) {
					LOGGER.warn("The soffice port must >=3000, the set value is {}", tempPort);
					continue;
				} else if (port > 65535) {
					LOGGER.warn("The soffice port must <=65535, the set value is {}", tempPort);
					continue;

				}
				tempPorts.add(port);

			} catch (Exception err) {
				LOGGER.warn("Invalid soffice port {} {}", tempPort, err.getLocalizedMessage());
			}
		}
		if (tempPorts.size() == 0) {
			return;
		}
		INST.ports = new int[tempPorts.size()];
		for (int i = 0; i < tempPorts.size(); i++) {
			INST.ports[i] = tempPorts.get(i).intValue();
		}
	}

	//文档转换服务的OpenOffice /LibreOffice配置
	private String sofficePath;

	// 启动服务的端口，默认8100，多个端口意味者多个服务
	private int[] ports = { 8100 };

	/**
	 * 文档转换服务的OpenOffice /LibreOffice安装路径
	 * @return
	 */
	public String getSofficePath() {
		return sofficePath;
	}

	/**
	 * 启动服务的端口，默认8100，多个端口意味者多个服务
	 * @return
	 */
	public int[] getPorts() {
		return ports;
	}

}
