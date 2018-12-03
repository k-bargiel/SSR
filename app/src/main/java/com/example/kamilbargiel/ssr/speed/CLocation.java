package com.example.kamilbargiel.ssr.speed;

import android.location.Location;

public class CLocation extends Location {

    public CLocation(Location location) {
        // TODO Auto-generated constructor stub
        super(location);
    }

    @Override
    public float distanceTo(Location dest) {
        // TODO Auto-generated method stub
        return super.distanceTo(dest);
    }

    @Override
    public float getAccuracy() {
        // TODO Auto-generated method stub
        return super.getAccuracy();
    }

    @Override
    public double getAltitude() {
        // TODO Auto-generated method stub
        return super.getAltitude();
    }

    @Override
    public float getSpeed() {
        // TODO Auto-generated method stub
        return super.getSpeed() * 3.6f;
    }

}