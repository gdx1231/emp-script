/**
 * 
 */
package com.gdxsoft.easyweb.script.Workflow;

import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.script.RequestValue;

/**
 * @author Administrator
 * 
 */
public class OrgMainDemoImpl extends OrgMain implements IOrgMain {

	/**
	 * 
	 */
	public OrgMainDemoImpl() {
	}

	 
	
	public void init(RequestValue rv) throws Exception {
		DataConnection cnn = new DataConnection();
		cnn.setRequestValue(rv);

		String sqlDept = "SELECT * FROM ADM_DEPT WHERE SUP_ID=@G_SUP_ID ORDER BY DEP_LVL,DEP_ORD";
		DTTable deptTb = DTTable.getJdbcTable(sqlDept, cnn);

		String sqlPost = "SELECT a.*,b.POS_IS_MASTER FROM ADM_r_dept_post a"
				+ " inner join adm_post b on a.pos_id=b.pos_id "
				+ "WHERE dep_id in(select dep_id from adm_dept where SUP_ID=@g_sup_id)";
		DTTable postTb = DTTable.getJdbcTable(sqlPost, cnn);

		String sqlUser = "SELECT adm_id, adm_name, dep_Id, dep_pos_id "
				+ "FROM dbo.V_ADM_USER_POST_DEPT WHERE  SUP_ID=@G_SUP_ID";
		DTTable userTb = DTTable.getJdbcTable(sqlUser, cnn);

		cnn.close();
		super.initDept(deptTb, "dep_Id", "dep_Name", "dep_Pid");
		super.initPost(postTb, "dep_pos_id", "dep_pos_Name", "dep_Id",
				"pos_is_Master");
		super.initUser(userTb, "adm_id", "adm_name", "dep_Id", "dep_pos_id");
	}
}
