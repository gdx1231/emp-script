package com.gdxsoft.easyweb.utils;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.RoundRectangle2D.Float;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.ImageIcon;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.log4j.Logger;
import net.coobird.thumbnailator.Thumbnails;

public class UImages {
	private static Logger LOGGER = Logger.getLogger(UImages.class);

	/**
	 * 图片附加 带圆角和描边的logo
	 * 
	 * @param originalImage 原始图片
	 * @param logo          logo图片
	 * @param logoMaxWidth  最大宽度
	 * @param logoMaxHeight 最大高度
	 * @return
	 * @throws IOException
	 */
	public static BufferedImage appendLogo(BufferedImage originalImage, BufferedImage logo, int logoMaxWidth, int logoMaxHeight)
			throws IOException {

		Graphics2D g = originalImage.createGraphics();

		int[] newSize = getNewSize(logo, logoMaxWidth, logoMaxHeight);

		int logo_width = newSize[0];
		int logo_height = newSize[0];

		int logo_x = (originalImage.getWidth() - logo_width) / 2;
		int logo_y = (originalImage.getHeight() - logo_height) / 2;

		// 缩放logo尺寸和二维码logo要求尺寸一致
		BufferedImage logoResized = UImages.createResizedCopy(logo, logo_width, logo_height);

		int radius = logoResized.getWidth() * 40 / 200; // 200宽度 30圆角
		int border = logoResized.getWidth() * 2 / 100;

		// 创建带圆角和描边的图片
		BufferedImage logoWidthRadius = setRadius(logoResized, radius, border, radius / 2);

		// 创建半透明的背景图
		BufferedImage logoBack = createBackground(logoResized, radius, border, radius / 2);

		int back_diff = UConvert.ToInt32(String.valueOf(logo_height * 0.05));
		// 绘制半透明背景
		g.drawImage(logoBack, logo_x + back_diff, logo_y + back_diff, logo_width, logo_height, null);
		// 开始绘制logo图片
		g.drawImage(logoWidthRadius, logo_x, logo_y, logo_width, logo_height, null);
		g.dispose();
		logo.flush();

		return originalImage;
	}

	/**
	 * 图片设置圆角
	 * 
	 * @param srcImage
	 * @param radius
	 * @param border
	 * @param padding
	 * @return
	 * @throws IOException
	 */
	public static BufferedImage setRadius(BufferedImage srcImage, int radius, int border, int padding) throws IOException {
		int width = srcImage.getWidth();
		int height = srcImage.getHeight();
		int canvasWidth = width + padding * 2;
		int canvasHeight = height + padding * 2;

		BufferedImage image = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);

		Graphics2D gs = image.createGraphics();

		gs.setComposite(AlphaComposite.Src);

		gs.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Color col = new Color(255, 255, 255, 255);
		gs.setColor(col); // Color.WHITE

		int out_radius = UConvert.ToInt32(String.valueOf(radius * canvasWidth * 1.2 / width));
		Float fill = new RoundRectangle2D.Float(0, 0, canvasWidth, canvasHeight, out_radius, out_radius);
		gs.fill(fill);
		gs.setComposite(AlphaComposite.SrcAtop);

		gs.drawImage(setClip(srcImage, radius), padding, padding, null);
		if (border != 0) {
			gs.setColor(Color.LIGHT_GRAY);
			gs.setStroke(new BasicStroke(border));
			gs.drawRoundRect(padding, padding, canvasWidth - 2 * padding, canvasHeight - 2 * padding, radius, radius);
		}
		gs.dispose();
		return image;
	}

	public static BufferedImage createBackground(BufferedImage srcImage, int radius, int border, int padding) {
		int width = srcImage.getWidth();
		int height = srcImage.getHeight();
		int canvasWidth = width + padding * 2;
		int canvasHeight = height + padding * 2;

		BufferedImage image = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);

		Graphics2D gs = image.createGraphics();

		gs.setComposite(AlphaComposite.Src);

		gs.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Color col = new Color(15, 15, 15, 67);
		gs.setColor(col); // Color.WHITE

		int out_radius = UConvert.ToInt32(String.valueOf(radius * canvasWidth * 1.2 / width));
		Float fill = new RoundRectangle2D.Float(0, 0, canvasWidth, canvasHeight, out_radius, out_radius);
		gs.fill(fill);
		gs.setComposite(AlphaComposite.SrcAtop);

		gs.drawImage(setClip(srcImage, radius), padding, padding, null);
		gs.dispose();
		return image;
	}

	/**
	 * 图片切圆角
	 * 
	 * @param srcImage
	 * @param radius
	 * @return
	 */
	public static BufferedImage setClip(BufferedImage srcImage, int radius) {
		int width = srcImage.getWidth();
		int height = srcImage.getHeight();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D gs = image.createGraphics();

		gs.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		gs.setClip(new RoundRectangle2D.Double(0, 0, width, height, radius, radius));
		gs.drawImage(srcImage, 0, 0, null);
		gs.dispose();
		return image;
	}

	/**
	 * 创建缩率图
	 * 
	 * @param imgPath   图片路径
	 * @param maxwidth  最大宽度
	 * @param maxheight 最大高度
	 * @return 缩率图文件路径
	 */
	public static String createSmallImage(String imgPath, int maxwidth, int maxheight) {
		java.awt.Dimension[] d = new java.awt.Dimension[1];
		d[0] = new java.awt.Dimension();
		d[0].setSize(maxwidth, maxheight);

		try {
			File[] fs = createResized(imgPath, d);
			if (fs.length == 0) {
				return null;
			} else {
				return fs[0].getAbsolutePath();
			}
		} catch (Exception e) {
			LOGGER.error(e);
			return null;
		}

	}

	/**
	 * 用java原生创建缩略图
	 * 
	 * @param imgPath
	 * @param d
	 * @return
	 * @throws Exception
	 */
	public static File[] createResizedByJava(String imgPath, java.awt.Dimension[] d) throws Exception {
		if (!(imgPath.toUpperCase().endsWith(".JPG") || imgPath.toUpperCase().endsWith(".JPEG")
				|| imgPath.toUpperCase().endsWith(".PNG") || imgPath.toUpperCase().endsWith(".BMP")
				|| imgPath.toUpperCase().endsWith(".GIF"))) {
			return new File[0];
		}
		File img = new File(imgPath);
		BufferedImage bufferedImage = null;
		try {
			bufferedImage = getBufferedImage(imgPath);
		} catch (Exception e) {
			LOGGER.error(e);
			throw e;
		}
		File[] names = new File[d.length];
		String path = img.getAbsolutePath() + "$resized";
		File pathResized = new File(path);
		pathResized.mkdirs();

		for (int i = 0; i < d.length; i++) {
			double width = d[i].getWidth();
			double height = d[i].getHeight();
			String name = (int) width + "x" + (int) height + ".jpg";
			File f1 = new File(pathResized.getPath() + "/" + name);

			int[] newSize = getNewSize(bufferedImage, (int) width, (int) height);

			int w = newSize[0];
			int h = newSize[1];
			try {
				BufferedImage buf = createResizedCopy(bufferedImage, w, h);
				imageSave(buf, f1);
				buf = null;
				names[i] = f1;
			} catch (Exception e) {
				LOGGER.error(e);
				names[i] = null;
			}
		}
		if (bufferedImage != null)
			bufferedImage = null;
		return names;
	}

	/**
	 * 根据最大宽度和最大高度的限制条件，获取目标图像的新尺寸。
	 * 
	 * @param bufferedImage 目标图像
	 * @param maxWidth      最大宽度
	 * @param maxHeight     最大高度
	 * @return 宽，高的二维数组
	 */
	public static int[] getNewSize(BufferedImage bufferedImage, int maxWidth, int maxHeight) {
		double wScale = maxWidth * 1.0 / bufferedImage.getWidth();
		double hScale = maxHeight * 1.0 / bufferedImage.getHeight();
		double width = maxWidth, height = maxHeight;
		if (wScale > hScale) {
			width = hScale * bufferedImage.getWidth();
		} else {
			height = bufferedImage.getHeight() * wScale;
		}

		int w = (int) width;
		int h = (int) height;

		int[] rets = { w, h };

		return rets;
	}

	/**
	 * 使用ImageMagick处理缩率图
	 * 
	 * @param imgPath
	 * @param d
	 * @return
	 * @throws Exception
	 */
	public static File[] createResizedByImageMagick(String imgPath, java.awt.Dimension[] d) throws Exception {
		if (!(imgPath.toUpperCase().endsWith(".JPG") || imgPath.toUpperCase().endsWith(".JPEG")
				|| imgPath.toUpperCase().endsWith(".PNG") || imgPath.toUpperCase().endsWith(".BMP")
				|| imgPath.toUpperCase().endsWith(".GIF"))) {
			return new File[0];
		}
		String magicHome = UPath.getCVT_IMAGEMAGICK_HOME();
		if (magicHome == null || magicHome.trim().length() == 0) {
			throw new Exception(
					"magicHome 没有定义（ewa_conf.xml）<path Name=\"cvt_ImageMagick_Home\" Value=\"/usr/local/Cellar/imagemagick/6.9.6-3/bin/\" />");
		}

		String command_line = magicHome + "convert -auto-orient -strip -resize ";

		// convert -resize "100x100>" -strip 360云盘/pics/DSC_0963.JPG aa1.jpg
		File img = new File(imgPath);

		File[] names = new File[d.length];
		String path = img.getAbsolutePath() + "$resized";
		File pathResized = new File(path);
		pathResized.mkdirs();

		for (int i = 0; i < d.length; i++) {
			double width = d[i].getWidth();
			double height = d[i].getHeight();
			int w = (int) width;
			int h = (int) height;
			String name = w + "x" + h + ".jpg";
			File f1 = new File(pathResized.getPath() + "/" + name);

			StringBuilder sb = new StringBuilder();
			sb.append(command_line);
			sb.append(" \"");
			sb.append(w);
			sb.append("x");
			sb.append(h);
			sb.append(">\" \"");
			sb.append(img.getAbsolutePath());
			sb.append("\" \"");
			sb.append(f1);
			sb.append("\"");
			String command_line1 = sb.toString();

			HashMap<String, String> rst = runImageMagick(command_line1);
			if (rst.get("RST").equals("true")) {
				names[i] = f1;
			} else {
				names[i] = null;
			}

		}

		return names;
	}

	/**
	 * 生成新尺寸图片，保持比例
	 * 
	 * @param imgPath
	 * @param d       尺寸列表
	 * @return
	 * @throws Exception
	 */
	public static File[] createResized(String imgPath, java.awt.Dimension[] d) throws Exception {
		String magicHome = UPath.getCVT_IMAGEMAGICK_HOME();
		if (magicHome == null || magicHome.trim().length() == 0) {
			// return createResizedByJava(imgPath, d);
			// 利用 net.coobird.thumbnailator.Thumbnails，
			return createResizedByThumbnails(imgPath, d);
		} else {
			File pathMagicHome = new File(magicHome);
			if (pathMagicHome.exists()) {
				return createResizedByImageMagick(imgPath, d);
			} else {
				LOGGER.warn("magicHome NOT defined in (ewa_conf.xml), using createResizedByThumbnails");
				// 利用 net.coobird.thumbnailator.Thumbnails，
				return createResizedByThumbnails(imgPath, d);
			}
		}
	}

	/**
	 * 利用 net.coobird.thumbnailator.Thumbnails， 生成新尺寸图片，保持比例
	 * 
	 * @param imgPath
	 * @param d       尺寸列表
	 * @return
	 * @throws Exception
	 */
	public static File[] createResizedByThumbnails(String imgPath, java.awt.Dimension[] d) {
		File img = new File(imgPath);
		String path = img.getAbsolutePath() + "$resized";
		File pathResized = new File(path);
		pathResized.mkdirs();
		File[] names = new File[d.length];
		for (int i = 0; i < d.length; i++) {
			double width = d[i].getWidth();
			double height = d[i].getHeight();
			int w = (int) width;
			int h = (int) height;
			String name = w + "x" + h + ".jpg";
			File f1 = new File(pathResized.getPath() + "/" + name);
			try {
				Thumbnails.of(img).size(w, h).outputFormat("jpg").outputQuality(0.7).toFile(f1);
				names[i] = f1;
			} catch (IOException e) {
				LOGGER.error(e);
				names[i] = null;
			}
		}
		return names;
	}

	/**
	 * 读取图片
	 * 
	 * @param imgPath
	 * @return
	 * @throws IOException
	 */
	public static BufferedImage getBufferedImage(String imgPath) throws IOException {
		File img = new File(imgPath);
		// bufferedImage = ImageIO.read(img);
		// java上传图片，压缩、更改尺寸等导致变色（表层蒙上一层红色）
		// https://blog.csdn.net/qq_25446311/article/details/79140008
		Image image = Toolkit.getDefaultToolkit().getImage(img.getAbsolutePath());
		BufferedImage bufferedImage = toBufferedImage(image);
		return bufferedImage;
	}

	/**
	 * 转换为 BufferedImage
	 * 
	 * @param image
	 * @return
	 */
	public static BufferedImage toBufferedImage(Image image) {
		if (image instanceof BufferedImage) {
			return (BufferedImage) image;
		}
		// This code ensures that all the pixels in the image are loaded
		image = new ImageIcon(image).getImage();
		BufferedImage bimage = null;
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		try {
			int transparency = Transparency.OPAQUE;
			GraphicsDevice gs = ge.getDefaultScreenDevice();
			GraphicsConfiguration gc = gs.getDefaultConfiguration();
			bimage = gc.createCompatibleImage(image.getWidth(null), image.getHeight(null), transparency);
		} catch (HeadlessException e) {
			// The system does not have a screen
		}
		if (bimage == null) {
			// Create a buffered image using the default color model
			int type = BufferedImage.TYPE_INT_RGB;
			bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
		}
		// Copy image to buffered image
		Graphics g = bimage.createGraphics();
		// Paint the image onto the buffered image
		g.drawImage(image, 0, 0, null);
		g.dispose();
		return bimage;
	}

	/**
	 * 保存图片(jpeg, 质量 0.8)
	 * 
	 * @param buf
	 * @param name
	 * @throws IOException
	 */
	public static void imageSave(BufferedImage buf, String name) throws IOException {
		File f1 = new File(name);
		imageSave(buf, f1);
	}

	/**
	 * 保存图片(jpeg, 质量 0.8)
	 * 
	 * @param buf
	 * @param f1
	 * @throws IOException
	 */
	public static void imageSave(BufferedImage buf, File f1) throws IOException {
		f1.getParentFile().mkdirs();
		byte[] bytes = getBytes(buf, "JPEG", 0.8f);
		try {
			UFile.createBinaryFile(f1.getAbsolutePath(), bytes, true);
		} catch (Exception err) {
			LOGGER.error(err);
		}
	}

	/**
	 * 将原始图片修改为新尺寸图片
	 * 
	 * @param originalImage 原始图像
	 * @param width         宽度
	 * @param height        高度
	 * @return
	 */
	public static BufferedImage createResizedCopy(Image originalImage, int width, int height) {
		BufferedImage scaledBI = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = scaledBI.createGraphics();

		// 保持Png图片的透明背景属性
		BufferedImage bufIma = g.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
		Graphics2D g1 = bufIma.createGraphics();

		g1.setComposite(AlphaComposite.Src);
		// 高保真缩放
		Image scaled = originalImage.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
		g1.drawImage(scaled, 0, 0, width, height, null);
		g1.dispose();
		return bufIma;
	}

	/**
	 * 裁剪图片
	 * 
	 * @param originalImage 原始图片
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 * @return
	 */
	public static BufferedImage createClipCopy(Image originalImage, int left, int top, int right, int bottom) {
		int width = right - left;
		int height = bottom - top;
		ImageFilter cropFilter = new CropImageFilter(left, top, width, height);
		Image img = Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(originalImage.getSource(), cropFilter));
		BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics g = tag.getGraphics();
		g.drawImage(img, 0, 0, null); // 绘制小图
		g.dispose();
		return tag;
	}

	/**
	 * 获取 BufferedImage的图像二进制
	 * 
	 * @param bi
	 * @param imageType 图像类型JPEG, PNG ...
	 * @return
	 */
	public static byte[] getBytes(BufferedImage bi, String imageType, float quality) {
		ByteArrayOutputStream output = new ByteArrayOutputStream(10240);
		ImageWriter writer = null;
		String formatName = imageType;

		Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName(formatName);
		if (iter.hasNext()) {
			writer = (ImageWriter) iter.next();
		}
		if (writer == null) {
			return null;
		}

		IIOImage iioImage = new IIOImage(bi, null, null);
		ImageWriteParam param = writer.getDefaultWriteParam();
		if (formatName.equalsIgnoreCase("JPEG")) {
			param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			param.setCompressionQuality(quality);
		}

		try {
			// ImageIO.write(bi, imageType, output);
			ImageOutputStream outputStream = ImageIO.createImageOutputStream(output);
			writer.setOutput(outputStream);
			writer.write(null, iioImage, param);
			outputStream.close();
			byte[] buf = output.toByteArray();

			return buf;
		} catch (Exception err) {
			LOGGER.error(err);
			return null;
		} finally {
			try {
				output.close();
			} catch (IOException e) {
				LOGGER.error(e);
			}
			bi = null;
		}
	}

	/**
	 * 命令行执行ImageMagick
	 * 
	 * @param line
	 * @return
	 */
	private static HashMap<String, String> runImageMagick(String line) {
		HashMap<String, String> rst = new HashMap<String, String>();
		rst.put("CMD", line);

		DefaultExecutor executor = new DefaultExecutor();
		int[] exitValues = { 0, 1 };

		executor.setExitValues(exitValues);

		ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);
		executor.setWatchdog(watchdog);

		File dir = new File(UPath.getCVT_IMAGEMAGICK_HOME());
		executor.setWorkingDirectory(dir);
		String line1 = line;
		try {
			line1 = new String(line.getBytes(), "gbk");
		} catch (UnsupportedEncodingException e1) {
			LOGGER.error(e1);
		}
		CommandLine commandLine = CommandLine.parse(line1);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
		executor.setStreamHandler(streamHandler);
		try {
			executor.execute(commandLine);
			String s = outputStream.toString();
			outputStream.close();
			LOGGER.info(s);
			rst.put("RST", "true");
			rst.put("MSG", s);
		} catch (ExecuteException e) {
			LOGGER.error(line);
			LOGGER.error(e);
			rst.put("RST", "false");
			rst.put("ERR", e.getMessage());
		} catch (IOException e) {
			LOGGER.error(line);
			LOGGER.error(e);
			rst.put("RST", "false");
			rst.put("ERR", e.getMessage());
		}

		return rst;
	}
}
