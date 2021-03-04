package com.gdxsoft.easyweb.document;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import com.gdxsoft.easyweb.data.DTCell;
import com.gdxsoft.easyweb.data.DTColumns;
import com.gdxsoft.easyweb.data.DTRow;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MListStr;
import com.gdxsoft.easyweb.utils.msnet.MStr;

public class OdtWorker {

	public static void main(String[] args) throws Exception {

	}

	public OdtWorker() {

	}

	/**
	 * 生成文件
	 * 
	 * @param tmplate    模板
	 * @param exportName 输出文件名
	 * @param rv         参数表
	 * @return
	 * @throws Exception
	 */
	public String doWork(String tmplate, String exportName, RequestValue rv) throws Exception {
		this.templateName = tmplate;
		this.exportName = exportName;
		this.rv = rv;
		List<String> lst = UFile.unZipFile(tmplate);
		String root = "";

		for (int i = 0; i < lst.size(); i++) {
			if (i == 0) {
				File f1 = new File(lst.get(0));
				root = f1.getParent();
			}
			if (lst.get(i).endsWith("content.xml")) {
				String cnt = UFile.readFileText(lst.get(i));
				cnt = cnt.replace("><", ">\n<");
				// String ss = "<office:automatic-styles>";
				// int loc1 = cnt.indexOf(ss);
				// int loc2 = cnt.lastIndexOf("</office:automatic-styles>");
				// String cnt2 = cnt.substring(loc1 + ss.length(), loc2);
				// cnt = cnt.replace(cnt2, "");
				String officeBody = "<office:body>";
				String officeBody1 = "</office:body>";
				int start = cnt.indexOf(officeBody);
				int end = cnt.indexOf(officeBody1);

				String top = cnt.substring(0, start);
				String bottom = cnt.substring(end + officeBody1.length());
				String middle = cnt.substring(start, end + officeBody1.length());

				this.initOdfLevels(middle);
				StringBuilder sb = new StringBuilder();
				sb.append(top);
				String newCnt = runOdfLevelOne(this.odfLevelRoot);
				sb.append(newCnt);
				sb.append(bottom);

				UFile.createNewTextFile(lst.get(i), sb.toString());
			} else if (lst.get(i).endsWith("styles.xml")) {
				String cnt = UFile.readFileText(lst.get(i));
				cnt = cnt.replace("><", ">\n<");

				OdfLevel o = new OdfLevel();
				o.setCntMiddle(cnt);
				o.setId("ROOT");
				o.setMeRv(this.odfLevelRoot.getSubs().get(0).getMeRv());

				String newCnt = this.createOdfLevelOne(o, null);
				UFile.createNewTextFile(lst.get(i), newCnt);
			}
			// System.out.println(lst.get(i));
		}

		UFile.zipPaths(root, exportName);

		return "";
	}

	private String runOdfLevelOne(OdfLevel o) throws Exception {
		this.initData(o);
		DTTable tb = null;
		if (o.getTables().size() > 0) {// 获取最后一个表
			tb = o.getTables().get(o.getTables().size() - 1);
		}
		int size = tb == null ? 1 : tb.getCount();
		DTRow r = null;
		MStr s = new MStr();
		s.a(o.getCntTop());

		for (int i = 0; i < size; i++) {
			if (tb != null) {
				r = tb.getRow(i);
			}
			o.setCurRow(r);
			String rst = this.createOdfLevelOne(o, r);
			for (int m = 0; m < o.getSubs().size(); m++) {
				OdfLevel oChild = o.getSubs().get(m);
				String child = this.runOdfLevelOne(oChild);
				rst = rst.replace(oChild.getUnid(), child);
			}
			s.a(rst);
		}
		s.a(o.getCntBottom());

		return s.toString();
	}

	private String createOdfLevelOne(OdfLevel o, DTRow r) throws Exception {
		String cnt = o.getCntMiddle();
		o.setCurRow(r);
		MListStr al = Utils.getParameters(cnt, "@");
		for (int i = 0; i < al.size(); i++) {
			String name = al.get(i);

			String val = this.getValue(name, o, r);
			if (val == null) {
				val = "";
			}
			// System.out.println(name + "=" + val);
			if (val.indexOf("\n") > 0) {
				int idx0 = cnt.indexOf("@" + name);
				int idx1 = cnt.lastIndexOf("<text:p", idx0);
				String tmp = cnt.substring(0, idx0);
				String prefix = tmp.substring(idx1, idx0);
				val = val.replace("\n", "</text:p>" + prefix);
			}
			cnt = cnt.replaceFirst("@" + name, val);

		}

		return cnt;
	}

	private String getValue(String name, OdfLevel o, DTRow r) {
		DTCell cell = getRowCell(name, r);
		if (cell != null) {
			return cell.toString();
		}
		if (!o.getId().equals("ROOT")) {
			OdfLevel o1 = o;

			while (!o1.getParent().getId().equals("ROOT")) {
				o1 = o1.getParent();
				if (o1.getCurRow() != null) {
					cell = getRowCell(name, o1.getCurRow());
					if (cell != null) {
						return cell.toString();
					}
				}
			}
		}
		return o.getMeRv().getString(name);
	}

	private DTCell getRowCell(String name, DTRow r) {
		if (r == null) {
			return null;
		}
		int idx = r.getTable().getColumns().getNameIndex(name);
		if (idx >= 0) {
			return r.getCell(idx);
		} else {
			return null;
		}
	}

	private void initData(OdfLevel o) throws Exception {
		if (o == null || o.getJson() == null || !o.getJson().has("sql")) {
			return;
		}
		DataConnection conn1 = new DataConnection();
		conn1.setConfigName("");
		RequestValue aRv = new RequestValue(this.rv.getRequest(), this.rv.getSession());
		if (o.getParent().getCurRow() != null) {
			DTRow r = o.getParent().getCurRow();
			DTColumns cols = r.getTable().getColumns();
			for (int i = 0; i < cols.getCount(); i++) {
				aRv.addValue(cols.getColumn(i).getName(), r.getCell(i).getValue());
			}
		}
		conn1.setRequestValue(aRv);
		o.setMeRv(aRv);
		String sql = o.getJson().getString("sql");
		sql = sql.replace("&apos;", "'").replace("&lt;", "<").replace("&gt;", ">").replace("?", "");
		if (sql.codePointAt(0) == 65279) {
			sql = sql.substring(1);
		}
		System.out.println(sql);
		String[] sqls = sql.split(";");
		for (int i = 0; i < sqls.length; i++) {
			String s = sqls[i].trim();
			if (s.length() == 0) {
				continue;
			}
			if (s.toUpperCase().indexOf("SELECT") == 0) {
				DTTable tb = DTTable.getJdbcTable(s, conn1);
				if (tb.getCount() == 1) {
					aRv.addValues(tb);
				}
				o.getTables().add(tb);
			} else {
				conn1.executeUpdate(s);
			}
		}
		String err = conn1.getErrorMsg();
		conn1.close();
		if (err != null) {
			throw new Exception(err);
		}

	}

	private void initOdfLevels(String cnt) throws JSONException {
		OdfLevel oRoot = new OdfLevel();
		oRoot.setMeRv(this.rv);
		oRoot.setId("ROOT");
		oRoot.setContent(cnt);
		ArrayList<OdfLevel> map = new ArrayList<OdfLevel>();
		map.add(oRoot);

		// Pattern pat = Pattern.compile(
		// "<text:p[^<.]*?>&lt;!--(?s).*?--&gt;(?s).*?</text:p>",
		// Pattern.CASE_INSENSITIVE);
		Pattern pat = Pattern.compile("<office:annotation>[\\s\\S.]*?</office:annotation>", Pattern.CASE_INSENSITIVE);

		Matcher mat = pat.matcher(cnt);
		String str0 = "{";
		String str1 = "}";

		OdfLevel o = null;
		OdfLevel oPrev = oRoot;
		int idx = 0;
		while (mat.find()) {
			MatchResult mr = mat.toMatchResult();
			String s1 = mr.group();
			String s = s1.replaceAll("<[^>]*?>", "");
			String json = subStr(s, str0, 0, str1);
			json = json.replace("\n", "").replace("\r", "");
			json = "{" + json.replace("“", "\"").replace("”", "\"").replace("，", ",").replace("&quot;", "\"")
					.replace("：", ":") + "}";
			JSONObject obj = new JSONObject(json);
			System.out.println(idx + "," + obj.toString());
			idx++;
			if (obj.has("id")) {
				o = new OdfLevel();
				o.setUnid("[" + Utils.getGuid() + "]");

				o.setLocStart(mr.start());
				o.setMarkStart(s1);
				o.setJsonExp(json);
				o.setJson(obj);
				map.add(o);

				oPrev.getSubs().add(o);

				o.setParent(oPrev);
				oPrev = o;
				o.setId(obj.getString("id"));
			} else if (obj.has("eid")) {
				o = oPrev;
				o.setLocEnd(mr.end());
				o.setMarkEnd(s1);
				o.setContent(cnt.substring(o.getLocStart(), o.getLocEnd()));
				oPrev = o.getParent();
			}
		}

		/**
		 * 替换文本内容,下级替换上级
		 */
		for (int i = 1; i < map.size(); i++) {
			o = map.get(i);
			oPrev = o.getParent();
			String parentCnt = oPrev.getContentFixed() == null ? oPrev.getContent() : oPrev.getContentFixed();
			String curCnt = o.getContent();
			String rept = o.getUnid();
			/*
			 * int loc1 = parentCnt.indexOf(curCnt); if (loc1 < 0) { int mm = 0; mm++; }
			 */
			parentCnt = parentCnt.replace(curCnt, rept);
			// System.out.println(parentCnt.indexOf(rept));
			oPrev.setContentFixed(parentCnt);
		}
		for (int i = 0; i < map.size(); i++) {
			o = map.get(i);
			initCntParts(o);
		}
		odfLevelRoot = oRoot;
	}

	/**
	 * 处理各部分数据
	 * 
	 * @param o
	 * @throws JSONException
	 */
	private void initCntParts(OdfLevel o) throws JSONException {
		String fixedCnt = o.getContentFixed() == null ? o.getContent() : o.getContentFixed();

		String cnt = subStr(fixedCnt, o.getMarkStart(), 0, o.getMarkEnd());
		if (cnt == null) {
			cnt = fixedCnt;
		}
		String top = "";
		String bottom = "";

		if (o.getJson() != null && o.getJson().has("rowRept")) {
			int skipTr = 0; // 跳过的头数量
			if (o.getJson().has("skipTr")) {
				skipTr = o.getJson().getInt("skipTr");
			}
			String[] parts = getTableParts(cnt, skipTr);
			top = parts[0]; // 头
			bottom = parts[2];// 尾部
			cnt = parts[1]; // 内容
		}
		o.setCntBottom(bottom);
		o.setCntTop(top);
		o.setCntMiddle(cnt);
	}

	public void checkTable(String cnt1) {
		int skip = 0;
		String cnt2 = subStr(cnt1, "<table:table-row>", skip, "</table:table-row>");

		System.out.println(cnt2.replace("><", ">\n<"));
	}

	private static String subStr(String cnt1, String str1, int str1SkipNumber, String str2) {
		int loc0 = 0;
		if (str1 == null || str2 == null) {
			return cnt1;
		}
		for (int i = 0; i <= str1SkipNumber; i++) {
			loc0 = cnt1.indexOf(str1, i == 0 ? 0 : loc0 + 1);
			if (loc0 < 0) {
				return null;
			}
			// System.out.println(loc0);
		}
		int loc1 = cnt1.indexOf(str2, loc0);
		if (loc1 < 0) {
			return null;
		}
		String cnt2 = cnt1.substring(loc0 + str1.length(), loc1);
		return cnt2;
	}

	/**
	 * 获取表的各部分
	 * 
	 * @param cnt1
	 * @param headSkipNumber
	 * @return
	 */
	private static String[] getTableParts(String cnt1, int headSkipNumber) {
		String[] parts = new String[3];
		parts[0] = parts[2] = "";
		parts[1] = cnt1;
		int loc0 = 0;
		String str1 = "<table:table-row>";
		String str2 = "</table:table-row>";
		for (int i = 0; i <= headSkipNumber; i++) {
			loc0 = cnt1.indexOf(str1, loc0 + 1);
			if (loc0 < 0) {
				return parts;
			}
		}

		int loc1 = cnt1.lastIndexOf(str2);
		if (loc1 < 0) {
			return parts;
		}
		String cnt2 = cnt1.substring(loc0, loc1 + str2.length());
		parts[1] = cnt2;
		parts[0] = cnt1.substring(0, loc0);
		parts[2] = cnt1.substring(loc1 + str2.length());
		return parts;
	}

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	public String getExportName() {
		return exportName;
	}

	public void setExportName(String exportName) {
		this.exportName = exportName;
	}

	private String templateName;
	private String exportName;
	private OdfLevel odfLevelRoot;
	private RequestValue rv;
}