var EWAC_F_OPTIONS = {
	"部门" : {
		Sql : "SELECT DEP_ID, DEP_NAME FROM ADM_DEPT WHERE sup_id=@g_sup_id order by DEP_NAME",
		DisplayField : "DEP_NAME",
		ValueField : "DEP_ID"
	},
	check_by_txt : function(txt) {
		for ( var n in EWAC_F_OPTIONS) {
			if (txt.indexOf(n) >= 0) {
				return EWAC_F_OPTIONS[n];
			}
		}
	}
}
/**
 * 从HTML生成配置文件
 */
var EWAC_F = {
	comments : [],
	create : function(obj) {
		var doc = window.frames[0].frames[0].document;
		if (doc.body.innerText.trim() == '') {
			$Tip("请先粘贴对象");
			var d = $(doc).find('body');
			d.animate({
				'background-color' : 'red'
			}, 500, function() {
				d.animate({
					'background-color' : '#fff'
				}, 100);
			});
			return;
		}

		doc.body.contentEditable = false;
		try {
			if ($(doc).find('input,select').length > 0) {
				this.create_method2(doc);
			} else {
				this.create_method1(doc);
			}
		} catch (e) {
			alert('分析出错:' + e);
			return;
		}
		$(obj).hide();
		$('#butSql').show();
		$('#butXml').show();

		$('select').attr('disabled', null);
	},

	create_method1 : function(doc) {
		this.clear_source(doc);

		map = {};
		g_idx = 0;
		$(doc).find(":contains('{')").each(function() {
			if ($(this).find("*").length == 0) {
				EWAC_F.change_to_element(this)
			}
		});

	},
	create_method2 : function(doc) {
		var cnt = doc.body;
		map = {};
		var css = "background-color:#08c;color:#fff;cursor:pointer";
		var js = "parent.parent.EWAC_F.show_field(this)";

		var mark0 = "<span class='mark' onclick=\"" + js + "\" style='" + css
			+ "' ref_id='@ID'><i style='font-size:12px;color:green'></i>[";
		var c=this;
		$(cnt).find('input').each(function() {
			var id = $(this).attr('name');
			if (id == null || id == '') {
				id = this.id;
			}
			id = c.fix_id(id);
			var id_old = id;
			id = id.toUpperCase();
			if (!map[id]) {
				map[id] = {
					id : id,
					type : this.type,
					values : [],
					txts : [],
					des : c.create_get_des(this),
					dt_type : "c"
				};
			}
			var o = map[id];

			var mark = mark0.replace("@ID", id);
			if (this.type == 'checkbox') {
				mark += "I-CHK";
			} else if (this.type == 'radio') {
				mark += "I-RAD";
			} else if (this.type == 'button') {
				mark += "I-BUT";
			} else {
				mark += "I-TXT";
			}
			mark += "#" + id_old + "]</span>";
			o.values.push(this.value);
			if (o.type == 'checkbox' || o.type == 'radio') {
				var txt = $(this).next().html();
				o.txts.push(txt);
				$(this).next().remove();
			}
			if (o.values.length == 1) {
				$(mark).insertBefore(this);
			}
			$(this).remove();
		});
		$(cnt).find('select').each(function() {
			var id = $(this).attr('name');
			if (id == null || id == '') {
				id = this.id;
			}
			id = c.fix_id(id);
			
			var id_old = id;
			id = id.toUpperCase();

			var values = [];
			var txts = [];
			for (var i = 0; i < this.options.length; i++) {
				var opt = this.options[i];
				values.push(opt.value);
				txts.push(opt.text);
			}
			map[id] = {
				id : id,
				type : "select",
				values : values,
				txts : txts,
				des : c.create_get_des(this)
			};
			var mark = mark0.replace("@ID", id) + "S#" + id_old + "]</span>";
			$(mark).insertBefore(this);
			$(this).remove();
		});
		for ( var n in map) {
			var o = map[n];
			if (o.values.length <= 1) {
				continue;
			}
			var dt_type = 'n';
			for (var i = 0; i < o.values.length; i++) {
				var v = o.values[i];
				if (v == '') {
					continue;
				}
				if (isNaN(v)) {
					dt_type = 'c';
					break;
				}
			}
			o.dt_type = dt_type;
		}
		var sql = c.create_sql(name);
		$('#sql').parent().find("iframe")[0].contentWindow.editor.setValue("");
		$('#sql').parent().find("iframe")[0].contentWindow.setText(sql);
		$('#sql').val(sql);
		//$(obj).hide();
	},
	fix_id : function(id) {
		return id.replace(/[^a-zA-Z0-9_]/ig, '_');
	},
	clear_comments : function(p) {
		for (var i = 0; i < p.childNodes.length; i++) {
			var o = p.childNodes[i];
			if (o.nodeType == 8) {
				this.comments.push(o);
			}
			this.clear_comments(o);
		}
	},
	clear_source : function(doc) {
		this.clear_comments(doc);
		for ( var n in this.comments) {
			var comment = this.comments[n];
			comment.parentNode.removeChild(comment);
		}
		$(doc).find('p').each(function() {
			this.innerHTML = this.innerText.trim();
			this.removeAttribute('class');
			this.removeAttribute('style');
		});
		$(doc).find('td,tr,table').each(function() {
			if (this.tagName == 'TD') {
				var borderL = $(this).css('border-left');
				var borderR = $(this).css('border-right');
				var borderT = $(this).css('border-top');
				var borderB = $(this).css('border-bottom');

				var css = {};
				if (borderL.indexOf('0') != 0) {
					css["border-left"] = borderL;
				}
				if (borderR.indexOf('0') != 0) {
					css["border-right"] = borderR;
				}
				if (borderT.indexOf('0') != 0) {
					css["border-top"] = borderT;
				}
				if (borderB.indexOf('0') != 0) {
					css["border-bottom"] = borderB;
				}

				css.width = Math.round($(this).css('width').replace('px', '') * 1);
				if (this.cellIndex == 0) {
					css.height = Math.round($(this).css('height').replace('px', '') * 1);
				}

				this.removeAttribute('style');
				this.removeAttribute('class');
				this.removeAttribute('width');
				this.removeAttribute('vAlign');

				$(this).css(css);

			} else if (this.tagName == 'TABLE') {
				this.removeAttribute('style');
				this.removeAttribute('class');
				this.removeAttribute('width');
				$(this).attr({
					"cellSpacing" : "0",
					"cellPadding" : "0",
					"align" : "center"
				});
			} else if (this.tagName == 'TR') {
				this.removeAttribute('style');
				this.removeAttribute('class');
				this.removeAttribute('width');
			}
		});
	},
	run_sql : function() {
		if (!this.create_sql()) {
			return;
		}
		var sql = $('#sql').val();
		if (sql == '') {
			$Tip('先干点别的');
			return;
		}

		if (!confirm('你确认要创建么')) {
			return;
		}

		var X = "/ewa/ewa.xml";
		var I = "sql_run";
		var P = "db=globaltravel&EWA_FRAMESET_NO=1&ewa_action=run&ewa_ajax=json&sql=" + sql.toURL();
		var u1 = $U2(X, I, P, true);
		$J(u1, function(rst) {
			if (rst.length == 0) {
				$Tip('ok');
				return;
			}
			var rst1 = rst[0];
			if (rst1.KEY.indexOf('ERR') == 0) {
				alert(rst1.intern);
			} else {
				$Tip('ok');
			}
		});
	},
	change_to_element : function(obj) {
		var txt0 = obj.innerText;
		var txt = obj.innerText.toUpperCase().replace(/ /ig, '');
		var loc0 = txt.indexOf('{');
		var loc1 = txt.indexOf('}');
		if (!(loc0 >= 0 && loc1 > loc0)) {
			return;
		}
		var txt = txt.substring(loc0, loc1 + 1);
		console.log(txt);

		var css = "background-color:#08c;color:#fff;cursor:pointer;font-size:12px";
		var js = "parent.parent.EWAC_F.show_field(this)";

		var id = "A_" + g_idx;
		g_idx++;
		
		o = {
			id : this.get_id(txt) || id,
			type : "",
			values : [],
			txts : [],
			des : this.create_get_des(obj),
			dt_type : "c"
		};
		var mark = "<span class='mark' onclick=\"" + js + "\" style='" + css + "' ref_id='" + o.id
		+ "'><i style='font-size:12px;color:green'></i>[";
		if (txt.indexOf('{SPAN') == 0) {
			mark += "I-SP";
			o.type = 'span';
		} else if (txt.indexOf('{DATETIME') == 0) {
			mark += "I-DT";
			o.type = 'datetime';
		} else if (txt.indexOf('{DATE') == 0) {
			mark += "I-DA";
			o.type = 'datetime';
		} else if (txt.indexOf('{TIME') == 0) {
			mark += "I-TI";
			o.type = 'datetime';
		} else if (txt.indexOf('{SELECT') == 0) {
			mark += "I-SE";
			o.type = 'select';
		} else if (txt.indexOf('{TEXT') == 0) {
			mark += "I-TE";
			o.type = 'textarea';
		} else if (txt.indexOf('{INPUT') == 0 || txt.indexOf('{IPT') == 0) {
			mark += "I-IP";
			o.type = 'text';
		} else if (txt.indexOf('{RADIO') == 0) {
			mark += "I-RA";
			o.type = 'radio';
			this.get_options(o, txt);
		} else if (txt.indexOf('{CHECK') == 0) {
			mark += "I-CH";
			o.type = 'checkbox';
			this.get_options(o, txt);
		} else {
			console.log('unknow:' + txt);
			return;
		}
		map[o.id] = o;
		mark += "#" + o.id + "]</span>";
		obj.innerHTML = obj.innerHTML.replace(txt0,mark);

		if (obj.innerHTML.indexOf('{') > 0 && obj.innerHTML.indexOf('}') > 0) {
			this.change_to_element(obj);
		}
	},
	get_id : function(txt) {
		var txt1 = txt.replace(/ /ig, '');
		var exp = /#[A-Za-z0-9_]+/ig;
		var id = txt.match(exp);
		if (id && id.length > 0) {
			return id[0].trim().replace('#','');
		} else {
			return null;
		}
	},
	get_options : function(o, txt) {
		var loc0 = txt.indexOf("(");
		var loc1 = txt.indexOf(")");
		if (!(loc0 > 0 && loc1 > loc0)) {
			return;
		}
		var s = txt.substring(loc0 + 1, loc1);
		var s2 = s.split(',');
		o.values = s2;
		o.txts = s2;
	},
	create_sql : function() {
		var name = $X('name').value;
		if (name == '') {
			$Tip('请填写名称');
			return false;
		}

		name = name.trim().toUpperCase();
		var ss = [ "CREATE TABLE " + name + "(" + name + "_ID INT IDENTITY(1,1)" ];
		var ss1 = [ "EXEC PR_DES '" + name + "', '" + name + "_ID', '编号';" ];
		for ( var n in map) {
			ss.push("\n\t, " + n.toUpperCase());
			var o = map[n];
			if (o.dt_type == 'n') {
				ss.push(' INT');
			} else if (o.type == 'datetime' || o.type == 'date' || o.type == 'time') {
				ss.push(' DATETIME');
			} else if (o.type == 'textarea') {
				ss.push(' NTEXT');
			} else {
				ss.push(' NVARCHAR(100)');
			}
			ss1.push("EXEC PR_DES '" + name + "', '" + n + "', '" + o.des + "';");
		}
		ss.push("\n\t, " + name + "_STATUS varchar(41)");
		ss.push("\n\t, " + name + "_CDATE datetime");
		ss.push("\n\t, " + name + "_MDATE datetime");
		ss.push("\n\t, ADM_ID INT");
		ss.push("\n\t, SUP_ID INT");

		ss.push("\n\t, PRIMARY KEY(" + name + "_ID)");
		ss.push("\n);");

		ss1.push("EXEC PR_DES '" + name + "', '" + name + "_STATUS', '状态';");
		ss1.push("EXEC PR_DES '" + name + "', '" + name + "_CDATE', '创建时间';");
		ss1.push("EXEC PR_DES '" + name + "', '" + name + "_MDATE', '修改时间';");
		ss1.push("EXEC PR_DES '" + name + "', 'ADM_ID', '创建人';");
		ss1.push("EXEC PR_DES '" + name + "', 'SUP_ID', '商户';");

		var sql = ss.join("") + "\n\n" + ss1.join("\n");

		$('#sql').parent().find("iframe")[0].contentWindow.editor.setValue(""); 
		$('#sql').parent().find("iframe")[0].contentWindow.setText(sql);
		$('#sql').val(sql);
		
		return true;
	},
	create_get_des : function(obj) {
		if (obj.parentNode.tagName == 'TD') {
			var thisTd = obj.parentNode;
			var tr = obj.parentNode.parentNode;
			if (thisTd.cellIndex > 0) {
				return GetInnerText(tr.cells[thisTd.cellIndex - 1]).trim();
			} else {
				return $(tr).prev().text();
			}
		}
		if (obj.parentNode.parentNode.tagName == 'TD') {
			var thisTd = obj.parentNode.parentNode;
			var tr = obj.parentNode.parentNode.parentNode;
			if (thisTd.cellIndex > 0) {
				return GetInnerText(tr.cells[thisTd.cellIndex - 1]).trim();
			} else {
				return $(tr).prev().text();
			}
		}
		if (obj.parentNode.tagName == 'LI') {
			var p1 = obj.parentNode.parentNode;
			if (p1 && p1.tagName == 'UL') {
				var p2 = p1.parentNode;
				if (p2.tagName == 'TD' && p2.cellIndex > 0) {
					var tr = p2.parentNode;
					return GetInnerText(tr.cells[p2.cellIndex - 1]).trim();
				} else if (p2.tagName == 'TD' && p2.cellIndex == 0) {
					var tr = p2.parentNode;
					return $(tr).prev().text();
				}
			}
		}
		return "";
	},
	show_tables : function(key) {
		if (key == '') {
			var obj = $X('tables');
			obj.options.length = 0;

			return;
		}
		var u = window.location + '&EWA_ACTION=tables&EWA_AJAX=json&key=' + key;
		$J(u, EWAC_F.show_tables1);
	},
	show_tables1 : function(tbs) {
		var obj = $X('tables');
		obj.options.length = 0;
		obj.options[0] = new Option('', '');
		for (var i = 2; i < tbs.length; i++) {
			var t = tbs[i];
			obj.options[obj.options.length] = new Option(t.Name, t.Key);
		}
	},
	show_fields : function(key) {
		var win = window.frames[0].frames[0];
		if (key == '') {
			return;
		}

		if (!window.map) {
			return;
		}
		for ( var n in map) {
			$(win.document).find(".mark[ref_id=" + n + "]").css({
				"backgroundColor" : '#08c',
				color : '#fff'
			}).attr('DataField', null).find('i').html("");
		}

		var keys = key.split(';');
		var u = './?xmlname=ewa/ewa.xml&EWA_FRAMESET_NO=1&itemname=fields&cfg=' + keys[1] + '&tablename=' + keys[3]
			+ '&ewa_ajax=json';
		var ajax = new EWA_AjaxClass();

		ajax.Get(u, function() {
			if (!ajax.IsRunEnd()) {
				return;
			}
			var rst = ajax.GetRst();
			eval('win.ffs=' + rst);

			var obj = win.document.getElementById('gdx');
			if (!obj) {
				var obj = win.document.createElement('select');
				obj.onchange = function() {
					if (this.value == '') {
						this.style.display = 'none';
						return;
					}

					win.lastObj.getElementsByTagName('i')[0].innerHTML = this.options[this.selectedIndex].text;
					win.lastObj.style.backgroundColor = 'yellow';
					win.lastObj.style.color = '#000';
					win.lastObj.setAttribute('DataField', this.value);

					this.style.display = 'none';
				}
				obj.id = 'gdx';
			}
			obj.length = 0;
			obj.options[0] = new Option('', '');

			for (var i = 0; i < win.ffs.length; i++) {
				var t = win.ffs[i];
				var txt = t.name + '(' + t.description + ')';
				obj.options[obj.options.length] = new Option(txt, t.name);
				if (map[t.name]) {
					var o1 = $(win.document).find(".mark[ref_id=" + t.name + "]");
					o1.css({
						"backgroundColor" : 'yellow',
						color : '#000'
					}).attr('DataField', t.name);
					o1.find('i').html(txt);
				}
			}
			obj.style.position = 'absolute';
			obj.style.display = 'none';
			obj.size = 5;
			win.document.body.appendChild(obj);
			$Tip("可以匹配蓝色对象和字段, 请点击蓝色对象");
		});
	},
	create_xml : function() {
		var ds = $X('datasource').value;
		var tb = $X('tables').value;

		if (ds == '') {
			alert('请选择数据源');
			return;
		}
		if (tb == '') {
			alert('请选择表');
			return;
		}
		ds = ds.split(';')[1];

		var X = "/ewa/ewa.xml";
		var I = "sql_create";
		var P = "F_XMLNAME=" + F_XMLNAME + "&FrameTag=ListFrame&auto=1&cfg=" + ds + "&tablename=" + tb;
		var u1 = $U2(X, I, P, true);
		window.frames[2].location = u1;
		ajaxxx = new EWA_AjaxClass();
		ajaxxx._CreateWaittingImg();
	},
	xml_create_ok : function(v) {
		$('[id=EWA_AJAX_WAITING]').remove();
		XMLS = v;
		var v1;
		var fn;
		for ( var n in v) {
			if (n.indexOf('.F.NM') > 0) {
				v1 = v[n];
				fn = n;
			}
		}
		xml = new EWA_XmlClass();
		xml.LoadXml(v1);

		var body = $('#create_xml')[0].contentWindow.document.body;
		body.innerHTML = $('#item1 iframe')[0].contentWindow.frames[0].document.body.innerHTML;
		$(body).find('#gdx').remove();

		$(body).find('.mark').each(function() {
			var o = $(this);
			var s;
			if (o.attr('DataField')) {
				s = "<span>{" + o.attr('DataField') + "#ITEM}</span>";
			} else {
				s = "<span>{" + o.attr('ref_id') + "#ITEM}</span>";
			}
			$(s).insertBefore(o);
			o.remove();
		});
		var ss = [ "<form method='post' id='f_@sys_frame_unid' name='f_@sys_frame_unid' onsubmit=\"EWA.F.FOS['@sys_frame_unid'].DoPost(this);return false;\">" ]
		ss.push(body.innerHTML);
		ss.push("<div align=right>{butOk#ITEM} &nbsp; {butClose#ITEM}</div>");
		ss.push("</form>");
		var str = ss.join("\n");

		//var txt1 = style_html(str, 1, '\t', 80);
		var txt1 = str;
		
		$(xml.XmlDoc).find('Page FrameHtml Set').html("<FrameHtml />");
		var fhtmls = $(xml.XmlDoc).find('Page FrameHtml Set FrameHtml');
		for (var i = 0; i < fhtmls.length; i++) {
			xml.SetCData(txt1, fhtmls[i]);
		}

		$(xml.XmlDoc).find('XItem').each(function() {
			var name = $(this).attr('Name');
			if (map[name]) {
				var type = map[name].type;
				$(this).find('Tag Set').attr('Tag', type);
				if (type == 'select' || type == 'checkbox' || type == 'radio') {
					var attr = {};
					if (map[name].values && map[name].values.length > 0) {
						attr.ValueList = map[name].values.join(',');
						attr.DisplayList = map[name].txts.join(',');
					} else {
						var paras = EWAC_F_OPTIONS.check_by_txt(map[name].des);
						if (paras) {
							attr = paras;
						}
					}
					$(this).find('List Set').attr(attr);
				}
			}
		});

		v[fn] = xml.GetXml();
		var msg = [];
		if (window.parent.NewFrame) {
			for ( var n in v) {
				if (v[n]) {
					window.parent.NewFrame(v[n], F_XMLNAME, n);
					msg.push("创建了:" + n);
				}
			}
			$Tip(msg.join("\n"));
		}
	},
	show_field : function(obj) {
		var doc = obj.ownerDocument;
		var select = $(doc).find('select[id=gdx]');
		doc.defaultView.lastObj = obj;
		var DataField = obj.getAttribute('DataField');

		select.value = DataField || "";
		var p = $(obj).position();
		var h = $(obj).height() + 5;
		select.css({
			left : p.left,
			top : p.top + h
		});
		select.show();
	}
};
xml_create_ok = EWAC_F.xml_create_ok;