package com.sftelehealth.doctor.domain;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Domain {
    public static String changeDateFormat(String date) {
        SimpleDateFormat datePickerFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SimpleDateFormat myFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        try {
            Date dateFromUser = datePickerFormat.parse(date);
            return myFormat.format(dateFromUser);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return "0";
    }

    public static String getTime(String dateStr){
        if (dateStr != null && !dateStr.isEmpty()){
            Date date;
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a");
                date = inputFormat.parse(dateStr);
                if (date != null) {
                    outputFormat.setTimeZone(TimeZone.getTimeZone("UTC+6"));
                    return outputFormat.format(date);
                } else {
                    return "";
                }
            } catch (ParseException e) {
                e.printStackTrace();
                return "";
            }
        } else {
            return "";
        }
    }
}
