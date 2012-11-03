package org.iypt.planner.gui;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.media.Image;
import org.iypt.planner.domain.CountryCode;
import org.iypt.planner.domain.Group;
import org.iypt.planner.domain.Juror;
import org.iypt.planner.domain.JurorType;
import org.iypt.planner.domain.JurySeat;
import org.iypt.planner.domain.Team;

/**
 *
 * @author jlocker
 */
public class GroupRoster extends Container {

    public static class JurorRow {

        private Juror juror;
        private Image icon;
        private Image flag;
        private String name;
        private boolean type;
        private boolean chair;

        public JurorRow(Juror juror) {
            this.juror = juror;
            this.icon = Images.getImage(Images.PERSON_DEFAULT);
            this.flag = Images.getFlag(juror.getCountry());
            this.name = toDisplayName2(juror);
            this.type = juror.getType() == JurorType.INDEPENDENT;
            this.chair = juror.isChairCandidate();
        }

        private static String toDisplayName1(Juror juror) {
            StringBuilder sb = new StringBuilder(20);
            if (juror.getLastName().length() > 10) {
                sb.append(juror.getLastName(), 0, 9).append("_");
            } else {
                sb.append(juror.getLastName());
            }
            sb.append(", ").append(juror.getFirstName(), 0, 1).append(".");
            return sb.toString();
        }

        private static String toDisplayName2(Juror juror) {
            return juror.getFirstName().substring(0, 1) + juror.getLastName().charAt(0);
        }

        public Image getIcon() {
            return icon;
        }

        public Image getFlag() {
            return flag;
        }

        public String getName() {
            return name;
        }

        public boolean isType() {
            return type;
        }

        public boolean isChair() {
            return chair;
        }
    }

    private static final class GroupRosterListenerList extends ListenerList<GroupRosterListener> implements GroupRosterListener {

        @Override
        public void groupRosterChanged(GroupRoster group) {
            for (GroupRosterListener listener : this) {
                listener.groupRosterChanged(group);
            }
        }
    }

    private GroupRosterListenerList groupRosterListenerList = new GroupRosterListenerList();
    private TournamentSchedule schedule;
    private Group group;
    private List<JurorRow> jurorList = new ArrayList<>();

    // TODO move this into the skin?
    void jurorSelected(Object row) {
        if (row != null) {
            JurorRow jurorRow = (JurorRow) row;
            schedule.jurorSelected(jurorRow.juror);
        }
    }

    public GroupRoster() {
        setSkin(new GroupRosterSkin());
    }

    GroupRoster(TournamentSchedule schedule, Group group) {
        this.schedule = schedule;
        this.group = group;

        for (JurySeat seat : schedule.getTournament().getJurySeats()) {
            if (seat.getJury().equals(group.getJury())) {
                jurorList.add(new JurorRow(seat.getJuror()));
            }
        }

        setSkin(new GroupRosterSkin());
    }

    public String getGroupName() {
        return group.getName();
    }

    public List<CountryCode> getTeams() {
        List<CountryCode> teams = new ArrayList<>();
        for (Team team : group.getTeams()) {
            teams.add(team.getCountry());
        }
        return teams;
    }

    public List<JurorRow> getJurorList() {
        return jurorList;
    }

    public ListenerList<GroupRosterListener> getGroupRosterListeners() {
        return groupRosterListenerList;
    }
}