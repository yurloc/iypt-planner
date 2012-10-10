package org.iypt.planner.domain.util;

import au.com.bytecode.opencsv.CSVReader;
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
    private Source teamSource;
    private Source jurySource;
    private Source schdSource;
    private Map<Integer, Round> rounds = new HashMap<>(5);
    private Map<CountryCode, Team> teams = new HashMap<>(30);
    private Map<String, Juror> jurors = new HashMap<>(100);
    private List<DayOff> dayOffs = new ArrayList<>(100);
    private List<Conflict> conflicts = new ArrayList<>(100);

    public CSVTournamentFactory(Class<?> baseType, String team, String jury) {
        this.teamSource = new Source(getResourceName(team), getReader(baseType, team));
        this.jurySource = new Source(getResourceName(jury), getReader(baseType, jury));
    }

    public CSVTournamentFactory(Class<?> baseType, String team, String jury, String schedule) {
        this(baseType, team, jury);
        this.schdSource = new Source(getResourceName(schedule), getReader(baseType, schedule));
    }

    public CSVTournamentFactory(String team, String jury, String schedule) {
        this(CSVTournamentFactory.class, team, jury, schedule);
    }

    public CSVTournamentFactory(String team, String jury) {
        this(CSVTournamentFactory.class, team, jury);
    }

    private CSVReader getReader(Class<?> baseType, String resource) {
        return new CSVReader(new InputStreamReader(baseType.getResourceAsStream(resource)), SEPARATOR);
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
    }

    private void readJuries(Source src) throws IOException {
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
            JurorType jt = JurorType.getByLetter(line[2].charAt(0));
            if (line[2].length() > 1 || jt == null) {
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
    }

    private void readSchedule(Source src, Tournament t) throws IOException {
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
                log.debug("Setting jury capacity to {}.", capacity);
                t.setJuryCapacity(capacity);
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
            for (Round r : t.getRounds()) {
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

            for (int i = 0; i < jury.getCapacity(); i++) {
                String name = line[i + 2];
                Juror juror = jurors.get(name);
                if (juror == null) {
                    throwIOE("Unkown juror", name, src.name, ln, i + 2);
                }
                jury.getSeats().get(i).setJuror(juror);
            }
            ln++;
        }
    }

    public Tournament newTournament() throws IOException {
        readTeams(teamSource);
        readJuries(jurySource);

        Tournament t = new Tournament();
        t.setRounds(rounds.values());
        t.setJurors(jurors.values());
        t.setDayOffs(dayOffs);
        t.setConflicts(conflicts);

        if (schdSource != null) {
            readSchedule(schdSource, t);
        }
        return t;
    }

    private class Source {

        private final String name;
        private final CSVReader reader;

        public Source(String name, CSVReader reader) {
            this.name = name;
            this.reader = reader;
        }
    }
}
