package org.iypt.planner.gui;

import java.util.List;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.TabPane;
import org.apache.pivot.wtk.TabPaneSelectionListener;
import org.apache.pivot.wtk.content.ButtonData;
import org.apache.pivot.wtk.skin.ContainerSkin;
import org.iypt.planner.domain.Juror;
import org.iypt.planner.domain.Round;

/**
 *
 * @author jlocker
 */
public class TournamentScheduleSkin extends ContainerSkin implements TournamentScheduleListener {

    private TabPane content;
    private RoundView[] views;
    private static final HashMap<String, String> tabPaneStyles = new HashMap<>();

    static {
        tabPaneStyles.put("tabOrientation", "horizontal");
    }

    @Override
    public void install(Component component) {
        super.install(component);
        final TournamentSchedule schedule = (TournamentSchedule) component;
        schedule.getTournamentScheduleListeners().add(this);
        content = new TabPane();
        content.getTabPaneSelectionListeners().add(new TabPaneSelectionListener.Adapter() {
            @Override
            public void selectedIndexChanged(TabPane tabPane, int previousSelectedIndex) {
                schedule.roundSelected(tabPane.getSelectedIndex());
            }
        });
        content.setStyles(tabPaneStyles);
        schedule.add(content);

        // initialize round views
        List<Round> rounds = schedule.getTournament().getRounds();
        views = new RoundView[rounds.size()];
        for (int i = 0; i < views.length; i++) {
            RoundView roundView = new RoundView(schedule, rounds.get(i));
            views[i] = roundView;
            content.getTabs().add(roundView);
            TabPane.setTabData(roundView, new ButtonData("Round #" + rounds.get(i).getNumber()));
        }
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
    public void scheduleChanged(TournamentSchedule schedule) {
        // TODO check this
        // TournamentSchedule tournament = (TournamentSchedule) getComponent();
        List<Round> rounds = schedule.getTournament().getRounds();
        for (int i = 0; i < views.length; i++) {
            views[i].update(schedule, rounds.get(i));
        }
    }

    // TODO refactor the listener interfaces
    @Override
    public void roundSelected(Round round) {
        // not interested
    }

    @Override
    public void jurorSelected(Juror juror) {
        // not interested
    }
}
