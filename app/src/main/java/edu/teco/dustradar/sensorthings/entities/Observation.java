package edu.teco.dustradar.sensorthings.entities;

import java.io.Serializable;

public class Observation extends Entity implements Serializable {

    // constructors

    public Observation() {
    }

    public Observation(Observation old) {
        super(old);
    }

    public Observation(String id) {
        super(id);
    }

}
