package org.iypt.planner.csv;

import java.io.IOException;
import java.io.InputStreamReader;
import org.junit.Test;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 * @author jlocker
 */
public class BiasReaderTest {

    private static final double ERROR = Double.MIN_VALUE;

    @Test
    public void testData() throws IOException {
        BiasReader reader = new BiasReader();
        reader.read(new InputStreamReader(BiasReaderTest.class.getResourceAsStream("bias_IYPT2012.csv")));
        assertThat(reader.getRows().size(), is(82));
        assertThat(reader.getBias("Martin Plesch"), closeTo(-0.7, ERROR));
        assertThat(reader.getBias("Joy Tan"), closeTo(0d, ERROR));
        assertThat(reader.getBias("Michael Gierling"), closeTo(0.89, ERROR));
    }
}
