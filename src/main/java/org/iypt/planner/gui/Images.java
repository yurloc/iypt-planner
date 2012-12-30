package org.iypt.planner.gui;

import com.neovisionaries.i18n.CountryCode;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.media.Image;

/**
 *
 * @author jlocker
 */
public class Images {

    public static final URL PERSON_DEFAULT = Images.class.getResource("img/status_offline.png");
    public static final URL PERSON_LOCKED = Images.class.getResource("img/lock.png");
    public static final URL PERSON_BROKEN_LOCK = Images.class.getResource("img/lock_break.png");
    private static final Map<CountryCode, URL> flags = new HashMap<>();

    static {
        for (CountryCode cc : CountryCode.values()) {
            flags.put(cc, Images.class.getResource(String.format("img/flags/%s.png", cc.getAlpha2().toLowerCase())));
        }
    }

    public static Image getImage(String imageName) {
        return getImage(Images.class.getResource("img/" + imageName));
    }

    public static Image getFlag(CountryCode country) {
        return getImage(flags.get(country));
    }

    public static Image getImage(URL url) {
        Image img = (Image) ApplicationContext.getResourceCache().get(url);

        if (img == null) {
            try {
                img = Image.load(url);
            } catch (TaskExecutionException exception) {
                throw new RuntimeException(exception);
            }

            ApplicationContext.getResourceCache().put(url, img);
        }
        return img;
    }
}
