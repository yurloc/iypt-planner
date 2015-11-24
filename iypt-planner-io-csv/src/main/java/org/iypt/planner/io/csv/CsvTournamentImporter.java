package org.iypt.planner.io.csv;

import com.neovisionaries.i18n.CountryCode;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.iypt.planner.api.domain.Assignment;
import org.iypt.planner.api.domain.BiasData;
import org.iypt.planner.api.domain.Group;
import org.iypt.planner.api.domain.Juror;
import org.iypt.planner.api.domain.JurorType;
import org.iypt.planner.api.domain.Role;
import org.iypt.planner.api.domain.Round;
import org.iypt.planner.api.domain.Schedule;
import org.iypt.planner.api.domain.Team;
import org.iypt.planner.api.domain.Tournament;
import org.iypt.planner.api.io.InputSource;
import org.iypt.planner.api.io.TournamentImporter;
import org.iypt.planner.api.util.CountryCodeIO;
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
public class CsvTournamentImporter implements TournamentImporter {

    private static final Logger LOG = LoggerFactory.getLogger(CsvTournamentImporter.class);
    private static final CsvPreference PREFERENCE = CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE;
    private SortedMap<Integer, Round> rounds;
    private Map<CountryCode, Team> teams;
    private Map<String, Juror> jurors;
    // TODO close input streams

    @Override
    public BiasData loadBiases(Tournament tournament, InputSource biasSource) throws IOException {
        BiasReader reader = new BiasReader();
        reader.read(new InputStreamReader(biasSource.getUrl().openStream(), biasSource.getCharset()));
        BiasData biasData = new BiasData();
        for (Juror j : tournament.getJurors()) {
            biasData.setBias(j, reader.getBias(String.format("%s %s", j.getFirstName(), j.getLastName())));
        }
        return biasData;
    }

    @Override
    public Schedule loadSchedule(Tournament tournament, InputSource scheduleSource) throws IOException {
        return readSchedule(tournament, new Source(scheduleSource));
    }

    @Override
    public Tournament loadTournament(InputSource teamsFile, InputSource jurorsFile) throws IOException {
        readTeams(new Source(teamsFile));
        readJurors(new Source(jurorsFile));
        return new Tournament(
                new ArrayList<>(rounds.values()),
                new ArrayList<>(jurors.values())
        );
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
    private static boolean ignore(List<String> line) {
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

    private void throwIOE(String message, String cause, String fileName, int lineNumber, int valuePosition)
            throws IOException {
        throw new IOException(String.format("%s '%s' in %s [%d:%d]", message, cause, fileName, lineNumber, valuePosition));
    }

    private void throwIOE(String message, String fileName, int lineNumber, int valuePosition) throws IOException {
        throw new IOException(String.format("%s in %s [%d:%d]", message, fileName, lineNumber, valuePosition));
    }

    private void readTeams(Source src) throws IOException {
        // initialize collections
        Map<Integer, List<Group>> groups = new HashMap<>(16);
        rounds = new TreeMap<>();
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

            // create the group
            if (line.size() < 2) {
                throwIOE("Incomplete entry: missing group", src.name, ln, 1);
            }
            String groupName = getGroupName(line.get(1));

            // get the teams in group
            List<Team> teamsInGroup = new ArrayList<>(4);
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
                teamsInGroup.add(teams.get(cc));
            }
            if (!groups.containsKey(roundNumber)) {
                groups.put(roundNumber, new ArrayList<Group>(4));
            }
            groups.get(roundNumber).add(new Group(groupName, teamsInGroup));
            ln++;
        }

        for (Map.Entry<Integer, List<Group>> entry : groups.entrySet()) {
            Integer roundNumber = entry.getKey();
            rounds.put(roundNumber, new Round(roundNumber, entry.getValue()));
        }
        LOG.info("Team data loaded");
    }

    private void readJurors(Source src) throws IOException {
        // initialize collections
        jurors = new HashMap<>(100);

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

                String firstName = line.get(0);
                String lastName = line.get(1);

                // get JurorType tag
                JurorType jurorType = null;
                try {
                    jurorType = JurorType.getByLetter(line.get(2).charAt(0));
                } catch (IllegalArgumentException e) {
                    throwIOE("Invalid juror type tag", line.get(2), src.name, ln, 2);
                }

                // get first country
                CountryCode cc = CountryCodeIO.getByShortName(line.get(3));
                if (cc == null) {
                    throwIOE("Unknown country", line.get(3), src.name, ln, 3);
                }

                // read country conflicts, absences, and optional chair tag
                boolean chairCandidate = false;
                boolean experienced = false;
                List<Round> missingRounds = new ArrayList<>(5);
                List<CountryCode> conflicts = new ArrayList<>(3);
                conflicts.add(cc);
                boolean readingAbsences = false;
                for (int i = 4; i < line.size(); i++) {
                    if (i == line.size() - 1 && line.get(i) == null) {
                        LOG.trace("Ignoring trailing '{}' in {}[{}:{}]",
                                new Object[]{(char) PREFERENCE.getDelimiterChar(), src.name, ln, i});
                        break;
                    }

                    // chair tag
                    if ("C".equals(line.get(i))) {
                        if (chairCandidate) {
                            throwIOE("Duplicate chair tag", src.name, ln, i);
                        }
                        chairCandidate = true;
                    } else if ("E0".equals(line.get(i))) {
                        //experience tag
                        experienced = true;
                    } else {
                        try {
                            int roundNumber = Integer.parseInt(line.get(i));
                            missingRounds.add(rounds.get(roundNumber));
                            readingAbsences = true;
                        } catch (NumberFormatException ex) {
                            if (readingAbsences) {
                                // when the first absence is read, the rest of values should all be numbers
                                // (except for optional tags)
                                throwIOE("Invalid round number for juror absence", line.get(i), src.name, ln, i);
                            }
                            CountryCode conflict = CountryCodeIO.getByShortName(line.get(i));
                            if (conflict == null) {
                                throwIOE("Unknown country", line.get(i), src.name, ln, 3);
                            }
                            LOG.debug("Juror with multiple conflicts: {} {} {}", conflict, firstName, lastName);
                            conflicts.add(conflict);
                        }
                    }
                }
                Juror juror = new Juror(firstName, lastName, conflicts, jurorType, chairCandidate, experienced, missingRounds);
                jurors.put(String.format("%s, %s", line.get(1), line.get(0)), juror);
            }
            ln++;
        }
        LOG.info("Jury data loaded");
    }

    private Schedule readSchedule(Tournament tournament, Source src) throws IOException {
        Map<Integer, Round> rounds = new HashMap<>();
        for (Round r : tournament.getRounds()) {
            rounds.put(r.getNumber(), r);
        }
        Map<String, Juror> jurors = new HashMap<>();
        for (Juror j : tournament.getJurors()) {
            jurors.put(j.getLastName() + ", " + j.getFirstName(), j);
        }
        List<Assignment> assignments = new ArrayList<>();
        // reset jury sizes
        for (Map.Entry<Integer, Round> roundEntry : rounds.entrySet()) {
            roundEntry.getValue().setJurySize(0);
        }

        int ln = 1;

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

            // get the round instance
            Round round = rounds.get(roundNumber);
            if (round == null) {
                throwIOE("Cannot find round with number", line.get(0), src.name, ln, 0);
            }
            assert round != null;

            int jurorCount = line.size() - 2;
            if (line.get(line.size() - 1) == null) {
                // don't break the jury size with trailing ';'
                LOG.trace("Ignoring trailing '{}' in {}[{}:{}]",
                        new Object[]{(char) PREFERENCE.getDelimiterChar(), src.name, ln, line.size() - 1});
                jurorCount--;
            }

            // get group
            String groupName = getGroupName(line.get(1));
            Group group = null;
            for (Group g : round.getGroups()) {
                if (groupName.equals(g.getName())) {
                    group = g;
                }
            }
            if (group == null) {
                throwIOE("Cannot find group for name", line.get(1), src.name, ln, 1);
            }

            int jurySize = jurorCount;
            for (int i = 0; i < jurorCount; i++) {
                String name = line.get(i + 2);
                Role role = i == 0 ? Role.CHAIR : Role.VOTING;
                if (name != null) {
                    // handle non-voting jurors
                    if (name.startsWith("(") && name.endsWith(")")) {
                        name = name.substring(1, name.length() - 1);
                        role = Role.NON_VOTING;
                        jurySize--;
                    }

                    Juror juror = jurors.get(name);
                    if (juror == null) {
                        throwIOE("Unkown juror", name, src.name, ln, i + 2);
                    }
                    assignments.add(new Assignment(juror, group, role));
                }
            }

            // set jury size
            if (round.getJurySize() <= 0) {
                LOG.debug("{} jury size: {}.", round, jurySize);
                round.setJurySize(jurySize);
            }

            ln++;
        }
        LOG.info("Schedule data loaded");
        return new Schedule(tournament, assignments);
    }

    private static class Source {

        private final String name;
        private final CsvListReader reader;

        public Source(InputSource inputSource) throws IOException {
            this.name = inputSource.getName();
            InputStream is = inputSource.getUrl().openStream();
            this.reader = new CsvListReader(new InputStreamReader(is, inputSource.getCharset()), PREFERENCE);
        }
    }
}
