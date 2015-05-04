package org.iypt.planner.gui;

import org.apache.pivot.collections.List;
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
    private RoundModel activeRound;

    @Override
    public void install(Component component) {
        super.install(component);

        // get component and register skin as a listener
        final TournamentSchedule schedule = (TournamentSchedule) component;
        schedule.getTournamentScheduleListeners().add(this);

        // create content and add it to component
        content = new TabPane();
        schedule.add(content);

        // set up controls (static)
        content.getStyles().put("tabOrientation", Orientation.HORIZONTAL);
        PushButton lockButton = new PushButton(getImage(LOCK_LIGHT));
        lockButton.getStyles().put("toolbar", true);
        BoxPane corner = new BoxPane(Orientation.HORIZONTAL);
        corner.add(lockButton);
        content.setCorner(corner);

        // register listeners
        content.getTabPaneSelectionListeners().add(new TabPaneSelectionListener.Adapter() {
            @Override
            public void selectedIndexChanged(TabPane tabPane, int previousSelectedIndex) {
                schedule.roundSelected(tabPane.getSelectedIndex());
            }
        });
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

        // initialize round views (dynamic controls)
        List<RoundModel> rounds = schedule.getRounds();
        views = new RoundView[rounds.getLength()];
        for (int i = 0; i < views.length; i++) {
            RoundView roundView = new RoundView(rounds.get(i));
            views[i] = roundView;
            content.getTabs().add(roundView);
            TabPane.setTabData(roundView, new ButtonData(getImage(LOCK_LIGHT), rounds.get(i).toString()));
            roundView.getRoundViewListeners().add(new RoundViewListener() {

                @Override
                public void roundChanged(RoundView round) {
                    // do nothing
                }

                @Override
                public void seatSelected(SeatInfo seat) {
                    schedule.seatSelected(seat);
                }

                @Override
                public void seatLockChanged(SeatInfo seat) {
                    if (seat.isLocked()) {
                        schedule.lockSeat(seat);
                    } else {
                        schedule.unlockSeat(seat);
                    }
                }
            });
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
        List<RoundModel> rounds = schedule.getRounds();
        for (int i = 0; i < views.length; i++) {
            views[i].update(rounds.get(i));
        }
    }

    @Override
    public void roundSelected(RoundModel round) {
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
    public void roundLockRequested(RoundModel round) {
        // not interested
    }

    @Override
    public void roundLocksChanged() {
        TournamentSchedule schedule = (TournamentSchedule) getComponent();
        for (RoundModel round : schedule.getRounds()) {
            ButtonData tabData = (ButtonData) TabPane.getTabData(views[round.getNumber() - 1]);
            tabData.setIcon(round.isLocked() ? LOCK : LOCK_LIGHT);
        }
    }
}
