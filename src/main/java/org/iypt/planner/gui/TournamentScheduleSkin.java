package org.iypt.planner.gui;

import org.apache.pivot.collections.HashMap;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.TabPane;
import org.apache.pivot.wtk.content.ButtonData;
import org.apache.pivot.wtk.skin.ContainerSkin;
import org.iypt.planner.domain.Round;

/**
 *
 * @author jlocker
 */
public class TournamentScheduleSkin extends ContainerSkin implements TournamentScheduleListener {

    private TabPane content;
    private static final HashMap<String, String> tabPaneStyles = new HashMap<>();

    static {
        tabPaneStyles.put("tabOrientation", "vertical");
    }

    @Override
    public void install(Component component) {
        super.install(component);
        TournamentSchedule tournament = (TournamentSchedule) component;
        tournament.getTournamentScheduleListeners().add(this);
        content = new TabPane();
        content.setStyles(tabPaneStyles);
        tournament.add(content);
    }

    @Override
    public int getPreferredWidth(int height) {
        return content.getPreferredWidth(height);
    }

    @Override
    public int getPreferredHeight(int width) {
        return content.getPreferredHeight(width);
    }

    @Override
    public Dimensions getPreferredSize() {
        return content.getPreferredSize();
    }

    @Override
    public void layout() {
        content.setLocation(0, 0);
        content.setSize(getWidth(), getHeight());
    }

    @Override
    public void scheduleChanged(TournamentSchedule tournament) {
        content.getTabs().remove(0, content.getTabs().getLength());
        for (Round round : tournament.getTournament().getRounds()) {
            RoundView roundView = new RoundView(tournament.getTournament(), round);
            content.getTabs().add(roundView);
            TabPane.setTabData(roundView, new ButtonData("Round #" + round.getNumber()));
        }
    }
}
