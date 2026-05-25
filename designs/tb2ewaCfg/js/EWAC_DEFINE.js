/**
 * @include "../../EWA_STYLE/js/EWA.js"
 */
function EWAC$Add() {
	this.Text;
	this.XmlPath;
	this.SetMethod;
}
function EWAC$Adds() {
	this.Adds = new Array();
	this.AddNodes = [];
	this._InitFromXml = function(classXml, node) {
		var nl = classXml.GetElements("Adds/Add", node);
		if (nl == null) {
			return;
		}
		for (var i = 0; i < nl.length; i++) {
			var add = new EWAC$Add();
			add.Text = classXml.GetElementText(nl[i]);
			add.XmlPath = classXml.GetElementAttribute(nl[i], "XmlPath");
			add.SetMethod = classXml.GetElementAttribute(nl[i], "SetMethod");
			this.Adds.push(add);
			this.AddNodes.push(nl[i]);
		}
	};
	this.CreateAdds = function(classXml) {
		this._AddXmlPathNode = {};
		for (var i = 0; i < this.Adds.length; i++) {
			var add = this.Adds[i];

			var node;

			if (this._AddXmlPathNode[add.XmlPath]) {
				node = this._AddXmlPathNode[add.XmlPath];
			} else {
				node = classXml.GetOrCreateElement(add.XmlPath);
				this._AddXmlPathNode[add.XmlPath] = node;
			}
			if (node != null) {
				var t1 = this._CreateValue(add.Text, classXml);

				if (add.SetMethod == "CDATA") {

					classXml.SetCData(t1, node);
				} else {
					var set = node.ownerDocument.createElement("Set");
					// classXml.SetAttribute(node.nodeName, t1, node);
					var nodeXml = this.AddNodes[i]; // xml element
					var atts = nodeXml.getElementsByTagName('Att');
					// <Att Name="HiddenFields" Value="butRestore"></Att>
					for (var ia = 0; ia < atts.length; ia++) {
						var attName = atts[ia].getAttribute('Name');
						var attVal = atts[ia].getAttribute('Value');
						set.setAttribute(attName, attVal);
					}
					node.appendChild(set);
					console.log(node);
				}
				// alert(node.xml);
			}
		}

	};
	this._CreateValue = function(text, classXml) {
		if (text == null)
			return "";
		// var r = /\{\$[^\}]*\}/ig; // xml value
		var r = /\{.+\}/ig;
		var m = text.match(r);
		if (m == null) {
			return text;
		}
		var t1 = text;
		for (var i = 0; i < m.length; m++) {
			var v = this._QueryValue(m[i], classXml);
			t1 = t1.replace(m[i], v);
		}

		var r1 = /\{\@[^\}]*\}/ig;// js value
		var m1 = text.match(r1);
		if (m1 == null) {
			return t1;
		}
		for (var i = 0; i < m1.length; i++) {
			var v = this._EvalValue(m1[i]);
			t1 = t1.replace(m1[i], v);
		}
		return t1;
	}
	this._EvalValue = function(s1) {
		var s2 = s1.substring(2, s1.length - 1);
		return eval(s2);
	}
	this._QueryValue = function(s1, classXml) {
		var s2 = s1.substring(2, s1.length - 1);
		var s3 = s2.split('#');
		var node = classXml.GetElement(s3[0]);
		if (node == null) {
			return s1;
		}
		return node.getAttribute(s3[1]);
	}
}

function EWAC$Para() {
	this.XmlPath = null;
	this.Val = null;
	this.Name = null;
}

function EWAC$Paras() {
	this.Paras = new Array();
	this._InitFromXml = function(classXml, node) {
		var nl = classXml.GetElements("Para", node);
		if (nl == null) {
			return;
		}
		for (var i = 0; i < nl.length; i++) {
			var para = new EWAC$Para();
			para.XmlPath = classXml.GetElementAttribute(nl[i], "XmlPath");
			para.Val = classXml.GetElementAttribute(nl[i], "Val");
			para.Name = classXml.GetElementAttribute(nl[i], "Name");
			this.Paras.push(para);
		}
	};
	this.CreateParas = function(classXml, curNode) {
		for (var i = 0; i < this.Paras.length; i++) {
			var para = this.Paras[i];
			var node = classXml.GetOrCreateElement(para.XmlPath, curNode);
			var val = this._EvalValue(para.Val);
			node.setAttribute(para.Name, val);
		}
	};

	this._EvalValue = function(s1) {
		try {
			return eval(s1);
		} catch (e) {
			alert(s1 + '\r\n' + e);
		}
	}
}

function EWAC$Field() {
	this.Name;
	this.Length;
	this.Description;
	this.IsPk = false;
	this.IsFk = false;
	this.FkTableName;
	this.FkColumnName;
	this.IsChecked;
	this.Parent; // EWAC$Fields
	this.CreateFkSql = function() {
		if (!this.IsFk) {
			return "";
		}
		return "SELECT * FROM " + this.FkTableName + " WHERE 1=1";
	};
	this.GetTag = function() {
		if (this.FrameTag == "Frame" && this.Parent.Parent.SelectedTypeName != 'V') {
			if (this.Type.toLowerCase().indexOf("date") >= 0 || this.Type.toLowerCase().indexOf("time") >= 0) {
				return "date";
			}
			if (this.IsFk) {
				return "select";
			}
			if (this.Length < 500) {
				return "text";
			} else {
				return "textarea";
			}
		} else {
			return "span";
		}
	};
	this.GetOrder = function() {
		if (this.FrameTag.toUpperCase() !== "ListFrame".toUpperCase()) {
			return "0";
		}
		var t = this.Type.toLowerCase();
		if (t.indexOf("blob") >= 0 || t.indexOf("image") >= 0) {
			return "0";
		}
		if (this.Length > 200) {
			return "0"
		}
		return "1";
	};
	this.GetSearch = function() {
		if (this.FrameTag.toUpperCase() !== "ListFrame".toUpperCase()) {
			return "";
		}
		const t = this.GetType().toLowerCase();
		console.log(t, this.Length, this.Name, this.Description);
		if (t == "string" && this.Length <= 200) {
			return "text";
		}
		return "";
	};
	this.GetType = function() {
		var t = this.Type.toLowerCase();
		if (t.indexOf("date") >= 0 || t.indexOf("time") >= 0) {
			return "Date";
		}
		if (t.indexOf("int") >= 0) {
			return "Int";
		}
		if (t.indexOf("num") >= 0 || t.indexOf("money") >= 0 || t.indexOf('decimal') >= 0) {
			return "Number";
		}
		if (t.indexOf("blob") >= 0 || t.indexOf("image") >= 0) {
			return "Binary";
		}
		return "String";
	};
	this.GetFormat = function() {
		var t = this.Type.toLowerCase();
		if (t.indexOf("date") >= 0 || t.indexOf("time") >= 0) {
			if (this.FrameTag == "Frame") {
				return "Date";
			} else {
				return "DateShortTime";
			}
		}
		if (t.indexOf("num") >= 0 || t.indexOf("money") >= 0) {
			return "LeastMoney"; //最短的货币格式
		}
		return "";
	};
	this.GetParentStyle = function() {
		if (this.FrameTag != "ListFrame") {
			return "";
		}
		var t = this.Type.toLowerCase();
		if (t.indexOf("date") >= 0 || t.indexOf("time") >= 0) {
			return "width:120px; text-align:center"
		}
	}
}
function EWAC$Fields() {
	this.Fields = new EWAC$SetBase();
	this.TableName;
	this.CfgName;
	this.Pk = "";
	this.PkFields = new Array();
	this.TreeKey = "";
	this.TreeParentKey = "";
	this.TreeText = "";
	this.TreeLevel = "";
	this.TreeOrder = "";
	this.Parent = {};// EWA_DefineClass
	this.GetSqlRelationSelect = function() {
		var s1 = "SELECT * FROM " + this.TableName + " WHERE NOT " + this.Pk + " IN( \r\n\t SELECT " + this.Pk
			+ " FROM RelationTable B WHERE B.REF_ID =@REF_ID\r\n)";
		console.log(s1);
		return s1;
	};
	this.GetSqlRelationUpdate = function() {
		var s1 = "INSERT INTO RelationTable(" + this.Pk + ",REF_ID)\r\n\t " + "SELECT DISTINCT " + this.Pk + ", @REF_ID FROM "
			+ this.TableName + " WHERE " + this.Pk + " IN (@IDS_SPLIT) AND NOT " + this.Pk + " IN( \r\n\t SELECT " + this.Pk
			+ " FROM RelationTable B WHERE B.REF_ID =@REF_ID\r\n)";
		console.log(s1);
		return s1;
	};
	/**
	 * 获取登录SQL语句
	 */
	this.GetSqlLogin = function() {
		var s1 = "SELECT *, 1 AS CNT FROM " + this.TableName + " WHERE ";
		var m = 0;
		for (var i = 0; i < this.Fields.Count(); i += 1) {
			var f = this.Fields.GetItem(i);
			if (f.IsChecked) {
				if (m > 0) {
					s1 += " AND ";
				} else {
					m = 1;
				}
				s1 += f.Name + "=@" + f.Name;
			}
		}
		console.log(s1);
		return s1;
	};
	this._GetTreeFields = function() {
		var node = _EWAC_DEFINE._Xml.GetElement("EasyWebTemplate/Page/Tree/Set");
		this.TreeKey = node.getAttribute("Key");
		this.TreeParentKey = node.getAttribute("ParentKey");
		this.TreeText = node.getAttribute("Text");
		this.TreeLevel = node.getAttribute("Level");
		this.TreeOrder = node.getAttribute("Order");
	};
	/**
	 * 获取树的修改名称SQL
	 */
	this.GetSqlTreeNodeRename = function() {
		this._GetTreeFields();
		const mdateFieldName = _GetMDateField();
		var s1 = "UPDATE " + this.TableName + " SET " + this.TreeText + " = @" + this.TreeText
			+ (mdateFieldName ? "," + mdateFieldName + " = @sys_date" : "")
			+ " WHERE " + this.TreeKey + "=@" + this.TreeKey;
		console.log(s1);
		return s1;
	};
	/**
	 * 获取树的删除节点SQL
	 */
	this.GetSqlTreeNodeDelete = function() {
		this._GetTreeFields();
		var s1 = "DELETE FROM " + this.TableName + " WHERE " + this.TreeKey + "=@" + this.TreeKey;
		console.log(s1);
		return s1;
	};
	/**
	 * 获取树的新增节点SQL
	 */
	this.GetSqlTreeNodeNew = function() {
		this._GetTreeFields();
		var s00 = 'SELECT CASE WHEN MAX(' + this.TreeLevel + ') IS NULL THEN 1 ELSE MAX(' + this.TreeLevel
			+ ') + 1 END LVL FROM ' + this.TableName + ' WHERE ' + this.TreeKey + '=@' + this.TreeParentKey;

		var s01 = 'SELECT CASE WHEN MAX(' + this.TreeOrder + ') IS NULL THEN 1 ELSE MAX(' + this.TreeOrder
			+ ') + 1 END ORD FROM ' + this.TableName + ' WHERE ' + this.TreeParentKey + '=@' + this.TreeParentKey;

		var s1 = "INSERT INTO " + this.TableName + "(" + this.TreeParentKey + ", " + this.TreeText + ", " + this.TreeLevel + ", "
			+ this.TreeOrder + ")\n\t";
		var s2 = "SELECT @" + this.TreeParentKey + ", @" + this.TreeText + ", A.LVL, B.ORD FROM \n\t\t(" + s00 + ") A,\n\t\t ("
			+ s01 + ") B";
		var s3 = "SELECT * FROM " + this.TableName + " WHERE " + this.TreeParentKey + "=@" + this.TreeParentKey + " AND "
			+ this.TreeText + "=@" + this.TreeText;
		s1 = s1 + s2 + ";\n" + s3;
		console.log(s1);
		return s1;
	};
	/**
	 * 获取树的加载SQL
	 */
	this.GetSqlTreeLoad = function() {
		this._GetTreeFields();
		var s1 = "SELECT * FROM " + this.TableName + " WHERE 1=1 ORDER BY ";
		var order = this.TreeLevel + " , " + this.TreeOrder;
		s1 = s1 + order;
		console.log(s1);
		return s1;
	};

	this.GetSqlTreeChangeNode = function() {
		this._GetTreeFields();
		var s1 = 'UPDATE TABLE_CHANGED SET ' + this.TreeKey + '=@' + this.TreeKey + ' WHERE TABLE_CHANGED_PK IN (@IDS_SPLIT)';
		return s1;
	}
	/**
	 * 获取主键的参数表达
	 * 
	 * @return {}
	 */
	this.GetPkParas = function() {
		var s1 = [];
		if (this.PkFields.length > 0) {
			for (var i = 0; i < this.PkFields.length; i += 1) {
				var s2 = this.PkFields[i].Name + "=@" + this.PkFields[i].Name;
				s1.push(s2);
			}
			return "&" + s1.join("&");
		} else {
			return "";
		}
	};
	/**
	 * 获取主键表达式
	 */
	this._GetSqlPk = function(notPk, prefix) {
		const ss = [];
		if (!notPk) {
			const pks = this.userDefinedPkFields || this.PkFields; // 使用自定义主键或表定义主键

			for (let i = 0; i < pks.length; i += 1) {
				const s1 = (prefix ? prefix + "." : "") + pks[i].Name + " = @" + pks[i].Name;
				ss.push(s1);
			}
		}
		if (window.EWAC_DEF && EWAC_DEF.WHERE) {
			for (const n in EWAC_DEF.WHERE) {
				const f = this.Fields.GetItem(n);
				if (f == null) {
					continue;
				}
				const s1 = n + " = @" + EWAC_DEF.WHERE[n];
				ss.push(s1);
			}
		}
		if (!notPk) {
			if (ss.length == 0) {
				return " 1>2 -- table not defined pk";
			}
		}
		return ss.join("\n\tAND ");
	};

	this._GetMDateField = function() {
		for (let i = 0; i < this.Fields.Count(); i += 1) {
			const f = this.Fields.GetItem(i);
			if (f.Name.toUpperCase().indexOf('_MDATE') > 0) {
				// 默认修改日期
				return f.Name;
			}

		}
		return null;
	};
	this._GetCDateField = function() {
		for (let i = 0; i < this.Fields.Count(); i += 1) {
			const f = this.Fields.GetItem(i);
			if (f.Name.toUpperCase().indexOf('_CDATE') > 0) {
				// 默认创建日期
				return f.Name;
			}

		}
		return null;
	};
	this._GetStatusField = function() {
		const node = _EWAC_DEFINE._Xml.GetElement("EasyWebTemplate/Page/PageSize/Set");
		if (!node) { //目前只有ListFrame有状态字段
			return null;
		}
		const statusField = node.getAttribute("StatusField");
		if (!statusField || statusField.toUpperCase().indexOf("[OBJECT") == 0) {
			return null;
		}
		return statusField;
	};
	this._GetSqlUpdateStatus = function(status) {
		if (this.PkFields.length == 0 && (this.userDefinedPkFields == null || this.userDefinedPkFields.length == 0)) {
			$Tip('请注意，主键没有定义，可以上一步进行设置');
		}

		const statusField = this._GetStatusField();
		if (!statusField) {
			$Tip('请注意，状态字段没有定义，可以上一步进行设置');
			statusField = " 没有定义状态字段 ";
		}
		const mdateFieldName = this._GetMDateField();
		var s1 = "UPDATE " + this.TableName + " SET " + statusField + "='" + status + "' "
			+ (mdateFieldName ? ", " + mdateFieldName + " = @sys_date" : "") + " WHERE "
			+ this._GetSqlPk();
		console.log(s1);
		return s1;
	};
	/**
	 * Listframe delete模式（逻辑删除）
	 */
	this.GetSqlDeleteA = function() {
		return this._GetSqlUpdateStatus('DEL');
	};

	// 恢复数据 2016-12-01
	this.GetSqlRestore = function() {
		return this._GetSqlUpdateStatus('USED');
	};
	/**
	 * 获取树的删除数据SQL
	 */
	this.GetSqlDelete = function() {
		var s1 = "DELETE FROM " + this.TableName + " WHERE " + this._GetSqlPk();
		return s1;
	};
	/**
	 * 获取加载数据SQL
	 */
	this.GetSqlSelect = function() {
		var s1 = "SELECT A.* FROM " + this.TableName + " A WHERE " + this._GetSqlPk(null, "A");
		console.log(s1);
		return s1;
	};
	/**
	 * 加载Listframe 数据
	 */
	this.GetSqlSelectLF = function() {
		var s2 = this._GetSqlPk(true); // 1=1
		var s1 = "SELECT A.* FROM " + this.TableName + " A WHERE " + (s2 == "" ? "1=1" : s2);
		var node = _EWAC_DEFINE._Xml.GetElement("EasyWebTemplate/Page/PageSize/Set");
		var recycle = node.getAttribute("Recycle"); // 回收站资料

		if (1 == recycle) {
			const statusField = this._GetStatusField();
			if (!statusField) {
				$Tip('请注意，状态字段没有定义，可以上一步进行设置');
				statusField = " 没有定义状态字段 ";
			}
			s1 += "\n\t-- ewa_test @EWA_RECYCLE is null"
			s1 += "\n\tAND A." + statusField + " =  'USED'";
			s1 += "\n\t-- ewa_test @EWA_RECYCLE = '1'"
			s1 += "\n\tAND A." + statusField + " =   'DEL'";
			s1 += "\n\t-- ewa_test\n"
		}
		let order = null;
		for (var i = 0; i < this.Fields.Count(); i += 1) {
			var f = this.Fields.GetItem(i);
			if (f.Type.toUpperCase().indexOf("IDENTITY") > 0) {
				order = f.Name; //  默认按自增字段排序
				break;
			}
		}
		if (order == null) {
			const mdateFieldName = this._GetMDateField();
			if (mdateFieldName) {
				order = mdateFieldName; // 默认按修改日期排序
			}
		}
		if (order) {
			s1 = s1 + "\nORDER BY A." + order + " DESC";
		}
		console.log(s1);
		return s1;
	};

	/**
	 * 获取更新SQL
	 */
	this.GetSqlUpdate = function() {
		const statusField = this._GetStatusField();
		const cdateFieldName = this._GetCDateField();
		var s1 = "UPDATE " + this.TableName + " SET ";
		var m = 0;
		for (var i = 0; i < this.Fields.Count(); i += 1) {
			var f = this.Fields.GetItem(i);
			if (f.Type.toUpperCase().indexOf("IDENTITY") > 0 || f.IsPk || f.IsChecked == false) {
				continue;
			}
			if (cdateFieldName && f.Name.toUpperCase() == cdateFieldName.toUpperCase()) {
				// 默认创建日期
				continue;
			}
			if (statusField && statusField.toUpperCase() == f.Name.toUpperCase()) {
				// 状态字段
				continue;
			}
			if (m > 0) {
				s1 += ",";
			} else {
				m = 1;
			}

			s1 += "\n\t" + f.Name + " = @" + this._GetPara(f);
		}
		s1 = s1 + "\nWHERE " + this._GetSqlPk();
		console.log(s1);
		return s1;
	};
	/**
	 * 获取新增数据SQL
	 */
	this.GetSqlNew = function() {
		let s1 = "INSERT INTO " + this.TableName + " (";
		let s2 = ") VALUES ( ";
		let m = 0;
		const statusField = this._GetStatusField();
		for (let i = 0; i < this.Fields.Count(); i += 1) {
			const f = this.Fields.GetItem(i);
			if (f.Type.toUpperCase().indexOf("IDENTITY") > 0 || f.IsChecked == false) {
				continue;
			}
			if (m > 0) {
				s1 += ", ";
				s2 += ", ";
			} else {
				m = 1;
			}
			s1 += f.Name;
			if (statusField && statusField.toUpperCase() == f.Name.toUpperCase()) {
				s2 += "'USED'";
			} else {
				s2 += "@" + this._GetPara(f);
			}
		}
		s1 = s1 + s2 + ")";
		console.log(s1);
		return s1;
	};
	this._GetPara = function(f) {
		if (window.EWAC_DEF && EWAC_DEF.FIELD_MAP) {
			var n = f.Name.toUpperCase().trim();
			if (EWAC_DEF.FIELD_MAP[n]) {
				return EWAC_DEF.FIELD_MAP[n];
			}
		}
		const name = f.Name.toUpperCase().trim();
		if (f.GetType() == 'Date') {
			return "SYS_DATE";
		} else if (name.indexOf('UNID') >= 0 && name !='REF_UNID' ) {
			return "SYS_UNID";
		} else if (name == "IP" || name.indexOf("IP_") == 0 || name.endsWith("_IP")) {
			return "SYS_REMOTEIP";
		} else if (name == "UA" || name == "USERAGENT" || name == "USER_AGENT"
			|| name.indexOf("USER_AGENT_") == 0 || name.endsWith("_USER_AGENT")
			|| name.indexOf("USERAGENT_") == 0 || name.endsWith("_USERAGENT")
			|| name.endsWith("_UA") || name.endsWith("UA_")
		) {
			return "SYS_USER_AGENT";
		} else if (name == "REFERER" || name.indexOf("REFERER_") == 0 || name.endsWith("_REFERER")
		) {
			return "SYS_REMOTE_REFERER";
		} else if (name == "REMOTE_URL" || name.indexOf("REMOTE_URL_") == 0 || name.endsWith("_REMOTE_URL")
			|| name == "JSP" || name.indexOf("JSP_") == 0 || name.endsWith("_JSP")
		) {
			return "SYS_REMOTE_URL_ALL";
		} else {
			return f.Name;
		}
	}
}

//
function EWA$Define$Button() {
	this.Name;
	this.Tag;
	this.ParentStyle = "width:40px; text-align: center";
	this.Descriptions = new EWAD$Descriptions();
	this.Paras;
	/**
	 * 
	 * @param {EWA_XmlClass}
	 *            classXml
	 * @param {}
	 *            node
	 */
	this._InitFromXml = function(classXml, node) {
		this.Name = classXml.GetElementAttribute(node, "Name");
		this.Tag = classXml.GetElementAttribute(node, "Tag");
		this.Descriptions.InitFromXml(classXml, node);

		this.Paras = new EWAC$Paras();
		this.Paras._InitFromXml(classXml, node);
	};
	this.GetTag = function() {
		return this.Tag;
	};
	this.GetType = function() {
		return "";
	};
	this.CreateFkSql = function() {
		return "";
	};
	this.GetFormat = function() {
		return "";
	};
	this.GetParentStyle = function() {
		return this.ParentStyle;
	};
	this.GetOrder = function() {
		return "0";
	};
	this.GetSearch = function() {
		return "";
	};
}
function EWA$Define$Menu() {
	this.Name;
	this.Cmd;
	this.Icon;
	this.Descriptions = new EWAD$Descriptions();
	this._InitFromXml = function(classXml, node) {
		this.Name = classXml.GetElementAttribute(node, "Name");
		this.Cmd = classXml.GetElementAttribute(node, "Cmd");
		this.Icon = classXml.GetElementAttribute(node, "Icon");
		this.Descriptions.InitFromXml(classXml, node);
	};
	this.GetTag = function() {
		return this.Tag;
	};
	this.GetType = function() {
		return "";
	};
	this.CreateFkSql = function() {
		return "";
	};
	this.GetOrder = function() {
		return "0";
	};
	this.GetSearch = function() {
		return "";
	};
}

//
function EWA$Define$Action() {
	this.Name;
	this.Descriptions = new EWAD$Descriptions();
	this.SqlType;
	this.Sql;
	this.ActionName;
	this.Test;

	this._InitFromXml = function(classXml, node) {
		this.Name = classXml.GetElementAttribute(node, "Name");
		this.SqlType = classXml.GetElementAttribute(node, "SqlType");
		this.Sql = classXml.GetElementAttribute(node, "Sql");
		this.Descriptions.InitFromXml(classXml, node);
		this.ActionName = classXml.GetElementAttribute(node, "ActionName");
		if (this.ActionName == null || this.ActionName.trim() == '') {
			this.ActionName = this.Name;
		}
		this.Test = classXml.GetElementAttribute(node, "Test");
		if (this.Test == null) {
			this.Test = '';
		}
	};
}
function EWA$Define$Tmp() {
	this.Name;
	this.Descriptions = new EWAD$Descriptions();
	this.Actions = new Array();
	this.Buttons = new Array();
	this.Menus = new Array();
	this.Adds = new EWAC$Adds();
	this.PageInfosXml = "";
	this.ReleateFrame = null;
	this.ReleateTmp = null;
	this._InitFromXml = function(classXml, node) {
		this.Name = classXml.GetElementAttribute(node, "Name");
		this.ReleateFrame = classXml.GetElementAttribute(node, "ReleateFrame");
		this.ReleateTmp = classXml.GetElementAttribute(node, "ReleateTmp");

		var nl = classXml.GetElements("Action", node);
		this.Descriptions.InitFromXml(classXml, node);
		for (var i = 0; i < nl.length; i += 1) {
			var tmp = new EWA$Define$Action();
			tmp._InitFromXml(classXml, nl[i]);
			this.Actions[i] = tmp;
		}
		// 初始化button
		nl = classXml.GetElements("Button", node);
		for (var i = 0; i < nl.length; i += 1) {
			var tmp = new EWA$Define$Button();
			tmp._InitFromXml(classXml, nl[i]);
			this.Buttons[i] = tmp;
		}
		// 初始化menu
		nl = classXml.GetElements("Menu", node);
		for (var i = 0; i < nl.length; i += 1) {
			var tmp = new EWA$Define$Menu();
			tmp._InitFromXml(classXml, nl[i]);
			this.Menus[i] = tmp;
		}
		this.Adds._InitFromXml(classXml, node);
		var pn = classXml.GetElement("PageInfos", node);
		if (pn != null) {
			this.PageInfosXml = classXml.GetXml(pn);
		}
	};
}
function EWA$Define$Step() {
	this.Name;
	this.Eval;
	this.AfterEval;
	this.Descriptions = new EWAD$Descriptions();
}
function EWA$Define$Frame() {
	this.Name;
	this.Tmps = new Array();
	this.Descriptions = new EWAD$Descriptions();
	this._InitFromXml = function(classXml, node) {
		this.Name = classXml.GetElementAttribute(node, "Name");
		var nl = classXml.GetElements("Tmp", node);
		for (var i = 0; i < nl.length; i += 1) {
			var tmp = new EWA$Define$Tmp();
			tmp._InitFromXml(classXml, nl[i]);
			this.Tmps[i] = tmp;
		}
		this.Descriptions.InitFromXml(classXml, node);
	};
	this.GetTmp = function(name) {
		for (var i = 0; i < this.Tmps.length; i++) {
			var tmp = this.Tmps[i];
			if (tmp.Name == name) {
				return tmp;
			}
		}
		return null;
	}
}

function EWA_DefineClass(frameTag) {
	this.Frames = new EWAC$SetBase();
	this.Steps = new Array();
	this.FrameTag = frameTag;
	this.CurStep = 0;
	this.CurFrame;
	this.Lang = "zhcn";
	this.Name = "";
	// --
	this.Xml = new EWA.C.Xml();
	this.Config = new EWAD$Class(); // EWAC_CONFIG.js defined
	this.Fields = new EWAC$Fields();

	this.SelectedTmpIdx = null; // 选择的模板Index
	this.SelectedTmpName = null; // 选择的模板名称
	this.SelectedTmp = null; // 当前选择的模板

	this.HiddenCaption = function() {
		if (this.FrameTag == 'Frame') {
			return '1';
		} else {
			return '0';
		}
	};
	this.Width = function() {
		if (this.FrameTag == 'Frame') {
			return '700';
		} else {
			return '100%';
		}
	}

	// 生成新的配置并关闭窗口
	this.CreateNewFrame = function() {
		var xml1 = this.CreateFrame();
		if (!window.XMLS) {
			window.XMLS = {};
		}
		window.XMLS[this.Name] = xml1;
		if (window._EWA_DialogWnd) {
			window.parent.parent.NewFrame(xml1, xmlName, this.Name);
		} else {
			if (location.href.indexOf('auto=1') > 0) {

			} else {
				alert(xml1);
			}
		}

		// 相关的模板类型
		if (this.SelectedTmp.ReleateFrame && this.SelectedTmp.ReleateTmp) {
			if (location.href.indexOf('auto=1') > 0 || window.confirm("你需要生成" + this.SelectedTmp.ReleateTmp + "么")) {

				var frameType = this.SelectedTmp.ReleateFrame;
				var tmpName = this.SelectedTmp.ReleateTmp;
				var objHidden = $X("STEP" + this.CurStep);
				objHidden.style.display = "none";

				var define1 = new EWA_DefineClass(frameType);
				define1._Init();
				define1.Fields = this.Fields;
				for (var i = 0; i < define1.Fields.Fields.Count(); i++) {
					define1.Fields.Fields.GetItem(i).FrameTag = frameType;
				}
				define1._CreateDefineXml();
				define1.SelectedTmp = this.Frames.GetItem(frameType).GetTmp(tmpName);
				var name = define1.Fields.TableName + "." + this.CreateFrameType(frameType) + "." + tmpName;
				define1._ChangeName(name);

				define1.CurStep = 1;
				define1.Next($X('butprev'), $X('butnext'), 1);

				define = define1;

			}
		} else {
			if (window._EWA_DialogWnd) {
				_EWA_DialogWnd.CloseWindow();
			}
		}
	};
	this.CreateFrameType = function(frameType) {
		var t = frameType.toUpperCase();
		if (t == 'FRAME') {
			return 'F';
		} else if (t == 'LISTFRAME') {
			return 'LF';
		} else if (t == 'TREE') {
			return 'T';
		}
		return frameType;
	};
	this.CreateFrame = function() {
		if (this.FrameTag != "Tree") {
			this._CreateFieldsXml();
		}
		this._CreateActionXml();
		this._CreateMenusXml();
		this._CreateAdds();
		var nodeRoot = this._Xml.GetElement("EasyWebTemplate");
		var nodeName = this._Xml.GetElement("EasyWebTemplate/Page/Name/Set");
		nodeRoot.setAttribute("Name", nodeName.getAttribute("Name"));
		var xml1 = this._CreatePageInfos(this._Xml.GetXml());
		zzz = xml1;
		xml1 = xml1.replace(/undefined/g, '');
		return xml1;
	}

	this._CreatePageInfos = function(xml) {
		var tmp = this.SelectedTmp;
		if (tmp.PageInfosXml != null) {
			var m0 = xml.indexOf("</EasyWebTemplate>");
			var s2 = xml.substring(0, m0) + tmp.PageInfosXml + "</EasyWebTemplate>";
			return s2;
		} else {
			return xml;
		}
	};
	this._CreateAdds = function() {
		var tmp = this.SelectedTmp;
		tmp.Adds.CreateAdds(this._Xml);
	};
	// 生成SQL脚本及调用模式
	this._CreateActionXml = function() {
		var tmp = this.SelectedTmp;
		var acts = tmp.Actions;
		var nodeAction = this._Xml.GetElement("EasyWebTemplate/Action");
		var nodeActionSet = this._Xml.NewChild("ActionSet", nodeAction);
		var nodeSqlSet = this._Xml.NewChild("SqlSet", nodeAction);
		var paraActionSet = this.Config.Action.XItemParameters.GetXItemParameter("ActionSet");
		var paraSqlSet = this.Config.Action.XItemParameters.GetXItemParameter("SqlSet");
		var paraCallSet = this.Config.Action.XItemParameters.GetXItemParameter("CallSet");
		var actions = {};
		for (var i = 0; i < acts.length; i += 1) {
			this.Action = acts[i];
			var node0 = actions[this.Action.ActionName];
			var isExists = true;
			if (node0 == null) {
				node0 = this._CreateDefineValuesXml(paraActionSet, nodeActionSet);
				actions[this.Action.ActionName] = node0;
				isExists = false;
			}
			this._CreateDefineValuesXml(paraSqlSet, nodeSqlSet);
			var nodeCallSet = this._Xml.NewChild("CallSet", nodeActionSet);
			this._CreateDefineValuesXml(paraCallSet, nodeCallSet);
			if (isExists) {
				node0.childNodes[0].appendChild(nodeCallSet.childNodes[0]);
			} else {
				node0.appendChild(nodeCallSet);
			}
		}
	};
	this._CreateMenuXml = function() {
		var tmp = this.CurFrame.Tmps[this.Steps[1].SelectedValue * 1];
		var menus = tmp.Menus;
	};
	// 页面点击“下一步”按钮
	this.Next = function(objPrev, objNext, step) {
		var afterEval = this.Steps[this.CurStep].AfterEval;
		if (!(afterEval == null || afterEval.trim() == "")) {
			this._Eval(afterEval);
		}
		var objHidden = $X("STEP" + this.CurStep);
		objHidden.style.display = "none";
		this.CurStep += step;
		var objShow = $X("STEP" + this.CurStep);
		objShow.style.display = "";
		if (this.CurStep > 0) {
			objShow.innerHTML = this._Eval(this.Steps[this.CurStep].Eval);
		}
		this.SetTitle();
		if (this.Steps.length == this.CurStep + 1) {
			objNext.disabled = true;
		} else {
			objNext.disabled = false;
		}
		if (this.CurStep > 0) {
			objPrev.disabled = false;
		} else {
			objPrev.disabled = true;
		}
	};
	// 第一步结束:生成主要信息
	this.CreateMainInfo = function() {
		if (this.Fields.CfgName == cfg && this.Fields.TableName == tablename) {
			// 表没有变动
			return;
		}
		// 表变动，重新生成初始化数据
		this.Name = tablename + "." + this.FrameTag.substring(0, 1);
		this._CreateFields();
		this._CreateDefineXml();

		// 清除设置的SQL
		for (var m = 0; m < this.CurFrame.Tmps.length; m += 1) {
			for (var i = 0; i < this.CurFrame.Tmps[m].Actions.length; i += 1) {
				this.CurFrame.Tmps[m].Actions[i].SetSql = null;
			}
		}
	};

	// 第二步:选择生成的类型,记录在Step的SelectedValue中
	this.SelectType = function() {
		var frame = this.CurFrame;
		EWAC_DEFINE = this;
		var s1 = "<table border=0 class='templates' bgcolor='#cccccc' cellpadding=4 cellspacing=1 width=99% align=center>";
		for (var i = 0; i < frame.Tmps.length; i += 1) {
			var tmp = frame.Tmps[i];
			var des = tmp.Descriptions.GetDescription("zhcn");
			var mark = "onclick='EWAC_DEFINE.SelectTypeEvent(this);'";
			if (this.Steps[this.CurStep].SelectedValue == i) {
				mark += " checkED";
			}
			s1 += "<tr style='cursor:pointer' onclick='this.cells[0].childNodes[0].click()' bgcolor=white>";
			s1 += "<td width=20>";
			s1 += "<input n1='" + tmp.Name + "' " + mark + " name=s1 type=radio value='" + i + "'></td>";
			s1 += "<td><div class='name' style='color:blue;font-weight:bold;padding:2px;border-bottom:1px dotted #ccc'>"
				+ des.Info + " </div>";
			s1 += "<div style='padding:4px;'>" + des.Memo + "</div></td></tr>";
		}
		s1 += "</table>";
		return s1;
	};

	// 第二步:选择生成的类型，点击后的事件
	this.SelectTypeEvent = function(obj) {
		var s = this.Steps[this.CurStep];
		s.SelectedValue = obj.value;
		s.SelectedName = obj.getAttribute("n1");

		this.SelectedTmpIdx = obj.value * 1;
		this.SelectedTmpName = obj.getAttribute("n1");
		this.SelectedTmp = this.CurFrame.Tmps[this.SelectedTmpIdx];
		// 类型
		var tag = this.CreateFrameType(this.FrameTag);
		this._ChangeName(tablename + "." + tag + "." + s.SelectedName);
		this.SelectedTypeName = s.SelectedName;
		console.log(this.SelectedTypeName);
	};

	// 修改配置项目名称
	this._ChangeName = function(name) {
		this.Name = name;
		var node = this._Xml.GetElement("EasyWebTemplate/Page/Name/Set");
		node.setAttribute("Name", this.Name);
		var node1 = this._Xml.GetElement("EasyWebTemplate");
		node.setAttribute("Name", this.Name);
	};

	// 第二步：修改主要信息
	this.ModifyMainInfo = function() {
		var s1 = "<table border=0 width=80% align=center cellspacing=1 cellpadding=2>";
		_EWAC_DEFINE = this;
		var curPara, curPValue;
		var ps = this.Config.Page.XItemParameters;
		for (var m = 0; m < ps.Count(); m += 1) {
			var p = ps.GetXItemParameter(m);
			if (!p.CheckFrame(this.FrameTag)) {
				continue;
			}
			var node = this._Xml.GetElement("EasyWebTemplate/Page/" + p.Name + "/Set");
			for (var i = 0; i < p.Values.Count(); i += 1) {
				var v = p.Values.GetValue(i);
				if (v.Step == null) {
					continue;
				}
				var val = node.getAttribute(v.Name);
				var cmd = "_EWAC_DEFINE.ModifyMainInfoEvent(this.value,\"" + p.Name + "\",\"" + v.Name + "\")";
				if (curPara != p) {
					curPara = p;
					s1 += "<tr><td bgcolor=#EFEFEF colspan=2 style='padding-left:5px;'><b style='color:darkblue'>";
					s1 += p.Name + " (" + p.Descriptions.GetInfo(lang) + ")</b></td></tr>";
					curPValue = v;
				}
				if (i > 0 && curPValue == v) {
					s1 += "<tr><td colspan=2 style='padding-left:5px;'></td></tr>";
				}
				s1 += "<tr><td nowrap bgcolor=#EFEFEF style='padding-left:20px;padding-right:5px;'>";
				s1 += v.Name + " (" + v.Descriptions.GetInfo(lang) + ")</td>";
				s1 += "<td  bgcolor=white>";
				s1 += this._ModifyMainInfoItem(v, val, cmd);
				s1 += "</td></tr>";
			}
		}
		s1 += "</table>";
		return s1;
	};
	this._ModifyMainInfoItem = function(v, val, cmd) {
		var s1 = "";
		if (v.StepUi == "group") {
			s1 += "<select onchange='" + cmd + "'>";
			s1 += "<option value=''></option>";
			var items = this._Eval(v.CreateValue);
			for (var m = 0; m < items.Count(); m += 1) {
				var item = items.GetItem(m);
				var name = item.Name;
				var des = item.Name;
				if (typeof item.Description != "undefined" && item.Description != des) {
					des += " (" + item.Description + ")";
				}
				var mark = val == name ? " selected" : "";
				s1 += "<option value=\"" + name + "\"" + mark + ">" + des + "</option>";
			}
			s1 += "</select>";
			return s1;
		}
		if (v.StepUi == "view") {
			s1 += "<i style='color:blue;font-family:Verdana'>" + ss.Value + "</i>";
		} else {
			s1 += "<input value=\"" + val + "\" onblur='" + cmd + "' type=text size1q=80>";
		}
		return s1;
	};
	// 第二步：修改主要信息变化后的事件
	this.ModifyMainInfoEvent = function(val, pName, vName) {
		if (pName == "Name") {
			this._ChangeName(val);
			return;
		}
		var node = this._Xml.GetElement("EasyWebTemplate/Page/" + pName + "/Set");
		var v = this.Config.Page.XItemParameters.GetXItemParameter(pName).Values.GetValue(vName);
		if (v.IsCDATA) {
			var node1 = this._Xml.GetElement(v.Name, node);
			this._Xml.SetText(val, node1);
		} else {
			node.setAttribute(v.Name, val);
		}

		if (pName == 'PageSize' && vName == 'KeyField' && this.Fields.Pk != val) {
			console.log('use self define pk: ' + val)
			this.Fields.Pk = val;
			var vals = val.split(',');
			var pks = [];
			for (var i = 0; i < vals.length; i++) {
				var pkField = new EWAC$Field();
				pkField.Name = vals[i];
				pks.push(pkField);
			}
			this.Fields.userDefinedPkFields = pks;
		}
	};
	//
	this.ModifyMenus = function() {
		var tmp = this.SelectedTmp;
		var menus = tmp.Menus;
		var s1 = "<table border=0 width=80% align=center cellspacing=1 cellpadding=2>";
		s1 += "<tr align=center bgcolor=#ABABAB height=25><td></td><td>\u540d\u79f0</td><td>\u83dc\u5355\u5185\u5bb9</td><td>\u6267\u884c\u811a\u672c</td><td>\u56fe\u6807</td></th>";
		for (var i = 0; i < menus.length; i += 1) {
			var b = menus[i];
			s1 += "<tr><td width=20 align=center bgcolor=#EFEFEF><input t1=b type=checkbox checked value=\"" + b.Name
				+ "\"></td>";
			s1 += "<td bgcolor=#EFEFEF>" + b.Name + "</td>";
			s1 += "<td bgcolor=#EFEFEF>" + b.Descriptions.GetDescription(this.Lang).Info + "</td>";
			s1 += "<td bgcolor=#EFEFEF>" + b.Cmd + "</td>";
			var s2 = "<div style=\"width:15px;height:15px;background-image:url('" + b.Icon.replace("@SYS_CONTEXTPATH", EWA.CP)
				+ "')\">&nbsp;</div>";
			s1 += "<td bgcolor=#EFEFEF>" + s2 + "</td>";
			s1 += "</tr>";
		}
		s1 += "</table>";
		return s1;
	};
	// 显示并修改字段
	this.ModifyFields = function() {
		var tmp = this.SelectedTmp;
		var buts = tmp.Buttons;
		var items = tmp.Items;
		var s1 = "<table border=0 id='fields_choose' width=80% align=center cellspacing=1 cellpadding=2>";
		s1 += "<tr><th><a href='javascript:$(&quot;#fields_choose input[type=checkbox]&quot;).each(function(){this.checked=!this.checked})'>Choose</a></th><th>Field</th><th>Type</th></tr>";

		for (var i = 0; i < this.Fields.Fields.Count(); i += 1) {
			var f = this.Fields.Fields.GetItem(i);
			s1 += "<tr><td width=20 align=center bgcolor=#EFEFEF><input t1=f type=checkbox checked value=\"" + f.Name
				+ "\"></td>";
			s1 += "<td bgcolor=#EFEFEF>" + f.Name + " (" + f.Description + ") " + "</td>";
			var item_tag = f.GetTag();
			if (this.SelectedTypeName == 'V') {
				item_tag = 'span';
			}
			s1 += "<td bgcolor=#EFEFEF>" + item_tag + "</td>";
			s1 += "</tr>";
		}
		s1 += "<tr><td colspan=3></td></tr>";
		for (var i = 0; i < buts.length; i += 1) {
			var b = buts[i];
			s1 += "<tr><td width=20 align=center bgcolor=#EFEFEF><input t1=b type=checkbox checked value=\"" + f.Name
				+ "\"></td>";
			s1 += "<td bgcolor=#EFEFEF>" + b.Name + " (" + b.Descriptions.GetDescription(this.Lang).Info + ") " + "</td>";
			s1 += "<td bgcolor=#EFEFEF>" + b.Tag + "</td>";
			s1 += "</tr>";
		}
		s1 += "</table>";
		return s1;
	};
	this.SelectFields = function(type) {
		var objs = $X("STEP" + this.CurStep).getElementsByTagName("input");
		var step = this.Steps[this.CurStep];
		var a = new Array();
		var bb = 0;
		var cc = 0;
		for (var i = 0; i < objs.length; i += 1) {
			a[i] = new Array();
			a[i][0] = objs[i].value;
			a[i][1] = objs[i].checked;
			a[i][2] = objs[i].getAttribute("t1");
			if (type == "field") {
				if (a[i][2] == "f") {// field
					var f = this.Fields.Fields.GetItem(i);
					f.IsChecked = a[i][1];
				} else {
					var b = this.SelectedTmp.Buttons[bb];
					b.IsChecked = a[i][1];
					bb += 1;
				}
			}
			if (type == "menu") {
				var b = this.SelectedTmp.Menus[bb];
				b.IsChecked = a[i][1];
				bb += 1;
			}
			if (type == "item") {
				var b = this.SelectedTmp.Items[cc];
				b.IsChecked = a[i][1];
				cc += 1;
			}
		}
		step.SelectedValue = a;
	};

	// 修改SQL语句
	this.ModifySQL = function() {
		var tmp = this.SelectedTmp;
		var acts = tmp.Actions;
		var s1 = "<table border=0 width=80% align=center cellspacing=1 cellpadding=2>";
		for (var i = 0; i < acts.length; i += 1) {
			var act = acts[i];
			var val = this._Eval(act.Sql);
			act.SetSql = val;
			s1 += "<tr><td bgcolor=#EFEFEF>" + act.Name + " (" + act.Descriptions.GetDescription(this.Lang).Info + ")</td></tr>";
			s1 += "<tr><td bgcolor=#EFEFEF><textarea style='width:100%;' rows=4>" + this._MarkHtml(val) + "</textarea></td></tr>";
		}
		s1 += "</table>";
		return s1;
	};
	this._Eval = function(exp) {
		// try {
		return eval(exp);
		// }
		// catch (e) {
		// return exp;
		// }
	};

	// 生成XML结构
	this._CreateDefineXml = function() {// 生成空结构
		this._Xml = new EWA.C.Xml();
		this._Xml.LoadXml("<?xml version=\"1.0\" encoding=\"UTF-8\"?><EasyWebTemplate Name='" + this.Name
			+ "'><Page /><Action /><XItems /><Menus /></EasyWebTemplate>");

		// page
		var nodePage = this._Xml.GetElement("EasyWebTemplate/Page");
		var ps = this.Config.Page;
		this._CreateDefineParametersXml(nodePage, ps);
	};
	this._CreateDefineParametersXml = function(node, part) {
		var ps = part.XItemParameters;
		for (var m = 0; m < ps.Count(); m += 1) {
			var p = ps.GetXItemParameter(m);
			if (p.CheckFrame(this.FrameTag)) {
				var child = this._Xml.NewChild(p.Name, node);
				this._CreateDefineValuesXml(p, child);
			}
		}
	};
	this._CreateDefineValuesXml = function(p, child) {
		var rowChild = this._Xml.NewChild("Set", child);
		for (var i = 0; i < p.Values.Count(); i = 1 + i) {
			var v = p.Values.GetValue(i);
			if (v.CreateValue == null) {
				continue;
			}
			var val = this._Eval(v.CreateValue);
			if (v.IsCDATA) {
				var cdataChild = this._Xml.NewChild(v.Name, rowChild);
				this._Xml.SetCData(val, cdataChild);
			} else {
				rowChild.setAttribute(v.Name, val);
			}
		}
		return rowChild;
	};

	this._CreateFieldsXml = function() {
		// items
		var xitemNode = this._Xml.GetElement("EasyWebTemplate/XItems");
		for (var i = 0; i < this.Fields.Fields.Count(); i += 1) {
			this.Field = this.Fields.Fields.GetItem(i);
			if (!this.Field.IsChecked) {
				continue;
			}
			var node = this._Xml.NewChild("XItem", xitemNode);
			node.setAttribute("Name", this.Field.Name);
			this._CreateDefineParametersXml(node, this.Config.Items);
			this._CreateFieldListXml(this.Field, node);
		}
		var buts = this.SelectedTmp.Buttons;
		for (var i = 0; i < buts.length; i += 1) {
			this.Field = buts[i];
			if (!this.Field.IsChecked) {
				continue;
			}
			var node = this._Xml.NewChild("XItem", xitemNode);
			node.setAttribute("Name", this.Field.Name);
			this._CreateDefineParametersXml(node, this.Config.Items);
			this._SetDescriptions(node, this.Field.Descriptions);
			this.Field.Paras.CreateParas(this._Xml, node);
		}
	};
	this._CreateFieldListXml = function(f, node) {
		if (window.EWAC_DEF == null || EWAC_DEF.LIST == null) {
			return;
		}
		if (this.FrameTag != 'ListFrame') {
			return;
		}
		var name = f.Name.trim().toUpperCase();
		if (!EWAC_DEF.LIST[name]) {
			return;
		}
		var o = EWAC_DEF.LIST[name];
		var p0 = new EWAC$Para();
		p0.XmlPath = "DataRef/Set";
		p0.Name = "RefSql";
		p0.Val = o.SQL;

		var p1 = new EWAC$Para();
		p1.XmlPath = "DataRef/Set";
		p1.Name = "RefKey";
		p1.Val = o.VALUE;

		var p2 = new EWAC$Para();
		p2.XmlPath = "DataRef/Set";
		p2.Name = "RefShow";
		p2.Val = o.TEXT;

		f.Paras = new EWAC$Paras();
		f.Paras.Paras.push(p0);
		f.Paras.Paras.push(p1);
		f.Paras.Paras.push(p2);
		f.Paras.CreateParas(this._Xml, node);
	}
	this._AppendChilds = function(node, add) {
		var tag = add.nodeName;
		var nl = node.getElementsByTagName(tag);
		var node1 = null;
		if (nl.length > 0) {
			node1 = nl[0];
		}
		if (node1 == null) {
			node1 = this._Xml.NewChild(tag, node);
		}
		for (var i = 0; i < add.attributes.length; i++) {
			var att = add.attributes[i];
			node1.setAttribute(att.name, att.value);
		}
		for (var i = 0; i < add.childNodes.length; i++) {
			var nodeAdd = add.childNodes[i];
			if (nodeAdd.nodeType == 1) {
				this._AppendChilds(node1, nodeAdd);
			}
		}
	}
	this._CreateMenusXml = function() {
		// items
		var xitemNode = this._Xml.GetElement("EasyWebTemplate/Menus");
		var buts = this.SelectedTmp.Menus;
		for (var i = 0; i < buts.length; i += 1) {
			this.Menu = buts[i];
			if (!this.Menu.IsChecked) {
				continue;
			}
			var node = this._Xml.NewChild("Menu", xitemNode);
			this._CreateDefineParametersXml(node, this.Config.Menu);
			this._SetDescriptions(node, this.Menu.Descriptions);
		}
	};
	this._SetDescriptions = function(node, dess) {
		var desNode = this._Xml.GetElement("DescriptionSet", node);
		while (desNode.childNodes.length > 0) {
			var n1 = desNode.childNodes[0];
			desNode.removeChild(n1);
		}
		for (var i = 0; i < dess.Count(); i += 1) {
			var des = dess.GetDescription(i);
			var nodeChild = this._Xml.NewChild("Set", desNode);
			nodeChild.setAttribute("Lang", des.Lang);
			nodeChild.setAttribute("Info", des.Info);
			nodeChild.setAttribute("Memo", des.Memo == null ? "" : des.Memo);
		}
	};
	//
	this._CreateFields = function(fieldTable) {
		this.Fields = new EWAC$Fields();
		this.Fields.CfgName = cfg;
		this.Fields.TableName = tablename;
		this.Fields.Parent = this;
		var table = fieldTable ? fieldTable : window.frames[1].T$("TABLE")[2];
		for (var i = 1; i < table.rows.length; i += 1) {
			var f = this._CreateField(table.rows[i]);
			f.FrameTag = this.FrameTag;
			f.Parent = this.Fields;
			this.Fields.Fields.AddObject(f.Name, f);
		}
	};
	// 生成Field对象
	this._CreateField = function(row) {
		var f = new EWAC$Field();
		f.Name = GetInnerText(row.cells[1]).trim();
		f.Description = GetInnerText(row.cells[2]).trim();
		f.IsPk = row.cells[1].childNodes[0].childNodes[0].id.indexOf("true") > 0 ? true : false;
		f.Type = GetInnerText(row.cells[3]).trim();
		f.Length = GetInnerText(row.cells[4]).trim() * 1;
		f.Length == f.Length > 0 ? f.Length : 150;
		if (f.IsPk) {
			if (this.Fields.Pk.length > 0) {
				this.Fields.Pk += ",";
			}
			this.Fields.Pk += f.Name;
			this.Fields.PkFields[this.Fields.PkFields.length] = f;
		}
		f.IsChecked = $T("input", row.cells[0])[0].checked;
		//非空
		f.MustInput = GetInnerText(row.cells[5]).trim() == "false" ? 1 : 0;
		//自增
		f.IsIdentity = GetInnerText(row.cells[6]).trim() == "true" ? 1 : 0;
		var fks = GetInnerText(row.cells[7]).split(".");
		f.FkTableName = fks[0].trim();
		f.FkColumnName = fks[1].trim();
		if (f.FkTableName.length > 0) {
			f.IsFk = true;
		} else {
			f.IsFk = false;
		}
		return f;
	};
	//
	this.GetMainInfo = function() {
		var des = this.CurFrame.Descriptions.GetDescription(this.Lang);
		var s1 = "<div style='font-size:12px;font-family:arial'>";
		s1 += "<div>" + des.Info + "</div><hr>";
		s1 += "<div>" + des.Memo + "</div>";
		return s1;
	};
	this.GetFrameDescription = function() {
		return this.CurFrame.Descriptions.GetDescription(this.Lang);
	};
	this.SetTitle = function() {
		var s11 = this.Steps[this.CurStep].Descriptions.GetDescription(this.Lang).Info;
		if (window._EWA_DialogWnd)
			_EWA_DialogWnd.SetCaption(s11);
	};
	//
	this._MarkHtml = function(s1) {
		if (s1 == null) {
			return s1;
		}
		var mm = /&/ig;
		var m0 = />/ig;
		var m1 = /</ig;
		var s2 = s1.replace(mm, "&amp;");
		s2 = s2.replace(m0, "&gt;");
		s2 = s2.replace(m1, "&lt;");
		return s2;
	};
	this._Init = function() {
		this.Config.Init();

		if (window._CFG_DEFINE) {
			this.Xml.LoadXml(_CFG_DEFINE);
		} else {
			var u = "/EWA_DEFINE/cgi-bin/xml/?TYPE=CFG_XML&XMLNAME=EwaDefine.xml";
			this.Xml.LoadXmlFile(EWA.CP + u);
		}
		var nl = this.Xml.GetElements("EwaDefine/Frame");
		for (var i = 0; i < nl.length; i += 1) {
			var frame = new EWA$Define$Frame();
			frame._InitFromXml(this.Xml, nl[i]);
			this.Frames.AddObject(frame.Name, frame);
		}
		nl = this.Xml.GetElements("Steps/Step");
		for (var i = 0; i < nl.length; i += 1) {
			var node = nl[i];
			var tmp = new EWA$Define$Step();
			var using = this.Xml.GetElementAttribute(node, "Using");
			var ok = false;
			if (using == null || using.trim() == "") {
				ok = true;
			} else {
				var usings = using.split(",");
				for (var m = 0; m < usings.length; m += 1) {
					if (usings[m].trim().toUpperCase() == this.FrameTag.trim().toUpperCase()) {
						ok = true;
						break;
					}
				}
			}
			if (!ok) {
				continue;
			}
			tmp.Name = this.Xml.GetElementAttribute(node, "Name");
			tmp.Eval = this.Xml.GetElementAttribute(node, "Eval");
			tmp.AfterEval = this.Xml.GetElementAttribute(node, "AfterEval");
			tmp.Descriptions.InitFromXml(this.Xml, node);
			this.Steps[this.Steps.length] = tmp;
		}
		this.CurFrame = this.Frames.GetItem(this.FrameTag);
	};
	this._Init();
}
