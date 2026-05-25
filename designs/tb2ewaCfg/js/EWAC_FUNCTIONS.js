var EWAC_FUNCTIONS = {
	setAction : function(v1) {
		var js = this.jssHide["loadActionJson"].trimEx();
		var js = js.replace(/\{NAME\}/ig, v1);
		this.setJs(js);
	},
	init : function() {
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
		var u = path + "EWAC_FUNCTIONS.xml";

		$J2(u, function(rst) {
			var x = new EWA_XmlClass();
			x.LoadXml(rst);
			EWAC_FUNCTIONS._xml = x;
			EWAC_FUNCTIONS.loadFunctions();
		});
	},
	loadFunctions : function() {
		var doc = EWAC_FUNCTIONS._xml.XmlDoc;
		this.jss = {};
		this.jssHide = {};
		this.jssRequireName = {};
		this.dess = {};
		var c = this;
		$(doc).find('func').each(function() {
			var name = $(this).attr('name');
			var js = $(this).text();
			var hide = $(this).attr('hide');

			if (hide) {
				c.jssHide[name] = js;
			} else {
				c.jss[name] = js;
			}

			// 需要提供名称的脚本
			var nameRequire = $(this).attr('nameRequire');
			if (nameRequire) {
				c.jssRequireName[name] = true;
			}

			c.dess[name] = $(this).attr('des');
		});
	},
	show : function(a) {
		var tr = a.parentNode.parentNode;
		if ($(a).attr('show')) {
			$(tr).next().hide();
			$(a).attr('show', null);
		} else {
			$(this.container).find('a[show=yes]').each(function() {
				if (a != this) {
					$(this.parentNode.parentNode).next().hide();
					$(this).attr('show', null);
				}
			});
			$(tr).next().show();
			$(a).attr('show', 'yes');
		}
	},
	useIt : function(a) {
		var td = a.parentNode;
		var v = $(td).find('pre')[0].innerText;
		var id = $(td.parentNode).prev().attr('id');
		if (this.jssRequireName[id]) {
			var name = window.prompt('请输入方法的名称');
			if (name == null) {
				$Tip('您没有输入');
				return;
			}
			v = v.replace(/\{NAME\}/ig, name);
		}
		this.setJs(v);
		this.dia.Close();

	},
	setJs : function(v) {
		console.log('overwrite EWAC_FUNCTIONS.setJs(v)');
		console.log(v);
	},
	showFunctions : function() {
		if (this.dia) {
			this.dia.Show();
			return;
		}
		var div = document.createElement('div');
		var tb = document.createElement('table');
		this.container = div;

		$(div).css({
			width : 800,
			height : 400,
			overflow : 'auto'
		});
		$(tb).css({
			width : '100%'
		});
		for ( var n in this.jss) {
			var tr = tb.insertRow(-1);
			tr.id = n;
			var td0 = tr.insertCell(-1);
			td0.style.width='150px';
			var td1 = tr.insertCell(-1);
			td0.innerHTML = "<a href='javascript:void(0)' onclick='EWAC_FUNCTIONS.show(this)'></a>";
			$(td0).find('a').html(n);
			td1.innerHTML = this.dess[n] || '';

			var tr1 = tb.insertRow(-1);
			tr1.id = 'code.' + n;
			tr1.style.display = 'none';

			var td2 = tr1.insertCell(-1);
			td2.colSpan = 2;
			td2.innerHTML = "<pre></pre>";
			td2.style.background = '#333';
			td2.style.color = '#f1f1f1';
			td2.style.position = 'relative';

			$(td2).find('pre')[0].innerText = this.jss[n].trimEx();
			$(td2)
				.append(
					'<a href="javascript:void(0)" onclick="EWAC_FUNCTIONS.useIt(this)" style="position:absolute;right:5px;top:5px;padding:4px 10px;background:green;color:#fff">use it</a>');
		}
		div.appendChild(tb);
		document.body.appendChild(div);

		this.dia = new $DialogHtml('', '代码片段', 800, 400);
		this.dia.IsCloseAsHidden = true;
		this.dia.SetObject(div);
		this.dia.MoveCenter();
	}
};
EWAC_FUNCTIONS.init();
