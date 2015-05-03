package org.iypt.planner.gui;

import org.apache.pivot.wtk.media.Image;
import org.iypt.planner.domain.Juror;
import org.iypt.planner.domain.JurorType;
import org.iypt.planner.domain.Seat;

/**
 *
 * @author jlocker
 */
public class SeatInfo {

    private final Seat seat;
    private final Juror juror;
    private final Image flag;
    private final String name;
    private final boolean type;
    private final boolean chair;
    private Image icon;
    private boolean locked;

    /**
     * Construct empty seat info.
     *
     * @param seat may be null
     */
    private SeatInfo(Seat seat) {
        this.icon = Images.getImage(Images.PERSON_DEFAULT);
        this.seat = seat;
        this.juror = null;
        this.flag = null;
        this.name = null;
        this.type = false;
        this.chair = false;
        this.locked = false;
    }

    /**
     * Construct juror info.
     *
     * @param seat may be null (idle juror)
     * @param juror must not be null
     */
    private SeatInfo(Seat seat, Juror juror) {
        if (juror == null) {
            throw new IllegalArgumentException("Argument 'juror' must not be null");
        }
        this.seat = seat;
        this.juror = juror;
        this.icon = Images.getImage(Images.PERSON_DEFAULT);
        this.flag = Images.getFlag(juror.getCountry());
        this.name = toDisplayName2(juror);
        this.type = juror.getType() == JurorType.INDEPENDENT;
        this.chair = juror.isChairCandidate();
    }

    public static SeatInfo newInstance(Juror juror) {
        // TODO properly implement null object for Juror (and country) and never use null
        if (juror == Juror.NULL || juror == null) {
            return new SeatInfo(null);
        }
        return new SeatInfo(null, juror);
    }

    static SeatInfo newInstance(Seat seat) {
        if (seat.getJuror() != null) {
            return new SeatInfo(seat, seat.getJuror());
        }
        return new SeatInfo(seat);
    }

    private static String toDisplayName1(Juror juror) {
        StringBuilder sb = new StringBuilder(20);
        if (juror.getLastName().length() > 10) {
            sb.append(juror.getLastName(), 0, 9).append("_");
        } else {
            sb.append(juror.getLastName());
        }
        sb.append(", ").append(juror.getFirstName(), 0, 1).append(".");
        return sb.toString();
    }

    private static String toDisplayName2(Juror juror) {
        return juror.getFirstName().substring(0, 1) + juror.getLastName().charAt(0);
    }

    public Seat getSeat() {
        return seat;
    }

    public Juror getJuror() {
        return juror;
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

    public boolean isLocked() {
        return locked;
    }

    public void breakLock() {
        if (!locked) {
            throw new IllegalStateException("Cannot break lock on unlocked seat.");
        }
        icon = Images.getImage(Images.PERSON_BROKEN_LOCK);
    }

    public void lock() {
        locked = true;
        icon = Images.getImage(Images.PERSON_LOCKED);
    }

    public void unlock() {
        locked = false;
        icon = Images.getImage(Images.PERSON_DEFAULT);
    }
}
