/**
 * 
 */
package com.gdxsoft.easyweb.script;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * @author guolei
 * 
 */
public class ValidCode {

	public static String SESSION_NAME = "_EWA_VAILD_CODE_SESSION";

	private String _RandomNumber;

	private Random _Random;

	public ValidCode() {
		this._Random = new Random();
	}

	public BufferedImage createCode() {
		// 在内存中创建图象
		int width = 60, height = 20;
		BufferedImage image = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		// 获取图形上下文
		Graphics g = image.getGraphics();
		// 生成随机类
		Random random = _Random;
		// 设定背景色
		g.setColor(getRandColor(200, 250));
		g.fillRect(0, 0, width, height);
		// 设定字体
		g.setFont(new Font("Times New Roman", Font.PLAIN, 18));

		// 随机产生155条干扰线，使图象中的认证码不易被其它程序探测到
		g.setColor(getRandColor(160, 200));
		for (int i = 0; i < 15; i++) {
			int x = random.nextInt(width);
			int y = random.nextInt(height);
			int xl = random.nextInt(12);
			int yl = random.nextInt(12);
			g.drawLine(x, y, x + xl, y + yl);
		}
		// 取随机产生的认证码(4位数字)
		String sRand = this.getRandNumber(4);
		for (int i = 0; i < sRand.length(); i++) {
			String rand = sRand.substring(i, i + 1);
			// 将认证码显示到图象中
			g.setColor(new Color(20 + random.nextInt(110), 20 + random
					.nextInt(110), 20 + random.nextInt(110)));// 调用函数出来的颜色相同，可能是因为种子太接近，所以只能直接生成
			g.drawString(rand, 13 * i + 6, 16);
		}
		// 图象生效
		g.dispose();
		return image;
	}

	private String getRandNumber(int len) {
		// 取随机产生的认证码(4位数字)
		String sRand = "";
		Random random = _Random;
		for (int i = 0; i < 4; i++) {
			String rand = String.valueOf(random.nextInt(10));
			sRand += rand;
		}
		this._RandomNumber = sRand;
		return sRand;
	}

	private Color getRandColor(int fc, int bc) {// 给定范围获得随机颜色
		Random random = new Random();
		if (fc > 255)
			fc = 255;
		if (bc > 255)
			bc = 255;
		int r = fc + random.nextInt(bc - fc);
		int g = fc + random.nextInt(bc - fc);
		int b = fc + random.nextInt(bc - fc);
		return new Color(r, g, b);
	}

	/**
	 * @return the _RandomNumber
	 */
	public String getRandomNumber() {
		return _RandomNumber;
	}
}
