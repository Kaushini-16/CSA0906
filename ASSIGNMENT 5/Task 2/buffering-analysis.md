# Task 2 output – A2 Buffering analysis

For a 50 MiB file, one-byte-at-a-time processing makes approximately 52,428,800 Java `read()` calls (excluding EOF). The underlying operating system may buffer disk access, but per-call overhead is still high.

With an 8 KiB buffer, underlying buffer refills are approximately:
`ceil(52,428,800 / 8,192) = 6,400`

Both approaches are O(n) time. Buffered streaming uses O(buffer size) extra memory. `Files.readAllLines()` uses O(file size) memory (often substantially more than the raw file due to String/list overhead). Use it only when the complete file comfortably fits in memory.
