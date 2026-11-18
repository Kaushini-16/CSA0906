# Task 1 output – Operations pseudocode

## Add pass
1. Validate all fields.
2. Open a UTF-8 buffered writer in CREATE + APPEND mode.
3. Encode the record using the documented CSV rules.
4. Write the record and newline.
5. Close the writer using try-with-resources.

## Find pass by ID
1. If the file does not exist, return NOT_FOUND.
2. Open a buffered reader.
3. Read each line and increment line number.
4. Parse and validate the line; report and skip malformed lines.
5. If pass ID matches, return the record.
6. If end-of-file is reached, return NOT_FOUND.

## List passes by route
1. Open a buffered reader.
2. Read and validate each line.
3. Append/emit records whose route number matches.
4. Report malformed records and continue.
