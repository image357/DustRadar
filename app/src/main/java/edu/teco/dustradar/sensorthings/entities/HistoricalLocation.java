package edu.teco.dustradar.sensorthings.entities;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import edu.teco.dustradar.sensorthings.entities.date.ISODateInstance;

public class HistoricalLocation extends Entity implements Serializable {

    // private members

    private String time = null;

    @SerializedName("Thing")
    private Thing thing = null;
    private List<Location> Locations = null;


    // constructors

    public HistoricalLocation() {
    }

    public HistoricalLocation(HistoricalLocation old) {
        super(old);

        this.time = old.getTime();

        if (old.getThing() != null) {
            this.thing = new Thing(old.getThing());
        }

        this.Locations = (List<Location>) deepCopy(old.getLocations());
    }

    public HistoricalLocation(String id) {
        super(id);
    }


    // public methods

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setTime(ISODateInstance time) {
        this.time = time.getISOString();
    }


    public Thing getThing() {
        return thing;
    }

    public void setThing(Thing thing) {
        this.thing = thing;
    }

    public void insertThing(Thing thing) {
        setThing(thing);
    }

    public void linkThing(String id) {
        insertThing(new Thing(id));
    }


    public List<Location> getLocations() {
        return Locations;
    }

    public void setLocations(List<Location> locations) {
        Locations = locations;
    }

    public void insertLocation(Location location) {
        if (Locations == null) {
            Locations = new ArrayList<>();
        }

        Locations.add(location);
    }

    public void linkLocation(String id) {
        insertLocation(new Location(id));
    }

}
