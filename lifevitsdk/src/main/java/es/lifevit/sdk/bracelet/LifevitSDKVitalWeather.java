package es.lifevit.sdk.bracelet;

public class LifevitSDKVitalWeather {

    public static class Status {
        public static final int SUNNY = 0;
        public static final int CLEAR_NIGHT = 1;
        public static final int FAIR = 2;
        public static final int FAIR_NIGHT = 3;
        public static final int CLOUDY = 4;
        public static final int PARTLY_CLOUDY = 5;
        public static final int PARTLY_CLOUDY_NIGHT = 6;
        public static final int MOSTLY_CLOUDY = 7;
        public static final int MOSTLY_CLOUDY_NIGHT = 8;
        public static final int OVERCAST = 9;
        public static final int SHOWER = 10;
        public static final int THUNDERSHOWER = 11;
        public static final int THUNDERSHOWER_HAIL = 12;
        public static final int RAIN = 13;
        public static final int MODERATE_RAIN = 14;
        public static final int HEAVY_RAIN = 15;
        public static final int STORM = 16;
        public static final int HEAVY_STORM = 17;
        public static final int SEVERE_STORM = 18;
        public static final int ICE_RAIN = 19;
        public static final int SLEET = 20;
        public static final int SNOW_FLURRY = 21;
        public static final int LIGHT_SNOW = 22;
        public static final int MODERATE_SNOW = 23;
        public static final int HEAVY_SNOW = 24;
        public static final int SNOWSTORM = 25;
        public static final int DUST = 26;
        public static final int SAND = 27;
        public static final int DUSTSTORM = 28;
        public static final int SANDSTORM = 29;
        public static final int FOGGY = 30;
        public static final int HAZE = 31;
        public static final int WINDY = 32;
        public static final int BLUSTERY = 33;
        public static final int HURRICANE = 34;
        public static final int TROPICAL_STORM = 35;
        public static final int TORNADO = 36;
        public static final int COLD = 37;
        public static final int HOT = 38;
    }

    private int status = Status.CLOUDY;
    private int temperature = 20;
    private int maxTemperature = 24;
    private int minTemperature=15;
    private int airQuality=90;
    private String location = "BARCELONA";

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public int getMaxTemperature() {
        return maxTemperature;
    }

    public void setMaxTemperature(int maxTemperature) {
        this.maxTemperature = maxTemperature;
    }

    public int getMinTemperature() {
        return minTemperature;
    }

    public void setMinTemperature(int minTemperature) {
        this.minTemperature = minTemperature;
    }

    public int getAirQuality() {
        return airQuality;
    }

    public void setAirQuality(int airQuality) {
        this.airQuality = airQuality;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "LifevitSDKVitalWeather{" +
                "status=" + status +
                ", temperature=" + temperature +
                ", maxTemperature=" + maxTemperature +
                ", minTemperature=" + minTemperature +
                ", airQuality=" + airQuality +
                ", location='" + location + '\'' +
                '}';
    }
}
