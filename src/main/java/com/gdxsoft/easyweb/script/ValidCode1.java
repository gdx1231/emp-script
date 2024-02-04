package com.gdxsoft.easyweb.script;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ref http://blog.csdn.net/ruixue0117/article/details/22829557
 * 
 * @author admin
 *
 */
public class ValidCode1 {
	private static Logger LOGGER = LoggerFactory.getLogger(ValidCode1.class);
	public static final String VERIFY_CODES = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
	public static final String VERIFY_NUMBERS = "0123456789";

	public static List<String> FONT_NAMES = new ArrayList<String>();
	private static String fontPath = "validCodeFonts";
	static {
		try {
			initializeFonts();
		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
		}
	}

	/**
	 * Initialized the valid fonts, in the java resources validCodeFonts
	 * 
	 * @throws IOException
	 * @throws FontFormatException
	 */
	public synchronized static void initializeFonts() throws Exception {
		Enumeration<URL> resources = ValidCode1.class.getClassLoader().getResources(fontPath);
		while (resources.hasMoreElements()) {
			URL url = resources.nextElement();
			// 通过判断协议是不是jar文件
			if (url.getProtocol().equals("jar")) {
				JarURLConnection urlConnection = (JarURLConnection) url.openConnection();
				JarFile jarFile = urlConnection.getJarFile();
				Enumeration<JarEntry> entries = jarFile.entries(); // 返回jar中所有的文件目录
				while (entries.hasMoreElements()) {
					JarEntry jarEntry = entries.nextElement();
					initializeFont(jarEntry);
				}
			} else if (url.getProtocol().equals("file")) {
				// 获取class 根目录
				URL resource = ValidCode1.class.getClassLoader().getResource(fontPath);
				File[] files = new File(resource.getPath()).listFiles();
				for (int i = 0; i < files.length; i++) {
					File fontFile = files[i];
					initializeFont(fontFile);
				}
			}
		}
	}

	/**
	 * 添加一个字体
	 * 
	 * @param jarEntry
	 */
	private static void initializeFont(JarEntry jarEntry) {
		String name = jarEntry.getName();
		if (jarEntry.isDirectory() || !name.startsWith(fontPath) || !name.endsWith(".ttf")) { // 是我们需要的文件类型
			return;
		}
		try {
			InputStream resourceAsStream = ValidCode1.class.getClassLoader().getResourceAsStream(jarEntry.getName());
			Font font = Font.createFont(Font.TRUETYPE_FONT, resourceAsStream);
			LOGGER.info("Added font {} from jar {}", font.getFontName(), jarEntry);
			registerFont(font);
		} catch (FontFormatException | IOException e1) {
			LOGGER.warn("Add font {}, {}", jarEntry, e1.getMessage());
		}
	}

	private static void initializeFont(File fontFile) {
		if (fontFile.isDirectory() || !fontFile.getName().endsWith(".ttf")) {
			return;
		}
		try {
			Font font = Font.createFont(Font.TRUETYPE_FONT, fontFile);
			LOGGER.info("Added font {} from File {}", font.getFontName(), fontFile);
			registerFont(font);
		} catch (FontFormatException | IOException e1) {
			LOGGER.warn("Add font {}, {}", fontFile, e1.getMessage());
		}
	}

	private static void registerFont(Font font) {
		GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();
		e.registerFont(font);
		FONT_NAMES.add(font.getName());
	}

	private Random random = new Random(System.currentTimeMillis());
	private boolean _IsNumberCode = true;
	private int _VcLen = 4;
	private String _RandomNumber;

	public String getRandomNumber() {
		return _RandomNumber;
	}

	public ValidCode1() {
	}

	public ValidCode1(int vcLen, boolean isNumberCode) {
		this._VcLen = vcLen;
		this._IsNumberCode = isNumberCode;
	}

	public BufferedImage createCode() {
		int w = (240 / 5) * this._VcLen + 20;
		int h = 80;
		return this.outputVerifyImage(w, h, _VcLen);
	}

	/**
	 * 使用系统默认字符源生成验证码
	 * 
	 * @param verifySize 验证码长度
	 * @return
	 */
	private String generateVerifyCode(int verifySize) {
		if (this._IsNumberCode) {
			return generateVerifyCode(verifySize, VERIFY_NUMBERS);
		} else {
			return generateVerifyCode(verifySize, VERIFY_CODES);
		}
	}

	/**
	 * 使用指定源生成验证码
	 * 
	 * @param verifySize 验证码长度
	 * @param sources    验证码字符源
	 * @return
	 */
	private String generateVerifyCode(int verifySize, String sources) {
		if (sources == null || sources.length() == 0) {
			sources = VERIFY_CODES;
		}
		int codesLen = sources.length();
		Random rand = new Random(System.currentTimeMillis());
		StringBuilder verifyCode = new StringBuilder(verifySize);
		for (int i = 0; i < verifySize; i++) {
			verifyCode.append(sources.charAt(rand.nextInt(codesLen - 1)));
		}
		this._RandomNumber = verifyCode.toString();
		return verifyCode.toString();
	}

	/**
	 * 输出随机验证码图片流,并返回验证码值
	 * 
	 * @param w
	 * @param h
	 * @param os
	 * @param verifySize
	 * @return
	 * @throws IOException
	 */
	public BufferedImage outputVerifyImage(int w, int h, int verifySize) {
		String verifyCode = generateVerifyCode(verifySize);
		return outputImage(w, h, verifyCode);
	}

	/**
	 * 输出指定验证码图片流
	 * 
	 * @param w
	 * @param h
	 * @param os
	 * @param code
	 * @throws IOException
	 */
	public BufferedImage outputImage(int w, int h, String code) {
		int verifySize = code.length();
		BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Random rand = new Random();
		Graphics2D g2 = image.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		Color[] colors = new Color[5];
		Color[] colorSpaces = new Color[] { Color.WHITE, Color.CYAN, Color.GRAY, Color.LIGHT_GRAY, Color.MAGENTA,
				Color.ORANGE, Color.PINK, Color.YELLOW };
		float[] fractions = new float[colors.length];
		for (int i = 0; i < colors.length; i++) {
			colors[i] = colorSpaces[rand.nextInt(colorSpaces.length)];
			fractions[i] = rand.nextFloat();
		}
		Arrays.sort(fractions);

		g2.setColor(Color.GRAY);// 设置边框色
		g2.fillRect(0, 0, w, h);

		Color c = getRandColor(200, 250);
		g2.setColor(c);// 设置背景色
		g2.fillRect(0, 2, w, h - 4);

		// 绘制干扰线
		Random random = new Random();
		g2.setColor(getRandColor(160, 200));// 设置线条的颜色
		for (int i = 0; i < 20; i++) {
			int x = random.nextInt(w - 1);
			int y = random.nextInt(h - 1);
			int xl = random.nextInt(6) + 1;
			int yl = random.nextInt(12) + 1;
			g2.drawLine(x, y, x + xl + 40, y + yl + 20);
		}

		// 添加噪点
		float yawpRate = 0.05f;// 噪声率
		int area = (int) (yawpRate * w * h);
		for (int i = 0; i < area; i++) {
			int x = random.nextInt(w);
			int y = random.nextInt(h);
			int rgb = getRandomIntColor();
			image.setRGB(x, y, rgb);
		}

		shear(g2, w, h, c);// 使图片扭曲

		g2.setColor(getRandColor(100, 160));
		int fontSize = h - 4;

		char[] chars = code.toCharArray();
		// 调用函数出来的颜色相同，可能是因为种子太接近，所以只能直接生成
		g2.setColor(new Color(20 + random.nextInt(110), 20 + random.nextInt(110), 20 + random.nextInt(110)));
		for (int i = 0; i < verifySize; i++) {
			Font font = new Font(getRandFont(), Font.ITALIC, fontSize);
			g2.setFont(font);

			AffineTransform affine = new AffineTransform();
			affine.setToRotation(Math.PI / 4 * rand.nextDouble() * (rand.nextBoolean() ? 1 : -1),
					(w / verifySize) * i + fontSize / 2, h / 2);
			g2.setTransform(affine);
//			g2.setColor(new Color(20 + random.nextInt(110), 20 + random.nextInt(110), 20 + random.nextInt(110)));// 调用函数出来的颜色相同，可能是因为种子太接近，所以只能直接生成
			g2.drawChars(chars, i, 1, ((w - 10) / verifySize) * i + 5, h / 2 + fontSize / 2 - 10);
		}

		g2.dispose();
		return image;
	}

	private String getRandFont() {
		if (FONT_NAMES.size() == 0) {
			try {
				initializeFonts();
			} catch (Exception e) {
				LOGGER.error("Can't initlized fonts. {}", e.getLocalizedMessage());
				return null;
			}
		}
		int index = random.nextInt(FONT_NAMES.size());
		if (index == FONT_NAMES.size()) {
			index = 0;
		}
		return FONT_NAMES.get(index);
	}

	private Color getRandColor(int fc, int bc) {
		if (fc > 255)
			fc = 255;
		if (bc > 255)
			bc = 255;
		int r = fc + random.nextInt(bc - fc);
		int g = fc + random.nextInt(bc - fc);
		int b = fc + random.nextInt(bc - fc);
		return new Color(r, g, b);
	}

	private int getRandomIntColor() {
		int[] rgb = getRandomRgb();
		int color = 0;
		for (int c : rgb) {
			color = color << 8;
			color = color | c;
		}
		return color;
	}

	private int[] getRandomRgb() {
		int[] rgb = new int[3];
		for (int i = 0; i < 3; i++) {
			rgb[i] = random.nextInt(255);
		}
		return rgb;
	}

	private void shear(Graphics g, int w1, int h1, Color color) {
		shearX(g, w1, h1, color);
		shearY(g, w1, h1, color);
	}

	private void shearX(Graphics g, int w1, int h1, Color color) {

		int period = random.nextInt(2);

		boolean borderGap = true;
		int frames = 1;
		int phase = random.nextInt(2);

		for (int i = 0; i < h1; i++) {
			double d = (double) (period >> 1)
					* Math.sin((double) i / (double) period + (6.2831853071795862D * (double) phase) / (double) frames);
			g.copyArea(0, i, w1, 1, (int) d, 0);
			if (borderGap) {
				g.setColor(color);
				g.drawLine((int) d, i, 0, i);
				g.drawLine((int) d + w1, i, w1, i);
			}
		}

	}

	private void shearY(Graphics g, int w1, int h1, Color color) {

		int period = random.nextInt(40) + 10; // 50;

		boolean borderGap = true;
		int frames = 20;
		int phase = 7;
		for (int i = 0; i < w1; i++) {
			double d = (double) (period >> 1)
					* Math.sin((double) i / (double) period + (6.2831853071795862D * (double) phase) / (double) frames);
			g.copyArea(i, 0, 1, h1, 0, (int) d);
			if (borderGap) {
				g.setColor(color);
				g.drawLine(i, (int) d, i, 0);
				g.drawLine(i, (int) d + h1, i, h1);
			}

		}

	}

}
