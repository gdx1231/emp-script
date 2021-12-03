# Easy Web Application 

For the development of a Web application system, such as CRM, OA, SCM, etc., during the system development process, a lot of time is applied to the page composition. Although there are some MVC middleware applications, it still feels low-tech coding And testing accounts for 60%-80% of the entire development process. These include such as HTML code synthesis, page unit testing, data verification and page display effects, browser compatibility, and endless user needs changes Wait. At the same time, this part of the work is the devil of development engineers, but also the devil of project management.

In a typical application system, the business logic is only about 10%-20% of the overall part of the project, and most of the work is relatively simple data entry, modification, deletion and query (including multiple tables and summaries). The design of complex business logic must be done in a hard-coded manner to ensure the integrity of the business logic. But if the corresponding simple parts are still hard-coded (even with a code generator) and synthesized into HTML, such low-tech work will greatly extend the development time.

The goal of the development system is to reduce the development cycle by 80%, deploy a large amount of low-tech work through XML configuration files, free up engineers’ time, and focus on database architecture design and core business logic. At the same time reduce development time and development costs.

针对一个Web应用系统开发，如CRM，OA，SCM等，在系统开发过程中，大量的时间被应用于页面合成上面，虽然有一些MVC的中间件的应用，但是还是感觉到低技术水准的编码和测试占了整个开发过程中的60%-80%的时间，这些包括如HTML代码的合成，页面单元的测试，数据验证和页面显示效果，浏览器的兼容性，用户没完没了的需求变更等。同时该部分工作又是开发工程师的恶魔，同时也是项目管理中的恶魔。

在典型的应用系统中，业务逻辑复杂的部分只占项目整体部分的10%-20%左右，大部分的工作是较为简单的数据录入、修改、删除和查询（包括多表和汇总）。对于复杂的业务逻辑部分的设计必须采用硬编码的方式进行，从而保证业务逻辑的完整性。但对应简单的部分如果还是采用硬编码（即使使用代码生成器）并合成为HTML，这样低技术水准的工作会大大延长开发的时间。

该开发系统的目标是降低80%部分的开发周期，将大量的低技术含量的工作通过XML配置文件进行部署，解放工程师的时间，使其将主要精力放到数据库架构设计和核心业务逻辑上，同时减少开发时间和开发成本。

 

## Create a EWA tables （MYSQL）
```sql
CREATE TABLE ewa_cfg (
  XMLNAME varchar(200) NOT NULL,
  ITEMNAME varchar(100) NOT NULL,
  XMLDATA longtext NOT NULL,
  HASH_CODE int NOT NULL,
  ADM_LID varchar(50) DEFAULT NULL,
  CREATE_DATE datetime DEFAULT NULL,
  UPDATE_DATE datetime DEFAULT NULL,
  MD5 varchar(32) DEFAULT NULL,
  DATASOURCE varchar(123) DEFAULT NULL,
  CLASS_ACL varchar(333) DEFAULT NULL,
  CLASS_LOG varchar(333) DEFAULT NULL,
  DESCRIPTION varchar(500) DEFAULT NULL,
  PRIMARY KEY (XMLNAME,ITEMNAME)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE ewa_cfg_his (
  HIS_ID int NOT NULL AUTO_INCREMENT,
  HIS_DATE datetime NOT NULL,
  XMLNAME varchar(200) NOT NULL,
  ITEMNAME varchar(100) NOT NULL,
  XMLDATA longtext NOT NULL,
  HASH_CODE int NOT NULL,
  ADM_LID varchar(50) DEFAULT NULL,
  CREATE_DATE datetime DEFAULT NULL,
  UPDATE_DATE datetime DEFAULT NULL,
  MD5 varchar(32) DEFAULT NULL,
  DATASOURCE varchar(123) DEFAULT NULL,
  CLASS_ACL varchar(333) DEFAULT NULL,
  CLASS_LOG varchar(333) DEFAULT NULL,
  DESCRIPTION varchar(500) DEFAULT NULL,
  PRIMARY KEY (HIS_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE ewa_cfg_oth (
  OTH_TAG varchar(50) NOT NULL,
  OTH_TXT longtext,
  OTH_CDATE datetime DEFAULT NULL,
  OTH_MDATE datetime DEFAULT NULL,
  ADM_LID varchar(50) DEFAULT NULL,
  PRIMARY KEY (OTH_TAG)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE ewa_cfg_rm (
  RM_ID int NOT NULL AUTO_INCREMENT,
  RM_DATE datetime NOT NULL,
  XMLNAME varchar(200) NOT NULL,
  ITEMNAME varchar(100) NOT NULL,
  XMLDATA longtext NOT NULL,
  HASH_CODE int NOT NULL,
  ADM_LID varchar(50) DEFAULT NULL,
  CREATE_DATE datetime DEFAULT NULL,
  UPDATE_DATE datetime DEFAULT NULL,
  MD5 varchar(32) DEFAULT NULL,
  DATASOURCE varchar(123) DEFAULT NULL,
  CLASS_ACL varchar(333) DEFAULT NULL,
  CLASS_LOG varchar(333) DEFAULT NULL,
  DESCRIPTION varchar(500) DEFAULT NULL,
  PRIMARY KEY (RM_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE ewa_cfg_tree (
  XMLNAME varchar(200) NOT NULL COMMENT '配置文件',
  EDIT_STATUS varchar(10) DEFAULT NULL COMMENT '编辑状态',
  PRIMARY KEY (XMLNAME)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## RESTful tables （MYSQL）
```sql
CREATE TABLE ewa_restful (
  rs_uid varchar(36) CHARACTER SET latin1 NOT NULL COMMENT '请求编号',
  cat_uid varchar(36) CHARACTER SET latin1 NOT NULL COMMENT '目录编号',
  rs_method varchar(10) NOT NULL COMMENT '请求方式',
  rs_name varchar(200) NOT NULL COMMENT '名称',
  rs_name_en varchar(200) NOT NULL COMMENT '名称英文',
  rs_xmlname varchar(200) NOT NULL COMMENT '配置文件',
  rs_itemname varchar(100) NOT NULL COMMENT '配置项',
  rs_parameters varchar(1000) NOT NULL COMMENT '参数',
  rs_memo longtext COMMENT '说明',
  rs_memo_en longtext COMMENT '英文说明',
  rs_status varchar(20) DEFAULT NULL COMMENT '状态',
  rs_ctime datetime DEFAULT NULL COMMENT '创建时间',
  rs_mtime datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (rs_uid),
  UNIQUE KEY ewa_restful_un (cat_uid,rs_method)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE ewa_restful_catalog (
  cat_uid varchar(36) CHARACTER SET latin1 NOT NULL COMMENT '目录编号',
  cat_puid varchar(36) CHARACTER SET latin1 NOT NULL COMMENT '上级目录',
  cat_path varchar(200) CHARACTER SET latin1 NOT NULL COMMENT '接口地址',
  cat_path_full varchar(1000) CHARACTER SET latin1 DEFAULT NULL COMMENT '完整接口地址',
  cat_name varchar(50) NOT NULL COMMENT '名称',
  cat_name_en varchar(100) DEFAULT NULL COMMENT '英文名称',
  cat_memo longtext COMMENT '说明',
  cat_memo_en longtext COMMENT '英文说明',
  cat_lvl int NOT NULL COMMENT '级别',
  cat_ord int NOT NULL COMMENT '排序',
  cat_status varchar(20) DEFAULT NULL COMMENT '状态',
  cat_ctime datetime DEFAULT NULL COMMENT '创建时间',
  cat_mtime datetime DEFAULT NULL COMMENT '修改时间',
  p0 varchar(150) CHARACTER SET latin1 DEFAULT NULL,
  p1 varchar(150) CHARACTER SET latin1 DEFAULT NULL,
  p2 varchar(150) CHARACTER SET latin1 DEFAULT NULL,
  p3 varchar(150) CHARACTER SET latin1 DEFAULT NULL,
  p4 varchar(150) CHARACTER SET latin1 DEFAULT NULL,
  p5 varchar(150) CHARACTER SET latin1 DEFAULT NULL,
  p6 varchar(150) CHARACTER SET latin1 DEFAULT NULL,
  p7 varchar(150) CHARACTER SET latin1 DEFAULT NULL,
  p8 varchar(150) CHARACTER SET latin1 DEFAULT NULL,
  p9 varchar(150) CHARACTER SET latin1 DEFAULT NULL,
  PRIMARY KEY (cat_uid),
  KEY idx_ewa_restful_catalog_cat_puid (cat_puid),
  KEY ewa_restful_catalog_p0_IDX (p0,p1,p2,p3,p4,p5,p6,p7,p8,p9) USING BTREE,
  KEY idx_ewa_restful_catalog (cat_status,p0,p1,p2,p3,p4,p5,p6,p7,p8,p9)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```
## Module 导入/导出 tables （MYSQL）
```sql
create table ewa_mod (
  mod_code varchar(100) not null comment '模块编码',
  mod_name varchar(100) not null comment '模块名称',
  mod_name_en varchar(100) not null comment '模块名称-英文',
  mod_open_source char(1) not null comment '是否开源-y/n',
  mod_company varchar(100) default null comment '公司',
  mod_contact varchar(100) default null comment '联系人',
  mod_web varchar(200) default null comment '网址',
  mod_email varchar(100) default null comment '地址邮件',
  mod_osp varchar(100) default null comment '开源协议',
  mod_memo longtext comment '模块说明',
  mod_memo_en longtext comment '模块说明英文',
  mod_cdate datetime not null comment '创建时间',
  mod_mdate datetime not null comment '修改时间',
  mod_status varchar(10) not null comment '模块状态- new,dlv,undlv,del',
  mod_adm_id int not null comment '用户',
  mod_sup_id int not null comment '商户',
  mod_meta_db_name varchar(64) default null comment '元数据库名称',
  mod_work_db_name varchar(64) default null comment '工作数据库名称',
  mod_ewa_conn varchar(67) not null default '' comment 'ewa数据库连接池',
  primary key (mod_code)
) engine=innodb default charset=utf8mb4 comment='模块列表';

create table ewa_mod_cfgs (
  mod_ver_id bigint not null comment '版本编号',
  xmlname varchar(200) not null comment '配置文件',
  itemname varchar(100) not null comment '配置项',
  description varchar(500) not null comment '描述',
  emc_cdate datetime not null comment '创建数据',
  emc_mdate datetime not null comment '修改时间',
  emc_adm_id int not null comment '用户',
  emc_sup_id int not null comment '商户',
  emc_def_xmlname varchar(200) not null comment '默认文件',
  emc_def_itemname varchar(100) not null comment '默认配置项名称',
  primary key (mod_ver_id,xmlname,itemname)
) engine=innodb default charset=utf8mb4 comment='模块版本对应的配置项';

create table ewa_mod_data (
  mod_ver_id bigint not null comment '版本编号',
  table_catalog varchar(64) not null comment '数据库目录',
  table_schema varchar(64) not null comment '数据库',
  table_name varchar(64) not null comment '表名称',
  data_index int not null comment '记录行',
  data_row longtext not null comment '行记录'
) engine=innodb default charset=utf8mb4 comment='表的数据';

create table ewa_mod_ddl (
  emd_id bigint not null auto_increment comment '编号',
  mod_ver_id bigint not null comment '版本编号',
  emd_ewa_conn varchar(40) not null comment '数据库链接名称',
  table_name varchar(64) not null comment '表名称',
  table_catalog varchar(64) not null comment '数据库目录',
  table_schema varchar(64) not null comment '数据库',
  emd_database_type varchar(20) not null comment '表类型',
  emd_type varchar(20) not null comment '表类型',
  emd_ddl_sql longtext comment '创建用的 sql',
  emd_export varchar(10) not null comment '是否导出数据',
  emd_export_where varchar(2500) not null comment '导出数据条件',
  emd_cdate datetime not null comment '创建时间',
  emd_mdate datetime not null comment '修改时间',
  emd_adm_id int not null comment '用户',
  emd_sup_id int not null comment '商户',
  emd_xml longtext comment '导出的xml格式',
  primary key (emd_id),
  unique key ewa_mod_ddl_emd_ewa_conn_idx (emd_ewa_conn,table_name) using btree
) engine=innodb default charset=utf8mb4 comment='数据库的 ddl';

create table ewa_mod_field (
  emd_id bigint not null comment '编号',
  column_name varchar(64) not null default '',
  mod_ver_id bigint not null comment '版本编号',
  table_catalog varchar(64) not null comment '数据库目录',
  table_schema varchar(64) not null comment '数据库',
  table_name varchar(64) not null default '',
  ordinal_position bigint unsigned not null default '0',
  column_default longtext,
  is_nullable varchar(3) not null default '',
  data_type varchar(64) not null default '',
  character_maximum_length bigint unsigned default null,
  character_octet_length bigint unsigned default null,
  numeric_precision bigint unsigned default null,
  numeric_scale bigint unsigned default null,
  datetime_precision bigint unsigned default null,
  character_set_name varchar(32) default null,
  collation_name varchar(32) default null,
  column_type longtext not null,
  column_key varchar(3) not null default '',
  extra varchar(30) not null default '',
  privileges varchar(80) not null default '',
  column_comment varchar(1024) not null default '',
  generation_expression longtext not null,
  primary key (emd_id,column_name)
) engine=innodb default charset=utf8mb4;

create table ewa_mod_index (
  emd_id bigint not null comment '编号',
  index_name varchar(64) not null comment '索引名称',
  mod_ver_id bigint not null comment '版本编号',
  table_catalog varchar(64) not null comment '数据库目录',
  table_schema varchar(64) not null comment '数据库',
  table_name varchar(64) not null comment '表名称',
  index_type varchar(20) not null comment '索引类型, primary, unique, index',
  index_ddl longtext comment '创建用的sql',
  primary key (emd_id,index_name)
) engine=innodb default charset=utf8mb4 comment='表索引';

create table ewa_mod_index_field (
  mod_ver_id bigint not null comment '版本编号',
  table_catalog varchar(64) not null comment '数据库目录',
  table_schema varchar(64) not null comment '数据库',
  table_name varchar(64) not null comment '表名称',
  index_name varchar(64) not null comment '索引名称',
  column_name varchar(64) not null comment '字段名称',
  index_asc char(1) not null comment '升序=y，降序=n',
  primary key (mod_ver_id,table_catalog,table_schema,table_name,index_name,column_name)
) engine=innodb default charset=utf8mb4 comment='表索引的字段';

create table ewa_mod_package (
  mod_ver_id bigint(20) not null comment '版本号',
  pkg_dlv_date datetime not null comment '发布时间',
  pkg_len int(11) not null comment '包长度',
  pkg_md5 varchar(32) not null comment '包md5',
  pkg_sup_id int(11) not null comment '商户',
  pkg_adm_id int(11) not null comment '员工',
  pkg_file longblob comment '包数据',
  pkg_publish_time datetime default null comment '发布时间',
  pkg_publish_url varchar(400) default null comment '发布网址',
  pkg_publish_result text comment '发布结果',
  pkg_publish_status varchar(10) default null comment '发布状态',
  primary key (mod_ver_id)
) engine=innodb default charset=utf8mb4 comment='模块包数据-zip';

create table ewa_mod_ver (
  mod_ver_id bigint not null comment '版本编号',
  mod_code varchar(100) not null comment '模块编码',
  mod_ver varchar(30) not null comment '模块版本',
  mod_ver_cdate datetime not null comment '创建时间',
  mod_ver_mdate datetime not null comment '修改时间',
  mod_ver_memo longtext comment '模块版本说明',
  mod_ver_memo_en longtext comment '模块版本说明英文',
  mod_ver_status varchar(10) not null comment '模块状态- used,del',
  mod_ver_adm_id int not null comment '用户',
  mod_ver_sup_id int not null comment '商户',
  primary key (mod_ver_id),
  unique key ewa_mod_ver_mod_code_idx (mod_code,mod_ver) using btree
) engine=innodb default charset=utf8mb4 comment='模块版本';

create table ewa_mod_download (
  mod_dl_id int not null auto_increment comment '下载序号',
  mod_code varchar(100) not null comment '模块编码',
  mod_ver varchar(30) not null comment '模块版本',
  mod_name varchar(100) not null comment '模块名称',
  mod_name_en varchar(100) default null comment '模块名称-英文',
  mod_open_source char(1) default null comment '是否开源-y/n',
  mod_company varchar(100) default null comment '公司',
  mod_contact varchar(100) default null comment '联系人',
  mod_web varchar(200) default null comment '网址',
  mod_email varchar(100) default null comment '地址邮件',
  mod_osp varchar(100) default null comment '开源协议',
  mod_memo longtext comment '模块说明',
  mod_memo_en longtext comment '模块说明英文',
  mod_cdate datetime default null comment '创建时间',
  mod_mdate datetime default null comment '修改时间',
  mod_sup_id int default null comment '商户',
  mod_ver_id bigint default null comment '版本编号',
  mod_ver_cdate datetime default null comment '创建时间',
  mod_ver_mdate datetime default null comment '修改时间',
  mod_ver_memo longtext comment '模块版本说明',
  mod_ver_memo_en longtext comment '模块版本说明英文',
  mod_ver_status varchar(10) default null comment '模块状态 ',
  pkg_len int not null comment '包大小',
  pkg_md5 varchar(32) not null comment '包md5',
  pkg_file longblob comment '包内容',
  mod_dl_cdate datetime not null comment '创建时间',
  mod_dl_mdate datetime not null comment '修改时间',
  mod_dl_status varchar(10) not null comment '模块状态- used,del',
  mod_dl_sup_id int not null comment '商户',
  mod_dl_url varchar(700) default null comment '下载网址',
  import_data_conn varchar(50) default null comment '导入数据的连接名称',
  replace_meta_databasename varchar(64) default null comment '替换元数据库名称',
  replace_work_databasename varchar(64) default null comment '替换工作据库名称',
  primary key (mod_dl_id)
) engine=innodb default charset=utf8mb4 comment '下载的模块包';
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
    <version>1.1.1</version>
</dependency>
```