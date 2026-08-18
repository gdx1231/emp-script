package com.gdxsoft.easyweb.define.database;

import org.json.JSONObject;

/**
 * 数据库对象差异结果
 */
public class DbSyncDiff {

	/** 状态：一致 */
	public static final String SAME = "SAME";
	/** 状态：仅源库存在 */
	public static final String SOURCE_ONLY = "SOURCE_ONLY";
	/** 状态：仅目标库存在 */
	public static final String TARGET_ONLY = "TARGET_ONLY";
	/** 状态：两边都有但内容不同 */
	public static final String DIFFERENT = "DIFFERENT";

	private String objectType;
	private String objectName;
	private String schemaName;
	private String status;
	private String sourceDdl;
	private String targetDdl;
	private String syncSql;
	private String description;

	public DbSyncDiff() {
	}

	public DbSyncDiff(DbSyncObject source, DbSyncObject target) {
		if (source != null) {
			this.objectType = source.getObjectType();
			this.objectName = source.getObjectName();
			this.schemaName = source.getSchemaName();
			this.sourceDdl = source.getDdl();
		}
		if (target != null) {
			if (this.objectType == null) {
				this.objectType = target.getObjectType();
			}
			if (this.objectName == null) {
				this.objectName = target.getObjectName();
			}
			if (this.schemaName == null) {
				this.schemaName = target.getSchemaName();
			}
			this.targetDdl = target.getDdl();
		}

		if (source != null && target == null) {
			this.status = SOURCE_ONLY;
		} else if (source == null && target != null) {
			this.status = TARGET_ONLY;
		} else if (source != null && target != null) {
			if (source.getMd5() != null && source.getMd5().equals(target.getMd5())) {
				this.status = SAME;
			} else {
				this.status = DIFFERENT;
			}
		}
	}

	public JSONObject toJson() {
		JSONObject obj = new JSONObject();
		obj.put("objectType", objectType);
		obj.put("objectName", objectName);
		if (schemaName != null) {
			obj.put("schemaName", schemaName);
		}
		obj.put("status", status);
		if (sourceDdl != null) {
			obj.put("sourceDdl", sourceDdl);
		}
		if (targetDdl != null) {
			obj.put("targetDdl", targetDdl);
		}
		if (syncSql != null) {
			obj.put("syncSql", syncSql);
		}
		if (description != null) {
			obj.put("description", description);
		}
		return obj;
	}

	public String getKey() {
		return objectType + "::" + (schemaName == null ? "" : schemaName + ".") + objectName;
	}

	// ---- getters / setters ----

	public String getObjectType() {
		return objectType;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	public String getObjectName() {
		return objectName;
	}

	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getSourceDdl() {
		return sourceDdl;
	}

	public void setSourceDdl(String sourceDdl) {
		this.sourceDdl = sourceDdl;
	}

	public String getTargetDdl() {
		return targetDdl;
	}

	public void setTargetDdl(String targetDdl) {
		this.targetDdl = targetDdl;
	}

	public String getSyncSql() {
		return syncSql;
	}

	public void setSyncSql(String syncSql) {
		this.syncSql = syncSql;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
