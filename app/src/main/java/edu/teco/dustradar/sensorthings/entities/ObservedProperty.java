package edu.teco.dustradar.sensorthings.entities;

import java.io.Serializable;

public class ObservedProperty extends Entity implements Serializable {

    // constructors

    public ObservedProperty() {
    }

    public ObservedProperty(ObservedProperty old) {
        super(old);
    }

    public ObservedProperty(String id) {
        super(id);
    }

}
