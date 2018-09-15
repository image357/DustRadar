package edu.teco.dustradar.sensorthings.entities;

import java.io.Serializable;

public class FeatureOfInterest extends Entity implements Serializable {

    // constructors

    public FeatureOfInterest() {
    }

    public FeatureOfInterest(FeatureOfInterest old) {
        super(old);
    }

    public FeatureOfInterest(String id) {
        super(id);
    }

}
