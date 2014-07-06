package org.iypt.planner.csv;

import com.neovisionaries.i18n.CountryCode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.iypt.planner.domain.Absence;
import org.iypt.planner.domain.Conflict;
import org.iypt.planner.domain.Juror;
import org.iypt.planner.domain.JurorType;
import org.iypt.planner.domain.Tournament;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author jlocker
 */
public class TournamentUtils {

    private Tournament t;
    private Map<Juror, List<CountryCode>> conflictMap = new HashMap<>();
    private Map<Juror, List<Integer>> absenceMap = new HashMap<>();

    public TournamentUtils(Tournament t) {
        this.t = t;

        for (Conflict conflict : t.getConflicts()) {
            Juror j = conflict.getJuror();
            if (!conflictMap.containsKey(j)) {
                conflictMap.put(j, new ArrayList<CountryCode>(2));
            }
            conflictMap.get(j).add(conflict.getCountry());
        }

        for (Absence absence : t.getAbsences()) {
            Juror j = absence.getJuror();
            if (!absenceMap.containsKey(j)) {
                absenceMap.put(j, new ArrayList<Integer>(5));
            }
            absenceMap.get(j).add(absence.getRoundNumber());
        }
    }

    public List<CountryCode> getConflicts(Juror j) {
        return conflictMap.get(j);
    }

    public Juror getJuror(int round, int group, int seat) {
        return t.getSeats(t.getRounds().get(round).getGroups().get(group).getJury()).get(seat).getJuror();
    }

    public void verifyJuror(Juror juror, String fullName, JurorType type, boolean chair) {
        assertThat(juror.fullName()).isEqualTo(fullName);
        assertThat(juror.getType()).isEqualTo(type);
        assertThat(juror.isChairCandidate()).isEqualTo(chair);
    }

    public void verifyJuror(Juror juror, String fullName, JurorType type, boolean chair, CountryCode... countries) {
        if (countries.length == 0) {
            throw new IllegalArgumentException();
        }
        verifyJuror(juror, fullName, type, chair);
        assertThat(juror.getCountry()).isEqualTo(countries[0]);
        assertThat(getConflicts(juror)).containsExactly(countries);

    }

    public void verifyJuror(Juror juror, String fullName, JurorType type, boolean chair, int... rounds) {
        verifyJuror(juror, fullName, type, chair);
    }

    public void verifyJuror(Juror juror, String fullName, JurorType type, boolean chair, CountryCode country, Integer... rounds) {
        verifyJuror(juror, fullName, type, chair, new CountryCode[]{country});
        assertThat(absenceMap.get(juror)).containsExactly(rounds);
    }
}
