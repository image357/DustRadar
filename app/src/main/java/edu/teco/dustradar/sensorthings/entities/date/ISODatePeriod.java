package edu.teco.dustradar.sensorthings.entities.date;

import android.util.Pair;

import java.io.Serializable;
import java.text.ParseException;

public class ISODatePeriod extends ISODateElement implements Serializable {

    // constructors

    public ISODatePeriod() {
        super();
        super.setEnd(super.getStart());
    }

    public ISODatePeriod(long millisec) {
        super(millisec);
        super.setEnd(millisec);
    }

    public ISODatePeriod(ISODate time) {
        super(time);
        super.setEnd(time);
    }

    public ISODatePeriod(ISODate start, ISODate end) {
        super(start, end);
    }


    // public mehtods

    @Override
    public String getISOString() {
        if (super.getStart() == 0 || super.getEnd() == 0) {
            throw new UnsupportedOperationException("ISODatePeriod must have valid start and end date");
        }

        return super.getISOString();
    }

    @Override
    public void setTime(long millisec) {
        super.setStart(millisec);
        super.setEnd(millisec);
    }


    @Override
    public Pair<ISODate, ISODate> getPeriod() {
        if (super.getStart() == 0 || super.getEnd() == 0) {
            throw new UnsupportedOperationException("ISODatePeriod must have valid start and end date");
        }

        return super.getPeriod();
    }


    // static methods

    public static ISODatePeriod fromString(String string) throws ParseException {
        if (!string.contains("/")) {
            throw new ParseException("String has wrong number of seperators", 0);
        }

        String[] parts = string.split("/");
        if (parts.length != 2) {
            throw new ParseException("String has wrong number of seperators", parts.length);
        }

        ISODate date1 = ISODate.fromString(parts[0]);
        ISODate date2 = ISODate.fromString(parts[1]);
        return (new ISODatePeriod(date1, date2));
    }
}
