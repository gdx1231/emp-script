 var EwaCUtils = {};
EwaCUtils.Refs = {};

EwaCUtils.initPanelValues = function(proxy) {
	var xmlNode = proxy.xmlNode;
	var sets = xmlNode.getElementsByTagName('Set');
	for (var i = 0; i < sets.length; i++) {
		var node = sets[i];
		for (var m = 0; m < node.attributes.length; m++) {
			var att = node.attributes[m];
			var uiValue = proxy.getUIValue(node.parentNode.tagName, att.name);
			if (uiValue.CLASS_NAME == 'EwaUIRow') {// set
				uiValue.UISetInfo.xmlNode = node.parentNode;
				uiValue.xmlNode = node.parentNode;

				EwaCUtils.initSetInfo(uiValue.UISetInfo);
				continue;
			} else {
				uiValue.initValue(att.value);
				uiValue.xmlNode = node;
			}
		} 
	}
}
EwaCUtils.initSetInfo = function(ewaUiSetInfo) {
	var nodes = ewaUiSetInfo.xmlNode.getElementsByTagName('Set');
	ewaUiSetInfo.UISetItems = null;
	ewaUiSetInfo.UISetItems = [];
	for (var i = 0; i < nodes.length; i++) {
		var node = nodes[i];
		var item = new EwaUISetItem();
		item.id = "item_" + i;
		item.text = EwaCUtils.getXmlInfo(node);
		item.xmlNode = node;
		ewaUiSetInfo.UISetItems.push(item);
	}
}
EwaCUtils.getXmlInfo=function(node){
	var s=[];
	for(var i=0;i<node.attributes.length;i++){
		var n=node.attributes[i];
		s.push(n.name);
		s.push("=");
		s.push(n.value);
		s.push(";")
	}
	return s.join("");
}

EwaCUtils.setItemChange = function(ewaUISetInfo) {
	var uiSetItem=ewaUISetInfo.UISet.getItem();
	var node=	uiSetItem.xmlNode;
	for (var m = 0; m < node.attributes.length; m++) {
		var att = node.attributes[m];
		var uiValue = ewaUISetInfo.UIGroup.UIRowsTable[att.name].UIValue;
		if (uiValue.CLASS_NAME == 'EwaUIRow') {// set
			uiValue.UISetInfo.xmlNode = node.parentNode;
			uiValue.xmlNode = node.parentNode;

			EwaCUtils.initSetInfo(uiValue.UISetInfo);
			continue;
		} else {
			uiValue.initValue(att.value);
			uiValue.xmlNode = node;
		}
	}
}

EwaCUtils.initSetValue = function(ewaUISet) {
	var item = ewaUISet.getItem();
	var xmlNode = item.xmlNode;

	for (var m = 0; m < node.attributes.length; m++) {
		var att = node.attributes[m];
		var uiValue = ewaUISet.UIGroup.UIRowsTable[att.name].UIValue;
		uiValue.initValue(att.value);
		uiValue.xmlNode = node;
	}
}

EwaCUtils.UISET = new EwaUISet();
/**
 * 调用配置文件的XML
 * 
 * @param {String}
 *            xmlName 文件名
 * @return {EWA_XmlClass}
 */
EwaCUtils.loadCfgXml = function(xmlName) {
	var url = "XMLNAME=" + xmlName + "&TYPE=CFG_XML";
	return EwaCUtils.loadXml(url);
}

/**
 * 调用XML
 * 
 * @param {String}
 *            url 地址
 * @return {EWA_XmlClass}
 */
EwaCUtils.loadXml = function(url) {
	EWA.CP = '/EmpScriptV2/';
	var oXml = new EWA.C.Xml();
	oXml.LoadXmlFile(EWA.CP + '/EWA_DEFINE/cgi-bin/xml/?' + url);
	return oXml;
}

EwaCUtils.loadDescription = function(xmlClass, node) {
	var info = {
		Lang : EWA.LANG,
		Info : "",
		Memo : ""
	};
	var nodes = xmlClass.GetElements('DescriptionSet', node);
	if (nodes.length == 0) {
		return info;
	}
	var nodeSets = xmlClass.GetElements('Set', nodes[0]);
	if (nodeSets.length == 0)
		return info;

	var node1 = nodeSets[0];
	for (var i = 0; i < nodeSets.length; i++) {
		var node = nodeSets[i];
		var lang = xmlClass.GetElementAttribute(node, "Lang");
		if (lang == EWA.LANG) {
			node1 = node;
			break;
		}
	}

	info.Info = xmlClass.GetElementAttribute(node1, "Info");
	info.Memo = xmlClass.GetElementAttribute(node1, "Memo");
	if (info.Memo == null || info.Memo == "") {
		info.Memo = xmlClass.GetElementText(node1);
	}
	return info;
}
function EwaCPanel() {
	this.cfgXml = null;
	this.CConfig = null;
	this.xmlNode = null;
	this.text = null;
	this.description = null;
	this.nodeXItems = null;
	this.nodeXGroups = null;
	this.nodeXParameters = null;
	this.UIPanel = new EwaUIPanel();
	this.UIListValsTable = {};
	this.UIGroups = {};

	/**
	 * 
	 * @param {EwaCConfig}
	 *            ewaCConfig
	 * @param {}
	 *            xmlNode
	 */
	this.init = function(ewaCConfig, xmlNode) {
		this.cfgXml = ewaCConfig.cfgXml;
		this.CConfig = ewaCConfig;
		this.xmlNode = xmlNode;
		this.text = xmlNode.tagName;
		this.description = EwaCUtils.loadDescription(this.cfgXml, xmlNode).Info;
		if (this.text == 'Items') {
			this.nodeXItems = this.getElemets('XItems/XItem');
		}
		this.nodeXGroups = this.getElemets('XGroupValues/XGroupValue');
		this.nodeXParameters = this
				.getElemets('XItemParameters/XItemParameter');

		this._initXGroup();
		this._initUIGroups();

	};

	this._initUIGroups = function() {
		var grps = this.xmlNode.getElementsByTagName('UIGroups');
		for (var i = 0; i < grps.length; i++) {
			this._initUIGroup(grps[i]);
		}
	}
	this._initUIGroup = function(node) {
		var tag = node.parentNode.tagName;
		var nodes = node.getElementsByTagName('UIGroup');
		this.UIGroups[tag] = {};
		for (var i = 0; i < nodes.length; i++) {
			var n = nodes[i];
			var name = n.getAttribute('Name');
			var des = EwaCUtils.loadDescription(this.cfgXml, n).Info;
			this.UIGroups[tag][name] = des;
		}
	}

	this._initXGroup = function() {
		for (var i = 0; i < this.nodeXGroups.length; i++) {
			var n = this.nodeXGroups[i];
			var name = this.cfgXml.GetElementAttribute(n, 'Name');
			var vs = this.cfgXml.GetElementAttribute(n, 'Values');
			var ds = EwaCUtils.loadDescription(this.cfgXml, n).Info;
			var vss = vs.split(',');
			var dss = ds.split(',');
			var o = this.UIListValsTable[name.trim().toUpperCase()] = [];
			for (var m = 0; m < vss.length; m++) {
				var list = new EwaUIListVal();
				list.value = vss[m].trim();
				list.text = list.value;
				var t = dss[m].trim();
				if (!(t == "" || t == null)) {
					list.text += " (" + t + ")";
				}
				o.push(list)
			}
		}
	}
	this.getElemets = function(path, node) {
		var node1;
		if (node == null) {
			node1 = this.xmlNode;
		} else {
			node1 = node;
		}
		return this.cfgXml.GetElements(path, node1);
	};

	this.createPanel = function() {
		var nodes = this.nodeXParameters;
		var uiGroups = this.UIGroups["XItemParameters"];
		var uiGrpTable = {};
		for (var a in uiGroups) {
			var grp = new EwaUIGroup();
			grp.text = uiGroups[a];
			grp.id = a;
			this.UIPanel.addGroup(grp);
			uiGrpTable[a] = grp;
		}

		for (var i = 0; i < nodes.length; i++) {
			var node = nodes[i];
			var nodeSets = this.getElemets('Values/Value', node);
			// 是否集合
			var uiGrpName = this.cfgXml.GetElementAttribute(node, "UIGroup");
			var grp = uiGrpTable[uiGrpName];

			var row = this.createRow(node);
			grp.addRow(row);
		}
	};

	this.createRow = function(node) {
		var row = new EwaUIRow();
		row.id = this.cfgXml.GetElementAttribute(node, "Name");

		var nodeSets = this.getElemets('Values/Value', node);
		var isSet = this.cfgXml.GetElementAttribute(node, "IsSet");

		row.text = this.cfgXml.GetElementAttribute(node, "Name");
		row.title = EwaCUtils.loadDescription(this.cfgXml, node).Info;

		row.setType(isSet == "1" ? "SET" : nodeSets.length > 1
				? "MULTI"
				: "SINGLE");

		if (row.type != "SINGLE") { // 多值 , SET （集合）
			if(row.type=='MULTI'){
			}
			for (var i = 0; i < nodeSets.length; i++) {
				var rowChild = new EwaUIRow();
				rowChild.text = this.cfgXml.GetElementAttribute(nodeSets[i],
						"Name");
				rowChild.id = rowChild.text;
				rowChild.title = EwaCUtils.loadDescription(this.cfgXml,
						nodeSets[i]).Info;
				var v = this.createValue(nodeSets[i]);
				rowChild.setUIValue(v);
				row.addRow(rowChild);
			}
			if (row.type == "SET") { // set条目修改执行的脚本
				var info = row.UISetInfo;
				info.onLoad = function() {
					EwaCUtils.initSetItems(this);
				}
				info.UISet = EwaCUtils.UISET;
				info.onItemChange = EwaCUtils.setItemChange;
			}
		} else {
			row.text = this.cfgXml.GetElementAttribute(nodeSets[0], "Name");
			row.title = EwaCUtils.loadDescription(this.cfgXml, nodeSets[0]).Info;
			var val = this.createValue(nodeSets[0]);
			row.setUIValue(val);
		}
		return row;
	}

	this.createValue = function(node) {
		var v = new EwaUIValue();
		var t = this.cfgXml.GetElementAttribute(node, "Type");
		if (t == 'group') {
			var name = node.getAttribute('Name').toUpperCase().trim()
			v.listValues = this.UIListValsTable[name];
			v.tagName = 'SELECT';
		} else if (t == 'ref') {
			v.listValues = this.getRef(node);
			v.tagName = 'SELECT';
		}
		v.id = this.cfgXml.GetElementAttribute(node, "Name");
		return v;
	}

	this.getRef = function(node) {
		var ref = node.getAttribute('Ref');
		var nodes = this.getNodes(ref);
		listValues = [];
		if (nodes == null) {
			return listValues;
		}
		for (var i = 0; i < nodes.length; i++) {
			var node = nodes[i];
			var v = new EwaUIListVal();
			v.value = node.getAttribute('Name');
			var t = EwaCUtils.loadDescription(this.cfgXml, node).Info;
			v.text = v.value;
			if (t.trim() != "") {
				v.text += " (" + t + ")";
			}
			listValues.push(v);
		}
		return listValues;
	}

	this.getNodes = function(ref) {
		if (ref.indexOf('|') > 0) {
			var s = ref.split('|');
			if (s[0] == 'EwaGobal.xml') {
				return this.CConfig.globalXml.GetElements(s[1]);
			}
		} else if (ref.indexOf('EWAD$') == 0) {
			return this.CConfig.cnnXml.GetElements('root/database');
		} else {
			var s1 = ref.split('/');
			if (s1[0] == 'EasyWebConfig') {
				return this.CConfig.cfgXml.GetElements(ref);
			} else if (s1[0] == 'EasyWebTemplate') {
				if (ref.indexOf('{') > 0) {
					return null;
				} else {
					return this.CConfig.itemXml.GetElements(ref);
				}
			}
		}
	}
}

function EwaCConfig(className) {
	this.itemXml = null;
	this.cfgXml = EwaCUtils.loadCfgXml('EwaConfig.xml');
	this.cnnXml = EwaCUtils.loadCfgXml('EwaConnections.xml');
	this.globalXml = EwaCUtils.loadCfgXml('EwaGlobal.xml');

	this.UIPanelTable = {};
	this.UIPanels = new EwaUIPanels();
	this.UIPanels.className = className + '.UIPanels';

	this.initItemsParas = function() {
		var nodeMain = this.cfgXml.GetElements('EasyWebConfig')[0];
		for (var i = 0; i < nodeMain.childNodes.length; i++) {
			var node = nodeMain.childNodes[i];
			if (node.nodeType != 1 || node.tagName == 'Frames') {
				continue;
			}
			if (node.tagName != 'Items')
				continue;

			var p = new EwaCPanel();
			p.init(this, node);
			this.UIPanelTable[p.text] = p;
			p.createPanel();
		}
		this.loadXItems();
	};

	this.loadScriptItem = function(xmlName, itemName) {
		var u = 'XMLNAME=' + xmlName + '&ITEMNAME=' + itemName;
		this.itemXml = EwaCUtils.loadXml(u);
	}

	this.loadXItems = function() {
		var ewaCPanel = this.UIPanelTable["Items"];
		var nodes = this.itemXml.GetElements('EasyWebTemplate/XItems/XItem');
		var json = new EWA_JSONXmlClass();
		for (var i = 0; i < nodes.length; i++) {
			var node = nodes[i];
			var sel = new EwaUIListVal();
			sel.text = node.getAttribute('Name');
			// EwaCUtils.initPanelValues
			var proxy = new EwaUIPanelProxy();
			proxy.UIListVal = sel;
			proxy.UIPanel = ewaCPanel.UIPanel;
			proxy.xmlNode = node;
			proxy.initValuesExp = 'EwaCUtils.initPanelValues(this)';
			// alert(json.toJSON(node))
			this.UIPanels.addProxy(proxy);
		}
	}
}
var cfg;
function main() {
	cfg = new EwaCConfig('cfg');
	cfg.loadScriptItem('test|aaa.xml', 'CRM_COMPANY.ListFrame.ViewAndModify');
	cfg.initItemsParas();
	document.body.appendChild(cfg.UIPanels.getDomObject());
	// for (var a in cfg.UIPanelTable) {
	// var obj = cfg.UIPanelTable[a];
	// document.body.appendChild(obj.UIPanel.getDomObject());
	// break;
	// }
}