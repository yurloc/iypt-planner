package org.iypt.planner.gui;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.iypt.planner.csv.CSVTournamentFactory;
import org.iypt.planner.domain.Tournament;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PlannerWindowTest implements PlanerWindowListener {

    private final BlockingQueue<Boolean> bq = new ArrayBlockingQueue<>(1);
    private PlannerWindow window;
    private Tournament t;

    @Before
    public void setUp() throws IOException, SerializationException, InterruptedException {
        CSVTournamentFactory factory = new CSVTournamentFactory();
        factory.readDataFromClasspath("/org/iypt/planner/csv/", "team_data.csv", "jurors.csv", "schedule2012.csv");
        t = factory.newTournament();

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
        window.setTournament(t);
        assertThat(window.getSchedule().getSchedule().getScore()).isNotEmpty();
        assertThat(window.getSchedule().getSelectedRound());
    }
}
