package com.gdxsoft.easyweb.script.display.frame;

public class FrameParameters {

	/**
	 * 在Frame的TD 附加标题，描述的开关参数名称，非空
	 *  在每个td上添加属性 ewa_cell_des, ewa_cell_memo
	 */
	public final static String EWA_CELL_ADD_DES = "EWA_CELL_ADD_DES";
	/**
	 * 在Frame的TD 附加标题的att名称
	 * 前端css：.ewa-col-name::before {content: attr(ewa_cell_des);}
	 */
	public final static String EWA_CELL_ADD_DES_NAME = "ewa_cell_des";
	/**
	 * 在Frame的TD 附加备注的att名称
	 * 前端css：.ewa-col-name::before {content: attr(ewa_cell_memo);}
	 */
	public final static String EWA_CELL_ADD_DES_NAME_MEMO = "ewa_cell_memo";
	
	
	/**
	 * 通过参数传递的Title，覆盖frame的title
	 */
	public final static String EWA_TITLE = "EWA_TITLE";
	/**
	 * 通过参数传递的TitleEn (ewa_lang=enus)，覆盖frame的title
	 */
	public final static String EWA_TITLE_EN = "EWA_TITLE_EN";

	/**
	 * 跳过图片滑动验证，NOT_CHECK=不检查验证码 pv.getPVTag() == PageValueTag.HTML_CONTROL_PARAS
	 * ||<br>
	 * pv.getPVTag() == PageValueTag.SYSTEM || <br>
	 * pv.getPVTag() == PageValueTag.SESSION
	 */
	public final static String EWA_SLIDE_PUZZLE_CHECK = "EWA_SLIDE_PUZZLE_CHECK";
	/**
	 * 不检查验证码，用于手机应用或AJAX调用<br>
	 * NOT_CHECK=不检查验证码<br>
	 * pv.getPVTag() == PageValueTag.HTML_CONTROL_PARAS ||<br>
	 * pv.getPVTag() == PageValueTag.SYSTEM || <br>
	 * pv.getPVTag() == PageValueTag.SESSION
	 */
	public static final String EWA_VALIDCODE_CHECK = "EWA_VALIDCODE_CHECK";

	/**
	 * 隐藏Frame字段的字符串表达式，用,分割
	 */
	public static final String EWA_HIDDEN_FIELDS = "EWA_HIDDEN_FIELDS";

	/**
	 * 更改FrameUnid的前缀
	 */
	public static final String FRAME_UNID_PREFIX = "FRAME_UNID_PREFIX";
	/**
	 * ewaconfigitem或Jsp程序调用内部调用，创建为 /EWA_STYLE/cgi-bin/?xmlname=xx&amp;itemname=yy
	 */
	public static final String INNER_CALL = "INNER_CALL";

	/**
	 * 列表内容默认为li，指定参数后更改标签，参数值大小写无关<br>
	 * a: a<br>
	 * div: div<br>
	 * div2: div&gt;&lt;<div
	 */
	public static final String EWA_GRID_AS = "ewa_grid_as";

	/**
	 * 列表每行所有TD字符串进行md5签名yes/1，<br>
	 * 用于刷新数据 refreshPage or replaceRowsData 的比对
	 */
	public static final String EWA_ROW_SIGN = "EWA_ROW_SIGN";
	/**
	 * 配置文件(参数值大小写有关)<br>
	 * 例如“|ewa|ewa_main.xml”，“|”表示目录分割符，..|不被支持，会出错。
	 */
	public static final String XMLNAME = "XMLNAME";

	/**
	 * 配置文件的配置项(参数值大小写无关)
	 */
	public static final String ITEMNAME = "ITEMNAME";

	/**
	 * 指定语言,语言列表 zhcn（简体中文）enus（英语）<br>
	 * 使用方式：参数调用为最高优先级，默认为zhcn，如果参数指定，则保留在session中，下次从session中获取
	 */
	public static final String EWA_LANG = "EWA_LANG";

	/**
	 * 指定要调用的ACTION(参数值大小写无关)<br>
	 * 用于AJAX调用的功能，如删除记录，修改记录等，在配置文件中定义，<br>
	 * 默认的调用Action名称是“OnPageLoad”
	 */
	public static final String EWA_ACTION = "EWA_ACTION";

	/**
	 * 不显示debug信息，覆盖ewa_conf.xml的debug 设置。
	 */
	public static final String EWA_DEBUG_NO = "EWA_DEBUG_NO";

	/**
	 * 不显示内容，仅用于执行，不打印内容
	 */
	public static final String EWA_NO_CONTENT = "EWA_NO_CONTENT";

	/**
	 * 提交后执行的脚本 用于AJAX调用后再执行的脚本
	 */
	public static final String EWA_P_BEHAVIOR = "EWA_P_BEHAVIOR";

	/**
	 * 是否是POST提交 =1
	 */
	public static final String EWA_POST = "EWA_POST";

	/**
	 * 不显示frame框架 在配置项中定义了HtmlFrame后，首先显示框架，然后显示当前配置项。仅EWA_FRAMESET_NO=1起作用
	 * 
	 */
	public static final String EWA_FRAMESET_NO = "EWA_FRAMESET_NO";

	/**
	 * 输出json的字段名称大小写，和EWA_AJAX联合使用，参数值大小写无关<br>
	 * 当为lower时是所有字段转为小写，<br>
	 * 为upper时所有字段转为大写，<br>
	 * 否则无转换。
	 * 
	 */
	public static final String EWA_JSON_FIELD_CASE = "EWA_JSON_FIELD_CASE";

	/**
	 * 当Frame输出json时候忽略null值，即不输出 addr: null
	 */
	public static final String EWA_JSON_SKIP_NULL = "EWA_JSON_SKIP_NULL";
	/**
	 * 当Frame输出json时，处理bin模式，HEX, BASE64, IMAGE(default)
	 */
	public static final String EWA_JSON_BIN_METHOD = "EWA_JSON_BIN_METHOD";
	/**
	 * Tree加载分层数据 EWA_TREE_MORE=1起作用
	 */
	public static final String EWA_TREE_MORE = "EWA_TREE_MORE";

	/**
	 * 获取Tree当前状态 EWA_TREE_STATUS=1起作用
	 */
	public static final String EWA_TREE_STATUS = "EWA_TREE_STATUS";

	/**
	 * 不获取Tree当前状态
	 */
	public static final String EWA_TREE_SKIP_GET_STATUS = "EWA_TREE_SKIP_GET_STATUS";

	/**
	 * 下载文件对应的字段名称
	 */
	public static final String EWA_DOWNLOAD_NAME = "EWA_DOWNLOAD_NAME";

	/**
	 * 是否是AJAX调用(参数值大小写无关)<br>
	 * 1. XML 输出XML字符串； <br>
	 * 2. JSON 输出JSON数组；<br>
	 * 3. JSON_ALL 输出所有指定Action的查询数据，以多个JSON数组输出；<br>
	 * 4. JSON_EXT 输出JSON和配置项信息；<br>
	 * 5. JSON_EXT1 输出JSON和配置项信息及配置脚本。<br>
	 * 6. DOWNLOAD_INLINE 输出图片、PDF<br>
	 * 7. DOWNLOAD 下载文件，EWA_DOWNLOAD_NAME 对应文件保存字段<br>
	 * 8. HAVE_DATA 显示为是否有数据<br>
	 * 9. VALIDCODE 输出验证码<br>
	 * 10. DOWN_DATA 表示下载数据<br>
	 * 11. SELECT_RELOAD 创建重新刷新item的json，例如select的重新刷新<br>
	 * 12. LF_RELOAD listframe reload<br>
	 * 13. INSTALL 安装HTML<br>
	 * 14. WORKFLOW 工作流<br>
	 * 15. TOP_CNT_BOTTOM 仅生成对象本身，不包括头部和尾部等<br>
	 * 16. XMLDATA 生成数据XML
	 */
	public static final String EWA_AJAX = "EWA_AJAX";

	/**
	 * 针对App调用，非空，参数大小写无关<br>
	 * ListFrame删除在tr上的事件。
	 */
	public static final String EWA_APP = "EWA_APP";

	/**
	 * 用于更改cookie的域，只能用于HtmlControl调用<br>
	 * 
	 * <pre>
	 * PageValue pv = rv.getPageValues().getPageValue("EWA_COOKIE_DOMAIN");
	 * if (pv != null && (pv.getPVTag() == PageValueTag.HTML_CONTROL_PARAS)) {
	 * 	domain = pv.getValue().toString();
	 * }
	 * </pre>
	 */
	public static final String EWA_COOKIE_DOMAIN = "EWA_COOKIE_DOMAIN";

	/**
	 * 用于初始化列表的搜索框，例如：<br>
	 * 1、EWA_SEARCH=nws_subject[lk]base,NWS_CAT_NAME[eq]documents<br>
	 * 2、EWA_SEARCH=MEMO_STATE[or]MEMO_ING;MEMO_FINISH（或表达式）<br>
	 * 语法：字段[方式]检索词<br>
	 * 方式：<br>
	 * 
	 * lk（包含）：字段 like '%检索词%'，<br>
	 * llk（左包含）：字段 like '检索词%'，<br>
	 * rlk（右包含）：字段 like '%检索词'，<br>
	 * eq（等于）：字段='检索词'<br>
	 * or（或）：多个词之间用分号分割，字段='检索词' OR 字段='检索词1'
	 */
	public static final String EWA_SEARCH = "EWA_SEARCH";

	/**
	 * 覆盖BOX参数的 parent_id，重新设定装载对象指定的id。 EWA_BOX=1时有效
	 */
	public static final String EWA_BOX_PARENT_ID = "EWA_BOX_PARENT_ID";

	/**
	 * 设置Listframe显示为BOX对象，需要在IDE中设定BOX参数，非空，参数大小写无关
	 */
	public static final String EWA_BOX = "EWA_BOX";
	/**
	 * 用户定义参数指定是否分页, yes表示分页，no表示不分页，超越PageSize.IsSplitPage定义
	 */
	public static final String EWA_IS_SPLIT_PAGE = "EWA_IS_SPLIT_PAGE";

	/**
	 * 列表不进行重绘按钮，限定值为NO，参数大小写无关<br>
	 */
	public static final String EWA_LU_BUTTONS = "EWA_LU_BUTTONS";
	/**
	 * 列表不进行重绘搜索，限定值为NO，参数大小写无关<br>
	 */
	public static final String EWA_LU_SEARCH = "EWA_LU_SEARCH";
	/**
	 * 列表不进行重绘按钮，限定值为S/M，参数大小写无关<br>
	 * s=单选<br>
	 * m=多选<br>
	 */
	public static final String EWA_LU_SELECT = "EWA_LU_SELECT";
	/**
	 * 列表行上双击，限定值为yes/no，参数大小写无关<br>
	 */
	public static final String EWA_LU_DBLCLICK = "EWA_LU_DBLCLICK";

	/**
	 * 列表行上双击，限定值为yes/no，参数大小写无关<br>
	 */
	public static final String EWA_LU_DBL_CLICK = "EWA_LU_DBL_CLICK";
	/**
	 * 列表当前页编号，参数大小写无关<br>
	 */
	public static final String EWA_PAGECUR = "EWA_PAGECUR";
	/**
	 * 列表分页记录数，参数大小写无关<br>
	 */
	public static final String EWA_PAGESIZE = "EWA_PAGESIZE";
	/**
	 * 排序方式参数，参数大小写无关<br>
	 */
	public final static String EWA_LF_ORDER = "EWA_LF_ORDER";

	/**
	 * 指定Frame 显示为3段，2段或1段，参数大小写无关<br>
	 * C2/2 = 2段，无备注框<br>
	 * C1/1 = 1段，无标题框<br>
	 * C11 = 标题和输入框，上下排列
	 */
	public final static String EWA_FRAME_COLS = "EWA_FRAME_COLS";

	/**
	 * Frame定义了自定义框架，此参数可以不使用此框架，非空，参数大小写无关
	 */
	public final static String EWA_TEMP_NO = "EWA_TEMP_NO";
	/**
	 * 不使用FrameHtml模板，非空，参数大小写无关
	 */
	public static final String EWA_LF_TEMP_NO = "EWA_LF_TEMP_NO";
	/**
	 * 用户参数指定宽度，参数大小写无关
	 */
	public final static String EWA_WIDTH = "EWA_WIDTH";
	/**
	 * 用户参数指定的高度，参数大小写无关
	 */
	public final static String EWA_HEIGHT = "EWA_HEIGHT";
	/**
	 * 不使用 Test1的table，非空，参数大小写无关
	 */
	public final static String EWA_SKIP_TEST1 = "ewa_skip_test1";
	/**
	 * 是否打开为dialog模式，限定高度宽带滚动条，非空，参数大小写无关<br>
	 * &lt;div class='ewa-in-dialog' &gt;
	 */
	public final static String EWA_IN_DIALOG = "ewa_in_dialog";
	/**
	 * EWA调用模式，参数大小写无关 <br>
	 * INNER_CALL表示为ewaconfigitem或 Jsp程序调用
	 */
	public final static String EWA_CALL_METHOD = "EWA_CALL_METHOD";

	/**
	 * ewa_conf中的 addedResource定义的附加资源的names，通过“,”分割，参数大小写无关
	 */
	public final static String EWA_ADDED_RESOURCES = "ewa_added_resources";

	/**
	 * 是否显示标题栏，判断参数EWA_IS_HIDDEN_CAPTION(yes/no,1/0),参数大小写无关，对于
	 * ListFrame是第一行的字段描述，对于Frame是第一行标题
	 */
	public final static String EWA_IS_HIDDEN_CAPTION = "EWA_IS_HIDDEN_CAPTION";

	/**
	 * 生成页面的JSON数据的名称，jsonp用，参数大小写无关
	 */
	public final static String EWA_JSON_NAME = "EWA_JSON_NAME";

	/**
	 * Frame按照ReDraw模式进行显示，非空有效，参数大小写无关
	 */
	public final static String EWA_REDRAW = "ewa_redraw";

	/**
	 * EWA_MTYPE指定Frame的处理数据方式，参数大小写无关<br>
	 * N=new, <br>
	 * M=modify, <br>
	 * C=copy
	 */
	public final static String EWA_MTYPE = "EWA_MTYPE";

	/**
	 * 创建 select 对象的reload事件，对应的UserXItem的名称
	 */
	public final static String EWA_RELOAD_ID = "EWA_RELOAD_ID";

	/**
	 * 列表左引导模式
	 */
	public static final String EWA_LEFT = "EWA_LEFT";

	/**
	 * 列表显示回收站<br>
	 * NO = 不显示
	 */
	public static final String EWA_RECYCLE = "EWA_RECYCLE";

	/**
	 * Tree初始化显示的值
	 */
	public static final String EWA_TREE_INIT_KEY = "EWA_TREE_INIT_KEY";

	/**
	 * 指定Tree的根节点Id
	 */
	public static final String EWA_TREE_ROOT_ID = "EWA_TREE_ROOT_ID";

	/**
	 * 多维表格转置，1=转置
	 */
	public static final String EWA_GRID_TRANS = "EWA_GRID_TRANS";

	public static final String EWA_INIT_GRP = "EWA_INIT_GRP";

	/**
	 * HtmlCreator 调用提交后执行的脚本的调用FrameUnid编号
	 */
	public static final String EWA_PARENT_FRAME = "EWA_PARENT_FRAME";

	/**
	 * 提交后执行的提示
	 */
	public static final String EWA_ACTION_TIP = "EWA_ACTION_TIP";

	/**
	 * 执行后是否重新加载页面<br>
	 * 0 = 不加载
	 */
	public static final String EWA_ACTION_RELOAD = "EWA_ACTION_RELOAD";

	/**
	 * 页面提交的要执行的事件 避免xxs攻击，仅支持EWA.F.FOS["xxxx"].NewNodeAfter，其它无效
	 */
	public static final String EWA_AFTER_EVENT = "EWA_AFTER_EVENT";

	/**
	 * shortName
	 */
	public static final String EWA_SN = "EWA_SN";

	/**
	 * 皮肤名称
	 */
	public static final String EWA_SKIN = "EWA_SKIN";

	/**
	 * 存在于session的皮肤名称
	 */
	public static final String EWA_SKIN_SESSION = "EWA_SKIN_SESSION";

	/**
	 * Frame执行操作的名称
	 */
	public static final String EWA_ACTION_KEY = "EWA_ACTION_KEY";

	public static final String EWA_TREE_KEY = "EWA_TREE_KEY";

	public static final String EWA_TREE_PARENT_KEY = "EWA_TREE_PARENT_KEY";

	public static final String EWA_TREE_TEXT = "EWA_TREE_TEXT";

	public static final String EWA_FRAME_URL = "EWA_FRAME_URL";

	/**
	 * 工作流控制点 =1
	 */
	public static final String EWA_WF_CTRL = "EWA_WF_CTRL";

	/**
	 * 在返回的表中 判断Action执行的表中是否包含错误
	 */
	public static final String EWA_ERR_OUT = "EWA_ERR_OUT";

	/**
	 * ListFrame 导出的模式, XLS,DBF,TXT,XML
	 */
	public static final String EWA_AJAX_DOWN_TYPE = "EWA_AJAX_DOWN_TYPE";

	/**
	 * ListFrame 检索，客户端操作后产生，一般不用于url手动调用，手动调用使用EWA_SEARCH
	 */
	public static final String EWA_LF_SEARCH = "EWA_LF_SEARCH";

	public static final String SYS_FRAME_UNID = "SYS_FRAME_UNID";

	/**
	 * writeDebug
	 */
	public static final String EWA_DB_LOG = "EWA_DB_LOG";

	/**
	 * EWA_LANG 从session取
	 */
	public static final String SYS_EWA_LANG = "SYS_EWA_LANG";

	/**
	 * 获取工作流类型<br>
	 * if (wfType.equals("cnns")) {<br>
	 * } else if (wfType.equals("units")) {<br>
	 * } else if (wfType.equals("gunid")) {<br>
	 * } else if (wfType.equals("all")) {<br>
	 * } else if (wfType.equals("get")) {<br>
	 * } else if (wfType.equals("ins_post")) { // 用户提交<br>
	 * } else if (wfType.equals("ins_get")) { // 用户提交<br>
	 */
	public static final String EWA_WF_TYPE = "EWA_WF_TYPE";

	public static final String EWA_SCRIPT_PATH = "EWA_SCRIPT_PATH";

	/**
	 * 强制刷新数据
	 */
	public static final String EWA_R = "EWA_R";

	/**
	 * "base64 , 16=16进制，默认存储为文件，itemValues
	 */
	public static final String EWA_BIN_TYPE = "EWA_BIN_TYPE";

	/**
	 * yes = 使用 XHTML头
	 */
	public static final String EWA_XHTML = "EWA_XHTML";
	/**
	 * Default is h5 , no = 不使用h5头
	 */
	public static final String EWA_H5 = "EWA_H5";

	/**
	 * 移动模式，非空
	 */
	public static final String EWA_MOBILE = "EWA_MOBILE";

	/**
	 * 输出为VUE格式，非空
	 */
	public static final String EWA_VUE = "EWA_VUE";

	/**
	 * 时差（分钟）
	 */
	public static final String EWA_TIMEDIFF = "EWA_TIMEDIFF";

	public static final String RV_EWA_STYLE_PATH = "RV_EWA_STYLE_PATH";

	public static final String EWA_DEBUG_KEY = "EWA_DEBUG_KEY";

	/**
	 * 脚本调试
	 */
	public static final String EWA_JS_DEBUG = "EWA_JS_DEBUG";

	/**
	 * 工作流名称
	 */
	public static final String EWA_WF_NAME = "EWA_WF_NAME";

	public static final String EWA_WF_UOK = "EWA_WF_UOK";

	public static final String EWA_ID = "EWA_ID";

	public static final String EWA_UP_NEWSIZES = "EWA_UP_NEWSIZES";

	public static final String EWA_KEY = "EWA_KEY";

	/**
	 * ListFrame分页查询时跳过的select语句
	 */
	public static final String EWA_SQL_SPLIT_NO = "EWA_SQL_SPLIT_NO";
	/**
	 * 强制制定为SELECT查询
	 */
	public static final String EWA_IS_SELECT = "EWA_IS_SELECT";
	/**
	 * 英文
	 */
	public static final String ENUS = "enus";
	/**
	 * 中文
	 */
	public static final String ZHCN = "zhcn";

	/**
	 * shortName
	 */
	public static String $S = "$S";

	/**
	 * 图片重新缩放，例如：800x600
	 */
	public static String EWA_IMAGE_RESIZE = "ewa_image_resize";

}
