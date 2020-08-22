package com.example.abdul.prayertimings;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class DateFormats {
    public static SimpleDateFormat hour24 = new SimpleDateFormat("HH:mm", Locale.getDefault());
    public static SimpleDateFormat hour12 = new SimpleDateFormat("hh:mm a", Locale.getDefault());
    public static SimpleDateFormat dd = new SimpleDateFormat("dd", Locale.getDefault());
    public static SimpleDateFormat MMMM = new SimpleDateFormat("MMMM", Locale.getDefault());
    public static SimpleDateFormat MM = new SimpleDateFormat("MM", Locale.getDefault());
    public static SimpleDateFormat yyyy = new SimpleDateFormat("yyyy", Locale.getDefault());
}
