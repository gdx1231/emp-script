package com.gdxsoft.easyweb.charts;

import java.util.Enumeration;
import java.util.Vector;

import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import com.gdxsoft.easyweb.data.DTRow;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValue;
import com.gdxsoft.easyweb.script.userConfig.UserXItems;

public class Charts {

	private Vector<IPie> _Pies = new Vector<IPie>();
	private Vector<ICategory> _Categories = new Vector<ICategory>();

	public void init(UserXItems userCharts) throws Exception {
		for (int i = 0; i < userCharts.count(); i++) {
			UserXItem o = userCharts.getItem(i);

			UserXItemValue cs = o.getItem("ChartSize").getItem(0);
			int[] cs1 = this.getSize(cs);

			UserXItemValue dm = o.getItem("DataMap").getItem(0);
			String[] dm1 = this.getDataMap(dm);

			String chartType = o.getSingleValue("ChartType").toUpperCase();
			if (chartType.substring(0, 3).equals("PIE")) {
				IPie p = null;
				if (chartType.equals("PIE")) {
					p = new Pie();
				} else {
					p = new Pie3D();
				}
				p.setSize(cs1[0], cs1[1]);

				p.setLabelDataName(dm1[0]);
				p.setChartDataName(dm1[2]);

				DefaultPieDataset defaultPieDataset = new DefaultPieDataset();
				p.setData(defaultPieDataset);
				p.setChartType(chartType);
				this._Pies.add(p);
			} else {
				ICategory c = null;
				if (chartType.equals("LINE")) {
					c = new Line();
				} else if (chartType.equals("COLUMN")) {
					c = new Column();
				} else if (chartType.equals("COLUMN3D")) {
					c = new Column3D();
				} else if (chartType.equals("COLUMNSTACKED")) {
					c = new ColumnStacked();
				} else if (chartType.equals("COLUMNSTACKED3D")) {
					c = new ColumnStacked3D();
				} else if (chartType.equals("BAR")) {
					c = new Bar();
				} else if (chartType.equals("BAR3D")) {
					c = new Bar3D();
				} else if (chartType.equals("BARSTACKED")) {
					c = new BarStacked();
				} else if (chartType.equals("BARSTACKED3D")) {
					c = new BarStacked3D();
				} else {
					c = new Bar();
				}
				c.setSize(cs1[0], cs1[1]);

				c.setLabelDataName(dm1[0]);
				c.setSeriesDataName(dm1[1]);
				c.setChartDataName(dm1[2]);

				DefaultCategoryDataset catagoryDataset = new DefaultCategoryDataset();
				c.setData(catagoryDataset);

				c.setChartType(chartType);

				this._Categories.add(c);
			}
		}
	}

	public void setChartData(DTTable table) {
		this.setPiesData(table);
		this.setCatagoriesData(table);
	}

	private void setCatagoriesData(DTTable table) {
		Enumeration<ICategory> b = this._Categories.elements();
		while (b.hasMoreElements()) {
			ICategory b0 = b.nextElement();
			StringBuilder s1 = new StringBuilder();
			s1.append("CHART_TYPE=" + b0.getChartType() + "&WIDTH="
					+ b0.getImageWidth() + "&HEIGHT=" + b0.getImageHeight());
			for (int i = 0; i < table.getCount(); i++) {
				DTRow row = table.getRow(i);
				String key;
				double val;
				String ser;
				try {
					key = row.getCell(b0.getLabelDataName()).getValue()
							.toString();
				} catch (Exception e) {
					key = e.getMessage();
				}
				try {
					val = Double.parseDouble(row.getCell(b0.getChartDataName())
							.getValue().toString());
				} catch (Exception e) {
					val = 0;
				}
				if (b0.getSeriesDataName() == null
						|| b0.getSeriesDataName().trim().length() == 0) {
					ser = "";
				} else {
					try {
						ser = row.getCell(b0.getSeriesDataName()).getValue()
								.toString();
					} catch (Exception e) {
						ser = e.getMessage();
					}
				}
				s1.append("&A=" + key + "&B=" + val + "&C=" + ser + "哈哈\r\n");
				b0.getDefaultCategoryDataset().addValue(val, key, ser);
			}
			b0.setChartName(s1.toString().hashCode() + "");
		}
	}

	private void setPiesData(DTTable table) {
		Enumeration<IPie> a = this._Pies.elements();
		while (a.hasMoreElements()) {
			IPie a0 = a.nextElement();
			StringBuilder s1 = new StringBuilder();
			s1.append("CHART_TYPE=" + a0.getChartType() + "&WIDTH="
					+ a0.getImageWidth() + "&HEIGHT=" + a0.getImageHeight());

			for (int i = 0; i < table.getCount(); i++) {
				DTRow row = table.getRow(i);
				String key;
				double val = 0;
				try {
					key = row.getCell(a0.getLabelDataName()).getValue()
							.toString();
				} catch (Exception e) {
					key = e.getMessage();
				}
				try {
					val = Double.parseDouble(row.getCell(a0.getChartDataName())
							.getValue().toString());
				} catch (Exception e) {
					val = 0;
				}
				a0.getDefaultPieDataset().setValue(key, val);
				s1.append("&A=" + key + "&B=" + val + "哈哈\r\n");
			}
			a0.setChartName(s1.toString().hashCode() + "");
		}
	}

	private String[] getDataMap(UserXItemValue dm) {
		String[] map = new String[3];
		try {
			map[0] = dm.getItem("LabelData");
			map[1] = dm.getItem("SeriesData");
			map[2] = dm.getItem("ChartData");
			return map;
		} catch (Exception e) {
			return null;
		}

	}

	private int[] getSize(UserXItemValue cs) {
		int iw = 400, ih = 300;
		try {
			String sw = cs.getItem("ChartWidth");
			iw = Integer.parseInt(sw);
		} catch (Exception e) {
		}
		try {
			String sh = cs.getItem("ChartHeight");
			ih = Integer.parseInt(sh);
		} catch (Exception e) {
		}
		if (iw > 2000) {
			iw = 400;
		}
		if (ih > 2000) {
			ih = 300;
		}
		int[] a = { iw, ih };
		return a;
	}

	/**
	 * @return the _Pies
	 */
	public Vector<IPie> getPies() {
		return _Pies;
	}

	/**
	 * @return the _Categories
	 */
	public Vector<ICategory> getCategories() {
		return _Categories;
	}
}
