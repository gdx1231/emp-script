package com.gdxsoft.easyweb.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class CachedXmlFileMeta implements Serializable, Cloneable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8867607559969742512L;
	
	/**
	 * 从序列化二进制中获取
	 * 
	 * @param buf
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static CachedXmlFileMeta fromSerialize(byte[] buf) throws IOException, ClassNotFoundException {
		// Serialize
		ByteArrayInputStream fis = new ByteArrayInputStream(buf);
		ObjectInputStream ois = new ObjectInputStream(fis);
		CachedXmlFileMeta tb = (CachedXmlFileMeta) ois.readObject();
		ois.close();
		fis.close();
		return tb;
	}
	
	private String code;
	private String file;
	private String key;
	private List<String> itemNames = new ArrayList<String>();

	public void addItemName(String itemName) {
		this.itemNames.add(itemName);
	}

	/**
	 * 序列化表
	 * 
	 * @return
	 * @throws IOException
	 */
	public byte[] toSerialize() throws IOException {
		// Serialize
		ByteArrayOutputStream fos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(fos);

		oos.writeObject(this);
		oos.close();

		byte[] buf = fos.toByteArray();
		fos.close();

		return buf;
	}
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public List<String> getItemNames() {
		return itemNames;
	}

}
