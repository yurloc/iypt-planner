package org.iypt.planner.csv;

import java.io.IOException;
import java.io.InputStreamReader;
import org.assertj.core.data.Offset;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

/**
 *
 * @author jlocker
 */
public class BiasReaderTest {

    private static final Offset<Double> OFFSET = offset(Double.MIN_VALUE);

    @Test
    public void testData() throws IOException {
        BiasReader reader = new BiasReader();
        reader.read(new InputStreamReader(BiasReaderTest.class.getResourceAsStream("bias_IYPT2012.csv")));
        assertThat(reader.getRows()).hasSize(82);
        assertThat(reader.getBias("Martin Plesch")).isEqualTo(-0.7, OFFSET);
        assertThat(reader.getBias("Joy Tan")).isEqualTo(0d, OFFSET);
        assertThat(reader.getBias("Michael Gierling")).isEqualTo(0.89, OFFSET);
    }
}
