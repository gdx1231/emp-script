import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;
import org.w3c.dom.ls.*;

/**
 * EWA XML 安全修改工具 (纯 Java, JDK 17, 无外部依赖)
 *
 * 编译: javac EwaXmlMod.java
 * 用法: java EwaXmlMod <command> [args]
 */
public class EwaXmlMod {

    static Document doc;
    static final DocumentBuilder builder;

    static {
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ─── load ──────────────────────────────────

    static void load(String path) throws Exception {
        byte[] raw;
        if ("-".equals(path)) {
            raw = System.in.readAllBytes();
        } else {
            raw = Files.readAllBytes(Path.of(path));
        }
        String text = new String(raw, StandardCharsets.UTF_8);

        // 跳过 ANSI 行，定位 JSON
        int jsonStart = text.indexOf('{');
        if (jsonStart >= 0 && text.charAt(jsonStart) == '{') {
            text = text.substring(jsonStart);
            text = text.replace("\\/", "/");
            doc = builder.parse(new org.xml.sax.InputSource(new StringReader(extractXmlFromJson(text))));
        } else {
            // 直接 XML
            int xmlStart = text.indexOf('<');
            text = text.substring(xmlStart);
            doc = builder.parse(new org.xml.sax.InputSource(new StringReader(extractInnerTemplate(text))));
        }
    }

    static String extractXmlFromJson(String json) {
        // 找到 "XML": 字段并提取完整 JSON 字符串值
        String key = "\"XML\": \"";
        int s = json.indexOf(key);
        if (s < 0) throw new RuntimeException("JSON response has no XML field");
        int start = s + key.length();
        // 追踪转义，找到未转义的 "
        StringBuilder xml = new StringBuilder();
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '\\' && i + 1 < json.length()) {
                char next = json.charAt(i + 1);
                if (next == '"') { xml.append('"'); i++; continue; }
                if (next == '\\') { xml.append('\\'); i++; continue; }
                if (next == '/')  { xml.append('/');  i++; continue; }
            }
            if (c == '"') break;
            xml.append(c);
        }
        return extractInnerTemplate(xml.toString());
    }

    static String extractInnerTemplate(String xml) {
        if (xml.contains("<EasyWebTemplates")) {
            int s = xml.indexOf("<EasyWebTemplate ");
            int e = xml.lastIndexOf("</EasyWebTemplate>") + "</EasyWebTemplate>".length();
            return xml.substring(s, e);
        }
        return xml;
    }

    // ─── save ──────────────────────────────────

    static void save() throws Exception {
        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        // 使用 LSSerializer 保留 CDATA
        DOMImplementationLS ls = (DOMImplementationLS)
            DocumentBuilderFactory.newInstance().newDocumentBuilder().getDOMImplementation();
        LSSerializer ser = ls.createLSSerializer();
        ser.getDomConfig().setParameter("xml-declaration", false);
        ser.getDomConfig().setParameter("cdata-sections", true);
        System.out.println(ser.writeToString(doc));
    }

    // ─── helpers ───────────────────────────────

    static Element findXItem(String name) {
        NodeList items = doc.getElementsByTagName("XItem");
        for (int i = 0; i < items.getLength(); i++) {
            Element el = (Element) items.item(i);
            String attr = el.getAttribute("Name");
            if (name.equals(attr)) return el;

            Element ns = getChild(el, "Name");
            if (ns != null) {
                Element set = getChild(ns, "Set");
                if (set != null && name.equals(set.getAttribute("Name")))
                    return el;
            }
        }
        return null;
    }

    static Element getChild(Element parent, String tag) {
        NodeList nl = parent.getElementsByTagName(tag);
        return nl.getLength() > 0 ? (Element) nl.item(0) : null;
    }

    static Element ensureChild(Element parent, String tag) {
        Element e = getChild(parent, tag);
        if (e == null) {
            e = doc.createElement(tag);
            parent.appendChild(e);
        }
        return e;
    }

    static void setCDATA(Element el, String content) {
        // 移除旧的子节点
        while (el.hasChildNodes()) el.removeChild(el.getFirstChild());
        el.appendChild(doc.createCDATASection(content));
    }

    static void setText(Element el, String content) {
        while (el.hasChildNodes()) el.removeChild(el.getFirstChild());
        el.appendChild(doc.createTextNode(content));
    }

    // ─── commands ──────────────────────────────

    static void cmdRename(String oldName, String newName) throws Exception {
        Element el = findXItem(oldName);
        if (el == null) { System.err.println("ERROR: XItem '" + oldName + "' not found"); System.exit(1); }
        el.setAttribute("Name", newName);
        Element ns = getChild(el, "Name");
        if (ns != null) { Element s = getChild(ns, "Set"); if (s != null) s.setAttribute("Name", newName); }
        Element di = getChild(el, "DataItem");
        if (di != null) { Element s = getChild(di, "Set"); if (s != null) s.setAttribute("DataField", newName); }
        save();
    }

    static void cmdSetTag(String name, String tag) throws Exception {
        Element el = findXItem(name);
        if (el == null) { System.err.println("ERROR: XItem '" + name + "' not found"); System.exit(1); }
        Element t = getChild(el, "Tag");
        if (t != null) { Element s = getChild(t, "Set"); if (s != null) s.setAttribute("Tag", tag); }
        save();
    }

    static void cmdSetEvent(String name, String eventName, String eventValue) throws Exception {
        Element el = findXItem(name);
        if (el == null) { System.err.println("ERROR: XItem '" + name + "' not found"); System.exit(1); }
        Element ev = getChild(el, "EventSet");
        if (ev != null) {
            Element s = getChild(ev, "Set");
            if (s != null) {
                s.setAttribute("EventName", eventName);
                s.setAttribute("EventType", "Javascript");
                s.setAttribute("EventValue", eventValue);
            }
        }
        save();
    }

    static void cmdSetAnchor(String name, String href, String target) throws Exception {
        Element el = findXItem(name);
        if (el == null) { System.err.println("ERROR: XItem '" + name + "' not found"); System.exit(1); }
        Element ap = ensureChild(el, "AnchorParas");
        Element s = ensureChild(ap, "Set");
        s.setAttribute("aHref", href);
        s.setAttribute("aTarget", target != null ? target : "");
        save();
    }

    static void cmdSetDesc(String xitemName, String zhcn, String enus) throws Exception {
        Element desc = null;
        if (xitemName != null) {
            Element el = findXItem(xitemName);
            if (el == null) { System.err.println("ERROR: XItem '" + xitemName + "' not found"); System.exit(1); }
            desc = getChild(el, "DescriptionSet");
        } else {
            NodeList pages = doc.getElementsByTagName("Page");
            if (pages.getLength() > 0) {
                desc = getChild((Element) pages.item(0), "DescriptionSet");
            }
        }
        if (desc == null) { System.err.println("ERROR: DescriptionSet not found"); System.exit(1); }
        NodeList sets = desc.getElementsByTagName("Set");
        for (int i = 0; i < sets.getLength(); i++) {
            Element s = (Element) sets.item(i);
            String lang = s.getAttribute("Lang");
            if ("zhcn".equals(lang)) s.setAttribute("Info", zhcn);
            else if ("enus".equals(lang)) s.setAttribute("Info", enus);
        }
        save();
    }

    static void cmdSetScript(String position, String content) throws Exception {
        NodeList pages = doc.getElementsByTagName("Page");
        if (pages.getLength() == 0) { System.err.println("ERROR: Page not found"); System.exit(1); }
        Element script = getChild((Element) pages.item(0), "AddScript");
        if (script == null) { System.err.println("ERROR: AddScript not found"); System.exit(1); }
        Element set = getChild(script, "Set");
        if (set == null) { set = doc.createElement("Set"); script.appendChild(set); }
        // position = Top or Bottom
        Element pos = getChild(set, position);
        if (pos == null) { pos = doc.createElement(position); set.appendChild(pos); }
        setCDATA(pos, content);
        save();
    }

    static void cmdSetCss(String content) throws Exception {
        NodeList pages = doc.getElementsByTagName("Page");
        if (pages.getLength() == 0) { System.err.println("ERROR: Page not found"); System.exit(1); }
        Element css = getChild((Element) pages.item(0), "AddCss");
        if (css != null) {
            Element set = getChild(css, "Set");
            if (set != null) {
                Element ac = getChild(set, "AddCss");
                if (ac != null) { setCDATA(ac, content); }
            }
        }
        save();
    }

    static void cmdSetFrameHtml(String content) throws Exception {
        NodeList pages = doc.getElementsByTagName("Page");
        if (pages.getLength() == 0) { System.err.println("ERROR: Page not found"); System.exit(1); }
        Element fh = getChild((Element) pages.item(0), "FrameHtml");
        if (fh != null) {
            Element set = getChild(fh, "Set");
            if (set != null) {
                Element fhc = getChild(set, "FrameHtml");
                if (fhc != null) { setCDATA(fhc, content); }
            }
        }
        save();
    }

    static void cmdSetSql(String sqlName, String content) throws Exception {
        NodeList sets = doc.getElementsByTagName("Set");
        for (int i = 0; i < sets.getLength(); i++) {
            Element s = (Element) sets.item(i);
            if (sqlName.equals(s.getAttribute("Name")) && s.getAttribute("SqlType") != null) {
                Element sql = getChild(s, "Sql");
                if (sql == null) { sql = doc.createElement("Sql"); s.appendChild(sql); }
                setCDATA(sql, content);
                save();
                return;
            }
        }
        // 不存在则创建
        cmdNewSql(sqlName, "query", content);
    }

    static void cmdNewActionSql(String name, String sqlType, String content) throws Exception {
        String sqlName = name + " SQL";

        // 1. SqlSet
        NodeList sqlSets = doc.getElementsByTagName("SqlSet");
        Element sqlSet;
        if (sqlSets.getLength() > 0) {
            sqlSet = (Element) sqlSets.item(0);
        } else {
            sqlSet = doc.createElement("SqlSet");
            NodeList actions = doc.getElementsByTagName("Action");
            if (actions.getLength() == 0) throw new RuntimeException("Action not found");
            actions.item(0).getParentNode().insertBefore(sqlSet, actions.item(0));
        }
        Element ss = doc.createElement("Set");
        ss.setAttribute("Name", sqlName);
        ss.setAttribute("SqlType", sqlType);
        Element sql = doc.createElement("Sql");
        setCDATA(sql, content);
        ss.appendChild(sql);
        Element cs = doc.createElement("CSSet");
        ss.appendChild(cs);
        sqlSet.appendChild(ss);

        // 2. ActionSet
        NodeList actionSets = doc.getElementsByTagName("ActionSet");
        if (actionSets.getLength() == 0) throw new RuntimeException("ActionSet not found");
        Element as = (Element) actionSets.item(0);
        Element ae = doc.createElement("Set");
        ae.setAttribute("LogMsg", "");
        ae.setAttribute("Transcation", "");
        ae.setAttribute("Type", name);
        Element callSet = doc.createElement("CallSet");
        Element call = doc.createElement("Set");
        call.setAttribute("CallIsChk", "");
        call.setAttribute("CallName", sqlName);
        call.setAttribute("CallType", "SqlSet");
        call.setAttribute("Test", "");
        callSet.appendChild(call);
        ae.appendChild(callSet);
        as.appendChild(ae);

        save();
    }

    // 保留旧方法，兼容 set-sql
    static void cmdNewSql(String sqlName, String sqlType, String content) throws Exception {
        // 找到或创建 SqlSet
        NodeList sqlSets = doc.getElementsByTagName("SqlSet");
        Element sqlSet;
        if (sqlSets.getLength() > 0) {
            sqlSet = (Element) sqlSets.item(0);
        } else {
            // 在 Action 之后创建
            NodeList actions = doc.getElementsByTagName("Action");
            if (actions.getLength() > 0) {
                sqlSet = doc.createElement("SqlSet");
                actions.item(0).getParentNode().insertBefore(sqlSet, actions.item(0).getNextSibling());
            } else {
                throw new RuntimeException("Cannot find Action or SqlSet to insert into");
            }
        }
        Element set = doc.createElement("Set");
        set.setAttribute("Name", sqlName);
        set.setAttribute("SqlType", sqlType);
        Element sql = doc.createElement("Sql");
        setCDATA(sql, content);
        set.appendChild(sql);
        Element cs = doc.createElement("CSSet");
        set.appendChild(cs);
        sqlSet.appendChild(set);
        save();
    }

    // ─── main ──────────────────────────────────

    static void usage() {
        System.out.println("""
            EWA XML 安全修改工具 (纯 Java)

            用法: java EwaXmlMod <command> [args] [-f FILE]

            命令:
              rename <old> <new>             重命名 XItem
              set-tag <name> <tag>           设置 Tag 类型
              set-event <name> <ev-name> <ev-value>  设置 JS 事件
              set-anchor <name> <href> [target]    设置锚点
              set-desc [--name XITEM] --zhcn <text> --enus <text>  设置描述
              set-script Top|Bottom [content]      修改 AddScript
              set-css [content]                    修改 AddCss
              set-frame-html [content]             修改 FrameHtml
              set-sql <sql-name> [content]         修改 SqlSet
              new-action-sql <name> [type] [sql]   新建 SqlSet action (type=query|update, 默认query)

            选项:
              -f FILE   从文件读取 (默认 stdin)
              content 为 "-" 或省略时从 stdin 读取
            """);
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) { usage(); return; }

        String cmd = args[0];
        String file = "-";
        String[] rest = new String[args.length - 1];
        int ri = 0;
        for (int i = 1; i < args.length; i++) {
            if ("-f".equals(args[i]) && i + 1 < args.length) {
                file = args[++i];
            } else {
                rest[ri++] = args[i];
            }
        }
        String[] ra = java.util.Arrays.copyOf(rest, ri);

        load(file);

        switch (cmd) {
            case "rename" -> {
                if (ra.length < 2) { System.err.println("usage: rename <old> <new>"); return; }
                cmdRename(ra[0], ra[1]);
            }
            case "set-tag" -> {
                if (ra.length < 2) { System.err.println("usage: set-tag <name> <tag>"); return; }
                cmdSetTag(ra[0], ra[1]);
            }
            case "set-event" -> {
                if (ra.length < 3) { System.err.println("usage: set-event <name> <ev> <val>"); return; }
                cmdSetEvent(ra[0], ra[1], ra[2]);
            }
            case "set-anchor" -> {
                if (ra.length < 2) { System.err.println("usage: set-anchor <name> <href> [target]"); return; }
                cmdSetAnchor(ra[0], ra[1], ra.length > 2 ? ra[2] : "");
            }
            case "set-desc" -> {
                String name = null, zhcn = null, enus = null;
                for (int i = 0; i < ra.length; i++) {
                    switch (ra[i]) {
                        case "--name" -> name = ra[++i];
                        case "--zhcn" -> zhcn = ra[++i];
                        case "--enus" -> enus = ra[++i];
                    }
                }
                if (zhcn == null || enus == null) { System.err.println("usage: set-desc --zhcn <text> --enus <text> [--name XITEM]"); return; }
                cmdSetDesc(name, zhcn, enus);
            }
            case "set-script" -> {
                if (ra.length < 1) { System.err.println("usage: set-script Top|Bottom [content]"); return; }
                String content = ra.length > 1 ? ra[1] : new String(System.in.readAllBytes(), StandardCharsets.UTF_8);
                cmdSetScript(ra[0], content);
            }
            case "set-css" -> {
                String c = ra.length > 0 ? ra[0] : new String(System.in.readAllBytes(), StandardCharsets.UTF_8);
                cmdSetCss(c);
            }
            case "set-frame-html" -> {
                String c = ra.length > 0 ? ra[0] : new String(System.in.readAllBytes(), StandardCharsets.UTF_8);
                cmdSetFrameHtml(c);
            }
            case "set-sql" -> {
                if (ra.length < 1) { System.err.println("usage: set-sql <sql-name> [content]"); return; }
                String c = ra.length > 1 ? ra[1] : new String(System.in.readAllBytes(), StandardCharsets.UTF_8);
                cmdSetSql(ra[0], c);
            }
            case "new-action-sql" -> {
                if (ra.length < 1) { System.err.println("usage: new-action-sql <name> [sql-type] [content]"); return; }
                String sqlType = ra.length > 1 ? ra[1] : "query";
                String c = ra.length > 2 ? ra[2] : new String(System.in.readAllBytes(), StandardCharsets.UTF_8);
                cmdNewActionSql(ra[0], sqlType, c);
            }
            default -> { System.err.println("Unknown command: " + cmd); usage(); }
        }
    }
}
