/**
 * 创建ewa_conf.xml
 */
function chang_database_strand(select) {
	if (select.value == 'user') {
		$(select).parent().parent().next().find('table:eq(0)').show();
	} else {
		$(select).parent().parent().next().find('table:eq(0)').hide();
	}
}
function import_ewa_conf_xml() {
	var html = "<textarea id='import_xml' style='height: 260px;'></textarea><input onclick='import_ewa_conf_xml1()' type=button value='ok'>";
	aaa = $DialogHtml(html, "aaa", 600, 300);
}
function import_ewa_conf_xml_local() {
	if (window.location.href.indexOf('file') >= 0) {
		$Tip("不能在file模式执行");
		return;
	}
	var u;
	if (parent == window) {
		u = EWA.CP + '/EWA_DEFINE/cgi-bin/xml/?type=EWA_CONF';
	} else {
		var u1 = new EWA_UrlClass();
		u1.SetUrl(parent.location.href);

		u = u1._Root + '../../EWA_DEFINE/cgi-bin/xml/?type=EWA_CONF';
	}
	$J(u, function(rst) {
		if (rst.RST) {
			import_ewa_conf_xml2(rst.XML);
		} else {
			alert(rst.ERR);
		}
	});
}
function import_ewa_conf_xml1() {
	var s = $('#import_xml').val();
	aaa.Close();
	delete aaa;
	import_ewa_conf_xml2(s);
}
function import_ewa_conf_xml2(xmlStr) {
	var xml = new EWA_XmlClass();
	xml.LoadXml(xmlStr);
	var cert = xml.XmlDoc.getElementsByTagName("cert");
	if (cert.length > 0) {
		cert = cert[0];
		for (var i = 0; i < cert.childNodes.length; i++) {
			var p = cert.childNodes[i];
			if (p.nodeType == 1) {
				var id = p.nodeName;
				var v = p.textContent;
				$('#cert [id="' + id + '"]').val(v);
			}
		}
	}

	var paths = xml.XmlDoc.getElementsByTagName("path");
	if (paths.length > 0) {
		for (var i = 0; i < paths.length; i++) {
			var p = paths[i];
			var name = p.getAttribute('Name');
			var value = p.getAttribute('Value');

			$('#paths input[id="' + name + '"]').val(value);
		}
	}

	var debug = xml.XmlDoc.getElementsByTagName("debug");
	if (debug.length > 0) {
		var d = debug[0];
		for (var i = 0; i < d.attributes.length; i++) {
			var a = d.attributes[i];
			var id = a.nodeName;
			var v = a.nodeValue;
			$('#debug input[id="' + id + '"]').val(v);
		}
	}

	var des = xml.XmlDoc.getElementsByTagName("des");
	if (des.length > 0) {
		var d = des[0];
		for (var i = 0; i < d.attributes.length; i++) {
			var a = d.attributes[i];
			var id = a.nodeName;
			var v = a.nodeValue;

			$('#des input[id="' + id + '"]').val(v);
		}
	}

	var smtp = xml.XmlDoc.getElementsByTagName("smtp");
	if (smtp.length > 0) {
		var d = smtp[0];
		for (var i = 0; i < d.attributes.length; i++) {
			var a = d.attributes[i];
			var id = a.nodeName;
			var v = a.nodeValue;
			$('#smtp input[id="' + id + '"]').val(v);
		}
	}

	var validDomains = xml.XmlDoc.getElementsByTagName("validDomains");
	if (validDomains.length > 0) {
		var d = validDomains[0];
		for (var i = 0; i < d.attributes.length; i++) {
			var a = d.attributes[i];
			var id = a.nodeName;
			var v = a.nodeValue;
			$('#validDomains input[id="' + id + '"]').val(v);
		}
	}

	var rvs = xml.XmlDoc.getElementsByTagName("rv");
	for (var i = 0; i < rvs.length; i++) {
		if (i > 0) {
			add_paras('rv');
		}

		var d = rvs[i];
		for (var ia = 0; ia < d.attributes.length; ia++) {
			var a = d.attributes[ia];
			var id = a.nodeName;
			var v = a.nodeValue;

			$('table[id=rv]:last input[id="' + id + '"]').val(v);
		}
	}

	var remote_syncs = xml.XmlDoc.getElementsByTagName("remote_syncs");
	if (remote_syncs.length > 0) {
		var d = remote_syncs[0];
		for (var i = 0; i < d.attributes.length; i++) {
			var a = d.attributes[i];
			var id = a.nodeName;
			var v = a.nodeValue;
			console.log(id, v);
			$('#remote_syncs input[id="' + id + '"]').val(v);
		}
	}
	var remote_syncs = xml.XmlDoc.getElementsByTagName("remote_sync");
	for (var i = 0; i < remote_syncs.length; i++) {
		if (i > 0) {
			add_paras('remote_sync');
		}

		var d = remote_syncs[i];
		for (var ia = 0; ia < d.attributes.length; ia++) {
			var a = d.attributes[ia];
			var id = a.nodeName;
			var v = a.nodeValue;

			$('table[id=remote_sync]:last input[id="' + id + '"]').val(v);
		}
	}

	var databases = xml.XmlDoc.getElementsByTagName("database");
	for (var i = 0; i < databases.length; i++) {
		if (i > 0) {
			add_paras('database');
		}

		var d = databases[i];
		for (var ia = 0; ia < d.attributes.length; ia++) {
			var a = d.attributes[ia];
			var id = a.nodeName;
			var v = a.nodeValue;

			$('table[id=database]:last *[id="' + id + '"]').val(v);
		}

		var pool = d.getElementsByTagName('pool');
		var pool_tb = $('table[id=database]:last table#pool');
		var standrad = $('table[id=database]:last select[id="Standard"]');
		if (pool.length == 0) {
			standrad.val('standard');
			pool_tb.hide();
		} else {
			standrad.val('user');
			pool_tb.show();

			var d1 = pool[0];
			for (var ia = 0; ia < d1.attributes.length; ia++) {
				var a = d1.attributes[ia];
				var id = a.nodeName;
				var v = a.nodeValue;
				pool_tb.find('[id="' + id + '"]').val(v);
			}
		}

	}

	var Admins = xml.XmlDoc.getElementsByTagName("Admin");
	for (var i = 0; i < Admins.length; i++) {
		if (i > 0) {
			add_paras('Admin');
		}

		var d = Admins[i];
		for (var ia = 0; ia < d.attributes.length; ia++) {
			var a = d.attributes[ia];
			var id = a.nodeName;
			var v = a.nodeValue;

			$('table[id=Admin]:last input[id="' + id + '"]').val(v);
		}
	}
}
function create_ewa_conf_xml() {
	var xml = new EWA_XmlClass();
	//
	xml
		.LoadXml('<?xml version="1.0" encoding="UTF-8"?>\
<ewa_confs><!-- 系统文件目录 --><paths />\
<!-- 数据库连接池 --><databases />\
<!-- Cookie加密解密DES --><des />\
<!-- 显示debug的ip地址，用","分割不同的ip, unincludes 排除的目录，不在DEFINE中列出 --><debug />\
<!-- 发送邮件服务器 --><smtp />\
<!-- 合法的域名，用于合并css，js的合法域名检查，避免跨域攻击 --><validDomains />\
<!-- 初始化加载的全局变量 --><requestValuesGlobal />\
<!-- DEFINE管理员 --><Admins />\
<!-- 认证加密信息 --><cert />\
<!-- remote 系统同步 --><remote_syncs /></ewa_confs>');

	var root = xml.XmlDoc.getElementsByTagName('ewa_confs')[0];

	root.setAttribute("CreateTime", new Date());

	var cert = xml.XmlDoc.getElementsByTagName("cert")[0];

	$('#cert input,#cert textarea').each(function() {
		var ipt = $(this);
		var id = ipt.attr('id');
		var v = ipt.val();
		var ele = xml.XmlDoc.createElement(id);
		cert.appendChild(ele);
		ele.textContent = v;
	});

	var paths = xml.XmlDoc.getElementsByTagName("paths")[0];
	$('#paths tr:gt(0)').each(function() {
		var path = xml.XmlDoc.createElement('path');
		var ipt = $(this).find('input');
		path.setAttribute("Name", ipt.attr('id'));
		path.setAttribute("Value", ipt.val());
		path.setAttribute('Des', this.cells[2].innerText);

		paths.appendChild(path);
	});

	var debug = xml.XmlDoc.getElementsByTagName('debug')[0];
	$('#debug tr:gt(0)').each(function() {
		var ipt = $(this).find('input,select');
		debug.setAttribute(ipt.attr('id'), ipt.val());
	});
	var des = xml.XmlDoc.getElementsByTagName('des')[0];
	$('#des tr:gt(0)').each(function() {
		var ipt = $(this).find('input');
		des.setAttribute(ipt.attr('id'), ipt.val());
	});
	var smtp = xml.XmlDoc.getElementsByTagName('smtp')[0];
	$('#smtp tr:gt(0)').each(function() {
		var ipt = $(this).find('input');
		smtp.setAttribute(ipt.attr('id'), ipt.val());
	});

	var validDomains = xml.XmlDoc.getElementsByTagName('validDomains')[0];
	$('#validDomains tr:gt(0)').each(function() {
		var ipt = $(this).find('input');
		validDomains.setAttribute(ipt.attr('id'), ipt.val());
	});

	var databases = xml.XmlDoc.getElementsByTagName("databases")[0];
	$('#databases>table').each(function() {
		var database = xml.XmlDoc.createElement('database');
		databases.appendChild(database);

		var have_pool = false;
		window.aaa = this;
		$(this).find('tbody:eq(0)>tr').each(function() {
			if ($(this).find('table').length > 0) {
				return;
			}
			var ipt = $(this).find('input,select');
			var id = ipt.attr('id');
			var v = ipt.val();
			database.setAttribute(id, v);
			if (id == 'Standard' && v == 'user') {
				have_pool = true;
			}
		});
		if (have_pool) {
			var tb_pool = $(this).find('table#pool');
			console.log(tb_pool)
			var pool = xml.XmlDoc.createElement('pool');
			database.appendChild(pool);

			tb_pool.find('tr').each(function() {
				var ipt = $(this).find('input,select');
				var id = ipt.attr('id');
				var v = ipt.val();
				pool.setAttribute(id, v);
			});
		}
	});

	var requestValuesGlobal = xml.XmlDoc.getElementsByTagName('requestValuesGlobal')[0];
	$('#requestValuesGlobal table').each(function() {
		var rv = xml.XmlDoc.createElement('rv');
		requestValuesGlobal.appendChild(rv);
		$(this).find('tbody tr').each(function() {
			var ipt = $(this).find('input');
			rv.setAttribute(ipt.attr('id'), ipt.val());
		});
	});

	var remote_syncs = xml.XmlDoc.getElementsByTagName('remote_syncs')[0];
	$('#remote_syncs tr:gt(0)').each(function() {
		var ipt = $(this).find('input');
		remote_syncs.setAttribute(ipt.attr('id'), ipt.val());
	});
	$('#remote_syncs1 table').each(function() {
		var remote_sync = xml.XmlDoc.createElement('remote_sync');
		remote_syncs.appendChild(remote_sync);
		$(this).find('tbody tr').each(function() {
			var ipt = $(this).find('input');
			remote_sync.setAttribute(ipt.attr('id'), ipt.val());
		});
	});

	var Admins = xml.XmlDoc.getElementsByTagName('Admins')[0];
	$('#Admins table').each(function() {
		var Admin = xml.XmlDoc.createElement('Admin');
		Admins.appendChild(Admin);
		$(this).find('tbody tr').each(function() {
			var ipt = $(this).find('input');
			Admin.setAttribute(ipt.attr('id'), ipt.val());
		});
	});

	var s = xml.GetXml();
	$('.xml-result').show();
	window.editor.setValue(s);
	window.beautify_code();
}
function get_path(obj) {
	// console.log(obj.files[0].getAsDataURL())
	var fr = new FileReader();
	fr.onloadend = function(e) {
		console.log(e);
	};
	fr.readAsDataURL(obj.files[0]);
}
function add_paras(id) {
	var o = $('#' + id);
	var o1 = o.clone();
	o1.addClass('copyed');
	o.parent().append(o1);
	o1.find('input').val("");
}
function del_paras(obj) {
	$(obj).parentsUntil('div').last().remove();
}
function r_key() {
	var s = new Uint8Array(128);
	for (var i = 0; i < s.length; i++) {
		var a = Math.round(Math.random() * 255);
		s[i] = a;
	}
	var s1 = btoa(s);// base64编码
	$('#key').val(s1);
}
function random_code(len) {
	var s = [];
	var inc = 0;
	var inc1 = 0;
	while (inc < len) {
		inc1++;
		if (inc1 > 2000) {
			break;
		}
		var a = Math.round(Math.random() * 127);
		if (a >= 33 && a <= 126) {
			var c = String.fromCharCode(a);
			if (c == '+' || c == '=' || c == '&' || c == '>' || c == '<' || c == '"') {
				continue;
			}
			s.push(c);
			inc++;
		}
	}
	return s.join('');
}
function r_des_code() {
	var s = random_code(128);
	$('#desKeyValue').val(s);
}
function r_des_iv() {
	var s = random_code(8);
	$('#desIvValue').val(s);
}
function r_code() {
	var s = random_code(64);
	$('#code').val(s);
}
function init_navs() {
	$('body>table, body>div[id]').each(function() {
		var id = this.id;
		var t = $(this).find('caption').text();
		var nav = $('<div id="nav#' + id + '"><a href="javascript:nav(&quot;' + id + '&quot;)"></a></div>');
		nav.find('a').text('[' + id + '] ' + t);
		$('.navs').append(nav);
	});
}
function nav(id) {
	var p = $('#' + id).position();
	console.log(p)
	// document.body.scrollTop = p.top;
	$(document.body).animate({
		scrollTop : p.top - 40
	});
}