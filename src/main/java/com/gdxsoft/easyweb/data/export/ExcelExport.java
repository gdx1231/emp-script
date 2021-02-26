package com.gdxsoft.easyweb.data.export;

import java.io.File;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;
import org.apache.log4j.Logger;
import org.json.JSONArray;

import com.gdxsoft.easyweb.data.DTColumn;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.Utils;

import jxl.CellView;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.VerticalAlignment;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class ExcelExport implements IExport {
	private int _Max = 10000;
	private WritableSheet _Sheet;
	private WritableWorkbook _WorkBook;
	private String _Lang;
	private JSONArray _WaitMergeCells;
	private static Logger LOGGER = Logger.getLogger(ExcelExport.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.data.export.IExport#Export(java.sql.ResultSet,
	 * java.lang.String)
	 */
	public ExcelExport(String lang) {
		this._Lang = lang;
	}

	public ExcelExport() {
	}

	public File export(ResultSet rs, String destXlsName) throws Exception {
		File f = new File(destXlsName);
		String id = Utils.getGuid();
		File f1 = new File(f.getParent() + "/" + id);
		f1.mkdirs();

		this._Max = 5000;
		ResultSetMetaData md = rs.getMetaData();
		int n = _Max;
		int m = 1;
		try {
			while (n == _Max) {
				String index = "000" + m;
				index = index.substring(index.length() - 4);
				File f2 = new File(f1.getAbsolutePath() + "/data_" + index + ".xls");
				String stName = (m - 1) * _Max + 1 + "-" + m * _Max;
				// 为了避免内存溢出，每5000行数据创建一个 Excel 文件
				this.createTemplateExcel(rs, f2.getAbsolutePath(), stName);
				n = this.createTmpSheet(f, rs, md);
				m++;
				if (m > 1000) {
					// 创建超过 1000个文件 1000x5000 条数据
					break;
				}
			}
		} catch (Exception e) {
			LOGGER.error(e);
			throw e;
		} finally {
		}
		String name = UFile.zipPath(f1.getAbsolutePath());
		File[] fs = f1.listFiles();
		for (int i = 0; i < fs.length; i++) {
			fs[i].delete();
		}
		f1.delete();
		return new File(name);
	}

	/**
	 * 创建Excel
	 * 
	 * @param f  文件
	 * @param rs 结果集
	 * @param md 结果集的MetaData
	 * @return
	 * @throws Exception
	 */
	private int createTmpSheet(File f, ResultSet rs, ResultSetMetaData md) throws Exception {
		int len = md.getColumnCount();
		int rowIndex = 1; // 第几行
		int records = 0; // 写人的行数
		while (rs.next()) {
			for (int i = 1; i <= len; i++) {
				String type = md.getColumnTypeName(i).toUpperCase();
				this.writeToSheet(rs.getObject(i), type, i - 1, rowIndex);
			}
			rowIndex++;
			records++;
			if (records == _Max) { // 写人的行数超过最大定义
				break;
			}
		}
		_WorkBook.write();
		_WorkBook.close();
		return records;
	}

	/**
	 * 将数据写入单元格
	 * 
	 * @param v1       数据
	 * @param type     类型
	 * @param colIndex 列编号
	 * @param rowIndex 行编号
	 * @throws RowsExceededException
	 * @throws WriteException
	 */
	private void writeToSheet(Object v1, String type, int colIndex, int rowIndex)
			throws RowsExceededException, WriteException, Exception {
		if (v1 == null)
			return;
		if (type.indexOf("NUM") >= 0 || type.indexOf("INT") >= 0 || type.equalsIgnoreCase("MONEY")) {
			String ori_val = v1.toString();
			double ff = Double.parseDouble(ori_val);
			jxl.write.Number num = new jxl.write.Number(colIndex, rowIndex, ff);
			this._Sheet.addCell(num);
		} else if (type.indexOf("DATE") >= 0 || type.indexOf("TIME") >= 0) {
			java.util.Date t1 = (java.util.Date) v1;

			if (this._Lang == null) {
				// 增加8小时，避免时区出问题
				long newTime = t1.getTime() + 1000 * 60 * 60 * 8;
				t1.setTime(newTime);
				jxl.write.DateTime dt = new jxl.write.DateTime(colIndex, rowIndex, t1);
				this._Sheet.addCell(dt);
			} else {
				String d = com.gdxsoft.easyweb.utils.UFormat.formatDate("datetime", t1, this._Lang);
				if (d.indexOf(" 00:00:00") > 0) {
					d = d.substring(0, d.indexOf(" "));
				}
				Label lbl = new Label(colIndex, rowIndex, d);
				this._Sheet.addCell(lbl);
			}
		} else {
			Label lbl = new Label(colIndex, rowIndex, v1.toString());
			this._Sheet.addCell(lbl);
		}
	}

	/**
	 * 创建XLS文件，同时创建标题行
	 * 
	 * @param rs        数据库返回结果集
	 * @param tmpPath   临时路径
	 * @param sheetName XLS 文件的 sheet名称
	 * @throws Exception
	 */
	private void createTemplateExcel(ResultSet rs, String tmpPath, String sheetName) throws Exception {
		File f = new File(tmpPath);
		WritableWorkbook workbook = Workbook.createWorkbook(f);
		// sheet
		WritableSheet sheet = workbook.createSheet(sheetName, 0);
		ResultSetMetaData md = rs.getMetaData();
		for (int i = 1; i <= md.getColumnCount(); i++) {
			String name = md.getColumnName(i);
			if (md.getColumnLabel(i) != null) {
				name = md.getColumnLabel(i);
			}
			// 创建标题行单元格标题
			Label lbl = new Label(i - 1, 0, name);
			sheet.addCell(lbl);
		}
		this._Sheet = sheet;
		this._WorkBook = workbook;
	}

	/**
	 * 导出Excel 数据
	 */
	public File export(DTTable table, String destXlsName) throws Exception {
		File f = new File(destXlsName);
		String id = Utils.getGuid();
		File f1 = new File(f.getParent() + "/" + id);
		f1.mkdirs();

		List<DTColumn> listColumns = new ArrayList<DTColumn>();
		for (int i = 0; i < table.getColumns().getCount(); i++) {
			DTColumn col = table.getColumns().getColumn(i);
			if (col.isHidden()) { // 字段是否隐藏
				continue;
			}
			listColumns.add(col);
		}

		Collections.sort(listColumns, new Comparator<DTColumn>() {
			@Override
			public int compare(DTColumn s1, DTColumn s2) {
				// 升序
				return s1.getOrder() - s2.getOrder();
			}
		});

		this._Max = 5000;
		int writeRows = _Max;
		int m = 1;
		try {
			while (writeRows == _Max) {
				String index = "000" + m;
				index = index.substring(index.length() - 4);
				File f2 = new File(f1.getAbsolutePath() + "/data_" + index + ".xls");
				String stName = (m - 1) * _Max + 1 + "-" + m * _Max;
				this.createTemplateExcel(listColumns, f2.getAbsolutePath(), stName);
				writeRows = this.createTmpSheet(listColumns, table, (m - 1) * _Max);
				m++;
				if (m > 1000) {
					break;
				}
			}
		} catch (Exception e) {
			LOGGER.error(e);
			throw e;
		} finally {
		}
		String name = UFile.zipPath(f1.getAbsolutePath());
		File[] fs = f1.listFiles();
		for (int i = 0; i < fs.length; i++) {
			fs[i].delete();
		}
		f1.delete();
		return new File(name);
	}

	/**
	 * 写Excel数据
	 * 
	 * @param table
	 * @param start 开始数据
	 * @return
	 * @throws Exception
	 */
	private int createTmpSheet(List<DTColumn> listColumns, DTTable table, int start) throws Exception {
		int writeRows = 0;
		for (int m = start; m < table.getCount(); m++) {
			writeRows++;
			if (writeRows == _Max) {
				_WorkBook.write();
				_WorkBook.close();
				return writeRows;
			}
			// 因为第一行是标题，因此数据行+1
			int rowIndex = m + 1;
			for (int i = 0; i < listColumns.size(); i++) {
				DTColumn col = listColumns.get(i);
				String type = col.getTypeName().toUpperCase();
				Object val = table.getCell(m, col.getName()).getValue();
				this.writeToSheet(val, type, i, rowIndex);
			}
		}
		_WorkBook.write();
		_WorkBook.close();
		return writeRows;
	}

	/**
	 * 创建 Excel文件
	 * 
	 * @param tmpPath
	 * @param sheetName
	 * @throws Exception
	 */
	private void createTemplateExcel(List<DTColumn> listColumns, String tmpPath, String sheetName) throws Exception {
		File f = new File(tmpPath);
		// 创建Excel文件
		WritableWorkbook workbook = Workbook.createWorkbook(f);
		// 创建 Sheet
		WritableSheet sheet = workbook.createSheet(sheetName, 0);

		// 写第一行标题
		for (int i = 0; i < listColumns.size(); i++) {
			DTColumn col = listColumns.get(i);
			String des = col.getDescription();
			String name = col.getName();

			String title;
			if (des != null && des.trim().length() > 0) {
				title = des;
			} else {
				title = name;
			}

			Label lbl = new Label(i, 0, title);
			sheet.addCell(lbl);
		}
		this._Sheet = sheet;
		this._WorkBook = workbook;
	}

	/**
	 * json导出到Excel
	 * json格式{"DATA":[{{"0":{"V":"aaa"}},{"1":{"V":"BBB"}}},{{"0":{"V":"143214"}},{"1":{"V":"24234"}}}]
	 * ,"HEADER":[{{"0":{"V":"KKKK","ROWS":3}},{"1":{"V":"SSSS","COLS":2}}},{{"0":{"V":""}},{"1":{"V":"DFDSAFSAD"}}}]}
	 * 单元格中的ROWS代表合并几行，COLS代表合并几列
	 * 
	 * @param json
	 * @param destXlsName
	 * @return
	 * @throws Exception
	 */
	public File export(JSONObject json, String destXlsName) throws Exception {
		File f = new File(destXlsName);
		String id = Utils.getGuid();
		File f1 = new File(f.getParent() + "/" + id);
		f1.mkdirs();

		this._WaitMergeCells = new JSONArray();
		this._Max = 5000;
		int n = _Max;
		int m = 1;
		try {
			while (n == _Max) {
				String index = "000" + m;
				index = index.substring(index.length() - 4);
				File f2 = new File(f1.getAbsolutePath() + "/data_" + index + ".xls");
				String stName = (m - 1) * _Max + 1 + "-" + m * _Max;
				int header = this.createTemplateExcel(json, f2.getAbsolutePath(), stName);
				n = this.createTmpSheet(f, json, (m - 1) * _Max, header);
				m++;
				if (m > 1000) {
					break;
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {

		}
		String name = UFile.zipPath(f1.getAbsolutePath());
		File[] fs = f1.listFiles();
		for (int i = 0; i < fs.length; i++) {
			fs[i].delete();
		}
		f1.delete();
		return new File(name);
	}

	private int createTmpSheet(File f, JSONObject json, int start, int header) throws Exception {
		int n = header;
		JSONArray data = json.getJSONArray("DATA");
		for (int m = start; m < data.length(); m++) {
			JSONObject d = data.getJSONObject(m);
			Iterator<?> it = d.keys();
			while (it.hasNext()) {
				String key = (String) it.next();
				JSONObject cell = d.getJSONObject(key);
				String v = cell.optString("V");
				int i = Integer.parseInt(key);
				WritableFont wtf2 = new WritableFont(WritableFont.createFont("Times"), 12, WritableFont.NO_BOLD, false);
				WritableCellFormat wcfmt2 = new WritableCellFormat(wtf2);

				wcfmt2.setVerticalAlignment(VerticalAlignment.TOP);
				if (v.indexOf("\n") >= 0) {
					wcfmt2.setWrap(true);
				}
				putCells(i, n, cell);

				Label lbl = new Label(i, n, v, wcfmt2);
				this._Sheet.addCell(lbl);
			}
			n++;
			if (n == _Max) {
				this.mergeCells();
				_WorkBook.write();
				_WorkBook.close();
				return n;
			}
		}
		this.mergeCells();
		_WorkBook.write();
		_WorkBook.close();
		return n;
	}

	private int createTemplateExcel(JSONObject json, String tmpPath, String sheetName) throws Exception {
		File f = new File(tmpPath);
		WritableWorkbook workbook = Workbook.createWorkbook(f);
		WritableSheet sheet = workbook.createSheet(sheetName, 0);
		int m = 0;
		if (json.has("HEADER")) {
			JSONArray data = json.getJSONArray("HEADER");
			JSONObject col_width = new JSONObject();
			for (; m < data.length(); m++) {
				JSONObject d = data.getJSONObject(m);
				Iterator<?> it = d.keys();
				while (it.hasNext()) {
					String key = (String) it.next();
					JSONObject cell = d.getJSONObject(key);
					String v = cell.optString("V");
					int i = Integer.parseInt(key);
					WritableFont wtf2 = new WritableFont(WritableFont.createFont("Times"), 12, WritableFont.BOLD,
							false);
					WritableCellFormat wcfmt2 = new WritableCellFormat(wtf2);
					if (v.indexOf("\n") >= 0) {
						wcfmt2.setWrap(true);
					}
					wcfmt2.setAlignment(Alignment.CENTRE);
					putCells(i, m, cell);

					Label lbl = new Label(i, m, v, wcfmt2);
					sheet.addCell(lbl);

					int len = v.length();
					if (!col_width.has(key)) {
						col_width.put(key, len);
					} else {
						int len_last = col_width.getInt(key);
						if (len_last < len) {
							col_width.put(key, len);
						}
					}
				}
			}
			// 强制设置表头宽度
			Iterator<?> it2 = col_width.keys();
			while (it2.hasNext()) {
				String key = (String) it2.next();
				int idx = Integer.parseInt(key);
				int len = col_width.getInt(key) * 2;
				if (len > 0) {
					if (len > 100) {
						len = 100;
					}
					sheet.setColumnView(idx, len);
				} else {
					CellView cellView = new CellView();
					cellView.setAutosize(true);
					sheet.setColumnView(idx, cellView);
				}
			}
		}
		this._Sheet = sheet;
		this._WorkBook = workbook;
		return m;
	}

	private boolean putCells(int colIdx, int rowIdx, JSONObject cell) {
		if (!cell.has("ROWS") && !cell.has("COLS")) {
			return false;
		}
		if (rowIdx > this._Max) {
			return false;
		}
		cell.put("COL_IDX", colIdx);
		cell.put("ROW_IDX", rowIdx);
		this._WaitMergeCells.put(cell);
		return true;
	}

	private void mergeCells() throws Exception {
		if (this._WaitMergeCells.length() == 0) {
			return;
		}
		for (int i = 0; i < this._WaitMergeCells.length(); i++) {
			JSONObject cell = this._WaitMergeCells.getJSONObject(i);
			int colIdx = cell.getInt("COL_IDX");
			int rowIdx = cell.getInt("ROW_IDX");
			int rows = cell.optInt("ROWS", 0);
			int rowEndIdx = rowIdx + (rows == 0 ? 0 : rows - 1);
			int cols = cell.optInt("COLS", 0);
			int colEndIdx = colIdx + (cols == 0 ? 0 : cols - 1);
			this._Sheet.mergeCells(colIdx, rowIdx, colEndIdx, rowEndIdx);
		}
	}
}
