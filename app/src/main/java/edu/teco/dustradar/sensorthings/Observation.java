package edu.teco.dustradar.sensorthings;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Map;

import edu.teco.dustradar.sensorthings.date.ISODateElement;
import edu.teco.dustradar.sensorthings.date.ISODateInstance;
import edu.teco.dustradar.sensorthings.date.ISODatePeriod;

public class Observation extends Entity implements Serializable {

    // private members

    private Object result = null;
    private String phenomenonTime = null;
    private String resultTime = null;
    private String validTime = null;
    private Object resultQuality = null;
    private Map<String, Object> parameters = null;

    @SerializedName("Datastream")
    private Datastream datastream = null;
    @SerializedName("FeatureOfInterest")
    private FeatureOfInterest featureOfInterest = null;


    // constructors

    public Observation() {
    }

    public Observation(Observation old) {
        super(old);

        this.result = deepCopy(old.getResult());
        this.phenomenonTime = old.getPhenomenonTime();
        this.resultTime = old.getResultTime();
        this.validTime = old.getValidTime();
        this.resultQuality = deepCopy(old.getResultQuality());
        this.parameters = (Map<String, Object>) deepCopy(old.getParameters());

        if (old.getDatastream() != null) {
            this.datastream = new Datastream(old.getDatastream());
        }

        if (old.getFeatureOfInterest() != null) {
            this.featureOfInterest = new FeatureOfInterest(old.getFeatureOfInterest());
        }
    }

    public Observation(String id) {
        super(id);
    }


    // public methods


    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }


    public String getPhenomenonTime() {
        return phenomenonTime;
    }

    public void setPhenomenonTime(String phenomenonTime) {
        this.phenomenonTime = phenomenonTime;
    }

    public void setPhenomenonTime(ISODateElement date) {
        this.phenomenonTime = date.getISOString();
    }


    public String getResultTime() {
        return resultTime;
    }

    public void setResultTime(String resultTime) {
        this.resultTime = resultTime;
    }

    public void setResultTime(ISODateInstance date) {
        this.resultTime = date.getISOString();
    }


    public String getValidTime() {
        return validTime;
    }

    public void setValidTime(String validTime) {
        this.validTime = validTime;
    }

    public void setValidTime(ISODatePeriod period) {
        this.validTime = period.getISOString();
    }


    public Object getResultQuality() {
        return resultQuality;
    }

    public void setResultQuality(Object resultQuality) {
        this.resultQuality = resultQuality;
    }


    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }


    public Datastream getDatastream() {
        return datastream;
    }

    public void setDatastream(Datastream datastream) {
        this.datastream = datastream;
    }

    public void insertDatastream(Datastream datastream) {
        setDatastream(datastream);
    }

    public void linkDatastream(String id) {
        insertDatastream(new Datastream(id));
    }


    public FeatureOfInterest getFeatureOfInterest() {
        return featureOfInterest;
    }

    public void setFeatureOfInterest(FeatureOfInterest featureOfInterest) {
        this.featureOfInterest = featureOfInterest;
    }

    public void insertFeatureOfInterest(FeatureOfInterest featureOfInterest) {
        setFeatureOfInterest(featureOfInterest);
    }

    public void linkFeatureOfInterest(String id) {
        insertFeatureOfInterest(new FeatureOfInterest(id));
    }

}
