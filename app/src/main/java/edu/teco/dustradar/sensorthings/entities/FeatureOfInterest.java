package edu.teco.dustradar.sensorthings.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FeatureOfInterest extends Entity implements Serializable {

    // private members

    private String name = null;
    private String description = null;
    private String encodingType = null;
    private Object feature = null;

    private List<Observation> Observations = null;


    // constructors

    public FeatureOfInterest() {
    }

    public FeatureOfInterest(FeatureOfInterest old) {
        super(old);

        this.name = old.getName();
        this.description = old.getDescription();
        this.encodingType = old.getEncodingType();
        this.feature = deepCopy(old.getFeature());

        this.Observations = (List<Observation>) deepCopy(old.getObservations());
    }

    public FeatureOfInterest(String id) {
        super(id);
    }


    // public methods

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getEncodingType() {
        return encodingType;
    }

    public void setEncodingType(String encodingType) {
        this.encodingType = encodingType;
    }

    public void setEncodingTypeGeoJson() {
        setEncodingType("application/vnd.geo+json");
    }


    public Object getFeature() {
        return feature;
    }

    public void setFeature(Object feature) {
        this.feature = feature;
    }


    public List<Observation> getObservations() {
        return Observations;
    }

    public void setObservations(List<Observation> observations) {
        Observations = observations;
    }

    public void insertObservation(Observation observation) {
        if (Observations == null) {
            Observations = new ArrayList<>();
        }

        Observations.add(observation);
    }

    public void linkObservation(String id) {
        insertObservation(new Observation(id));
    }

}
