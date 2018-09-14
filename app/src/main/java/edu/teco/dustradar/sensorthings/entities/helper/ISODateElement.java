package edu.teco.dustradar.sensorthings.entities.helper;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
        super();

        if (time == null) {
            super.setTime(0);
        }
        else {
            super.setTime(time.getTime());
        }
    }

    public ISODateElement(ISODate start, ISODate end) {
        super();

        if (start == null) {
            super.setTime(0);
        }
        else {
            super.setTime(start.getTime());
        }

        this.end = end;
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
        if (end == null) {
            setEnd(0);
        }
        else {
            setEnd(end.getTime());
        }
    }


    public String getISOString() {
        if (getEnd() == 0) {
            return super.getISOString();
        }

        return (super.getISOString() + "/" + end.getISOString());
    }


    public List<ISODate> getPeriod() {
        if (getStart() == 0 || getEnd() == 0) {
            Log.w(TAG, "ISODateElement is missing either start or end");
            return null;
        }

        List<ISODate> list = new ArrayList<>();
        list.add(this);
        list.add(end);

        return list;
    }

    public void setPeriod(ISODate start, ISODate end) {
        if (start == null) {
            super.setTime(0);
        }
        else {
            super.setTime(start.getTime());
        }

        this.end = end;
    }
}
