package org.iypt.planner.csv.full_data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 *
 * @author jlocker
 */
public class CsvTablesProcessorTest {

    @Test
    public void testProcess() throws IOException {
        CsvTablesProcessor proc = new CsvTablesProcessor();
        proc.process(new InputStreamReader(CsvTablesProcessorTest.class.getResourceAsStream("full_data.csv")));

        String[] tables = {"Tournaments", "Persons", "Problems", "Teams", "Fights", "Locks", "Marks",
            "Jurors", "Members", "Stages", "Rejections", "FightStatusCache"};
        assertThat(proc.getTables()).hasSize(tables.length);
        assertThat(proc.getTables()).containsOnly(tables);
        assertThat(new BufferedReader(proc.getTableReader("Tournaments")).readLine()).isEqualTo("id,tournament_name,rule_set");

        try {
            proc.process(new InputStreamReader(CsvTablesProcessorTest.class.getResourceAsStream("full_data.csv")));
            fail("It is possible to overwrite tables that have been read previously.");
        } catch (IllegalStateException ex) {
            if (!ex.getMessage().contains("Tournaments")) {
                throw ex;
            }
        }
    }
}
