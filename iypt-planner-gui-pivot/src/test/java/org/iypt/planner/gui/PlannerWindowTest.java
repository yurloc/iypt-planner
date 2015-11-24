package org.iypt.planner.gui;

import java.io.IOException;
import java.util.ServiceLoader;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.iypt.planner.api.domain.Schedule;
import org.iypt.planner.api.domain.Tournament;
import org.iypt.planner.api.io.InputSource;
import org.iypt.planner.api.io.TournamentImporter;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PlannerWindowTest implements PlanerWindowListener {

    private final BlockingQueue<Boolean> bq = new ArrayBlockingQueue<>(1);
    private PlannerWindow window;
    private Schedule schedule;

    @Before
    public void setUp() throws IOException, SerializationException, InterruptedException {
        TournamentImporter importer = ServiceLoader.load(TournamentImporter.class).iterator().next();
        InputSource.ClasspathFactory f = InputSource.newClasspathFactory(PlannerWindow.class, "/org/iypt/planner/csv/");
        Tournament t = importer.loadTournament(
                f.newInputSource("team_data.csv"),
                f.newInputSource("jury_data.csv"));
        schedule = importer.loadSchedule(t, f.newInputSource("schedule2012.csv"));
        importer.loadBiases(t, f.newInputSource("bias_IYPT2012.csv"));

        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        window = (PlannerWindow) bxmlSerializer.readObject(PlannerApplication.class, "planner.bxml");
        window.getPlannerWindowListeners().add(this);
        window.initializeSolver();
        Boolean result = bq.poll(30, TimeUnit.SECONDS);
        assertThat(result).isTrue();
    }

    @Override
    public void solverCreated(boolean success) {
        bq.add(success);
    }

    @Test
    public void testInitialization() {
        window.setTournament(schedule);
        assertThat(window.getSchedule().getSchedule().getTournament().getScore()).isNotNull();
        assertThat(window.getSchedule().getSelectedRound());
    }
}
