package edu.teco.dustradar.sensorthings.date;

import java.io.Serializable;
import java.text.ParseException;

public class ISODateInstance extends ISODateElement implements Serializable {

    // constructors

    public ISODateInstance() {
        super();
    }

    public ISODateInstance(long millisec) {
        super(millisec);
    }

    public ISODateInstance(ISODateInstance time) {
        super(time);
    }


    // public methods

    @Override
    public String getISOString() {
        if (super.getEnd() != 0) {
            throw new UnsupportedOperationException("ISODateInstance cannot have end date");
        }

        return super.getISOString();
    }


    // static methods

    public static ISODateInstance fromString(String string) throws ParseException {
        if (string.contains("/")) {
            throw new ParseException("String has wrong number of seperators", 1);
        }

        long millisec = millisecFromString(string);
        return (new ISODateInstance(millisec));
    }

}
