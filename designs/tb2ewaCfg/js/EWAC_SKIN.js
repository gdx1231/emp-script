function complex_menu_click(obj) {
	var o1 = $X("EWAC_SELECT_TYPE");
	o1.value = obj.id;
	o1.onchange();
}
var EWAC_UISkinEvents = {
	MouseOver : function(obj, isRow) {
		var oDiv = EWAC_UISkinEvents._GetDiv(obj);
		//
		var p1 = EWA.UI.Utils.GetPosition(obj);
		var tb = obj.parentNode.parentNode.parentNode;
		var w = obj.offsetWidth;
		var t = p1.Y - obj.offsetHeight;
		var l = p1.X;
		if (isRow) {
			var h = obj.offsetHeight;
		} else {
			var h = tb.offsetHeight;
		}
		with (oDiv.style) {
			top = t + "px";
			left = l + "px";
			display = "";
			width = w + "px";
			height = h + "px";
		}
		oDiv = null;
		EWAC_UISkinEvents._FOCUS_OBJ = obj;
	},
	MouseOut : function(obj) {
		var oDiv = EWAC_UISkinEvents._GetDiv(obj);
		oDiv.style.display = "none";
		oDiv = null;
	},
	MouseDown : function() {
		var obj = EWAC_UISkinEvents._FOCUS_OBJ;
		var oDiv1 = EWAC_UISkinEvents._GetDiv1(obj);
		var oDiv = EWAC_UISkinEvents._GetDiv(obj);
		with (oDiv1.style) {
			display = "";
			top = oDiv.style.top;
			left = oDiv.style.left;
			height = oDiv.style.height;
			width = oDiv.style.width;
		}
		oDiv1 = oDiv = null;
		var o1 = $X("EWAC_SELECT_TYPE");
		o1.value = "Items$" + obj.id;
		o1.onchange();
	},
	_GetDiv : function(obj) {
		var doc = obj.ownerDocument;
		var oDiv = doc.getElementById("_EWAC_RED_DIV_0");
		if (oDiv == null) {
			oDiv = doc.createElement("div");
			oDiv.id = "_EWAC_RED_DIV_0";
			with (oDiv.style) {
				border = "1px dotted green";
				position = "absolute";
				display = "none";
				cursor = "pointer";
				backgroundColor = "green";
				filter = "alpha(opacity=30)";
				opacity = "0.3";

			}
			oDiv.onmousedown = function() {
				EWAC_UISkinEvents.MouseDown();
			}
			doc.body.appendChild(oDiv);
			var oDiv1 = doc.createElement("div");
			oDiv1.id = "_EWAC_RED_DIV_1";
			with (oDiv1.style) {
				border = "1px solid yellow";
				position = "absolute";
				display = "none";
				backgroundColor = "yellow";
				filter = "alpha(opacity=30)";
				opacity = "0.3";
			}
			doc.body.appendChild(oDiv1);
			oDiv1 = null;
		}
		doc = null;
		return oDiv;
	},
	_GetDiv1 : function(obj) {
		var doc = obj.ownerDocument;
		var oDiv1 = doc.getElementById("_EWAC_RED_DIV_1");
		doc = null;
		return oDiv1;
	}
};
function EWAC_UISkinClass(userClass) {
	this._Skin = new EWAC$Skin();
	this._UserClass = userClass;
	this._XItems = this._UserClass.Config.Items;
	this._UItems = this._UserClass.ItemsTablesSet;
	this._Menus = this._UserClass.MenuTablesSet;

	this._CurSkin;
	this._FrameSkin;
	this._AllSkin;
	this.FrameTag;
	this.Lang = "zhcn";

	this.CreateHtml = function() {
		var p = EWA.RV_STATIC_PATH ? EWA.RV_STATIC_PATH : top.EWA.RV_STATIC_PATH;
		var skinName = this._GetPageItemValue("SkinName");
		var frameTag = this._GetPageItemValue("FrameTag");
		this.FrameTag = frameTag;
		this._CurSkin = this._Skin.GetSkin(skinName);
		this._FrameSkin = this._CurSkin.List.GetItem(frameTag);
		this._AllSkin = this._CurSkin.List.GetItem("All");
		// Top
		var s1 = this._CreateTop();
		// console.log(s1)
		// Header
		s1 += "<tr>" + this._CreateHeader() + "</tr>";
		if (frameTag == "ListFrame") {
			s1 += this._CreateListFrameHtml();
		} else if (frameTag == "Combine") {
			s1 += this._CreateCombineHtml();
		} else if (frameTag == "Complex") {

			s1 += this._CreateComplexHtml();

		} else {
			s1 += this._CreateFrameHtml();
		}
		s1 += this._CreateBottom();
		if (frameTag == "Complex") {
			var buts = this._CreateComplexMenus();
			s1 = s1.replace('<!--buts-->', buts);
		}

		s1 = s1.replace(/@SYS_CONTEXTPATH/ig, p);
		s1 = s1.replace(/@rv_ewa_style_path/ig, p);
		s1 = s1.replace(/@rv_remote_ewa_style_path/ig, p);
		return s1;
	};
	this.CreateFrameHtml = function() {
		var skinName = this._GetPageItemValue("SkinName");
		var frameTag = this._GetPageItemValue("FrameTag");
		this.FrameTag = frameTag;
		this._CurSkin = this._Skin.GetSkin(skinName);
		this._FrameSkin = this._CurSkin.List.GetItem(frameTag);
		this._AllSkin = this._CurSkin.List.GetItem("All");
		var s1 = "";
		s1 += this._FrameSkin.Top;
		s1 += "<TR>\r\n\t" + this._CreateHeader(true) + "</TR>";
		s1 += this._CreateFrameHtml(true);
		s1 += this._FrameSkin.Bottom;
		return s1;
	};
	this._CreateCombineHtml = function() {
		var ss = [];
		ss.push("<tr><td>");
		for (var i = 0; i < this._UItems.Count(); i++) {
			tbs = this._UItems.GetItem(i);
			if (tbs.IsDelete) {
				continue;
			}
			var tag = tbs.Tables.GetItem("Tag").GetSingleValue();
			if (tag != 'CombineItem') {
				continue;
			}
			var xItem = this._GetXItem(tag);
			var des1 = this._GetDes(tbs);
			var xname = this._GetXItemValue(tbs, "CombineFrame", "CbXmlName");
			var iname = this._GetXItemValue(tbs, "CombineFrame", "CbItemName");
			var grp = this._GetXItemValue(tbs, "CombineFrame", "CbGrp");
			var install = this._GetXItemValue(tbs, "CombineFrame", "CbInstall");
			var item = this._GetXItem(tag).Template.Html;
			var val = '<div id="'
				+ tbs.Name
				+ '" onmouseover=\'self.parent.EWAC_UISkinEvents.MouseOver(this,true)\' style="font-size:14px;font-family:arial;color:#ccc;line-height:40px">'
				+ xname + " - " + iname + ' - ' + install + '</div>';

			item = item.replace("@DES", des1 + ' [分组：' + grp + ']');
			item = item.replace("{__EWA_VAL__}", val);
			item = item.replace("<div", "<div style='width:100%'")
			ss.push(item);
		}
		return ss.join("") + "</td></tr>";
	};
	this._CreateComplexHtml = function() {
		var ss = [];
		for (var i = 0; i < this._UItems.Count(); i++) {
			tbs = this._UItems.GetItem(i);
			if (tbs.IsDelete) {
				continue;
			}
			var tag = tbs.Tables.GetItem("Tag").GetSingleValue();
			if (tag != 'ComplexItem') {
				continue;
			}
			var xItem = this._GetXItem(tag);
			var des1 = this._GetDes(tbs);
			var xname = this._GetXItemValue(tbs, "CombineFrame", "CbXmlName");
			var iname = this._GetXItemValue(tbs, "CombineFrame", "CbItemName");
			var grp = this._GetXItemValue(tbs, "CombineFrame", "CbGrp");
			var install = this._GetXItemValue(tbs, "CombineFrame", "CbInstall");
			var item = this._GetXItem(tag).Template.Html;
			var val = '<div id="' + tbs.Name + '" onmouseover=\'self.parent.EWAC_UISkinEvents.MouseOver(this,true)\' '
				+ 'style="font-size:14px;font-family:arial;color:#ccc;line-height:40px">' + xname + " - " + iname + ' - '
				+ install + '</div>';

			item = item.replace("@DES", des1 + ' [' + tbs.Name + ']');
			item = item.replace("class=\"fcaption", "class=\"c" + (i % 5) + " fcaption");
			item = item.replace("{__EWA_VAL__}", val);
			item = item.replace("<div", "<div style='width:100%'")
			ss.push(item);
		}
		return ss.join("");
	};
	this._CreateComplexMenus = function() {
		var ss = [];
		for (var i = 0; i < this._Menus.Count(); i++) {
			var icon = this._Menus.GetItem(i).Tables.GetItem('Icon').GetValue('Icon');
			var name = this._Menus.GetItem(i).Tables.GetItem('Name').GetValue('Name');
			var s = "<td class=tdbut><div class=but><i id='Menu$" + name
				+ "' onclick='parent.complex_menu_click(this)' class='fa " + icon + "'></i></div></td>";
			ss.push(s);
		}
		return ss.join('');
	}
	this._CreateListFrameHtml = function() {
		var p = EWA.RV_STATIC_PATH ? EWA.RV_STATIC_PATH : EWA.CP;
		var s2 = "<tr>";
		for (var i = 0; i < this._UItems.Count(); i++) {
			tbs = this._UItems.GetItem(i);
			if (tbs.IsDelete) {
				continue;
			}
			var tag = tbs.Tables.GetItem("Tag").GetSingleValue();
			if (tag.toUpperCase() == "HIDDEN" || tag.toUpperCase() == "DATATYPE") {
				continue;
			}
			var xItem = this._GetXItem(tag);
			var item = this._FrameSkin.Item;
			var des1 = this._GetDes(tbs);
			var memo = this._GetMemo(tbs);
			var parentStyle = this._GetXItemValue(tbs, "ParentStyle", "ParentStyle");
			item = item.replace("!!", " style='" + parentStyle + "' ");
			var style = this._GetXItemValue(tbs, "Style", "Style");
			var tmp = xItem.Template.Html.replace("!!", " style='" + style + "' ");
			item = item.replace("{__EWA_ITEM__}", tmp);
			item = item.replace("{__EWA_DES__}", des1);
			item = item.replace("{__EWA_MSG__}", memo);
			item = item.replace("{__EWA_VAL__}", des1);
			item = item.replace("@SYS_CONTEXTPATH", p);
			s2 += item;
		}
		s2 += "</tr>"
		var s1 = "";
		for (i = 0; i < 5; i++) {
			s1 += s2;
		}
		return s1;
	};
	this._CreateFrameHtml = function(noEvent) {
		var s1 = "";
		var buttonRow = this._FrameSkin.ItemButton;
		var v1 = "";
		if (!noEvent) {
			v1 = "onmouseover='self.parent.EWAC_UISkinEvents.MouseOver(this,true)' ";
			v1 += "style='cursor:pointer'";
		}
		var p = EWA.RV_STATIC_PATH ? EWA.RV_STATIC_PATH : EWA.CP;
		for (var i = 0; i < this._UItems.Count(); i += 1) {
			var tbs = this._UItems.GetItem(i);
			if (tbs.IsDelete) {
				continue;
			}
			var tag = tbs.Tables.GetItem("Tag").GetSingleValue();
			if (tag.toUpperCase() == "HIDDEN" || tag.toUpperCase() == "DATATYPE") {
				continue;
			}
			var item = this._FrameSkin.Item;
			var xItem = noEvent ? "{" + tbs.Name + "#ITEM}" : this._GetXItem(tag).Template.Html;
			var des = noEvent ? "{" + tbs.Name + "#DES}" : this._GetDes(tbs);
			var memo = noEvent ? "{" + tbs.Name + "#MEMO}" : this._GetMemo(tbs);
			if (tag.toUpperCase() == "BUTTON" || tag.toUpperCase() == "SUBMIT") {
				var ss;
				if (noEvent) {
					ss = "{" + tbs.Name + "#ITEM} ";
				} else {
					ss = this._GetXItem(tag).Template.Html.replace("{__EWA_DES__}", des);
					ss = ss.replace("!!", "id='" + tbs.Name + "' ");
				}
				var bs = buttonRow.split("{__EWA_ITEM__}");
				buttonRow = bs[0] + ss + "{__EWA_ITEM__}" + bs[1];
			} else {
				item = item.replace("{__EWA_ITEM__}", xItem);
				item = item.replace("{__EWA_DES__}", des);
				item = item.replace("{__EWA_MSG__}", memo);
				item = item.replace("{__EWA_VAL__}", "");
				item = item.replace("@SYS_CONTEXTPATH", p);
				s1 += "<TR SHOW_MSG=\"1\"" + (noEvent ? "" : " id='" + tbs.Name + "' " + v1) + ">\r\n\t" + item + "\r\n</TR>";
			}
		}
		buttonRow = buttonRow.replace("{__EWA_ITEM__}", "").replace("{__EWA_COL_SPAN__}", "3");
		s1 += "<TR>" + buttonRow + "</TR>";
		return s1;
	};
	this._GetDes = function(dataTables) {
		return dataTables.Tables.GetItem("DescriptionSet").SearchValue("Lang=" + this.Lang, "Info");
	};
	this._GetMemo = function(dataTables) {
		return dataTables.Tables.GetItem("DescriptionSet").SearchValue("Lang=" + this.Lang, "Memo");
	};
	this._GetXItemValue = function(dataTables, paraName, tagName) {
		var tb = dataTables.Tables.GetItem(paraName);
		return tb.GetValue(tagName);
	};
	this._CreateTop = function() {
		var s1 = "";// this._AllSkin.Top + this._AllSkin.BodyStart;

		var addCss = this._GetPageItemValue("AddCss");

		if (addCss) {
			var addCsslink = "<style>" + addCss + "</style>";
			s1 += addCsslink;
		}

		var w = this._GetPageItemValue("Size", "Width");
		var h = this._GetPageItemValue("Size", "Height");
		var va = this._GetPageItemValue("Size", "VAlign");
		var ha = this._GetPageItemValue("Size", "HAlign");
		var st = "";
		if (w != null && w.length > 0) {
			st += "width: " + w + ";";
		}
		if (h != null && h.length > 0) {
			st += "height: " + h + ";";
		}
		st += "'";
		s1 += "<div id=EWA_MAIN_FRAME><div id='Test1'><table border=0 cellpadding=0 cellspacing=0 style='" + st + "'";
		if (ha != null && ha.length > 0) {
			s1 += " align=" + ha;
		}
		s1 += "><tr><td";
		if (va != null && va.length > 0) {
			s1 += " valign=" + va;
		}
		s1 += ">";
		var addTop = this._GetPageItemValue("AddHtml", "Top");
		if (addTop != null) {
			s1 += addTop;
		}
		s1 += this._FrameSkin.Top;
		return s1;
	};
	this._CreateHeader = function(noEvent) {
		var h = this._FrameSkin.ItemHeader;
		if (this.FrameTag == "ListFrame") {
			var v1 = "onmouseover='self.parent.EWAC_UISkinEvents.MouseOver(this,false)' ";
			v1 += "style='cursor:pointer'";
			var s1 = "<tr>";
			for (var i = 0; i < this._UItems.Count(); i += 1) {
				var tbs = this._UItems.GetItem(i);
				if (tbs.IsDelete) {
					continue;
				}
				var des = "<div>" + this._GetDes(tbs) + "</div>";
				var s2 = h.replace("!!", v1 + " id='" + tbs.Name + "'");
				s1 += s2
					.replace("{__EWA_ITEM__}", des + '<span style="font-size:9px;font-weight:normal">' + tbs.Name + '</span>');
			}
			s1 += "</tr>";
			return s1;
		} else {
			var des1 = this._GetDes(this._UserClass.PageTables);
			return h.replace("{__EWA_ITEM__}", noEvent ? "{PAGE#DES}" : des1);
		}
	};
	this._CreateFooter = function() {
		return this._FrameSkin.ItemHeader;
	};
	this._CreateBottom = function() {
		var s1 = this._FrameSkin.Bottom;
		var addBottom = this._GetPageItemValue("AddHtml", "Bottom");
		if (addBottom != null) {
			s1 += addBottom;
		}
		s1 += this._AllSkin.Bottom;
		s1 += "</td></tr></table></div></div>";
		return s1;
	};
	this.CreateStyle = function() {
		var s1 = "" + this._Skin.MainSkin.Style + "\r\n";
		s1 += this._AllSkin.Style + "\r\n";
		s1 += this._FrameSkin.Style + "\r\n";
		var p = EWA.RV_STATIC_PATH ? EWA.RV_STATIC_PATH : top.EWA.RV_STATIC_PATH;
		s1 = s1.replace(/@SYS_CONTEXTPATH/ig, p);
		return s1;
	};
	this._GetPageItemValue = function(tbName, tagName) {
		if (tagName == null) {
			return this._UserClass.PageTables.Tables.GetItem(tbName).GetSingleValue();
		} else {
			return this._UserClass.PageTables.Tables.GetItem(tbName).GetValue(tagName);
		}
	};
	this._GetXItem = function(name) {
		return this._XItems.XItems.GetXItem(name);
	};
}

// ---------------------------------------
function EWAC$FrameItem() {
	this.Name = "";
	this.Head = "";
	this.Script = "";
	this.Style = "";
	this.Title = "";
	this.BodyStart = "";
	this.BodyEnd = "";
	this.ItemHeader = "";
	this.Item = "";
	this.ItemButton = "";
	this.Bottom = "";
	this.Top = "";
}

function EWAC$FrameItemList() {
	this.Name;
	this.List = new EWAC$SetBase();
}

function EWAC$Skin() {
	this.MainSkin = new EWAC$FrameItem();
	this.SkinList = new EWAC$SetBase();
	this.Xml = new EWA_XmlClass();
	// ---------------------------------------------
	this.GetSkin = function(skinName) {// 获取皮肤
		return this.SkinList.GetItem(skinName);
	};
	// ----------------初始化皮肤---------------------
	this.Init = function() {
		if (window._CFG_SKIN) {
			this.Xml.LoadXml(_CFG_SKIN);
		} else {
			this.Xml.LoadXmlFile(EWA.CP + "/EWA_DEFINE/cgi-bin/xml/?TYPE=CFG_XML&XMLNAME=EwaSkin.xml");
		}
		var mainNode = this.Xml.GetElement("SkinRoot/MainSkin");
		this._InitSkin(mainNode, this.MainSkin);
		var skins = this.Xml.GetElements("SkinRoot/Skin");
		for (var i = 0; i < skins.length; i++) {
			var sks = new EWAC$FrameItemList();
			var name = this.Xml.GetElementAttribute(skins[i], "Name");
			this.SkinList.AddObject(name, sks);
			var itemNodes = this.Xml.GetElements("FrameItem", skins[i]);
			for (var m = 0; m < itemNodes.length; m++) {
				var frameItem = new EWAC$FrameItem();
				this._InitSkin(itemNodes[m], frameItem);
				frameItem.Name = this.Xml.GetElementAttribute(itemNodes[m], "FrameType");
				sks.List.AddObject(frameItem.Name, frameItem);
			}
		}
	};
	this._InitSkin = function(node, frameItem) {
		frameItem.Name = this.Xml.GetElementAttribute(node, "Name");
		frameItem.Head = this._GetText(node, "Head");
		frameItem.Script = this._GetText(node, "Script");
		frameItem.Style = this._GetText(node, "Style");
		frameItem.Title = this._GetText(node, "Title");
		frameItem.BodyStart = this._GetText(node, "BodyStart");
		frameItem.BodyEnd = this._GetText(node, "BodyEnd");
		frameItem.ItemHeader = this._GetText(node, "ItemHeader");
		frameItem.ItemButton = this._GetText(node, "ItemButton");
		frameItem.Item = this._GetText(node, "Item");
		frameItem.Bottom = this._GetText(node, "Bottom");
		frameItem.Top = this._GetText(node, "Top");
	};
	this._GetText = function(node, tagName) {
		var n1 = this.Xml.GetElement(tagName, node);

		if (n1 == null) {
			return "";
		} else {
			var txt = this.Xml.GetElementText(n1);
			return txt == null ? "" : txt.trim();
		}
	};
	this.Init();
}
