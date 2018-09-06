package edu.teco.dustradar.data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ISODate extends Date {

    // constructors

    public ISODate() {
        super();
    }


    public ISODate(long millisec) {
        super(millisec);
    }


    // public methods

    public String getISOString() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(this);
    }

}
