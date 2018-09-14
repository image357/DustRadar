package edu.teco.dustradar.sensorthings.entities.helper;

import java.io.Serializable;
import java.util.List;

public class ISODatePeriod extends ISODateElement implements Serializable {

    // constructors

    public ISODatePeriod() {
        super();
        super.setEnd(super.getStart());
    }

    public ISODatePeriod(long millisec) {
        super(millisec);
        super.setEnd(super.getStart());
    }

    public ISODatePeriod(ISODate time) {
        super(time);
        super.setEnd(super.getStart());
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
        super.setTime(millisec);
        super.setEnd(super.getStart());
    }


    @Override
    public List<ISODate> getPeriod() {
        if (super.getStart() == 0 || super.getEnd() == 0) {
            throw new UnsupportedOperationException("ISODatePeriod must have valid start and end date");
        }

        return super.getPeriod();
    }
}
