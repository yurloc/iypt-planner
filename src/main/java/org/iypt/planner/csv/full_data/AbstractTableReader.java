package org.iypt.planner.csv.full_data;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;

/**
 *
 * @param <T> bean type representing a table row
 * @author jlocker
 */
public abstract class AbstractTableReader<T extends HasIntId> {

    private final Class<T> clazz;
    private final Map<Integer, T> rows = new HashMap<>();

    public abstract String getTableName();

    public abstract CellProcessor[] getProcessors();

    protected AbstractTableReader(Class<T> clazz) {
        this.clazz = clazz;
    }

    public Map<Integer, T> read(CsvTablesProcessor tablesProcessor) throws IOException {
        if (!rows.isEmpty()) {
            throw new UnsupportedOperationException("Repeated reading not supported.");
        }
        try (ICsvBeanReader beanReader = new CsvBeanReader(
                        tablesProcessor.getTableReader(getTableName()),
                        CsvPreference.STANDARD_PREFERENCE)) {

            // the header elements are used to map the values to the bean (names must match)
            final String[] header = beanReader.getHeader(true);
            final CellProcessor[] processors = getProcessors();
            T bean;
            while ((bean = beanReader.read(clazz, header, processors)) != null) {
                rows.put(bean.getId(), bean);
            }

        }
        return getRows();
    }

    public Map<Integer, T> getRows() {
        return Collections.unmodifiableMap(rows);
    }
}
