package com.gdxsoft.easyweb.utils.fileConvert;

import java.io.File;

import org.apache.log4j.Logger;
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeManager;

import com.gdxsoft.easyweb.utils.UPath;

public class OpenOfficeInstance {

	private static Logger LOGGER = Logger.getLogger(OpenOfficeInstance.class);

	public static OfficeManager officeManager;
	public static int port[] = { 8100 };
	public static boolean SERVICE_START = false;

	public synchronized static void startService() {
		DefaultOfficeManagerConfiguration configuration = new DefaultOfficeManagerConfiguration();
		try {
			configuration.setOfficeHome(UPath.getCVT_OPENOFFICE_HOME());// 设置OpenOffice.org安装目录
			configuration.setPortNumbers(port); // 设置转换端口，默认为8100
			configuration.setTaskExecutionTimeout(1000 * 60 * 5L);// 设置任务执行超时为5分钟
			configuration.setTaskQueueTimeout(1000 * 60 * 60 * 24L);// 设置任务队列超时为24小时

			officeManager = configuration.buildOfficeManager();
			officeManager.start(); // 启动服务

			LOGGER.info("Start - " + UPath.getCVT_OPENOFFICE_HOME());

			SERVICE_START = true;
		} catch (Exception ce) {
			// 寻找soffice.bin时，自动添加program文件夹。如果再配置成D:\\Program Files (x86)\\OpenOffice
			// 4\\program，则它在验证是否有soffice.bin文件时。
			// 实际路径为：D:\\Program Files (x86)\\OpenOffice
			// 4\\program\\program\\soffice.bin。所以报错。

			LOGGER.error("office转换服务启动失败!详细信息:" + ce);
			LOGGER.error(UPath.getCVT_OPENOFFICE_HOME());
			LOGGER.error(ce);
			SERVICE_START = false;
		}
	}

	public synchronized static void stopService() {
		if (officeManager != null && SERVICE_START) {
			officeManager.stop();
			SERVICE_START = false;
		}
	}

	/**
	 * 转换文件格式
	 * 
	 * @param inputFile 来源文件
	 * @param outFile   输出文件
	 */
	public static void convert(File inputFile, File outFile) {
		if (!SERVICE_START) {
			startService();
		}
		OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);
		long t0 = System.currentTimeMillis();
		LOGGER.info("CVT " + inputFile.getAbsolutePath() + " -> " + outFile.toString());
		converter.convert(inputFile, outFile);
		long t1 = System.currentTimeMillis();

		long span = t1 - t0;
		LOGGER.info("CVT 完毕(" + span + "ms)");
	}

	/**
	 * 转换文件格式
	 * 
	 * @param inputFilePath 来源文件路径
	 * @param outFilePath   输出文件路径
	 */
	public void convert(String inputFilePath, String outFilePath) {
		convert(new File(inputFilePath), new File(outFilePath));
	}
}
