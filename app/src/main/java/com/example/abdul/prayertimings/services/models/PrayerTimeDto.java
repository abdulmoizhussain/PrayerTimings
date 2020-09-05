package com.example.abdul.prayertimings.services.models;

public class PrayerTimeDto {
    public PrayerTimeDto(
            String city,
            int month,
            int date,
            String fajr,
            String sunrise,
            String zawal,
            String zuhur,
            String asr,
            String maghrib,
            String isha
    ) {
        this.city = city;
        this.month = month;
        this.date = date;
        this.fajr = fajr;
        this.sunrise = sunrise;
        this.zawal = zawal;
        this.zuhur = zuhur;
        this.asr = asr;
        this.maghrib = maghrib;
        this.isha = isha;
    }

    public String city;
    public int month;
    public int date;
    public String fajr;
    public String sunrise;
    public String zawal;
    public String zuhur;
    public String asr;
    public String maghrib;
    public String isha;
}
