package org.iypt.planner.benchmark;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import org.iypt.planner.csv.CSVTournamentFactory;
import org.iypt.planner.csv.ScheduleWriter;
import org.iypt.planner.domain.Tournament;
import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.persistence.common.api.domain.solution.SolutionFileIO;

public class TournamentIO implements SolutionFileIO {

    @Override
    public String getInputFileExtension() {
        return "csv";
    }

    @Override
    public String getOutputFileExtension() {
        return "csv";
    }

    @Override
    public Solution read(File file) {
        CSVTournamentFactory factory = new CSVTournamentFactory();
        File parent = file.getParentFile();
        File jury = new File(parent, "jury_data.csv");
        File team = new File(parent, "team_data.csv");
        File bias = new File(parent, "bias_IYPT2012.csv");
        try {
            factory.readTeamData(team);
            factory.readJuryData(jury);
            if (bias.exists()) {
                factory.readBiasData(bias);
            }
            factory.readSchedule(file);
            return factory.newTournament();
        } catch (IOException ex) {
            throw new RuntimeException();
        }
    }

    @Override
    public void write(Solution sltn, File file) {
        OutputStreamWriter os = null;
        try {
            os = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            new ScheduleWriter((Tournament) sltn).write(os);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException ex) {
                // do nothing
            }
        }
    }
}
