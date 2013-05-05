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
import org.iypt.planner.domain.Juror;
import org.iypt.planner.domain.Round;
import org.iypt.planner.domain.Tournament;
import org.iypt.planner.gui.GroupRoster.JurorRow;
import org.iypt.planner.solver.TournamentSolver;

import static org.iypt.planner.gui.Images.LOCK;
import static org.iypt.planner.gui.Images.LOCK_LIGHT;
import static org.iypt.planner.gui.Images.getImage;

/**
 *
 * @author jlocker
 */
public class TournamentScheduleSkin extends ContainerSkin implements TournamentScheduleListener, LockListener {

    private TabPane content;
    private RoundView[] views;

    @Override
    public void install(Component component) {
        super.install(component);
        final TournamentSchedule schedule = (TournamentSchedule) component;
        schedule.getTournamentScheduleListeners().add(this);
        schedule.getSolver().getLockListeners().add(this);
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
                schedule.requestRoundLock();
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
        List<Round> rounds = schedule.getSolver().getTournament().getRounds();
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
        List<Round> rounds = schedule.getSolver().getTournament().getRounds();
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

    @Override
    public void jurorLocked(JurorRow jurorRow) {
        // not interested
    }

    @Override
    public void jurorUnlocked(JurorRow jurorRow) {
        // not interested
    }

    @Override
    public void requestRoundLock() {
        // not interested
    }

    @Override
    public void roundLockChanged(TournamentSolver solver) {
        for (int i = 0; i < views.length; i++) {
            ButtonData tabData = (ButtonData) TabPane.getTabData(views[i]);
            Tournament t = solver.getTournament();
            Round round = t.getRounds().get(i);
            if (t.isLocked(round)) {
                tabData.setIcon(LOCK);
            } else {
                tabData.setIcon(LOCK_LIGHT);
            }
        }
    }
}
