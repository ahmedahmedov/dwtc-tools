package webreduce.indexing;

import java.util.regex.Pattern;

/**
 * Created by ahmedov on 13/06/16.
 */
public final class constants {

    //static Pattern toDouble = Pattern.compile("-?([$])?[\\d,]+(?:\\.\\d*)?(?:(?:[bm]il)|M|m|K|k|B|b)?(?(1)|[€])");
    //(-?([$])?[\d,]+(?:\.\d*)?(?:(?:[bm]il)|M|m|K|k|B|b)?(?(2)|[€]))
    //(-?[$]?[\d,]+(?:\.\d*)?((?:[bm]il)|M|m|K|k|B|b)?[€]?) -- catch a second captured group
    public static final Pattern toDouble = Pattern.compile("-?([$|€])?[\\d,]+(\\.\\d*)?\\s?((billion[s]?|million[s]?)|[b]n|([bm]il[s]?)|m|k|b)?[€]?", Pattern.CASE_INSENSITIVE);
    public static final Pattern toDoubleM = Pattern.compile("-?[$|€]?[\\d,]+\\.?\\d*\\s?(millions[s]?|mn|mil[s]?|m)", Pattern.CASE_INSENSITIVE);
    public static final Pattern toDoubleK = Pattern.compile("-?[$|€]?[\\d,]+\\.?\\d*\\s?(?:k)", Pattern.CASE_INSENSITIVE);
    public static final Pattern toDoubleB = Pattern.compile("-?[$|€]?[\\d,]+\\.?\\d*\\s?(billion[s]?|bn|bil[s]?|B|b)", Pattern.CASE_INSENSITIVE);

    private constants(){

    }
}
