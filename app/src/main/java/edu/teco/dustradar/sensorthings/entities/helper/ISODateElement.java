package edu.teco.dustradar.sensorthings.entities.helper;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ISODateElement implements Serializable {

    private static final String TAG = ISODateElement.class.getSimpleName();

    // private members

    private ISODate start = null;
    private ISODate end = null;


    // constructors

    public ISODateElement() {
        this.start = new ISODate();
    }

    public ISODateElement(long millisec) {
        this.start = new ISODate(millisec);
    }

    public ISODateElement(ISODate time) {
        this.start = time;
    }

    public ISODateElement(ISODate start, ISODate end) {
        this.start = start;
        this.end = end;
    }


    // public methods

    public ISODate getStart() {
        return start;
    }

    public void setStart(ISODate start) {
        this.start = start;
    }


    public ISODate getEnd() {
        return end;
    }

    public void setEnd(ISODate end) {
        this.end = end;
    }


    public String getISOString() {
        if (start == null && end == null) {
            throw new UnsupportedOperationException("ISODateElement must contain at least one ISODate");
        }

        String startString = null;
        if (start != null) {
            startString = start.getISOString();
        }

        String endString = null;
        if (end != null) {
            endString = end.getISOString();
        }

        if (start == null) {
            return endString;
        }

        if (end == null) {
            return startString;
        }

        return (startString + "/" + endString);
    }


    public ISODate getTime() {
        if (end != null) {
            Log.w(TAG, "ISODateElement has end date");
        }

        return start;
    }

    public void setTime(ISODate time) {
        if (end != null) {
            Log.w(TAG, "ISODateElement has end date");
        }

        start = time;
        end = null;
    }

    public void setTime(long millisec) {
        setTime(new ISODate(millisec));
    }


    public List<ISODate> getPeriod() {
        if (start == null || end == null) {
            Log.w(TAG, "ISODateElement is missing either start or end");
            return null;
        }

        List<ISODate> list = new ArrayList<>();
        list.add(start);
        list.add(end);

        return list;
    }

    public void setPeriod(ISODate start, ISODate end) {
        this.start = start;
        this.end = end;
    }
}
