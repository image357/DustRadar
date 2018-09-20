package edu.teco.dustradar.sensorthings;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class ObservedProperty extends Entity implements Serializable {

    // private members

    private String name = null;
    private String description = null;
    private URI definition = null;

    private List<Datastream> Datastreams = null;

    // constructors

    public ObservedProperty() {
    }

    public ObservedProperty(ObservedProperty old) {
        super(old);

        this.name = old.getName();
        this.description = old.getDescription();
        this.definition = (URI) deepCopy(old.getDefinition());

        this.Datastreams = (List<Datastream>) deepCopy(old.getDatastreams());
    }

    public ObservedProperty(String id) {
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


    public URI getDefinition() {
        return definition;
    }

    public void setDefinition(URI definition) {
        this.definition = definition;
    }

    public void setDefinition(String definition) throws URISyntaxException {
        this.definition = new URI(definition);
    }


    public List<Datastream> getDatastreams() {
        return Datastreams;
    }

    public void setDatastreams(List<Datastream> datastreams) {
        this.Datastreams = datastreams;
    }

    public void insertDatastream(Datastream datastream) {
        if (Datastreams == null) {
            Datastreams = new ArrayList<>();
        }

        Datastreams.add(datastream);
    }

    public void linkDatastream(String id) {
        insertDatastream(new Datastream(id));
    }

}
