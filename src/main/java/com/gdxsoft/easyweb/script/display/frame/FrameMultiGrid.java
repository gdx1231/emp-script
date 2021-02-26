package com.gdxsoft.easyweb.script.display.frame;

import java.text.NumberFormat;
import java.util.HashMap;

import com.gdxsoft.easyweb.data.DTCell;
import com.gdxsoft.easyweb.data.DTColumn;
import com.gdxsoft.easyweb.data.DTRow;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.script.display.HtmlUtils;
import com.gdxsoft.easyweb.script.html.HtmlDocument;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValue;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MListStr;
import com.gdxsoft.easyweb.utils.msnet.MStr;

/**
 * 多维表生成
 * 
 * @author Administrator
 * 
 */
public class FrameMultiGrid extends FrameBase implements IFrame {
	private static String ID_PREFIX = "[~!@#%烱]";

	private HashMap<String, DTRow> _MapOriData;

	/**
	 * 生成多维表内容<br>
	 * 第一步，获取X，Y，DATA配置信息<br>
	 * 第二步，生产表头和左边描述信息，同时生成多维表的数据结构<br>
	 * 第三步，填充多维表数据<br>
	 * 第四步，生产多维HTML
	 */
	public void createContent() throws Exception {
		_MapOriData = new HashMap<String, DTRow>();

		HtmlDocument doc = this.getHtmlClass().getDocument();

		String pageAddTop = this.getPageItemValue("AddHtml", "Top");
		doc.addScriptHtml(pageAddTop == null ? "" : pageAddTop);

		DTTable t = new DTTable(); // 填充数据用临时表

		String EWA_GRID_TRANS = super.getHtmlClass().getItemValues().getRequestValue().getString("EWA_GRID_TRANS");
		MStr[] heads;
		MStr[] leftRows;
		boolean isTrans = false;
		if (EWA_GRID_TRANS != null && EWA_GRID_TRANS.equals("1")) {
			// 转置表格，xy对调
			isTrans = true;
		}
		// 行标头
		heads = this.createGridHeaders(t, isTrans);
		// 列左侧标头
		leftRows = this.createGridLeftRows(t, isTrans);

		// 填充数据
		this.fillGridData(t, isTrans);

		// 生成网格
		MStr sb = new MStr();
		String tbId = "EWA_MG_" + super.getHtmlClass().getSysParas().getFrameUnid();
		sb.append("<table border=0 cellspacing=1 cellpadding=4" + " id='" + tbId
				+ "' class='EWA_GRID_TABLE' onmouseover='EWA.UI.MultiGrid.MOut(event)'>");

		String htmlTans = "<td class=EWA_GRID_H rowSpan='" + heads.length + "' colSpan='" + leftRows.length
				+ "'><a href=\"javascript:EWA.F.FOS['" + super.getHtmlClass().getSysParas().getFrameUnid()
				+ "'].Trans()\">转置</a></td>";

		for (int m = 0; m < heads.length; m++) {
			sb.append("<tr>");
			if (m == 0) {
				sb.append(htmlTans);
			}
			sb.append(heads[m]);
			sb.append("</tr>");
		}

		String[][] leftRows1 = new String[leftRows.length][];
		for (int i = 0; i < leftRows.length; i++) {
			leftRows1[i] = leftRows[i].toString().split("#");
		}

		UserXItemValue item = super.getHtmlClass().getUserConfig().getUserPageItem().getItem("MGCell").getItem(0);
		String cellTemplateHtml = item.testName("CellHtml") ? item.getItem("CellHtml").trim() : "";
		MListStr al = Utils.getParameters(cellTemplateHtml, "@");
		// 生成数据HTML

		for (int i = 0; i < t.getCount(); i++) {
			DTRow r = t.getRow(i);
			sb.append("<tr>");
			try {
				for (int m = 0; m < leftRows1.length; m++) {
					if (i >= leftRows1[m].length) {
						sb.append("");
					} else {
						String left = leftRows1[m][i];
						sb.append(left);
					}
				}
				for (int m = 0; m < t.getColumns().getCount(); m++) {
					DTColumn col = t.getColumns().getColumn(m);
					DTCell cell = r.getCell(m);
					if (col.getName().indexOf("$$EWA_COL$$") == 0) {
						// 列汇总
						Double v2 = getRowComputValue(t, i, col.getName());
						String[] names = col.getName().split("\\$\\$");

						cell.setValue(v2);
						try {
							String v = createCellHtml(cell, "", al);
							v = v.replace("<td", "<td name=\"" + names[names.length - 1] + "\" ");
							sb.append(v);
						} catch (Exception err) {
							sb.append(err.getMessage());
						}
					} else if (r.getName().indexOf("$$EWA_ROW$$") == 0) {
						// 行汇总
						Double v2 = getColComputValue(t, m, r.getName());
						String[] names = r.getName().split("\\$\\$");
						cell.setValue(v2);
						try {
							String v = createCellHtml(cell, "", al);
							v = v.replace("<td", "<td name=\"" + names[names.length - 1] + "\" ");
							sb.append(v);
						} catch (Exception err) {
							sb.append(err.getMessage());
						}
					} else {
						try {
							String v = createCellHtml(cell, cellTemplateHtml, al);
							sb.append(v);
						} catch (Exception err) {
							sb.append(err.getMessage());
						}
					}
				}
			} catch (Exception err) {
				sb.append(err.getMessage());
			}
			sb.append("</tr>\r\n");
		}
		sb.append("</table><div id=X></div><div id=Y></div>");

		doc.addScriptHtml(sb.toString());

		// String pageAddBottom = this.getPageItemValue("AddHtml", "Bottom");
		// doc.addScriptHtml(pageAddBottom == null ? "" : pageAddBottom);
	}

	private Double getRowComputValue(DTTable t, int rowIndex, String rowName) {
		String[] exps = rowName.split("\\$\\$");

		if (exps[2].endsWith("AVG")) {
			return t.getRowAvg(rowIndex);
		} else if (exps[2].endsWith("MAX")) {
			return t.getRowMax(rowIndex);
		} else if (exps[2].endsWith("MIN")) {
			return t.getRowMin(rowIndex);
		} else if (exps[2].endsWith("COUNT")) {
			return Double.valueOf(t.getColumns().getCount());
		} else if (exps[2].endsWith("SUM")) {
			return t.getRowSum(rowIndex);
		}

		return Double.valueOf(0);

	}

	private Double getColComputValue(DTTable t, int colIndex, String colName) {
		String[] exps = colName.split("\\$\\$");
		if (exps[2].endsWith("AVG")) {
			return t.getColAvg(colIndex);
		} else if (exps[2].endsWith("MAX")) {
			return t.getColMax(colIndex);
		} else if (exps[2].endsWith("MIN")) {
			return t.getColMin(colIndex);
		} else if (exps[2].endsWith("COUNT")) {
			return Double.valueOf(t.getCount());
		} else if (exps[2].endsWith("SUM")) {
			return t.getColSum(colIndex);
		}

		return Double.valueOf(0);
	}

	/**
	 * 生成单元格HTML
	 * 
	 * @param cell
	 * @param cellTemplateHtml 模板
	 * @param al               参数对象
	 * @return
	 */
	private String createCellHtml(DTCell cell, String cellTemplateHtml, MListStr al) {
		String tbId = super.getHtmlClass().getSysParas().getFrameUnid();
		String v;
		if (cell.getValue() != null) {
			NumberFormat formatter = NumberFormat.getNumberInstance();
			formatter.setMaximumFractionDigits(2);
			try {
				v = formatter.format(cell.getValue());
			} catch (Exception err) {
				v = cell.toString();
			}
		} else {
			v = cell.toString();
		}

		String html = "";

		String EWA_GRID_TRANS = super.getHtmlClass().getSysParas().getRequestValue().getString("EWA_GRID_TRANS");
		boolean isTrans = false;
		if (EWA_GRID_TRANS != null && EWA_GRID_TRANS.equals("1")) {
			isTrans = true;
		}
		if (v == null || v.trim().length() == 0) {
			v = "&nbsp;";
		}
		if (cellTemplateHtml.length() == 0) {
			html = v;
		} else {
			html = cellTemplateHtml;
			String rowId = cell.getRow().getName().replace(ID_PREFIX, "");
			String colId = cell.getColumn().getName().replace(ID_PREFIX, "");
			if (isTrans) {
				String tmp = rowId;
				rowId = colId;
				colId = tmp;
			}
			for (int i = 0; i < al.size(); i++) {
				String tag = al.get(i);
				if (tag.toUpperCase().trim().equals("VAL")) {
					html = html.replace("@" + tag, v);
				} else if (tag.toUpperCase().trim().equals("COL")) {
					html = html.replace("@" + tag, colId);
				} else if (tag.toUpperCase().trim().equals("ROW")) {
					html = html.replace("@" + tag, rowId);
				}

			}

			try {
				// 获取原始行数据
				String key = cell.getRow().getName() + "," + cell.getColumn().getName();
				DTRow cellRowData = this._MapOriData.get(key);
				html = this.myReplaceParameters(html, cellRowData);
				if (html.trim().equals("+")) {
					html = "";
				}
			} catch (Exception e) {
			}

		}
		String id = tbId + "$MGC_" + cell.getRow().getIndex() + "_" + cell.getColumn().getIndex();
		String yId = cell.getRow().getName().replace(ID_PREFIX + ID_PREFIX, ";").replace(ID_PREFIX, "");
		String xId = cell.getColumn().getName().replace(ID_PREFIX + ID_PREFIX, ";").replace(ID_PREFIX, "");
		String id1 = xId + "_" + yId;
		MStr sb = new MStr();
		sb.append("<td id=\"" + id + "\" val=\"" + v + "\" ids=\"" + cell.getColumn().getTypeName()
				+ "\" nowrap class='EWA_GRID' id1='" + id1 + "'><nobr>" + html + "</nobr>");
		String[] ids = cell.getColumn().getTypeName().split("\\;");
		for (int k = 0; k < ids.length; k++) {
			sb.append("<" + ids[k] + "/>");
		}
		sb.append("</td>\r\n");
		return sb.toString();
	}

	/**
	 * 将数据填充到多维表中，根据actionName获取所有表名为actionName的表<br>
	 * 并将数据根据X（DTRow名称），Y（DTColumn名称）轴名称填充数据到单元格中。
	 * 
	 * @param t    多维表
	 * @param item
	 * @throws Exception
	 */
	private void fillGridData(DTTable t, boolean isTrans) throws Exception {
		UserXItemValue item = super.getHtmlClass().getUserConfig().getUserPageItem().getItem("MGCell").getItem(0);

		String fieldXName = item.getItem("CellXField");
		String fieldYName = item.getItem("CellYField");
		String fieldDataName = item.getItem("CellDataField");
		String actionName = item.getItem("CellAction");

		String[] xx = fieldXName.split(",");
		String[] yy = fieldYName.split(",");
		String[] dd = fieldDataName.split(",");

		DTTable tbData = this.getDTTable(actionName);
		while (tbData != null) {
			tbData.setName(null);
			this.fillGridData(t, tbData, xx, yy, dd, isTrans);
			tbData = this.getDTTable(actionName);
		}
	}

	/**
	 * 将数据填充到多维表中，根据actionName获取所有表名为actionName的表<br>
	 * 并将数据根据X（DTRow名称），Y（DTColumn名称）轴名称填充数据到单元格中。
	 * 
	 * @param t       多维表
	 * @param tbData  数据
	 * @param xx      X轴名称列表
	 * @param yy      Y轴名称列表
	 * @param dd      数据字段列表
	 * @param isTrans 是否转置
	 * @throws Exception
	 */
	private void fillGridData(DTTable t, DTTable tbData, String[] xx, String[] yy, String[] dd, boolean isTrans)
			throws Exception {
		for (int i = 0; i < tbData.getCount(); i++) {
			DTRow r = tbData.getRow(i);
			for (int m1 = 0; m1 < dd.length; m1++) {
				String xId = "";
				String yId = "";

				for (int m = 0; m < xx.length; m++) {
					String f = xx[m].trim().equalsIgnoreCase("@") ? dd[m1].toUpperCase().trim()
							: r.getCell(xx[m]).toString();
					xId = xId + ID_PREFIX + f + ID_PREFIX;
				}
				for (int m = 0; m < yy.length; m++) {
					String f = yy[m].trim().equalsIgnoreCase("@") ? dd[m1].toUpperCase().trim()
							: r.getCell(yy[m]).toString();
					yId = yId + ID_PREFIX + f + ID_PREFIX;
				}
				if (isTrans) {
					String tmp = xId;
					xId = yId;
					yId = tmp;
				}

				if (t.getColumns().testName(xId)) {
					DTRow r1 = t.getRows().getRow(yId);
					if (r1 == null)
						continue;
					DTCell cell = r1.getCell(xId);
					Object v = r.getCell(dd[m1]).getValue();
					cell.setValue(v);
					String rowKey = yId + "," + xId;
					if (_MapOriData.containsKey(rowKey)) {
						System.out.println(rowKey + "REPEAT");
					} else {
						_MapOriData.put(rowKey, r);
					}
				}
			}
		}
	}

	/**
	 * 生成头部表达式，同时生成网格表的字段表达式
	 * 
	 * @param t    网格表
	 * @param item
	 * @return
	 * @throws Exception
	 */
	private MStr[] createGridHeaders(DTTable t, boolean isTrans) throws Exception {

		String fieldName;
		String actionName;
		String attrExp = null;
		if (isTrans) {
			UserXItemValue item = super.getHtmlClass().getUserConfig().getUserPageItem().getItem("MGAxisY").getItem(0);
			fieldName = item.getItem("YAxisField");
			actionName = item.getItem("YAxisAction");
			if (item.testName("YAxisAttrExp")) {
				attrExp = item.getItem("YAxisAttrExp");
			}
		} else {
			UserXItemValue item = super.getHtmlClass().getUserConfig().getUserPageItem().getItem("MGAxisX").getItem(0);
			fieldName = item.getItem("XAxisField");
			actionName = item.getItem("XAxisAction");

			if (item.testName("XAxisAttrExp")) {
				attrExp = item.getItem("XAxisAttrExp");
			}
		}

		DTTable tbY = this.getDTTable(actionName);
		String[] fys = fieldName.split(",");
		String[] heads = new String[fys.length];
		int[] colSpans = new int[fys.length];
		MStr[] sbHeads = new MStr[fys.length];
		for (int i = 0; i < fys.length; i++) {
			sbHeads[i] = new MStr();
			heads[i] = "asdaks812812dkmakdakaskda__k~!@!sd";
			colSpans[i] = 0;
		}

		String frameUnid = super.getHtmlClass().getSysParas().getFrameUnid();
		for (int i = 0; i < tbY.getCount(); i++) {
			DTRow r = tbY.getRow(i);
			DTColumn col = new DTColumn();
			String htmlId = "MGA_" + i;
			String colName = "";
			String ids = "";
			for (int m = 0; m < fys.length; m++) {
				String[] ff = fys[m].split(";");
				String v = r.getCell(ff[0]).toString();
				String id = r.getCell(ff[ff.length - 1]).toString();
				if (id == null) {
					id = "";
				}
				if (ff.length > 1) {
					// 列的的名称，用于数据填充时查找
					colName = colName + ID_PREFIX + id.toUpperCase() + ID_PREFIX;
				}
				if (m > 0) {
					ids += ";";
				}
				ids += ("i__" + id);
				if (!id.equals(heads[m])) {
					String rep = "colspan='" + colSpans[m] + "'";
					sbHeads[m].replace("[COLSPAN]", rep);
					String htmlId1 = htmlId + "_" + m;

					String attExp1 = "";
					if (attrExp != null && attrExp.trim().length() > 0) {
						attExp1 = myReplaceParameters(attrExp, r);
					}
					sbHeads[m].append("<td " + attExp1 + " id=\"" + htmlId1 + "\" nowrap val=\"" + v + "\" ids=\""
							+ ids.replace("\"", "&quot;") + "\" class='EWA_GRID_H' [COLSPAN]><nobr>" + v
							+ "</nobr>[MARK+]</td>\r\n");
					rep = "";
					String[] idds = ids.split("\\;");
					for (int kk = 0; kk < idds.length - 1; kk++) {
						rep += "<" + idds[kk] + "></" + idds[kk] + ">";
					}
					if (m < fys.length - 1) {
						rep += "<span onclick=\"EWA.F.FOS['" + frameUnid + "'].CollapseCol(" + ("'i__" + id)
								+ "',this)\" style='cursor:pointer;font-size:14px'>-</span>";
					}
					sbHeads[m].replace("[MARK+]", rep);
					heads[m] = id;
					colSpans[m] = 1;
				} else {
					colSpans[m]++;
				}
			}
			col.setName(colName);
			col.setTypeName(ids);
			t.getColumns().addColumn(col);
		}
		for (int m = 0; m < fys.length; m++) {
			sbHeads[m].replace("[COLSPAN]", "colspan='" + colSpans[m] + "'");
			String rep;
			if (colSpans[m] > 1) {
				rep = "<span onclick=\"EWA.F.FOS['" + frameUnid
						+ "'].CollapseCol(this)\" style='cursor:pointer'>-</span>";
			} else {
				rep = "";
			}
			sbHeads[m].replace("[MARK+]", rep);
		}

		// 自定义字段 ，X 增加 COLUMN
		for (int i = 0; i < super.getHtmlClass().getUserConfig().getUserXItems().count(); i++) {
			UserXItem item = super.getHtmlClass().getUserConfig().getUserXItems().getItem(i);
			String tag = item.getSingleValue("Tag");
			if (!tag.equalsIgnoreCase("MGAddField")) {
				continue;
			}
			String mgfTarget = item.getSingleValue("MGAddField", "MgfTarget");
			mgfTarget = (mgfTarget == null || mgfTarget.trim().length() == 0) ? "X" : mgfTarget.trim().toUpperCase();

			// 当转置时，获取Y，否则获取X
			if ((!isTrans && mgfTarget.equals("X")) || (isTrans && mgfTarget.equals("Y"))) {

				String mgfComput = item.getSingleValue("MGAddField", "MgfComput");
				mgfComput = (mgfComput == null || mgfComput.trim().length() == 0) ? "" : mgfComput.trim().toUpperCase();
				String des = HtmlUtils.getDescription(item.getItem("DescriptionSet"), "Info",
						super.getHtmlClass().getSysParas().getLang());
				// 计算方式
				String calc = item.getSingleValue("MGAddField", "MgfCalc");

				DTColumn col = new DTColumn();
				col.setName("$$EWA_COL$$" + mgfComput + "$$" + calc + "$$" + item.getName());
				col.setTypeName("number");
				t.getColumns().addColumn(col);
				MStr sb = new MStr();
				sb.append("<td  nowrap class='EWA_GRID_H' rowspan='" + sbHeads.length + "'><nobr>" + des
						+ "</nobr></td>\r\n");
				sbHeads[0].append(sb);
			}

		}
		return sbHeads;
	}

	/**
	 * 生成表左边描述
	 * 
	 * @param t    多维表
	 * @param item
	 * @return
	 * @throws Exception
	 */
	private MStr[] createGridLeftRows(DTTable t, boolean isTrans) throws Exception {
		String fieldName;
		String actionName;
		String attrExp = null;
		if (!isTrans) {
			UserXItemValue item = super.getHtmlClass().getUserConfig().getUserPageItem().getItem("MGAxisY").getItem(0);
			fieldName = item.getItem("YAxisField");
			actionName = item.getItem("YAxisAction");
			if (item.testName("YAxisAttrExp")) {
				attrExp = item.getItem("YAxisAttrExp");
			}
		} else {
			UserXItemValue item = super.getHtmlClass().getUserConfig().getUserPageItem().getItem("MGAxisX").getItem(0);
			fieldName = item.getItem("XAxisField");
			actionName = item.getItem("XAxisAction");
			if (item.testName("XAxisAttrExp")) {
				attrExp = item.getItem("XAxisAttrExp");
			}
		}

		DTTable tbX = this.getDTTable(actionName);

		String[] fxs = fieldName.split(",");
		String[] leftRows = new String[fxs.length];
		int[] rowSpans = new int[fxs.length];
		MStr[] sbLefRows = new MStr[fxs.length];

		for (int i = 0; i < fxs.length; i++) {
			sbLefRows[i] = new MStr();
			leftRows[i] = "asdaks812812dkmakdakaskda__k~!@!sd";
			rowSpans[i] = 0;
		}

		for (int i = 0; i < tbX.getCount(); i++) {
			DTRow r = t.addRow();
			String name = "";
			String htmlId = "MGB_" + i;
			String id1 = "";
			for (int m = 0; m < fxs.length; m++) {
				String[] ff = fxs[m].replace(" ", "").split(";");
				String v = tbX.getRow(i).getCell(ff[0]).toString();
				if (v == null)
					v = "[NULL]";
				String id = tbX.getRow(i).getCell(ff[ff.length - 1]).toString();
				id1 += (id1.length() == 0 ? "" : ",") + id;
				if (ff.length > 1) {
					// 行的id，用于数据填充时用
					name = name + ID_PREFIX + id.toUpperCase() + ID_PREFIX;
				}
				if (!v.equals(leftRows[m])) {
					sbLefRows[m].replace("[ROWSPAN]", "rowspan='" + rowSpans[m] + "'");
					String htmlId1 = htmlId + "_" + m;

					String attExp1 = "";
					if (attrExp != null && attrExp.trim().length() > 0) {
						attExp1 = myReplaceParameters(attrExp, tbX.getRow(i));
					}

					sbLefRows[m].append("<td id=\"" + htmlId1 + "\" id1=\"" + id1 + "\" class='EWA_GRID_L' " + attExp1
							+ "  nowrap [ROWSPAN]>" + v + "</td>\r\n" + "#");
					rowSpans[m] = 1;
					leftRows[m] = v; // (m > 0 ? leftRows[m - 1] + ID_PREFIX :
					// "")
				} else {
					rowSpans[m]++;
					sbLefRows[m].append("#");
				}
			}
			r.setName(name);
		}
		for (int m = 0; m < fxs.length; m++) {
			sbLefRows[m].replace("[ROWSPAN]", "rowspan='" + rowSpans[m] + "'");
		}

		String lang = super.getHtmlClass().getSysParas().getLang();

		// 自定义字段 ，Y 增加 行
		for (int i = 0; i < super.getHtmlClass().getUserConfig().getUserXItems().count(); i++) {
			UserXItem item = super.getHtmlClass().getUserConfig().getUserXItems().getItem(i);
			String tag = item.getSingleValue("Tag");
			if (!tag.equalsIgnoreCase("MGAddField")) {
				continue;
			}
			String mgfTarget = item.getSingleValue("MGAddField", "MgfTarget");
			mgfTarget = (mgfTarget == null || mgfTarget.trim().length() == 0) ? "X" : mgfTarget.trim().toUpperCase();

			if (isTrans && mgfTarget.equals("X") || !isTrans && mgfTarget.equals("Y")) {

				String mgfComput = item.getSingleValue("MGAddField", "MgfComput");
				mgfComput = (mgfComput == null || mgfComput.trim().length() == 0) ? "SUM"
						: mgfComput.trim().toUpperCase();
				String des = HtmlUtils.getDescription(item.getItem("DescriptionSet"), "Info", lang);
				String calc = item.getSingleValue("MGAddField", "MgfCalc");
				DTRow row = t.addRow();
				row.setName("$$EWA_ROW$$" + mgfComput + "$$" + calc + "$$" + item.getName());
				MStr sb = new MStr();
				sb.append("<td  nowrap class='EWA_GRID_L'  colspan='" + sbLefRows.length + "'><nobr>" + des
						+ "</nobr></td>\r\n#");
				sbLefRows[sbLefRows.length - 1].append(sb);
			}
		}
		return sbLefRows;
	}

	/**
	 * 根据表名获取表
	 * 
	 * @param tableName
	 * @return
	 */
	private DTTable getDTTable(String tableName) {
		if (tableName == null || tableName.trim().length() == 0) {
			return null;
		}
		String n = tableName.toUpperCase().trim();
		for (int i = 0; i < super.getHtmlClass().getItemValues().getDTTables().size(); i++) {
			DTTable tb = (DTTable) super.getHtmlClass().getItemValues().getDTTables().get(i);
			if (tb.getName() == null) {
				continue;
			}
			if (tb.getName().trim().toUpperCase().equals(n)) {
				return tb;
			}
		}
		return null;
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
		return "";
	}

	public void createJsFramePage() throws Exception {
		String gunid = super.getHtmlClass().getSysParas().getFrameUnid();
		String lang = super.getHtmlClass().getSysParas().getLang();

		// 页面URL的JS表达式
		String url = super.getUrlJs();

		MStr sJs = new MStr();

		super.createJsFrameXml(); // item描述XML字符串
		super.createJsFrameMenu(); // menu描述XML字符串

		sJs.append("\r\nEWA.LANG='" + lang + "'; //page language\r\n");
		String funName = "EWA_F" + gunid + "()";
		String pageDescription = super.getPageJsTitle();
		sJs.append("\r\nfunction " + funName + "{\r\n");
		sJs.append("var o1=EWA.F.FOS['" + gunid + "']=new EWA.F.M.C();\r\n");
		sJs.append("\t o1._Id = '" + gunid + "';\r\n");
		sJs.append("\t o1.Url = \"" + url + "\";\r\n");
		sJs.append("\t o1.Init(EWA_ITEMS_XML_" + gunid + ");\r\n");
		sJs.al(" o1.Title = \"" + pageDescription + "\";");
		sJs.append("\t o1 = null;\r\n");
		sJs.append("}\r\n");
		sJs.append(funName + ";\r\n");

		this.getHtmlClass().getDocument().addJs("JsFrame", sJs.toString(), false);
	}

	public String createaXmlData() throws Exception {
		return "";
	}

	private String myReplaceParameters(String s1, DTRow r) {
		if (s1 == null)
			return s1;
		MListStr a = Utils.getParameters(s1, "@");
		MStr sb = new MStr(s1);
		for (int i = 0; i < a.size(); i++) {
			String name = a.get(i);
			String val;
			try {
				val = this.getCellValue(r, name);
			} catch (Exception e) {
				val = "";
			}

			if (val == null) {
				val = "";
			}
			String find = "@" + name;
			sb.replace(find, val);
		}
		return sb.toString();
	}

	private String getCellValue(DTRow row, String dataFieldName) throws Exception {
		DTCell cell = row.getCell(dataFieldName);
		if (cell.getColumn().getTypeName() != null && cell.getColumn().getTypeName().toUpperCase() == "CLOB") {
			return cell.toString();
		}
		return cell == null ? null : cell.getValue().toString();
	}
}
