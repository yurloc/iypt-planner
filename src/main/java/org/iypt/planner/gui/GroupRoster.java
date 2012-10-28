package org.iypt.planner.gui;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.media.Image;
import org.iypt.planner.domain.CountryCode;
import org.iypt.planner.domain.Juror;
import org.iypt.planner.domain.JurorType;

/**
 *
 * @author jlocker
 */
public class GroupRoster extends Container {

    public static class JurorRow {

        private Image icon;
        private Image flag;
        private String name;
        private boolean type;
        private boolean chair;

        public JurorRow(Juror juror) {
            this.icon = Images.getImage(Images.PERSON_DEFAULT);
            this.flag = Images.getFlag(juror.getCountry());
            this.name = juror.fullName();
            this.type = juror.getType() == JurorType.INDEPENDENT;
            this.chair = juror.isChairCandidate();
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

    public GroupRoster() {
        setSkin(new GroupRosterSkin());
    }

    public String getGroupName() {
        return "Group A";
    }

    public List<CountryCode> getTeams() {
        List<CountryCode> teams = new ArrayList<>();
        teams.add(CountryCode.SK);
        teams.add(CountryCode.CZ);
        teams.add(CountryCode.DE);
        return teams;
    }

    public List<JurorRow> getJurorList() {
        List<JurorRow> list = new ArrayList<>();
        Juror juror = new Juror("Jiří", "Locker", CountryCode.CZ, JurorType.TEAM_LEADER, true);
        list.add(new JurorRow(juror));
        list.add(new JurorRow(juror));
        list.add(new JurorRow(juror));
        return list;
    }

    public ListenerList<GroupRosterListener> getGroupRosterListeners() {
        return groupRosterListenerList;
    }
}
