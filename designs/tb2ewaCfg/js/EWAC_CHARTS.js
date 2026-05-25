var EWAC_ChartUtils = {
	CreateSimpleData : function(len) {
		var data = [];
		for (var i = 0; i < len; i++) {
			data.push(Math.random() * 100);
		}
		return data;
	},
	CreateArrayJSON : function(arr) {
		var s1 = "[";
		for (var i = 0; i < arr.length; i++) {
			if (i > 0) {
				s1 += ", ";
			}
			s1 += arr[i];
		}
		s1 += "]";
		return s1;
	},
	ChartExp : '<OBJECT id="chart" classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000" width="850" height="300">\
<PARAM name="movie" value="{CONTEXT}/EWA_STYLE/charts/open-flash-chart2/open-flash-chart.swf" />\
</OBJECT>',
	Charts : []

};
function EWAC_ChartBar() {
	this.chartTypes = ["bar", "hbar", "bar_stack", "bar_sketch", "bar_glass",
			"bar_cylinder", "bar_cylinder_outline", "bar_dome", "bar_round",
			"bar_round_glass", "bar_round3d"];
}
function EWAC_Chart() {
	this.chartType;
	this.data = EWAC_ChartUtils.CreateSimpleData(12);
	this.CreateChart = function() {
		var dataExp = EWAC_ChartUtils.CreateArrayJSON(this.data);
		var s1 = "{elements[type:'" + this.chartType + "',values:" + dataExp
				+ "]}";
	}
	this._CreateChartExp = function() {
		var id = '_EWA_CHART_' + EWAC_ChartUtils.Charts.length;
		var s1 = "<OBJECT id='"
				+ id
				+ "' classid='clsid:D27CDB6E-AE6D-11cf-96B8-444553540000' width='850' height='300'>"
				+ "<PARAM name='movie' value='/EmpScriptV2/EWA_STYLE/charts/open-flash-chart2/open-flash-chart.swf' />"
				+ "<OBJECT>";
		return s1;
	}
}