package org.iypt.planner.io.csv;

import com.neovisionaries.i18n.CountryCode;
import org.iypt.planner.api.domain.Assignment;
import org.iypt.planner.api.domain.Group;
import org.iypt.planner.api.domain.Juror;
import org.iypt.planner.api.domain.JurorType;
import org.iypt.planner.api.domain.Schedule;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author jlocker
 */
public class TournamentUtils {

    private final Schedule schedule;

    public TournamentUtils(Schedule schedule) {
        this.schedule = schedule;
    }

    public Juror getJuror(int round, int group, int seat) {
        Group g = schedule.getTournament().getRounds().get(round).getGroups().get(group);
        for (Assignment a : schedule.getAssignments()) {
            if (a.getGroup().equals(g)) {
                if (seat == 0) {
                    return a.getJuror();
                }
                seat--;
            }
        }
        return null;
    }

    public void verifyJuror(Juror juror, String firstName, String lastName, JurorType type, boolean chair) {
        assertThat(juror.getFirstName()).isEqualTo(firstName);
        assertThat(juror.getLastName()).isEqualTo(lastName);
        assertThat(juror.getType()).isEqualTo(type);
        assertThat(juror.isChairCandidate()).isEqualTo(chair);
    }

    public void verifyJuror(Juror juror, String firstName, String lastName, JurorType type, boolean chair, CountryCode... countries) {
        if (countries.length == 0) {
            throw new IllegalArgumentException();
        }
        verifyJuror(juror, firstName, lastName, type, chair);
        assertThat(juror.getConflicts()).as(juror.getLastName()).containsExactly(countries);
        assertThat(juror.getConflicts()).containsExactly(countries);

    }

    public void verifyJuror(Juror juror, String firstName, String lastName, JurorType type, boolean chair, int... rounds) {
        verifyJuror(juror, firstName, lastName, type, chair);
    }

    public void verifyJuror(Juror juror, String firstName, String lastName, JurorType type, boolean chair, CountryCode country, Integer... rounds) {
        verifyJuror(juror, firstName, lastName, type, chair, new CountryCode[]{country});
        assertThat(juror.getMissingRounds()).extracting("number").containsExactly((Object[]) rounds);
    }
}
