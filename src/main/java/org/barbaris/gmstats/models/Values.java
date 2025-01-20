package org.barbaris.gmstats.models;

public class Values {
    public static final String BAD_ID = "bad_id";
    public static final String DEFAULT_ADDRESS = "0.0.0.0:0000";
    public static final String DEFAULT_CRITERIA = "default";
    public static final String STEAM_WORKSHOP_LINK = "https://steamcommunity.com/workshop/browse/?appid=4000&searchtext=";

    public static final short MINUTES_A_RECORD = 5;
    public static final short MINUTES_A_HOUR = 60;
    public static final short HOURS_A_DAY = 24;
    public static final float RECORDS_A_DAY = 288f; // 60 minutes in an hour : 5 minutes between records * 24 hours in a day
    public static final long MILLISECONDS_A_DAY = 86400000;

    public static final String[] WEEKLY_DATA_MODEL_NAMES = {"mondayData", "tuesdayData", "wednesdayData", "thursdayData", "fridayData", "saturdayData", "sundayData"};
}
