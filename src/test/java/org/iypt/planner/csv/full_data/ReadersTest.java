package org.iypt.planner.csv.full_data;

import java.io.IOException;
import java.io.InputStreamReader;
import org.iypt.planner.csv.full_data.FightReader.FightRow;
import org.iypt.planner.csv.full_data.JurorReader.JurorRow;
import org.iypt.planner.csv.full_data.MarkReader.MarkRow;
import org.iypt.planner.csv.full_data.PersonReader.PersonRow;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 *
 * @author jlocker
 */
public class ReadersTest {

    private static CsvTablesProcessor processor;

    @BeforeClass
    public static void setUpClass() throws IOException {
        processor = new CsvTablesProcessor();
        processor.process(new InputStreamReader(ReadersTest.class.getResourceAsStream("full_data.csv")));
    }

    @Test
    public void testTournamentReader() throws Exception {
        TournamentReader reader = new TournamentReader();
        reader.read(processor);
        assertThat(reader.getRows().get(9).getTournament_name(), is("IYPT2012"));
    }

    @Test
    public void testPersonReader() throws Exception {
        PersonReader reader = new PersonReader();
        reader.read(processor);
        PersonRow person = reader.getRows().get(9437);
        assertThat(person.getFull_name(), is("Prapun Manyum"));
        assertThat(person.isJuror(), is(true));
    }

    @Test
    public void testFightReader() throws Exception {
        FightReader reader = new FightReader();
        reader.read(processor);
        FightRow fight5K = reader.getRows().get(1452);
        FightRow fightFinal = reader.getRows().get(1453);
        assertThat(fight5K.getGroup_name(), is("Group K"));
        assertThat(fight5K.getRound(), is(5));
        assertThat(fight5K.getTeam_4(), is(nullValue()));
        assertThat(fightFinal.getGroup_name(), is("Final"));
        assertThat(fightFinal.getRound(), is(6));
        assertThat(fightFinal.getTeam_1(), is(1182));
        assertThat(fightFinal.getTeam_2(), is(1188));
        assertThat(fightFinal.getTeam_3(), is(1180));
        assertThat(fightFinal.getTeam_4(), is(nullValue()));
    }

    @Test
    public void testMarkReader() throws Exception {
        MarkReader reader = new MarkReader();
        reader.read(processor);
        // 55112,1453,3,1,3,8
        MarkRow mark = reader.getRows().get(55112);
        assertThat(mark.getFight(), is(1453));
        assertThat(mark.getStage(), is(3));
        assertThat(mark.getJuror_number(), is(1));
        assertThat(mark.getRole(), is(3));
        assertThat(mark.getMark(), is(8));
    }

    @Test
    public void testJurorReader() throws Exception {
        JurorReader reader = new JurorReader();
        reader.read(processor);
        // 7345,1453,9437,1
        JurorRow juror = reader.getRows().get(7345);
        assertThat(juror.getFight(), is(1453));
        assertThat(juror.getJuror(), is(9437));
        assertThat(juror.getJuror_number(), is(1));
    }
}
