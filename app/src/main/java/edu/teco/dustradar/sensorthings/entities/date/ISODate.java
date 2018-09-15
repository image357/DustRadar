package edu.teco.dustradar.sensorthings.entities.date;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ISODate extends Date implements Serializable {

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


    // static methods

    public static ISODate fromString(String string) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        Date retval = dateFormat.parse(string);

        if (retval == null) {
            return null;
        }

        return (new ISODate(retval.getTime()));
    }

}
