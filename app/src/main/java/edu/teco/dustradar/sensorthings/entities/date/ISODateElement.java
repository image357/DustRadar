package edu.teco.dustradar.sensorthings.entities.date;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ISODateElement extends Date implements Serializable {

    private static final String TAG = ISODateElement.class.getSimpleName();

    // private members

    private Date end = null;


    // constructors

    public ISODateElement() {
        super();
    }

    public ISODateElement(long millisec) {
        super(millisec);
    }

    public ISODateElement(long start, long end) {
        super(start);
        this.end = new Date(end);
    }

    public ISODateElement(ISODateElement time) {
        super(0);
        this.end = new Date(0);

        setTime(time);
    }

    public ISODateElement(ISODateElement start, ISODateElement end) {
        super(0);
        this.end = new Date(0);

        setStart(start);
        setEnd(end);
    }


    // public methods

    public long getStart() {
        return super.getTime();
    }

    public void setStart(long millisec) {
        super.setTime(millisec);
    }

    public void setStart(ISODateElement start) {
        if (start == null) {
            setStart(0);
            return;
        }

        setStart(start.getStart());
    }


    public long getEnd() {
        if (this.end == null) {
            return 0;
        }

        return this.end.getTime();
    }

    public void setEnd(long millisec) {
        if (this.end == null) {
            this.end = new Date(0);
        }

        this.end.setTime(millisec);
    }

    public void setEnd(ISODateElement end) {
        if (end == null) {
            setEnd(0);
            return;
        }

        long millisec = end.getEnd();
        if (millisec == 0) {
            setEnd(end.getStart());
            return;
        }

        setEnd(millisec);
    }


    public void setTime(ISODateElement time) {
        if (time == null) {
            setStart(0);
            setEnd(0);
        }
        else {
            setStart(time.getStart());
            setEnd(time.getEnd());
        }
    }

    public void setTime(long start, long end) {
        setStart(start);
        setEnd(end);
    }

    public void setTime(ISODateElement start, ISODateElement end) {
        setStart(start);
        setEnd(end);
    }


    public String getISOString() {
        if (getStart() == 0) {
            throw new UnsupportedOperationException("invalid date");
        }

        if (getEnd() == 0) {
            return getISOString(this);
        }

        return (getISOString(this) + "/" + getISOString(end));
    }


    // static methods

    public static long millisecFromString(String string) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        Date date = dateFormat.parse(string);

        if (date == null) {
            return 0;
        }

        return date.getTime();
    }

    public static ISODateElement fromString(String string) throws ParseException {
        if (!string.contains("/")) {
            long millisec = millisecFromString(string);
            return (new ISODateElement(millisec));
        }

        String[] parts = string.split("/");
        if (parts.length != 2) {
            throw new ParseException("String has wrong number of seperators", parts.length);
        }

        long millisec1 = millisecFromString(parts[0]);
        long millisec2 = millisecFromString(parts[1]);
        return (new ISODateElement(millisec1, millisec2));
    }


    // protected methods

    protected String getISOString(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        return dateFormat.format(date);
    }

}
