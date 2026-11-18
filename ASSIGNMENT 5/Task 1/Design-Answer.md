## A1 – Design a file format

### A1.1 Record layouts
**Delimited text:** one record per line, fields in this order:
`passId,regNo,name,routeNo,validityDate,amountPaid`.
Use UTF-8 and a comma delimiter. Quote/escape fields containing commas, quotes or line breaks (for example CSV quoting: double embedded quotes). Do not use naïve `split(",")` for a production CSV parser. For this assignment's simple implementation, constrain names to exclude commas/newlines and reject malformed rows. The supplied Java implementation uses five columns matching the provided Complete-the-Code example; add validity date if your final design stores the full seven-field scenario.

**Binary (`DataOutputStream`):** write fields in a documented order, e.g. `writeUTF(passId)`, `writeUTF(regNo)`, `writeUTF(name)`, `writeInt(routeNo)`, `writeUTF(validityDate)`, `writeDouble(amountPaid)`. Strings are length-prefixed by `writeUTF`; numeric fields have fixed-width encodings. Read in exactly the same order with `DataInputStream`.

### A1.2 Operations (text-file design)
- **Add:** open `Files.newBufferedWriter` with `CREATE` and `APPEND`; validate fields; write one encoded record and newline; close using try-with-resources.
- **Find by ID:** use `BufferedReader` to read one line at a time; parse/validate each line; skip and report malformed records; return immediately when the ID matches. This is O(n) time and O(1) auxiliary space.
- **List by route:** scan with `BufferedReader`; parse valid rows; collect or stream matching records. This is O(n) time; output storage is O(k) if k results are collected, otherwise O(1).
`FileReader`/`FileWriter` are character streams but default-charset-sensitive in older Java APIs; prefer explicit UTF-8 via `Files.newBufferedReader/newBufferedWriter`. `FileInputStream/FileOutputStream` are byte streams and suit binary data, not ordinary text. Buffering reduces small read/write operations.

### A1.3 Damaged data and failures
Validate field count, required values, numeric conversion and date format. Log the line number and reason; skip invalid rows rather than aborting the entire scan. If the file is missing, distinguish an empty store from an unexpected path/permission error; create it on first add. For safer writes, write a complete replacement to a temporary file, flush/close it, then atomically move it over the original where supported. A simple append can still leave a partial final line after power loss; detect it as malformed on the next read and preserve/backup the damaged file.

**Alternative – fixed-length `RandomAccessFile`:** assign each record a fixed record size and calculate byte offset `index × recordSize`, giving O(1) access by record index. Finding by pass ID still needs O(n) unless a separate ID-to-index index is maintained. Padding wastes space for short names/IDs; variable-length values complicate fixed offsets.
