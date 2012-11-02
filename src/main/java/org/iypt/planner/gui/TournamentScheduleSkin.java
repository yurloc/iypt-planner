package org.iypt.planner.gui;

import org.apache.pivot.collections.HashMap;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.TabPane;
import org.apache.pivot.wtk.TabPaneSelectionListener;
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
        tabPaneStyles.put("tabOrientation", "horizontal");
    }

    @Override
    public void install(Component component) {
        super.install(component);
        final TournamentSchedule tournament = (TournamentSchedule) component;
        tournament.getTournamentScheduleListeners().add(this);
        content = new TabPane();
        content.getTabPaneSelectionListeners().add(new TabPaneSelectionListener.Adapter() {
            @Override
            public void selectedIndexChanged(TabPane tabPane, int previousSelectedIndex) {
                tournament.roundSelected(tabPane.getSelectedIndex());
            }
        });
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
        // TODO check this
//        TournamentSchedule tournament = (TournamentSchedule) getComponent();
        for (Round round : tournament.getTournament().getRounds()) {
            RoundView roundView = new RoundView(tournament.getTournament(), round);
            content.getTabs().add(roundView);
            TabPane.setTabData(roundView, new ButtonData("Round #" + round.getNumber()));
        }
    }

    @Override
    public void roundSelected(Round round) {
        // not interested
    }
}
