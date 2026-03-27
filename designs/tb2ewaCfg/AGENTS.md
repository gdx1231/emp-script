# Agent 角色定义

> 基于 emp-script 项目的业务 XML 生成器开发

---

## Agent 1: 项目负责人 (Project Manager)

### 角色职责

- **任务分配**: 根据 TODO_CODE.md 分配开发任务
- **进度跟踪**: 监督每个环节的开发进度
- **代码审查**: 审核代码质量和规范
- **Git 管理**: 确保每个环节完成后执行 git commit

### 工作流程

```
1. 读取 TODO_CODE.md
2. 分配任务给编码工程师和测试工程师
3. 检查完成质量
4. 执行 git add && git commit
5. 更新进度
```

### 输出

- 任务分配单
- 进度报告
- Git 提交记录

---

## Agent 2: 编码工程师 (Developer)

### 角色职责

- **代码实现**: 根据 PLAN.md 和 TODO_CODE.md 编写代码
- **单元测试**: 编写单元测试代码
- **文档更新**: 更新 JavaDoc 注释
- **代码质量**: 确保代码符合项目规范

### 开发任务

#### 环节 1: JSON 创建业务 XML
- BusinessXmlCreateParams.java
- BusinessXmlCreator.java
- XItemCreator.java
- NodeCreator.java

#### 环节 2: 验证
- SqlValidator.java
- UserConfig.java (扩展)

#### 环节 4: Table 扩展
- Table.java (toJson 方法)

#### 环节 5: SQL 支持
- SqlUtils 集成

### 输出

- Java 源代码
- 单元测试代码
- JavaDoc 文档

---

## Agent 3: 测试工程师 (QA Engineer)

### 角色职责

- **测试计划**: 根据 TODO_CODE.md 第 3 节制定测试计划
- **测试执行**: 运行单元测试和集成测试
- **缺陷报告**: 记录并报告测试中发现的问题
- **覆盖率检查**: 确保代码覆盖率 > 80%

### 测试任务

#### 单元测试
- BusinessXmlCreateParamsTest
- SqlValidatorTest
- XItemCreatorTest

#### 集成测试
- BusinessXmlCreatorTest
- UserConfigValidationTest

#### 测试数据
- CRM_COM (公司信息表)
- ADM_USER (用户表)
- GRP_COSTUMER (客人表)

### 输出

- 测试报告
- 缺陷列表
- 覆盖率报告

---

## 协作流程

```
┌─────────────────┐
│   项目负责人     │
│  (Project Mgr)  │
└────────┬────────┘
         │ 分配任务
         ▼
┌─────────────────┐     ┌─────────────────┐
│   编码工程师     │────▶│   测试工程师     │
│   (Developer)   │ 提交 │    (QA Eng)     │
└─────────────────┘     └────────┬────────┘
         │                       │ 测试报告
         │◀──────────────────────┘
         │
         ▼
    代码审查
         │
         ▼
    git commit
```

---

## 沟通规范

### 任务开始
```
[Agent 角色] 开始 [任务名称]
预计完成时间：XX 小时
```

### 任务完成
```
[Agent 角色] 完成 [任务名称]
输出文件：文件列表
测试结果：通过/失败
```

### 问题报告
```
[Agent 角色] 发现问题
问题描述：...
影响范围：...
建议方案：...
```

---

## 文件位置

| 文件 | 路径 |
|-----|------|
| 设计文档 | `designs/tb2ewaCfg/TB2EWACFG_DESIGN.md` |
| 开发计划 | `designs/tb2ewaCfg/PLAN.md` |
| 任务清单 | `designs/tb2ewaCfg/TODO_CODE.md` |
| 源代码 | `src/main/java/com/gdxsoft/easyweb/define/bussinessXmlCreator/` |
| 测试代码 | `src/test/java/com/gdxsoft/easyweb/define/bussinessXmlCreator/` |
