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
        if (super.getStart() == null || super.getEnd() == null) {
            throw new UnsupportedOperationException("ISODatePeriod must have start and end date");
        }

        return super.getISOString();
    }


    @Override
    public ISODate getTime() {
        throw new UnsupportedOperationException("Cannot use ISODatePeriod as time instance");
    }

    @Override
    public void setTime(ISODate time) {
        super.setStart(time);
        super.setEnd(super.getStart());
    }

    @Override
    public void setTime(long millisec) {
        super.setTime(millisec);
        super.setEnd(super.getStart());
    }


    @Override
    public List<ISODate> getPeriod() {
        if (super.getStart() == null || super.getEnd() == null) {
            throw new UnsupportedOperationException("ISODatePeriod must have start and end date");
        }

        return super.getPeriod();
    }
}
