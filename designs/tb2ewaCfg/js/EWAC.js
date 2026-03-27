function EWAC_WfCreate() {
	this.Name = "_";
	this.Cfg = eval('new EWAD$Class()');
	this.Cfg.Init();
	this._ElementsTemplate = {
		I: "<input map='input' type='text' id='[id]' name='[id]' class='EWA_INPUT' />", // input
		D: "<input map='datetime' type='text' id='[id]' name='[id]' size='8' maxlength='8' class='EWA_INPUT' />", // date
		T: "<textarea map='textarea' class='EWA_TEXTAREA' id='[id]' name='[id]'></textarea>", // text
		O: "<select map='select' class='EWA_SELECT' id='[id]' name='[id]'></select>", // select
		A: "<span map='span' class='IX_SPAN' id='[id]' name='[id]' style='background-color:#ddd'>[AUTO]</span>", // select
		H: "<div map='dHtml' class='ewa_wf_h'  id='[id]' name='[id]'></div>", // html
		F: "<input map='file' type='file' class='EWA_INPUT'  id='[id]' name='[id]'/>", // file
		S: "<div map='user' class='ewa_wf_s' style='background-color:#ddd;width:100%;height:100%'"
			+ " id='[id]' name='[id]'>[SIGN]</div>" // sign 签名
	};
	this._Tmp = '<XItem Name="[N]"><Tag><Set Tag="[M]" IsLFEdit="0" />'
		+ '</Tag><Name><Set Name="[N]" /></Name><DescriptionSet><Set Lang="zhcn" Info="[D]"  /></DescriptionSet><IsMustInput><Set IsMustInput="1" /></IsMustInput><DataItem><Set DataType="String" DataField="[N]" IsEncrypt="" Valid="" Format="" /></DataItem></XItem>';

	this._Elements;
	this.create = function() {
		var x1 = EWAC_CFG_XML.replace("[NAME]", this.Name);
		x = new EWA.C.Xml();
		var x1 = EWAC_CFG_XML.replace("[NAME]", this.Name);
		var s = this._getElements();
		x1 = x1.replace('<XItems />', '<XItems>' + s + '</XItems>');
		x.LoadXml(x1);
		alert(x.GetXml());

	};
	this._getElements = function() {
		var objs = document.getElementsByTagName('kk');
		this._Elements = [];
		var ss = [];
		for (var i = 0; i < objs.length; i++) {
			var o = objs[i].nextSibling;
			var id = this._createRandomId();
			o.id = id;
			var s = this._createItem(o);
			ss.push(s);
		}
		return ss.join('\r\n');
	};
	this._createItem = function(obj) {
		var s = this._Tmp;
		s = s.replace(/\[N\]/g, obj.id);
		s = s.replace(/\[M\]/g, obj.getAttribute("map"));
		s = s.replace(/\[D\]/g, this._getDes(obj));
		return s;
	};
	this._createItmPara = function(tag, val) {

	};
	this._getDes = function(obj) {
		var op = obj.parentNode;
		var des = '未定义';
		var inc = 0;
		while (op.tagName.toUpperCase() != 'TD') {
			op = op.parentNode;

			if (op.tagName.toUpperCase() == 'BODY') {
				return des;
			}
			inc++;
			if (inc > 10) {
				return des;
			}
		}
		if (op.previousSibling != null) {
			return GetInnerText(op.previousSibling).trimEx();
		} else {
			return des;
		}
	};
	this.analysis = function(sourceHtml) {
		this._Elements = [];
		for (var n in this._ElementsTemplate) {
			var txt = '<kk />' + this._ElementsTemplate[n];
			var exp = eval('/\\[' + n + '\\]/g')
			sourceHtml = sourceHtml.replace(exp, txt);
		}
		return sourceHtml;
	};
	this._createRandomId = function() {
		var r = Math.random();
		var d = new Date();
		var id = d.getTime() + "" + r + "";
		return id.replace('.', "G");
	};
}

var AJAX;
var SAVE_XML_NEW = false;
var SAVE_XML_ITEM;
var SAVE_XML_NAME;
var EWAC = {};

EWAC.SaveXml = function(xmlName, itemName, xml, isNew) {
	if (!isNew) {
		SAVE_XML_NEW = isNew;
		SAVE_XML_ITEM = itemName;
		SAVE_XML_NAME = xmlName;
	}
	var AJAX = new EWA.C.Ajax(false); // 不使用同步，因为未来不支持了

	var url = EWA.CP + "/EWA_DEFINE/cgi-bin/xml/?TYPE=SAVE&XMLNAME=" + xmlName + "&ITEMNAME=" + itemName;
	AJAX.AddParameter("XML", xml);
	var ok = false;
	$Tip("保存中...", function() {
		return ok;
	});
	AJAX.PostNew(url, function() {
		if (AJAX._Http.readyState != 4) {
			return;
		}
		ok = true;
		AJAX.HiddenWaitting();
		if (AJAX._Http.status == 200) {
			var ret = AJAX._Http.responseText;
			if (isNew) {
				var key = xmlName + "*" + itemName;
				var s1 = itemName.split('.');
				if (s1.length == 3) {
					itemName = s1[0] + ".<font color=red><b>" + s1[1] + "</b></font>.<font color=darkred><b>" + s1[2]
						+ "</b></font>";
				}
				if (window.frames[0] && window.frames[0].frames[0] && window.frames[0].frames[0].NewFrameToTree) {
					// old method
					window.frames[0].frames[0].NewFrameToTree(key, itemName);
					window.frames[0].frames[0].link(key);
				} else { // dark method
					window.NewFrameToTree(key, itemName);
					window.link(key);
				}
			} else {
				if (ret.indexOf("alert('ok')") >= 0) {
					// $Tip("保存成功");
				} else {
					eval(ret);
				}
			}
		} else {
			alert("ERROR:\r\n" + AJAX._Http.statusText);
		}
	});
};

EWAC.AddItemToTree = function() {
	var key = SAVE_XML_NAME + "*" + SAVE_XML_ITEM;
	var s1 = SAVE_XML_ITEM.split('.');
	var itemName = SAVE_XML_ITEM;
	if (s1.length == 3) {
		itemName = s1[0] + ".<font color=red><b>" + s1[1] + "</b></font>.<font color=darkred><b>" + s1[2] + "</b></font>";
	}

	window.frames[0].frames[0].NewFrameToTree(key, itemName);
	window.frames[0].frames[0].link(key);
};

EWAC.InitKey = function() {
	if (window.stopKeyMaster) {
		return;
	}
	if (!window.key || !(key instanceof Function)) {
		setTimeout(EWAC.InitKey, 100);
		return;
	}
	var ref = location.href.toLowerCase();
	// console.log(ref);
	key.filter = function() {
		// 在任何元素上都执行
		return true;
	}
	if (ref.indexOf("ewa.xml&ITEMNAME=index".toLowerCase()) > 0) {
		key('⌘+s,ctrl+s', function(event) {
			console.log("save");
			try {
				saveXml();
			} catch (e) {
				console.log(e);
			}
			return false; // prevent default && stop propagation
		});
	} else if (ref.indexOf("ewa.xml&ITEMNAME=define_right".toLowerCase()) > 0) {
		key('⌘+s,ctrl+s', function(event) {
			console.log("save - define_right");
			try {
				top.saveXml();
			} catch (e) {
				console.log(e);
			}
			return false; // prevent default && stop propagation
		});
	} else if (ref.indexOf("ewa.xml&ITEMNAME=define_code".toLowerCase()) > 0) {
		key('⌘+s,ctrl+s', function(event) {
			console.log("save- define_code");
			try {
				save();
				top.saveXml();
			} catch (e) {
				console.log(e);
			}
			return false; // prevent default && stop propagation
		});
	} else if (ref.indexOf("ewa.xml&ITEMNAME=define_edit_value".toLowerCase()) > 0) {
		key('⌘+s,ctrl+s', function(event) {
			console.log("save- define_edit_value");
			try {
				parent.save();
				top.saveXml();
			} catch (e) {
				console.log(e);
			}
			return false; // prevent default && stop propagation
		});
	} else if (ref.indexOf("ewa.xml&itemname=define".toLowerCase()) > 0) {
		key('⌘+s,ctrl+s', function(event) {
			console.log("save- define");
			try {
				if (parent.save) { parent.save() };
				top.saveXml();
			} catch (e) {
				console.log(e);
			}
			return false; // prevent default && stop propagation
		});
	} else if (ref.indexOf("CodeMirror/index.html".toLowerCase()) > 0) {
		key('⌘+s,ctrl+s', function(event) {
			console.log("save- CodeMirror");
			try {
				parent.parent.save();
				top.saveXml();
			} catch (e) {
				console.log(e);
			}
			return false; // prevent default && stop propagation
		});
	} else {
		// console.log('bind ⌘+s,ctrl+s')
		key('⌘+s,ctrl+s', function(event) {
			console.log("save2");
			try {
				top.saveXml();
			} catch (e) {
				console.log(e);
			}
			return false; // prevent default && stop propagation
		});
	}
};
EWAC.Theme = {
	get_theme: function() {
		if (window.EWA_UrlClass) {
			var u1 = new EWA_UrlClass();
			var t = u1.GetParameter("theme");
			if (t) {
				if (t == 'dark') {
					return dark;
				} else if (t == 'light') {
					return 'light';
				}
			}
		}

		var theme = window.localStorage['ewa_define_theme'];
		if (theme == 'dark') {
			return 'dark';
		} else if (theme == 'light') {
			return 'light';
		}
		return 'dark';
	},
	change_theme: function(theme) {
		document.body.className = theme;
		window.localStorage['ewa_define_theme'] = theme;

		this.change_theme_frames(window, theme);

		setTimeout(function() {
			EWAC.Theme.mark_menu(theme);
		}, 103);
	},
	change_theme_frames: function(w, theme) {
		for (var i = 0; i < w.frames.length; i++) {
			w.frames[i].document.body.className = theme;
			this.change_theme_frames(w.frames[i], theme);
		}
	},
	mark_menu: function(theme) {
		try {
			$('.ewa_menu_m[ewa_cmd*="change_theme"]').each(function() {
				var cmd = $(this).attr('ewa_cmd');
				if (cmd.indexOf(theme) > 0) {
					$(this).find('.ewa_menu_m0').addClass('fa fa-check');
				} else {
					$(this).find('.ewa_menu_m0').removeClass('fa fa-check');
				}
			});
		} catch (e) {

		}
	}
};
(function() {
	if (!window.stopKeyMaster) {
		var js = document.scripts;
		js = js[js.length - 1].src;
		if (!js) {
			return;
		}
		var js2 = js.split('://');
		var js3 = js2[1];
		while (js3.indexOf('//') >= 0) {
			js3 = js3.replace('//', '/');
		}
		js = js2[0] + '://' + js3;
		var loc = js.lastIndexOf('/');
		var path = js.substring(0, loc + 1);
		var u = path + "../../third-party/keymaster-master/keymaster.js";
		var script = document.createElement("script");
		script.id = "keymaster.js"
		script.src = u;
		document.getElementsByTagName('head')[0].appendChild(script);
		setTimeout(EWAC.InitKey, 100);
	}
	var t = EWAC.Theme.get_theme();
	EWAC.Theme.change_theme(t);
})();