var EWAC_DDL = {
	init : function() {
		this.map = {};
		this.lst = [];
		for ( var n in ddls) {
			var d = ddls[n];
			var key = d.CallXmlName + "," + d.CallItemName;
			if (key.indexOf('|') != 0) {
				key = '|' + key;
			}
			key = key.toUpperCase();
			if (this.map[key]) {
				this.map[key].push(d);
			} else {
				this.map[key] = [ d ];
				this.lst.push(key);
			}
		}
		this.lst.sort();
		var ss = [
				"<table id='EWA_LF_EWA_C_DDL' cellpadding=1 cellspacing=1 class='EWA_TABLE ewa-lf-frame' style='background:#333' width=100%><tr><th class='EWA_TD_H'>CallXmlName</th>",
				"<th class='EWA_TD_H'>CallItemName</th>", "<th class='EWA_TD_H'>DlsShow</th>",
				"<th class='EWA_TD_H'>DlsAction</th>", "<th class='EWA_TD_H'>CallPara</th>",
				"<th class='EWA_TD_H'>Use it!</th></tr>" ];
		for ( var n in this.lst) {
			var arr = this.map[this.lst[n]];
			for ( var m in arr) {
				var d = arr[m];
				ss
					.push("<tr onmouseover=\"EWA.F.FOS[&quot;EWA_C_DDL&quot;].MOver(this,event);\" onmousedown=\"EWA.F.FOS[&quot;EWA_C_DDL&quot;].MDown(this,event);\" class=\"ewa-lf-data-row\"><td class='EWA_TD_M'>");
				ss.push(d.CallXmlName);
				ss.push("</td><td class='EWA_TD_M'>");
				ss.push(d.CallItemName);
				ss.push("</td><td class='EWA_TD_M'>");
				ss.push(d.DlsShow);
				ss.push("</td><td class='EWA_TD_M'>");
				ss.push(d.DlsAction);
				ss.push("</td><td class='EWA_TD_M'>");
				ss.push(d.CallPara);
				ss.push("</td><td class='EWA_TD_M'><input type=button value='Use It' onclick='EWAC_DDL.useIt(this)'></td><tr>");
			}
		}
		ss.push("</table>");
		this.ewa = EWA.F.FOS['EWA_C_DDL'] = new EWA_ListFrameClass();
		this.ewa._Id = this.ewa.Id = 'EWA_C_DDL';
		this.ewa.IsTrSelect = true;
		this.html = ss.join('');
	},
	bind : function() {
		$('tr[ewa_id="DopListShow"]').each(function() {
			var b = $(this).find('b:last()');
			if (!b.attr('bind')) {
				b.parent().append("<a href='javascript:void(0)' onclick='EWAC_DDL.show(this);'> Qu</a>");
				b.attr('bind', 1);
			}
		});
	},
	show : function(fromObj) {
		var tr = $(fromObj).parentsUntil('tr').parent();
		if (!tr.attr('id')) {
			var id = 'DDL_REF_' + Math.random();
			id = id.replace('.', '_');
			tr.attr('id', id);
		}
		this.tr_id = tr.attr('id');
		this.tr = tr;
		this.dia = $DialogHtml(this.html, 'DropList', 1000, 400);
	},
	useIt : function(fromObj) {
		var tr = $(fromObj).parent().parent()[0];
		var CallXmlName = tr.cells[0].innerHTML;
		var CallItemName = tr.cells[1].innerHTML;
		var DlsShow = tr.cells[2].innerHTML;
		var DlsAction = tr.cells[3].innerHTML;
		var CallPara = tr.cells[4].innerHTML;

		var tb = this.tr.parent();
		tb.find('input[id="ITEMS$Frame$CallXmlName"]').val(CallXmlName);
		tb.find('input[id="ITEMS$Frame$CallXmlName"]')[0].onblur();

		tb.find('input[id="ITEMS$Frame$CallItemName"]').val(CallItemName);
		tb.find('input[id="ITEMS$Frame$CallItemName"]')[0].onblur();

		tb.find('input[id="ITEMS$Frame$CallPara"]').val(CallPara);
		tb.find('input[id="ITEMS$Frame$CallPara"]')[0].onblur();

		tb.find('input[id="ITEMS$DopListShow$DlsShow"]').val(DlsShow);
		tb.find('input[id="ITEMS$DopListShow$DlsShow"]')[0].onblur();
		tb.find('input[id="ITEMS$DopListShow$DlsAction"]').val(DlsAction);
		tb.find('input[id="ITEMS$DopListShow$DlsAction"]')[0].onblur();

		this.dia.Close();
	}
};

//var ddls = [ {
//	"DlsShow" : "@CITY_NAME/@CITY_NAME_EN",
//	"CallPara" : "a",
//	"DlsAction" : "ExtendAction0",
//	"CallItemName" : "dl_city",
//	"CallXmlName" : "|2015_exchange|ddl.xml"
//}, {
//	"DlsShow" : "@HOTEL_NAME/ @HOTEL_NAME_EN",
//	"CallPara" : "a",
//	"DlsAction" : "SAct0",
//	"CallItemName" : "ddl_hotel",
//	"CallXmlName" : "|2015_exchange|ddl.xml"
//}, {
//	"DlsShow" : "@SUP_NAME",
//	"CallPara" : "A",
//	"DlsAction" : "ExtendAction0",
//	"CallItemName" : "dl_sup_main",
//	"CallXmlName" : "|global_travel|common.xml"
//}, {
//	"DlsShow" : "@sup_id/ @sup_name",
//	"CallPara" : "a",
//	"DlsAction" : "SAct0",
//	"CallItemName" : "dl_my_supply",
//	"CallXmlName" : "|2014_b2b|common|common.xml"
//}, {
//	"DlsShow" : "@CITY_NAME, @COUNTRY_NAME",
//	"CallPara" : "a",
//	"DlsAction" : "ExtendAction0",
//	"CallItemName" : "dl_city",
//	"CallXmlName" : "|2015_exchange|ddl.xml"
//}, {
//	"DlsShow" : "@spot_name_cn/@spot_name_en",
//	"CallPara" : "a",
//	"DlsAction" : "SAct0",
//	"CallItemName" : "ddl_senic",
//	"CallXmlName" : "|2015_exchange|ddl.xml"
//}, {
//	"DlsShow" : "@HOTEL_NAME/@CITY_NAME",
//	"CallPara" : "a",
//	"DlsAction" : "SAct0",
//	"CallItemName" : "dl_hotel",
//	"CallXmlName" : "|2014_b2b|common|common.xml"
//}, {
//	"DlsShow" : "@CITY_NAME/@CITY_NAME_EN",
//	"CallPara" : "a",
//	"DlsAction" : "ExtendAction0",
//	"CallItemName" : "dl_country",
//	"CallXmlName" : "|2014_b2b|common|common.xml"
//}, {
//	"DlsShow" : "@CITY_NAME/ @CITY_NAME_EN",
//	"CallPara" : "a",
//	"DlsAction" : "ExtendAction0",
//	"CallItemName" : "dl_city",
//	"CallXmlName" : "|2014_b2b|common|common.xml"
//}, {
//	"DlsShow" : "@spot_name_cn/ @spot_name_en",
//	"CallPara" : "a",
//	"DlsAction" : "SAct0",
//	"CallItemName" : "dl_bas_spot",
//	"CallXmlName" : "|2014_center|common.xml"
//}, {
//	"DlsShow" : "@city_code (@city_name/@city_name_en)",
//	"CallPara" : "a",
//	"DlsAction" : "SAct0",
//	"CallItemName" : "v_city.LF.V",
//	"CallXmlName" : "|nz|sys.xml"
//}, {
//	"DlsShow" : "@CITY_NAME, @country_name",
//	"CallPara" : "a",
//	"DlsAction" : "ExtendAction0",
//	"CallItemName" : "dl_city",
//	"CallXmlName" : "|2015_exchange|ddl.xml"
//}, {
//	"DlsShow" : "@city_code (@city_name/@city_name_en)",
//	"CallPara" : "a",
//	"DlsAction" : "ExtendAction0",
//	"CallItemName" : "dl_city",
//	"CallXmlName" : "|2014_b2b|common|common.xml"
//}, {
//	"DlsShow" : "@CITY_ID/@CITY_NAME",
//	"CallPara" : "a",
//	"DlsAction" : "ExtendAction0",
//	"CallItemName" : "dl_city",
//	"CallXmlName" : "|global_travel|common.xml"
//}, {
//	"DlsShow" : "@CITY_NAME/@CITY_NAME_EN",
//	"CallPara" : "a",
//	"DlsAction" : "ExtendAction0",
//	"CallItemName" : "dl_city",
//	"CallXmlName" : "|global_travel|common.xml"
//}, {
//	"DlsShow" : "@city_code (@city_name/@city_name_en)",
//	"CallPara" : "a",
//	"DlsAction" : "ExtendAction0",
//	"CallItemName" : "CITY_CITY",
//	"CallXmlName" : "|2014_rob|common|droplist.xml"
//}, {
//	"DlsShow" : "@CITY_NAME/@country_name",
//	"CallPara" : "a",
//	"DlsAction" : "ExtendAction0",
//	"CallItemName" : "dl_city",
//	"CallXmlName" : "|2015_exchange|ddl.xml"
//}, {
//	"DlsShow" : "@city_code (@city_name/@city_name_en)",
//	"CallPara" : "a",
//	"DlsAction" : "SAct0",
//	"CallItemName" : "dl_city",
//	"CallXmlName" : "|2014_b2b|common|common.xml"
//}, {
//	"DlsShow" : "@city_code (@city_name/@city_name_en)",
//	"CallPara" : "a",
//	"DlsAction" : "ExtendAction0",
//	"CallItemName" : "CITY_LIVE.ListFrame.View",
//	"CallXmlName" : "|global_travel_scm|journey.xml"
//}, {
//	"DlsShow" : "@city_name/ @city_name_en",
//	"CallPara" : "a",
//	"DlsAction" : "ExtendAction0",
//	"CallItemName" : "dl_city",
//	"CallXmlName" : "|2014_center|common.xml"
//}, {
//	"DlsShow" : "@CITY_NAME, @COUNTRY_name",
//	"CallPara" : "a",
//	"DlsAction" : "ExtendAction0",
//	"CallItemName" : "dl_city",
//	"CallXmlName" : "|2015_exchange|ddl.xml"
//}, {
//	"DlsShow" : "@SUP_NAME",
//	"CallPara" : "A",
//	"DlsAction" : "ExtendAction0",
//	"CallItemName" : "dl_sup_main",
//	"CallXmlName" : "global_travel|common.xml"
//}, {
//	"DlsShow" : "@CITY_ID/@CITY_NAME/@CITY_NAME_EN",
//	"CallPara" : "a",
//	"DlsAction" : "ExtendAction0",
//	"CallItemName" : "dl_city",
//	"CallXmlName" : "|global_travel|common.xml"
//}, {
//	"DlsShow" : "@GRP_HY_CODE(@GRP_NAME)",
//	"CallPara" : "a",
//	"DlsAction" : "ExtendAction0",
//	"CallItemName" : "GRP_MAIN.ListFrame.View",
//	"CallXmlName" : "global_travel_scm|sysconf.xml"
//}, {
//	"DlsShow" : "@SUP_NAME",
//	"CallPara" : "A",
//	"DlsAction" : "SACT0",
//	"CallItemName" : "bas_sup_main.LF.V",
//	"CallXmlName" : "|2014_center|supply|api.xml"
//}, {
//	"DlsShow" : "@city_name/@city_name_en",
//	"CallPara" : "a",
//	"DlsAction" : "ExtendAction0",
//	"CallItemName" : "dl_country",
//	"CallXmlName" : "|2014_center|common.xml"
//}, {
//	"DlsShow" : "@SUP_NAME(@SUP_TAG)",
//	"CallPara" : "a",
//	"DlsAction" : "ExtendAction0",
//	"CallItemName" : "SUP_MAIN.ListFrame.View",
//	"CallXmlName" : "global_travel_scm|sysconf.xml"
//}, {
//	"DlsShow" : "@VIS_TYPE/ @GRP_NAME",
//	"CallPara" : "A",
//	"DlsAction" : "ExtendAction0",
//	"CallItemName" : "dl_vis_grp",
//	"CallXmlName" : "|global_travel|common.xml"
//}, {
//	"DlsShow" : "@city_code (@city_name/@city_name_en)",
//	"CallPara" : "A",
//	"DlsAction" : "ExtendAction0",
//	"CallItemName" : "CITY_CITY",
//	"CallXmlName" : "|2014_rob|common|droplist.xml"
//}, {
//	"DlsShow" : "@SUP_ID/@SUP_NAME/@ADM_NAME",
//	"CallPara" : "A",
//	"DlsAction" : "ExtendAction0",
//	"CallItemName" : "dl_sup_main",
//	"CallXmlName" : "|2014_center|supply|bk_supply.xml"
//}, {
//	"DlsShow" : "@city_name, @country_name",
//	"CallPara" : "a",
//	"DlsAction" : "ExtendAction0",
//	"CallItemName" : "dl_city",
//	"CallXmlName" : "|2015_exchange|ddl.xml"
//}, {
//	"DlsShow" : "@USR_NAME",
//	"CallPara" : "A",
//	"DlsAction" : "ExtendAction0",
//	"CallItemName" : "WEB_USER.DDL&WX_CFG_NO=@WX_CFG_NO",
//	"CallXmlName" : "|2014_rob|product|web_user.xml"
//}, {
//	"DlsShow" : "@city_code (@city_name/@city_name_en)",
//	"CallPara" : "a",
//	"DlsAction" : "SAct0",
//	"CallItemName" : "v_contury.LF.V",
//	"CallXmlName" : "|nz|sys.xml"
//}, {
//	"DlsShow" : "@GRP_NAME",
//	"CallPara" : "A",
//	"DlsAction" : "ExtendAction0",
//	"CallItemName" : "dl_grp_main",
//	"CallXmlName" : "|global_travel|common.xml"
//}, {
//	"DlsShow" : "@HOTEL_NAME/@HOTEL_NAME_EN (@city_name/@city_CODE)",
//	"CallPara" : "A",
//	"DlsAction" : "SAct0",
//	"CallItemName" : "dl_hotel",
//	"CallXmlName" : "|2014_b2b|common|common.xml"
//}, {
//	"DlsShow" : "@city_code (@city_name/@city_name_en)",
//	"CallPara" : "A",
//	"DlsAction" : "ExtendAction0",
//	"CallItemName" : "dl_city",
//	"CallXmlName" : "|2014_b2b|common|common.xml"
//}, {
//	"DlsShow" : "@CITY_NAME, @COUNTRY_name",
//	"CallPara" : "a",
//	"DlsAction" : "ExtendAction0",
//	"CallItemName" : "dl_city",
//	"CallXmlName" : "|2014_center|common.xml"
//}, {
//	"DlsShow" : "@SPOT_NAME_CN/ @SPOT_NAME_EN",
//	"CallPara" : "a",
//	"DlsAction" : "SAct0",
//	"CallItemName" : "ddl_spot",
//	"CallXmlName" : "|2015_exchange|ddl.xml"
//}, {
//	"DlsShow" : "@GRP_HY_CODE|@GRP_NAME",
//	"CallPara" : "A",
//	"DlsAction" : "ExtendAction0",
//	"CallItemName" : "oa_fin_grp",
//	"CallXmlName" : "global_travel|common.xml"
//}, {
//	"DlsShow" : "@city_code (@city_name/@city_name_en)",
//	"CallPara" : "a",
//	"DlsAction" : "ExtendAction0",
//	"CallItemName" : "dl_country",
//	"CallXmlName" : "|2014_b2b|common|common.xml"
//}, {
//	"DlsShow" : "@CITY_NAME",
//	"CallPara" : "a",
//	"DlsAction" : "ExtendAction0",
//	"CallItemName" : "dl_city",
//	"CallXmlName" : "|2014_b2b|common|common.xml"
//}, {
//	"DlsShow" : "@BBS_NICK_NAME/@USR_NAME",
//	"CallPara" : "A",
//	"DlsAction" : "SAct0",
//	"CallItemName" : "WEB_USER.bbs.droplist",
//	"CallXmlName" : "|global_travel|bk_bbs.xml"
//}, {
//	"DlsShow" : "@city_code (@city_name/@city_name_en)",
//	"CallPara" : "a",
//	"DlsAction" : "ExtendAction0",
//	"CallItemName" : "CITY_LIVE.ListFrame.View",
//	"CallXmlName" : "|global_travel_scm|sup_ser.xml"
//}, {
//	"DlsShow" : "@sup_name/ @sup_tele",
//	"CallPara" : "a",
//	"DlsAction" : "SAct0",
//	"CallItemName" : "dl_my_supply",
//	"CallXmlName" : "|2014_b2b|common|common.xml"
//}, {
//	"DlsShow" : "@SUP_NAME/@SUP_ID",
//	"CallPara" : "a",
//	"DlsAction" : "SAct0",
//	"CallItemName" : "dl_my_supply",
//	"CallXmlName" : "|2014_b2b|common|common.xml"
//}, {
//	"DlsShow" : "@a_code/@a_company(@a_d_airport-@a_a_airport)",
//	"CallPara" : "a",
//	"DlsAction" : "SAct0",
//	"CallItemName" : "ddl_air_line",
//	"CallXmlName" : "|2015_exchange|ddl.xml"
//}, {
//	"DlsShow" : "@SUP_NAME",
//	"CallPara" : "A",
//	"DlsAction" : "ExtendAction0",
//	"CallItemName" : "dl_sup_main",
//	"CallXmlName" : "|2014_rob|common|droplist.xml"
//}, {
//	"DlsShow" : "@CITY_ID/@CITY_NAME",
//	"CallPara" : "a",
//	"DlsAction" : "ExtendAction0",
//	"CallItemName" : "dl_city",
//	"CallXmlName" : "|2014_center|common.xml"
//}, {
//	"DlsShow" : "@SRV_NAME",
//	"CallPara" : "a",
//	"DlsAction" : "SAct0",
//	"CallItemName" : "SRV_MAIN_forApp",
//	"CallXmlName" : "|2014_rob|common|droplist.xml"
//}, {
//	"DlsShow" : "@CITY_NAME/@CITY_NAME_EN",
//	"CallPara" : "a",
//	"DlsAction" : "ExtendAction0",
//	"CallItemName" : "dl_country",
//	"CallXmlName" : "|2014_center|common.xml"
//}, {
//	"DlsShow" : "@CITY_NAME/@COUNTRU_NAME",
//	"CallPara" : "a",
//	"DlsAction" : "ExtendAction0",
//	"CallItemName" : "dl_city",
//	"CallXmlName" : "|2014_b2b|common|common.xml"
//}, {
//	"DlsShow" : "@AIR_LINE_NAME/@AIR_LINE_NAME_EN (@city_name/@city_CODE)",
//	"CallPara" : "A",
//	"DlsAction" : "SAct0",
//	"CallItemName" : "dl_air",
//	"CallXmlName" : "|2014_b2b|common|common.xml"
//}, {
//	"DlsShow" : "@SPOT_NAME_CN/@SPOT_NAME_EN",
//	"CallPara" : "a",
//	"DlsAction" : "SAct0",
//	"CallItemName" : "ddl_senic",
//	"CallXmlName" : "|2015_exchange|ddl.xml"
//}, {
//	"DlsShow" : "@REC_NAME_S",
//	"CallPara" : "A",
//	"DlsAction" : "ExtendAction0",
//	"CallItemName" : "REC_MAIN.HOTEL",
//	"CallXmlName" : "|2014_rob|common|droplist.xml"
//}, {
//	"DlsShow" : "@CITY_NAME (@country_name)",
//	"CallPara" : "a",
//	"DlsAction" : "ExtendAction0",
//	"CallItemName" : "dl_city",
//	"CallXmlName" : "|2014_b2b|common|common.xml"
//}, {
//	"DlsShow" : "@city_code (@city_name/@city_name_en)",
//	"CallPara" : "a",
//	"DlsAction" : "ExtendAction0",
//	"CallItemName" : "dl_city",
//	"CallXmlName" : "|backadmin|common.xml"
//}, {
//	"DlsShow" : "@CITY_NAME/@CITY_NAME_EN",
//	"CallPara" : "a",
//	"DlsAction" : "ExtendAction0",
//	"CallItemName" : "dl_city",
//	"CallXmlName" : "|2014_b2b|common|common.xml"
//}, {
//	"DlsShow" : "@are_id (@are_name)",
//	"CallPara" : "a",
//	"DlsAction" : "ExtendAction0",
//	"CallItemName" : "V_BAS_CN_AREA_CITY.ListFrame.View",
//	"CallXmlName" : "|global_travel_scm|crm.xml"
//} ];
