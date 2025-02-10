package org.barbaris.gmstats.services;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.barbaris.gmstats.models.OnlinePerMap;
import org.barbaris.gmstats.models.OnlinePerTime;
import org.barbaris.gmstats.models.Values;
import org.springframework.stereotype.Component;

@Component
public class Utils {

    public Timestamp stringToTimestamp(String date, int plusDays) {
        String[] dateParts =  date.split("\\.");
        return new Timestamp(Integer.parseInt(dateParts[2]) - 1900, Integer.parseInt(dateParts[1]) - 1, Integer.parseInt(dateParts[0]) + plusDays, 0, 0, 0, 0);
    }

    public List<String> generateTimes() {
        List<String> times = new ArrayList<>();

        for(int hour = 0; hour < 24; hour++) {
            String hourStr = hour < 10 ? "0" + hour : "" + hour;

            for(int minute = 0; minute < 60; minute += 5) {
                String minuteStr = minute < 10 ? "0" + minute : "" + minute;
                times.add(hourStr + ":" + minuteStr);
            }
        }

        return times;
    }

    public int maxNumberIndex(List<Integer> array) {
        int maxNumber = array.getFirst();
        int maxNumberIndex = 0;

        for(int i = 1; i < array.size(); i++) {
            if(array.get(i) > maxNumber) {
                maxNumberIndex = i;
                maxNumber = array.get(i);
            }
        }

        return maxNumberIndex;
    }

    // two perfectly same methods but ima too dumb to come up with solution how to make this one method
    public List<OnlinePerMap> sortOpm(List<OnlinePerMap> list) {
        List<OnlinePerMap> sorted = new ArrayList<>();

        for(int i = 0; i < list.size() - 1; i++) {
            OnlinePerMap max = new OnlinePerMap(Values.DEFAULT, 0, 0);

            for(OnlinePerMap el : list) {
                if(el.online() > max.online()) {
                    max = el;
                }
            }

            list.remove(max);
            sorted.add(max);
        }

        return sorted;
    }

    public List<OnlinePerTime> sortOpt(List<OnlinePerTime> list) {
        List<OnlinePerTime> sorted = new ArrayList<>();

        for(int i = 0; i < list.size() - 1; i++) {
            OnlinePerTime max = new OnlinePerTime(Values.DEFAULT, 0);

            for(OnlinePerTime el : list) {
                if(el.online() > max.online()) {
                    max = el;
                }
            }

            list.remove(max);
            sorted.add(max);
        }

        return sorted;
    }

    public String recordsToTime(int records) {
        int minutes = records * Values.MINUTES_A_RECORD;
        if(minutes < Values.MINUTES_A_HOUR) {
            return String.format("%d минут", minutes);
        }

        int hours = minutes / Values.MINUTES_A_HOUR;
        minutes %= Values.MINUTES_A_HOUR;
        if(hours < Values.HOURS_A_DAY) {
            return switch (hours) {
                case 1 -> String.format("%d час %d минут", hours, minutes);
                case 2, 3, 4 -> String.format("%d часа %d минут", hours, minutes);
                default -> String.format("%d часов %d минут", hours, minutes);
            };
        }

        int days = hours / Values.HOURS_A_DAY;
        hours %= Values.HOURS_A_DAY;

        String time;
        switch (hours) {
            case 1 -> time = String.format("%d час %d минут", hours, minutes);
            case 2, 3, 4 -> time = String.format("%d часа %d минут", hours, minutes);
            default -> time = String.format("%d часов %d минут", hours, minutes);
        }

        return switch (days) {
            case 1 -> String.format("%d день ", days) + time;
            case 2, 3, 4 -> String.format("%d дня ", days) + time;
            default -> String.format("%d дней ", days) + time;
        };
    }

    public Timestamp findEarliest(List<Timestamp> times) {
        Timestamp earliest = times.getFirst();

        for(Timestamp time : times) {
            if(time.before(earliest)) {
                earliest = time;
            }
        }

        return earliest;
    }

    public Timestamp findLatest(List<Timestamp> times) {
        Timestamp latest = times.getFirst();

        for(Timestamp time : times) {
            if(time.after(latest)) {
                latest = time;
            }
        }

        return latest;
    }
}












