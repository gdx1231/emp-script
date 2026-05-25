
var testArray = new Array();
var testDate = new Date();
var testString = "aaa";
var testVal = 1;
var testObj = new myObj;
function myObj() {
	myObj.prototype.testFunc = function () {
	};
	this.testProperty = "test";
}
function KeyDown() {
 //alert(event.altKey);
	if (event.keyCode == 9) { //TAB 键
	}
	clipboardData.setData("text", "    ");
	event.srcElement.document.execCommand("paste");
	return false;
	if (event.keyCode == 8) { //Backspace 键
	}
	var oSel = document.selection.createRange();
	var offset = event.srcElement.document.selection.createRange();
	offset.moveToPoint(oSel.offsetLeft, oSel.offsetTop);
	offset.moveStart("character", -4);
	if (offset.text.length < 4) {
		return true;
	}
	for (var i = 0; i < offset.text.length; i++) {
		if (offset.text.charAt(i) != " ") {
			return true;
		}
	}
	offset.select();
	event.srcElement.document.execCommand("Delete");
	return false;
	return true;
}
function KeyUp() {
	var oSel, offset;
	if (event.keyCode == 13) {
		testStr = event.srcElement.innerText.substring(0, getCursorPosition());
  //alert(testStr);
		var space = "";
		for (var i = testStr.length - 1; i >= 0; i--) {
   //alert(testStr.length+":"+testStr.charAt(i) + ":" + space.length);
			if (testStr.charAt(i) == "\n") {
				break;
			}
			if (testStr.charAt(i) == " ") {
				space += " ";
			} else {
				space = "";
			}
		}
  //alert(testStr);
		clipboardData.setData("text", space);
		event.srcElement.document.execCommand("paste");
	}
	oSel = document.selection.createRange();
	var left = oSel.offsetLeft;
	var top = oSel.offsetTop;
	var token = getCurrentToken(event.srcElement);
	var chars = getCursorPosition();
	parseSyntax(event.srcElement);
	offset = event.srcElement.document.selection.createRange();
	offset.moveToPoint(left, top);
	offset.select();
	if (event.keyCode == 190) { //.键
	}
	setMethods(token.posTok.slice(0, -1));
}
//解析当前文本
function parseSyntax(src) {
	var text = src.innerHTML;
	text = text.replace(/<FONT[^<>]*>/gi, "").replace(/<\/FONT[^<>]*>/gi, "");
	text = text.replace(/<P>/gi, "\xfe").replace(/<\/P>/gi, "\xff");
	text = text.replace(/\&nbsp;/gi, "\xfd");
	text = text.replace(/\r\n/gi, "");
	for (var i = 0; i < SyntaxSet.All.length; i++) {
		var syntaxes = SyntaxSet.All[i];
		for (var j = 0; j < syntaxes.rules.All.length; j++) {
			syntaxes.rules.All[j].color = syntaxes.color;
			syntaxes.rules.All[j].cons = syntaxes.cons;
			text = parseRule(text, syntaxes.rules.All[j]);
		}
	}
	src.innerHTML = text.replace(/\xfc/g, "'").replace(/\xfe/g, "<P>").replace(/\xff/g, "</P>").replace(/\xfd/g, "&nbsp;");
}
function parseRule(text, rule) {
 //利用正则表达式
	var newText = "";
	var idx = text.search(rule.expr);
	while (idx != -1) {
		var remark = text.match(rule.expr);
  //alert(text.substring(0, idx+remark[0].length));
		var subText = text.substring(0, idx + remark[0].length);
		if (rule.cons == null || (idx == 0 || rule.cons.test(text.charAt(idx - 1))) && (idx + remark[0].length >= text.length || rule.cons.test(text.charAt(idx + remark[0].length)))) {
   //alert(remark[0]);
   //alert(remark[0].replace(/<FONT[^<>]*>/gi, "").replace(/</FONT[^<>]*>/gi,""));
			subText = subText.replace(remark[0], "<FONT color=\xfc" + rule.color + "\xfc>" + remark[0].replace(/<FONT[^<>]*>/gi, "").replace(/<\/FONT[^<>]*>/gi, "") + "</FONT>");
   //alert(subText);
		}
		newText += subText;
		text = text.substring(idx + remark[0].length);
		idx = text.search(rule.expr);
	}
	newText += text;
	return newText;
}
function getCurrentToken(src) {
	var oSel = document.selection.createRange();
	var offset = src.document.selection.createRange();
	offset.moveToPoint(oSel.offsetLeft, oSel.offsetTop);
	offset.moveStart("character", -99999);
	var tokens = offset.text.split(/[\s\+\-\*\/]/);  //token由连续字母数字、下划线、点号、括号、引号构成
	var currentToken = tokens[tokens.length - 1];
	var idx = offset.text.length;
	var fullToken = src.innerText.substring(idx);
	fullToken = fullToken.replace(/[\s\+\-\*\/]/, "@@@@");
	idx = fullToken.indexOf("@@@@");
	if (idx != -1) {
		fullToken = fullToken.substring(0, idx);
	}
	var token = new Array();
	token.currentToken = currentToken + fullToken;
	token.posTok = currentToken;
	return token;
}
Array.prototype.pushDistinct = function (obj) {
	for (var i = 0; i < this.length; i++) {
		if (this[i] == obj) {
			return null;
		}
	}
	this.push(obj);
	return obj;
};
 //将方法添加到方法列表
function putMethods(methodList, obj, methods) {
	var list = methods.split(",");
	for (var i = 0; i < list.length; i++) {
		if (obj[list[i]] != null) {
			methodList.pushDistinct(list[i]);
		}
	}
}
var now = new Date(); //测试用
var a = 33.3333; //测试用
var __expr = new RegExp("tt"); //测试用
function setMethods(objStr) {
	var oSel = document.selection.createRange();
	try {
		if (objStr == "alert") {
			return;
		}
		var methodList = new Array();
		var obj = eval(objStr);
		if (obj.prototype != null) {
			methodList.pushDistinct("prototype");
		}
		if (obj != null) {

    //基本Object方法
			putMethods(methodList, obj, "constructor,hasOwnProperty,isPrototypeOf,propertyIsEnumerable,toLocaleString,toString,valueOf");
    
    //基本Array方法
			putMethods(methodList, obj, "concat,join,length,pop,push,reverse,shift,slice,sort,splice,unshift");     

    //基本Date方法
			putMethods(methodList, obj, "getDate,getUTCDate,getDay,getUTCDay,getFullYear,getUTCFullYear,getHours,getUTCHours,getMilliseconds,getUTCMilliseconds,getMinutes,getUTCMinutes,getMonth,getUTCMonth,getSeconds,getUTCSeconds,getTime,getTimezoneoffset,getYear");
			putMethods(methodList, obj, "setDate,setUTCDate,setFullYear,setUTCFullYear,setHours,setUTCHours,setMilliseconds,setUTCMilliseconds,setMinutes,setUTCMinutes,setMonth,setUTCMonth,setSeconds,setUTCSeconds,setTime,setYear,toDateString,toGMTString,toLocaleDateString,toLocaleTimeString,toString,toTimeString,toUTCString,valueOf,parse,UTC");

    //基本Math方法
			putMethods(methodList, obj, "E,LN10,LN2,LOG10E,LOG2E,PI,SQRT1_2,SQRT2");
			putMethods(methodList, obj, "abs,acos,asin,atan,atan2,ceil,cos,exp,floor,log,max,min,pow,random,round,sin,sqrt,tan");

    //基本Function方法
			putMethods(methodList, obj, "arguments,caller,length,prototype,apply,call,toString");
    
    //基本Number方法
			putMethods(methodList, obj, "MAX_VALUE,MIN_VALUE,NaN,NEGATIVE_INFINITY,POSITIVE_INFINITY");
			putMethods(methodList, obj, "toString,toLocalString,toFixed,toExponential,toPrecision");

    //基本RegExp方法
			putMethods(methodList, obj, "global,ignoreCase,lastIndex,multiline,source,exec,test");

    //基本String方法
			putMethods(methodList, obj, "charAt,charCodeAt,contact,indexOf,lastIndexOf,match,replace,search,slice,split,substring,substr,toLowerCase,toString,toUpperCase,valueOf,fromCharCode");
			putMethods(methodList, obj, "anchor,big,blink,bold,fixed,fontcolor,fontsize,italics,link,small,strike,sub,sup");
		}
		for (each in obj) {
			methodList.pushDistinct(each);
		}
		methodList.sort();
		if (methodList.length > 0) {
			methods.options.length = 0;
			for (var i = 0; i < methodList.length; i++) {
				methods.options.add(new Option(methodList[i]));
			}
			if (methods.options.length > 10) {
				methods.size = 10;
			} else {
				methods.size = methods.options.length;
			}
			methods.style.top = oSel.offsetTop;
			methods.style.left = oSel.offsetLeft;
			methods.style.display = "";
			methods.options[0].selected = true;
			methods.focus();
		}
	}
	catch (e) {
	}
}
function SelectMethod() {
	var src = event.srcElement;
	if (event.keyCode == 13) {
		SelMethod(src);
	}
	if (event.keyCode == 27 || event.keyCode == 8 || event.keyCode == 32) {
		src.style.display = "none";
		editbox.focus();
	}
}
function SelMethod(src) {
	clipboardData.setData("text", src.options[src.selectedIndex].text);
	editbox.focus();
	editbox.document.execCommand("paste");
	src.style.display = "none";
	getCursorPosition();
}
//计算行数、列数
function getPos(text) {
	var rows = 1;
	var cols = 1;
	var idx = 0;
	var subText = text;
	while ((idx = subText.indexOf("\n")) != -1) {
		subText = subText.substring(idx + 1);
		rows++;
	}
	return new Array(rows, subText.length + 1);
}
//计算空行
function getNullRows(src, oSel) {
	var rows = 0;
	var offsetEnd = src.document.selection.createRange();
	var oldTop = 2;
	var oldLeft = 2;
	while (1) {
		offsetEnd.moveToPoint(oSel.offsetLeft, oSel.offsetTop);
		offsetEnd.moveStart("character", -1 - rows);
		if (offsetEnd.text.length > 0 || offsetEnd.offsetTop == oldTop && offsetEnd.offsetLeft == oldLeft) {
			break;
		}
		rows++;
		oldTop = offsetEnd.offsetTop;
		oldLeft = offsetEnd.offsetLeft;
	}
	return rows;
}
function getCursorPosition() {
	var src = event.srcElement;
	var offset = src.document.selection.createRange();
	var oSel = document.selection.createRange();
	var textLength = src.innerText.length;
	offset.moveToPoint(oSel.offsetLeft, oSel.offsetTop);
	offset.moveStart("character", -99999);
 //src.document.execCommand("ForeColor",false,"#ff0000");
	var rowSpans = offset.getClientRects();
	var pos = getPos(offset.text);
	var charCodes = offset.text.length; //字符总数
	var chars = offset.text.replace(/\r\n/g, "").length + 1; //字符
	var extRows = getNullRows(src, oSel);
	if (extRows > 0) {
		pos[0] += extRows;
		pos[1] = 1;
	}
	window.status = "\u884c: " + pos[0] + ", \u5217: " + pos[1] + ", \u7b2c " + chars + " \u4e2a\u5b57\u7b26" + "  (" + oSel.offsetTop + "," + oSel.offsetLeft + ")";
	return charCodes;
}


///词法解析过程................................................................................
///............................................................................................
///............................................................................................
var SyntaxSet = new Array(); //词法规则集合
SyntaxSet.All = new Array();
//针对token返回rule
SyntaxSet.parse = function (token) {
	for (var i = 0; i < this.All.length; i++) {
		var syntaxes = this.All[i];
		for (var j = 0; j < syntaxes.rules.All.length; j++) {
			if (syntaxes.rules.All[j].test(token)) {
				syntaxes.rules.All[j].color = syntaxes.color;
				return syntaxes.rules.All[j];
			}
		}
	}
	return null;
};
SyntaxSet.add = function (syntaxes) {
	if (this[syntaxes.name] != null) {
		return;
	}
	this[syntaxes.name] = syntaxes;
	this.All.push(syntaxes);
};

//词法规则组（同组规则用一种颜色标记）
function Syntaxes(name, color, cons) {
	this.name = name; //规则组名称
	this.color = color;  //标记该语法的颜色
	this.rules = new Array();  //语法规则（以次序决定优先级）
	this.rules.All = new Array();
	this.cons = cons;  //边界约束
	Syntaxes.prototype.addRule = function (rule) {
		if (this.rules[rule.name] != null) {
			return;
		}
		this.rules[rule.name] = rule;
		this.rules.All.push(rule);
	};
}
//词法规则
function SyntaxRule(name, regExp) {
	this.name = name;    //规则名称
	this.expr = regExp;  //规则描述 (正则表达式)
	SyntaxRule.prototype.test = function (token) {
		return this.expr.test(token);
	};
}
//扩展正则表达式的功能，支持定义嵌套
function RegExprX(exprStr) {
	this.expr = exprStr;
}
//获取正则表达式对象
RegExprX.prototype.getPattern = function (tag) {
	if (tag == null) {
		return new RegExp(this.expr);
	} else {
		return new RegExp(this.expr, tag);
	}
};
 //连接两个正则表达式串
RegExprX.prototype.concat = function (expr, rule) {
	if (rule == null) {
		this.expr += expr;  //直接连接
	} else {
		if (rule == "union") {  //联合
			this.expr = "(" + this.expr + ")" + "|" + "(" + expr + ")";
		} else {
			if (rule == "cons") { //约束
				this.expr = this.expr + "(?=" + expr + ")";
			}
		}
	}
	return this.expr;
};

//为保证正确计算偏移量需要替换回车nr为xff
SyntaxSet.add(new Syntaxes("keywords", "#0000ff", /[\s\.\xfe\xff\xfd\(\{\}\)\;\,]/));  //词法?关键词?蓝色
SyntaxSet["keywords"].addRule(new SyntaxRule("Function", /function/));
SyntaxSet["keywords"].addRule(new SyntaxRule("Variable", /var/));
SyntaxSet["keywords"].addRule(new SyntaxRule("Return", /return/));
SyntaxSet["keywords"].addRule(new SyntaxRule("Exception", /(try|catch|throw)/));
SyntaxSet["keywords"].addRule(new SyntaxRule("Condition", /(if|else|switch)/));
SyntaxSet["keywords"].addRule(new SyntaxRule("Cycle", /(for|while|do)/));
SyntaxSet["keywords"].addRule(new SyntaxRule("Type", /(int|double|float|void|char)/));
SyntaxSet["keywords"].addRule(new SyntaxRule("Right", /(public|private|protected|static)/));
SyntaxSet["keywords"].addRule(new SyntaxRule("Constant", /(null|undefined|NaN|Infinity)/));
SyntaxSet["keywords"].addRule(new SyntaxRule("Construct", /(new|delete)/));
SyntaxSet.add(new Syntaxes("objects", "#FF0000", /[\s\.\xfe\xff\xfd\(\{\}\)\;\,]/));  //词法?对象?红色
SyntaxSet["objects"].addRule(new SyntaxRule("Object", /(Array|arguments|Boolean|Date|Error|Function|Object|Number|Math|RegExp|String)/));
SyntaxSet.add(new Syntaxes("global", "#800000", /[\s\.\xfe\xff\xfd\(\{\}\)\;\,]/));  //词法?系统函数?红色
SyntaxSet["global"].addRule(new SyntaxRule("SystemFunc", /(alert|parseFloat|parseInt|eval|decodeURI|decodeURIComponent|encodeURI|encodeURIComponent|escape|eval|isFinite|isNaN|unescape)/));
SyntaxSet.add(new Syntaxes("String", "#ff00ff", /[\s\.\xfe\xff\xfd\(\{\}\)\;\,\+\-\*\/]/));  //词法?字符串?粉色
SyntaxSet["String"].addRule(new SyntaxRule("String", /('((\\\')|[^\xff\'])*([^\\\']|(\\\'))')|("((\\\")|[^\xff\"])*([^\\\"]|(\\\"))")/));
SyntaxSet.add(new Syntaxes("remarks", "#008000")); //词法?注释?绿色
SyntaxSet["remarks"].addRule(new SyntaxRule("ShortRemark", /\/\/[^\xff]*/));
SyntaxSet["remarks"].addRule(new SyntaxRule("LongRemark", /\/\*((.*\*\/)|(.*$))/));
 //语法规则
function Grammars() {
}

