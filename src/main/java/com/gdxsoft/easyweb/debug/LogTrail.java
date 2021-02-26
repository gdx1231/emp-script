package com.gdxsoft.easyweb.debug;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import com.gdxsoft.easyweb.utils.msnet.MStr;

public class LogTrail {

	public static int BUF_LEN = 1024 * 10; // 1M bytes
	private String _LogFile;
	private String _Status;
	private String _Encode="gbk";

	/**
	 * @return the _Encode
	 */
	public String getEncode() {
		return _Encode;
	}

	/**
	 * @param encode the _Encode to set
	 */
	public void setEncode(String encode) {
		_Encode = encode;
	}

	public static void main(String[] args) {
		LogTrail a = new LogTrail("c:/a.log");
		String s = a.getLog(20);
		System.out.println(s);
	}

	private LogTrail(String logFile) {
		this._LogFile = logFile;
	}

	public String getLog(int len) {
		FileChannel fc = null;
		try {
			File f = new File(this._LogFile);
			long length = f.length();
			fc = new RandomAccessFile(f, "r").getChannel();
			if (length > BUF_LEN) {
				long pos = length - BUF_LEN;
				fc.position(pos);
			}
			ByteBuffer buf = ByteBuffer.allocate(BUF_LEN);
			int reads = fc.read(buf);
			byte[] bytes = new byte[reads];
			bytes = buf.array();

			buf.clear();

			String s = getTrail(bytes, len);
			return s;

		} catch (Exception e) {
			return e.getMessage();
		} finally {
			if (fc != null) {
				try {
					fc.close();
				} catch (IOException e) {
					return e.getMessage();
				}
			}
		}
	}

	private String getTrail(byte[] bytes, int len) throws UnsupportedEncodingException {
		String s = new String(bytes,this._Encode);
		String[] ss = s.split("\n");
		MStr s1 = new MStr();
		int inc = 0;
		for (int i = ss.length - 1; i >= 0; i--) {
			s1.al(ss[i].replace("\r", ""));
			inc++;
			if (inc > len) {
				break;
			}
		}

		return s1.toString();

	}
}
