package org.iypt.planner.io.csv.full_data.readers;

import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.ift.BoolCellProcessor;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.cellprocessor.ift.DateCellProcessor;
import org.supercsv.cellprocessor.ift.DoubleCellProcessor;
import org.supercsv.cellprocessor.ift.LongCellProcessor;
import org.supercsv.cellprocessor.ift.StringCellProcessor;
import org.supercsv.util.CsvContext;

/**
 *
 * @author jlocker
 */
public class ParseNull extends CellProcessorAdaptor implements BoolCellProcessor, DateCellProcessor,
        DoubleCellProcessor, LongCellProcessor, StringCellProcessor {

    public static final String DEFAULT_NULL_VALUE = "<NULL>";
    private String nullValue;

    public ParseNull() {
        this(DEFAULT_NULL_VALUE);
    }

    public ParseNull(String nullValue) {
        super();
        this.nullValue = nullValue;
    }

    public ParseNull(String nullValue, CellProcessor next) {
        super(next);
        this.nullValue = nullValue;
    }

    @Override
    public Object execute(Object value, CsvContext context) {
        if (nullValue.equals(value)) {
            return null;
        }

        return next.execute(value, context);
    }
}
