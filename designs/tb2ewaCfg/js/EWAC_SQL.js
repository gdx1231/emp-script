function EWAC_SqlCreator() {
	this._TableName;
	this._Fields = new Array();
	this._ClassName;
	this._HtmlTable = null;
	this.IsCopy = true;
	this.isCreateSwaggerAnnotations = false;
	/**
	 * 获取表HTML对象
	 * 
	 * @return {nsiDomElement}
	 */
	this.GetHtmlTable = function() {
		if (this._HtmlTable != null) {
			return this._HtmlTable;
		}

		var o = document.createElement("table");
		o.style.border = '1px solid gray';
		o.style.fontSize = '10px';
		o.style.fontFamily = 'arial';
		o.style.backgroundColor = '#ddd';
		o.cellSpacing = 1;
		o.cellPadding = 3;
		o.onselectstart = function() {
			return false;
		}
		o.id = 'TABLE#' + this._TableName;

		var tr = o.insertRow(-1);
		var td = tr.insertCell(-1);
		td.colSpan = 4;
		td.innerHTML = this._TableName;
		td.style.backgroundColor = 'darkblue';
		td.style.color = '#fff'
		td.style.cursor = 'pointer';
		td.style.fontSize = '14px';
		td.style.fontWeight = 'bold';

		for (var i = 0; i < this._Fields.length; i++) {
			var tr = o.insertRow(-1);

			tr.id = 'ROW#' + this._TableName + "#" + this._Fields[i].Name;

			var td0 = tr.insertCell(-1);
			td0.style.backgroundColor = '#fff';
			td0.innerHTML = '<input type=checkbox id="' + this._Fields[i].Name
				+ '">'

			var td1 = tr.insertCell(-1);
			td1.style.backgroundColor = '#fff';
			td1.innerHTML = '<nobr>' + this._Fields[i].Name;

			var td2 = tr.insertCell(-1);
			td2.style.backgroundColor = '#fff';
			td2.innerHTML = '<nobr>' + this._Fields[i].Type;

			var td3 = tr.insertCell(-1);
			td3.style.backgroundColor = '#fff';
			td3.innerHTML = '<nobr>' + this._Fields[i].Description;

		}
		this._HtmlTable = o;
		return o;
	}
	this.LoadFromJson = function(json) {
		// {"charoctetlength": "6", "columnsize": "6", "databasetype": "char",
		// "datatype": "1", "decimaldigits": "0", "description": "代码", "fk":
		// "false", "fkcolumnname": "", "fktablename": "", "identity": "false",
		// "maplength": "0", "maxlength": "6", "name": "CODE", "null": "false",
		// "ordinalposition": "0", "pk": "true", "tablename": "BAS_CEEB"},
		for (var i = 0; i < json.length; i++) {
			var f = json[i];
			if (i == 0) {
				this._TableName = f['tablename'];
			}
			var field = {
				Name: f.name,
				Type: f.databasetype,
				Description: f.description,
				IsPk: f.pk == 'true' ? true : false
			};
			this._InitField(field);
			this._Fields.push(field);
		}
	}
	this.Init = function(name, fields) {
		this._TableName = name;
		for (var i = 0; i < fields.Count(); i++) {
			this._InitField(fields.GetItem(i));
			this._Fields.push(fields.GetItem(i));
		}
		this._ClassName = CreateClassName(this._TableName);
	};
	this._InitField = function(f) {
		f.Class = {};
		// 字段的类名称
		f.Class.Name = CreateClassName(f.Name);

		var private_field_name = f.Class.Name.substring(0, 1).toLowerCase()
			+ f.Class.Name.substring(1) + "_";
		f.Class.PrivateName = private_field_name;
		// 字段的类型
		f.Class.Type = CreateType(f.Type);
		// 字段的类备注
		f.Class.Comment = "\t*@param " + f.ClassName + " " + f.Description;
		//
		f.Class.RequestValue = "rv.addValue(\"" + f.Name.toUpperCase()
			+ "\", para.get" + f.Class.Name + "(), \"" + f.Class.Type
			+ "\", " + f.Length + ");";
		f.Class.RequestValue1 = "rv.addValue(\"" + f.Name.toUpperCase()
			+ "\", para" + f.Class.Name + ", \"" + f.Class.Type + "\", "
			+ f.Length + ");";
		let des = f.Description + ', ' + f.Type  + ', length:' + f.Length 
			+ ', null:' + !f.MustInput + ', pk:' + f.IsPk;
		f.Class.des = des;
		var fjson = ["if(json.has(\"" + f.Name + "\"){\n o.set" + f.Class.Name + "("];

		if (f.ClassType == 'Integer') {
			fjson.push("json.optInt(\"" + f.Name + "\")");
		} else if (f.ClassType == 'Double') {
			fjson.push("json.optDouble(\"" + f.Name + "\")");
		} else if (f.ClassType == 'Date') {
			fjson.push("Utils.getDate(json.optString(\"" + f.Name + "\"))");

		} else {
			fjson.push("json.optString(\"" + f.Name + "\")");
		}
		fjson.push(");}");
		f.Class.fromJson = fjson.join('\n');
	};
	this.CreateClass = function() {
		var s0 = [];
		s0.push("import java.util.*;");
		s0.push("import com.gdxsoft.easyweb.datasource.ClassBase;");

		// UInt32, UInt64, UInt16
		for (var i = 0; i < this._Fields.length; i++) {
			var f = this._Fields[i];
			var fc = f.Class;
			if (fc.Type.indexOf("UInt") == 0) {
				s0.push("import  com.gdxsoft.easyweb.utils.types.*;");
				break;
			}
		}

		if (this.isCreateSwaggerAnnotations) {
			s0.push("import io.swagger.annotations.ApiModel;");
			s0.push("import io.swagger.annotations.ApiModelProperty;");
		}


		s0.push("/**表" + this._TableName + "映射类");
		s0.push("* @author gdx 时间：" + new Date() + "*/");
		if (this.isCreateSwaggerAnnotations) {
			s0.push('@ApiModel(value = "' + this._TableName + '", description = "表' + this._TableName + '映射类")')
		}

		s0.push("public class " + this._ClassName + " extends ClassBase{");

		var s2 = []; // 类声明
		var s3 = [];
		for (var i = 0; i < this._Fields.length; i++) {
			var f = this._Fields[i];
			var fc = f.Class;
			var private_field_name = fc.PrivateName;
			s2.push("// "	+ fc.des);
			s2.push("private " + fc.Type + " " + private_field_name + ";");
			// get
			s3.push("\n\n/**\n * 获取 " + f.Description + "\n *");
			s3.push("* @return " + fc.des + "\n" + "*/");
			if (this.isCreateSwaggerAnnotations) {
				let swaggerAnnotation = this.createFieldSwaggerAnnotation(f);
				s3.push(swaggerAnnotation);
			}
			s3.push("public " + fc.Type + " get" + fc.Name + "() {return this."
				+ private_field_name + ";}");

			// set
			s3.push("/**\n" + "* 赋值 " + f.Description);
			s3.push("\n* @param para" + fc.Name + "\n" + "* " + fc.des + "\n */\n");
			s3.push("public void set" + fc.Name + "(" + fc.Type + " para"
				+ fc.Name + "){");
			s3.push('  super.recordChanged("' + f.Name + '", this.'
				+ private_field_name + ', para' + fc.Name + ');');
			s3.push("  this." + private_field_name + " = para" + fc.Name + ";");
			s3.push("}");
		}
		var s4 = this.CreateFieldValue();
		s1 = s0.join("\n") + s2.join("\n") + s3.join('\n') + s4 + "\n}";
		copyToClipboard(s1);
		return s1;
	};
	this.createFieldSwaggerAnnotation = function(f) {
		let s = '@ApiModelProperty(value = "' + f.Description + '", required = ' + (f.MustInput == 1) + ')';
		return s;
	};
	this.CreateFieldValue = function() {
		var s1 = ["/**根据字段名称获取值，如果名称为空或字段未找到，返回空值", " @param filedName 字段名称",
			" @return 字段值*/", "public Object getField(String filedName){"];
		s1.push("if(filedName == null){ return null; }");
		s1.push("String n=filedName.trim().toUpperCase();");
		for (var i = 0; i < this._Fields.length; i++) {
			var f = this._Fields[i];
			var fc = f.Class;
			s1.push('if(n.equalsIgnoreCase("' + f.Name + '")){return this.'
				+ fc.PrivateName + ";}");
		}
		s1.push("return null;}");
		s1 = [];//去除了
		return s1.join("\n");
	};
	this.CreateClassDao = function() {
		var ins = this.CreateSqlInsert().replace(/\r\n/ig, " ");
		var del = this.CreateSqlDelete();
		var upd = this.CreateSqlUpdate().replace(/\r\n/ig, " ").replace(/  /ig,
			" ");
		var sel = this.CreateSqlSelect();

		var rv = ["public RequestValue createRequestValue(" + this._ClassName
			+ " para){"];
		rv.push("RequestValue rv = new RequestValue();");
		var fieldList = [];
		var pks = [];
		for (var i = 0; i < this._Fields.length; i++) {
			var f = this._Fields[i];
			var fc = f.Class;
			rv.push(fc.RequestValue + " // " + fc.des);
			//IsFk: false, IsIdentity: 0, IsPk: false, Length: 50, MustInput: 1
			fieldList.push('"' + f.Name + '" /* ' + fc.des + ' */');
			if (f.IsPk) {
				pks.push("\"" + f.Name + "\" /* " + fc.des + " */");
			}
		}
		rv.push("return rv;\n\t}");
		rv = []; //不用了

		var s1 = [];
		s1.push("import java.util.*;");
		s1.push("import com.gdxsoft.easyweb.script.RequestValue;");
		s1.push("import com.gdxsoft.easyweb.datasource.IClassDao;");
		s1.push("import com.gdxsoft.easyweb.datasource.ClassDaoBase;");

		// UInt32, UInt64, UInt16
		for (var i = 0; i < this._Fields.length; i++) {
			var f = this._Fields[i];
			var fc = f.Class;
			if (fc.Type.indexOf("UInt") == 0) {
				s1.push("import  com.gdxsoft.easyweb.utils.types.*;");
				break;
			}
		}

		s1.push("/** " + this._TableName + "");
		s1.push("* @author gdx date: " + new Date() + " */");
		s1.push("public class " + this._ClassName + "Dao extends ClassDaoBase<"
			+ this._ClassName + "> implements IClassDao<" + this._ClassName
			+ ">{");
		// s1.push(" private static String SQL_SELECT=\"" + sel + "\";");
		// s1.push(" private static String SQL_UPDATE=\"" + upd + "\";");
		// s1.push(" private static String SQL_DELETE=\"" + del + "\";");
		// s1.push(" private static String SQL_INSERT=\"" + ins + "\";");

		s1.push(" public final static String TABLE_NAME =\"" + this._TableName + "\";");
		s1.push(" public final static String[] KEY_LIST = { " + pks.join(", ") + "   };");
		s1.push(" public final static String[] FIELD_LIST = { " + fieldList.join(", ") + " };");
		s1.push(" public " + this._ClassName + "Dao(){ ");
		s1.push("   // 设置数据库连接配置名称，在 ewa_conf.xml中定义");
		s1.push("   // super.setConfigName(\"" + cfg + "\");");
		// 2020-07-11 为了 parseFromDTTable
		s1.push("   super.setInstanceClass(" + this._ClassName + ".class);");
		s1.push("   super.setTableName(TABLE_NAME);");
		s1.push("   super.setFields(FIELD_LIST);");
		s1.push("   super.setKeyFields(KEY_LIST);");
		// 自增字段
		if (this._AutoIncrementField) {
			s1.push("   // 自增字段 ");
			s1.push('   super.setAutoKey("' + this._AutoIncrementField.Name + '");');
		}
		s1.push(" }");

		var newRecord = [];
		newRecord.push("/**");
		newRecord.push(" * 生成一条记录");
		newRecord.push(" * @param para  表" + this._TableName + "的映射类");
		newRecord.push(" * @return true/false");
		newRecord.push(" */");
		newRecord.push("public boolean newRecord(" + this._ClassName + " para){");
		newRecord.push("	Map<String, Boolean> updateFields = super.createAllUpdateFields(FIELD_LIST);");
		newRecord.push("	return this.newRecord(para, updateFields);");
		newRecord.push("}");

		newRecord.push("/**");
		newRecord.push(" * 生成一条记录");
		newRecord.push(" * @param para 表" + this._TableName + "的映射类");
		newRecord.push(" * @param updateFields 变化的字段Map");
		newRecord.push(" * @return true/false");
		newRecord.push(" */");
		newRecord.push("public boolean newRecord(" + this._ClassName + " para, Map<String, Boolean> updateFields){");
		newRecord.push("  String sql = super.sqlInsertChanged(TABLE_NAME, updateFields, para);");
		newRecord.push("  if (sql == null) { //没有可更新数据");
		newRecord.push("  	return false;");
		newRecord.push("  }");
		newRecord.push("  RequestValue rv = this.createRequestValue(para);");
		if (this._AutoIncrementField) {
			let auto = this._createAutoIncrement(this._AutoIncrementField);
			newRecord.push(auto);
		} else {
			newRecord.push("  return super.executeUpdate(sql, rv);");
		}
		newRecord.push("}");

		var updRecord = ["/**", " * 更新一条记录，全字段",
			"*@param para 表" + this._TableName + "的映射类"];
		updRecord.push("\t *@return 是否成功 \n\t */");
		updRecord.push("public boolean updateRecord(" + this._ClassName + " para){\n");
		updRecord.push("  Map<String, Boolean> updateFields = super.createAllUpdateFields(FIELD_LIST);");
		updRecord.push("  return updateRecord(para, updateFields);");
		updRecord.push("}");

		updRecord.push("/**");
		updRecord.push(" * 更新一条记录，根据类的字段变化");
		updRecord.push(" * ");
		updRecord.push(" * @param para");
		updRecord.push(" *            表" + this._TableName + "的映射类");
		updRecord.push(" * @param updateFields");
		updRecord.push(" *            变化的字段Map");
		updRecord.push(" * @return");
		updRecord.push(" */");
		updRecord.push("public boolean updateRecord(" + this._ClassName + " para, Map<String, Boolean> updateFields){");
		updRecord.push("  // 没定义主键的话不能更新");
		updRecord.push("  if(KEY_LIST.length == 0){return false; } ");
		updRecord.push("  String sql = super.sqlUpdateChanged(TABLE_NAME, KEY_LIST, updateFields);");
		updRecord.push("  if (sql == null) { //没有可更新数据");
		updRecord.push("  	return false;");
		updRecord.push("  }");
		updRecord.push("  RequestValue rv = this.createRequestValue(para);");
		updRecord.push("  return super.executeUpdate(sql, rv);");
		updRecord.push("}");

		var sss = s1.join("\n") + newRecord.join("\n") + updRecord.join("\n")
			+ this.CreateClassGetRecord() + this.CreateClassGetRecords()
			+ this.CreateClassDeleteRecord() + rv.join("\n") + "}";
		copyToClipboard(sss);
		return sss;
	};
	this._createAutoIncrement = function(field) {
		let newRecord = [];
		let isUInt = false;
		if (field.Class.Type == 'UInt32') { //长整型
			isUInt = true;
			newRecord
				.push("UInt32 autoKey = super.executeUpdateAutoIncrementUInt32(sql, rv);");
		} else if (field.Class.Type == 'UInt64') { //长整型
			isUInt = true;
			newRecord
				.push("UInt64 autoKey = super.executeUpdateAutoIncrementUInt64(sql, rv);");
		} else if (field.Class.Type == 'UInt16') { //长整型
			isUInt = true;
			newRecord
				.push("UInt16 autoKey = super.executeUpdateAutoIncrementUInt16(sql, rv);");
		} else if (field.Class.Type == 'Long') { //长整型
			newRecord
				.push("long autoKey = super.executeUpdateAutoIncrementLong(sql, rv);");
		} else {
			newRecord
				.push("int autoKey = super.executeUpdateAutoIncrement(sql, rv);");
		}
		if (isUInt) {
			newRecord.push("if (autoKey.doubleValue() > 0) {");
		} else {
			newRecord.push("if (autoKey > 0) {");
		}
		newRecord.push("para.set" + field.Class.Name + "(autoKey);");
		newRecord.push("	return true;");
		newRecord.push("} else {");
		newRecord.push("	return false;");
		newRecord.push("}");
		return newRecord.join("\n")
	};
	this.CreateClassDaoMapFromJson = function() {
		for (var n in this._Fields) {
			var f = this._Fields[n];
			var fc = f.Class;
		}
	};
	this.CreateClassDeleteRecord = function() {
		var s1 = "public boolean deleteRecord(";
		var para = "";
		var rv = "";
		var m = 0;
		var comment = "/**\n\t * 根据主键删除一条记录\n";
		for (var i = 0; i < this._Fields.length; i++) {
			if (!this._Fields[i].IsPk) {
				continue;
			}
			var f = this._Fields[i];
			var fc = f.Class;
			if (m == 0) {
				m = 1;
			} else {
				para += ", ";
			}
			para += fc.Type + " para" + fc.Name;
			rv += fc.RequestValue1 + "\n";
			comment += "\t*@param para" + fc.Name + " " + f.Description + "\n";
		}
		comment += "\t*@return \u662f\u5426\u6210\u529f\n\t*/\n";
		s1 += para + "){\n";
		s1 += "\tRequestValue rv = new RequestValue();\n";
		s1 += rv;
		s1 += "\treturn super.executeUpdate(super.createDeleteSql() , rv);\n";
		s1 += "}\n";
		return comment + s1;
	};
	this.CreateClassGetRecords = function() {
		var comment = "/**\n\t * \u6839\u636e\u67e5\u8be2\u6761\u4ef6\u8fd4\u56de\u591a\u6761\u8bb0\u5f55\uff08\u9650\u5236\u4e3a500\u6761\uff09\n";
		comment += "\t*@param  whereString \u67e5\u8be2\u6761\u4ef6\uff0c\u6ce8\u610f\u8fc7\u6ee4\u201c'\u201d\u7b26\u53f7\uff0c\u907f\u514dSQL\u6ce8\u5165\u653b\u51fb\n";
		comment += "\t*@return \u8bb0\u5f55\u96c6\u5408\n\t*/\n";

		var s1 = "\tpublic ArrayList<" + this._ClassName;
		s1 += "> getRecords(String whereString){\n";
		s1 += "\t\tString sql=\"SELECT * FROM " + this._TableName
			+ " WHERE \" + whereString;\n";
		s1 += "\t\treturn super.executeQuery(sql, new " + this._ClassName
			+ "(), FIELD_LIST);\n";
		s1 += "\t}\n";

		var comment2 = "/**\n\t * \u6839\u636e\u67e5\u8be2\u6761\u4ef6\u8fd4\u56de\u591a\u6761\u8bb0\u5f55\uff08\u9650\u5236\u4e3a500\u6761\uff09\n";
		comment2 += "\t*@param  whereString \u67e5\u8be2\u6761\u4ef6\uff0c\u6ce8\u610f\u8fc7\u6ee4\u201c'\u201d\u7b26\u53f7\uff0c\u907f\u514dSQL\u6ce8\u5165\u653b\u51fb\n";
		comment2 += "\t*@param  fields 指定返回的字段\n";
		comment2 += "\t*@return \u8bb0\u5f55\u96c6\u5408\n\t*/\n";

		var s3 = "\tpublic ArrayList<" + this._ClassName;
		s3 += "> getRecords(String whereString, List<String> fields){\n";
		s3 += "\t\tString sql = super.createSelectSql(TABLE_NAME, whereString, fields);\n";
		s3 += "		String[] arrFields = new String[fields.size()];\n";
		s3 += "		arrFields = fields.toArray(arrFields);\n";
		s3 += "\t\treturn super.executeQuery(sql, new " + this._ClassName
			+ "(), arrFields);\n";
		s3 += "\t}\n";

		var comment1 = "/**\n\t * \u6839\u636e\u67e5\u8be2\u6761\u4ef6\u8fd4\u56de\u591a\u6761\u8bb0\u5f55\uff08\u9650\u5236\u4e3a500\u6761\uff09\n";
		comment1 += "\t*@param  whereString \u67e5\u8be2\u6761\u4ef6\uff0c\u6ce8\u610f\u8fc7\u6ee4\u201c'\u201d\u7b26\u53f7\uff0c\u907f\u514dSQL\u6ce8\u5165\u653b\u51fb\n";
		comment1 += "\t* @param pkFieldName   \u4e3b\u952e\n";
		comment1 += "\t* @param pageSize      \u6bcf\u9875\u8bb0\u5f55\u6570\n";
		comment1 += "\t* @param currentPage   \u5f53\u524d\u9875\n";
		comment1 += "\t*@return \u8bb0\u5f55\u96c6\u5408\n\t*/\n";
		var s2 = "\tpublic ArrayList<"
			+ this._ClassName
			+ "> getRecords(String whereString, String pkFieldName, int pageSize, int currentPage){\n";
		s2 += "\t\tString sql=\"SELECT * FROM " + this._TableName
			+ " WHERE \" + whereString;\n";
		s2 += "\t\treturn super.executeQuery(sql, new " + this._ClassName
			+ "(), FIELD_LIST, pkFieldName, pageSize, currentPage);\n";
		s2 += "\t}\n";

		// return comment + s1 + comment2 + s3 + comment1 + s2;
		return "";
	};
	this.CreateClassGetRecord = function() {
		var s1 = "public " + this._ClassName + " getRecord(";
		var para = "";
		var rv = "";
		var m = 0;
		var comment = "/**\n\t * \u6839\u636e\u4e3b\u952e\u8fd4\u56de\u4e00\u6761\u8bb0\u5f55\n";
		for (var i = 0; i < this._Fields.length; i++) {
			if (!this._Fields[i].IsPk) {
				continue;
			}
			var f = this._Fields[i];
			var fc = f.Class;
			if (m == 0) {
				m = 1;
			} else {
				para += ", ";
			}
			para += fc.Type + " para" + fc.Name;
			rv += fc.RequestValue1 + "\n";
			comment += "\t*@param para" + fc.Name + " " + f.Description + "\n";
		}
		comment += "\t*@return \u8bb0\u5f55\u7c7b(" + this._ClassName
			+ ")\n\t*/\n";
		s1 += para + "){\n";
		s1 += "\tRequestValue rv = new RequestValue();\n";
		s1 += rv;
		s1 += "	String sql = super.getSqlSelect();\n";
		s1 += "ArrayList<" + this._ClassName
			+ "> al = super.executeQuery(sql, rv, new ";
		s1 += this._ClassName + "(), FIELD_LIST);\n";
		s1 += "if(al.size()>0){\n";
		s1 += this._ClassName + " o = al.get(0);\nal.clear();\n";
		s1 += "return o;\n}else{\nreturn null;\n}\n";
		s1 += "}\n";
		return comment + s1;
	};
	this.CreateSqlInsert = function() {
		var s1 = "INSERT INTO " + this._TableName + "(";
		var s2 = "VALUES(";
		var inc = 0;
		for (var i = 0; i < this._Fields.length; i++) {
			var field = this._Fields[i];
			if (this.CheckIsAutoIncrement(field)) {
				// 自增字段
				continue;
			}
			if (inc == 0) {
				s1 += field.Name;
				s2 += "@" + field.Name;
			} else {
				s1 += ", " + field.Name;
				s2 += ", @" + field.Name;
			}
			inc++;
		}
		s1 += ")\r\n\t" + s2 + ")";
		if (this.IsCopy) {
			copyToClipboard(s1);
		}
		return s1;
	};

	/**
	 * 测试是否为自增字段
	 */
	this.CheckIsAutoIncrement = function(field) {
		// sqlserver ,老方法
		var is_auto = field.Type.indexOf('identity') > 0;

		// 新方法 2018-08-01 郭磊
		if (field.IsIdentity) {
			console.log(field)
			is_auto = true;
		}

		if (is_auto) {
			this._AutoIncrementField = field;
		}
		return is_auto;
	};

	/**
	 * 生成 SELECT表达式，保护字段名称
	 * 
	 * @return {}
	 */
	this.CreateSqlSelect1 = function() {
		var s = [];
		s.push("SELECT ")
		for (var i = 0; i < this._Fields.length; i++) {
			if (i == 0) {
				s.push(this._Fields[i].Name);
			} else {
				s.push(", " + this._Fields[i].Name);
			}
		}
		s.push(" FROM " + this._TableName + " WHERE " + this.CreateSqlPk());
		if (this.IsCopy) {
			copyToClipboard(s.join(""));
		}
		return s.join("");
	};

	/**
	 * 生成 SELECT表达式，不包含字段名称
	 * 
	 * @return {}
	 */
	this.CreateSqlSelect = function() {
		var s1 = "SELECT * FROM " + this._TableName + " WHERE "
			+ this.CreateSqlPk();
		if (this.IsCopy) {
			copyToClipboard(s1);
		}
		return s1;
	};
	this.CreateSqlUpdate = function() {
		var s1 = "UPDATE " + this._TableName + " SET\r\n";
		var m = 0;
		for (var i = 0; i < this._Fields.length; i++) {
			if (this._Fields[i].IsPk) {
				continue;
			}
			if (m == 0) {
				s1 += "\t " + this._Fields[i].Name + " = @"
					+ this._Fields[i].Name;
				m = 1;
			} else {
				s1 += ',\r\n\t ' + this._Fields[i].Name + ' = @'
					+ this._Fields[i].Name;
			}
		}
		s1 += "\r\nWHERE " + this.CreateSqlPk();
		if (this.IsCopy) {
			copyToClipboard(s1);
		}
		return s1;
	};
	this.CreateSqlDelete = function() {
		var s1 = "DELETE FROM " + this._TableName + " WHERE "
			+ this.CreateSqlPk();
		if (this.IsCopy) {
			copyToClipboard(s1);
		}
		return s1;
	};
	this.CreateSqlPk = function() {
		var pk = "";
		var m = 0;
		for (var i = 0; i < this._Fields.length; i++) {
			if (this._Fields[i].IsPk) {
				if (m == 0) {
					pk = this._Fields[i].Name + "=@" + this._Fields[i].Name;
					m = 1;
				} else {
					pk += " AND " + this._Fields[i].Name + "=@"
						+ this._Fields[i].Name;
				}
			}
		}
		if (pk == "") {
			pk = " 1>2";
		}
		return pk;
	};
}
function CreateClassName(s1) {
	var s2 = s1.split("_");
	var s3 = "";
	for (var i = 0; i < s2.length; i++) {
		s3 += CreateClassWord(s2[i]);
	}
	return s3;
}
function CreateClassParaName(s1) {
	var s2 = CreateClassName(s1);
	return s2.substring(0, 1).toLowerCase() + s2.substring(1, 1000);
}
function CreateClassWord(s1) {
	return s1.substring(0, 1).toUpperCase()
		+ s1.substring(1, 1000).toLowerCase();
}
function CreateType(s1) {
	s1 = s1.toLowerCase();
	if (s1 == 'bigint' || s1 == 'lang') {
		return 'Long';
	} else if (s1 == "smallint") {//UNSIGNED
		return "Short";
	} else if (s1 == "tinyint") {//UNSIGNED
		return "Byte";
	} else if (s1 == "smallint unsigned") {//UNSIGNED
		return "UInt16";
	} else if (s1 == "int unsigned") {//UNSIGNED
		return "UInt32";
	} else if (s1 == "bigint unsigned") {//UNSIGNED
		return "UInt64";
	} else if (s1.indexOf("int") >= 0) {
		return "Integer";
	} else if (s1.indexOf("decimal") >= 0 || s1.indexOf("num") >= 0
		|| s1.indexOf("money") >= 0 || s1.indexOf('float') >= 0
		|| s1.indexOf('double') >= 0) {
		return "Double";
	} else if (s1.indexOf("date") >= 0 || s1.indexOf("time") >= 0) {
		return "Date";
	} else if (s1.indexOf("bin") >= 0 || s1.indexOf("blob") >= 0
		|| s1.indexOf("image") >= 0) {
		return "byte[]";
	}  else if (s1 == "bit" || s1.indexOf("bool") >= 0) {
		return "Boolean";
	} else {
		return "String";
	}
}
// -------------------
function copyToClipboard(text) {
	let o = document.createElement('textarea');
	document.body.appendChild(o);
	o.value = text;
	o.select();
	document.execCommand('copy');
	$Tip('Copyed');
	document.body.removeChild(o);
}
