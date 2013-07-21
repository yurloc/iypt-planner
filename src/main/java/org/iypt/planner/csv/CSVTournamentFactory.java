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
import org.iypt.planner.domain.Conflict;
import org.iypt.planner.domain.DayOff;
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
 * <p>NOTE: According to <a href="http://tools.ietf.org/html/rfc4180">RFC 4180</a> trailing commas are not allowed, whitespace
 * is part of the entry.</p>
 *
 * @author jlocker
 */
public class CSVTournamentFactory {

    private static final Logger log = LoggerFactory.getLogger(CSVTournamentFactory.class);
    private CsvPreference preference = CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE;
    private Map<Integer, Round> rounds;
    private Map<CountryCode, Team> teams;
    private Map<String, Juror> jurors;
    private Map<String, Double> biases;
    private List<DayOff> dayOffs;
    private List<Conflict> conflicts;
    private Map<Jury, List<Juror>> juries;
    private int juryCapacity = 0;
    private Tournament tournament;
    private State state = new State();

    private class Source {

        private final String name;
        private final CsvListReader reader;

        public Source(Class<?> baseType, String resourcePath, Charset charset) {
            name = getResourceName(resourcePath);
            reader = getReader(baseType, resourcePath, charset);
        }

        public Source(File file) throws FileNotFoundException {
            name = file.getName();
            reader = getReader(file);
        }

        public Source(File file, Charset charset) throws FileNotFoundException {
            name = file.getName();
            reader = getReader(file, charset);
        }
    }

    private class State {

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

    private String getResourceName(String resource) {
        return resource.substring(resource.lastIndexOf('/') + 1);
    }

    private String getGroupName(String value) {
        return value.replaceAll("Group ", "");
    }

    /**
     * Determines if the line should be ignored. We are ignoring:
     *
     * <ul> <li>comment lines starting with {@code '#'} character (ignoring leading spaces)</li> <li>empty lines (including
     * non-empty whitespace-only lines)</li> </ul>
     *
     * @param line
     * @return
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

    private CsvListReader getReader(Class<?> baseType, String resource, Charset charset) {
        return new CsvListReader(new InputStreamReader(baseType.getResourceAsStream(resource), charset), preference);
    }

    private CsvListReader getReader(File file) throws FileNotFoundException {
        return new CsvListReader(new FileReader(file), preference);
    }

    private CsvListReader getReader(File file, Charset charset) throws FileNotFoundException {
        return new CsvListReader(new InputStreamReader(new FileInputStream(file), charset), preference);
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
                rounds.put(roundNumber, new Round(roundNumber, roundNumber));
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
                    log.trace("Ignoring trailing '{}' in {}[{}:{}]",
                            new Object[]{(char) preference.getDelimiterChar(), src.name, ln, i});
                    break;
                }
                CountryCode cc = CountryCodeIO.getByShortName(line.get(i));
                if (cc == null) {
                    throwIOE("Unknown country", line.get(i), src.name, ln, i);
                }
                // add the team only if it's new
                // TODO replace the Map with Set when equals is overriden
                if (!teams.containsKey(cc)) {
                    teams.put(cc, new Team(teams.size(), cc));
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
        dayOffs = new ArrayList<>(100);
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
                conflicts.add(new Conflict(juror, cc));

                // read country conflicts, day offs, and optional chair tag
                boolean dayOffMode = false;
                for (int i = 4; i < line.size(); i++) {
                    if (i == line.size() - 1 && line.get(i) == null) {
                        log.trace("Ignoring trailing '{}' in {}[{}:{}]",
                                new Object[]{(char) preference.getDelimiterChar(), src.name, ln, i});
                        break;
                    }

                    // chair tag
                    if ("C".equals(line.get(i))) {
                        if (juror.isChairCandidate()) {
                            throwIOE("Duplicate chair tag", src.name, ln, i);
                        }
                        juror.setChairCandidate(true);
                    } else {

                        try {
                            dayOffs.add(new DayOff(juror, Integer.valueOf(line.get(i))));
                            dayOffMode = true;
                        } catch (NumberFormatException ex) {
                            if (dayOffMode) {
                                // when the first day off is read, the rest of values should be all numbers (except for optional C)
                                throwIOE("Invalid day off number", line.get(i), src.name, ln, i);
                            }
                            CountryCode conflict = CountryCodeIO.getByShortName(line.get(i));
                            if (conflict == null) {
                                throwIOE("Unknown country", line.get(i), src.name, ln, 3);
                            }
                            log.debug("Juror with multiple conflicts: {} {}", conflict, juror);
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
                    log.trace("Ignoring trailing '{}' in {}[{}:{}]",
                            new Object[]{(char) preference.getDelimiterChar(), src.name, ln, line.size() - 1});
                    capacity--;
                }
                log.debug("Inferred jury capacity: {}.", capacity);
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
                if (r.getDay() == roundNumber) {
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
        readTeams(new Source(baseType, resourcePath, charset));
    }

    public void readTeamData(File dataFile) throws IOException {
        readTeams(new Source(dataFile));
    }

    public void readTeamData(File dataFile, Charset charset) throws IOException {
        readTeams(new Source(dataFile, charset));
    }

    //-------------------------------------------------------------------------------------------------------------------------
    // Reading jurors data
    //-------------------------------------------------------------------------------------------------------------------------
    public void readJuryData(Class<?> baseType, String resourcePath) throws IOException {
        readJuryData(baseType, resourcePath, StandardCharsets.UTF_8);
    }

    // for testing only
    protected void readJuryData(Class<?> baseType, String resourcePath, Charset charset) throws IOException {
        readJuries(new Source(baseType, resourcePath, charset));
    }

    public void readJuryData(File dataFile) throws IOException {
        readJuries(new Source(dataFile));
    }

    public void readJuryData(File dataFile, Charset charset) throws IOException {
        readJuries(new Source(dataFile, charset));
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

    public void readBiasData(File dataFile) throws IOException {
        readBiases(new FileReader(dataFile));
    }

    public void readBiasData(File dataFile, Charset charset) throws IOException {
        readBiases(new InputStreamReader(new FileInputStream(dataFile), charset));
    }

    //-------------------------------------------------------------------------------------------------------------------------
    // Reading jury schedule
    //-------------------------------------------------------------------------------------------------------------------------
    public void readSchedule(Class<?> baseType, String resourcePath) throws IOException {
        readSchedule(baseType, resourcePath, StandardCharsets.UTF_8);
    }

    // for testing only
    protected void readSchedule(Class<?> baseType, String resourcePath, Charset charset) throws IOException {
        readSchedule(new Source(baseType, resourcePath, charset));
    }

    public void readSchedule(File dataFile) throws IOException {
        readSchedule(new Source(dataFile));
    }

    public void readSchedule(File dataFile, Charset charset) throws IOException {
        readSchedule(new Source(dataFile, charset));
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
        tournament.setDayOffs(dayOffs);
        tournament.setConflicts(conflicts);

        if (state.hasBiasData()) {
            for (Juror juror : jurors.values()) {
                Double bias = biases.get(juror.fullName());
                juror.setBias((bias == null || Double.isNaN(bias)) ? 0 : bias);
            }
        }

        if (juryCapacity > 0) {
            tournament.setJuryCapacity(juryCapacity);
        }

        if (juries != null) {
            // compare the number of juries coming from team data to number of juries in jury schedule
            int size = tournament.getJuries().size();
            if (size != juries.size()) {
                log.debug("Juries needed: {}, juries scheduled: {}", size, juries.size());
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
}
