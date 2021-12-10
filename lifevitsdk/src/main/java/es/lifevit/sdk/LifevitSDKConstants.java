package es.lifevit.sdk;


import no.nordicsemi.android.dfu.DfuBaseService;

/**
 * Created by aescanuela on 4/8/17.
 */

public class LifevitSDKConstants {

    public enum BraceletVitalNotification {
        STOP_CALL(0xff), CALL(0), INFO(1), WECHAT(2), FACEBOOK(3), INSTAGRAM(4), SKYPE(5), TELEGRAM(6), TWITTER(7), VKCLIENT(8), WHATSAPP(9), QQ(10), LINKEDIN(11);
        public final int value;

        private BraceletVitalNotification(int value) {
            this.value = value;
        }
    }

    public enum BraceletVitalDataType {
        HR(0), TEMPERATURE(1), OXIMETER(2), VITALS(3), STEPS(4), SLEEP(5), SPORTS(6);
        public final int value;

        private BraceletVitalDataType(int value) {
            this.value = value;
        }
    }

    public enum BraceletVitalSportType {
        RUN(0x00), CYCLING(0x01), BADMINTON(0x02), FOOTBALL(0x03),
        TENNIS(0x04), YOGA(0x05), BREATH(0x06), DANCE(0x07),
        BASKETBALL(0x08),
        WALK(0x09),
        GYM(0x10),
        CRICKET(0x11),
        HIKING(0x12),
        AEROBICS(0x13),
        PINGPONG(0x14),
        ROPEJUMP(0x15),
        SITUPS(0x16),
        VOLLEYBALL(0x17);

        public final int value;

        private BraceletVitalSportType(int value) {
            this.value = value;
        }
    }

    public enum BraceletVitalMeditationLevel {
        SMALL_TIME(0x00),
        MEDIUM_TIME(0x01),
        LONG_TIME(0x02);
        public final int value;

        private BraceletVitalMeditationLevel(int value) {
            this.value = value;
        }
    }

    public enum BraceletVitalAlarmType {
        ALARM(0x01),
        MEDICATION(0x02),
        DRINK_WATER(0x03);
        public final int value;

        BraceletVitalAlarmType(int value) {
            this.value = value;
        }
    }

    public enum BraceletVitalPeriodWorkingMode {
        OFF(0x00),
        TIME_PERIOD(0x01),
        TIME_INTERVAL(0x02);
        public final int value;

        BraceletVitalPeriodWorkingMode(int value) {
            this.value = value;
        }
    }

    public enum BraceletVitalOperation {
        HANG_UP(0), PHOTO(1), MUSIC_PLAY(2), MUSIC_PAUSE(3), MUSIC_PREVIOUS(4), MUSIC_NEXT(5), MUSIC_VOLUME_UP(6), MUSIC_VOLUME_DOWN(7), FIND_PHONE(8);

        public final int value;

        private BraceletVitalOperation(int value) {
            this.value = value;
        }
    }

    public enum BraceletVitalCommand {
        SET_TIME, GET_TIME, REALTIME, GET_STEPS_GOAL, SET_STEPS_GOAL, GET_AUTOMATIC_PERIOD, GET_ACTIVITY_PERIOD,
        SET_ACTIVITY_PERIOD, GET_MAC_ADDRESS, SET_REALTIME, SET_DEVICE_PARAMETERS, GET_DEVICE_PARAMETERS, SET_USER_INFO,
        GET_USER_INFO, GET_BATTERY, SET_AUTOMATIC_PERIOD, HEALTH_MEASUREMENT, GET_TOTAL_STEPS, GET_STEPS, GET_SLEEP,
        GET_HR, GET_HR_AUTO, GET_OXY, GET_OXY_AUTO, GET_VITALS, GET_SPORTS, GET_TEMPERATURE, GET_TEMPERATURE_AUTO,
        START_ECG, GET_ECG_STATUS, GET_ECG_DATA, GET_ECG_TEMP_DATA,
        GET_ECG_WAVEFORM, SHOW_QR, SET_ALARMS, START_SPORT, DISTANCE, SET_WEATHER, UNKNOWN, GET_VITALS_MEASURE,
        GET_HR_MEASURE, GET_OXY_MEASURE, ECG_MEASUREMENT_HEART_RATE, ECG_HEARTBEAT_PACKET;
    }

    public enum BraceletVitalError {
        ERROR_SENDING_COMMAND, ERROR_MAX_ALARMS, ERROR_MAX_PERIOD, ERROR_HR_SETTING, ERROR_SCREEN_SETTING, ERROR_DIAL_INTERFACE, ERROR_INVALID_TIMES, ERROR_PERIOD_INTERVAL, ERROR_MAX_STEPS
    }


    public enum BraceletVitalGPSStrengh {
         LOW(1), MEDIUM(2), HIGH(3);
        public final int value;

        private BraceletVitalGPSStrengh(int value) {
            this.value = value;
        }
    }


    public static final class WeightScale {
        public static final int TYPE1 = 1, TYPE2 = 2;
    }


    public static final String TAG = "LifeVitSDK";

    // region ---------- Devices

    /**
     * Other devices.
     */
    public static final int DEVICE_OTHERS = -1;

    /**
     * Sphygmomanometer.
     */
    public static final int DEVICE_TENSIOMETER = 0;

    /**
     * Now each Bracelet has its own constant. For AT-500HR, please use {@link #DEVICE_BRACELET_AT500HR} instead.
     */
    @Deprecated
    public static final int DEVICE_BRACELET = 1;

    /**
     * AT-500HR Bracelet.
     */
    public static final int DEVICE_BRACELET_AT500HR = 1;

    /**
     * Oximeter device.
     */
    public static final int DEVICE_OXIMETER = 2;

    /**
     * FH-BPM150 Sphygmomanometer Bracelet.
     */
    public static final int DEVICE_TENSIOBRACELET = 3;

    /**
     * Thermometer device.
     */
    public static final int DEVICE_THERMOMETER = 4;

    /**
     * BL-2000 Weight scale.
     */
    public static final int DEVICE_WEIGHT_SCALE = 5;

    /**
     * AT-250 and AT-260 Bracelets.
     */
    public static final int DEVICE_BRACELET_AT250 = 6;

    /**
     * BT-125 Baby Thermometer Bracelet.
     */
    public static final int DEVICE_BABY_TEMP_BT125 = 7;

    /**
     * AT-250 firmware update State.
     */
    public static final int DEVICE_BRACELET_AT250_FIRMWARE_UPDATER = 8;

    /**
     * AT-250 Pill Reminder.
     */
    public static final int DEVICE_PILL_REMINDER = 9;

    /**
     * VITAL bracelet.
     */
    public static final int DEVICE_BRACELET_VITAL = 10;


    // endregion

    // region ---------- Connection status

    public static final int STATUS_DISCONNECTED = -1;
    public static final int STATUS_SCANNING = 0;
    public static final int STATUS_CONNECTING = 1;
    public static final int STATUS_CONNECTED = 2;

    // endregion


    // Time to find nearest device during connection
    public static final int CONNECT_DELAY = 1000;

    // Check and force the GPS to be enabled in order to scan
    public static final boolean CHECK_GPS = false;

    // Force AT-250Hr Firmware Update (ONLY FOR DEBUG)
    public static final boolean FORCE_FIRMWARE_UPDATE = false;

    public static final int CODE_OK = 0;
    public static final int CODE_UNKNOWN = -1;
    public static final int CODE_LOW_SIGNAL = -2;
    public static final int CODE_NOISE = -3;
    public static final int CODE_INFLATION_TIME = -4;
    public static final int CODE_ABNORMAL_RESULT = -5;
    public static final int CODE_RETRY = -6;
    public static final int CODE_LOW_BATTERY = -7;
    public static final int CODE_BLUETOOTH_DISABLED = -8;
    public static final int CODE_LOCATION_DISABLED = -9;
    public static final int CODE_NOTIFICATION_ACCESS = -10;
    public static final int CODE_LOCATION_TURN_OFF = -11;
    public static final int CODE_NO_RESULTS = -12;
    public static final int CODE_TOO_MUCH_INTERFERENCE = -13;
    public static final int CODE_WRONG_PARAMETERS = -14;

    public static final int DEEP_SLEEP = 0;
    public static final int LIGHT_SLEEP = 1;

    public static final int BRACELET_PARAM_HEIGHT = 0;
    public static final int BRACELET_PARAM_WEIGHT = 1;
    public static final int BRACELET_PARAM_DATE = 2;
    public static final int BRACELET_PARAM_TARGET = 3;
    public static final int BRACELET_PARAM_ACNS = 4;
    public static final int BRACELET_PARAM_HANDS = 5;
    public static final int BRACELET_PARAM_FIND_PHONE = 6;
    public static final int BRACELET_PARAM_HRMONITOR = 7;
    public static final int BRACELET_PARAM_FIND_DEVICE = 8;
    public static final int BRACELET_PARAM_ANTILOST = 9;
    public static final int BRACELET_PARAM_DISTANCE_UNIT = 10;
    public static final int BRACELET_PARAM_SIT = 11;
    public static final int BRACELET_PARAM_ALARM_1 = 12;
    public static final int BRACELET_PARAM_ALARM_2 = 13;
    public static final int BRACELET_PARAM_CAMERA = 14;

    public static final int BRACELET_HAND_NONE = 0;
    public static final int BRACELET_HAND_AUTO = 1;
    public static final int BRACELET_HAND_LEFT = 2;
    public static final int BRACELET_HAND_RIGHT = 3;

    public static final int BRACELET_DISTANCE_MILES = 0;
    public static final int BRACELET_DISTANCE_KM = 1;

    public static final int SNS_TYPE_CALL = 0;
    public static final int SNS_TYPE_SMS = 1;
    public static final int SNS_TYPE_QQ = 2;
    public static final int SNS_TYPE_WECHAT = 3;
    public static final int SNS_TYPE_FACEBOOK = 4;
    public static final int SNS_TYPE_TWITTER = 5;
    public static final int SNS_TYPE_WHATSAPP = 6;
    public static final int SNS_TYPE_INSTAGRAM = 7;
    public static final int SNS_TYPE_EMAIL = 8;
    public static final int SNS_TYPE_LINE = 9;
    public static final int SNS_TYPE_SKYPE = 10;

    public static final int THERMOMETER_ERROR_BODY_TEMPERATURE_HIGH = 0;
    public static final int THERMOMETER_ERROR_BODY_TEMPERATURE_LOW = 1;
    public static final int THERMOMETER_ERROR_AMBIENT_TEMPERATURE_HIGH = 2;
    public static final int THERMOMETER_ERROR_AMBIENT_TEMPERATURE_LOW = 3;
    public static final int THERMOMETER_ERROR_HARDWARE = 4;
    public static final int THERMOMETER_ERROR_LOW_VOLTAGE = 5;


    public static final int THERMOMETERV2_COMMAND_STOP = 0x0029;
    public static final int THERMOMETERV2_COMMAND_SHUTDOWN = 0x0030;
    public static final int THERMOMETERV2_COMMAND_CELSIUS = 0x0031;
    public static final int THERMOMETERV2_COMMAND_FARENHEIT = 0x0032;
    public static final int THERMOMETERV2_COMMAND_LAST_MEASURE = 0x0033;
    public static final int THERMOMETERV2_COMMAND_VERSION_NUMBER = 0x0034;
    //public static final int THERMOMETERV2_COMMAND_HISTORICAL_DATA = 0x0035;

    public static final int TEMPERATURE_UNIT_CELSIUS = 0;
    public static final int TEMPERATURE_UNIT_FAHRENHEIT = 1;

    public static final int THERMOMETER_MODE_ENVIRONMENT = 0;
    public static final int THERMOMETER_MODE_BODY = 1;
    public static final int THERMOMETER_MODE_EAR = 2;
    public static final int THERMOMETER_MODE_FOREHEAD = 3;
    public static final int THERMOMETER_SUCCESS_UNIT = 0xff;
    //public static final int THERMOMETER_SUCCESS_HISTORY = 0xfe;
    public static final int THERMOMETER_SUCCESS_VERSION = 0xfd;
    public static final int THERMOMETER_SUCCESS_SHUTDOWN = 0xfc;

    // Weight units
    public static final int WEIGHT_UNIT_KG = 0;
    public static final int WEIGHT_UNIT_LB = 2;

    public static final int WEIGHT_SCALE_GENDER_MALE = 0;
    public static final int WEIGHT_SCALE_GENDER_FEMALE = 1;

    public static final int GENDER_MALE = 0;
    public static final int GENDER_FEMALE = 1;


    public static final int TENSIOBRACELET_ERROR_HAND_HIGH = 0;
    public static final int TENSIOBRACELET_ERROR_HAND_LOW = 1;
    public static final int TENSIOBRACELET_ERROR_GENERAL = 2;
    public static final int TENSIOBRACELET_ERROR_LOW_POWER = 3;
    public static final int TENSIOBRACELET_ERROR_INCORRECT_POSITION = 4;
    public static final int TENSIOBRACELET_ERROR_BODY_MOVED = 5;
    public static final int TENSIOBRACELET_ERROR_TIGHT_WEARING = 6;
    public static final int TENSIOBRACELET_ERROR_LOOSE_WEARING = 7;
    public static final int TENSIOBRACELET_ERROR_AIR_LEAKAGE = 8;

    public static final int PILLREMINDER_REQUEST_SET_DEVICETIME = 0;
    public static final int PILLREMINDER_REQUEST_GET_DEVICETIME = 1;
    public static final int PILLREMINDER_REQUEST_SET_DEVICETIMEZONE = 2;
    public static final int PILLREMINDER_REQUEST_GET_DEVICETIMEZONE = 3;
    public static final int PILLREMINDER_REQUEST_GET_BATTERYLEVEL = 4;
    public static final int PILLREMINDER_REQUEST_GET_LATESTSYNCHRONIZATIONTIME = 5;
    public static final int PILLREMINDER_REQUEST_SET_SUCCESSFULSYNCHRONIZATIONSTATUS = 6;
    public static final int PILLREMINDER_REQUEST_GET_CLEARSCHEDULEPERFORMANCEHISTORY = 7;
    public static final int PILLREMINDER_REQUEST_SET_ALARMSSCHEDULE = 8;
    public static final int PILLREMINDER_REQUEST_GET_ALARMSCHEDULE = 9;
    public static final int PILLREMINDER_REQUEST_GET_SCHEDULEPERFORMANCEHISTORY = 10;
    public static final int PILLREMINDER_REQUEST_GET_SCHEDULEPERFORMANCEHISTORY_END = 11;
    public static final int PILLREMINDER_REQUEST_SET_ALARMDURATION = 12;
    public static final int PILLREMINDER_REQUEST_SET_ALARMCONFIRMATIONTIME = 13;
    public static final int PILLREMINDER_REQUEST_GET_CLEARALARMSCHEDULE = 14;

    public static final int PILLREMINDER_COLOR_NOT_SET = -1;
    public static final int PILLREMINDER_COLOR_RED = 1;
    public static final int PILLREMINDER_COLOR_GREEN = 2;
    public static final int PILLREMINDER_COLOR_BLUE = 3;
    public static final int PILLREMINDER_COLOR_YELLOW = 4;
    public static final int PILLREMINDER_COLOR_PURPLE = 5;

    public static final int PILLREMINDER_STATUS_TAKEN_IGNORED_FIRST_ALARM = 0;
    public static final int PILLREMINDER_STATUS_TAKEN_IGNORED_SECOND_ALARM = 1;
    public static final int PILLREMINDER_STATUS_TAKEN_RESPONDED_FIRST_ALARM = 2;
    public static final int PILLREMINDER_STATUS_TAKEN_RESPONDED_SECOND_ALARM = 3;


    // region --- Firmware Update ---

    protected static final int DELAY_TO_WAIT_TO_SEND_FIRMWARE_UPDATE = 3000;

    // J055_07_V634_20181025.hex
//    protected static final int AT250_VERSION_1 = 54;
//    protected static final int AT250_VERSION_2 = 51;
//    protected static final int AT250_VERSION_3 = 52;
//    protected static final int AT250_VERSION_4 = 1;

    // J055_07_V638_20190122.hex
    protected static final int AT250_VERSION_1 = 54;
    protected static final int AT250_VERSION_2 = 51;
    protected static final int AT250_VERSION_3 = 56;
    protected static final int AT250_VERSION_4 = 1;

    public static final String AT250_DFU_BROADCAST_ACTION = DfuBaseService.BROADCAST_ACTION_DFU_PROGRESS;

    public static final String AT250_DFU_BROADCAST_EXTRA_STATE = DfuBaseService.BROADCAST_EXTRA_STATE;
    public static final String AT250_DFU_BROADCAST_EXTRA_PROGRESS = DfuBaseService.BROADCAST_EXTRA_PROGRESS;

    public static final int AT250_DFU_STATE_CONNECTING = DfuBaseService.PROGRESS_CONNECTING;
    public static final int AT250_DFU_STATE_STARTING = DfuBaseService.PROGRESS_STARTING;
    public static final int AT250_DFU_STATE_ENABLING_DFU_MODE = DfuBaseService.PROGRESS_ENABLING_DFU_MODE;
    public static final int AT250_DFU_STATE_VALIDATING = DfuBaseService.PROGRESS_VALIDATING;
    public static final int AT250_DFU_STATE_DISCONNECTING = DfuBaseService.PROGRESS_DISCONNECTING;
    public static final int AT250_DFU_STATE_COMPLETED = DfuBaseService.PROGRESS_COMPLETED;
    public static final int AT250_DFU_STATE_ABORTED = DfuBaseService.PROGRESS_ABORTED;

    public static final int AT250_DFU_STATE_ERROR = DfuBaseService.MY_STATE_ERROR;
    public static final int AT250_DFU_STATE_PROGRESS = DfuBaseService.MY_STATE_PROGRESS;

    public static final String AT250_NOTIFICATION_CHANNEL_ID_DFU = DfuBaseService.NOTIFICATION_CHANNEL_ID_DFU;
    public static final int AT250_FOREGROUND_NOTIFICATION_ID = DfuBaseService.FOREGROUND_NOTIFICATION_ID;


    // Sleep types
    protected int SLEEP_TYPE_UNKNOWN = 13;
    protected int SLEEP_TYPE_DEEP_SLEEP = 12;
    protected int SLEEP_TYPE_LIGHT_SLEEP = 11;
    protected int SLEEP_TYPE_SLEEP_BREAK = 1;
    protected int SLEEP_TYPE_SLEEP_NODE = 0;

    // Sport types?
    protected static final int SPORTS_TYPE_RUN = 3;
    protected static final int SPORTS_TYPE_WALK = 2;


    // endregion

}
