

function EWA_ColorMark2(object, func) {
	this._Object = object;
	this.Select = null;
	this._LastValue = "";
	this._KeyUpEvent = true;
	this._Keys = [];
	this._Funcs = func;

	this.KeyDown = function(evt) {
		var e = evt ? evt : event;
		var code = e.keyCode;
		switch (code) {
			case 83 : // s
				if (!e.altKey) {// ctrl
					break;
				}
				window.parent.ok();
			case 90 : // z
				if (!e.ctrlKey) {// ctrl
					break;
				}
				e.returnValue = false;
				break;
			case 13 : // enter
				break;
		}
		this._LastKeyCode = code;
	};
	this.KeyUp = function(evt) {
		if (!this._KeyUpEvent) {
			this._KeyUpEvent = true;
			return;
		}
		var e = evt ? evt : event;
		window._OBJ_ = e.srcElement ? e.srcElement : e.target;
		var code = e.keyCode;
		var sel2 = EWA.B.IE ? document.selection.createRange() : window
				.getSelection().getRangeAt(0);
		var left = sel2.offsetLeft;
		var top = sel2.offsetTop;
		if (code == 190) { // .
			this.ShowFunction(left, top);
		} else {
			this._Keys.push(code)
		}
		return;
	};
	this.ShowFunction = function(left, top) {
		if (!this._Funcs) {
			return;
		}

		var token = this._GetToken();
		token = token.replace(/\u3000/ig, "");
		var ft = this._Funcs.FunctionTable;
		var c = /EWA.F.FOS\[.*\]/ig;
		var t = token.substring(0, token.length - 1);
		t = t.replace(c, this._Funcs.Tag);
		t = t.toUpperCase();
		// $('butOk').value = token+','+ this._Funcs.Tag + "," + t;
		var f = ft[t];
		if (f != null && f.Properites.length > 0) {
			var loc = EWA$UI$COMMON.GetPosition(this._Object);
			this.Select.style.position = "absolute";
			this.Select.style.left = loc.X + "px";
			this.Select.style.top = loc.Y + "px";
			this.Select.style.display = "";
			if (this.Select.childNodes.length == 0) {
				this._Init();
			} else {
				this.Select.childNodes[0].options.length = 0;
			}
			for (var i = 0; i < f.Properites.length; i += 1) {
				var b = f.Properites[i];
				var v = b.Name;
				if (b.Parameters.length > 0) {
					v += b.Parameters;
				}
				this.Select.childNodes[0].options[i] = new Option(b.Name
								+ "   " + b.Type, v);
			}
			this.Select.childNodes[0].focus();
		}
	};
	this._GetToken = function() {
		return this._Object.value;
	};

	this.Init = function() {
		var s1 = "<select ondblclick='_EWA_COLOR_MARK_WND._Object.value += this.options[this.selectedIndex].value;"
				+ "this.parentNode.style.display=\"none\";_EWA_COLOR_MARK_WND._Object.focus();'"
				+ " onkeydown='if (event.keyCode == 13){";
		s1 += "_EWA_COLOR_MARK_WND._Object.value += this.options[this.selectedIndex].value;";
		s1 += "this.parentNode.style.display=\"none\";_EWA_COLOR_MARK_WND._Object.focus();";
		s1 += "}else if(event.keyCode == 27 || event.keyCode == 8){this.parentNode.style.display=\"none\";_EWA_COLOR_MARK_WND._Object.focus();}' size=6>";
		s1 += "</select>";
		this.Select = document.createElement("div");
		this.Select.style.display = 'none';
		document.body.appendChild(this.Select);
		this.Select.innerHTML = s1;
		_EWA_COLOR_MARK_WND = this;

		if (this._Object) {
			this._Object.onkeyup = function(event) {
				_EWA_COLOR_MARK_WND.KeyUp(event);
			}
			this._Object.onkeydown = function(event) {
				_EWA_COLOR_MARK_WND.KeyDown(event);
			}
		}
	};

}

function EWA_FunctionProperty() {
	this.Name;
	this.FullName;
	this.Properites = new Array();
	this.Parent;
	this.Type;
	this.Level;
	this.Parameters = "";
	this.Dispose = function() {
		for (var i = 0; i < this.Properites.length; i += 1) {
			this.Properites[i].Dispose();
			this.Properites[i] = null;
		}
		this.Name = null;
		this.FullName = null;
		this.Parent = null;
		this.Type = null;
		this.Level = null;
		this.Parameters = null;
	};
}

function EWA_Function() {
	this.Functions = new Array();
	this.FunctionTable = {};
	this.Dispose = function() {
		for (var i = 0; i < this.Functions.length; i += 1) {
			this.Functions[i].Dispose();
		}
		for (a in this.FunctionTable) {
			this.FunctionTable[a] = null;
		}
		this.Functions = null;
		this.FunctionTable = null;
	};
	this.AddTopObject = function(obj, objName) {
		var topProp = new EWA_FunctionProperty();
		topProp.Name = objName;
		topProp.Object = obj;
		topProp.Level = 0;
		topProp.Type = typeof obj;
		topProp.FullName = objName;
		this.Functions[this.Functions.length] = topProp;
	};
	this.LoadFunctions = function() {
		for (var i = 0; i < this.Functions.length; i += 1) {
			var prop0 = this.Functions[i];
			this.GetFuncs(prop0.Object, 0, false, prop0);
			this.FunctionTable[prop0.FullName] = prop0;
		}
	};
	this.LoadResources = function(tableSet, tagName) {
		this.Tag = "EWA.F.F.C";
		if (tagName.toUpperCase() == "ListFrame".toUpperCase()) {
			this.Tag = "EWA.F.L.C";
		} else {
			if (tagName.toUpperCase() == "Tree".toUpperCase()) {
				this.Tag = "EWA.F.T.C";
			}
		}
		var r = this.FunctionTable[(this.Tag + ".Resources").toUpperCase()];
		for (var i = 0; i < tableSet.Count(); i += 1) {
			var tbs = tableSet.GetItem(i);
			var propChild = new EWA_FunctionProperty();
			propChild.Name = tbs.Name;
			propChild.Parent = r;
			propChild.Level = r.Level + 1;
			propChild.FullName = r.FullName + "." + propChild.Name;
			var tb = tbs.Tables.GetItem("DescriptionSet");
			propChild.Type = tb.SearchValue("Lang=" + EWA.LANG, "Info") + ", "
					+ tb.SearchValue("Lang=" + EWA.LANG, "Memo");

			// getinfo
			var pc0 = new EWA_FunctionProperty();
			pc0.Name = "GetInfo";
			pc0.FullName = propChild.FullName + "." + pc0.Name;
			pc0.Level = r.Level + 2;
			pc0.Parent = propChild;
			pc0.Type = "function";
			pc0.Parameters = "()";
			propChild.Properites[0] = pc0;

			// getmemo
			var pc1 = new EWA_FunctionProperty();
			pc1.Name = "GetMemo";
			pc1.FullName = propChild.FullName + "." + pc1.Name;
			pc1.Level = r.Level + 2;
			pc1.Parent = propChild;
			pc1.Type = "function";
			pc1.Parameters = "()";
			propChild.Properites[1] = pc1;
			this.FunctionTable[propChild.FullName.toUpperCase()] = propChild;
			r.Properites[i] = propChild;
		}
	};
	this.GetFuncs = function(obj, lvl, isFunc, prop) {
		if (lvl > 4) {
			return;
		}
		try {
			for (a in obj) {
				var b = obj[a];
				var c = typeof b;
				var propChild = new EWA_FunctionProperty();
				propChild.Level = lvl;
				propChild.Name = a;
				propChild.FullName = prop.FullName + "." + a;
				propChild.Parent = prop;
				propChild.Type = c;
				prop.Properites[prop.Properites.length] = propChild;
				this.FunctionTable[propChild.FullName.toUpperCase()] = propChild;
				if (c == "function") {
					var b1 = b.toString();
					var a1 = b1.indexOf('(');
					var a2 = b1.indexOf(')');
					var s1 = b1.substring(a1 + 1, a2);
					propChild.Parameters = "(" + s1 + ");";
				}
				if (propChild.FullName == "EWA.F.FOS") {
					propChild.Parameters = "[\"@SYS_FRAME_UNID\"]";
				}
				if (isFunc) {
					continue;
				}
				if (c == "object") {
					this.GetFuncs(b, lvl + 1, false, propChild);
				}
				if (c == "function" && b.toString().indexOf("this.") > 0) {
					propChild.Type = "class";
					try {
						var d = new b();
						this.GetFuncs(d, lvl + 1, true, propChild);
						d = null;
					} catch (e) {
					}
				}
				a = b = c = null;
			}
			prop.Properites.sort(function(a, b) {
						return a.Name >= b.Name ? 1 : -1;
					});
		} catch (e) {

		}
	};
	this.List = function() {
		var b = new Array();
		for (var a1 in this.FunctionTable) {
			var a = this.FunctionTable[a1];
			var s2 = "";
			for (var i = 0; i < a.Level; i += 1) {
				s2 += "&nbsp;&nbsp;&nbsp;&nbsp;";
			}
			s2 += a.FullName + ", " + a.Type + "<br>";
			b[b.length] = s2;
		}
		return b.join("");
	};
}
