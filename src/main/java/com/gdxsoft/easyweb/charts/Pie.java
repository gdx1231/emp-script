package com.gdxsoft.easyweb.charts;

import java.awt.RenderingHints;
import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import com.gdxsoft.easyweb.script.RequestValue;


public class Pie extends Column implements IPie {
	protected DefaultPieDataset _DefaultPieDataset;

	public Pie(){
		
	}
	
	public Pie(HttpServletRequest request, HttpSession session) {
		_RequestValue = new RequestValue(request, session);
		this.createData();
		Object lbl = _RequestValue.getObject(Utils.PIE_LABLE_NAME
				.toUpperCase());
		Object data = _RequestValue.getObject(Utils.PIE_DATA_NAME
				.toUpperCase());
		if (lbl == null || data == null) {
			return;
		}

		Object size = _RequestValue.getObject(Utils.PIE_IMAGE_SIZE
				.toUpperCase());
		int w = 400;
		int h = 330;
		if (size != null) {
			try {
				String[] s1 = size.toString().split(",");
				w = Integer.parseInt(s1[0]);
				h = Integer.parseInt(s1[1]);
				if (w > 1998) {
					w = 400;
				}
				if (h > 1972) {
					h = 330;
				}

			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		_ImageWidth = w;
		_ImageHeight = h;
		_LabelData = lbl.toString();
		_ChartData = data.toString();
	}

	/**
	 * @param labelData
	 *            标签数据
	 * @param chartData
	 *            图表数据
	 * @param imageWidth
	 *            图片宽度
	 * @param imageHeight
	 *            图片高度
	 */
	public Pie(String labelData, String chartData, int imageWidth,
			int imageHeight) {
		this._LabelData = labelData;
		this._ChartData = chartData;
		this._ImageHeight = imageHeight;
		this._ImageWidth = imageWidth;

	}

	protected void createData() {
		if(this._DefaultPieDataset !=null)
			return;
		this._DefaultPieDataset = new DefaultPieDataset();
		String[] labels = _LabelData.toString().split(",");
		String[] datas = _ChartData.toString().split(",");
		try {
			for (int i = 0; i < labels.length; i++) {
				double b1 = Double.parseDouble(datas[i]);
				this._DefaultPieDataset.setValue(labels[i], b1);
			}
		} catch (Exception e) {
			this._DefaultPieDataset.setValue(e.getMessage(), 100000);
		}
	}

	/* (non-Javadoc)
	 * @see com.gdxsoft.easyweb.charts.IPie#createChart()
	 */
	public String createChart() {
		String chartPath = super.createDataHashCode();
		File f1 = new File(chartPath);
		if (!f1.exists()) {
			//super.createData();
			this.createChart(chartPath);
		}
		return super.getUrl();
	}

	private void createChart(String name) {
		JFreeChart jfc = ChartFactory.createPieChart("",
				this._DefaultPieDataset, true, true, true);
		jfc.getRenderingHints().put(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		PiePlot pp = (PiePlot) jfc.getPlot();
		pp.setNoDataMessage("nodata");
		pp.setExplodePercent(_DefaultPieDataset.getKey(0).toString(), 0.2);
		Utils.saveChart(name, jfc, this._ImageWidth, this._ImageHeight);
	}

	protected String createDataHashCode() {
		
		return super.createDataHashCode();
	}

	/* (non-Javadoc)
	 * @see com.gdxsoft.easyweb.charts.IPie#setData(org.jfree.data.general.DefaultPieDataset)
	 */
	public void setData(DefaultPieDataset defaultPieDataset) {
		this._DefaultPieDataset = defaultPieDataset;
	}

	/* (non-Javadoc)
	 * @see com.gdxsoft.easyweb.charts.IPie#setSize(int, int)
	 */
	public void setSize(int width, int height) {
		super.setSize(width, height);
	}

	/**
	 * @return the _DefaultPieDataset
	 */
	public DefaultPieDataset getDefaultPieDataset() {
		return _DefaultPieDataset;
	}

}
