package org.iypt.planner.csv.full_data;

import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.Collection;
import org.supercsv.cellprocessor.FmtNumber;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

/**
 *
 * @author jlocker
 */
public class BiasWriter {

    public class JurorBiasRow {

        private final Juror juror;

        public JurorBiasRow(Juror juror) {
            this.juror = juror;
        }

        public String getGiven_name() {
            return juror.getGivenName();
        }

        public String getLast_name() {
            return juror.getLastName();
        }

        public float getBias() {
            float bias = juror.getAverageBias();
            return Float.isNaN(bias) ? 0 : bias;
        }
    }
    private static final String[] header = new String[]{"given_name", "last_name", "bias"};
    private final CellProcessor[] processors = new CellProcessor[]{
        new NotNull(),
        new NotNull(),
        new FmtNumber(new DecimalFormat("#.##"))
    };

    public BiasWriter(Collection<Juror> jurors) {
        this.jurors = jurors;
    }
    private final Collection<Juror> jurors;

    public void write(Writer writer) throws IOException {
        try (ICsvBeanWriter beanWriter = new CsvBeanWriter(writer, CsvPreference.STANDARD_PREFERENCE)) {

            // write the header
            beanWriter.writeHeader(header);

            // write the beans
            for (final Juror juror : jurors) {
                beanWriter.write(new JurorBiasRow(juror), header, processors);
            }
        }
    }
}
