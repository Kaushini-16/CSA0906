## A5 – Correctness and safety

### A5.1 Transaction pseudocode
```text
BEGIN
  SET autoCommit = false
  TRY
    UPDATE wallet SET balance = balance - 500 WHERE wallet_id = 'HOSTEL'
    REQUIRE exactly one row updated and sufficient balance
    UPDATE wallet SET balance = balance + 500 WHERE wallet_id = 'CANTEEN'
    REQUIRE exactly one row updated
    COMMIT
  CATCH error
    ROLLBACK
    REPORT failure
  FINALLY
    RESTORE previous autoCommit setting
END
```
Use conditional debit (`balance >= 500`) and verify affected-row counts. Both updates must share one database transaction.

### A5.2 SQL injection
String concatenation allows input to alter SQL syntax; an attacker may supply quote/operator/comment characters to bypass the password predicate. Use `PreparedStatement` with placeholders and `setString` for both values. Parameters are treated as data, not executable SQL.

**Alternative – escaping/regex:** escaping and validation are easy to get wrong, depend on SQL dialect/encoding and can miss edge cases. Prepared statements separate SQL structure from data. Validation adds O(length of input) work; parameter binding has similarly small linear input handling, but provides robust structural separation.
