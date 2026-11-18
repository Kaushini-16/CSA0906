## A2 – Buffering

### A2.1 Cost prediction
A 50 MiB file read one byte at a time makes approximately 52,428,800 Java `read()` calls (50 × 1,048,576), ignoring EOF. A call is not necessarily one physical disk operation, but per-call overhead and frequent transitions through the I/O stack make it slow.

### A2.2 Buffered redesign
Wrap `FileInputStream` in `BufferedInputStream` (or read a byte array in blocks). With a 8 KiB buffer, the number of underlying refill operations is approximately `ceil(fileBytes / 8192)` rather than one per byte. Count newline bytes while iterating through the buffer. Time remains O(n), auxiliary memory is O(buffer size).

### A2.3 When buffering helps less
If the source is already memory-backed or the consumer performs expensive processing per byte, I/O buffering may not be the bottleneck. It can also provide little benefit for tiny files.

**Alternative – `Files.readAllLines`:** still O(n) time but uses O(n) memory for all lines and String objects. For 50 MiB, actual heap use can be substantially larger than 50 MiB. Use for small files that comfortably fit in memory; use streaming/buffering for large or unbounded logs.
