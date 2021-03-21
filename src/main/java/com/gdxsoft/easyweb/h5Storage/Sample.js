var EWA_Res = {
	storeageName : "APP001",
	defaultQ : {
		css : {
			"default" : {}
		},
		js : {
			"default" : {},
			"app" : {}
		}
	},
	url : EWA.CP + "/back_admin/storage-resources/resources.jsp",

	/**
	 * 主程序
	 * 
	 * @param callback
	 *            回调程序
	 */
	doGet : function(callback) {
		if (!callback) {
			alert('需要指定回调函数');
			return;
		}
		var q = this.defaultQ;
		if (window.localStorage && window.localStorage[this.storeageName]) {
			try {
				// 创建请求
				var q1 = JSON.parse(window.localStorage[this.storeageName]);
				var q2 = {};
				for ( var n in q1) {
					var tag = q1[n];
					q2[n] = {};
					for ( var m in tag) {
						var group = tag[m];
						q2[n][m] = {};
						for ( var k in group) {
							var r = group[k];
							var key = this.getStorageResKey(r);
							if (window.localStorage[key]) {
								q2[n][m][k] = {
									"id" : r.id,
									"hash" : r.hash
								}
							} else {
								console.log(key + " not exists");
							}
						}
					}
				}
				q = q2;
			} catch (e) {
				console.log(e);
				window.localStorage.removeItem(this.storeageName);
			}
		}
		// console.log(q);
		this.get(q, callback);
	},
	get : function(q, callback) {
		var t0 = new Date().getTime();
		var sinfo = "q=" + encodeURIComponent(JSON.stringify(q));
		var url = this.url;
		var ajax = new XMLHttpRequest();
		ajax.open("POST", url, true);
		ajax.setRequestHeader("Content-Type",
				"application/x-www-form-urlencoded");
		ajax.send(sinfo);
		var c = this;
		ajax.onreadystatechange = function() {
			if (ajax.readyState != 4) {
				return;
			}
			if (ajax.status == 200) {
				var t1 = new Date().getTime();
				console.log(t1 - t0);
				c.handleRst(ajax.responseText, q, callback);
			} else {
				alert(ajax.statusText);
			}
		}
	},
	handleRst : function(result, query, callback) {
		var json = JSON.parse(result);
		var rst = json.RES;
		var cache = {};
		var ress = [];
		var cnts = [];

		// 已经存在的资源
		var exists_keys = {};
		if (window.localStorage) {
			for ( var n in window.localStorage) {
				if (n.indexOf(this.storeageName + "/") == 0) {
					exists_keys[n] = true;
				}
			}
		}
		for ( var n in rst) {
			var r = rst[n];
			var c_tag = cache[r.tag];
			if (!c_tag) {
				c_tag = {};
				cache[r.tag] = c_tag;
			}
			var c_group = c_tag[r.group];
			if (!c_group) {
				c_group = {};
				c_tag[r.group] = c_group;
			}

			c_group[r.id] = r;

			ress[r.index] = r;

			var key = this.getStorageResKey(r);

			if (exists_keys[key]) {
				// 标记已经存在资源不删除
				exists_keys[key] = false;
			}

			if (r.content) {
				if (window.localStorage) {
					// 在本地保存资源
					window.localStorage[key] = r.content;
				}
				cnts[r.index] = r.content;
				r.content = null;
			} else {
				cnts[r.index] = window.localStorage[key];
			}
		}

		for ( var n in exists_keys) {
			if (exists_keys[n]) {
				// 删除不需要的资源, 原来存储过，后来不需要了，删除
				console.log('DELETE UNUSED ' + n);
				window.localStorage.removeItem(n);
			}
		}
		// console.log(cache);

		if (window.localStorage) {
			window.localStorage[this.storeageName] = JSON.stringify(cache);
		}

		var t0 = new Date().getTime();

		// 创建样式
		var css = [];
		for ( var n in ress) {
			var r = ress[n];
			if (r.tag == 'css') {
				css.push("\n\n/*   " + r.id + "    */\n\n");
				css.push(cnts[n]);
			}
		}

		// 在页面创建样式
		this.addCss(css.join("\n"));

		// 创建老的EWA副本
		var old = JSON.parse(JSON.stringify(EWA));
		// 创建 js
		for ( var n in ress) {
			var r = ress[n];
			if (r.tag == 'js') {
				this.addJs(r, cnts[n]);
			}
		}

		// 从副本中恢复 初始化的 EWA;
		console.log(old);
		for ( var n in old) {
			EWA[n] = old[n];
		}
		var t1 = new Date().getTime();

		console.log(t1 - t0);

		callback(); // 启动app
	},
	getStorageResKey : function(r) {
		var key = this.storeageName + "/" + r.tag + "/" + r.group + "/" + r.id;
		return key;
	},
	/**
	 * 在页面创建 样式表
	 */
	addCss : function(css) {
		var st = document.createElement('style');
		st.type = 'text/css';
		st.innerHTML = css;
		document.getElementsByTagName("head")[0].appendChild(st);
	},
	/**
	 * 在页面创建 脚本
	 */
	addJs : function(r, js) {
		var sc = document.createElement("script");
		sc.text = js;
		sc.type = "text/javascript";
		sc.id = r.id;
		sc.setAttribute("idx", r.index);
		document.getElementsByTagName("head")[0].appendChild(sc);
	}
};

//程序调用
EWA_Res.doGet(function  () {
	var u = "student/2019-competition-home.jsp?" + $U();
	$J3(u, function(rst) {
		document.body.innerHTML = rst;
		EWA.C.Utils.JsRegisters(rst);

		$('body').append("<section id='home'></section>")
		startApp();
	});
});