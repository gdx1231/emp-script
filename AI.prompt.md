# 数据操作使用库为 com.gdxsoft.easyweb emp-script
# 源代码：https://github.com/gdx1231/emp-script 

## SQL获取数据的用法
```java
String sql = "SELECT * FROM table_name WHERE id=@id and status=@status";
RequestValue rv = new RequestValue();
rv.addOrUpdateValue("id", someId);
rv.addOrUpdateValue("status", someStatus);
DTTable tb = DTTable.getJdbcTable(sql, rv);
// 处理结果集
for (int i = 0; i < tb.getCount(); i++) {
    String name = tb.getCell(i, "name").toString();
    int age = tb.getCell(i, "age").toInt();
    Date date = tb.getCell(i, "created_at").toTime();
}
```
## insert语句的用法
```java
String sql = "INSERT INTO table_name (name, age, created_at) VALUES (@name, @age, @created_at)";
RequestValue rv = new RequestValue();
rv.addOrUpdateValue("name", "John Doe");
rv.addOrUpdateValue("age", 30);
rv.addOrUpdateValue("created_at", new Date());
DataConnection.updateAndClose(sql, "", rv);
```
## update语句的用法
```java
String sql = "UPDATE table_name SET name=@name, age=@age WHERE id=@id";
RequestValue rv = new RequestValue();
rv.addOrUpdateValue("name", "Jane Doe");
rv.addOrUpdateValue("age", 28);
rv.addOrUpdateValue("id", someId);
DataConnection.updateAndClose(sql, "", rv);
```
## delete语句的用法
```java
String sql = "DELETE FROM table_name WHERE id=@id";
RequestValue rv = new RequestValue();
rv.addOrUpdateValue("id", someId);
DataConnection.updateAndClose(sql, "", rv);
```
 