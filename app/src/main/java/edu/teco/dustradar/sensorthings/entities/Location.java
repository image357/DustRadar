package edu.teco.dustradar.sensorthings.entities;

import java.io.Serializable;

public class Location extends Entity implements Serializable {

    // constructors

    public Location() {
    }

    public Location(Location old) {
        super(old);
    }

    public Location(String id) {
        super(id);
    }

}
