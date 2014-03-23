package org.iypt.planner.gui;

import java.util.List;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentMouseListener;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TabPane;
import org.apache.pivot.wtk.TabPaneSelectionListener;
import org.apache.pivot.wtk.content.ButtonData;
import org.apache.pivot.wtk.skin.ContainerSkin;
import org.iypt.planner.domain.Round;
import org.iypt.planner.domain.Tournament;

import static org.iypt.planner.gui.Images.LOCK;
import static org.iypt.planner.gui.Images.LOCK_LIGHT;
import static org.iypt.planner.gui.Images.getImage;

/**
 *
 * @author jlocker
 */
public class TournamentScheduleSkin extends ContainerSkin implements TournamentScheduleListener {

    private TabPane content;
    private RoundView[] views;
    private Round activeRound;

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
        content.getStyles().put("tabOrientation", Orientation.HORIZONTAL);
        schedule.add(content);
        PushButton lockButton = new PushButton(getImage(LOCK_LIGHT));
        lockButton.getStyles().put("toolbar", true);
        lockButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                schedule.requestRoundLock(activeRound);
            }
        });
        lockButton.getComponentMouseListeners().add(new ComponentMouseListener.Adapter() {
            @Override
            public void mouseOver(Component component) {
                ((PushButton) component).setButtonData(getImage(LOCK));
            }

            @Override
            public void mouseOut(Component component) {
                ((PushButton) component).setButtonData(getImage(LOCK_LIGHT));
            }
        });
        BoxPane corner = new BoxPane(Orientation.HORIZONTAL);
        corner.add(lockButton);
        content.setCorner(corner);

        // initialize round views
        List<Round> rounds = schedule.getTournament().getRounds();
        views = new RoundView[rounds.size()];
        for (int i = 0; i < views.length; i++) {
            RoundView roundView = new RoundView(schedule, rounds.get(i));
            views[i] = roundView;
            content.getTabs().add(roundView);
            TabPane.setTabData(roundView, new ButtonData(getImage(LOCK_LIGHT), "Round #" + rounds.get(i).getNumber()));
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
        activeRound = round;
    }

    @Override
    public void seatSelected(SeatInfo seatInfo) {
        // not interested
    }

    @Override
    public void seatLocked(SeatInfo seatInfo) {
        // not interested
    }

    @Override
    public void seatUnlocked(SeatInfo seatInfo) {
        // not interested
    }

    @Override
    public void roundLockRequested(Round round) {
        // not interested
    }

    @Override
    public void roundLocksChanged(Tournament tournament) {
        for (Round round : tournament.getRounds()) {
            ButtonData tabData = (ButtonData) TabPane.getTabData(views[round.getNumber() - 1]);
            tabData.setIcon(tournament.isLocked(round) ? LOCK : LOCK_LIGHT);
        }
    }
}
