package com.gdxsoft.easyweb.test;

import java.util.Random;

public class TestBase {

	public void printCaption(String caption) {
		int width = 80;
		int capWidth = this.captionLength(caption);
		int aLen = (width - capWidth - 2) / 2;
		StringBuilder sb = new StringBuilder("\n");
		for (int i = 0; i < aLen; i++) {
			sb.append("-");
		}
		sb.append(" \033[32;1m");
		sb.append(caption);
		sb.append("\033[39;49;0m ");

		int start = capWidth + aLen + 1;
		for (int i = start; i < width; i++) {
			sb.append("-");
		}
		System.out.println(sb);
	}

	public int captionLength(String caption) {
		char[] chars = caption.toCharArray();
		int len = 0;
		for (int i = 0; i < chars.length; i++) {
			byte[] bytes = ("" + chars[i]).getBytes();
			if (bytes.length == 1) {
				len++;
			} else {
				len += 2;
			}
		}

		return len;
	}

	public static void printColor() {
		// 背景颜色代号(41-46)
		// 前景色代号(31-36)
		// 前景色代号和背景色代号可选，就是或可以写，也可以不写
		// 数字+m：1加粗；3斜体；4下划线
		// 格式：System.out.println("\33[前景色代号;背景色代号;数字m");
		Random backgroundRandom = new Random();
		Random fontRandom = new Random();
		for (int i = 1; i <= 50; i++) {
			int font = fontRandom.nextInt(6) + 31;
			int background = backgroundRandom.nextInt(6) + 41;
			System.out.format("前景色是%d,背景色是%d------\33[%d;%d;4m我是博主%n", font, background, font, background);
		}
	}
}
