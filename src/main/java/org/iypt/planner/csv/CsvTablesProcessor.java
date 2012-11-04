package org.iypt.planner.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author jlocker
 */
public class CsvTablesProcessor {

    private Map<String, StringBuilder> tables = new HashMap<>();

    public void process(Reader reader) throws IOException {
        try (BufferedReader br = new BufferedReader(reader)) {
            String line = br.readLine();
            while (line != null) {
                // read until table or EOF is found
                while (line != null && !line.startsWith("TABLE ")) {
                    line = br.readLine();
                }

                if (line == null) {
                    break;
                }
                String table = line.replaceFirst("TABLE ", "");
                if (tables.containsKey(table)) {
                    throw new IllegalStateException("Table '" + table + "' already processed.");
                }
                StringBuilder sb = new StringBuilder(4096);
                tables.put(table, sb);
                line = br.readLine();
                // write the header line (and drop the table name prefix)
                sb.append(line.replaceAll(table + "\\.", "")).append('\n');

                // write all table records
                line = br.readLine();
                while (!line.trim().isEmpty()) {
                    sb.append(line).append('\n');
                    line = br.readLine();
                }

                // process next table
            }
        } catch (IOException ex) {
            throw ex;
        }
    }

    public Set<String> getTables() {
        return tables.keySet();
    }

    public Reader getTableReader(String table) {
        return new StringReader(tables.get(table).toString());
    }
}
