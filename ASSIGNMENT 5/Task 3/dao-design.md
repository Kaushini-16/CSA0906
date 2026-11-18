# Task 3 output – A3 DAO design

```java
interface Dao<T, K> {
    T insert(T value) throws SQLException;
    Optional<T> findById(K id) throws SQLException;
    List<T> findAll() throws SQLException;
    boolean update(T value) throws SQLException;
    boolean delete(K id) throws SQLException;
}
```

`StudentDao implements Dao<Student, String>` and contains SQL statements and ResultSet-to-Student mapping. `Student.registerNumber` maps to `student.register_no` and is the primary key. Other Student fields map to their corresponding table columns.

Repeated DAO work includes connection/resource handling, SQL execution, result mapping and exception handling. Entity/table/column names and key conditions differ. Putting SQL in GUI/service code spreads schema changes across multiple locations; a DAO centralizes those changes. Per-operation database time/space complexity is essentially unchanged.
