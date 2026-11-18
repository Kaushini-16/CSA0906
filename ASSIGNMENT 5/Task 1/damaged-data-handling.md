# Task 1 output – Damaged data handling

- Damaged line: validate field count, required values, numeric fields and date; report line number and skip it.
- Missing file: treat as empty only when that is the intended first-run behaviour; distinguish permission/path errors.
- Incomplete append after power loss: detect malformed final record; preserve a backup and use temp-file + atomic replace for safer whole-file updates.
- Fixed-size alternative: `RandomAccessFile.seek(index * recordSize)` gives O(1) access by record index; ID lookup remains O(n) without an index. Padding wastes space.
