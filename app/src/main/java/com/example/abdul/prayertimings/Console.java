package com.example.abdul.prayertimings;

import android.util.Log;

public class Console {
    public static final String _prefix = "prayer_timings:";

    public static void log(String str) {
        Log.v(_prefix, str);
    }
}
