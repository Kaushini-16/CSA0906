# Task 5 output – A5 Transactions and injection

## Wallet transfer
```text
save old autoCommit
set autoCommit false
try:
    debit 500 from hostel wallet only if balance >= 500
    verify exactly one row changed
    credit 500 to canteen wallet
    verify exactly one row changed
    commit
catch error:
    rollback
    report failure
finally:
    restore old autoCommit
```

Both updates must use the same transaction/connection. Two independent auto-commit updates can leave money debited but not credited if the second update fails.

## Safe login
```java
String sql = "SELECT * FROM users WHERE id=? AND pwd=?";
try (PreparedStatement ps = con.prepareStatement(sql)) {
    ps.setString(1, id);
    ps.setString(2, pwd);
    try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
    }
}
```
String concatenation can allow quote/operator/comment input to change SQL logic. PreparedStatement binds user input as data rather than SQL syntax.
