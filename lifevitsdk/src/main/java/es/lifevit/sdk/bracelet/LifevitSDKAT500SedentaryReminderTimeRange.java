package es.lifevit.sdk.bracelet;


/**
 * @Deprecated
 * <p> Use {@link LifevitSDKSedentaryAlarm} instead.</p>
 */
@Deprecated
public class LifevitSDKAT500SedentaryReminderTimeRange extends LifevitSDKSedentaryAlarm {


    public LifevitSDKAT500SedentaryReminderTimeRange() {
        super();
    }


    public LifevitSDKAT500SedentaryReminderTimeRange(int startHour, int startMinute, int endHour, int endMinute, SedentaryIntervals intervalCode) {
        super(startHour, startMinute, endHour, endMinute, intervalCode);
    }

}
