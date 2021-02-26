package com.gdxsoft.easyweb.script.display;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.gdxsoft.easyweb.data.DTRow;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.script.userConfig.UserConfig;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MStr;

public class MenuMain {
	private String _Key;
	private String _ParentKey;
	private String _Text;
	private String _JavascriptCmd;
	private String _Icon;
	private String _IconType;

	private UserConfig _UserConfig;
	private String _Lang = "zhcn";
	private String _Guid;
	private ItemValues _ItemValues;
	private ArrayList<MenuItem> _MenuItems;
	private HashMap<String, MenuItem> _MenuItemMap;

	private String MENU_ITEM = "<div class='[IS_HAVE_CHIDREN]ewa_menu_m' EWA_MF_PID=\"[EWA_MF_PID]\" id=\"[KEY]\" "
			+ "onclick='_EWA_[GUID].OnClick(event,this)'"
			+ " onmouseover='_EWA_[GUID].MouseOver(this)' EWA_CMD=\"[CMD]\">"
			+ "<table class='ewa_menu_table' border=0 cellpadding=0 cellspacing=0>\n<tr>\n"
			+ "<td><div CLASS='ewa_menu_m0 [ICON-FA]' style=\"[ICON]\"></div></td>"
			+ "\n<td nowrap><div CLASS='ewa_menu_m1'>[TEXT]</div></td>"
			+ "\n<td align=right> &nbsp;</td></tr>\n</table>\n</div>";

	private String MENU_SPLIT = "<div EWA_MF_PID=\"[EWA_MF_PID]\" id=\"[KEY]\">"
			+ "<table border=0 width=100% cellpadding=0 cellspacing=0 style='font-size:1px'>"
			+ "\n<tr><td><hr class='ewa_menu_split'/></td></tr>\n</table></div>";

	public MenuMain(ItemValues itemValues, UserConfig userConfig, String lang, String guid) throws Exception {
		this._ItemValues = itemValues;
		this._UserConfig = userConfig;
		this._Lang = lang;
		this.initParameters();
		this._Guid = guid;
		this._MenuItems = new ArrayList<MenuItem>();
		this._MenuItemMap = new HashMap<String, MenuItem>();
		MENU_ITEM = MENU_ITEM.replace("[GUID]", this._Guid);
		MENU_SPLIT = MENU_SPLIT.replace("[GUID]", this._Guid);

	}

	private void initParameters() throws Exception {
		UserXItem userXitem = this._UserConfig.getUserPageItem();
		this._Text = userXitem.getSingleValue("Menu", "Text");
		this._JavascriptCmd = userXitem.getSingleValue("Menu", "Cmd");
		this._Icon = userXitem.getSingleValue("Menu", "Icon");
		this._ParentKey = userXitem.getSingleValue("Menu", "ParentKey");
		this._Key = userXitem.getSingleValue("Menu", "Key");
		this._IconType = userXitem.getSingleValue("Menu", "IconType");

	}

	/**
	 * 根据表生成菜单
	 * 
	 * @param table
	 * @return
	 * @throws Exception
	 */
	public String createMenusHtml(DTTable table) throws Exception {
		if (this._Lang != null && this._Lang.equalsIgnoreCase("enus")) {
			if (table.getColumns().testName(this._Text + "_en")) {
				this._Text += "_en";
			} else if (table.getColumns().testName(this._Text + "en")) {
				this._Text += "en";
			} else if (table.getColumns().testName(this._Text + "_enus")) {
				this._Text += "_enus";
			} else if (table.getColumns().testName(this._Text + "enus")) {
				this._Text += "enus";
			}
		}
		for (int i = 0; i < table.getCount(); i++) {
			try {
				DTRow row = table.getRow(i);
				MenuItem o = this.createMenuItem(row);
				o.setDTRow(row);

				this._MenuItems.add(o);
				this._MenuItemMap.put(o.getKey(), o);

			} catch (Exception err) {
				System.out.println(err.getMessage());
			}
		}
		for (int i = 0; i < this._MenuItems.size(); i++) {
			MenuItem o = this._MenuItems.get(i);
			String pkey = this.getParentKey(o);
			// 设置有子节点
			if(this._MenuItemMap.containsKey(pkey)) {
				this._MenuItemMap.get(pkey).setIsHaveChildren(true);
			}
		}
		return createMenusHtml();
	}

	private String createMenusHtml() {
		MStr sJs = new MStr();
		MStr sb = new MStr();

		

		sb.append("<div style='display:none' id='_EWA_MF_" + this._Guid + "'>");
		for (int i = 0; i < this._MenuItems.size(); i++) {
			MenuItem o = this._MenuItems.get(i);
			String s1 = createMenuHtml(o);
			sb.a(s1);
		}
		sb.append("</div>");

		UserXItem userXitem = this._UserConfig.getUserPageItem();
		// 安装的容器ID （html）
		String installParent = userXitem.getSingleValue("MenuShow", "MenuShow");
		// 显示模式（默认Top）
		String menuType = userXitem.getSingleValue("MenuShow", "MenuType");
		String jsName = "_EWA_" + this._Guid;

		sJs.al("<script type='text/javascript'>");
		sJs.al("var " + jsName + "= new EWA.UI.Menu.C('" + jsName + "');");
		sJs.al("EWA.F.FOS['" + this._Guid + "']=" + jsName);
		sJs.al(jsName + ".InstallMenus('_EWA_MF_" + this._Guid + "',\"" + installParent + "\", \"" + menuType + "\")");
		sJs.al("</script>\r\n");
		return sb.toString() + sJs.toString();
	}

	private String createMenuHtml(MenuItem o) {
		String pkey = this.getParentKey(o);
		String s1 = MENU_ITEM;

		if (o.getText() != null && o.getText().trim().equals("\\-")) {
			s1 = MENU_SPLIT;
		} else {
			String icon = o.getIcon() == null ? "" : o.getIcon();
			if (icon.length() > 0) {
				// System.out.println(icon);
			}

			if (icon.indexOf("fa ") == 0) {
				// fontawesome.io
				s1 = s1.replace("[ICON-FA]", icon + "");
				s1 = s1.replace("[ICON]", "");
			} else {
				s1 = s1.replace("[ICON-FA]", "");
				if (icon != null && icon.trim().length() > 0) {
					s1 = s1.replace("[ICON]", "background-image:url('" + icon + "')");
				} else {
					s1 = s1.replace("[ICON]", "");
				}
			}

			String cmd = o.getJavascriptCmd() == null ? "" : o.getJavascriptCmd();
			if (cmd.length() > 0 && cmd.indexOf("@") >= 0) {
				if (o.getDTRow() != null) {
					DTRow r = o.getDTRow();
					// 设置当前行
					r.getTable().getRows().setCurRow(r);
				}
				cmd = this._ItemValues.replaceParameters(cmd, false, false);
			}
			cmd = Utils.textToJscript(cmd);
			s1 = s1.replace("[CMD]", cmd);
		}
		if ( pkey.trim().length() == 0) {
			s1 = s1.replace("[EWA_MF_PID]", "");
		} else {
			s1 = s1.replace("[EWA_MF_PID]", "_EWA_MF_" + this._Guid + "*" + pkey);
		}
		s1 = s1.replace("[KEY]", "_EWA_MF_" + this._Guid + "*" + o.getKey());
		s1 = s1.replace("[TEXT]", o.getText() == null ? "" : o.getText());
		
		if(o.isHaveChildren()) {
			s1 =s1.replace("[IS_HAVE_CHIDREN]", " ewa_menu_have_chidren ");
		} else {
			s1 =s1.replace("[IS_HAVE_CHIDREN]","");
		}
		
		return s1;
	}

	/**
	 * 获取处理过的上级节点
	 * @param o
	 * @return
	 */
	private String getParentKey(MenuItem o) {
		String pkey = o.getParentKey();
		if (pkey == null || pkey.trim().length() == 0 || pkey.trim().equals("0")) {
			return "";
		}else {
			return pkey;
		}
	}
	
	private MenuItem createMenuItem(DTRow row) throws Exception {
		String text = row.getCell(this._Text).getValue().toString();

		String cmd = row.getCell(this._JavascriptCmd).getValue() == null ? ""
				: row.getCell(this._JavascriptCmd).getValue().toString();
		String icon = null;
		if (this._IconType != null && this._IconType.equalsIgnoreCase("Image")) {
			byte[] image = (byte[]) row.getCell(this._Icon).getValue();
			if (image != null) {
				String md5 = Utils.md5(image);
				String path = UPath.getRealContextPath() + "temp_img_cache";
				java.io.File dir = new java.io.File(path);
				if (!dir.exists()) {
					dir.mkdirs();
				}
				String fileName = path + "/" + md5 + ".jpg";
				File img = new File(fileName);
				if (!img.exists()) {
					FileOutputStream fs = null;
					try {
						fs = new FileOutputStream(img);
						fs.write(image);
						fs.close();
					} catch (Exception e) {
						icon = e.getMessage();
					} finally {
						if (fs != null) {
							try {
								fs.close();
							} catch (IOException e) {
								System.err.println(e.getMessage());
							}
						}
					}
				}
				icon = "@SYS_CONTEXTPATH/temp_img_cache/" + md5 + ".jpg";
			} else {
				icon = "";
			}
		} else {
			icon = row.getCell(this._Icon).getValue() == null ? "" : row.getCell(this._Icon).getValue().toString();
		}
		String key = row.getCell(this._Key).getValue().toString();
		String pKey = row.getCell(this._ParentKey).getValue().toString();
		return this.createMenuItem(key, pKey, text, cmd, icon);
	}

	private MenuItem createMenuItem(String key, String pKey, String text, String cmd, String icon) {
		MenuItem o = new MenuItem();
		o.setIcon(icon);
		o.setJavascriptCmd(Utils.textToInputValue(cmd));
		o.setKey(key);
		o.setParentKey(pKey);
		o.setText(text);
		o.setIconType(this._IconType);
		return o;
	}

	/**
	 * 获取语言
	 * 
	 * @return the _Lang
	 */
	public String getLang() {
		return _Lang;
	}

	/**
	 * @param lang the _Lang to set
	 */
	public void setLang(String lang) {
		_Lang = lang;
	}

}
