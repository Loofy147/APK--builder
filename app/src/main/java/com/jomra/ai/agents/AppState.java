package com.jomra.ai.agents;

public class AppState {
    private final long timestamp;
    private final int hourOfDay;
    private final int dayOfWeek;
    private final float batteryLevel;
    private final NetworkType networkType;
    private final String lastAppUsed;
    private final int notificationCount;
    private final LocationCategory locationCategory;

    private AppState(Builder builder) {
        this.timestamp = builder.timestamp;
        this.hourOfDay = builder.hourOfDay;
        this.dayOfWeek = builder.dayOfWeek;
        this.batteryLevel = builder.batteryLevel;
        this.networkType = builder.networkType;
        this.lastAppUsed = builder.lastAppUsed;
        this.notificationCount = builder.notificationCount;
        this.locationCategory = builder.locationCategory;
    }

    public long getTimestamp() { return timestamp; }
    public int getHourOfDay() { return hourOfDay; }
    public int getDayOfWeek() { return dayOfWeek; }
    public float getBatteryLevel() { return batteryLevel; }
    public NetworkType getNetworkType() { return networkType; }
    public String getLastAppUsed() { return lastAppUsed; }
    public int getNotificationCount() { return notificationCount; }
    public LocationCategory getLocationCategory() { return locationCategory; }

    public enum NetworkType {
        WIFI, CELLULAR_5G, CELLULAR_4G, CELLULAR_3G, OFFLINE
    }

    public enum LocationCategory {
        HOME, WORK, COMMUTE, GYM, SHOPPING, UNKNOWN
    }

    public static class Builder {
        private long timestamp = System.currentTimeMillis();
        private int hourOfDay = 0;
        private int dayOfWeek = 0;
        private float batteryLevel = 100f;
        private NetworkType networkType = NetworkType.WIFI;
        private String lastAppUsed = "";
        private int notificationCount = 0;
        private LocationCategory locationCategory = LocationCategory.UNKNOWN;

        public Builder timestamp(long ts) { this.timestamp = ts; return this; }
        public Builder hourOfDay(int hour) { this.hourOfDay = hour; return this; }
        public Builder dayOfWeek(int day) { this.dayOfWeek = day; return this; }
        public Builder batteryLevel(float level) { this.batteryLevel = level; return this; }
        public Builder networkType(NetworkType type) { this.networkType = type; return this; }
        public Builder lastAppUsed(String app) { this.lastAppUsed = app; return this; }
        public Builder notificationCount(int count) { this.notificationCount = count; return this; }
        public Builder locationCategory(LocationCategory cat) { this.locationCategory = cat; return this; }

        public AppState build() {
            return new AppState(this);
        }
    }
}
