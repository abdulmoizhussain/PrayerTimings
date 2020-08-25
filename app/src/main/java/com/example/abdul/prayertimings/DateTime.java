package com.example.abdul.prayertimings;

import java.util.Date;

public class DateTime extends Date {

    public String formatDate() {
        return DateFormats.dd.format(this);
    }

    public String formatMonth() {
        return DateFormats.MMMM.format(this);
    }

    public String formatMonth_MM() {
        return DateFormats.MM.format(this);
    }

    public String formatYear() {
        return DateFormats.yyyy.format(this);
    }

    public String formatIn24Hour() {
        return DateFormats.hour24.format(this);
    }
}
