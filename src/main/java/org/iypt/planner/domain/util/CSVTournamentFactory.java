package org.iypt.planner.domain.util;

import au.com.bytecode.opencsv.CSVReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.iypt.planner.domain.Conflict;
import org.iypt.planner.domain.CountryCode;
import org.iypt.planner.domain.DayOff;
import org.iypt.planner.domain.Group;
import org.iypt.planner.domain.Juror;
import org.iypt.planner.domain.JurorType;
import org.iypt.planner.domain.Jury;
import org.iypt.planner.domain.Round;
import org.iypt.planner.domain.Team;
import org.iypt.planner.domain.Tournament;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This CSV processor ignores whitespace-only (empty) lines and trailing separators if the last value is whitespace-only
 * (warning is logged).
 *
 * <p>NOTE: According to <a href="http://tools.ietf.org/html/rfc4180">RFC 4180</a> trailing commas are not allowed,
 * whitespace is part of the entry.</p>
 *
 * @author jlocker
 */
public class CSVTournamentFactory {

    private static final Logger log = LoggerFactory.getLogger(CSVTournamentFactory.class);
    private static final Map<String, CountryCode> countryNameMap = new HashMap<>();

    static {
        for (CountryCode cc : CountryCode.values()) {
            countryNameMap.put(cc.getName(), cc);
        }
        // support custom country names
        countryNameMap.put("Chinese Taipei", CountryCode.TW);
        countryNameMap.put("Iran", CountryCode.IR);
        countryNameMap.put("Korea", CountryCode.KR);
        countryNameMap.put("Russia", CountryCode.RU);
    }
    
    private char SEPARATOR = ';';
    private Map<Integer, Round> rounds;
    private Map<CountryCode, Team> teams;
    private Map<String, Juror> jurors;
    private List<DayOff> dayOffs;
    private List<Conflict> conflicts;
    private Map<Jury, List<Juror>> juries;
    private int juryCapacity = 0;
    private Tournament tournament;
    private State state = new State();

    public void readDataFromClasspath(String commonPath, String teamFile, String juryFile) throws IOException {
        readTeamData(CSVTournamentFactory.class, commonPath + teamFile);
        readJuryData(CSVTournamentFactory.class, commonPath + juryFile);
    }

    public void readDataFromClasspath(String commonPath, String teamFile, String juryFile, String scheduleFile) throws IOException {
        readDataFromClasspath(commonPath, teamFile, juryFile);
        readSchedule(CSVTournamentFactory.class, commonPath + scheduleFile);
    }

    public void readTeamData(Class<?> baseType, String resourcePath) throws IOException {
        readTeams(new Source(baseType, resourcePath));
    }

    public void readJuryData(Class<?> baseType, String resourcePath) throws IOException {
        readJuries(new Source(baseType, resourcePath));
    }

    public void readSchedule(Class<?> baseType, String resourcePath) throws IOException {
        readSchedule(new Source(baseType, resourcePath));
    }

    public void readTeamData(File dataFile) throws IOException {
        readTeams(new Source(dataFile));
    }

    public void readJuryData(File dataFile) throws IOException {
        readJuries(new Source(dataFile));
    }

    public void readSchedule(File dataFile) throws IOException {
        readSchedule(new Source(dataFile));
    }

    private CSVReader getReader(Class<?> baseType, String resource) {
        return new CSVReader(new InputStreamReader(baseType.getResourceAsStream(resource)), SEPARATOR);
    }

    private CSVReader getReader(File file) throws FileNotFoundException {
        return new CSVReader(new FileReader(file), SEPARATOR);
    }

    private String getResourceName(String resource) {
        return resource.substring(resource.lastIndexOf('/') + 1);
    }

    private void throwIOE(String message, String cause, String fileName, int lineNumber, int valuePosition) throws IOException {
        throw new IOException(String.format("%s '%s' in %s [%d:%d]", message, cause, fileName, lineNumber, valuePosition));
    }

    private void throwIOE(String message, String fileName, int lineNumber, int valuePosition) throws IOException {
        throw new IOException(String.format("%s in %s [%d:%d]", message, fileName, lineNumber, valuePosition));
    }

    private String getGroupName(String value) {
        return value.replaceAll("Group ", "");
    }

    private boolean ignore(String[] line) {
        if (line.length == 0) return true;
        if (line.length > 0 && line[0].trim().isEmpty()) {
            // ignore empty lines
            if (line.length == 1) {
                return true;
            }
        } else {
            // skip comments
            if (line[0].charAt(0) == '#') {
                return true;
            }
        }
        return false;
    }

    private void readTeams(Source src) throws IOException {
        // initialize collections
        rounds = new HashMap<>(5);
        teams = new HashMap<>(30);
        
        int ln = 1; // line number
        for (String[] line : src.reader.readAll()) {
            if (ignore(line)) {
                continue;
            }

            // get round number
            int roundNumber = 0;
            try {
                roundNumber = Integer.parseInt(line[0]);
            } catch (NumberFormatException ex) {
                throwIOE("Invalid round number", line[0], src.name, ln, 0);
            }

            // get the round to be populated
            if (!rounds.containsKey(roundNumber)) {
                rounds.put(roundNumber, new Round(roundNumber, roundNumber));
            }
            Round round = rounds.get(roundNumber);

            // create the group
            if (line.length < 2) {
                throwIOE("Incomplete entry: missing group", src.name, ln, 1);
            }
            String groupName = getGroupName(line[1]);
            Group group = round.createGroup(groupName);

            // get the teams in group
            for (int i = 2; i < line.length; i++) {
                if (i == line.length - 1 && line[i].trim().isEmpty()) {
                    log.warn("Ignoring trailing '{}' in {}[{}:{}]", new Object[]{SEPARATOR, src.name, ln, i});
                    break;
                }
                CountryCode cc = countryNameMap.get(line[i]);
                if (cc == null) {
                    throwIOE("Unknown country", line[i], src.name, ln, i);
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
        for (String[] line : src.reader.readAll()) {
            if (ignore(line)) {
                continue;
            }

            // check minmal number of values
            if (line.length < 4) {
                if (line.length == 1) throwIOE("Incomplete entry: missing juror's last name", src.name, ln, 1);
                if (line.length == 2) throwIOE("Incomplete entry: missing juror's type tag", src.name, ln, 2);
                if (line.length == 3) throwIOE("Incomplete entry: missing juror's country", src.name, ln, 3);
            }
            
            // get JurorType tag
            JurorType jt = null;
            try {
                jt = JurorType.getByLetter(line[2].charAt(0));
            } catch (IllegalArgumentException e) {
                throwIOE("Invalid juror type tag", line[2], src.name, ln, 2);
            }

            // get first country
            CountryCode cc = countryNameMap.get(line[3]);
            if (cc == null) {
                throwIOE("Unknown country", line[3], src.name, ln, 3);
            }

            // create the juror
            Juror juror = new Juror(line[0], line[1], cc, jt);
            jurors.put(String.format("%s, %s", line[1], line[0]), juror);
            conflicts.add(new Conflict(juror, cc));

            // read country conflicts, day offs, and optional chair tag
            boolean dayOffMode = false;
            for (int i = 4; i < line.length; i++) {
                if (i == line.length - 1) {
                    if ("C".equals(line[i])) {
                        juror.setChairCandidate(true);
                        break;
                    } else if(line[i].trim().isEmpty()) {
                        log.warn("Ignoring trailing '{}' in {}[{}:{}]", new Object[]{SEPARATOR, src.name, ln, i});
                        break;
                    }
                }
                try {
                    dayOffs.add(new DayOff(juror, Integer.valueOf(line[i])));
                    dayOffMode = true;
                } catch (NumberFormatException ex) {
                    if (dayOffMode) {
                        // when the first day off is read, the rest of values should be all numbers (except for optional C)
                        throwIOE("Invalid day off number", line[i], src.name, ln, i);
                    }
                    log.debug("Juror with multiple conflicts: {} {}", countryNameMap.get(line[i]), juror);
                    conflicts.add(new Conflict(juror, countryNameMap.get(line[i])));
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

        for (String[] line : src.reader.readAll()) {
            if (ignore(line)) {
                continue;
            }

            // set jury capacity
            if (!capacitySet) {
                int capacity = line.length - 2;
                if (line[line.length - 1].trim().isEmpty()) {
                    // don't break the capacity with trailing ';'
                    log.warn("Ignoring trailing '{}' in {}[{}:{}]", new Object[]{SEPARATOR, src.name, ln, line.length - 1});
                    capacity--;
                }
                log.debug("Inferred jury capacity: {}.", capacity);
                juryCapacity = capacity;
                capacitySet = true;
            }

            // get round number
            int roundNumber = 0;
            try {
                roundNumber = Integer.valueOf(line[0]);
            } catch (NumberFormatException ex) {
                throwIOE("Invalid round number", line[0], src.name, ln, 0);
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
                throwIOE("Cannot find round with number", line[0], src.name, ln, 0);
            }

            // get group
            String groupName = getGroupName(line[1]);
            Jury jury = null;
            for (Group g : round.getGroups()) {
                if (groupName.equals(g.getName())) {
                    jury = g.getJury();
                }
            }
            if (jury == null) {
                throwIOE("Cannot find group for name", line[1], src.name, ln, 1);
            }

            List<Juror> jurorList = new ArrayList<>();
            juries.put(jury, jurorList);
            for (int i = 0; i < juryCapacity; i++) {
                String name = line[i + 2];
                Juror juror = jurors.get(name);
                if (juror == null) {
                    throwIOE("Unkown juror", name, src.name, ln, i + 2);
                }
                jurorList.add(juror);
            }
            ln++;
        }
    }

    public Tournament newTournament() throws IOException {
        if (!state.canCreateTournament()) {
            throw new IllegalStateException("Missing some data to create new tournament. Read teams and jurors first.");
        }
        tournament = new Tournament();
        tournament.setRounds(rounds.values());
        tournament.setJurors(jurors.values());
        tournament.setDayOffs(dayOffs);
        tournament.setConflicts(conflicts);

        if (juryCapacity > 0) {
            tournament.setJuryCapacity(juryCapacity);
        }
        for (Jury jury : tournament.getJuries()) {
            for (int i = 0; i < juryCapacity; i++) {
                tournament.getSeats(jury).get(i).setJuror(juries.get(jury).get(i));
            }
        }
        return tournament;
    }

    public boolean canReadSchedule() {
        return state.canReadSchedule();
    }

    private class Source {

        private final String name;
        private final CSVReader reader;

        public Source(Class<?> baseType, String resourcePath) {
            name = getResourceName(resourcePath);
            reader = getReader(baseType, resourcePath);
        }

        public Source(File file) throws FileNotFoundException {
            name = file.getName();
            reader = getReader(file);
        }

        public Source(String name, CSVReader reader) {
            this.name = name;
            this.reader = reader;
        }
    }

    private class State {
        boolean teams = false;
        boolean jurors = false;

        void teamsReady() {
            teams = true;
        }

        void jurorsReady() {
            jurors = true;
        }

        boolean canReadSchedule() {
            return teams && jurors;
        }

        boolean canCreateTournament() {
            return teams && jurors;
        }
    }
}
