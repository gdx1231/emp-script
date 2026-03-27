var clss = [];
function jsonMap(nameSpace, clsName, jsonStr, isNet) {
	clss = [];

	var o;
	eval('o=' + jsonStr);
	jsonWalk(clsName, o);
	var ss = [];
	if (isNet) {
		ss.push('using System;');
		ss.push('using System.Collections.Generic;');
		ss.push('using System.Text;');
		ss.push('using LitJson;');
		ss.push('namespace ' + nameSpace + '{');
	}
	for (var i = 0; i < clss.length; i++) {
		var s = clss[i].ToCode(isNet);
		ss.push(s);
	}
	if (isNet) {
		ss.push('}');
	}
	code.value = ss.join('\r\n');
}

function jsonWalk(name, obj) {
	var cls = new JsonClass(name);
	clss.push(cls);

	for (var n in obj) {
		var o1 = obj[n];
		var type = jsonType(o1);
		if (type == 'Object') {
			var c = jsonWalk(n, o1);
			cls.AddField(n, c._Name);
		} else if (type == 'Array') {
			var c = jsonWalk(n, o1[0]);
			cls.AddField(n, c._Name, true);
		} else {
			cls.AddField(n, type);
		}
	}
	return cls;
}

function jsonType(obj) {
	if (obj == null) {
		return 'String';
	}

	if (typeof(obj) == "string") {
		return "String";
	}
	if (typeof(obj) == "number") {
		return "Double";
	}

	if (typeof(obj) == "boolean") {
		return "Boolean";
	}

	if (obj.constructor === Array) {
		return 'Array';
	}

	return "Object";
}

function JsonClass(name, isNet) {
	this._Name = 'cls' + fixName(name);
	this._Fields = {};
	this.AddField = function(name, type, isArray) {
		var f = new JsonField(name, type);
		this._Fields[name] = f;
		f._IsArray = isArray;
	}

	this.ToCode = function(isNet) {
		var ss = [];
		ss.push('public class ' + this._Name + '{');
		var instance = this._CodeInstance();
		ss.push(instance);
		if (isNet) {
			ss.push('#region props');
		}
		for (var n in this._Fields) {
			var f = this._Fields[n];
			ss.push(f.ToCode(isNet));
		}
		if (isNet) {
			ss.push('#endregion');
		}

		ss.push('}');
		return ss.join('\r\n');
	}

	this._CodeInstance = function() {
		var ss = [];
		ss.push('\tpublic static ' + this._Name
				+ ' InstanceOf(String jsonString){');
		ss.push('\t\tJsonData data = JsonMapper.ToObject(jsonString);');

		ss.push('\t\treturn InstanceOf(data);');
		ss.push('\t}');

		ss
				.push('\tpublic static ' + this._Name
						+ ' InstanceOf(JsonData data){');
		ss.push('\t\t' + this._Name + ' cls = new ' + this._Name + '();');
		for (var n in this._Fields) {
			var f = this._Fields[n];
			var s = 'data["' + f._JsonName + '"].ToString()';
			var s1 = 'data["' + f._JsonName + '"]';

			var s0 = '\t\tcls._' + f._Name + ' = ';
			ss.push('if(' + s1 + ' != null){');
			if (f._Type == 'String') {
				ss.push(s0 + s + ';');
			} else if (f._Type == 'Double') {
				ss.push(s0 + 'Convert.ToDouble(' + s + ');');
			} else if (f._Type == 'Boolean') {
				ss.push(s0 + 'Convert.ToBoolean(' + s + ');');
			} else if (f._IsArray) {
				var n = 'data' + f._JsonName;
				ss
						.push('\t\tJsonData ' + n + ' = data["' + f._JsonName
								+ '"];');
				ss.push('\t\tcls._' + f._Name + ' = new List<cls' + f._Name
						+ '>();');

				ss.push('\t\tfor(int i=0; i<' + n + '.Count; i++){');
				ss.push('\t\t\tJsonData item=' + n + '[i];');
				ss.push('\t\t\tcls' + f._Name + ' c = cls' + f._Name
						+ '.InstanceOf(item);');
				ss.push('\t\t\tcls._' + f._Name + '.Add( c );');
				ss.push('\t\t}');
			} else {
				var n = 'data' + f._JsonName;
				ss
						.push('\t\tJsonData ' + n + ' = data["' + f._JsonName
								+ '"];');
				ss.push('\t\tcls._' + f._Name + ' = cls' + f._Name
						+ '.InstanceOf(' + n + ');');

			}
			ss.push('}');
			ss.push('');
		}
		ss.push('\t\treturn cls;');
		ss.push('\t}');
		return ss.join('\r\n');
	}
}
function JsonField(name, type) {
	this._Name = fixName(name);
	this._JsonName = name;
	this._Type = type;
	this._IsArray = false;

	this.ToCode = function(isNet) {
		var ss = [];
		var t = this._IsArray ? (isNet
				? 'List<' + this._Type + '>'
				: 'ArrayList<' + this._Type + '>') : this._Type;
		ss.push('\tprivate ' + t + ' _' + this._Name + ';');
		if (isNet) {
			ss.push('\tpublic ' + t + ' ' + this._Name + '{');
			ss.push('\t\tget{return this._' + this._Name + ';}');
			ss.push('\t\tset{this._' + this._Name + '=value;}');
			ss.push('\t}');
			ss.push('');

		} else {
			ss.push('');
			ss.push('\tpublic ' + t + ' get' + this._Name + '(){');
			ss.push('\t\treturn this._' + this._Name + ';');
			ss.push('\t}');
			ss.push('');

			ss.push('\tpublic void set' + this._Name + '(' + t + ' v){');
			ss.push('\t\tthis._' + this._Name + '=v;');
			ss.push('\t}');
			ss.push('');
		}
		return ss.join('\r\n');
	}

}

function fixName(name) {
	var len = name.length;
	var n = name.toUpperCase().substring(0, 1) + name.substring(1, len);
	return n;
}
