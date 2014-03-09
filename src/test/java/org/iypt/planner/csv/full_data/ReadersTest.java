package org.iypt.planner.csv.full_data;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.iypt.planner.csv.full_data.FightReader.FightRow;
import org.iypt.planner.csv.full_data.JurorReader.JurorRow;
import org.iypt.planner.csv.full_data.MarkReader.MarkRow;
import org.iypt.planner.csv.full_data.PersonReader.PersonRow;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author jlocker
 */
public class ReadersTest {

    private static CsvTablesProcessor processor;

    @BeforeClass
    public static void setUpClass() throws IOException {
        processor = new CsvTablesProcessor();
        processor.process(new InputStreamReader(ReadersTest.class.getResourceAsStream("full_data.csv"), StandardCharsets.UTF_8));
    }

    @Test
    public void testTournamentReader() throws Exception {
        TournamentReader reader = new TournamentReader();
        reader.read(processor);
        assertThat(reader.getRows().get(9).getTournament_name()).isEqualTo("IYPT2012");
    }

    @Test
    public void testPersonReader() throws Exception {
        PersonReader reader = new PersonReader();
        reader.read(processor);
        PersonRow person = reader.getRows().get(9437);
        assertThat(person.getFull_name()).isEqualTo("Prapun Manyum");
        assertThat(person.isJuror()).isTrue();

        assertThat(reader.getRows().get(9416).getFull_name()).isEqualTo("Tomáš Bzdušek");
        assertThat(reader.getRows().get(9425).getFull_name()).isEqualTo("Władysław Borgieł");
        assertThat(reader.getRows().get(9427).getFull_name()).isEqualTo("František Kundracik");
    }

    @Test
    public void testFightReader() throws Exception {
        FightReader reader = new FightReader();
        reader.read(processor);
        FightRow fight5K = reader.getRows().get(1452);
        FightRow fightFinal = reader.getRows().get(1453);
        assertThat(fight5K.getGroup_name()).isEqualTo("Group K");
        assertThat(fight5K.getRound()).isEqualTo(5);
        assertThat(fight5K.getTeam_4()).isNull();
        assertThat(fightFinal.getGroup_name()).isEqualTo("Final");
        assertThat(fightFinal.getRound()).isEqualTo(6);
        assertThat(fightFinal.getTeam_1()).isEqualTo(1182);
        assertThat(fightFinal.getTeam_2()).isEqualTo(1188);
        assertThat(fightFinal.getTeam_3()).isEqualTo(1180);
        assertThat(fightFinal.getTeam_4()).isNull();
    }

    @Test
    public void testMarkReader() throws Exception {
        MarkReader reader = new MarkReader();
        reader.read(processor);
        // 55112,1453,3,1,3,8
        MarkRow mark = reader.getRows().get(55112);
        assertThat(mark.getFight()).isEqualTo(1453);
        assertThat(mark.getStage()).isEqualTo(3);
        assertThat(mark.getJuror_number()).isEqualTo(1);
        assertThat(mark.getRole()).isEqualTo(3);
        assertThat(mark.getMark()).isEqualTo(8);
    }

    @Test
    public void testJurorReader() throws Exception {
        JurorReader reader = new JurorReader();
        reader.read(processor);
        // 7345,1453,9437,1
        JurorRow juror = reader.getRows().get(7345);
        assertThat(juror.getFight()).isEqualTo(1453);
        assertThat(juror.getJuror()).isEqualTo(9437);
        assertThat(juror.getJuror_number()).isEqualTo(1);
    }
}
