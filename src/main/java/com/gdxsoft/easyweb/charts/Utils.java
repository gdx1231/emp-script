package com.gdxsoft.easyweb.charts;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;



public class Utils {
	public static String PIE_LABLE_NAME = "__EMP_CHART_LABLE";

	public static String PIE_DATA_NAME = "__EMP_CHART_DATA";

	public static String PIE_IMAGE_SIZE = "__EMP_CHART_SIZE";

	public static String PIE_SERIES_NAME = "__EMP_CHART_SERIES";

	public static String getDispalyOptions(String v1) {
		String v = "0,1,2";
		String t = "仅表,图表,仅图";
		return com.gdxsoft.easyweb.utils.Utils.getOptions(v, t, v1);
	}

	public static String getChartTypeOptions(String v1) {
		String v = "PIE,PIE3D,COLUMN,COLUMN3D,COLUMN_STACKED,COLUMN_STACKED3D,BAR,BAR3D,BAR_STACKED,BAR_STACKED3D";
		return com.gdxsoft.easyweb.utils.Utils.getOptions(v, v, v1.toUpperCase());
	}

	/**
	 * 保存图片
	 * @param chartName 文件名
	 * @param jfc 图像
	 * @param width 宽度
	 * @param height 高度
	 * @return 是否成功
	 */
	public static boolean saveChart(String chartName, JFreeChart jfc,
			int width, int height) {
		try {
			FileOutputStream fos = new FileOutputStream(chartName);
			try {
				ChartUtilities.writeChartAsPNG(fos, jfc, width, height);
				fos.close();
				return true;
			} catch (IOException e) {
				System.out.println(e.getMessage());
				return false;
			}

		} catch (FileNotFoundException e) {
			System.err.println(e.getMessage());
			return false;
		}
	}

	/**
	 * 根据参数获取图像尺寸
	 * 
	 * @param size
	 *            例如400,200
	 * @return 尺寸数组 0-width,1-height
	 */
	public static int[] getImageSize(String size) {
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
		int[] mm = new int[2];
		mm[0] = w;
		mm[1] = h;
		return mm;
	}


	

}
