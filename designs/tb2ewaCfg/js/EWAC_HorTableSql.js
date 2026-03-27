var EWAC_HorTableSql = {
	getTableName : function (field) {
		var tbName;
		if (field.EWA_F_TYPE == 'INT') {
			tbName = "_EWA_HOR_VINT";
		} else if (field.EWA_F_TYPE == 'STR') {
			tbName = "_EWA_HOR_VSTR";
		} else if (field.EWA_F_TYPE == 'TXT') {
			tbName = "_EWA_HOR_VTXT";
		} else if (field.EWA_F_TYPE == 'DATE') {
			tbName = "_EWA_HOR_VDATE";
		} else if (field.EWA_F_TYPE == 'NUM') {
			tbName = "_EWA_HOR_VNUM";
		} else if (field.EWA_F_TYPE == 'BLOB') {
			tbName = "_EWA_HOR_VBLOB";
		} else {
			alert('未知的类型：' + field.EWA_F_TYPE);
		}
		return tbName;
	},
	// 获取存储过程名称
	getPrName : function (field) {
		var tbName;
		if (field.EWA_F_TYPE == 'INT') {// 整型
			tbName = "PR_EWA_HOR_INT";
		} else if (field.EWA_F_TYPE == 'STR') {// 长度200限制
			tbName = "PR_EWA_HOR_STR";
		} else if (field.EWA_F_TYPE == 'TXT') {// 不限长度字符串
			tbName = "PR_EWA_HOR_TXT";
		} else if (field.EWA_F_TYPE == 'DATE') {// 日期时间
			tbName = "PR_EWA_HOR_DATE";
		} else if (field.EWA_F_TYPE == 'NUM') {// 数字，sqlserver是money
			tbName = "PR_EWA_HOR_NUM";
		} else if (field.EWA_F_TYPE == 'BLOB') {// 二进制
			tbName = "PR_EWA_HOR_BLOB";
		} else {
			alert('未知的类型：' + field.EWA_F_TYPE);
		}
		return tbName;
	},
	/**
	 * 创建SELECT SQL
	 * 
	 * @param table
	 *            表
	 * @param fields
	 *            字段
	 * @param callback
	 *            回调函数
	 */
	createSqlSelect : function (table, fields, callback) {
		var ss_fields = [];
		var select = "SELECT A." + table.EWA_H_PKS + " AS EXT_KEY ";
		ss_fields.push(select);
		var ss = [ "\nFROM " + table.EWA_H_TABLE + " AS A " ];
		for ( var n in fields) {
			var field = fields[n];
			var tbName = this.getTableName(field);
			var s = "LEFT JOIN " + tbName + " as tb" + field.EWA_F_NAME + " ON tb" + field.EWA_F_NAME + ".EWA_F_ID = '" + field.EWA_F_ID + "'\n\t AND A."
					+ table.EWA_H_PKS + " = tb" + field.EWA_F_NAME + ".EWA_F_KEY0";
			ss.push(s);
			// 字段名称
			var s_field = "\t, tb" + field.EWA_F_NAME + ".EWA_V as " + field.EWA_F_NAME;
			ss_fields.push(s_field);
		}
		var sql = ss_fields.join("\n") + ss.join("\n");
		if (callback) {
			callback(sql, table, fields);
			return;
		}
		this.showSqlDialog(sql, 'SELECT');
	},
	createSqlUpdate : function (table, fields, callback) {
		var ss = [];
		for ( var n in fields) {
			var field = fields[n];

			// var sql = this.createSqlUpdate0(table, field);
			var memo = this.createSqlMemo(field);
			ss.push(memo)
			var sql = this.createProcdure(table, field);
			ss.push(sql);

			ss.push("");
		}
		var sql = ss.join("\n");
		if (callback) {
			callback(sql, table, fields);
			return;
		}
		this.showSqlDialog(sql, 'UPDATE');
	},
	createSqlInsert : function (table, fields, callback) {
		var ss = [];
		for ( var n in fields) {
			var field = fields[n];
			var memo = this.createSqlMemo(field);
			ss.push(memo)
			// var sql = this.createSqlInsert0(table, field);
			var sql = this.createProcdure(table, field);
			ss.push(sql);
			ss.push("");
		}
		var sql = ss.join("\n");
		if (callback) {
			callback(sql, table, fields);
			return;
		}
		this.showSqlDialog(sql, 'INSERT');
	},
	createProcdure : function (table, field) {
		var prName = this.getPrName(field);
		var s = "CALL " + prName + " ('" + field.EWA_F_ID + "', @" + table.EWA_H_PKS + ", @" + field.EWA_F_NAME;
		if (prName == "PR_EWA_HOR_BLOB" || prName == "PR_EWA_HOR_STR" || prName == "PR_EWA_HOR_TXT") {
			// 传递值的hash，进行比较用
			s += ", @" + field.EWA_F_NAME + ".hash";
		}
		s += ", @G_SUP_ID, @G_ADM_ID);"
		return s;
	},
	createSqlInsert0 : function (table, field) {
		var tbName = this.getTableName(field);
		var s = "INSERT INTO " + tbName + " (EWA_F_ID, EWA_F_KEY0, EWA_V)\n SELECT '" + field.EWA_F_ID + "', A." + table.EWA_H_PKS + ", @" + field.EWA_F_NAME
				+ "\nFROM " + table.EWA_H_TABLE + " A WHERE A." + table.EWA_H_PKS + " = @" + "SYS_UNID;";
		return s;
	},
	createSqlMemo : function (field) {
		var memo = "-- " + field.EWA_F_NAME + ", " + field.EWA_F_DES.replace(/;/ig, '').replace(/\n/ig, ' ') + ", " + field.EWA_F_TYPE + "";
		return memo;
	},

	createSqlUpdate0 : function (table, field) {
		var tbName = this.getTableName(field);
		var where = " WHERE EWA_F_ID='" + field.EWA_F_ID + "'\n\tAND EWA_F_KEY0 = @" + table.EWA_H_PKS;

		var ss = [];
		var select = "SELECT EWA_F_ID AS TMP_" + field.EWA_F_NAME + " FROM " + tbName + where + ";";
		ss.push(select);

		var memo = this.createSqlMemo(field);
		ss.push(memo)
		var update = "UPDATE " + tbName + " SET EWA_V=@" + field.EWA_F_NAME + " WHERE EWA_F_ID='" + field.EWA_F_ID + "' AND EWA_F_KEY0 = @" + table.EWA_H_PKS
				+ "\n\tAND @" + "TMP_" + field.EWA_F_NAME + " IS NOT NULL;";
		ss.push(update);

		var insert = this.createSqlInsert0(table, field);
		insert = insert.replace("@" + "SYS_UNID;", "@" + table.EWA_H_PKS + "\n\tAND @" + "TMP_" + field.EWA_F_NAME + " IS NULL;");
		ss.push(insert);

		return ss.join("\n");
	},
	showSqlDialog : function (sql, title) {
		var id = EWA_Utils.tempId("sql")
		var html = "<div  id='" + id + "'><div style='width:700px;height:300px;padding:10px'></div><textarea></textarea></div>";
		var sqlColor = cmclass.MarkSql(sql);

		$DialogHtml(html, title, 900, 300);

		$('#' + id + " textarea").val(sql);
		$('#' + id + " div").html(sqlColor);
		$('#' + id + " div p").css('margin', 0).css('font-size', 11).css('font-family', 'monospace');

		$('#' + id + " textarea")[0].select();
		document.execCommand("copy");
		$Tip('Copyed');
		$('#' + id + " textarea").remove();
	}
};