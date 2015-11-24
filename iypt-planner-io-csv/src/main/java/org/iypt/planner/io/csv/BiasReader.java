package org.iypt.planner.io.csv;

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;

/**
 *
 * @author jlocker
 */
public class BiasReader {

    public static class BiasRow {

        private String given_name;
        private String last_name;
        private double bias;

        public void setBias(double bias) {
            this.bias = bias;
        }

        public void setLast_name(String last_name) {
            this.last_name = last_name;
        }

        public void setGiven_name(String given_name) {
            this.given_name = given_name;
        }

        public String getFullName() {
            return String.format("%s %s", given_name, last_name);
        }

        public double getBias() {
            return bias;
        }
    }
    private static final CellProcessor[] processors = new CellProcessor[]{
        new NotNull(),
        new NotNull(),
        new ParseDouble()
    };
    private final Map<String, Double> rows = new HashMap<>(100);

    public Map<String, Double> read(Reader reader) throws IOException {
        if (!rows.isEmpty()) {
            throw new UnsupportedOperationException("Repeated reading not supported.");
        }
        try (ICsvBeanReader beanReader = new CsvBeanReader(reader, CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE)) {

            // the header elements are used to map the values to the bean (names must match)
            final String[] header = beanReader.getHeader(true);
            BiasRow bean;
            while ((bean = beanReader.read(BiasRow.class, header, processors)) != null) {
                rows.put(bean.getFullName(), bean.getBias());
            }

        }
        return getRows();
    }

    public Map<String, Double> getRows() {
        return Collections.unmodifiableMap(rows);
    }

    public double getBias(String jurorName) {
        if (!rows.containsKey(jurorName)) {
            return 0;
        }
        return rows.get(jurorName);
    }
}
