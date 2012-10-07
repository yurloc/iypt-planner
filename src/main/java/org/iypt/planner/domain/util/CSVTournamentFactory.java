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
    
    private CSVReader teamReader;
    private CSVReader juryReader;
    private String teamFile;
    private String juryFile;
    private Map<Integer, Round> rounds = new HashMap<>(5);
    private Map<CountryCode, Team> teams = new HashMap<>(30);
    private List<Juror> jurors = new ArrayList<>(100);
    private List<DayOff> dayOffs = new ArrayList<>(100);
    private List<Conflict> conflicts = new ArrayList<>(100);

    public CSVTournamentFactory(Class<?> baseType, String team, String jury) {
        teamReader = getReader(baseType, team);
        teamFile = getResourceName(team);
        juryReader = getReader(baseType, jury);
        juryFile = getResourceName(jury);
    }

    private CSVReader getReader(Class<?> baseType, String resource) {
        return new CSVReader(new InputStreamReader(baseType.getResourceAsStream(resource)), ';');
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

    private void readTeams() throws IOException {
        int ln = 1; // line number
        for (String[] line : teamReader.readAll()) {
            if (line.length == 1 && line[0].trim().isEmpty()) {
                // ignore empty lines
                continue;
            }

            // get round number
            int roundNumber = 0;
            try {
                roundNumber = Integer.parseInt(line[0]);
            } catch (NumberFormatException ex) {
                throwIOE("Invalid round number", line[0], teamFile, ln, 0);
            }

            // get the round to be populated
            if (!rounds.containsKey(roundNumber)) {
                rounds.put(roundNumber, new Round(roundNumber, roundNumber));
            }
            Round round = rounds.get(roundNumber);

            // create the group
            if (line.length < 2) {
                throwIOE("Incomplete entry: missing group", teamFile, ln, 1);
            }
            String groupName = line[1].replaceAll("Group ", "");
            Group group = round.createGroup(groupName);

            // get the teams in group
            for (int i = 2; i < line.length; i++) {
                if (i == line.length - 1 && line[i].trim().isEmpty()) {
                    // ignore trailing ';'
                    break;
                }
                CountryCode cc = countryNameMap.get(line[i]);
                if (cc == null) {
                    throwIOE("Unknown country", line[i], teamFile, ln, i);
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

    private void readJuries() throws IOException {
        int ln = 1; // line number
        for (String[] line : juryReader.readAll()) {
            if (line.length == 1 && line[0].trim().isEmpty()) {
                // ignore empty lines
                break;
            }

            // check minmal number of values
            if (line.length < 4) {
                if (line.length == 1) throwIOE("Incomplete entry: missing juror's last name", juryFile, ln, 1);
                if (line.length == 2) throwIOE("Incomplete entry: missing juror's type tag", juryFile, ln, 2);
                if (line.length == 3) throwIOE("Incomplete entry: missing juror's country", juryFile, ln, 3);
            }
            
            // get JurorType tag
            JurorType jt = JurorType.getByLetter(line[2].charAt(0));
            if (line[2].length() > 1 || jt == null) {
                throwIOE("Invalid juror type tag", line[2], juryFile, ln, 2);
            }

            // get first country
            CountryCode cc = countryNameMap.get(line[3]);
            if (cc == null) {
                throwIOE("Unknown country", line[3], teamFile, ln, 3);
            }

            // create the juror
            Juror juror = new Juror(line[0], line[1], cc, jt);
            jurors.add(juror);
            conflicts.add(new Conflict(juror, cc));

            // read country conflicts, day offs, and optional chair tag
            boolean dayOffMode = false;
            for (int i = 4; i < line.length; i++) {
                if (i == line.length - 1 && "C".equals(line[i])) {
                    juror.setChairCandidate(true);
                    break;
                }
                try {
                    dayOffs.add(new DayOff(juror, Integer.valueOf(line[i])));
                    dayOffMode = true;
                } catch (NumberFormatException ex) {
                    if (dayOffMode) {
                        // when the first day off is read, the rest of values should be all numbers (except for optional C)
                        throwIOE("Invalid day off number", line[i], juryFile, ln, i);
                    }
                    log.debug("Juror with multiple conflicts: {} {}", countryNameMap.get(line[i]), juror);
                    conflicts.add(new Conflict(juror, countryNameMap.get(line[i])));
                }
            }
            ln++;
        }
    }

    public Tournament newTournament() throws IOException {
        readTeams();
        readJuries();

        Tournament t = new Tournament();
        t.setRounds(rounds.values());
        t.setJurors(jurors);
        t.setDayOffs(dayOffs);
        t.setConflicts(conflicts);
        return t;
    }
}
