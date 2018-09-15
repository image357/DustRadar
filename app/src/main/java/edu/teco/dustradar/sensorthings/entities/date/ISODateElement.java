package edu.teco.dustradar.sensorthings.entities.date;

import android.util.Log;
import android.util.Pair;

import java.io.Serializable;
import java.text.ParseException;

public class ISODateElement extends ISODate implements Serializable {

    private static final String TAG = ISODateElement.class.getSimpleName();

    // private members

    private ISODate end = null;


    // constructors

    public ISODateElement() {
        super();
    }

    public ISODateElement(long millisec) {
        super(millisec);
    }

    public ISODateElement(long start, long end) {
        super(start);
        this.end = new ISODate(end);
    }

    public ISODateElement(ISODate time) {
        super(0);

        if (time != null) {
            super.setTime(time.getTime());
        }
    }

    public ISODateElement(ISODate start, ISODate end) {
        super(0);
        end = new ISODate(0);

        if (start != null) {
            super.setTime(start.getTime());
        }

        if (end != null) {
            this.end.setTime(end.getTime());
        }
    }


    // public methods

    public long getStart() {
        return super.getTime();
    }

    public void setStart(long millisec) {
        super.setTime(millisec);
    }

    public void setStart(ISODate start) {
        if (start == null) {
            setStart(0);
        }
        else {
            setStart(start.getTime());
        }
    }


    public long getEnd() {
        if (this.end == null) {
            return 0;
        }

        return this.end.getTime();
    }

    public void setEnd(long millisec) {
        if (this.end == null) {
            this.end = new ISODate(0);
        }

        this.end.setTime(millisec);
    }

    public void setEnd(ISODate end) {
        this.end = new ISODate(0);

        if (end != null) {
            this.end.setTime(end.getTime());
        }
    }


    @Override
    public String getISOString() {
        if (getStart() == 0) {
            throw new UnsupportedOperationException("invalid date");
        }

        if (getEnd() == 0) {
            return super.getISOString();
        }

        return (super.getISOString() + "/" + end.getISOString());
    }


    public Pair<ISODate, ISODate> getPeriod() {
        if (getStart() == 0 || getEnd() == 0) {
            Log.w(TAG, "ISODateElement is missing either start or end date");
            return null;
        }

        ISODate first = new ISODate(getStart());
        ISODate second = new ISODate(getEnd());

        Pair<ISODate, ISODate> pair = new Pair<>(first, second);
        return pair;
    }

    public void setPeriod(long start, long end) {
        setStart(start);
        setEnd(end);
    }

    public void setPeriod(ISODate start, ISODate end) {
        setStart(start);
        setEnd(end);
    }

    public void setPeriod(Pair<ISODate, ISODate> pair) {
        setStart(pair.first);
        setEnd(pair.second);
    }


    // static methods

    public static ISODateElement fromString(String string) throws ParseException {
        if (!string.contains("/")) {
            ISODate date = ISODate.fromString(string);
            return (new ISODateElement(date));
        }

        String[] parts = string.split("/");
        if (parts.length != 2) {
            throw new ParseException("String has wrong number of seperators", parts.length);
        }

        ISODate date1 = ISODate.fromString(parts[0]);
        ISODate date2 = ISODate.fromString(parts[1]);
        return (new ISODateElement(date1, date2));
    }

}
