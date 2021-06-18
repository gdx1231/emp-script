# Easy Web Application 

For the development of a Web application system, such as CRM, OA, SCM, etc., during the system development process, a lot of time is applied to the page composition. Although there are some MVC middleware applications, it still feels low-tech coding And testing accounts for 60%-80% of the entire development process. These include such as HTML code synthesis, page unit testing, data verification and page display effects, browser compatibility, and endless user needs changes Wait. At the same time, this part of the work is the devil of development engineers, but also the devil of project management.

In a typical application system, the business logic is only about 10%-20% of the overall part of the project, and most of the work is relatively simple data entry, modification, deletion and query (including multiple tables and summaries). The design of complex business logic must be done in a hard-coded manner to ensure the integrity of the business logic. But if the corresponding simple parts are still hard-coded (even with a code generator) and synthesized into HTML, such low-tech work will greatly extend the development time.

The goal of the development system is to reduce the development cycle by 80%, deploy a large amount of low-tech work through XML configuration files, free up engineers’ time, and focus on database architecture design and core business logic. At the same time reduce development time and development costs.

针对一个Web应用系统开发，如CRM，OA，SCM等，在系统开发过程中，大量的时间被应用于页面合成上面，虽然有一些MVC的中间件的应用，但是还是感觉到低技术水准的编码和测试占了整个开发过程中的60%-80%的时间，这些包括如HTML代码的合成，页面单元的测试，数据验证和页面显示效果，浏览器的兼容性，用户没完没了的需求变更等。同时该部分工作又是开发工程师的恶魔，同时也是项目管理中的恶魔。

在典型的应用系统中，业务逻辑复杂的部分只占项目整体部分的10%-20%左右，大部分的工作是较为简单的数据录入、修改、删除和查询（包括多表和汇总）。对于复杂的业务逻辑部分的设计必须采用硬编码的方式进行，从而保证业务逻辑的完整性。但对应简单的部分如果还是采用硬编码（即使使用代码生成器）并合成为HTML，这样低技术水准的工作会大大延长开发的时间。

该开发系统的目标是降低80%部分的开发周期，将大量的低技术含量的工作通过XML配置文件进行部署，解放工程师的时间，使其将主要精力放到数据库架构设计和核心业务逻辑上，同时减少开发时间和开发成本。

 

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
    <version>1.1.0</version>
</dependency>
```