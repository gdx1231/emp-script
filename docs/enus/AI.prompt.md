# Data operations using the com.gdxsoft.easyweb emp-script library
# Source code: https://github.com/gdx1231/emp-script

## Querying data with SQL
```java
String sql = "SELECT * FROM table_name WHERE id=@id and status=@status";
RequestValue rv = new RequestValue();
rv.addOrUpdateValue("id", someId);
rv.addOrUpdateValue("status", someStatus);
DTTable tb = DTTable.getJdbcTable(sql, rv);
// Process result set
for (int i = 0; i < tb.getCount(); i++) {
    String name = tb.getCell(i, "name").toString();
    int age = tb.getCell(i, "age").toInt();
    Date date = tb.getCell(i, "created_at").toTime();
}
```
## Insert usage
```java
String sql = "INSERT INTO table_name (name, age, created_at) VALUES (@name, @age, @created_at)";
RequestValue rv = new RequestValue();
rv.addOrUpdateValue("name", "John Doe");
rv.addOrUpdateValue("age", 30);
rv.addOrUpdateValue("created_at", new Date());
DataConnection.updateAndClose(sql, "", rv);
```
## Update usage
```java
String sql = "UPDATE table_name SET name=@name, age=@age WHERE id=@id";
RequestValue rv = new RequestValue();
rv.addOrUpdateValue("name", "Jane Doe");
rv.addOrUpdateValue("age", 28);
rv.addOrUpdateValue("id", someId);
DataConnection.updateAndClose(sql, "", rv);
```
## Delete usage
```java
String sql = "DELETE FROM table_name WHERE id=@id";
RequestValue rv = new RequestValue();
rv.addOrUpdateValue("id", someId);
DataConnection.updateAndClose(sql, "", rv);
```
