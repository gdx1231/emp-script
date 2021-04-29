# Easy Web Application 

## Create a EWA tables （MYSQL）
```sql
-- ewa.ewa_cfg definition

CREATE TABLE `ewa_cfg` (
  `XMLNAME` varchar(200) NOT NULL,
  `ITEMNAME` varchar(100) NOT NULL,
  `XMLDATA` longtext NOT NULL,
  `HASH_CODE` int(11) NOT NULL,
  `ADM_LID` varchar(50) DEFAULT NULL,
  `CREATE_DATE` datetime DEFAULT NULL,
  `UPDATE_DATE` datetime DEFAULT NULL,
  `MD5` varchar(32) DEFAULT NULL,
  `DATASOURCE` varchar(123) DEFAULT NULL,
  `CLASS_ACL` varchar(333) DEFAULT NULL,
  `CLASS_LOG` varchar(333) DEFAULT NULL,
  `DESCRIPTION` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`XMLNAME`,`ITEMNAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ewa.ewa_cfg_his definition

CREATE TABLE `ewa_cfg_his` (
  `HIS_ID` int(11) NOT NULL AUTO_INCREMENT,
  `HIS_DATE` datetime NOT NULL,
  `XMLNAME` varchar(200) NOT NULL,
  `ITEMNAME` varchar(100) NOT NULL,
  `XMLDATA` longtext NOT NULL,
  `HASH_CODE` int(11) NOT NULL,
  `ADM_LID` varchar(50) DEFAULT NULL,
  `CREATE_DATE` datetime DEFAULT NULL,
  `UPDATE_DATE` datetime DEFAULT NULL,
  `MD5` varchar(32) DEFAULT NULL,
  `DATASOURCE` varchar(123) DEFAULT NULL,
  `CLASS_ACL` varchar(333) DEFAULT NULL,
  `CLASS_LOG` varchar(333) DEFAULT NULL,
  `DESCRIPTION` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`HIS_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ewa.ewa_cfg_oth definition

CREATE TABLE `ewa_cfg_oth` (
  `OTH_TAG` varchar(50) NOT NULL,
  `OTH_TXT` longtext,
  `OTH_CDATE` datetime DEFAULT NULL,
  `OTH_MDATE` datetime DEFAULT NULL,
  `ADM_LID` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`OTH_TAG`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ewa.ewa_cfg_rm definition

CREATE TABLE `ewa_cfg_rm` (
  `RM_ID` int(11) NOT NULL AUTO_INCREMENT,
  `RM_DATE` datetime NOT NULL,
  `XMLNAME` varchar(200) NOT NULL,
  `ITEMNAME` varchar(100) NOT NULL,
  `XMLDATA` longtext NOT NULL,
  `HASH_CODE` int(11) NOT NULL,
  `ADM_LID` varchar(50) DEFAULT NULL,
  `CREATE_DATE` datetime DEFAULT NULL,
  `UPDATE_DATE` datetime DEFAULT NULL,
  `MD5` varchar(32) DEFAULT NULL,
  `DATASOURCE` varchar(123) DEFAULT NULL,
  `CLASS_ACL` varchar(333) DEFAULT NULL,
  `CLASS_LOG` varchar(333) DEFAULT NULL,
  `DESCRIPTION` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`RM_ID`)
) ENGINE=InnoDB AUTO_INCREMENT=861 DEFAULT CHARSET=utf8mb4;


-- ewa.ewa_cfg_tree definition

CREATE TABLE `ewa_cfg_tree` (
  `XMLNAME` varchar(200) NOT NULL COMMENT '配置文件',
  `EDIT_STATUS` varchar(10) DEFAULT NULL COMMENT '编辑状态',
  PRIMARY KEY (`XMLNAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## Create a EWA tables （SQLSERVER）
```sql
CREATE TABLE EWA_CFG (
	XMLNAME varchar(200) NOT NULL,
	ITEMNAME varchar(100) NOT NULL,
	XMLDATA ntext NOT NULL,
	HASH_CODE int NOT NULL,
	ADM_LID varchar(50) NULL,
	CREATE_DATE datetime NULL,
	UPDATE_DATE datetime NULL,
	MD5 varchar(32) NULL,
	DATASOURCE varchar(123) NULL,
	CLASS_ACL varchar(333) NULL,
	CLASS_LOG varchar(333) NULL,
	DESCRIPTION varchar(500) NULL,
	CONSTRAINT PK_EWA_CFG PRIMARY KEY (XMLNAME,ITEMNAME)
);



CREATE TABLE EWA_CFG_HIS (
	HIS_ID int IDENTITY(1,1) NOT NULL,
	HIS_DATE datetime NOT NULL,
	XMLNAME varchar(200) NOT NULL,
	ITEMNAME varchar(100) NOT NULL,
	XMLDATA ntext NOT NULL,
	HASH_CODE int NOT NULL,
	ADM_LID varchar(50) NULL,
	CREATE_DATE datetime NULL,
	UPDATE_DATE datetime NULL,
	MD5 varchar(32) NULL,
	DATASOURCE varchar(123) NULL,
	CLASS_ACL varchar(333) NULL,
	CLASS_LOG varchar(333) NULL,
	DESCRIPTION varchar(500) NULL,
	CONSTRAINT PK_EWA_CFG_HIS PRIMARY KEY (HIS_ID)
);

CREATE TABLE EWA_CFG_OTH (
	OTH_TAG varchar(50) NOT NULL,
	OTH_TXT ntext NULL,
	OTH_CDATE datetime NULL,
	OTH_MDATE datetime NULL,
	ADM_LID varchar(50) NULL,
	CONSTRAINT PK_EWA_CFG_OTH PRIMARY KEY (OTH_TAG)
);

CREATE TABLE EWA_CFG_RM (
	RM_ID int IDENTITY(1,1) NOT NULL,
	RM_DATE datetime NOT NULL,
	XMLNAME varchar(200) NOT NULL,
	ITEMNAME varchar(100) NOT NULL,
	XMLDATA ntext NOT NULL,
	HASH_CODE int NOT NULL,
	ADM_LID varchar(50) NULL,
	CREATE_DATE datetime NULL,
	UPDATE_DATE datetime NULL,
	MD5 varchar(32) NULL,
	DATASOURCE varchar(123) NULL,
	CLASS_ACL varchar(333) NULL,
	CLASS_LOG varchar(333) NULL,
	DESCRIPTION varchar(500) NULL,
	CONSTRAINT PK_EWA_CFG_RM PRIMARY KEY (RM_ID)
);

CREATE TABLE EWA_CFG_TREE (
	XMLNAME varchar(200) NOT NULL,
	EDIT_STATUS varchar(10) NULL,
	CONSTRAINT PK_EWA_CFG_TREE PRIMARY KEY (XMLNAME)
);
```


## Maven
```xml
<!-- https://mvnrepository.com/artifact/com.gdxsoft.easyweb/emp-script -->
<dependency>
    <groupId>com.gdxsoft.easyweb</groupId>
    <artifactId>emp-script</artifactId>
    <version>1.0.6</version>
</dependency>
```