package com.example.abdul.prayertimings;

import java.util.Calendar;
import java.util.Date;

public class CalendarHelper {
    public static Calendar toCalendar(final Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }
}
