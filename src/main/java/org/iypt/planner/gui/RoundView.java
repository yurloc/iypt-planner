package org.iypt.planner.gui;

import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.Rollup;
import org.iypt.planner.domain.Group;
import org.iypt.planner.domain.Round;
import org.iypt.planner.domain.Tournament;

/**
 *
 * @author jlocker
 */
public class RoundView extends Rollup {

    private Label roundLabel = new Label();
    private Container content = new BoxPane(Orientation.VERTICAL);

    public RoundView() {
        super(true);
        setHeading(roundLabel);
        setContent(content);
    }

    public void update(Tournament tournament, Round round) {
        roundLabel.setText("Round #" + round.getNumber());
        roundLabel.setTooltipText("Day " + round.getDay());
        roundLabel.setTooltipDelay(300);

//        content.clear();
        for (Group group : round.getGroups()) {
            content.add(new GroupView(tournament, group));
        }
    }
}
