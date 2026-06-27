package com.gdxsoft.easyweb.conf;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

/**
 * 命令行工具：交互式生成 ewa_conf.xml 配置文件
 *
 * <pre>
 * # 交互模式（默认）
 * ./ewa-conf-creator -o ./ewa_conf.xml
 *
 * # 模板模式（一键生成默认模板）
 * ./ewa-conf-creator -t -o ./ewa_conf.xml
 * </pre>
 */
public class EwaConfCreator {

	private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd");
	private final Scanner scanner;
	private final String today;

	public EwaConfCreator(Scanner scanner) {
		this.scanner = scanner;
		this.today = DATE_FMT.format(new Date());
	}

	public static void main(String[] args) {
		String outputPath = "./ewa_conf.xml";
		boolean templateMode = false;

		for (int i = 0; i < args.length; i++) {
			switch (args[i]) {
			case "-o":
			case "--output":
				if (i + 1 < args.length)
					outputPath = args[++i];
				break;
			case "-t":
			case "--template":
				templateMode = true;
				break;
			case "-h":
			case "--help":
				printHelp();
				return;
			default:
				break;
			}
		}

		try {
			Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8.name());
			EwaConfCreator creator = new EwaConfCreator(scanner);
			String xml;
			if (templateMode) {
				xml = creator.buildTemplateXml();
			} else {
				xml = creator.buildInteractiveXml();
			}
			try (Writer w = new OutputStreamWriter(new FileOutputStream(outputPath), StandardCharsets.UTF_8)) {
				w.write(xml);
			}
			System.out.println("\news_conf.xml 已生成: " + outputPath);
		} catch (Exception e) {
			System.err.println("生成失败: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}

	private static void printHelp() {
		System.out.println("Usage: ewa-conf-creator [options]");
		System.out.println("  -o, --output   输出文件路径 (默认 ./ewa_conf.xml)");
		System.out.println("  -t, --template 模板模式（跳过交互，直接生成默认配置）");
		System.out.println("  -h, --help     显示帮助");
		System.out.println();
		System.out.println("无 -t 参数时进入交互模式，逐项询问配置。");
	}

	// ==================== interactive mode ====================

	private String buildInteractiveXml() {
		System.out.println();
		System.out.println("\033[1;36m╔══════════════════════════════════════════════════════╗\033[0m");
		System.out.println("\033[1;36m║   EWA Conf Creator — 交互式配置文件生成器          ║\033[0m");
		System.out.println("\033[1;36m╚══════════════════════════════════════════════════════╝\033[0m");
		System.out.println();
		System.out.println("按 Enter 使用括号中的默认值，输入 \"skip\" 跳过当前项。");

		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("<!-- 由 EwaConfCreator 生成 (" + today + ") -->\n");
		sb.append("<ewa_confs>\n");

		// Core sections
		askAdmins(sb);
		askDefine(sb);
		sb.append("\t<cfgCacheMethod value=\"sqlcached\" />\n\n");
		askSqlCached(sb);

		askRedises(sb);
		askSnowflake(sb);
		askSecurities(sb);
		askRequestValuesGlobal(sb);
		askScriptPaths(sb);
		askAddedResources(sb);
		askPaths(sb);
		askDebug(sb);
		askSmtps(sb);
		askDatabases(sb);
		askRestfuls(sb);
		askRemoteSyncs(sb);

		sb.append("</ewa_confs>\n");
		return sb.toString();
	}

	// ==================== template mode (original) ====================

	static String buildTemplateXml() {
		StringBuilder sb = new StringBuilder();
		String today = DATE_FMT.format(new Date());

		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("<!-- 由 EwaConfCreator 生成 (" + today + ") -->\n");
		sb.append("<ewa_confs>\n\n");

		sb.append("\t<!-- ========== 管理员 ========== -->\n");
		sb.append("\t<admins>\n");
		sb.append("\t\t<admin createDate=\"" + today + "\" loginId=\"admin\" password=\"\" userName=\"SysAdmin\" />\n");
		sb.append("\t</admins>\n\n");

		sb.append("\t<!-- ========== 定义开关 ========== -->\n");
		sb.append("\t<define value=\"true\" />\n\n");

		sb.append("\t<!-- ========== 配置缓存方法 ========== -->\n");
		sb.append("\t<cfgCacheMethod value=\"sqlcached\" />\n\n");

		sb.append("\t<!-- ========== SQL缓存 ========== -->\n");
		sb.append("\t<!-- cachedMethod: hsqldb | redis (生产环境推荐) -->\n");
		sb.append("\t<sqlCached cachedMethod=\"hsqldb\" />\n\n");

		sb.append("\t<!-- ========== Redis配置 ========== -->\n");
		sb.append("\t<redises>\n");
		sb.append("\t\t<!-- method: single | shared | cluster -->\n");
		sb.append("\t\t<redis name=\"r0\" method=\"single\" auth=\"\" hosts=\"127.0.0.1:6379\" />\n");
		sb.append("\t</redises>\n\n");

		sb.append("\t<!-- ========== 雪花算法 ========== -->\n");
		sb.append("\t<snowflake workId=\"1\" datacenterId=\"1\" />\n\n");

		sb.append("\t<!-- ========== 安全加密 ========== -->\n");
		sb.append("\t<securities>\n");
		sb.append("\t\t<security name=\"default\" default=\"true\" algorithm=\"aes-256-gcm\"\n");
		sb.append("\t\t\tiv=\"\" aad=\"\" key=\"change_me_to_a_32_char_key!\" />\n");
		sb.append("\t</securities>\n\n");

		sb.append("\t<!-- ========== 全局请求值 ========== -->\n");
		sb.append("\t<requestValuesGlobal>\n");
		sb.append("\t\t<rv name=\"rv_ewa_style_path\" value=\"/work/EmpScriptV2\" />\n");
		sb.append("\t</requestValuesGlobal>\n\n");

		sb.append("\t<!-- ========== 脚本路径 ========== -->\n");
		sb.append("\t<!-- path格式: resources:/xxx | jdbc:xxx | /path/to/dir -->\n");
		sb.append("\t<scriptPaths>\n");
		sb.append("\t\t<scriptPath name=\"ewa\" path=\"resources:/define.xml\" />\n");
		sb.append("\t</scriptPaths>\n\n");

		sb.append("\t<!-- ========== 附加资源 ========== -->\n");
		sb.append("\t<addedResources>\n");
		sb.append("\t\t<addedResource src=\"/static/main.js\" name=\"mainjs\" defaultConf=\"true\" />\n");
		sb.append("\t</addedResources>\n\n");

		sb.append("\t<!-- ========== 路径配置 ========== -->\n");
		sb.append("\t<paths>\n");
		sb.append("\t\t<path name=\"cached_path\" value=\"/tmp/ewa/cached\" />\n");
		sb.append("\t\t<path name=\"img_tmp_path\" value=\"/tmp/ewa/imgs/\" />\n");
		sb.append("\t\t<path name=\"img_tmp_path_url\" value=\"/imgs/\" />\n");
		sb.append("\t\t<path name=\"group_path\" value=\"/tmp/ewa/groups/\" />\n");
		sb.append("\t</paths>\n\n");

		sb.append("\t<!-- ========== 调试配置 ========== -->\n");
		sb.append("\t<debug ips=\"127.0.0.1,::1\" excludes=\"\" />\n\n");

		sb.append("\t<!-- ========== SMTP邮件 ========== -->\n");
		sb.append("\t<smtps>\n");
		sb.append("\t\t<smtp name=\"default\" default=\"true\" ip=\"127.0.0.1\" port=\"25\" />\n");
		sb.append("\t</smtps>\n\n");

		sb.append("\t<!-- ========== 数据库配置 ========== -->\n");
		sb.append("\t<!-- password支持 file:///path/to/file 从文件读取 -->\n");
		sb.append("\t<databases>\n");
		sb.append("\t\t<database name=\"work\" type=\"MYSQL\" connectionString=\"jdbc/work\" schemaName=\"\">\n");
		sb.append("\t\t\t<pool username=\"root\" password=\"\"\n");
		sb.append("\t\t\t\tmaxActive=\"40\" maxIdle=\"10\" maxWait=\"5000\"\n");
		sb.append("\t\t\t\tdriverClassName=\"com.mysql.cj.jdbc.Driver\"\n");
		sb.append(
				"\t\t\t\turl=\"jdbc:mysql://localhost:3306/work?useUnicode=true&amp;characterEncoding=utf8\" />\n");
		sb.append("\t\t</database>\n");
		sb.append("\t</databases>\n\n");

		sb.append("\t<!-- ========== RESTful API ========== -->\n");
		sb.append("\t<!-- path取值: jdbc:ewa (从数据库加载) | /api/root (文件路径) -->\n");
		sb.append("\t<restfuls path=\"jdbc:ewa\" cors=\"*\" />\n\n");

		sb.append("\t<!-- ========== 远程同步 ========== -->\n");
		sb.append(
				"\t<remote_syncs des=\"文件同步至远程服务器\" url=\"https://yourdomain/EWA_DEFINE/cgi-bin/remoteSync/\" code=\"\">\n");
		sb.append("\t\t<remote_sync name=\"static\" id=\"1\" source=\"/path/to/static\"\n");
		sb.append("\t\t\ttarget=\"/var/www/static\" filter=\"js,css,htm,html,png,jpg\" />\n");
		sb.append("\t</remote_syncs>\n\n");

		sb.append("</ewa_confs>\n");
		return sb.toString();
	}

	// ==================== interactive helpers ====================

	private void section(String title) {
		System.out.println();
		System.out.println("  \033[1;33m▶ " + title + "\033[0m");
	}

	private String ask(String prompt, String defaultValue) {
		String label = "    " + prompt;
		if (!defaultValue.isEmpty()) {
			label += " [" + defaultValue + "]";
		}
		label += ": ";
		System.out.print(label);
		String input = scanner.nextLine().trim();
		if (input.equalsIgnoreCase("skip")) {
			return null;
		}
		return input.isEmpty() ? defaultValue : input;
	}

	private boolean askYesNo(String prompt, boolean defaultValue) {
		String yn = defaultValue ? "Y/n" : "y/N";
		String label = "    " + prompt + " (" + yn + ")? ";
		System.out.print(label);
		String input = scanner.nextLine().trim().toLowerCase();
		if (input.isEmpty()) {
			return defaultValue;
		}
		return input.startsWith("y");
	}

	private String esc(String s) {
		if (s == null)
			return "";
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
	}

	// ==================== individual sections ====================

	private void askAdmins(StringBuilder sb) {
		section("管理员账户");
		if (!askYesNo("添加管理员?", true)) {
			return;
		}
		String loginId = ask("登录ID", "admin");
		if (loginId == null)
			return;
		String userName = ask("用户名", "SysAdmin");

		sb.append("\t<admins>\n");
		sb.append("\t\t<admin createDate=\"" + today + "\" loginId=\"" + esc(loginId)
				+ "\" password=\"\" userName=\"" + esc(userName != null ? userName : "SysAdmin") + "\" />\n");
		sb.append("\t</admins>\n\n");
	}

	private void askDefine(StringBuilder sb) {
		section("定义开关 (ewa_define 模块管理)");
		boolean v = askYesNo("启用定义功能?", true);
		sb.append("\t<define value=\"" + v + "\" />\n\n");
	}

	private void askSqlCached(StringBuilder sb) {
		section("SQL 缓存");
		if (!askYesNo("配置 SQL 缓存?", true)) {
			return;
		}
		String method = ask("缓存方式 (hsqldb / redis)", "hsqldb");
		if (method == null) {
			sb.append("\t<sqlCached cachedMethod=\"hsqldb\" />\n\n");
			return;
		}
		if ("redis".equalsIgnoreCase(method)) {
			String redisName = ask("Redis 配置名称", "r0");
			String debug = askYesNo("开启缓存调试?", false) ? "true" : "false";
			sb.append("\t<sqlCached cachedMethod=\"redis\" redisName=\"" + esc(redisName) + "\" debug=\"" + debug
					+ "\" />\n\n");
		} else {
			sb.append("\t<sqlCached cachedMethod=\"hsqldb\" />\n\n");
		}
	}

	private void askRedises(StringBuilder sb) {
		section("Redis 配置");
		if (!askYesNo("配置 Redis?", false)) {
			return;
		}
		String name = ask("Redis 名称", "r0");
		String method = ask("连接模式 (single / shared / cluster)", "single");
		String auth = ask("密码", "");
		String hosts = ask("地址 (host:port,多个逗号分隔)", "127.0.0.1:6379");
		if (hosts == null || hosts.isEmpty())
			return;

		sb.append("\t<redises>\n");
		sb.append("\t\t<redis name=\"" + esc(name) + "\" method=\"" + esc(method) + "\"");
		if (auth != null && !auth.isEmpty()) {
			sb.append(" auth=\"" + esc(auth) + "\"");
		}
		sb.append(" hosts=\"" + esc(hosts) + "\" />\n");
		sb.append("\t</redises>\n\n");
	}

	private void askSnowflake(StringBuilder sb) {
		section("雪花算法 (分布式ID)");
		if (!askYesNo("配置 Snowflake?", true)) {
			return;
		}
		String workId = ask("Work ID (0-31)", "1");
		String datacenterId = ask("Datacenter ID (0-31)", "1");
		sb.append("\t<snowflake workId=\"" + esc(workId) + "\" datacenterId=\"" + esc(datacenterId) + "\" />\n\n");
	}

	private void askSecurities(StringBuilder sb) {
		section("安全加密 (Cookie/敏感数据)");
		if (!askYesNo("配置加密?", true)) {
			return;
		}
		String algorithm = ask("算法 (aes-256-gcm / aes-192-gcm)", "aes-256-gcm");
		String key = ask("密钥 (AES-256 需 32 字符)", "change_me_to_a_32_char_key!");
		String iv = ask("初始化向量 IV", "");
		String aad = ask("附加认证数据 AAD", "");

		sb.append("\t<securities>\n");
		sb.append("\t\t<security name=\"default\" default=\"true\" algorithm=\"" + esc(algorithm) + "\"\n");
		sb.append("\t\t\tiv=\"" + esc(iv) + "\" aad=\"" + esc(aad) + "\" key=\"" + esc(key) + "\" />\n");
		sb.append("\t</securities>\n\n");
	}

	private void askRequestValuesGlobal(StringBuilder sb) {
		section("全局请求值 (注入到每个 RequestValue)");
		if (!askYesNo("添加全局请求值?", true)) {
			return;
		}
		List<String[]> pairs = new ArrayList<>();
		while (true) {
			String name = ask("变量名 (回车结束)", "");
			if (name == null || name.isEmpty())
				break;
			String value = ask("变量值", "");
			if (value == null)
				break;
			pairs.add(new String[] { name, value });
		}
		if (pairs.isEmpty()) {
			// add default
			pairs.add(new String[] { "rv_ewa_style_path", "/work/EmpScriptV2" });
		}
		sb.append("\t<requestValuesGlobal>\n");
		for (String[] p : pairs) {
			sb.append("\t\t<rv name=\"" + esc(p[0]) + "\" value=\"" + esc(p[1]) + "\" />\n");
		}
		sb.append("\t</requestValuesGlobal>\n\n");
	}

	private void askScriptPaths(StringBuilder sb) {
		section("脚本路径 (EWA 定义文件查找路径)");
		if (!askYesNo("配置脚本路径?", true)) {
			return;
		}
		List<String[]> paths = new ArrayList<>();
		while (true) {
			System.out.println("    路径格式: resources:/define.xml | jdbc:ewa | /path/to/dir");
			String name = ask("路径名称 (回车结束)", "");
			if (name == null || name.isEmpty())
				break;
			String path = ask("路径", "");
			if (path == null || path.isEmpty())
				break;
			paths.add(new String[] { name, path });
		}
		if (paths.isEmpty()) {
			paths.add(new String[] { "ewa", "resources:/define.xml" });
		}
		sb.append("\t<scriptPaths>\n");
		for (String[] p : paths) {
			sb.append("\t\t<scriptPath name=\"" + esc(p[0]) + "\" path=\"" + esc(p[1]) + "\" />\n");
		}
		sb.append("\t</scriptPaths>\n\n");
	}

	private void askAddedResources(StringBuilder sb) {
		section("附加 JS/CSS 资源");
		if (!askYesNo("添加附加资源?", false)) {
			return;
		}
		List<String[]> res = new ArrayList<>();
		while (true) {
			String src = ask("资源路径 src (回车结束)", "");
			if (src == null || src.isEmpty())
				break;
			String name = ask("资源名称 name", "");
			if (name == null)
				break;
			boolean defaultConf = askYesNo("设为默认配置?", false);
			String last = askYesNo("放在最后加载?", false) ? " true" : "";
			String dc = defaultConf ? " defaultConf=\"true\"" : "";
			res.add(new String[] { src, name, last, dc });
		}
		if (res.isEmpty())
			return;
		sb.append("\t<addedResources>\n");
		for (String[] r : res) {
			sb.append("\t\t<addedResource src=\"" + esc(r[0]) + "\" name=\"" + esc(r[1]) + "\"" + r[3] + r[2] + " />\n");
		}
		sb.append("\t</addedResources>\n\n");
	}

	private void askPaths(StringBuilder sb) {
		section("路径配置");
		if (!askYesNo("配置路径?", true)) {
			return;
		}
		String cachedPath = ask("缓存目录 cached_path", "/tmp/ewa/cached");
		String imgTmpPath = ask("图片缩略图根路径 img_tmp_path", "/tmp/ewa/imgs/");
		String imgTmpUrl = ask("图片缩略图 URL img_tmp_path_url", "/imgs/");
		String groupPath = ask("导入导出目录 group_path", "/tmp/ewa/groups/");
		String officeHome = ask("OpenOffice 路径 cvt_office_home (回车跳过)", "");

		sb.append("\t<paths>\n");
		if (cachedPath != null)
			sb.append("\t\t<path name=\"cached_path\" value=\"" + esc(cachedPath) + "\" />\n");
		if (imgTmpPath != null)
			sb.append("\t\t<path name=\"img_tmp_path\" value=\"" + esc(imgTmpPath) + "\" />\n");
		if (imgTmpUrl != null)
			sb.append("\t\t<path name=\"img_tmp_path_url\" value=\"" + esc(imgTmpUrl) + "\" />\n");
		if (groupPath != null)
			sb.append("\t\t<path name=\"group_path\" value=\"" + esc(groupPath) + "\" />\n");
		if (officeHome != null && !officeHome.isEmpty())
			sb.append("\t\t<path name=\"cvt_office_home\" value=\"" + esc(officeHome) + "\" />\n");
		sb.append("\t</paths>\n\n");
	}

	private void askDebug(StringBuilder sb) {
		section("调试配置");
		if (!askYesNo("配置调试?", true)) {
			return;
		}
		String ips = ask("允许调试的 IP (多个逗号分隔)", "127.0.0.1,::1");
		String excludes = ask("排除目录", "");
		sb.append("\t<debug ips=\"" + esc(ips) + "\" excludes=\"" + esc(excludes) + "\" />\n\n");
	}

	private void askSmtps(StringBuilder sb) {
		section("SMTP 邮件");
		if (!askYesNo("配置 SMTP?", false)) {
			return;
		}
		String ip = ask("邮件服务器 IP", "127.0.0.1");
		String port = ask("端口", "25");
		sb.append("\t<smtps>\n");
		sb.append("\t\t<smtp name=\"default\" default=\"true\" ip=\"" + esc(ip) + "\" port=\"" + esc(port) + "\" />\n");
		sb.append("\t</smtps>\n\n");
	}

	private void askDatabases(StringBuilder sb) {
		section("数据库配置");
		if (!askYesNo("配置数据库?", true)) {
			return;
		}
		List<DbConfig> dbs = new ArrayList<>();
		while (true) {
			String name = ask("数据库名称 (用于代码引用, 回车结束)", "");
			if (name == null || name.isEmpty())
				break;
			String type = ask("数据库类型 (MYSQL / HSQLDB / SQLSERVER)", "MYSQL");
			String schema = ask("Schema 名称", "");
			String driver = ask("JDBC 驱动类", driverForType(type));
			String url = ask("JDBC URL", urlForType(type, name));
			String username = ask("用户名", "root");
			String password = ask("密码 (或 file:///path/to/file)", "");
			String maxActive = ask("最大活动连接数", "40");
			String maxIdle = ask("最大空闲连接数", "10");
			String maxWait = ask("最大等待时间 ms", "5000");

			DbConfig cfg = new DbConfig();
			cfg.name = name;
			cfg.type = type;
			cfg.schema = schema;
			cfg.driver = driver;
			cfg.url = url;
			cfg.username = username;
			cfg.password = password;
			cfg.maxActive = maxActive;
			cfg.maxIdle = maxIdle;
			cfg.maxWait = maxWait;

			// Ask for aliases
			List<String[]> aliases = new ArrayList<>();
			while (true) {
				String aliasName = ask("  别名 (回车结束)", "");
				if (aliasName == null || aliasName.isEmpty())
					break;
				String aliasDesc = ask("  别名描述", "");
				aliases.add(new String[] { aliasName, aliasDesc != null ? aliasDesc : "" });
			}
			cfg.aliases = aliases;

			boolean addAnother = askYesNo("再添加一个数据库?", false);
			dbs.add(cfg);
			if (!addAnother)
				break;
		}
		if (dbs.isEmpty())
			return;

		sb.append("\t<databases>\n");
		for (DbConfig cfg : dbs) {
			sb.append("\t\t<database name=\"" + esc(cfg.name) + "\" type=\"" + esc(cfg.type)
					+ "\" connectionString=\"jdbc/" + esc(cfg.name) + "\" schemaName=\"" + esc(cfg.schema) + "\">\n");
			for (String[] alias : cfg.aliases) {
				sb.append(
						"\t\t\t<alias name=\"" + esc(alias[0]) + "\" description=\"" + esc(alias[1]) + "\" />\n");
			}
			sb.append("\t\t\t<pool username=\"" + esc(cfg.username) + "\" password=\"" + esc(cfg.password) + "\"\n");
			sb.append("\t\t\t\tmaxActive=\"" + esc(cfg.maxActive) + "\" maxIdle=\"" + esc(cfg.maxIdle)
					+ "\" maxWait=\"" + esc(cfg.maxWait) + "\"\n");
			sb.append("\t\t\t\tdriverClassName=\"" + esc(cfg.driver) + "\"\n");
			sb.append("\t\t\t\turl=\"" + esc(cfg.url) + "\" />\n");
			sb.append("\t\t</database>\n");
		}
		sb.append("\t</databases>\n\n");
	}

	private static String driverForType(String type) {
		switch (type.toUpperCase()) {
		case "MYSQL":
			return "com.mysql.cj.jdbc.Driver";
		case "HSQLDB":
			return "org.hsqldb.jdbcDriver";
		case "SQLSERVER":
			return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
		default:
			return "";
		}
	}

	private static String urlForType(String type, String name) {
		switch (type.toUpperCase()) {
		case "MYSQL":
			return "jdbc:mysql://localhost:3306/" + name
					+ "?useUnicode=true&characterEncoding=utf8&autoReconnect=true&useSSL=false";
		case "HSQLDB":
			return "jdbc:hsqldb:hsql://localhost:11002/" + name;
		case "SQLSERVER":
			return "jdbc:sqlserver://localhost;DatabaseName=" + name;
		default:
			return "";
		}
	}

	private void askRestfuls(StringBuilder sb) {
		section("RESTful API");
		if (!askYesNo("配置 RESTful API?", true)) {
			return;
		}
		String path = ask("配置路径 (jdbc:ewa 或 /api-path)", "jdbc:ewa");
		String cors = ask("CORS", "*");
		sb.append("\t<restfuls path=\"" + esc(path) + "\" cors=\"" + esc(cors) + "\" />\n\n");
	}

	private void askRemoteSyncs(StringBuilder sb) {
		section("远程文件同步");
		if (!askYesNo("配置远程同步?", false)) {
			return;
		}
		String url = ask("同步服务 URL", "https://yourdomain/EWA_DEFINE/cgi-bin/remoteSync/");
		String code = ask("授权码", "");
		String source = ask("本地源目录", "/path/to/static");
		String target = ask("远程目标目录", "/var/www/static");
		String filter = ask("文件过滤 (扩展名逗号分隔)", "js,css,htm,html,png,jpg");

		sb.append("\t<remote_syncs des=\"文件同步至远程服务器\" url=\"" + esc(url) + "\" code=\"" + esc(code) + "\">\n");
		sb.append("\t\t<remote_sync name=\"static\" id=\"1\" source=\"" + esc(source) + "\"\n");
		sb.append("\t\t\ttarget=\"" + esc(target) + "\" filter=\"" + esc(filter) + "\" />\n");
		sb.append("\t</remote_syncs>\n\n");
	}

	// ==================== dummy type ====================

	private static class DbConfig {
		String name, type, schema, driver, url, username, password;
		String maxActive, maxIdle, maxWait;
		List<String[]> aliases = new ArrayList<>();
	}
}
