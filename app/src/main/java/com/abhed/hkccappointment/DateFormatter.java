package com.abhed.hkccappointment;

import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.Date;

public class DateFormatter {
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static String formatDayDate(Calendar cal) {
        int day = cal.get(Calendar.DATE);
        Date date = cal.getTime();
        if (!((day > 10) && (day < 19)))
            switch (day % 10) {
                case 1:
                    return new SimpleDateFormat("EEEE, d'st' 'of' MMMM yyyy").format(date);
                case 2:
                    return new SimpleDateFormat("EEEE, d'nd' 'of' MMMM yyyy").format(date);
                case 3:
                    return new SimpleDateFormat("EEEE, d'rd' 'of' MMMM yyyy").format(date);
                default:
                    return new SimpleDateFormat("EEEE, d'th' 'of' MMMM yyyy").format(date);
            }
        return new SimpleDateFormat("EEEE, d'th' 'of' MMMM yyyy").format(date);

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static String formatDayDateTime(Calendar cal) {
        SimpleDateFormat format = new SimpleDateFormat("EEEE MMM dd yyyy HH:mm");
        Date dt = cal.getTime();
        String dateString = format.format(dt);
        return dateString;
        // Date   date       = format.parse ( dt );

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static String formatTime(Calendar cal) {
        SimpleDateFormat format = new SimpleDateFormat("hh:mm a");
        Date dt = cal.getTime();
        String dateString = format.format(dt);
        return dateString;
        // Date   date       = format.parse ( dt );

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static String formatDate(Calendar cal) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date dt = cal.getTime();
        String dateString = format.format(dt);
        return dateString;
        // Date   date       = format.parse ( dt );

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static int formatDateAsId(Calendar cal) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        Date dt = cal.getTime();
        String dateString = format.format(dt);
        return Integer.parseInt(dateString);

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static String formatDateTime(Calendar cal) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date dt = cal.getTime();
        String dateString = format.format(dt);
        return dateString;
        // Date   date       = format.parse ( dt );

    }


}
