package com.example.abdul.prayertimings;

import java.util.Date;

public class DateTime extends Date {

    public DateTime() {
        super();
    }

    public DateTime(long date) {
        super(date);
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
