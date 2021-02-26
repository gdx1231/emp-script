package com.gdxsoft.easyweb.script.display.frame;

import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.datasource.DataResult;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.html.HtmlDocument;
import com.gdxsoft.easyweb.utils.msnet.MList;
import com.gdxsoft.easyweb.utils.msnet.MStr;

public class FrameGrid extends FrameList implements IFrame {
	public void createContent() throws Exception {
		HtmlDocument doc = this.getHtmlClass().getDocument();

		// 用户自定义头部html
		String pageAddTop = this.getPageItemValue("AddHtml", "Top");
		doc.addScriptHtml(pageAddTop == null ? "" : pageAddTop.trim());
		// System.out.println("top=" + pageAddTop);
		// Frame内容
		createFrameContent();
		// throw new Exception(pageAddTop);
	}

	public void createFrameContent() throws Exception {
		HtmlDocument doc = this.getHtmlClass().getDocument();

		// 皮肤定义的头部
		doc.addScriptHtml("<div>");
		String top = super.createSkinFCTop();
		doc.addScriptHtml(top);

		// Frame定义的页头
		doc.addScriptHtml(createFrameHeader(), "frame head");

		MList tbs = super.getHtmlClass().getAction().getDTTables();

		if (tbs == null || tbs.size() == 0) {
			doc.addScriptHtml("no data");
		}
		DTTable tb = (DTTable) tbs.get(tbs.size() - 1);

		super.getHtmlClass().getItemValues().setListFrameTable(tb);

		DataConnection conn = super.getHtmlClass().getItemValues().getDataConn();

		// 获取所有记录数，根据执行的最后的select语句获取
		DataResult ds = conn.getLastResult();
		if (ds != null && this.getHtmlClass().getUserConfig().getUserPageItem()
				.getSingleValue("PageSize", "IsSplitPage").equals("1")) {
			_ListFrameRecordCount = conn.getRecordCount(ds.getSqlOrigin());
		}

		int colSize = 0; // 分栏数
		String s1 = super.getPageItemValue("PageSize", "ColSize");
		if (!(s1 == null || s1.trim().length() == 0)) {
			try {
				colSize = Integer.parseInt(s1);
			} catch (Exception e) {
				// nothing
			}
		}
		if (colSize <= 0) {
			colSize = 0;
		}

		int colSizeInc = 1;

		MStr sb = new MStr();
		sb.al("<div class='EWA_TABLE ewa-grid-frame' id='EWA_LF_" + super.getHtmlClass().getSysParas().getFrameUnid()
				+ "' >");
		RequestValue rv = super.getHtmlClass().getItemValues().getRequestValue();

		String tag0 = "li";
		String tag1 = "li";
		if (rv.s("ewa_grid_as") != null) {
			if (rv.s("ewa_grid_as").equalsIgnoreCase("a")) {
				tag0 = "a";
				tag1 = "a";
			} else if (rv.s("ewa_grid_as").equalsIgnoreCase("div")) {
				tag0 = "div";
				tag1 = "div";
			} else if (rv.s("ewa_grid_as").equalsIgnoreCase("div2")) {
				tag0 = "div><div";
				tag1 = "div></div";
			}
		}

		// 是否使用模板
		boolean isUseTemplate = false;
		String frameTemplate = super.getPageItemValue("FrameHtml", "FrameHtml");
		if (frameTemplate != null && frameTemplate.trim().length() > 0 && rv.s("EWA_LF_TEMP_NO") == null) {
			isUseTemplate = true;
		}

		if (colSize == 0) {

			sb.al("<ul class='ewa_grid_ul'>");
			for (int i = 0; i < tb.getCount(); i++) {
				tb.getRow(i); // 将数据移动到当前行
				String keyExp = super.createItemKeys();
				String rowHtml = isUseTemplate ? super.createItemHtmlsByFrameHtml(frameTemplate, "Grid")
						: super.createItemHtmls();

				sb.al("<" + tag0 + "  class='ewa_grid_li' ewa_key=\"" + keyExp + "\">");
				sb.al(rowHtml);
				sb.al("</" + tag1 + ">");
			}
			sb.al("</ul>");
		} else {
			sb.al("<table border='0' cellpadding='0' cellspacing='0' align='center'>");
			for (int i = 0; i < tb.getCount(); i++) {
				tb.getRow(i); // 将数据移动到当前行
				String keyExp = super.createItemKeys();

				String rowHtml = isUseTemplate ? super.createItemHtmlsByFrameHtml(frameTemplate, "Grid")
						: super.createItemHtmls();

				if (colSizeInc == 1) {
					sb.al("<tr>");
				}
				sb.al("<td " + keyExp + ">");
				sb.al(rowHtml);
				sb.al("</td>");
				if (colSizeInc == colSize && colSize > 0) {
					sb.al("</tr>");
					sb.al("<tr>");
				}
				colSizeInc++;
				if (colSizeInc > colSize) {
					colSizeInc = 1;
				}
			}
			for (int i = colSizeInc; i <= colSize; i++) {
				sb.al("<td>&nbsp;</td>");
			}
			sb.a("</tr></table>");
		}

		sb.al("</div>");
		doc.addScriptHtml(sb.toString(), "Frame content");

		// 皮肤定义定义的尾部
		String bottom = super.createSkinFCBottom();
		doc.addScriptHtml(bottom);

		// Frame定义的页脚
		this.createFrameFooter();
		doc.addScriptHtml("</div>");

	}

	public String createFrameHeader() throws Exception {
		return "";
	}

	public void createHtml() throws Exception {
		HtmlDocument doc = this.getHtmlClass().getDocument();

		super.createSkinTop();
		super.createCss();
		super.createJsTop();

		doc.addScriptHtml("<div>");
		this.createContent();
		doc.addScriptHtml("</div>");

		this.createSkinBottom();
		this.createJsBottom();
		// Frame脚本
		this.createJsFramePage();

	}

}
