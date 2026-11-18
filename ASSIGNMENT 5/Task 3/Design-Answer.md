## A3 – Data-access pattern

### A3.1 Common pattern
Repeated steps: acquire connection, prepare/execute SQL, map `ResultSet` rows to objects, handle exceptions, close resources. Differences: entity type, table/columns, key and SQL specifics.

### A3.2 Generic DAO
```java
interface Dao<T, K> {
  T insert(T value) throws SQLException;
  Optional<T> findById(K id) throws SQLException;
  List<T> findAll() throws SQLException;
  boolean update(T value) throws SQLException;
  boolean delete(K id) throws SQLException;
}
```
`StudentDao implements Dao<Student, String>` and owns SQL plus row-to-object mapping. Service/UI code calls the interface and does not construct SQL.

### A3.3 Object/table mapping
`Student.registerNumber` → `student.register_no` (primary key); `name` → `student.name`; other Student properties map to corresponding columns. Keep mapping in the DAO/repository layer.

**Alternative – SQL in service/GUI:** renaming a column requires finding and changing every SQL statement in each client/service location. DAO centralizes changes. Runtime/space complexity per query is essentially unchanged; maintainability and coupling differ, not database asymptotic cost.
