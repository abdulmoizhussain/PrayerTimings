package com.example.abdul.prayertimings;

import java.util.Calendar;
import java.util.Date;

// source: Overriding deprecated methods reason: https://stackoverflow.com/a/36839096
// source: addDays https://github.com/abdulmoizhussain/javascript-helpers/blob/master/DateExtended.js

public class DateTime extends Date {
    private Calendar calendar;

    public DateTime() {
        super();
        initCalendar();
    }

    public DateTime(long date) {
        super(date);
        initCalendar();
    }

    private void initCalendar() {
        calendar = Calendar.getInstance();
        calendar.setTime(this);
    }

    public int getMinuteOfHour() {
        return calendar.get(Calendar.MINUTE);
    }

    public int getHourOfDay() {
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    public int getDayOfMonth() {
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * @return integer value of this month, where January is 1.
     */
    public int getMonthOfYear() {
        return calendar.get(Calendar.MONTH) + 1;
    }

    /**
     * Adds/Subtracts given number of days in this instance and returns a new instance.
     *
     * @param daysToAdd - Positive or Negative number of days
     * @return {DateTime} - Instance of added/subtracted day.
     */
    public DateTime addDays(int daysToAdd) {
        // 24 * 60 * 60 * 1000 = 86400000
        return new DateTime(this.getTime() + (daysToAdd * 86400000));
    }

    /**
     * @return {Number} Total number of passed seconds up-till now, expressed in whole and fractional number.
     */
    public long totalSeconds() {
        return this.getTime() / 1000L;
    }

    /**
     * @return {Number} Total number of passed minutes up-till now, expressed in whole and fractional number.
     */
    public long totalMinutes() {
        return this.totalSeconds() / 60L;
    }

    /**
     * @return {Number} Total number of passed hours up-till now, expressed in whole and fractional number.
     */
    public long totalHours() {
        return this.totalMinutes() / 60L;
    }

    public final String formatDate() {
        return DateFormats.dd.format(this);
    }

    public String formatMonth() {
        return DateFormats.MMMM.format(this);
    }

    public final String formatMonth_MM() {
        return DateFormats.MM.format(this);
    }

    public final String formatYear() {
        return DateFormats.yyyy.format(this);
    }

    public final String formatIn24Hour() {
        return DateFormats.hour24.format(this);
    }
}
