package es.lifevit.sdk.pillreminder;

import java.util.ArrayList;

public class LifevitSDKPillReminderAlarmListData extends LifevitSDKPillReminderData {

    private ArrayList<Object> alarmList = new ArrayList<>();

    public void setAlarmList(ArrayList alarmList) {
        this.alarmList = alarmList;
    }

    public ArrayList getAlarmList() {
        return alarmList;
    }
}
