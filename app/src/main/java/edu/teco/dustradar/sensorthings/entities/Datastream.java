package edu.teco.dustradar.sensorthings.entities;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import edu.teco.dustradar.sensorthings.entities.date.ISODate;
import edu.teco.dustradar.sensorthings.entities.date.ISODatePeriod;
import edu.teco.dustradar.sensorthings.entities.geojson.GeoPolygon;
import edu.teco.dustradar.sensorthings.entities.helper.UnitOfMeasurement;

public class Datastream extends Entity implements Serializable {

    // private members

    private String name = null;
    private String description = null;
    private UnitOfMeasurement unitOfMeasurement = null;
    private String observationType = null;
    private GeoPolygon observedArea = null;
    private String phenomenonTime = null;
    private String resultTime = null;

    @SerializedName("Thing")
    private Thing thing = null;
    @SerializedName("Sensor")
    private Sensor sensor = null;
    @SerializedName("ObservedProperty")
    private ObservedProperty observedProperty = null;
    private List<Observation> Observations = null;


    // constructors

    public Datastream() {
    }

    public Datastream(Datastream old) {
        super(old);

        this.name = old.getName();
        this.description = old.getDescription();

        if (old.unitOfMeasurement != null) {
            this.unitOfMeasurement = new UnitOfMeasurement(old.getUnitOfMeasurement());
        }

        this.observationType = old.getObservationType();

        if (old.getObservedArea() != null) {
            this.observedArea = new GeoPolygon(old.getObservedArea());
        }

        this.phenomenonTime = old.getPhenomenonTime();
        this.resultTime = old.getResultTime();

        if (old.getThing() != null) {
            this.thing = new Thing(old.getThing());
        }
        if (old.getSensor() != null) {
            this.sensor = new Sensor(old.getSensor());
        }
    }

    public Datastream(String id) {
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


    public UnitOfMeasurement getUnitOfMeasurement() {
        return unitOfMeasurement;
    }

    public void setUnitOfMeasurement(UnitOfMeasurement unitOfMeasurement) {
        this.unitOfMeasurement = unitOfMeasurement;
    }


    public String getObservationType() {
        return observationType;
    }

    public void setObservationType(String observationType) {
        this.observationType = observationType;
    }


    public GeoPolygon getObservedArea() {
        return observedArea;
    }

    public void setObservedArea(GeoPolygon observedArea) {
        this.observedArea = observedArea;
    }


    public String getPhenomenonTime() {
        return phenomenonTime;
    }

    public void setPhenomenonTime(String phenomenonTime) {
        this.phenomenonTime = phenomenonTime;
    }

    public void setPhenomenonTime(ISODatePeriod period) {
        this.phenomenonTime = period.getISOString();
    }

    public void setPhenomenonTime(ISODate start, ISODate end) {
        this.phenomenonTime = (new ISODatePeriod(start, end)).getISOString();
    }


    public String getResultTime() {
        return resultTime;
    }

    public void setResultTime(String resultTime) {
        this.resultTime = resultTime;
    }

    public void setResultTime(ISODatePeriod period) {
        this.resultTime = period.getISOString();
    }

    public void setResultTime(ISODate start, ISODate end) {
        this.resultTime = (new ISODatePeriod(start, end)).getISOString();
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


    public Sensor getSensor() {
        return sensor;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    public void insertSensor(Sensor sensor) {
        setSensor(sensor);
    }

    public void linkSensor(String id) {
        insertSensor(new Sensor(id));
    }


    public ObservedProperty getObservedProperty() {
        return observedProperty;
    }

    public void setObservedProperty(ObservedProperty observedProperty) {
        this.observedProperty = observedProperty;
    }

    public void insertObservedProperty(ObservedProperty observedProperty) {
        setObservedProperty(observedProperty);
    }

    public void linkObservedProperty(String id) {
        insertObservedProperty(new ObservedProperty(id));
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
