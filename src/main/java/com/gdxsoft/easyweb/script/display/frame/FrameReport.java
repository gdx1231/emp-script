package com.gdxsoft.easyweb.script.display.frame;

import com.gdxsoft.easyweb.data.DTRow;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.script.template.SkinFrame;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;
import com.gdxsoft.easyweb.script.userConfig.UserXItems;
import com.gdxsoft.easyweb.utils.msnet.MStr;

public class FrameReport extends FrameBase implements IFrame {

	private static String REPT_STR0 = "<!--EWA_REPT_START-->";
	private static String REPT_STR1 = "<!--EWA_REPT_END-->";

	public void createContent() throws Exception {
		String pageAddTop = this.getPageItemValue("AddHtml", "Top");
		this.getHtmlClass().getDocument().addScriptHtml(pageAddTop);

		String cnt = createItemHtmls();
		this.getHtmlClass().getDocument().addScriptHtml(cnt);
	}

	public void createFrameContent() throws Exception {

	}

	public void createFrameFooter() throws Exception {

	}

	public String createFrameHeader() throws Exception {
		MStr sb = new MStr();
		return sb.toString();
	}

	public void createHtml() throws Exception {
		super.createSkinTop();
		super.createCss();
		super.createJsTop();

		// content
		this.createContent();

		// Frame脚本
		this.createJsFramePage();
		this.createSkinBottom();
		this.createJsBottom();

	}

	public String createItemHtmls() throws Exception {
		MStr sb = new MStr();
		UserXItems items = super.getHtmlClass().getUserConfig().getUserXItems();
		for (int i = 0; i < items.count(); i++) {
			UserXItem uxi = items.getItem(i);

			String s2 = this.createItemReport(uxi);
			sb.al(s2);
		}
		if (sb.indexOf("@") > 0) { // 替换未替换的值
			return super.getHtmlClass().getItemValues().replaceParameters(
					sb.toString(), false);
		} else {
			return sb.toString();
		}
	}

	/**
	 * 生成Report内容
	 * 
	 * @param uxi
	 * @return
	 * @throws Exception
	 */
	private String createItemReport(UserXItem uxi) throws Exception {
		String parentHtml = "<div>{__EWA_ITEM__}</div>";

		String ReportAction = uxi.getSingleValue("ReportCfg", "ReportAction");
		String IsReportRepeat = uxi.getSingleValue("ReportCfg",
				"IsReportRepeat");
		String ReportTemplate = uxi.getSingleValue("ReportCfg",
				"ReportTemplate");

		super.getHtmlClass().getItemValues().getDTTables().clear();
		super.getHtmlClass().getHtmlCreator().executeAction(ReportAction);

		String tmp = ReportTemplate;

		if (IsReportRepeat != null && IsReportRepeat.equals("1")) { // 重复项目
			MStr s = new MStr();
			String tmpTop = "";
			String tmpBottom = "";

			int loc0 = tmp.indexOf(REPT_STR0);
			int loc1 = tmp.indexOf(REPT_STR1);
			if (loc0 >= 0 && loc1 > loc0) {
				tmpTop = tmp.substring(0, loc0 + REPT_STR0.length());
				tmpBottom = tmp.substring(loc1);
				tmp = tmp.substring(loc0 + REPT_STR0.length(), loc1);
			}
			s.a(tmpTop);

			// 最后一个select
			DTTable tb = (DTTable) super.getHtmlClass().getItemValues()
					.getDTTables().getLast();
			super.getHtmlClass().getItemValues().setListFrameTable(tb);

			MStr s1 = new MStr();

			for (int i = 0; i < tb.getCount(); i++) {
				DTRow r = tb.getRow(i);
				tb.getRows().setCurRow(r);

				String itemHtml = getHtmlClass().getItemValues()
						.replaceParameters(tmp, true, true, true);
				itemHtml = itemHtml.replace("@", "\1\2$$##GDX~##JZY$$\3\4"); // 替换值
				s1.al(itemHtml);
			}

			s.al(s1.toString());
			s.a(tmpBottom);
			return s.toString();
		} else {
			String itemHtml = getHtmlClass().getItemValues().replaceParameters(
					tmp, true, true, true);
			itemHtml = itemHtml.replace("@", "\1\2$$##GDX~##JZY$$\3\4"); // 替换值
			String s2 = parentHtml.replace(SkinFrame.TAG_ITEM, itemHtml);
			return s2;
		}
	}

	public void createJsFramePage() throws Exception {

	}

	public String createaXmlData() throws Exception {
		return "";
	}

}
