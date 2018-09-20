package edu.teco.dustradar.sensorthings.date;

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

    public ISODatePeriod(long start, long end) {
        super(start, end);
    }

    public ISODatePeriod(ISODatePeriod time) {
        super(time);
    }

    public ISODatePeriod(ISODateElement start, ISODateElement end) {
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


    // static methods

    public static ISODatePeriod fromString(String string) throws ParseException {
        if (!string.contains("/")) {
            throw new ParseException("String has wrong number of seperators", 0);
        }

        String[] parts = string.split("/");
        if (parts.length != 2) {
            throw new ParseException("String has wrong number of seperators", parts.length);
        }

        long millisec1 = millisecFromString(parts[0]);
        long millisec2 = millisecFromString(parts[1]);
        return (new ISODatePeriod(millisec1, millisec2));
    }
}
