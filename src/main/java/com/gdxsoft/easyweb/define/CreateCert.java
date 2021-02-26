package com.gdxsoft.easyweb.define;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.gdxsoft.easyweb.utils.UAes;
import com.gdxsoft.easyweb.utils.UConvert;
import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;
import com.gdxsoft.easyweb.utils.Utils;

public class CreateCert {
	public static String encodeOthers(String fromPath,String newPath) throws Exception {
		UAes aes = UAes.getInstance();
		String path = UPath.getRealPath()+"/"+fromPath;
		File fPath = new File(path);
		File fNewPath = new File(newPath);

		encodeXmls(aes, fPath.getAbsolutePath(), fPath.getAbsolutePath(),
				fNewPath.getAbsolutePath());

		return "";
	}
	public static String encodeXmls(String newPath) throws Exception {
		UAes aes = UAes.getInstance();
		String path = UPath.getScriptPath();
		File fPath = new File(path);
		File fNewPath = new File(newPath);

		encodeXmls(aes, fPath.getAbsolutePath(), fPath.getAbsolutePath(),
				fNewPath.getAbsolutePath());

		return "";
	}

	private static void encodeXmls(UAes aes, String searchPath,
			String pathRoot, String newPathRoot) throws Exception {
		File f = new File(searchPath);

		File[] ff = f.listFiles();
		String addPath = searchPath.replace(pathRoot, "");

		String pathSave = newPathRoot + "/" + addPath;
		for (int i = 0; i < ff.length; i++) {
			File f1 = ff[i];
			if (f1.isDirectory()) {
				continue;
			}
			if (!f1.getName().toUpperCase().endsWith(".XML")) {
				continue;
			}
			System.out.println("开始" + f1.getAbsolutePath());

			String xml = UFile.readFileText(f1.getAbsolutePath());
			byte[] buf = aes.getEncBytes(xml);

			String path = pathSave + "/" + f1.getName() + ".bin";

			System.out.println(" --->" + path);
			UFile.createBinaryFile(path, buf, true);
		}
		for (int i = 0; i < ff.length; i++) {
			File f1 = ff[i];
			if (f1.isDirectory()) {
				encodeXmls(aes, f1.getAbsolutePath(), pathRoot, newPathRoot);
			}
		}
	}

	public static String create(String unid, String own, String web,
			String start, String end, String nation) {
		byte[] buf = new byte[256];
		int inc = 0;
		while (inc < buf.length) {
			String uuid = Utils.getGuid().replace("-", "");
			byte[] buf1 = Utils.hex2bytes(uuid);
			inc += buf1.length;

			int length = buf.length - inc > buf1.length ? buf1.length
					: buf.length - inc;
			System.arraycopy(buf1, 0, buf, inc, length);
		}

		String key = UConvert.ToBase64String(buf);

		Document doc = UXml.createBlankDocument();
		Element n1 = doc.createElement("cert");
		doc.appendChild(n1);

		createSub(n1, "own", own);
		createSub(n1, "web", web);
		createSub(n1, "begin", start);
		createSub(n1, "end", end);
		createSub(n1, "nation", nation);

		createSub(n1, "key", key);

		String path0 = UPath.getCachedPath() + "/certs";
		UFile.buildPaths(path0);

		String path = path0 + "/" + unid + ".xml";
		UXml.saveDocument(doc, path);

		return path;

	}

	private static void createSub(Element cert, String tag, String val) {
		Node node = (Node) cert;
		Element e1 = node.getOwnerDocument().createElement(tag);
		e1.setTextContent(val);

		node.appendChild(e1);
	}

}
