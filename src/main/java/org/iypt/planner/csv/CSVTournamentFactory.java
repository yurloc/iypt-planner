package org.iypt.planner.csv;

import com.neovisionaries.i18n.CountryCode;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.iypt.planner.domain.Absence;
import org.iypt.planner.domain.Conflict;
import org.iypt.planner.domain.Group;
import org.iypt.planner.domain.Juror;
import org.iypt.planner.domain.JurorType;
import org.iypt.planner.domain.Jury;
import org.iypt.planner.domain.Round;
import org.iypt.planner.domain.Seat;
import org.iypt.planner.domain.Team;
import org.iypt.planner.domain.Tournament;
import org.iypt.planner.domain.util.CountryCodeIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;

/**
 * This CSV processor ignores whitespace-only (empty) lines and trailing separators if the last value is whitespace-only.
 *
 * <p>
 * NOTE: According to <a href="http://tools.ietf.org/html/rfc4180">RFC 4180</a> trailing commas are not allowed, whitespace is
 * part of the entry.</p>
 *
 * @author jlocker
 */
public class CSVTournamentFactory {

    private static final Logger LOG = LoggerFactory.getLogger(CSVTournamentFactory.class);
    private static final CsvPreference PREFERENCE = CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE;
    private final State state = new State();
    private Map<Integer, Round> rounds;
    private Map<CountryCode, Team> teams;
    private Map<String, Juror> jurors;
    private Map<String, Double> biases;
    private List<Absence> absences;
    private List<Conflict> conflicts;
    private Map<Jury, List<Juror>> juries;
    private int juryCapacity = 0;
    private Tournament tournament;

    private static class Source {

        private final String name;
        private final CsvListReader reader;

        public Source(String name, CsvListReader reader) {
            this.name = name;
            this.reader = reader;
        }
    }

    private static class State {

        private boolean teams = false;
        private boolean jurors = false;
        private boolean biases = false;

        private void teamsReady() {
            teams = true;
        }

        private void biasesReady() {
            biases = true;
        }

        private void jurorsReady() {
            jurors = true;
        }

        private boolean canReadSchedule() {
            return teams && jurors;
        }

        private boolean canCreateTournament() {
            return teams && jurors;
        }

        private boolean hasBiasData() {
            return biases;
        }
    }

    private String getGroupName(String value) {
        return value.replaceAll("Group ", "");
    }

    /**
     * Determines if the line should be ignored. We are ignoring:
     * <ul>
     * <li>comment lines starting with {@code '#'} character (ignoring leading spaces)</li>
     * <li>empty lines (including non-empty whitespace-only lines)</li>
     * </ul>
     *
     * @param line the line to check
     * @return True if the line should be ignored
     */
    private boolean ignore(List<String> line) {
        if (line.isEmpty()) {
            return true;
        }
        if (line.get(0).trim().isEmpty()) {
            // ignore empty lines
            if (line.size() == 1) {
                return true;
            }
        } else {
            // skip comments
            if (line.get(0).charAt(0) == '#') {
                return true;
            }
        }
        return false;
    }

    private void throwIOE(String message, String cause, String fileName, int lineNumber, int valuePosition) throws IOException {
        throw new IOException(String.format("%s '%s' in %s [%d:%d]", message, cause, fileName, lineNumber, valuePosition));
    }

    private void throwIOE(String message, String fileName, int lineNumber, int valuePosition) throws IOException {
        throw new IOException(String.format("%s in %s [%d:%d]", message, fileName, lineNumber, valuePosition));
    }

    //-------------------------------------------------------------------------------------------------------------------------
    // Source factory methods
    //-------------------------------------------------------------------------------------------------------------------------
    private Source makeSource(Class<?> baseType, String resourcePath, Charset charset) {
        String resourceName = resourcePath.substring(resourcePath.lastIndexOf('/') + 1);
        CsvListReader csvReader = new CsvListReader(
                new InputStreamReader(baseType.getResourceAsStream(resourcePath), charset), PREFERENCE);
        return new Source(resourceName, csvReader);
    }

    private Source makeSource(File file, Charset charset) throws FileNotFoundException {
        String resourceName = file.getName();
        CsvListReader csvReader = new CsvListReader(
                new InputStreamReader(new FileInputStream(file), charset), PREFERENCE);
        return new Source(resourceName, csvReader);
    }

    private Source makeSource(File file) throws FileNotFoundException {
        String resourceName = file.getName();
        CsvListReader csvReader = new CsvListReader(
                new FileReader(file), PREFERENCE);
        return new Source(resourceName, csvReader);
    }

    private void readBiases(Reader reader) throws IOException {
        BiasReader br = new BiasReader();
        biases = br.read(reader);
        state.biasesReady();
    }

    private void readTeams(Source src) throws IOException {
        // initialize collections
        rounds = new HashMap<>(5);
        teams = new HashMap<>(30);

        int ln = 1; // line number
        List<String> line;
        while ((line = src.reader.read()) != null) {
            if (ignore(line)) {
                continue;
            }

            // get round number
            int roundNumber = 0;
            try {
                roundNumber = Integer.parseInt(line.get(0));
            } catch (NumberFormatException ex) {
                throwIOE("Invalid round number", line.get(0), src.name, ln, 0);
            }

            // get the round to be populated
            if (!rounds.containsKey(roundNumber)) {
                rounds.put(roundNumber, new Round(roundNumber));
            }
            Round round = rounds.get(roundNumber);

            // create the group
            if (line.size() < 2) {
                throwIOE("Incomplete entry: missing group", src.name, ln, 1);
            }
            String groupName = getGroupName(line.get(1));
            Group group = round.createGroup(groupName);

            // get the teams in group
            for (int i = 2; i < line.size(); i++) {
                if (i == line.size() - 1 && line.get(i) == null) {
                    LOG.trace("Ignoring trailing '{}' in {}[{}:{}]",
                            new Object[]{(char) PREFERENCE.getDelimiterChar(), src.name, ln, i});
                    break;
                }
                CountryCode cc = CountryCodeIO.getByShortName(line.get(i));
                if (cc == null) {
                    throwIOE("Unknown country", line.get(i), src.name, ln, i);
                }
                // add the team only if it's new
                // TODO replace the Map with Set when equals is overriden
                if (!teams.containsKey(cc)) {
                    teams.put(cc, new Team(cc));
                }
                group.addTeam(teams.get(cc));
            }
            ln++;
        }
        state.teamsReady();
    }

    private void readJuries(Source src) throws IOException {
        // initialize collections
        jurors = new HashMap<>(100);
        absences = new ArrayList<>(100);
        conflicts = new ArrayList<>(100);

        int ln = 1; // line number
        List<String> line;
        while ((line = src.reader.read()) != null) {
            if (!ignore(line)) {
                // check minmal number of values
                if (line.size() < 4) {
                    if (line.size() == 1) {
                        throwIOE("Incomplete entry: missing juror's last name", src.name, ln, 1);
                    }
                    if (line.size() == 2) {
                        throwIOE("Incomplete entry: missing juror's type tag", src.name, ln, 2);
                    }
                    if (line.size() == 3) {
                        throwIOE("Incomplete entry: missing juror's country", src.name, ln, 3);
                    }
                }

                // get JurorType tag
                JurorType jt = null;
                try {
                    jt = JurorType.getByLetter(line.get(2).charAt(0));
                } catch (IllegalArgumentException e) {
                    throwIOE("Invalid juror type tag", line.get(2), src.name, ln, 2);
                }

                // get first country
                CountryCode cc = CountryCodeIO.getByShortName(line.get(3));
                if (cc == null) {
                    throwIOE("Unknown country", line.get(3), src.name, ln, 3);
                }

                // create the juror
                Juror juror = new Juror(line.get(0), line.get(1), cc, jt);
                jurors.put(String.format("%s, %s", line.get(1), line.get(0)), juror);

                // read country conflicts, absences, and optional chair tag
                boolean readingAbsences = false;
                for (int i = 4; i < line.size(); i++) {
                    if (i == line.size() - 1 && line.get(i) == null) {
                        LOG.trace("Ignoring trailing '{}' in {}[{}:{}]",
                                new Object[]{(char) PREFERENCE.getDelimiterChar(), src.name, ln, i});
                        break;
                    }

                    // chair tag
                    if ("C".equals(line.get(i))) {
                        if (juror.isChairCandidate()) {
                            throwIOE("Duplicate chair tag", src.name, ln, i);
                        }
                        juror.setChairCandidate(true);
                    } else if ("E0".equals(line.get(i))) {
                        //experience tag
                        juror.setExperienced(false);
                    } else {

                        try {
                            absences.add(new Absence(juror, Integer.valueOf(line.get(i))));
                            readingAbsences = true;
                        } catch (NumberFormatException ex) {
                            if (readingAbsences) {
                                // when the first absence is read, the rest of values should all be numbers (except for optional tags)
                                throwIOE("Invalid round number for juror absence", line.get(i), src.name, ln, i);
                            }
                            CountryCode conflict = CountryCodeIO.getByShortName(line.get(i));
                            if (conflict == null) {
                                throwIOE("Unknown country", line.get(i), src.name, ln, 3);
                            }
                            LOG.debug("Juror with multiple conflicts: {} {}", conflict, juror);
                            conflicts.add(new Conflict(juror, conflict));
                        }
                    }
                }
            }
            ln++;
        }
        state.jurorsReady();
    }

    private void readSchedule(Source src) throws IOException {
        if (!state.canReadSchedule()) {
            throw new IllegalStateException("Not ready to read schedule. Teams and jurors must be read first.");
        }
        juries = new HashMap<>();

        int ln = 1;
        boolean capacitySet = false;

        List<String> line;
        while ((line = src.reader.read()) != null) {
            if (ignore(line)) {
                continue;
            }

            // set jury capacity
            if (!capacitySet) {
                int capacity = line.size() - 2;
                if (line.get(line.size() - 1) == null) {
                    // don't break the capacity with trailing ';'
                    LOG.trace("Ignoring trailing '{}' in {}[{}:{}]",
                            new Object[]{(char) PREFERENCE.getDelimiterChar(), src.name, ln, line.size() - 1});
                    capacity--;
                }
                LOG.debug("Inferred jury capacity: {}.", capacity);
                juryCapacity = capacity;
                capacitySet = true;
            }

            // get round number
            int roundNumber = 0;
            try {
                roundNumber = Integer.valueOf(line.get(0));
            } catch (NumberFormatException ex) {
                throwIOE("Invalid round number", line.get(0), src.name, ln, 0);
            }

            // get the round instance
            Round round = null;
            for (Round r : rounds.values()) {
                if (r.getNumber() == roundNumber) {
                    round = r;
                    break;
                }
            }
            if (round == null) {
                throwIOE("Cannot find round with number", line.get(0), src.name, ln, 0);
            }

            // get group
            String groupName = getGroupName(line.get(1));
            Jury jury = null;
            for (Group g : round.getGroups()) {
                if (groupName.equals(g.getName())) {
                    jury = g.getJury();
                }
            }
            if (jury == null) {
                throwIOE("Cannot find group for name", line.get(1), src.name, ln, 1);
            }

            List<Juror> jurorList = new ArrayList<>();
            juries.put(jury, jurorList);
            for (int i = 0; i < juryCapacity; i++) {
                String name = line.get(i + 2);
                Juror juror = jurors.get(name);
                if (juror == null) {
                    throwIOE("Unkown juror", name, src.name, ln, i + 2);
                }
                jurorList.add(juror);
            }
            ln++;
        }
    }

    //-------------------------------------------------------------------------------------------------------------------------
    // Shortcuts to read multiple classpath resources
    //-------------------------------------------------------------------------------------------------------------------------
    public void readDataFromClasspath(String commonPath, String teamFile, String juryFile) throws IOException {
        readTeamData(CSVTournamentFactory.class, commonPath + teamFile);
        readJuryData(CSVTournamentFactory.class, commonPath + juryFile);
    }

    public void readDataFromClasspath(String commonPath, String teamFile, String juryFile, String scheduleFile) throws IOException {
        readDataFromClasspath(commonPath, teamFile, juryFile);
        readSchedule(CSVTournamentFactory.class, commonPath + scheduleFile);
    }

    public void readDataFromClasspath(String commonPath, String teamFile, String juryFile, String biasFile, String scheduleFile) throws IOException {
        readDataFromClasspath(commonPath, teamFile, juryFile);
        readBiasData(CSVTournamentFactory.class, commonPath + biasFile);
        readSchedule(CSVTournamentFactory.class, commonPath + scheduleFile);
    }

    //-------------------------------------------------------------------------------------------------------------------------
    // Reading teams data
    //-------------------------------------------------------------------------------------------------------------------------
    public void readTeamData(Class<?> baseType, String resourcePath) throws IOException {
        readTeamData(baseType, resourcePath, StandardCharsets.UTF_8);
    }

    // for testing only
    protected void readTeamData(Class<?> baseType, String resourcePath, Charset charset) throws IOException {
        readTeams(makeSource(baseType, resourcePath, charset));
    }

    public void readTeamData(File dataFile, Charset charset) throws IOException {
        readTeams(makeSource(dataFile, charset));
    }

    public void readTeamData(File dataFile) throws IOException {
        readTeams(makeSource(dataFile));
    }

    //-------------------------------------------------------------------------------------------------------------------------
    // Reading jurors data
    //-------------------------------------------------------------------------------------------------------------------------
    public void readJuryData(Class<?> baseType, String resourcePath) throws IOException {
        readJuryData(baseType, resourcePath, StandardCharsets.UTF_8);
    }

    // for testing only
    protected void readJuryData(Class<?> baseType, String resourcePath, Charset charset) throws IOException {
        readJuries(makeSource(baseType, resourcePath, charset));
    }

    public void readJuryData(File dataFile, Charset charset) throws IOException {
        readJuries(makeSource(dataFile, charset));
    }

    public void readJuryData(File dataFile) throws IOException {
        readJuries(makeSource(dataFile));
    }

    //-------------------------------------------------------------------------------------------------------------------------
    // Reading biases data
    //-------------------------------------------------------------------------------------------------------------------------
    public void readBiasData(Class<?> baseType, String resourcePath) throws IOException {
        readBiasData(baseType, resourcePath, StandardCharsets.UTF_8);
    }

    // for testing only
    protected void readBiasData(Class<?> baseType, String resourcePath, Charset charset) throws IOException {
        readBiases(new InputStreamReader(baseType.getResourceAsStream(resourcePath), charset));
    }

    public void readBiasData(File dataFile, Charset charset) throws IOException {
        readBiases(new InputStreamReader(new FileInputStream(dataFile), charset));
    }

    public void readBiasData(File dataFile) throws IOException {
        readBiases(new FileReader(dataFile));
    }

    //-------------------------------------------------------------------------------------------------------------------------
    // Reading jury schedule
    //-------------------------------------------------------------------------------------------------------------------------
    public void readSchedule(Class<?> baseType, String resourcePath) throws IOException {
        readSchedule(baseType, resourcePath, StandardCharsets.UTF_8);
    }

    // for testing only
    protected void readSchedule(Class<?> baseType, String resourcePath, Charset charset) throws IOException {
        readSchedule(makeSource(baseType, resourcePath, charset));
    }

    public void readSchedule(File dataFile, Charset charset) throws IOException {
        readSchedule(makeSource(dataFile, charset));
    }

    public void readSchedule(File dataFile) throws IOException {
        readSchedule(makeSource(dataFile));
    }

    public void setBiases(Map<String, Double> biases) {
        this.biases = biases;
        state.biasesReady();
    }

    /**
     * Creates a new Tournament instance defined by the data that were previously read from CSV files. It is necessary to
     * {@link #readTeamData(java.io.File)} and {@link #readJuryData(java.io.File)} before calling this method. Optionally, an
     * existing schedule can be loaded with {@link #readSchedule(java.io.File)} if you don't want to start from scratch.
     *
     * @return new Tournament instance
     * @throws IllegalStateException if the factory is missing some data that is required to create a new Tournament.
     * @see #readTeamData(java.io.File)
     */
    public Tournament newTournament() {
        if (!state.canCreateTournament()) {
            throw new IllegalStateException("Missing some data to create new tournament. Read teams and jurors first.");
        }
        tournament = new Tournament();
        tournament.setRounds(rounds.values());
        tournament.setJurors(jurors.values());
        tournament.setAbsences(absences);
        tournament.addConflicts(conflicts);

        if (state.hasBiasData()) {
            for (Juror juror : jurors.values()) {
                Double bias = biases.get(juror.fullName());
                juror.setBias(bias == null || Double.isNaN(bias) ? 0 : bias);
            }
        }

        if (juryCapacity > 0) {
            tournament.setJuryCapacity(juryCapacity);
        }

        if (juries != null) {
            // compare the number of juries coming from team data to number of juries in jury schedule
            int size = tournament.getJuries().size();
            if (size != juries.size()) {
                LOG.debug("Juries needed: {}, juries scheduled: {}", size, juries.size());
            }

            for (Jury jury : tournament.getJuries()) {
                List<Juror> scheduledJury = juries.get(jury);
                // don't fail if only partial jury schedule is provided
                if (scheduledJury != null) {
                    for (int i = 0; i < juryCapacity; i++) {
                        Seat seat = tournament.getSeats(jury).get(i);
                        seat.setJuror(scheduledJury.get(i));
                    }
                }
            }
        }
        return tournament;
    }

    public boolean canReadSchedule() {
        return state.canReadSchedule();
    }

    public boolean canCreateTournament() {
        return state.canCreateTournament();
    }
}
