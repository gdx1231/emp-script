// uiClass=EWAC_UIClass
var _EWAC_UI;
var _EWAC_TABLE;
var tbRepeat;
var TYPE = 0;
var _EWAC_TABLES_SET;
var _EWAC_XITEM_PARAMETERS;
function ok() {
	window.clearInterval(timer);
	if (TYPE == 0) {
		okSet();
	} else {
		okItems();
	}
	_EWA_DialogWnd._OpenerWindow._EWAC_UI.IsChanged = true;
	_EWA_DialogWnd.CloseWindow();// close window
}

function okItems() {
	var tb = tbRepeat._obj;
	var tablesSet = _EWAC_TABLES_SET;
	for (var i = 0; i < tablesSet.Count(); i += 1) {
		tablesSet.GetItem(i).IsDelete = true;
	}
	var lang = EWA.LANG ? EWA.LANG : "zhcn";

	for (var i = 2; i < tb.rows.length - 1; i += 1) {
		if (tb.rows[i].style.display == "none") {
			continue;
		}
		var rowIndex = tb.rows[i].getAttribute("R_INDEX");
		var data = tablesSet.GetData(rowIndex);
		var dataTables = tablesSet.GetItem(rowIndex);
		dataTables.IsDelete = false;
		data._Index = i;// 数据的序列，用于排序用
		for (var m = 1; m < tb.rows[i].cells.length; m += 1) {
			var val;
			var ids = tb.rows[0].cells[m].id.split(".");
			var table = dataTables.Tables.GetItem(ids[0]);
			var objs = tb.rows[i].cells[m].getElementsByTagName("input");
			if (objs.length == 0) {
				objs = tb.rows[i].cells[m].getElementsByTagName("select");
				val = objs[0].value;
			} else {
				val = objs[0].getAttribute("EWA_VALUE");
			}

			if (m == 1) {
				data._Name = val;
				dataTables.Name = val;
			}
			if (ids[0] == "DescriptionSet") {
				var indexLang = table.Columns.GetIndex("Lang");
				var rowLang1 = null;
				for (var kk = 0; kk < table.Rows.length; kk++) {
					var dRow = table.Rows[kk];
					var rowLang = dRow.Fields[indexLang];
					if (rowLang == lang) {
						rowLang1 = dRow;
						break;
					}
				}
				if (rowLang1 == null) {
					// lang
					rowLang1 = table.AddRow();
					rowLang1.Fields[indexLang] = lang;
				}
				// info
				var index = table.Columns.GetIndex(ids[1]);
				rowLang1.Fields[index] = val;
			} else {
				if (table.Rows.length == 0) {
					table.AddRow();
				}

				var index = table.Columns.GetIndex(ids[1]);
				table.Rows[0].Fields[index] = val;
			}
			objs = null;
		}
	}
	tablesSet.Sort();
	_EWAC_UI.CreateType();
}

function okSet() {
	var tb = tbRepeat._obj;
	for (var i = 0; i < _EWAC_TABLE.Rows.length; i += 1) {
		_EWAC_TABLE.Rows[i].IsDelete = true;
	}
	for (var i = 2; i < tb.rows.length - 1; i += 1) {
		var rowIndex = tb.rows[i].getAttribute("R_INDEX");
		var dataRow = _EWAC_TABLE.Rows[rowIndex];
		_EWAC_TABLE.Rows[rowIndex].IsDelete = false;
		_EWAC_TABLE.Rows[rowIndex].Index = i;// 数据的序列，用于排序用
		for (var m = 0; m < _EWAC_TABLE.Columns.Count(); m += 1) {
			if (_EWAC_TABLE.Columns.GetItem(m).IsSet) {
				continue;
			}
			var objs = tb.rows[i].cells[m + 1].getElementsByTagName("input");
			if (objs.length == 0) {
				objs = tb.rows[i].cells[m + 1].getElementsByTagName("select");
				dataRow.Fields[m] = objs[0].value;
			} else {
				dataRow.Fields[m] = objs[0].getAttribute("EWA_VALUE");
			}
			objs = null;
		}
	}
	_EWAC_TABLE.Sort();
}
/**
 * 打开窗口调用，调用来自IDE OpenDialog
 * 
 * @param args
 */
// 初始化ITEMS，用于排序
function InitItemsSet(args) {
	TYPE = 1;
	console.log(args)
	var uiClass = args[0];
	_EWAC_UI = args[0];
	var lang = EWA.LANG;
	var objTable = args[1].GetIframeWindow().document.getElementById("tbMain");
	var valArray = new Array();
	var tag = args[2].toUpperCase();
	var tablesSet;
	var xItemParameters;
	var frameTag = uiClass._UserClass.PageTables.Tables.GetItem('FrameTag').GetSingleValue();
	window.G_frameTag = frameTag;
	switch (tag) {
	case "ITEMS":
		tablesSet = uiClass._UserClass.ItemsTablesSet;
		xItemParameters = uiClass._UserClass.Config.Items.XItemParameters;
		valArray[0] = {
			P : xItemParameters.GetXItemParameter("Name"),
			V : xItemParameters.GetXItemParameter("Name").Values.GetValue("Name")
		};
		if (frameTag == 'ListFrame') {
			valArray.push({
				P : xItemParameters.GetXItemParameter("DataItem"),
				V : xItemParameters.GetXItemParameter("DataItem").Values.GetValue("DataField")
			});
			valArray.push({
				P : xItemParameters.GetXItemParameter("Tag"),
				V : xItemParameters.GetXItemParameter("Tag").Values.GetValue("Tag")
			});

			valArray.push({
				P : xItemParameters.GetXItemParameter("DescriptionSet"),
				V : xItemParameters.GetXItemParameter("DescriptionSet").Values.GetValue("Info")
			});

			valArray.push({
				P : xItemParameters.GetXItemParameter("OrderSearch"),
				V : xItemParameters.GetXItemParameter("OrderSearch").Values.GetValue("IsOrder")
			});
			valArray.push({
				P : xItemParameters.GetXItemParameter("OrderSearch"),
				V : xItemParameters.GetXItemParameter("OrderSearch").Values.GetValue("SearchType")
			});
			valArray.push({
				P : xItemParameters.GetXItemParameter("DataItem"),
				V : xItemParameters.GetXItemParameter("DataItem").Values.GetValue("Format")
			});

		} else if (frameTag == 'Combine') {
			valArray.push({
				P : xItemParameters.GetXItemParameter("DescriptionSet"),
				V : xItemParameters.GetXItemParameter("DescriptionSet").Values.GetValue("Info")
			});
			valArray.push({
				P : xItemParameters.GetXItemParameter("CombineFrame"),
				V : xItemParameters.GetXItemParameter("CombineFrame").Values.GetValue("CbInstall")
			});
			valArray.push({
				P : xItemParameters.GetXItemParameter("CombineFrame"),
				V : xItemParameters.GetXItemParameter("CombineFrame").Values.GetValue("CbGrp")
			});
			valArray.push({
				P : xItemParameters.GetXItemParameter("CombineFrame"),
				V : xItemParameters.GetXItemParameter("CombineFrame").Values.GetValue("CbMearge")
			});

		} else {
			valArray.push({
				P : xItemParameters.GetXItemParameter("DataItem"),
				V : xItemParameters.GetXItemParameter("DataItem").Values.GetValue("DataField")
			});

			valArray.push({
				P : xItemParameters.GetXItemParameter("DescriptionSet"),
				V : xItemParameters.GetXItemParameter("DescriptionSet").Values.GetValue("Info")
			});
			valArray.push({
				P : xItemParameters.GetXItemParameter("Tag"),
				V : xItemParameters.GetXItemParameter("Tag").Values.GetValue("Tag")
			});

			valArray.push({
				P : xItemParameters.GetXItemParameter("DataItem"),
				V : xItemParameters.GetXItemParameter("DataItem").Values.GetValue("DataType")
			});
			valArray.push({
				P : xItemParameters.GetXItemParameter("DataItem"),
				V : xItemParameters.GetXItemParameter("DataItem").Values.GetValue("Format")
			});
			valArray.push({
				P : xItemParameters.GetXItemParameter("GroupIndex"),
				V : xItemParameters.GetXItemParameter("GroupIndex").Values.GetValue("GroupIndex")
			});
			valArray.push({
				P : xItemParameters.GetXItemParameter("IsMustInput"),
				V : xItemParameters.GetXItemParameter("IsMustInput").Values.GetValue("IsMustInput")
			});

		}
		break;
	case "MENU":
		tablesSet = uiClass._UserClass.MenuTablesSet;
		xItemParameters = uiClass._UserClass.Config.Menu.XItemParameters;
		valArray.push({
			P : xItemParameters.GetXItemParameter("Name"),
			V : xItemParameters.GetXItemParameter("Name").Values.GetValue("Name")
		});
		valArray.push({
			P : xItemParameters.GetXItemParameter("DescriptionSet"),
			V : xItemParameters.GetXItemParameter("DescriptionSet").Values.GetValue("Info")
		});
		valArray.push({
			P : xItemParameters.GetXItemParameter("Cmd"),
			V : xItemParameters.GetXItemParameter("Cmd").Values.GetValue("Cmd")
		});
		valArray.push({
			P : xItemParameters.GetXItemParameter("Group"),
			V : xItemParameters.GetXItemParameter("Group").Values.GetValue("Group")
		});
		break;

	case "CHART":
		tablesSet = uiClass._UserClass.ChartTableSet;
		xItemParameters = uiClass._UserClass.Config.Chart.XItemParameters;
		valArray[0] = {
			P : xItemParameters.GetXItemParameter("Name"),
			V : xItemParameters.GetXItemParameter("Name").Values.GetValue("Name")
		};
		valArray[1] = {
			P : xItemParameters.GetXItemParameter("ChartType"),
			V : xItemParameters.GetXItemParameter("ChartType").Values.GetValue("ChartType")
		};
		valArray[2] = {
			P : xItemParameters.GetXItemParameter("ChartSize"),
			V : xItemParameters.GetXItemParameter("ChartSize").Values.GetValue("ChartWidth")
		};
		break;
	case "WORKFLOW":
		tablesSet = uiClass._UserClass.WorkflowTableSet;
		xItemParameters = uiClass._UserClass.Config.Workflow.XItemParameters;
		valArray.push({
			P : xItemParameters.GetXItemParameter("Name"),
			V : xItemParameters.GetXItemParameter("Name").Values.GetValue("Name")
		});
		valArray.push({
			P : xItemParameters.GetXItemParameter("WfType"),
			V : xItemParameters.GetXItemParameter("WfType").Values.GetValue("WfType")
		});
		valArray.push({
			P : xItemParameters.GetXItemParameter("WfLogic"),
			V : xItemParameters.GetXItemParameter("WfLogic").Values.GetValue("WfLogic")
		});

		valArray.push({
			P : xItemParameters.GetXItemParameter("WfAction"),
			V : xItemParameters.GetXItemParameter("WfAction").Values.GetValue("WFABefore")
		});
		valArray.push({
			P : xItemParameters.GetXItemParameter("WfAction"),
			V : xItemParameters.GetXItemParameter("WfAction").Values.GetValue("WFANextYes")
		});
		valArray.push({
			P : xItemParameters.GetXItemParameter("WfAction"),
			V : xItemParameters.GetXItemParameter("WfAction").Values.GetValue("WFANextNo")
		});
		break;
	case "PAGEINFOS":
		tablesSet = uiClass._UserClass.PageInfosTablesSet;
		xItemParameters = uiClass._UserClass.Config.PageInfos.XItemParameters;
		valArray[0] = {
			P : xItemParameters.GetXItemParameter("Name"),
			V : xItemParameters.GetXItemParameter("Name").Values.GetValue("Name")
		};
		break;
	}
	// header 信息
	var tr0 = InitRow(objTable, valArray.length);
	tr0.bgColor = "#DCDCDC";
	for (var i = 0; i < valArray.length; i += 1) {
		var v = valArray[i];
		InitHeader(tr0.cells[i + 1], v.V, lang);
		tr0.cells[i + 1].id = v.P.Name + "." + v.V.Name;
	}
	// 模板行
	tr0 = InitRowData(objTable, valArray.length);
	tr0.style.display = "none";
	for (var m = 0; m < valArray.length; m += 1) {
		var td = tr0.cells[m + 1];
		InitSetCell(uiClass, td, valArray[m].P, valArray[m].V, "");
	}
	// 数据行
	for (i = 0; i < tablesSet.Count(); i += 1) {
		var dataTables = tablesSet.GetItem(i);
		if (dataTables.IsDelete) {
			continue;
		}
		var tr = InitRowData(objTable, valArray.length);
		for (m = 0; m < valArray.length; m += 1) {
			var dataTable = dataTables.Tables.GetItem(valArray[m].P.Name);
			var vvvv = "";
			if (valArray[m].P.Name == 'DescriptionSet') {
				window.DT = dataTable;
				vvvv = dataTable.SearchValue('LANG=' + EWA.LANG, valArray[m].V.Name);
			} else {
				vvvv = dataTable.GetValue(valArray[m].V.Name)
			}
			var td = tr.cells[m + 1];
			InitSetCell(uiClass, td, valArray[m].P, valArray[m].V, vvvv);
		}
	}
	tbRepeat = new TableRepeat(objTable);
	tbRepeat.AddRow();
	_EWAC_TABLES_SET = tablesSet;
	_EWAC_XITEM_PARAMETERS = xItemParameters;

	// set default to span

	if (window.G_frameTag == 'ListFrame') {
		$('select[id="PAGE$Tag$Tag"]')[0].value = 'span';
	}

	$('input[id="PAGE$Name$Name"]').bind('blur', function () {
		var o = $(this.parentNode.parentNode.parentNode).find('input[id="PAGE$DataItem$DataField"]');
		if (o.length > 0 && (o.val() == "" || o.val() == null)) {
			o.val(this.value);
		}
	});
}

function InitRow(table, rowLength) {
	var tr = table.insertRow(-1);
	
	// 避免出现越来越长的问题
	var w = table.getAttribute('w');
	if(!w){
		w = (table.clientWidth - 20) / rowLength;
		table.setAttribute('w', w);
	} else {
		w = w * 1;
	}
	
	for (var i = 0; i < rowLength + 1; i += 1) {
		tr.insertCell(-1);
		if (i == 0) {
			tr.cells[i].width = 20;
		} else {
			tr.cells[i].width = w;
			tr.cells[i].innerHTML = "<SPAN></SPAN><SPAN></SPAN>";
		}
		if (table.rows.length > 1) {
			tr.cells[i].bgColor = "white";
		}
	}
	if (table.rows.length > 2) {
		tr.setAttribute("R_INDEX", table.rows.length - 3);
	}
	return tr;
}

function InitRowData(table, rowLength) {
	var tr = InitRow(table, rowLength);
	tr.cells[0].width = "20";
	tr.cells[0].innerHTML = "<input type=checkbox>";
	tr.cells[0].bgColor = "white";
	return tr;
}

function InitHeader(td, paraValue, lang) {
	td.innerHTML = "<b><nobr>" + paraValue.Name + "</nobr><br><nobr>(" + paraValue.Descriptions.GetDescription(lang).Info
		+ ")</nobr></b>";
	td.align = "center";
	td.style.color = 'darkblue';
	td = null;
}

/**
 * 打开窗口调用，调用来自IDE OpenDialog
 * 
 * @param args
 */
function InitSet(args) {
	var uiClass = args[1];
	var table = args[0];
	var xItemParameter = table.XItemParameter;
	var lang = EWA.LANG;
	_EWAC_UI = uiClass;
	console.log(table)
	_EWAC_TABLE = table;
	var objTable = args[2].GetIframeWindow().document.getElementById("tbMain");
	var vLen = xItemParameter.Values.Count();
	var childrenLen = xItemParameter.Children == null ? 0 : xItemParameter.Children.Count();
	var cellLen = vLen + childrenLen;
	// Header行
	var tr = InitRow(objTable, cellLen);
	for (var i = 0; i < vLen; i += 1) {
		var v = xItemParameter.Values.GetValue(i);
		InitHeader(tr.cells[i + 1], v, lang);
	}
	for (var i = 0; i < childrenLen; i += 1) {
		var childParameter = xItemParameter.Children.GetItem(i);
		var td = tr.insertCell(-1);
		InitHeader(tr.cells[vLen + i + 1], childParameter, lang);
	}
	tr = null;
	// 生成模板行
	var tr = InitRowData(objTable, cellLen);
	tr.style.display = "none";
	for (var k = 0; k < vLen; k += 1) {
		var value = xItemParameter.Values.GetValue(k);
		InitSetCell(uiClass, tr.cells[k + 1], xItemParameter, value, "");
	}
	for (var k = 0; k < childrenLen; k += 1) {
		var childParameter = xItemParameter.Children.GetItem(k);
		InitSetChildCell(uiClass, tr.cells[k + 1 + vLen], childParameter, xItemParameter);
	}

	// 生成数据行
	tr = null;
	for (var i = 0; i < table.Rows.length; i += 1) {
		if (table.Rows[i].IsDelete) {
			continue; // 已经删除，不显示
		}
		var tr = InitRowData(objTable, cellLen);
		tr.setAttribute("R_INDEX", i);// 数据行的排序
		for (var k = 0; k < vLen; k += 1) {
			var value = xItemParameter.Values.GetValue(k);

			var val = table.Rows[i].Fields[k];
			InitSetCell(uiClass, tr.cells[k + 1], xItemParameter, value, val);
		}
		for (var k = 0; k < childrenLen; k += 1) {
			var childParameter = xItemParameter.Children.GetItem(k);
			InitSetChildCell(uiClass, tr.cells[k + 1 + vLen], childParameter, xItemParameter, i);
		}
		tr = null;
	}
	tbRepeat = new TableRepeat(objTable);
	tbRepeat.AddRow();

}

function InitSetChildCell(uiClass, td, childParameter, parentParameter, rowIndex) {
	var lang = EWA.LANG;
	var s1 = childParameter.Name + "(" + childParameter.Descriptions.GetDescription(lang).Info + ")";
	var but = uiClass.CreateButton(parentParameter, null, childParameter, rowIndex);
	td.innerHTML = "<span>" + s1 + "</span>" + but;
}

function InitSetCell(uiClass, td, xItemParameter, value, val) {
	var obj = uiClass.CreateEdit(xItemParameter, value, val, document);
	td.childNodes[0].appendChild(obj);
	if (obj.tagName == "SELECT") {
		obj.style.width = td.width - 4 + "px";
	} else {
		obj.style.width = td.width - 2 - 34 + "px";
		var name = " P_NAME='" + xItemParameter.Name + "' V_NAME='" + value.Name + "' ";
		if (value.Name == 'HiddenFields') {
			td.childNodes[1].innerHTML = "<input style='width:25px;'" + name
				+ " type=button value='..' onclick='popHiddenFields(this.parentNode.parentNode.childNodes[0].childNodes[0]);' />";

		} else {
			td.childNodes[1].innerHTML = "<input style='width:25px;'"
				+ name
				+ " type=button value='..' onclick='_EWAC_UI.Edit(this,this.parentNode.parentNode.childNodes[0].childNodes[0]);' />";
		}
	}
	td = obj = null;
}

// -------------------------------------------------------------------------
var timer;
function Moninter() {
	tbRepeat.RepeatRow(tbRepeat._obj.rows[tbRepeat._obj.rows.length - 1]);
}

function TableRepeat(objTable) {
	this._obj = objTable;
	this._LastRow = this._obj.rows[this._obj.rows.length - 1];
	this._LastValue = this._LastRow.innerHTML;
	this.RepeatRow = function (obj) {
		while (obj.tagName != "TR") {
			obj = obj.parentNode;
		}
		if (obj != this._LastRow) {
			return;
		}
		var lastRow = this._obj.rows[this._obj.rows.length - 1];
		if (lastRow.innerHTML == this._LastValue) {
			return;
		}
		var rIndex;
		if (TYPE == 0) {
			_EWAC_TABLE.AddRow();
			rIndex = _EWAC_TABLE.Rows.length - 1;
		} else { // items repeat
			var tables = _EWAC_UI._UserClass.CreateItemTables(_EWAC_XITEM_PARAMETERS);
			tables.Name = "NEW" + Math.random();
			rIndex = _EWAC_TABLES_SET.AddObject(tables.Name, tables);
		}
		this._LastRow.setAttribute("R_INDEX", rIndex);
		this._LastRow.cells[0].innerHTML = this._obj.rows[1].cells[0].innerHTML;
		var objs = this._LastRow.getElementsByTagName("input");
		for (var i = 0; i < objs.length; i += 1) {
			objs[i].setAttribute("R_INDEX", rIndex);
		}
		this.AddRow();
	};
	this.AddRow = function () {
		var w = EWA.B.IE ? this._obj.ownerDocument.parentWindow : this._obj.ownerDocument.defaultView;
		w.clearInterval(w.timer);
		var tmpTr = this._obj.rows[1];
		tmpTr.parentNode.appendChild(tmpTr.cloneNode(true));
		var tr = this._obj.rows[this._obj.rows.length - 1];
		tr.style.display = "";
		for (var i = 0; i < tr.cells.length; i += 1) {
			var td = tr.cells[i];
			if (i == 0) {
				td.innerHTML = "<div align=center>*</div>";
			} else {
				this._AddOnBlur(td);
				td = null;
			}
		}
		var objs = tr.getElementsByTagName("select");
		for (var i = 0; i < objs.length; i += 1) {
			if (objs[i].getAttribute("EWA_TARGET") != null) {
				objs[i].onchange = function () {
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
		if (window.G_frameTag == 'ListFrame') {
			$(tr).find('select[id="PAGE$Tag$Tag"]:first()').val('span');
		}
		this._LastRow = tr;
		this._LastValue = this._LastRow.innerHTML;
		tr = null;
		w.timer = w.setInterval(w.Moninter, 200);

	};
	this._AddOnBlur = function (obj) {
		var objs = obj.getElementsByTagName("input");
		if (objs == null || objs.length == 0 || objs[0].type == "button") {
			return;
		}

		objs[0].onblur = function () {
			var oldVal = this.getAttribute("EWA_OLD_VALUE");
			if (oldVal != this.value) {
				this.setAttribute("EWA_VALUE", this.value);
				this.setAttribute("EWA_OLD_VALUE", this.value);
			}
			if (this.id == 'PAGE$Name$Name') {
				var o = $(this.parentNode.parentNode.parentNode).find('input[id="PAGE$DataItem$DataField"]');
				if (o.length > 0 && (o.val() == "" || o.val() == null)) {
					o.val(this.value);
				}
			}
		};
		objs = null;
	};
	this.DeleteRows = function () {
		for (var i = this._obj.rows.length - 1; i > 0; i -= 1) {
			if (this._obj.rows[i].cells[0].childNodes[0].checked) {
				this._obj.deleteRow(i);
			}
		}
	};
	this.RowUpDown = function (inc) {
		objTb = this._obj;
		var oTr;
		var iIndex;
		var ids = [];
		var isChecked = false;
		for (i = 2; i < objTb.rows.length - 1; i += 1) {
			tr = objTb.rows[i];
			if (tr.style.display == "none") { // 被删除的
				continue;
			}
			if (tr.cells[0].childNodes[0].checked) {
				ids.push([ tr, true, ids.length ]);
				isChecked = true;
			} else {
				ids.push([ tr, false, ids.length ]); // 获取未被删除的
			}
		}
		if (!isChecked) {
			return;
		}
		if (inc == -10) { // top
			var idx = 0;
			for (var i = 0; i < ids.length; i++) {
				var row = ids[i];
				if (!row[1]) {
					continue;
				}
				this._Swap(ids, i, idx);
				idx++;
			}
		} else if (inc == 10) { // bottom
			var idx = ids.length - 1;
			for (var i = ids.length - 1; i >= 0; i--) {
				var row = ids[i];
				if (!row[1]) {
					continue;
				}
				this._Swap(ids, i, idx);
				idx--;
			}
		} else if (inc == -1) {
			for (var i = 0; i < ids.length; i++) {
				var row = ids[i];
				if (!row[1]) {
					continue;
				}
				var idx = row[2] - 1;
				this._Swap(ids, i, idx);
			}
		} else if (inc == 1) {
			for (var i = ids.length - 1; i >= 0; i--) {
				var row = ids[i];
				if (!row[1]) {
					continue;
				}
				var idx = row[2] + 1;
				this._Swap(ids, i, idx);
			}
		}

	};
	this._Swap = function (ids, fromIdx, toIdx) {
		if (toIdx < 0 || toIdx > ids.length - 1) { // 不存在的序号
			return;
		}
		var from = ids[fromIdx];
		var to = ids[toIdx];

		if (from[0] != to[0]) {
			from[0].swapNode(to[0]);
			from[0].getElementsByTagName('input')[0].checked = true;
		}
		from[1] = false; // 被交换过了
		// 交换位置
		ids[fromIdx] = to;
		ids[toIdx] = from;
	}
}

// -------------------------------------------------------------------------
var itemLastFoucs;
function ItemBlur(obj) {
	var o1 = obj.parentNode.parentNode.cells[1];
	o1.bgColor = "white";
	o1.childNodes[0].style.color = "";
}

function ItemFocus(obj) {
	var o1 = obj.parentNode.parentNode.cells[1];
	o1.bgColor = "darkBlue";
	o1.childNodes[0].style.color = "white";
}

// --------------------------------------------------------------------------
if (!Node.prototype.swapNode) {
	Node.prototype.swapNode = function (node) {// 交换节点
		var p = this.parentNode;
		if (this.nextSibling != node) {
			p.insertBefore(this, node);
		} else {
			p.insertBefore(node, this);
		}
	}
}

// 打开选择隐含字段的列表
function popHiddenFields(obj) {
	var v = obj.title;
	var cnt = loadHiddenFields(v);
	window['ewa_hidden_obj'] = obj;
	
	/*ddd = EWA.UI.Dialog.Pop(cnt, obj, true);
	ddd.SetNewSize(420,240);
	ddd.Move(100, 20);
	ddd.SetCaption("选择参数");
	*/
	ddd = $DialogHtml(cnt, '选择参数');
	ddd.AutoSize();
}
function okHiddenFields(obj) {
	var ipts = obj.parentNode.getElementsByTagName('table')[0].getElementsByTagName('input');
	var ss = [];
	for (var i = 0; i < ipts.length; i++) {
		var ipt = ipts[i];
		if (ipt.checked) {
			ss.push(ipt.value);
		}
	}
	var s = ss.join(',');
	window['ewa_hidden_obj'].value = s;
	window['ewa_hidden_obj'].setAttribute('title', s);
	window['ewa_hidden_obj'].setAttribute('ewa_value', s);
	//ddd.CloseWindow();
	ddd.Close();
	
	window['ewa_hidden_obj'].focus();
}
// 创建隐含字段的列表
function loadHiddenFields(ref) {
	var map = {};
	var vv = ref.split(',');
	for (var i = 0; i < vv.length; i++) {
		var v = vv[i].trim().toUpperCase();
		map[v] = true;
	}
	EWA.OW.Load();
	var w = EWA.OW.PWin;
	// IDE的对象下拉框
	var opts = w.$X('EWAC_SELECT_TYPE').options;
	var ss = [];
	ss.push('<div><div style="width:410px;height:200px;overflow:auto"><table style="width:400px;">');
	for (var i = 0; i < opts.length; i++) {
		var opt = opts[i];
		if (opt.value.indexOf('Items$') != 0) {
			continue;
		}
		var v = opt.value.replace('Items$', '');
		let id =  'item_'+v;
		var chk = map[v.toUpperCase()] ? ' checked ' : '';
		var t = opt.text;
		ss.push('<tr><td width=20><input id="'+id+'" type=checkbox ' + chk + ' value="' + v + '"></td><td><label for="'+id+'">' + t + "</label></td></tr>");
	}
	ss.push("</table></div><hr><input type=button onclick='okHiddenFields(this)' value='确定'></div>");
	return ss.join('');
}