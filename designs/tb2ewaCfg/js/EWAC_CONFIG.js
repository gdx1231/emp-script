function EWAC$SetBaseData() {
	this._Index = null;
	this._Object = null;
	this._Name = null;
}

function EWAC$SetBase() {
	this._SetArray = new Array();
	this._Names = new Object();
	this.Count = function() {
		return this._SetArray.length;
	};
	this.GetItem = function(name) {
		var data = this.GetData(name);
		if (data == null) {
			return null;
		}
		return data._Object;
	};
	this.GetData = function(name) {
		if (name == null) {
			return null;
		}
		var a = parseInt(name);
		if (isNaN(a)) { // 字符
			var n1 = name.trim().toUpperCase();
			var index = this._Names[n1];
			if (index == null) {
				return null;
			}
			return this._SetArray[index];
		} else { // 数字索引
			return this._SetArray[a];
		}
	};
	this.GetIndex = function(name) { // 获取名称索引
		var obj = this.GetData(name);
		if (obj == null) {
			return null;
		}
		return obj._Index;
	};
	this.AddObject = function(name, obj) {
		var index = this._SetArray.length;
		this._SetArray[index] = new EWAC$SetBaseData();
		this._SetArray[index]._Index = index;
		this._SetArray[index]._Object = obj;
		this._SetArray[index]._Name = name;
		this._Names[name.trim().toUpperCase()] = index;
		return index;
	};
	this.Sort = function() { // 重新排序对象
		for (var i = 0; i < this._SetArray.length; i += 1) {
			var o1 = this._SetArray[i];
			if (o1._Object.IsDelete) {
				o1._Index = 1972 * 100 + i;
			}
		}
		this._SetArray.sort(function(a, b) {
			return a._Index >= b._Index ? 1 : -1;
		});
		this._Names = null;
		this._Names = new Object();
		for (var i = 0; i < this._SetArray.length; i += 1) {
			var o1 = this._SetArray[i];
			o1._Index = i;
			this._Names[o1._Name.trim().toUpperCase()] = i;
		}
	};
}

/**
 * 公共信息类
 */
function EWAD$Info() {
	this.Name;
	this.Descriptions = new EWAD$Descriptions();
}

/**
 * 公共信息类集合
 */
function EWAD$Infos() {
	this.Infos = new EWAC$SetBase();
	this.InitFromXml = function(classXml) {
		var path = "EasyWebConfig/Infos/Info";
		var nl = classXml.GetElements(path);
		for (var i = 0; i < nl.length; i = i + 1) {
			var val = new EWAD$Info();
			val.Name = classXml.GetElementAttribute(nl[i], "Name");
			val.Descriptions.InitFromXml(classXml, nl[i]);
			this.Infos.AddObject(val.Name, val);
		}
	};
	this.CreateArray = function(lang) {
		var vs = new Array();
		var ts = new Array();
		var vals = this.Infos;
		vs[0] = ts[0] = "";
		for (var i = 0; i < vals.Count(); i += 1) {
			var v = vals.GetItem(i);
			var d = v.Descriptions.GetDescription(lang);
			vs[i + 1] = v.Name;
			ts[i + 1] = d.Info;
		}
		return [vs, ts];
	};
}

function EWAD$Connection() {
	this.Name;
	this.Type;
	this.ConnectionString;
	this.SchemaName;
}

function EWAD$Connections() {
	// <database Name="A1" Type="ORACLE" ConnectionString="jdbc/tjj"
	// SchemaName="HDSTAT" />
	this._Xml = new EWA.C.Xml();
	this.Connections = new EWAC$SetBase();
	this._Url = EWA.CP + "/EWA_DEFINE/cgi-bin/xml/?xmlname=EwaConnections.xml&TYPE=CFG_XML";
	this.CreateArray = function() {
		var vs = [];
		var ts = [];
		vs.push("");
		ts.push("");
		var vals = this.Connections;
		for (var i = 0; i < vals.Count(); i += 1) {
			var v = vals.GetItem(i);
			vs.push(v.Name);
			ts.push(v.Type + "," + v.ConnectionString);
		}
		return [vs, ts];
	};
	this._Init = function() {
		if (window._CFG_CNN) {
			this._Xml.LoadXml(_CFG_CNN);
		} else {
			this._Xml.LoadXmlFile(this._Url);
		}
		this._InitFromXml(this._Xml);
		this._Xml = null;
	};
	this._InitFromXml = function(classXml) {
		var nl = classXml.GetElements("root/database");
		for (var i = 0; i < nl.length; i = i + 1) {
			var cnn = new EWAD$Connection();
			let name = classXml.GetElementAttribute(nl[i], "name");
			if (!name) {
				// old conf
				name = classXml.GetElementAttribute(nl[i], "Name");
			}
			let type = classXml.GetElementAttribute(nl[i], "type");
			if (!type) {
				type = classXml.GetElementAttribute(nl[i], "Type");
			}
			let connectionSring = classXml.GetElementAttribute(nl[i], "connectionString");
			if (!connectionSring) {
				connectionSring = classXml.GetElementAttribute(nl[i], "ConnectionString");
			}
			let schemaName = classXml.GetElementAttribute(nl[i], "schemaName");
			if (!schemaName) {
				schemaName = classXml.GetElementAttribute(nl[i], "SchemaName");
			}
			cnn.Name = name === null ? "undefined" : name.toLowerCase();
			cnn.Type = type;
			cnn.ConnectionString = connectionSring;
			cnn.SchemaName = schemaName;
			this.Connections.AddObject(cnn.Name, cnn);
		}
	};
	this._Init();
}

// --------------------------------------------------
function EWAD$Description() { // 描述
	EWA.LANG;
	this.Info;
	this.Memo;
}

function EWAD$Descriptions() { // 描述集合
	this._Set = new EWAC$SetBase();
	this.AddDescription = function(description) {
		this._Set.AddObject(description.Lang, description);
	};
	this.GetDescription = function(name) {
		var des = this._Set.GetItem(name);
		if (des == null) {
			des = this._Set.GetItem(0);
		}
		return des;
	};
	this.GetInfo = function(lang) {
		var des = this.GetDescription(lang);
		return des ? des.Info : null;
	};
	this.GetMemo = function(lang) {
		var des = this.GetDescription(lang);
		return des ? des.Memo : null;
	};
	this.Count = function() {
		return this._Set.Count();
	};
	this.InitFromXml = function(classXml, xmlNode) {
		var node = classXml.GetElement("DescriptionSet", xmlNode);
		var nl = classXml.GetElements("Set", node);
		for (var i = 0; i < nl.length; i = i + 1) {
			var des = new EWAD$Description();
			des.Lang = classXml.GetElementAttribute(nl[i], "Lang");
			des.Info = classXml.GetElementAttribute(nl[i], "Info");
			des.Memo = classXml.GetElementAttribute(nl[i], "Memo");
			if (des.Memo == null || des.Memo == "") {
				des.Memo = classXml.GetElementText(nl[i]);
			}
			this.AddDescription(des);
		}
	};
}

function EWAD$Value() {
	this.Type; // ="string"
	this.Name; // ="Name"
	this.OuterButton; // 外部定义按钮
	this.IsUnique; //
	this.Html;
	this.IsJsShow;
	this.IsCDATA;
	this.Ref;
	this.Target; // 影响的值
	this.Step; // 定义时候的步骤
	this.StepUi = "STRING"; // 定义时的显示方式
	this.CreateValue; // 定义时生成的值
	this.Descriptions = new EWAD$Descriptions();
}

function EWAD$Values() {
	this._Set = new EWAC$SetBase();
	this.AddValue = function(value) {
		this._Set.AddObject(value.Name, value);
	};
	this.GetValue = function(name) {
		return this._Set.GetItem(name);
	};
	this.Count = function() {
		return this._Set.Count();
	};
	this.InitFromXml = function(classXml, xmlNode) {
		var nl = classXml.GetElements("Values/Value", xmlNode);
		for (var i = 0; i < nl.length; i = i + 1) {
			var val = new EWAD$Value();
			val.Name = classXml.GetElementAttribute(nl[i], "Name");
			val.Type = classXml.GetElementAttribute(nl[i], "Type");
			val.IsUnique = classXml.GetElementAttribute(nl[i], "IsUnique");
			val.Html = classXml.GetElementAttribute(nl[i], "Html");
			val.IsJsShow = classXml.GetElementAttribute(nl[i], "IsJsShow");
			val.IsCDATA = classXml.GetElementAttribute(nl[i], "IsCDATA");
			val.Ref = classXml.GetElementAttribute(nl[i], "Ref");
			val.Target = classXml.GetElementAttribute(nl[i], "Target");
			val.OuterButton = classXml.GetElementAttribute(nl[i], "OuterButton");
			// 是否显示在属性面板上
			val.IsShow = classXml.GetElementAttribute(nl[i], "IsShow");
			// 只读属性
			val.ReadOnly = classXml.GetElementAttribute(nl[i], "ReadOnly");
			// 定义时的显示方式
			var ui = classXml.GetElementAttribute(nl[i], "StepUi");
			val.StepUi = ui == null ? "string" : ui;

			// 生成时的默认值
			val.CreateValue = classXml.GetElementAttribute(nl[i], "CreateValue");
			// 生成时的步骤
			val.Step = classXml.GetElementAttribute(nl[i], "Step");
			val.Descriptions.InitFromXml(classXml, nl[i]);

			val.IsShow = classXml.GetElementAttribute(nl[i], "IsShow");
			this.AddValue(val);
		}
	};
}

function EWAD$XItemParameter() {
	this.Name; // ="CookieSet"
	this.IsSet; // ="1"
	this.IsShow; // ="1"
	this.IsMulti; // ="1"
	this.IsChildren; // 是否为子集合
	this.Children = null; // 子集合参数 EWADXItemParameter
	this.FrameTag; // 是否为指定的FrameTag
	this.OuterButton; // 附加的按钮
	this.Descriptions = new EWAD$Descriptions();
	this.Values = new EWAD$Values();

	// 检查参数是否属于 FrameTag
	this.CheckFrame = function(frameTag) {
		if (this.FrameTag == null || this.FrameTag.trim() == "") {
			return true;
		}
		var v1 = this.FrameTag.trim().toUpperCase();
		var v2 = v1.split(",");
		var t = frameTag.trim().toUpperCase();
		for (var i = 0; i < v2.length; i += 1) {
			if (t == v2[i]) {
				return true;
			}
		}
		return false;
	};
}

function EWAD$XItemParameters() {
	this._Set = new EWAC$SetBase();
	this.Name = "";
	this.AddXItemParameter = function(value) {
		this._Set.AddObject(value.Name, value);
	};
	this.GetXItemParameter = function(name) {
		return this._Set.GetItem(name);
	};
	this.Count = function() {
		return this._Set.Count();
	};
	this.InitFromXml = function(classXml, xmlNode, parentClass) {
		var nl = classXml.GetElements("XItemParameters/XItemParameter", xmlNode);
		if (nl == null) {
			return;
		}
		for (var i = 0; i < nl.length; i = i + 1) {
			var val = this._InitParameter(classXml, nl[i]);
			val.Parent = parentClass;
			this.AddXItemParameter(val);
		}
		// 子集合参数
		for (var i = 0; i < this.Count(); i += 1) {
			var p = this.GetXItemParameter(i);
			var cps = p.cps;
			if (cps == null || cps.trim().length == 0) {
				continue;
			}
			p.Children = new EWAC$SetBase();
			var s1 = cps.split(",");
			for (var m = 0; m < s1.length; m += 1) {
				var pname = s1[m].trim();
				if (pname.length == 0) {
					continue;
				}
				p.Children.AddObject(pname, this.GetXItemParameter(pname));
			}
		}
	};
	this._InitParameter = function(classXml, nodeItem) {
		var val = new EWAD$XItemParameter();
		val.Name = classXml.GetElementAttribute(nodeItem, "Name");
		val.IsSet = classXml.GetElementAttribute(nodeItem, "IsSet");
		val.IsShow = classXml.GetElementAttribute(nodeItem, "IsShow");
		val.IsMulti = classXml.GetElementAttribute(nodeItem, "IsMulti");
		val.IsChildren = classXml.GetElementAttribute(nodeItem, "IsChildren");
		val.FrameTag = classXml.GetElementAttribute(nodeItem, "FrameTag");
		val.OuterButton = classXml.GetElementAttribute(nodeItem, "OuterButton");
		val.Frames = classXml.GetElementAttribute(nodeItem, "Frames");
		val.Descriptions.InitFromXml(classXml, nodeItem);
		val.Values.InitFromXml(classXml, nodeItem);
		val.cps = classXml.GetElementAttribute(nodeItem, "ChildrenParameters");
		val.XItemParameters = this;
		return val;
	};
}

function EWAD$Template() {
	this.Html;
	this.Repeat;
}

function EWAD$XItem() {
	this.Name; // text
	this.HtmlTag; // inputtext
	this.BaseType; // 1
	this.Parameters;
	this.Descriptions = new EWAD$Descriptions();
	this.Template = new EWAD$Template();
}

function EWAD$XItems() {
	this._Set = new EWAC$SetBase();
	this.AddXItem = function(value) {
		this._Set.AddObject(value.Name, value);
	};
	this.GetXItem = function(name) {
		return this._Set.GetItem(name);
	};
	this.Count = function() {
		return this._Set.Count();
	};
	this.InitFromXml = function(classXml, xmlNode) {
		var nl = classXml.GetElements("XItems/XItem", xmlNode);
		for (var i = 0; i < nl.length; i = i + 1) {
			var val = new EWAD$XItem();
			val.Name = classXml.GetElementAttribute(nl[i], "Name");
			val.HtmlTag = classXml.GetElementAttribute(nl[i], "HtmlTag");
			val.BaseType = classXml.GetElementAttribute(nl[i], "BaseType");
			val.Parameters = classXml.GetElementAttribute(nl[i], "Parameters");
			val.Descriptions.InitFromXml(classXml, nl[i]);
			var nodeHtml = classXml.GetElement("Template/Html", nl[i]);
			var nodeRepeat = classXml.GetElement("Template/Repeat", nl[i]);
			if (nodeHtml != null) {
				val.Template.Html = classXml.GetElementText(nodeHtml);
			}
			if (nodeRepeat != null) {
				val.Template.Repeat = classXml.GetElementText(nodeRepeat);
			}
			this.AddXItem(val);
		}
	};
}

// -----------------------------------------
function EWAD$XGroupValue() {
	this.Name; // "InitValue"
	this.Default; // ="0"
	this.Values; // =",Tody,Tody-Time,Year-Month,Year,Time,IP">
	this.ListValues;
	this.ListDescriptions;
	this.Descriptions = new EWAD$Descriptions();
}

function EWAD$XGroupValues() {
	this._Set = new EWAC$SetBase();
	this.AddXGroupValue = function(value) {
		this._Set.AddObject(value.Name, value);
	};
	this.GetXGroupValue = function(name) {
		return this._Set.GetItem(name);
	};
	this.Count = function() {
		return this._Set.Count();
	};
	this.InitFromXml = function(classXml, xmlNode) {
		var nl = classXml.GetElements("XGroupValues/XGroupValue", xmlNode);
		for (var i = 0; i < nl.length; i = i + 1) {
			var val = new EWAD$XItem();
			val.Name = classXml.GetElementAttribute(nl[i], "Name");
			val.Default = classXml.GetElementAttribute(nl[i], "Default");
			val.Values = classXml.GetElementAttribute(nl[i], "Values");
			val.Descriptions.InitFromXml(classXml, nl[i]);
			val.ListValues = val.Values.split(",");
			val.ListDescriptions = val.Descriptions.GetDescription(EWA.LANG).Info.split(",");
			this.AddXGroupValue(val);
		}
	};
}

// -----------------------------------------
function EWAD$Valid() {
	this.Name;
	this.Regex;
	this.Descriptions = new EWAD$Descriptions();
}

function EWAD$Valids() {
	this.Valids = new EWAC$SetBase();
	this.InitFromXml = function(classXml, xmlNode) {
		var nl = classXml.GetElements("Valids/Valid", xmlNode);
		for (var i = 0; i < nl.length; i = i + 1) {
			var val = new EWAD$Valid();
			val.Name = classXml.GetElementAttribute(nl[i], "Name");
			val.Default = classXml.GetElementAttribute(nl[i], "Regex");
			val.Descriptions.InitFromXml(classXml, nl[i]);
			this.Valids.AddObject(val.Name, val);
		}
	};
}

// ----------------------------------------
function EWAD$Items() {
	this.Name = "Items";
	this.XItems = new EWAD$XItems();
	this.Descriptions = new EWAD$Descriptions();
	this.XItemParameters = new EWAD$XItemParameters();
	this.XItemParameters.Name = this.Name;
	this.XGroupValues = new EWAD$XGroupValues();
	this.InitFromXml = function(xmlClass) {
		var XItemPath = "EasyWebConfig/Items";
		var nodeXItem = xmlClass.GetElement(XItemPath);
		this.XItemParameters.InitFromXml(xmlClass, nodeXItem, this);
		this.XItems.InitFromXml(xmlClass, nodeXItem);
		this.Descriptions.InitFromXml(xmlClass, nodeXItem);
		this.XGroupValues.InitFromXml(xmlClass, nodeXItem);
	};
}

function EWAD$Page() {
	this.Name = "Page";
	this.Descriptions = new EWAD$Descriptions();
	this.XItemParameters = new EWAD$XItemParameters();
	this.XItemParameters.Name = this.Name;
	this.XGroupValues = new EWAD$XGroupValues();
	this.InitFromXml = function(xmlClass) {
		var XItemPath = "EasyWebConfig/Page";
		var nodeXItem = xmlClass.GetElement(XItemPath);
		this.XItemParameters.InitFromXml(xmlClass, nodeXItem, this);
		this.Descriptions.InitFromXml(xmlClass, nodeXItem);
		this.XGroupValues.InitFromXml(xmlClass, nodeXItem);
	};
}

function EWAD$Chart() {
	this.Name = "Chart";
	this.Descriptions = new EWAD$Descriptions();
	this.XItemParameters = new EWAD$XItemParameters();
	this.XItemParameters.Name = this.Name;
	this.XGroupValues = new EWAD$XGroupValues();
	this.InitFromXml = function(xmlClass) {
		var XItemPath = "EasyWebConfig/Chart";
		var nodeXItem = xmlClass.GetElement(XItemPath);
		this.XItemParameters.InitFromXml(xmlClass, nodeXItem, this);
		this.Descriptions.InitFromXml(xmlClass, nodeXItem);
		this.XGroupValues.InitFromXml(xmlClass, nodeXItem);
	};
}

function EWAD$Menu() {
	this.Name = "Menu";
	this.Descriptions = new EWAD$Descriptions();
	this.XItemParameters = new EWAD$XItemParameters();
	this.XItemParameters.Name = this.Name;
	this.XGroupValues = new EWAD$XGroupValues();
	this.InitFromXml = function(xmlClass) {
		var XItemPath = "EasyWebConfig/Menu";
		var nodeXItem = xmlClass.GetElement(XItemPath);
		this.XItemParameters.InitFromXml(xmlClass, nodeXItem, this);
		this.Descriptions.InitFromXml(xmlClass, nodeXItem);
		this.XGroupValues.InitFromXml(xmlClass, nodeXItem);
	};
}
function EWAD$Workflow() {
	this.Name = "Workflow";
	this.Descriptions = new EWAD$Descriptions();
	this.XItemParameters = new EWAD$XItemParameters();
	this.XItemParameters.Name = this.Name;
	this.XGroupValues = new EWAD$XGroupValues();
	this.InitFromXml = function(xmlClass) {
		var XItemPath = "EasyWebConfig/Workflow";
		var nodeXItem = xmlClass.GetElement(XItemPath);
		this.XItemParameters.InitFromXml(xmlClass, nodeXItem, this);
		this.Descriptions.InitFromXml(xmlClass, nodeXItem);
		this.XGroupValues.InitFromXml(xmlClass, nodeXItem);
	};
}
function EWAD$PageInfos() {
	this.Name = "PageInfos";
	this.Descriptions = new EWAD$Descriptions();
	this.XItemParameters = new EWAD$XItemParameters();
	this.XItemParameters.Name = this.Name;
	this.XGroupValues = new EWAD$XGroupValues();
	this.InitFromXml = function(xmlClass) {
		var XItemPath = "EasyWebConfig/PageInfos";
		var nodeXItem = xmlClass.GetElement(XItemPath);
		this.XItemParameters.InitFromXml(xmlClass, nodeXItem, this);
		this.Descriptions.InitFromXml(xmlClass, nodeXItem);
		this.XGroupValues.InitFromXml(xmlClass, nodeXItem);
	};
}

function EWAD$Action() {
	this.Name = "Action";
	this.Descriptions = new EWAD$Descriptions();
	this.XItemParameters = new EWAD$XItemParameters();
	this.XItemParameters.Name = this.Name;
	this.XGroupValues = new EWAD$XGroupValues();
	this.InitFromXml = function(xmlClass) {
		var XItemPath = "EasyWebConfig/Action";
		var nodeXItem = xmlClass.GetElement(XItemPath);
		this.XItemParameters.InitFromXml(xmlClass, nodeXItem, this);
		this.Descriptions.InitFromXml(xmlClass, nodeXItem);
		this.XGroupValues.InitFromXml(xmlClass, nodeXItem);
	};
}

// -----------------------------------------
function EWAD$Class() { // 定义类型
	this.Xml = new EWA.C.Xml();
	this.Items = new EWAD$Items();
	this.Page = new EWAD$Page();
	this.Action = new EWAD$Action();
	this.Menu = new EWAD$Menu();
	this.Infos = new EWAD$Infos();
	this.PageInfos = new EWAD$PageInfos();
	this.Valids = new EWAD$Valids();
	this.Chart = new EWAD$Chart(); // 图表定义
	this.Workflow = new EWAD$Workflow(); // 工作流程

	this.Init = function() {
		if (window._CFG_MAIN) {
			this.Xml.LoadXml(_CFG_MAIN);
		} else {
			var u = "/EWA_DEFINE/cgi-bin/xml/?TYPE=CFG_XML&XMLNAME=EwaConfig.xml";
			this.Xml.LoadXmlFile(EWA.CP + u);
		}
		if (window._CFG_GLOBAL) {
			var xml1 = new EWA.C.Xml();
			xml1.LoadXml(_CFG_GLOBAL);
			_XML_OBJS["EwaGlobal.xml"] = xml1;
		}
		_XML_OBJS["EwaConfig.xml"] = this.Xml;

		this.Items.InitFromXml(this.Xml);
		this.Page.InitFromXml(this.Xml);
		this.Action.InitFromXml(this.Xml);
		this.Menu.InitFromXml(this.Xml);

		this.PageInfos.InitFromXml(this.Xml);

		this.Chart.InitFromXml(this.Xml);

		this.Workflow.InitFromXml(this.Xml);
	};
	this._InitInfos = function() {
		if (window._CFG_GLOBAL) {
			this.Xml.LoadXml(_CFG_GLOBAL);
		} else {
			var u = "/EWA_DEFINE/cgi-bin/xml/?TYPE=CFG_XML&XMLNAME=EwaGlobal.xml";
			this.Xml.LoadXmlFile(EWA.CP + u);
		}
		this.Infos.InitFromXml(this.Xml);
		this.Valids.InitFromXml(this.Xml);
	}
}

// ----------------------------------------------
function EWAC$Column() {
	this.XItemParameterValue;
	this.Name;
	this.IsSet = false;
	this.ChildParameter; // 子集合参数
}

function EWAC$Row() {
	this.Fields = new Array();
	this.IsDelete = false;
	this.Index = -1231;
	this.RowId = -1;
}

function EWAC$Table() {
	this.XmlNode;
	this.Name;
	this.XItemParameter;
	this.Columns = new EWAC$SetBase();
	this.Rows = new Array();
	this._UserClass;
	this.Sort = function() { // 排序
		this.Rows.sort(function(a, b) {
			return a.Index >= b.Index ? 1 : -1;
		});
	};
	this.AddRow = function() {
		var row = this.Rows[this.Rows.length] = new EWAC$Row();
		row.RowId = this.Rows.length - 1;
		if (this.XItemParameter.Children != null) {
			// 增加子集合字段解构
			for (var i = 0; i < this.Columns.Count(); i += 1) {
				var cp = this.Columns.GetItem(i);
				if (cp.IsSet) {
					row.Fields[i] = this._UserClass._InitTable(cp.ChildParameter);
				}
			}
		}
		return row;
	};
	this.GetSingleValue = function() {
		if (this.Rows.length == 0 || this.Rows[0].Fields.length == 0) {
			return null;
		}
		return this.Rows[0].Fields[0];
	};
	this.GetValue = function(name) {
		var index = this.Columns.GetIndex(name);
		if (this.Rows.length == 0 || this.Rows[0].Fields.length == 0) {
			return null;
		}
		if (index == null) {
			alert(name + " index not fuound");
			return null;
		}
		return this.Rows[0].Fields[index];
	};
	this.SearchRow = function(searchExpr) {
		var s1 = searchExpr.split("=");
		var fieldIndex = this.Columns.GetIndex(s1[0].trim());
		for (var i = 0; i < this.Rows.length; i += 1) {
			this.Rows[i].RowId = i;
			if (this.Rows[i].Fields[fieldIndex] == s1[1]) {
				return this.Rows[i];
			}
		}
		return null;
	};
	this.SearchValue = function(searchExpr, valName) {
		var row = this.SearchRow(searchExpr);
		if (row == null) {
			return null;
		}
		var fieldIndex = this.Columns.GetIndex(valName.trim());
		if (fieldIndex == null) {
			alert(name + " index not fuound");
			return null;
		}
		return row.Fields[fieldIndex];
	};
	this.Clear = function() {
		this.Rows = new Array();
	};
}

function EWAC$Tables() {
	this.Tables = new EWAC$SetBase();
	this.SetValue = function(tableName, fieldName, rowIndex, val) {
		var table = this.Tables.GetItem(tableName);
		fieldIndex = table.Columns.GetIndex(fieldName);
		if (table.Rows[rowIndex] == null) {
			table.Rows[rowIndex] = new EWAC$Row();
		}
		var v1 = table.Rows[rowIndex].Fields[fieldIndex];
		if (v1 == val) {
			return false;
		} else {
			table.Rows[rowIndex].Fields[fieldIndex] = val;
			return true;
		}
	};
	this.Clear = function(tableName) {
		var table = this.Tables.GetItem(tableName);
		table.Clear();
	};
}

function EWAC$Res() {
	this.Table;
}

// ----------------------------------------------
var EWAC_CFG_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
	+ "<EasyWebTemplate Name='[NAME]'><Page /><Action /><XItems /><Menus />"
	+ "<Charts /><PageInfos /><Workflows /></EasyWebTemplate>";
function EWAC_UserClass() {
	this.PageTables = new EWAC$Tables(); // Page参数
	this.ActionTables = new EWAC$Tables(); // Action参数
	this.MenuTablesSet = new EWAC$SetBase(); // Menu参数
	this.ItemsTablesSet = new EWAC$SetBase(); // Items参数
	this.PageInfosTablesSet = new EWAC$SetBase(); // PageInfos参数
	this.ChartTableSet = new EWAC$SetBase(); // Chart参数
	this.WorkflowTableSet = new EWAC$SetBase(); // Workflow参数
	// -------------------------------------
	this.Xml = new EWA.C.Xml();
	this.Config = new EWAD$Class(); // 系统定义类型
	this.ResTables = new Array();
	// --------------------------------------
	this.XmlUrl;
	this.Name;
	this.FrameTag;
	// -------------------------------------
	this.CreateXml = function() {
		var xml = new EWA.C.Xml();
		var x1 = EWAC_CFG_XML.replace("[NAME]", this.Name);
		xml.LoadXml(x1);
		// page
		var nodePage = xml.GetElement("EasyWebTemplate/Page");
		this._CreateXmlPart(nodePage, this.PageTables, xml);
		// Action
		var actionPage = xml.GetElement("EasyWebTemplate/Action");
		this._CreateXmlPart(actionPage, this.ActionTables, xml);
		// items
		var itemsPage = xml.GetElement("EasyWebTemplate/XItems");
		for (var i = 0; i < this.ItemsTablesSet.Count(); i += 1) {
			var tables = this.ItemsTablesSet.GetItem(i);
			if (tables.IsDelete) {
				continue;
			}
			var nodeItem = xml.NewChild("XItem", itemsPage);
			this._CreateXmlPart(nodeItem, tables, xml);
			xml.SetAttribute("Name", tables.Name, nodeItem);
		}

		// menus
		var menusPage = xml.GetElement("EasyWebTemplate/Menus");
		for (var i = 0; i < this.MenuTablesSet.Count(); i += 1) {
			var tables = this.MenuTablesSet.GetItem(i);
			if (tables.IsDelete) {
				continue;
			}
			var nodeItem = xml.NewChild("Menu", menusPage);
			this._CreateXmlPart(nodeItem, tables, xml);
			xml.SetAttribute("Name", tables.Name, nodeItem);
		}

		// charts
		var menusPage = xml.GetElement("EasyWebTemplate/Charts");
		for (var i = 0; i < this.ChartTableSet.Count(); i += 1) {
			var tables = this.ChartTableSet.GetItem(i);
			if (tables.IsDelete) {
				continue;
			}
			var nodeItem = xml.NewChild("Chart", menusPage);
			this._CreateXmlPart(nodeItem, tables, xml);
			xml.SetAttribute("Name", tables.Name, nodeItem);
		}

		// workflow
		var menusPage = xml.GetElement("EasyWebTemplate/Workflows");
		for (var i = 0; i < this.WorkflowTableSet.Count(); i += 1) {
			var tables = this.WorkflowTableSet.GetItem(i);
			if (tables.IsDelete) {
				continue;
			}
			var nodeItem = xml.NewChild("Workflow", menusPage);
			this._CreateXmlPart(nodeItem, tables, xml);
			xml.SetAttribute("Name", tables.Name, nodeItem);
		}

		// pageinfos
		var menusPage = xml.GetElement("EasyWebTemplate/PageInfos");
		for (var i = 0; i < this.PageInfosTablesSet.Count(); i += 1) {
			var tables = this.PageInfosTablesSet.GetItem(i);
			if (tables.IsDelete) {
				continue;
			}
			var nodeItem = xml.NewChild("PageInfo", menusPage);
			this._CreateXmlPart(nodeItem, tables, xml);
			xml.SetAttribute("Name", tables.Name, nodeItem);
		}
		var node = xml.GetElement("EasyWebTemplate");
		var s1 = xml.GetXml(node);
		xml = null;
		return s1;
	};
	this._CreateXmlPart = function(node, tables, xml) {
		/*
		 * <DescriptionSet> <Set Lang="zhcn" Info="测试信息" Memo="" /> <Set
		 * Lang="enus" Info="Test Information" Memo="" /> </DescriptionSet>
		 */
		for (var i = 0; i < tables.Tables.Count(); i += 1) {
			var table = tables.Tables.GetItem(i);
			this._CreateTableXml(table, node, xml);
			child = null;
		}
	};
	this._CreateTableXml = function(table, node, xml) {
		var p = table.XItemParameter;
		var child = xml.NewChild(p.Name, node);
		for (var n = 0; n < table.Rows.length; n += 1) {
			var dataRow = table.Rows[n];
			if (dataRow.IsDelete) {
				continue;
			}
			var rowChild = xml.NewChild("Set", child);
			for (var m = 0; m < table.Columns.Count(); m += 1) {
				var col = table.Columns.GetItem(m);
				if (col.IsSet) {
					this._CreateTableXml(dataRow.Fields[m], rowChild, xml);
				} else {
					var v = col.XItemParameterValue;
					var uVal = dataRow.Fields[m];
					if (v.IsCDATA == "1") {
						var cdataChild = xml.NewChild(col.Name, rowChild);
						xml.SetCData(uVal, cdataChild);
					} else {
						// uVal=
						// uVal.replace(/\r/ig,'&#x000A;').replace(/\n/ig,'&#x000D;');
						xml.SetAttribute(col.Name, uVal, rowChild);
					}
				}
			}
			rowChild = null;
		}
	};
	// ------------调用XML文件-----------------------------
	this.Load = function() {
		this.Config.Init();
		this.Xml.LoadXml(_CFG_ITEM);
		this._Init();
	}
	this.LoadXml = function(url) {
		this.Config.Init();
		this.XmlUrl = url;
		this.Xml.LoadXmlFile(url);
		this._Init();
	};
	this.LoadXmlString = function(xmlString) {
		this.Config.Init();
		this.Xml.LoadXml(xmlString);
		this._Init();
	};
	this._Init = function() {
		var node1 = this.Xml.GetElement("EasyWebTemplate");
		this.Name = this.Xml.GetElementAttribute(node1, "Name");
		var pagePath = "EasyWebTemplate/Page";
		var nodePage = this.Xml.GetElement(pagePath);
		this.PageTables = this._InitTables(nodePage, this.Config.Page.XItemParameters);
		this.FrameTag = this.PageTables.Tables.GetItem("FrameTag").GetSingleValue(); // 配置文件类型
		//
		var actionPath = "EasyWebTemplate/Action";
		var actionPage = this.Xml.GetElement(actionPath);
		this.ActionTables = this._InitTables(actionPage, this.Config.Action.XItemParameters);
		//
		var ItemPath = "EasyWebTemplate/XItems/XItem";
		var items = this.Xml.GetElements(ItemPath);
		if (items != null) {
			for (var i = 0; i < items.length; i += 1) {
				var tables = this._InitTables(items[i], this.Config.Items.XItemParameters);
				var name = tables.Tables.GetItem("Name").GetSingleValue();
				tables.Name = name;
				this.ItemsTablesSet.AddObject(name, tables);
			}
		}
		//
		var menuPath = "EasyWebTemplate/Menus/Menu";
		var items = this.Xml.GetElements(menuPath);
		if (items != null) {
			for (var i = 0; i < items.length; i += 1) {
				var tables = this._InitTables(items[i], this.Config.Menu.XItemParameters);
				var name = tables.Tables.GetItem("Name").GetSingleValue();
				tables.Name = name;
				this.MenuTablesSet.AddObject(name, tables);
			}
		}

		// workflows
		var workflowPath = "EasyWebTemplate/Workflows/Workflow";
		var items = this.Xml.GetElements(workflowPath);
		if (items != null) {
			for (var i = 0; i < items.length; i += 1) {
				var tables = this._InitTables(items[i], this.Config.Workflow.XItemParameters);
				var name = tables.Tables.GetItem("Name").GetSingleValue();
				tables.Name = name;
				this.WorkflowTableSet.AddObject(name, tables);
			}
		}

		var pageInfosPage = "EasyWebTemplate/PageInfos/PageInfo";
		var items = this.Xml.GetElements(pageInfosPage);
		if (items != null) {
			for (var i = 0; i < items.length; i += 1) {
				var tables = this._InitTables(items[i], this.Config.PageInfos.XItemParameters);
				var name = tables.Tables.GetItem("Name").GetSingleValue();
				tables.Name = name;
				this.PageInfosTablesSet.AddObject(name, tables);
			}
		}

		var chartsPage = "EasyWebTemplate/Charts/Chart";
		var items = this.Xml.GetElements(chartsPage);
		if (items != null) {
			for (var i = 0; i < items.length; i += 1) {
				var tables = this._InitTables(items[i], this.Config.Chart.XItemParameters);
				var name = tables.Tables.GetItem("Name").GetSingleValue();
				tables.Name = name;
				this.ChartTableSet.AddObject(name, tables);
			}
		}
	};
	this._InitTables = function(node, xItemParameters) {
		var tables = new EWAC$Tables();
		for (var i = 0; i < xItemParameters.Count(); i += 1) {
			var p = xItemParameters.GetXItemParameter(i);
			var table = this._InitTable(p);
			tables.Tables.AddObject(table.Name, table);
			if (node != null) {
				var nodeItem = this.Xml.GetElement(p.Name, node);
				if (nodeItem != null) {
					this._InitTableValue(table, nodeItem, p.Values);
				}
			}
		}
		return tables;
	};
	this.CreateItemTables = function(xItemParameters) { // 生成Item数据结构
		return this._InitTables(null, xItemParameters);
	};
	this._InitTable = function(xItemParameter) {
		var table = new EWAC$Table();
		table.Name = xItemParameter.Name;
		table.XItemParameter = xItemParameter;
		for (var m = 0; m < xItemParameter.Values.Count(); m += 1) {
			var v = xItemParameter.Values.GetValue(m);
			var col = new EWAC$Column();
			col.Name = v.Name;
			table.Columns.AddObject(col.Name, col);
			col.XItemParameterValue = v;
			col = null;
		}
		if (xItemParameter.Children != null) { // 有子集合
			table._UserClass = this;
			for (var i = 0; i < xItemParameter.Children.Count(); i += 1) {
				var p1 = xItemParameter.Children.GetItem(i);
				col = new EWAC$Column();
				col.Name = p1.Name;
				col.IsSet = true;
				col.ChildParameter = p1; // 子集合参数
				table.Columns.AddObject(col.Name, col);
			}
		}
		if (table.Name == "DescriptionSet") {
			this.ResTables[this.ResTables.length] = table;
		}
		return table;
	};
	this._InitTableValue = function(table, nodeItem, values) {
		table.XmlNode = nodeItem;
		// 获取参数行集合 <Set DataSource="Test" />
		var mmm = 0;
		for (var n = 0; n < nodeItem.childNodes.length; n += 1) { // 获取用户定义数据
			var setNode = nodeItem.childNodes[n];
			if (setNode.nodeType != 1 || setNode.nodeName != "Set") {
				continue;
			}
			var row = table.AddRow(); // 行
			for (var m = 0; m < values.Count(); m += 1) {
				var v = values.GetValue(m);
				if (v.IsCDATA == "1") {
					var nodeData = this.Xml.GetElement(v.Name, setNode);
					if (nodeData != null) {
						row.Fields[m] = this.Xml.GetElementText(nodeData);
					}
					nodeData = null;
				} else {
					row.Fields[m] = this.Xml.GetElementAttribute(setNode, v.Name);
				}
			}
			mmm += 1;
			if (table.XItemParameter.Children == null) {
				continue;
			}
			for (var i = 0; i < table.XItemParameter.Children.Count(); i += 1) {
				var p1 = table.XItemParameter.Children.GetItem(i);
				var tableIndex = table.Columns.GetIndex(p1.Name);
				var table1 = row.Fields[tableIndex];
				var nodeItem1 = this.Xml.GetElement(p1.Name, setNode);
				if (nodeItem1 != null) {
					this._InitTableValue(table1, nodeItem1, p1.Values);
				}
			}
		}
		return table;
	};
}

// ----------------------------------------
function EWAC_UIClass(obj, userClass) {
	this._UIObject = obj;
	this._UserClass = userClass;
	this._Table;
	this._CurType = "PAGE";
	this._CurItem = "";
	this.IsChanged = false; // 属性内容是否更改了
	this.Connections = new EWAD$Connections();
	this.__GROUP = new Object();

	/**
	 * 建立资源列表信息
	 */
	this.CreateResList = function() {
		var s1 = "<table border=0 id='resTable' class='EWA_TABLE' width=100% cellspacing=1 cellpadding=1>";
		var langGroup = this._UserClass.Config.Page.XGroupValues.GetXGroupValue("Lang");
		var ress = this._UserClass.ResTables;
		var vals = langGroup.ListValues;
		var ts = langGroup.ListDescriptions;
		var pValues = ress[0].XItemParameter.Values;
		s1 += "<tr><td class=EWA_TD_H rowSpan=2 width=130><a href='javascript:tall();'>翻译</a></td>";
		var s2 = "<tr>";
		var a = new Array();
		for (var i = 0; i < vals.length; i += 1) {
			s1 += "<td class=EWA_TD_H colSpan=" + (pValues.Count() - 1) + ">" + vals[i] + " (" + ts[i] + ")</td>";
			a[i] = {
				Name: vals[i],
				Fields: new Array()
			};
			for (var m = 0; m < pValues.Count(); m += 1) {
				var v = pValues.GetValue(m);
				if (v.Name == "Lang") {
					continue;
				}
				var des = v.Descriptions.GetDescription(EWA.LANG).Info;
				s2 += "<td class=EWA_TD_H>" + v.Name + " (" + des + ")</td>";
				a[i].Fields[a[i].Fields.length] = v.Name;
			}
		}
		s1 += "</tr>" + s2 + "</tr>";
		var onclick = "onblur='var o1='";
		for (i = 0; i < ress.length; i += 1) {
			var table = ress[i];
			if (table.IsDelete) {
				continue;
			}
			var parent = table.XItemParameter.Parent;
			var id1 = parent.Name;
			var id2 = "";
			var psName = table.XItemParameter.XItemParameters.Name.trim();
			s2 = "<tr><td class=EWA_TD_M width=150><nobr>" + parent.Name + " ("
				+ parent.Descriptions.GetDescription(EWA.LANG).Info + ")";
			if (psName == "Items" || psName == "Menu" || psName == "PageInfos" || psName == "Chart") {
				if (table.XmlNode != null) {
					id2 = table.XmlNode.parentNode.getAttribute("Name");
				}
				s2 += " - <b>" + id2 + "</b>";
			}
			s2 += "</nobr></td>";
			for (var m = 0; m < a.length; m += 1) {
				var a1 = a[m];
				var row = table.SearchRow("Lang=" + a1.Name);
				var id3 = a1.Name;
				for (var n = 0; n < a1.Fields.length; n += 1) {
					var f = a1.Fields[n];
					var val = "";
					var index = table.Columns.GetIndex(f);
					if (row != null) {
						val = row.Fields[index];
					} else {
						row = table.AddRow();
						var index1 = table.Columns.GetIndex("Lang");
						row.Fields[index1] = a1.Name;
					}
					var id = id1 + "." + id2 + "." + id3 + "." + f;
					s2 += "<td class=EWA_TD_M><input onblur='aa(this);window.parent._EWAC_UI.ResChange(this);' lang_id='"
						+ a1.Name + "' table_id='" + i + "' row_id='" + row.RowId + "' field_id=" + index + " id='" + id
						+ "' style='width:100%' type=text value=\"" + (val == null ? "" : val.replace(/\\"/ig, "\\\""))
						+ "\"></td>";
				}
			}
			s1 = s1 + s2 + "</tr>";
		}
		return s1 + "</table>";
	};

	// ---------------调整ITEMS-----------------
	this.ChangeItems = function(tag) { //
		// 调用命令
		var callCmd = new EWA_Command();
		callCmd.IsRunAuto = true;
		callCmd.CmdArgus[0] = this;
		callCmd.CmdArgus[2] = tag;
		// 返回参数
		callCmd.Cmd = function(a) {
			a[1].GetIframeWindow().InitItemsSet(a);
		};
		// 返回命令
		var retCmd = new EWA_Command();
		retCmd.CmdArgus[0] = this;
		// opened url
		url = EWA.CP + "/EWA_STYLE/cgi-bin/?XMLNAME=ewa/ewa.xml&ITEMNAME=define_edit_set";
		callCmd.CmdArgus[1] = EWA.UI.Dialog.OpenWindow(url, Math.random(), 510, 220, false, callCmd, retCmd);
	};
	// ----------------类型修改-----------------
	this.ChangeType = function(type) { // 类型修改
		type = type.toUpperCase();
		if (type.indexOf("$") > 0) {
			var s = type.split("$");
			this._CurType = s[0];
			this._CurItem = s[1];
		} else {
			this._CurType = type;
			this._CurItem = "";
		}
		this.ShowPageParameters();
		// droplist 辅助绑定按钮
		EWAC_DDL.bind();
	};

	this.clickAndCopyProperities = function(tr) {
		let title = tr.cells[1].title;

		let s;
		if (title.indexOf('EWA_UP') >= 0) {
			s = title.replace(/EWA\_UP/ig, "@EWA_UP");
		} else {
			let name = $('#EWAC_SELECT_TYPE').val();
			name = "@" + name.split('$')[1];
			s = title.replace(/\{NAME\}/ig, name);
		}


		// console.log(title, s);
		this.copyValue(s);
	};

	this.copyValue = function(s) {
		let input1 = $('<textarea style="position:absolute;right:0;bottom:0;opacity:0;"></textarea>');
		let input = input1[0];
		input.value = s;
		$('body').append(input);
		setTimeout(function() {
			input.focus();
			if (navigator.userAgent.match(/(iPhone|iPod|iPad);?/i)) { //区分iPhone设备
				input.select();
				input.setSelectionRange(0, input.value.length); //兼容ios
			} else {
				input.select("All");
			}
			var rst = document.execCommand('copy');
			console.log(rst);
			if (!rst) {
				alert('复制失败' + rst);
			} else {
				$Tip(window.EWA && EWA.LANG == 'enus' ? 'Copyed' : '复制完成');
			}
			input.blur();
			$(input).remove();
		}, 111);
	};

	// ----------------显示参数-----------------
	this.ShowPageParameters = function() {
		var tbId = "EWA_TYPE$" + this._CurType + "$" + this._CurItem; // this._ParametersTable

		if (this._Table != null) {
			this._Table.style.display = "none";
			this._Table = null;
		}
		this._Table = document.getElementById(tbId);
		if (this._Table == null) {
			this._CreateParametersTable(tbId);
		} else {
			this._Table.style.display = "";
		}
		if (this._CurItem != "") {
			this.ShowHiddenItemParameters();
		}


	};
	// 显示隐藏参数行,对应XITEM
	this.ShowHiddenItemParameters = function() {
		var table = this._GetTables().Tables.GetItem("Tag");
		if (table == null) {
			return;
		}
		var tag = table.GetSingleValue();
		var xitem = this._UserClass.Config.Items.XItems.GetXItem(tag);
		var pss = xitem.Parameters.split(",");
		for (var i = 0; i < pss.length; i += 1) {
			pss[i] = pss[i].trim();
		}
		var ps = "~~" + pss.join("~~") + "~~";
		for (i = 0; i < this._Table.rows.length; i += 1) {
			var ewaid = this._Table.rows[i].getAttribute("EWA_ID");
			if (ewaid.toUpperCase() == "TAG" || ewaid.toUpperCase() == "NAME") {
				this._Table.rows[i].style.display = "";
				continue;
			}
			var id = "~~" + ewaid + "~~";
			if (ps.indexOf(id) >= 0) {
				var frames = this._UserClass.Config.Items.XItemParameters.GetXItemParameter(ewaid).Frames;

				if (this._CheckFrameTag(frames)) {
					this._Table.rows[i].style.display = "";
				} else {
					this._Table.rows[i].style.display = "none";
				}
			} else {
				this._Table.rows[i].style.display = "none";
			}
		}
	};
	this._CheckFrameTag = function(tag) {
		if (tag == null || tag.trim().length == 0) {
			return true;
		}
		var tags = tag.toUpperCase().split(',');
		var frameTag = this._UserClass.FrameTag.toUpperCase();
		for (var i = 0; i < tags.length; i++) {
			var t = tags[i].trim();
			if (t == frameTag) {
				return true;
			}
		}
		return false;

	}
	// 生成参数显示列表
	this._CreateParametersTable = function(tbId) {
		this._Table = document.createElement("table");
		this._Table.cellSpacing = "1";
		this._Table.cellPadding = "0";
		this._Table.bgColor = "#CCCCCC";
		this._Table.width = "100%";
		this._Table.id = tbId;
		var ps = this._GetParameters();
		for (var i = 0; i < ps.Count(); i += 1) {
			var p = ps.GetXItemParameter(i);
			if (!this._CheckFrameTag(p.FrameTag)) {
				continue;
			}
			if (p.IsChildren == "1") { // 子集合，不生成
				continue;
			}
			if (p.IsSet == "1") {
				this._CreateSet(p);
			} else {
				if (p.IsMulti != "1") {
					this._CreateSingle(p);
				} else {
					this._CreateMulti(p);
				}
			}
		}
		this._UIObject.appendChild(this._Table);
		var oSelect = document.getElementsByTagName("select")[0];
		if (oSelect.options.length == 0) {
			this.CreateType();
		}
	};
	// 生成列表框内容
	this.CreateType = function() {
		var frameTag = this._UserClass.FrameTag.toUpperCase();
		var oSelect = document.getElementsByTagName("select")[0];
		var sb = [];
		var s1 = "<select style='width:100%' id='EWAC_SELECT_TYPE' onchange='_EWAC_UI.ChangeType(this.value)'>";
		sb.push(s1);
		// page
		s1 = this._CreateTypeOption(this._UserClass.Config.Page, oSelect);
		sb.push(s1);
		// action
		s1 = this._CreateTypeOption(this._UserClass.Config.Action, oSelect);
		sb.push(s1);
		// items
		if (frameTag != "TREE1") {
			var s2 = this._CreateTypeGroup(this._UserClass.Config.Items, this._UserClass.ItemsTablesSet);
			sb.push(s2);
		}

		// menu
		var s2 = this._CreateTypeGroup(this._UserClass.Config.Menu, this._UserClass.MenuTablesSet);
		sb.push(s2);

		// chart
		if (frameTag == "LISTFRAME") {
			var s2 = this._CreateTypeGroup(this._UserClass.Config.Chart, this._UserClass.ChartTableSet);
			sb.push(s2);
		}

		if (frameTag == "LISTFRAME" || frameTag == "FRAME") {
			var s2 = this._CreateTypeGroup(this._UserClass.Config.Workflow, this._UserClass.WorkflowTableSet);
			sb.push(s2);
		}

		// pageinfos
		// var s2 = this._CreateTypeGroup(this._UserClass.Config.PageInfos,
		// this._UserClass.PageInfosTablesSet);
		// s1 += s2;

		//
		sb.push("</select>");
		oSelect.parentNode.innerHTML = sb.join('');

	};
	this._CreateTypeGroup = function(typeClass, tablesSet) {
		var des = typeClass.Descriptions.GetDescription(EWA.LANG).Info;
		var s1 = "<optgroup label='" + typeClass.Name + " (" + des + ")'>";
		var items = tablesSet;
		for (var i = 0; i < items.Count(); i += 1) {
			var tables = items.GetItem(i);
			if (tables.IsDelete) {
				continue;
			}
			var name = tables.Tables.GetItem("Name").GetSingleValue();
			var info = tables.Tables.GetItem("DescriptionSet").SearchValue("Lang=zhcn", "Info");
			s1 += "<option value=\"" + typeClass.Name + "$" + name + "\">" + name + "( " + info + ")</option>";
		}
		s1 += "</optgroup>";
		return s1;
	};
	this._CreateTypeOption = function(typeClass, oSelect) {
		var info = typeClass.Descriptions.GetDescription(EWA.LANG).Info;
		var name = typeClass.Name;
		return "<option value=\"" + name + "\">" + name + "( " + info + ")</option>";
	};

	// 集合属性
	this._CreateSet = function(xItemParameter) {
		var tr = this._CreateRow(xItemParameter);
		if (!tr) {
			return;
		}
		tr.cells[1].width = "";
		var s0 = xItemParameter.Name + " (" + xItemParameter.Descriptions.GetDescription(EWA.LANG).Info + ")";
		var s1 = "&nbsp;<b style='color:darkblue'>" + s0 + "</b>";
		tr.cells[0].innerHTML = "<div>-</div>";
		tr.cells[1].innerHTML = s1;
		tr = null;
	};

	// 单值属性
	this._CreateSingle = function(xItemParameter) {
		var tr = this._CreateRow(xItemParameter, xItemParameter.Values.GetValue(0));
		if (!tr) {
			return;
		}
		tr.cells[1].noWrap = true;
		tr.cells[1].innerHTML = "&nbsp;<b>" + xItemParameter.Name + "</b>";
		tr.cells[1].title = xItemParameter.Descriptions.GetDescription(EWA.LANG).Info;
		var table = this._GetTables().Tables.GetItem(xItemParameter.Name);
		var val = table.GetSingleValue();
		var obj1 = this.CreateEdit(xItemParameter, xItemParameter.Values.GetValue(0), val);
		tr.cells[2].appendChild(obj1);
	};

	// 多值属性
	this._CreateMulti = function(xItemParameter) {
		var tr = this._Table.insertRow(-1);
		tr.setAttribute("EWA_ID", xItemParameter.Name); // 用于显示隐藏用
		tr.bgColor = "white";
		tr.insertCell(-1);
		tr.insertCell(-1);
		var s1 = "<div style='float:left'>&nbsp;<b style='color:blue'>" + xItemParameter.Name + " ("
			+ xItemParameter.Descriptions.GetDescription(EWA.LANG).Info + ")</b></div>";
		if (xItemParameter.OuterButton != null) {
			s1 += "<div align=right><input type=button value='A' onclick=\"" + xItemParameter.OuterButton + "\"></div>";
		}
		tr.cells[1].innerHTML = s1;
		tr.cells[1].colSpan = 3;
		tr.cells[1].height = 22;
		tr.cells[0].innerHTML = "<div style='cursor:pointer;padding-left:2px;padding-right:2px'><b>-</b></div>";
		tr.cells[0].bgColor = "#CCCCCC";
		tr.cells[0].align = "center";
		var table = this._GetTables().Tables.GetItem(xItemParameter.Name);
		for (var i = 0; i < xItemParameter.Values.Count(); i += 1) {
			var v = xItemParameter.Values.GetValue(i);
			var tra = this._CreateRow(xItemParameter, v);
			if (!tra) {
				continue;
			}
			let title = v.Descriptions.GetDescription(EWA.LANG).Info;
			tra.cells[1].style.paddingLeft = "10px";
			tra.cells[1].title = title;
			if (title.indexOf('{NAME}') >= 0 || title.indexOf('EWA_UP') >= 0) {// 复制属性
				tra.cells[1].innerHTML = "<a>" + v.Name + " <i class='fa fa-copy'></i></a>";
				tra.cells[1].setAttribute('onclick', '_EWAC_UI.clickAndCopyProperities(this.parentNode)');
			} else {
				tra.cells[1].innerHTML = v.Name;
			}
			var val = table.GetValue(v.Name);
			var obja = this.CreateEdit(xItemParameter, v, val);
			tra.cells[2].appendChild(obja);
			tra = obja = null;
		}
		obj = tr = null;
	};

	// 生成参数行
	this._CreateRow = function(xItemParameter, xValue) {
		if (xValue && xValue.IsShow == '0') {
			console.log(xValue, xItemParameter);
			return null;
		}

		var tr = this._Table.insertRow(-1);
		tr.setAttribute("EWA_ID", xItemParameter.Name); // 用于显示隐藏用
		tr.bgColor = "white";
		tr.insertCell(-1);
		tr.insertCell(-1);
		tr.insertCell(-1);
		tr.cells[0].align = "center";
		tr.cells[1].width = "100";
		if (xValue != null && (xValue.Type.toLowerCase() == "group" || xValue.Type.toLowerCase() == "ref")) { // 选择没有按钮
			tr.cells[2].colSpan = 2;
		} else {
			if (xItemParameter.IsSet == "1") {
				tr.cells[1].colSpan = 2;
			} else {
				tr.insertCell(-1);
				tr.cells[3].width = "20";
			}
			tr.cells[tr.cells.length - 1].align = "right";
			if (xValue != null && xValue.OuterButton != null && xValue.OuterButton.length > 0) {
				var tmp = '<input id="T_' + Math.random() + '" type=button onclick="' + xValue.OuterButton
					+ '" value="..." style="border:1px solid #ccc; cursor:pointer">';
				tr.cells[tr.cells.length - 1].innerHTML = tmp;
			} else {
				if (xValue && xValue.ReadOnly == '1') {
				} else {
					tr.cells[tr.cells.length - 1].innerHTML = this.CreateButton(xItemParameter, xValue, null);
				}
			}
		}
		return tr;
	};
	// xItemParameter:当前参数
	// xValue:当前参数值
	//
	this.CreateButton = function(xItemParameter, xValue, childXItemParameter, rowIndex) {
		var s1 = "<input style='border:1px solid #ccc; cursor:pointer' type=button P_NAME='" + xItemParameter.Name + "' ";
		if (xValue != null) {
			s1 += "V_NAME='" + xValue.Name + "' ";
		}
		if (childXItemParameter != null) {
			s1 += " C_NAME='" + childXItemParameter.Name + "' ";
			s1 += " R_INDEX='" + rowIndex + "' ";
		}

		s1 += " value='...' onclick='_EWAC_UI.Edit(this)' />";
		return s1;
	};
	// 生成编辑框
	this.CreateEdit = function(xItemParameter, xValue, val, doc) {
		var obj;
		if (xValue.Type.toLowerCase() == "group") {
			obj = this._CreateGroup(xItemParameter, xValue, val, doc);
			obj.style.width = "100%";
		} else {
			if (xValue.Type.toLowerCase() == "ref") {
				obj = this._CreateGroup(xItemParameter, xValue, val, doc);
				obj.style.width = "100%";
			} else if (xValue.Type.toLowerCase() == "button") {
				obj = this._CreateInput(xItemParameter, xValue, val, doc);
				obj.style.width = "120px";
			} else {
				obj = this._CreateInput(xItemParameter, xValue, val, doc);
				obj.style.width = "120px";
			}
		}
		obj.id = this._CurType + "$" + xItemParameter.Name + "$" + xValue.Name;
		return obj;
	};
	// 生成输入框
	this._CreateInput = function(xItemParameter, xValue, val, doc) {
		var doc1 = doc;
		if (doc1 == null) {
			doc1 = document;
		}
		var obj = doc1.createElement("input");
		obj.style.border = "none";
		if (xValue.Type.toLowerCase() == "html" || xValue.Type.toLowerCase() == "jscript"
			|| xValue.Type.toLowerCase() == "css" || xValue.ReadOnly == '1'
			|| xValue.Type.toLowerCase() == "md"
		) {
			obj.setAttribute("readOnly", true);
			obj.style.backgroundColor = "#DDD";
			obj.setAttribute("EWA_COLOR_TYPE", xValue.Type.toLowerCase());
		} else {
			obj.style.backgroundColor = "#fff";
		}
		var v1 = val == null ? "" : val;
		obj.value = v1;
		obj.title = v1;
		obj.setAttribute("EWA_VALUE", v1);
		obj.setAttribute("EWA_OLD_VALUE", obj.value);
		if (doc != null) {
			obj.setAttribute("EWA_SET", 1);
		}
		obj.onblur = function() {
			var oldVal = this.getAttribute("EWA_OLD_VALUE");
			if (oldVal != this.value) {
				// 在输入框内输入内容
				this.setAttribute("EWA_VALUE", this.value);
				this.setAttribute("EWA_OLD_VALUE", this.value);
				this.title = this.value;
			}
			if (this.getAttribute("EWA_SET") != "1") {
				// 不是集合，修改内容
				var w = this.ownerDocument.parentWindow ? this.ownerDocument.parentWindow : this.ownerDocument.defaultView;
				w._EWAC_UI.Change(this);
				w = null;
			}
		};
		doc1 = null;
		return obj;
	};
	this.Change = function(obj) {
		var id = obj.id.split("$");
		var val = obj.value;
		if (obj.tagName == "INPUT") {
			val = obj.getAttribute("EWA_VALUE");
		}
		if (val == null)
			val = "";
		this.IsChanged = this._GetTables().SetValue(id[1], id[2], 0, val);
	};
	// 生成选择框
	this._CreateGroup = function(xItemParameter, xValue, val, doc) {
		var doc1 = doc;
		if (doc1 == null) {
			doc1 = document;
		}
		var vs = "?",
			ts = "?";
		var obj = doc1.createElement("select");
		if (xValue.Type.toLowerCase() == "group") {
			// var groups = this._GetXGroupValues();
			// var grp = groups.GetXGroupValue(xValue.Name);
			var grp = xItemParameter.Parent.XGroupValues.GetXGroupValue(xValue.Name);
			if (grp == null) {
				grp = this._UserClass.Config.Items.XGroupValues.GetXGroupValue(xValue.Name);
			}
			try {
				vs = grp.ListValues;
				ts = grp.ListDescriptions;
			} catch (e) {
				console.log(e);
			}
		} else { // ref
			var refs = xValue.Ref.split("/");
			if (xValue.Ref.indexOf('.xml|') > 0) {
				var v = EWAD$GetRef(xValue.Ref);
				vs = v.VS;
				ts = v.TS;
			} else if (xValue.Ref == "EasyWebConfig/XItems/XItem") {
				var xitems = this._UserClass.Config.Items.XItems;
				vs = new Array();
				ts = new Array();
				for (var i = 0; i < xitems.Count(); i += 1) {
					var xitem = xitems.GetXItem(i);
					vs[i] = xitem.Name;
					ts[i] = xitem.Descriptions.GetDescription(EWA.LANG).Info;
				}
				obj.onchange = function() {
					var w = this.ownerDocument.parentWindow ? this.ownerDocument.parentWindow : this.ownerDocument.defaultView;
					w._EWAC_UI.Change(this);
					w._EWAC_UI.ShowHiddenItemParameters();
					w = null;
				};
			} else if (xValue.Ref == "EasyWebConfig/Valids/Valid") {
				var xitems = this._UserClass.Config.Valids.Valids;
				vs = new Array();
				ts = new Array();
				vs.push("");
				ts.push("");
				for (var i = 0; i < xitems.Count(); i += 1) {
					var xitem = xitems.GetItem(i);
					vs.push(xitem.Name);
					ts.push(xitem.Descriptions.GetDescription(EWA.LANG).Info);
				}
			} else if (xValue.Ref == "EasyWebTemplate/Action/{CallType}") {
				var dataTable;
				var refValName = refs[2].trim().toUpperCase();
				var refObj = this.__GROUP[refValName];
				var s1 = refObj.getAttribute("EWA_T_" + refObj.value).split("$$$GDX1231$$$");
				vs = new Array();
				ts = new Array();
				for (var i = 0; i < s1.length; i += 1) {
					vs[i] = s1[i];
					ts[i] = "";
				}
			} else if (xValue.Ref == "EasyWebTemplate/Action/XmlSetData") {
				vs = new Array();
				ts = new Array();
				var tbs = this._UserClass.ActionTables.Tables.GetItem('XmlSetData');
				vs.push("");
				ts.push("");
				for (var i = 0; i < tbs.Rows.length; i++) {
					vs.push(tbs.Rows[i].Fields[0]);
					ts.push("");
				}
			} else if (xValue.Ref == "EasyWebTemplate/PageInfos/PageInfo") {
				var tbs = this._UserClass.PageInfosTablesSet;
				vs = new Array();
				ts = new Array();
				vs[0] = ts[0] = "";
				for (var i = 0; i < tbs.Count(); i += 1) {
					vs[i + 1] = tbs.GetItem(i).Name;
					ts[i + 1] = "";
				}
			} else if (xValue.Ref == "EasyWebTemplate/Action/ActionSet") {
				var dataTable = this._UserClass.ActionTables.Tables.GetItem("ActionSet");
				var typeIndex = dataTable.Columns.GetIndex("Type");
				vs = new Array();
				ts = new Array();
				vs[0] = ts[0] = "";
				for (var i = 0; i < dataTable.Rows.length; i += 1) {
					vs[i + 1] = dataTable.Rows[i].Fields[typeIndex];
					ts[i + 1] = "";
				}
			} else if (xValue.Ref == "EasyWebTemplate/XItems/XItem") {
				var tbs = this._UserClass.ItemsTablesSet;
				vs = new Array();
				ts = new Array();
				vs[0] = ts[0] = "";
				for (var i = 0; i < tbs.Count(); i += 1) {
					var a = tbs.GetItem(i);
					vs[i + 1] = a.Name;
					ts[i + 1] = a.Tables.GetItem("DescriptionSet").SearchValue("Lang=" + EWA.LANG, "Info");
				}
			} else if (xValue.Ref == "EWAD$Connections") { // 数据连接池
				var vals = this.Connections.CreateArray();
				vs = vals[0];
				ts = vals[1];
			} else if (xValue.Ref == "EWAD$Infos") { // 数据连接池
				var vals = this._UserClass.Config.Infos.CreateArray(EWA.LANG);
				vs = vals[0];
				ts = vals[1];
			}
		}
		var isSelected = false;
		for (var i = 0; i < vs.length; i += 1) {
			ts[i] = ts[i] == null ? "" : ts[i].trim();
			vs[i] = vs[i] == null ? "" : vs[i].trim();
			if (vs[i].trim() == "" && ts[i].trim() == "") {
				obj.options[i] = new Option("", "");
			} else {
				obj.options[i] = new Option(vs[i].trim() + " (" + ts[i].trim() + ")", vs[i].trim());
			}
			if (val != null && vs[i].trim() == val.trim()) {
				obj.selectedIndex = i;
				isSelected = true;
			}
			if (xValue.Target != null) {
				var s1 = "";
				if (vs[i].trim() != "") {
					var dataTable = this._UserClass.ActionTables.Tables.GetItem(vs[i].trim());
					var nameIndex = dataTable.Columns.GetIndex("Name");
					for (var i1 = 0; i1 < dataTable.Rows.length; i1 += 1) {
						s1 += "$$$GDX1231$$$" + dataTable.Rows[i1].Fields[nameIndex];
					}
				}
				obj.setAttribute("EWA_T_" + vs[i].trim(), s1);
				obj.setAttribute("EWA_TARGET", xValue.Target);
				obj.onchange = function() {
					var target = this.getAttribute("EWA_TARGET");
					var objs = this.parentNode.parentNode.parentNode.getElementsByTagName("select");
					for (var i = 0; i < objs.length; i += 1) {
						if (objs[i].id.indexOf("$" + target) > 0) {
							var s1 = this.getAttribute("EWA_T_" + this.value).split("$$$GDX1231$$$");
							objs[i].options.length = 0;
							for (var m = 0; m < s1.length; m += 1) {
								objs[i].options[m] = new Option(s1[m]);
							}
						}
					}
				};
			}
		}
		if (isSelected == false) {
			// obj.selectedIndex = grp.Default * 1;
		}
		this.__GROUP["{" + xValue.Name.toUpperCase() + "}"] = obj;
		if (doc == null && obj.onchange == null) {
			obj.onchange = function() {
				var w = this.ownerDocument.parentWindow ? this.ownerDocument.parentWindow : this.ownerDocument.defaultView;
				w._EWAC_UI.Change(this);
				w = null;
			};
		}
		return obj;
	};

	// -----页面事件-------------------------------------
	this.Edit = function(obj, objTarget) {
		var pName = obj.getAttribute("P_NAME"); // XItemParameter
		var vName = obj.getAttribute("V_NAME"); // XItemParameter 的 Value
		var objTarget = objTarget == null ? obj.parentNode.parentNode.cells[2].childNodes[0] : objTarget;
		var xItemParameter = this._GetParameters().GetXItemParameter(pName);
		// 调用命令
		var callCmd = new EWA_Command();
		// 返回命令
		var retCmd = new EWA_Command();
		callCmd.IsRunAuto = true;
		var url = "";
		if (vName == null) { // 集合
			var cName = obj.getAttribute("C_NAME"); // XItemParameter 的子
			// XItemParameter
			var table = this._GetTables().Tables.GetItem(pName);
			if (cName != null) {
				var rIndex = obj.getAttribute("R_INDEX"); // 子集合对应的数据行
				if (rIndex == "undefined") {
					return;
				}
				var colIndex = table.Columns.GetIndex(cName);
				table = table.Rows[rIndex].Fields[colIndex];
			}
			callCmd.CmdArgus[0] = table;
			callCmd.CmdArgus[1] = this;
			url = "./?XMLNAME=ewa/ewa.xml&ITEMNAME=define_edit_set";
			// 返回参数
			retCmd.CmdArgus[0] = this;
			retCmd.CmdArgus[1] = table;
		} else {
			callCmd.CmdArgus[0] = obj;
			callCmd.CmdArgus[1] = objTarget;
			callCmd.CmdArgus[2] = null; // dialog
			var v = xItemParameter.Values.GetValue(vName);

			callCmd.CmdArgus[3] = v.Type.toLowerCase();

			if (callCmd.CmdArgus[3] == 'icon') { // 显示图标
				var showIconTd = $(objTarget).parent().parent().find('td:eq(1)');
				if (showIconTd.find('b').length == 0) {
					showIconTd.append(" <b style='font-family:FontAwesome;'></b>");
				}
				fas.open_icons(objTarget, showIconTd);
				return;
			}

			callCmd.CmdArgus[4] = this;
			retCmd.CmdArgus[0] = objTarget;
			// url = "define_edit_value.htm";
			url = "./?XMLNAME=ewa/ewa.xml&ITEMNAME=define_edit_value&EWA_DEBUG_NO=1";
		}
		callCmd.Cmd = function(a) {
			var w = a[2].GetIframeWindow();
			var doc = w.document;
			if (w.location.href.indexOf("ITEMNAME=define_edit_set") > 0) {
				w.InitSet(a);
			} else {
				w.init(a);
			}
			w = doc = null;
		};
		var op = EWA.UI.Dialog.OpenWindow(url, Math.random(), 810, 220, false, callCmd, retCmd);
		callCmd.CmdArgus[2] = op;
	};
	this.ChangeSet = function(dataTable, valArray) {
		var pName = xItemParameter.Name;
		dataTable.Clear(pName); // 清楚表内容
		for (var i = 0; i < valArray.length; i += 1) {
			dataTable.AddRow;
			for (var m = 0; m < valArray[0].length; m += 1) {
				var vName = xItemParameter.Values.GetValue(m).Name;
				tables.SetValue(pName, vName, i, valArray[i][m]);
			}
		}
	};
	// 资源修改页面内容修改事件
	this.ResChange = function(obj) {
		var table = this._UserClass.ResTables[obj.getAttribute("table_id") * 1];
		var row = table.Rows[obj.getAttribute("row_id") * 1];
		if (row.Fields[obj.getAttribute("field_id") * 1] != obj.value) {
			row.Fields[obj.getAttribute("field_id") * 1] = obj.value;
			this.IsChanged = true;
		}
	};
	// ------------获取参数--------------------
	this._GetConfig = function() { // 获取系统定义
		if (this._CurType == "PAGE") {
			return this._UserClass.Config.Page;
		} else if (this._CurType == "ACTION") {
			return this._UserClass.Config.Action;
		} else if (this._CurType == "ITEMS") {
			return this._UserClass.Config.Items;
		} else if (this._CurType == "MENU") {
			return this._UserClass.Config.Menu;
		} else if (this._CurType == "PAGEINFOS") {
			return this._UserClass.Config.PageInfos;
		} else if (this._CurType == "CHART") {
			return this._UserClass.Config.Chart;
		} else if (this._CurType == "WORKFLOW") {
			return this._UserClass.Config.Workflow;
		}
		// alert(this._CurType + ' not found [this._GetConfig]')

	};
	this._GetXGroupValues = function() {
		var a = this._GetConfig().XGroupValues;
		return a;
	};
	this._GetParameters = function() {
		return this._GetConfig().XItemParameters;
	};
	this._GetTables = function() { // 获取用户定义数据
		if (this._CurType == "PAGE") {
			return this._UserClass.PageTables;
		} else if (this._CurType == "ACTION") {
			return this._UserClass.ActionTables;
		} else if (this._CurType == "ITEMS") {
			return this._UserClass.ItemsTablesSet.GetItem(this._CurItem);
		} else if (this._CurType == "MENU") {
			return this._UserClass.MenuTablesSet.GetItem(this._CurItem);
		} else if (this._CurType == "PAGEINFOS") {
			return this._UserClass.PageInfosTablesSet.GetItem(this._CurItem);
		} else if (this._CurType == "CHART") {
			return this._UserClass.ChartTableSet.GetItem(this._CurItem);
		} else if (this._CurType == "WORKFLOW") {
			return this._UserClass.WorkflowTableSet.GetItem(this._CurItem);
		}
		// alert(this._CurType + ' not found!');
	};
}

var _XML_OBJS = {};
function EWAD$GetRef(ref) {
	var r = ref.split('|');
	var xml = _XML_OBJS[r[0]];
	if (xml == null) {
		var xml = new EWA.C.Xml();
		var url = EWA.CP + "/EWA_DEFINE/cgi-bin/xml/?xmlname=" + r[0] + "&TYPE=CFG_XML"
		xml.LoadXmlFile(url);
		_XML_OBJS[r[0]] = xml;
	}
	var path = ref.split('|')[1];
	var nl = xml.GetElements(path);
	var vs = new Array();
	var ts = new Array();
	for (var i = 0; i < nl.length; i++) {
		if (nl[i].getAttribute('DefinedHidden') == '1') {
			continue;
		}
		var name = nl[i].getAttribute('Name');
		var des = new EWAD$Descriptions();
		des.InitFromXml(xml, nl[i]);
		var text = des.GetInfo(EWA.LANG);
		vs.push(name);
		ts.push(text);
	}
	return {
		VS: vs,
		TS: ts
	};
}