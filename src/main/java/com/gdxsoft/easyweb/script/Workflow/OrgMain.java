package com.gdxsoft.easyweb.script.Workflow;

import java.util.HashMap;
import java.util.Iterator;

import com.gdxsoft.easyweb.data.DTRow;
import com.gdxsoft.easyweb.data.DTTable;

public class OrgMain implements IOrgMain {
	private HashMap<String, OrgDept> _Depts;
	private HashMap<String, OrgUser> _Users;
	private HashMap<String, OrgPost> _Posts;

	private OrgDept _DeptRoot;

	
	/* (non-Javadoc)
	 * @see com.gdxsoft.easyweb.script.Workflow.IOrgMain#initDept(com.gdxsoft.easyweb.data.DTTable, java.lang.String, java.lang.String, java.lang.String)
	 */
	public void initDept(DTTable deptTb, String deptId, String deptName,
			String deptParent) throws Exception {
		_Depts = new HashMap<String, OrgDept>();
		for (int i = 0; i < deptTb.getCount(); i++) {
			DTRow row = deptTb.getRow(i);
			OrgDept d = new OrgDept();
			d.setDeptId(row.getCell(deptId).toString());
			d.setDeptName(row.getCell(deptName).toString());
			d.setLowDepts(new HashMap<String, OrgDept>());
			d.setUsers(new HashMap<String, OrgUser>());

			OrgDept upDept = new OrgDept();
			upDept.setDeptId(row.getCell(deptParent).toString());
			d.setUpDept(upDept);
			_Depts.put(d.getDeptId(), d);
		}

		Iterator<String> it = _Depts.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			OrgDept d = _Depts.get(key);
			String upKey = d.getUpDept().getDeptId();
			if (_Depts.containsKey(upKey)) {
				OrgDept upDept = _Depts.get(upKey);
				d.setUpDept(upDept);
				upDept.getLowDepts().put(d.getDeptId(), d);
			} else {
				d.setUpDept(null);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.gdxsoft.easyweb.script.Workflow.IOrgMain#initPost(com.gdxsoft.easyweb.data.DTTable, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public void initPost(DTTable postTb, String postId, String postName,
			String deptId, String isMaster) throws Exception {
		this._Posts = new HashMap<String, OrgPost>();
		for (int i = 0; i < postTb.getCount(); i++) {
			DTRow row = postTb.getRow(i);
			OrgPost p = new OrgPost();
			p.setPostId(row.getCell(postId).toString());
			p.setPostName(row.getCell(postName).toString());

			p.setUsers(new HashMap<String, OrgUser>());
			String master = row.getCell(isMaster).toString();
			if (master != null
					&& (master.equals("1") || master.equalsIgnoreCase("true"))) {
				p.setIsMaster(true);
			} else {
				p.setIsMaster(false);
			}

			if (_Depts.containsKey(deptId)) {
				OrgDept d = _Depts.get(deptId);

				p.setDept(d);

			}

			this._Posts.put(p.getPostId(), p);
		}
	}

	/* (non-Javadoc)
	 * @see com.gdxsoft.easyweb.script.Workflow.IOrgMain#initUser(com.gdxsoft.easyweb.data.DTTable, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public void initUser(DTTable userTb, String userId, String userName,
			String deptId, String postId) throws Exception {
		this._Users = new HashMap<String, OrgUser>();
		for (int i = 0; i < userTb.getCount(); i++) {
			DTRow row = userTb.getRow(i);
			String uid = row.getCell(userId).toString();
			OrgUser u;
			if (this._Users.containsKey(uid)) {
				u = this._Users.get(uid);
			} else {
				u = new OrgUser();
				u.setUId(uid);
				u.setUName(row.getCell(userName).toString());

				if (_Depts.containsKey(deptId)) {
					OrgDept d = _Depts.get(deptId);
					u.setDept(d);
					d.getUsers().put(uid, u);
				}
				u.setPosts(new HashMap<String, OrgPost>());
			}
			String pid = row.getCell(postId).toString();
			if (this._Posts.containsKey(pid)) {
				OrgPost p = this._Posts.get(pid);
				u.getPosts().put(pid, p);
				p.getUsers().put(uid, u);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.gdxsoft.easyweb.script.Workflow.IOrgMain#getDepts()
	 */
	public HashMap<String, OrgDept> getDepts() {
		return _Depts;
	}

	/* (non-Javadoc)
	 * @see com.gdxsoft.easyweb.script.Workflow.IOrgMain#getUsers()
	 */
	public HashMap<String, OrgUser> getUsers() {
		return _Users;
	}

	/* (non-Javadoc)
	 * @see com.gdxsoft.easyweb.script.Workflow.IOrgMain#getPosts()
	 */
	public HashMap<String, OrgPost> getPosts() {
		return _Posts;
	}

	/* (non-Javadoc)
	 * @see com.gdxsoft.easyweb.script.Workflow.IOrgMain#getDeptRoot()
	 */
	public OrgDept getDeptRoot() {
		return _DeptRoot;
	}
}
