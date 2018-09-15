package edu.teco.dustradar.sensorthings.entities;

import java.io.Serializable;

public class HistoricalLocation extends Entity implements Serializable {

    // constructors

    public HistoricalLocation() {
    }

    public HistoricalLocation(HistoricalLocation old) {
        super(old);
    }

    public HistoricalLocation(String id) {
        super(id);
    }

}
