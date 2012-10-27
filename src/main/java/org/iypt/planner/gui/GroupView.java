package org.iypt.planner.gui;

import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.TablePane;
import org.iypt.planner.domain.Group;
import org.iypt.planner.domain.Juror;
import org.iypt.planner.domain.JurySeat;
import org.iypt.planner.domain.Team;
import org.iypt.planner.domain.Tournament;

/**
 *
 * @author jlocker
 */
public class GroupView extends TablePane {

    GroupView(Tournament tournament, Group group) {
        super();
        getColumns().add(new TablePane.Column());
        getColumns().add(new TablePane.Column());

        TablePane.Row header = new TablePane.Row();
        header.add(new Label("Group " + group.getName()));
        header.add(new Label("Jury"));
        getRows().add(header);

        BoxPane teamPane = new BoxPane(Orientation.HORIZONTAL);
        for (Team team : group.getTeams()) {
            ImageView teamFlag = new ImageView(Images.getFlag(team.getCountry()));
            teamFlag.setTooltipText(team.getCountry().getName());
            teamFlag.setTooltipDelay(300);
            teamPane.add(teamFlag);
        }

        BoxPane juryPane = new BoxPane(Orientation.HORIZONTAL);
        for (JurySeat seat : tournament.getJurySeats()) {
            if (seat.getJury().equals(group.getJury())) {
                BoxPane boxPane = new BoxPane(Orientation.HORIZONTAL);
                Juror juror = seat.getJuror();
                if (juror == null) {
                    boxPane.add(new ImageView(Images.getImage(Images.PERSON_DEFAULT)));
                } else {
                    boxPane.add(new ImageView(Images.getFlag(juror.getCountry())));
                }
                boxPane.add(new Label("name"));
                juryPane.add(boxPane);
            }
        }

        TablePane.Row row = new TablePane.Row();
        row.add(teamPane);
        row.add(juryPane);
        getRows().add(row);
    }
}
