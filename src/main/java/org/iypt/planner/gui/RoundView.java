package org.iypt.planner.gui;

import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.Expander;
import org.apache.pivot.wtk.Orientation;
import org.iypt.planner.domain.Group;
import org.iypt.planner.domain.Round;
import org.iypt.planner.domain.Tournament;

/**
 *
 * @author jlocker
 */
public class RoundView extends Expander {

    private Container content = new BoxPane(Orientation.HORIZONTAL);

    public RoundView() {
        super();
        super.setExpanded(true);
        setContent(content);
    }

    public void update(Tournament tournament, Round round) {
        this.setTitle("Round #" + round.getNumber());

        content.removeAll();
        for (Group group : round.getGroups()) {
            content.add(new GroupRoster(tournament, group));
        }
    }
}
