package es.lifevit.sdk.bracelet;


/**
 * @Deprecated
 * <p> Use {@link LifevitSDKAlarmTime} instead.</p>
 */
@Deprecated
public class LifevitSDKAt500HrAlarmTime extends LifevitSDKAlarmTime {

    public LifevitSDKAt500HrAlarmTime(boolean isSecondaryAlarm, int hour, int minute, boolean monday, boolean tuesday, boolean wednesday,
                               boolean thursday, boolean friday, boolean saturday, boolean sunday) {

        super(isSecondaryAlarm, hour, minute, monday, tuesday, wednesday, thursday, friday, saturday, sunday);

    }
}
