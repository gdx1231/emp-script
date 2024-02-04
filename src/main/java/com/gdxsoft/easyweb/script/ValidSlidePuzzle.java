package com.gdxsoft.easyweb.script;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Random;

import javax.imageio.ImageIO;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.utils.UConvert;
import com.gdxsoft.easyweb.utils.UImages;
import com.gdxsoft.easyweb.utils.UJSon;

// ref https://blog.51cto.com/u_14230/8798220
public class ValidSlidePuzzle {
	private static Logger logger = LoggerFactory.getLogger(ValidSlidePuzzle.class);

	/**
	 * 默认的图，来源于emp-script-static.jar
	 */
	private static String[] DEF_IMGS = { "/EmpScriptV2/backgrounds/Ubuntu/1526616480990.jpg",
			"/EmpScriptV2/backgrounds/Ubuntu/1526616497815.jpg", "/EmpScriptV2/backgrounds/Ubuntu/1526616508896.jpg",
			"/EmpScriptV2/backgrounds/Ubuntu/152661651342.jpg", "/EmpScriptV2/backgrounds/Ubuntu/1526616517697.jpg",
			"/EmpScriptV2/backgrounds/Ubuntu/1526616521273.jpg", "/EmpScriptV2/backgrounds/Ubuntu/1526616541182.jpg" };

	/**
	 * 默认的图片系列
	 * @return
	 */
	public static String[] getDefImgs() {
		return DEF_IMGS;
	}

	/**
	 * 设置默认的图片系列
	 * @param defImgs
	 */
	public synchronized static void setDefImgs(String[] defImgs) {
		DEF_IMGS = defImgs;
	}

	// 背景图宽度（原图裁剪拼图后的背景图）
	private int bigWidth = 400;
	// 背景图高度
	private int bigHeight = 250;
	// 滑块图宽度（滑块拼图）
	private int smallWidth = 50;
	// 滑块图高度
	private int smallHeight = 50;
	// 小圆半径，即拼图上的凹凸轮廓半径
	private int smallCircle = 8;
	// 小圆距离点
	private int smallCircleR1 = smallCircle / 2;

	/**
	 * 随机坐标Y
	 */
	private int posY;
	/**
	 * 随机坐标X
	 */
	private int posX;

	/**
	 * 滑块图转BASE64字符串
	 */
	private String smallImageBase64;

	/**
	 * 滑块图
	 */
	private BufferedImage smallImage;

	/**
	 * 背景图转BASE64字符串
	 */
	private String bigImageBase64;

	/**
	 * 背景图
	 */
	private BufferedImage bigImage;

	public void randomImg(String[] imgs) throws IOException {
		Random rand = new Random();
		int index = rand.nextInt(0, imgs.length);
		String img = imgs[index];
		URL url = ValidSlidePuzzle.class.getResource(img);

		this.createImage(url);
	}

	/**
	 * 初始化尺寸
	 * 
	 * @param bigWidth
	 * @param bigHeight
	 * @param smallWidth
	 * @param smallHeight
	 */
	public void initSize(int bigWidth, int bigHeight, int smallWidth, int smallHeight) {
		this.bigHeight = bigHeight;
		this.bigWidth = bigWidth;
		this.smallWidth = smallWidth;
		this.smallHeight = smallHeight;
	}

	/**
	 * 生成滑块拼图验证码
	 * 
	 * @param originalImagePath
	 * @throws IOException
	 */
	public void createImage(String originalImagePath) throws IOException {
		BufferedImage originalImage = UImages.getBufferedImage(originalImagePath);
		this.createImage(originalImage);
	}

	/**
	 * 生成滑块拼图验证码
	 * 
	 * @param imgURL
	 * @throws IOException
	 */
	public void createImage(URL imgURL) throws IOException {
		BufferedImage originalImage = UImages.getBufferedImage(imgURL);
		this.createImage(originalImage);
	}

	/**
	 * 生成滑块拼图验证码
	 * 
	 * @param originalImage
	 * @throws IOException
	 */
	public void createImage(BufferedImage originalImage) throws IOException {
		// 规范原图的大小
		BufferedImage bigImage = resizeImage(originalImage, bigWidth, bigHeight, true);

		// 2.随机生成离左上角的(X,Y)坐标，上限为 [bigWidth-smallWidth,
		// bigHeight-smallHeight]。最好离背景图左边远一点，上限不要紧挨着背景图边界
		Random random = new Random();
		int randomX = random.nextInt(bigWidth / 2, bigWidth - smallWidth - smallCircle); // X范围：[2*smallWidth,
		int randomY = random.nextInt(bigHeight - smallHeight - 2 * smallCircle) + smallCircle; // Y范围：[smallCircle,

		logger.info("原图大小：{} x {}，背景图大小：{} x {}，随机生成的坐标：(X,Y)=({},{})", originalImage.getWidth(),
				originalImage.getHeight(), bigImage.getWidth(), bigImage.getHeight(), randomX, randomY);

		// 3.创建滑块图对象
		BufferedImage smallImage = new BufferedImage(smallWidth, smallHeight, BufferedImage.TYPE_4BYTE_ABGR);

		// 4.随机生成拼图轮廓数据
		int[][] slideTemplateData = getSlideTemplateData(smallWidth, smallHeight, smallCircle, smallCircleR1);

		// 5.从背景图中裁剪拼图。抠原图，裁剪拼图
		cutByTemplate(bigImage, smallImage, slideTemplateData, randomX, randomY);

		this.posX = randomX;
		this.posY = randomY;
		this.bigImage = bigImage;
		this.bigImageBase64 = getImageBASE64(bigImage);
		this.smallImage = smallImage;
		this.smallImageBase64 = getImageBASE64(smallImage);

	}

	/**
	 * 获取拼图图轮廓数据
	 * 
	 * @param smallWidth
	 * @param smallHeight
	 * @param smallCircle
	 * @param r1
	 * @return 0和1，其中0表示没有颜色，1有颜色
	 */
	private int[][] getSlideTemplateData(int smallWidth, int smallHeight, int smallCircle, int r1) {
		// 拼图轮廓数据
		int[][] data = new int[smallWidth][smallHeight];

		// 拼图去掉凹凸的白色距离
		int xBlank = smallWidth - smallCircle - smallCircleR1; // 不写smallCircleR1时，凹凸为半圆
		int yBlank = smallHeight - smallCircle - smallCircleR1;

		// 圆的位置
		int rxa = xBlank / 2;
		int ryb = smallHeight - smallCircle;
		double rPow = Math.pow(smallCircle, 2);

		/**
		 * 计算需要的拼图轮廓(方块和凹凸)，用二维数组来表示，二维数组有两张值，0和1，其中0表示没有颜色，1有颜色 圆的标准方程
		 * (x-a)²+(y-b)²=r²,标识圆心（a,b）,半径为r的圆
		 */
		for (int i = 0; i < smallWidth; i++) {
			for (int j = 0; j < smallHeight; j++) {
				// 圆在拼图下方内
				double topR = Math.pow(i - rxa, 2) + Math.pow(j - 2, 2);
				// 圆在拼图下方外
				double downR = Math.pow(i - rxa, 2) + Math.pow(j - ryb, 2);
				// 圆在拼图左侧内 || (i <= xBlank && leftR <= rPow)
				// double leftR = Math.pow(i - 2, 2) + Math.pow(j - rxa, 2);
				// 圆在拼图右侧外
				double rightR = Math.pow(i - ryb, 2) + Math.pow(j - rxa, 2);
				if ((j <= yBlank && topR <= rPow) || (j >= yBlank && downR >= rPow)
						|| (i >= xBlank && rightR >= rPow)) {
					data[i][j] = 0;
				} else {
					data[i][j] = 1;
				}
			}
		}
		return data;
	}

	/**
	 * 裁剪拼图
	 * 
	 * @param bigImage          - 原图规范大小之后的背景图
	 * @param smallImage        - 滑块图
	 * @param slideTemplateData - 拼图轮廓数据
	 * @param x                 - 坐标x
	 * @param y                 - 坐标y
	 */
	private void cutByTemplate(BufferedImage bigImage, BufferedImage smallImage, int[][] slideTemplateData, int x,
			int y) {
		int[][] martrix = new int[3][3];
		int[] values = new int[9];
		// 拼图去掉凹凸的白色距离
		// int xBlank = smallWidth - smallCircle - smallCircleR1; //
		// 不写smallCircleR1时，凹凸为半圆
		int yBlank = smallHeight - smallCircle - smallCircleR1;

		// 创建shape区域，即原图抠图区域模糊和抠出滑块图
		/**
		 * 遍历滑块图轮廓数据,创建shape区域。即原图抠图处模糊和抠出滑块图
		 */
		for (int i = 0; i < smallImage.getWidth(); i++) {
			for (int j = 0; j < smallImage.getHeight(); j++) {
				// 获取背景图中对应位置变色
				// logger.info("随机生成的坐标：(X,Y)=({},{}),（i,j=({},{})，获取原图大小：{} x {}", x, y, i, j,
				// x + i, y + j);
				int rgb_ori = bigImage.getRGB(x + i, y + j);

				// 0和1，其中0表示没有颜色，1有颜色
				int rgb = slideTemplateData[i][j];
				if (rgb == 1) {
					// 设置滑块图中对应位置变色
					smallImage.setRGB(i, j, rgb_ori);

					// 背景图抠图区域高斯模糊
					readPixel(bigImage, x + i, y + j, values);
					fillMatrix(martrix, values);
					bigImage.setRGB(x + i, y + j, avgMatrix(martrix));

					// 边框颜色
					// Color white = new Color(230, 230, 230);
					Color black = new Color(20, 20, 20);
					// 左侧边界，加重高亮阴暗
					if (j < yBlank) {
						bigImage.setRGB(x, y + j, black.getRGB());
						// smallImage.setRGB(0, j, white.getRGB());
					}
				} else {
					// 这里把背景设为透明
					smallImage.setRGB(i, j, rgb_ori & 0x00ffffff);
				}
			}
		}
	}

	/**
	 * 图片转BASE64
	 *
	 * @param image
	 * @return
	 * @throws IOException
	 */
	public String getImageBASE64(BufferedImage image) throws IOException {
		byte[] imagedata = null;
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		ImageIO.write(image, "png", bao);
		imagedata = bao.toByteArray();
		String bas64 = "data:image/png;base64," + UConvert.ToBase64String(imagedata);
		return bas64;
	}

	/**
	 * 改变图片大小
	 *
	 * @param image  原图
	 * @param width  目标宽度
	 * @param height 目标高度
	 * @return 目标图
	 */
	public BufferedImage resizeImage(final Image image, int width, int height, boolean type) {
		BufferedImage bufferedImage;
		if (type) {
			bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		} else {
			bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		}

		final Graphics2D graphics2D = bufferedImage.createGraphics();
		graphics2D.setComposite(AlphaComposite.Src);
		// below three lines are for RenderingHints for better image quality at cost of
		// higher processing time
		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics2D.drawImage(image, 0, 0, width, height, null);
		graphics2D.dispose();
		return bufferedImage;
	}

	private void readPixel(BufferedImage img, int x, int y, int[] pixels) {
		int xStart = x - 1;
		int yStart = y - 1;
		int current = 0;
		for (int i = xStart; i < 3 + xStart; i++) {
			for (int j = yStart; j < 3 + yStart; j++) {
				int tx = i;
				if (tx < 0) {
					tx = -tx;

				} else if (tx >= img.getWidth()) {
					tx = x;
				}
				int ty = j;
				if (ty < 0) {
					ty = -ty;
				} else if (ty >= img.getHeight()) {
					ty = y;
				}
				pixels[current++] = img.getRGB(tx, ty);

			}
		}
	}

	private void fillMatrix(int[][] matrix, int[] values) {
		int filled = 0;
		for (int i = 0; i < matrix.length; i++) {
			int[] x = matrix[i];
			for (int j = 0; j < x.length; j++) {
				x[j] = values[filled++];
			}
		}
	}

	private int avgMatrix(int[][] matrix) {
		int r = 0;
		int g = 0;
		int b = 0;
		for (int i = 0; i < matrix.length; i++) {
			int[] x = matrix[i];
			for (int j = 0; j < x.length; j++) {
				if (j == 1) {
					continue;
				}
				Color c = new Color(x[j]);
				r += c.getRed();
				g += c.getGreen();
				b += c.getBlue();
			}
		}
		return new Color(r / 8, g / 8, b / 8).getRGB();
	}

	/**
	 * 前端用json
	 * 
	 * @return
	 */
	public JSONObject toJsonWeb() {
		JSONObject json = UJSon.rstTrue();
		json.put("imgBig", this.getBigImageBase64());
		json.put("imgSmall", this.getSmallImageBase64());
		json.put("imgTop", this.getPosY());
		json.put("imgBigWidth", this.getBigWidth());
		json.put("imgBigHeight", this.getBigHeight());
		json.put("imgSmallWidth", this.getSmallWidth());
		json.put("imgSmallHeight", this.getSmallHeight());

		return json;
	}

	/**
	 * 系统用json
	 * 
	 * @return
	 */
	public JSONObject toJsonSys() {
		JSONObject json = UJSon.rstTrue();
		json.put("posX", this.getPosX());
		json.put("posY", this.getPosY());
		json.put("imgBigWidth", this.getBigWidth());
		json.put("imgBigHeight", this.getBigHeight());
		json.put("imgSmallWidth", this.getSmallWidth());
		json.put("imgSmallHeight", this.getSmallHeight());
		return json;
	}

	/**
	 * 背景图宽度（原图裁剪拼图后的背景图）
	 * 
	 * @return
	 */
	public int getBigWidth() {
		return bigWidth;
	}

	/**
	 * 背景图宽度（原图裁剪拼图后的背景图）
	 * 
	 * @param bigWidth
	 */
	public void setBigWidth(int bigWidth) {
		this.bigWidth = bigWidth;
	}

	/**
	 * 背景图高度
	 */
	public int getBigHeight() {
		return bigHeight;
	}

	/**
	 * 背景图高度
	 * 
	 * @param bigHeight
	 */
	public void setBigHeight(int bigHeight) {
		this.bigHeight = bigHeight;
	}

	/**
	 * 滑块图宽度（滑块拼图）
	 * 
	 * @return
	 */
	public int getSmallWidth() {
		return smallWidth;
	}

	/**
	 * 滑块图宽度（滑块拼图）
	 * 
	 * @param smallWidth
	 */
	public void setSmallWidth(int smallWidth) {
		this.smallWidth = smallWidth;
	}

	/**
	 * 滑块图高度（滑块拼图）
	 * 
	 * @return
	 */
	public int getSmallHeight() {
		return smallHeight;
	}

	/**
	 * 滑块图高度（滑块拼图）
	 * 
	 * @param smallHeight
	 */
	public void setSmallHeight(int smallHeight) {
		this.smallHeight = smallHeight;
	}

	/**
	 * 小圆半径，即拼图上的凹凸轮廓半径
	 * 
	 * @return
	 */
	public int getSmallCircle() {
		return smallCircle;
	}

	/**
	 * 小圆半径，即拼图上的凹凸轮廓半径
	 * 
	 * @param smallCircle
	 */
	public void setSmallCircle(int smallCircle) {
		this.smallCircle = smallCircle;
		smallCircleR1 = smallCircle / 2;
	}

	public int getSmallCircleR1() {
		return smallCircleR1;
	}

	/**
	 * 随机坐标Y
	 * 
	 * @return
	 */
	public int getPosY() {
		return posY;
	}

	/**
	 * 随机坐标X
	 * 
	 * @return
	 */
	public int getPosX() {
		return posX;
	}

	/**
	 * 滑动图Base64编码
	 * 
	 * @return
	 */
	public String getSmallImageBase64() {
		return smallImageBase64;
	}

	/**
	 * 滑动图
	 * 
	 * @return
	 */
	public BufferedImage getSmallImage() {
		return smallImage;
	}

	/**
	 * 背景图Base64编码
	 * 
	 * @return
	 */
	public String getBigImageBase64() {
		return bigImageBase64;
	}

	/**
	 * 背景图
	 * 
	 * @return
	 */
	public BufferedImage getBigImage() {
		return bigImage;
	}

}
