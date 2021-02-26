package com.gdxsoft.easyweb.define;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

public class Dirs {
	private ArrayList<Dir> _Dirs;
	private String _InitPathName;
	private boolean _DirAndFile;
	private String[] _Filetes;
	private ArrayList<String> _UnIncludes;

	public Dirs() {
		this._Dirs = new ArrayList<Dir>();
	}
	
	public Dirs(String initPathName, boolean dirAndFile) {
		_DirAndFile = dirAndFile;
		File f1 = new File(initPathName);
		_InitPathName = f1.getPath();
		this._Dirs = new ArrayList<Dir>();

	}

	public void addDir(Dir dir) {
		this._Dirs.add(dir);
	}
	
	public void init() throws IOException {

		this.initDirsNio(this._InitPathName);
	}

	/**
	 * 获取过滤的目录
	 */
	public void initUnIncludes(String unincludes) {
		this._UnIncludes = new ArrayList<String>();

		if (unincludes == null || unincludes.trim().length() == 0) {
			return;
		}
		String[] unincludes1 = unincludes.split(",");
		for (int i = 0; i < unincludes1.length; i++) {
			String s = unincludes1[i].trim();
			if (s.length() > 0) {
				this._UnIncludes.add(s);
			}
		}

	}

	/**
	 * 检查是否为可用的目录
	 * 
	 * @param d
	 * @return
	 */
	private boolean isInclude(File d) {
		if (this._UnIncludes == null) {
			return true;
		}
		for (int i = 0; i < this._UnIncludes.size(); i++) {
			String s = this._UnIncludes.get(i);
			if (s.equals("*") || s.equalsIgnoreCase(d.getName())) {
				return false;
			}

			if (s.endsWith("*")) {
				String s1 = s.replace("*", "").toUpperCase();
				if (d.getName().toUpperCase().startsWith(s1)) {
					return false;
				}
			}

		}
		return true;
	}

	/**
	 * 初始化目录(jdk1.7)
	 * 
	 * @param name
	 *            根目录
	 * @throws IOException
	 */
	private void initDirsNio(String name) throws IOException {
		long t0 = System.currentTimeMillis();
		Path root = Paths.get(name);
		MyVisitor fv = new MyVisitor(this);

		Files.walkFileTree(root, fv);
		long t1 = System.currentTimeMillis();

		System.out.println(t1 - t0);

		long t2 = System.currentTimeMillis();
		System.out.println(t2 - t1);
	}

	/**
	 * 老方法,速度慢jdk1.6 初始化目录(递归程序)
	 * 
	 * @param name
	 *            根目录
	 */
	void initDirs(String name) {
		long t0 = System.currentTimeMillis();

		File f1 = new File(name);
		File[] ff = f1.listFiles();
		String[] ff1 = f1.list();

		long t11 = System.currentTimeMillis();

		if (ff == null) {
			return;
		}
		for (int i = 0; i < ff.length; i++) {
			long ta0 = System.currentTimeMillis();

			// 先进行文件扩展名判断,如果bak文件比较多的话,会大量节约事件
			// 此时文件没有被打开
			if (ff1[i].toLowerCase().endsWith(".bak")) {
				continue;
			}
			// ff[i] 此时需要打开文件, 无线网络的话比较慢(30ms)左右
			if (!ff[i].isDirectory()) {
				if (this._DirAndFile) {

					this.initFiles(ff[i]);
					long ta1 = System.currentTimeMillis();
					if (name.equals("aaaaa")) {
						System.out.println(ff1[i] + ": " + (ta1 - ta0));
					}

				}
				continue;
			}
			if (!this.isInclude(ff[i])) {
				continue;
			}
			String denyFile = ff[i].getAbsolutePath() + "/deny.ewa";
			File f = new File(denyFile);
			if (f.exists()) {
				continue;
			}

			String parentPath = name;
			String path = ff[i].getPath();
			if (name.equals(this._InitPathName)) {
				parentPath = "";
			} else {
				parentPath = name.replace(this._InitPathName, "");
			}
			path = path.replace(this._InitPathName, "").replace(File.separator, "|");
			parentPath = parentPath.replace(File.separator, "|");
			Dir dir = new Dir(ff[i].getName(), path, parentPath, false);
			this._Dirs.add(dir);
			initDirs(ff[i].getPath());
		}
		long t1 = System.currentTimeMillis();
		if (t1 - t0 > 1000) {
			// [文件数量], 获取文件列表时间, 处理完成时间
			System.out.println(name + "[" + ff.length + "]: " + (t11 - t0) + ", " + (t1 - t0));
		}
	}

	private void initFiles(File file) {
		if (file.getName().toLowerCase().endsWith(".bak")) {
			return;
		}

		String parentPath = file.getParent().replace(this._InitPathName, "");
		String path = file.getPath().replace(this._InitPathName, "");

		path = path.replace(this._InitPathName, "").replace(File.separator, "|");
		parentPath = parentPath.replace(File.separator, "|");

		Dir dir = new Dir(file.getName(), path, parentPath, true);
		if (checkFilter(dir)) {
			this._Dirs.add(dir);
		}

	}

	/**
	 * 检查是否符合扩展名过滤条件
	 * 
	 * @param dir
	 * @return
	 */
	private boolean checkFilter(Dir dir) {
		if (this._Filetes == null) {
			return true;
		}
		for (int i = 0; i < this._Filetes.length; i++) {
			String ext = dir.getExt().toLowerCase();
			String f1 = this._Filetes[i].toLowerCase();
			if (ext.equals(f1) || ext.equals("." + f1)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return the _Dirs
	 */
	public ArrayList<Dir> getDirs() {
		if (this._Dirs.size() == 0) {
			try {
				init();
			} catch (IOException err) {
				System.out.println(err.getMessage());
				return null;
			}

		}
		return _Dirs;
	}

	/**
	 * @return the _Filetes
	 */
	public String[] getFiletes() {
		return _Filetes;
	}

	/**
	 * @param filetes
	 *            the _Filetes to set
	 */
	public void setFiletes(String[] filetes) {
		_Filetes = filetes;
	}

	public static void main(String[] args) {
		Dirs dirs = new Dirs("c:\\Volumes\\b2b\\user.config.xml\\", true);
		String[] filter = { "xml" };
		dirs.setFiletes(filter);
		for (int i = 0; i < dirs.getDirs().size(); i++) {
			System.out.println(dirs.getDirs().get(i).getPath());
		}
	}

	private class MyVisitor extends SimpleFileVisitor<Path> {
		private Dirs _Inst;

		public MyVisitor(Dirs instance) {
			_Inst = instance;

		}

		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
			String name = dir.getName(dir.getNameCount() - 1).toString();
			String path = dir.toString().replace(this._Inst._InitPathName, "").replace(File.separator, "|");
			String parentPath = dir.getParent().toString().replace(this._Inst._InitPathName, "").replace(File.separator,
					"|");

			Dir d = new Dir(name, path, parentPath, false);

			this._Inst._Dirs.add(d);

			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
			if (this._Inst._DirAndFile) {
				String name = file.getName(file.getNameCount() - 1).toString();
				String path = file.toString().replace(this._Inst._InitPathName, "").replace(File.separator, "|");
				String parentPath = file.getParent().toString().replace(this._Inst._InitPathName, "")
						.replace(File.separator, "|");

				Dir d = new Dir(name, path, parentPath, true);

				if (this._Inst.checkFilter(d)) {
					this._Inst._Dirs.add(d);
				}
			}
			return FileVisitResult.CONTINUE;
		}
	}
}
