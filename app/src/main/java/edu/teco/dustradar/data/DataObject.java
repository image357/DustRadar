package edu.teco.dustradar.data;

import android.location.Location;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import edu.teco.dustradar.gps.GPSService;
import edu.teco.dustradar.sensorthings.entities.date.ISODate;

public class DataObject implements Serializable {

    private final static String TAG = DataObject.class.getSimpleName();

    // private members

    private ISODate time;
    private double latitude;
    private double longitude;
    private double height;
    private String data;

    private boolean isvalid;


    // constructors

    public DataObject(String inData) {
        isvalid = true;

        time = new ISODate();

        Location location = GPSService.getLocation();
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            height = location.getAltitude();
        }
        else {
            Log.w(TAG, "No location fix. Cannot create valid data.");
            isvalid = false;
            latitude = 0.0;
            longitude = 0.0;
            height = 0.0;
        }

        if (inData != null) {
            data = inData;
        }
        else {
            Log.w(TAG, "Data is null. Cannot create valid data.");
            isvalid = false;
            data = "";
        }
    }


    public DataObject() {
        this(null);
    }


    // public methods

    public boolean isValid() {
        if (isvalid == false) {
            return false;
        }

        // TODO: check data

        return true;
    }


    public ISODate getTime() {
        return time;
    }


    public double getLatitude() {
        return latitude;
    }


    public double getLongitude() {
        return longitude;
    }


    public double getHeight() {
        return height;
    }


    public String getData() {
        return data;
    }


    // static methods

    public static byte[] serialize(DataObject object) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(object);
        return out.toByteArray();
    }


    public static DataObject deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        ObjectInputStream is = new ObjectInputStream(in);
        return ((DataObject) is.readObject());
    }

}
