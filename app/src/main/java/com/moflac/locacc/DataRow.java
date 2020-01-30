package com.moflac.locacc;

// class to store one row of collected data

public class DataRow {
    private String time;
    private Double latitude;
    private Double longitude;
    private Float bearing;
    private Float speed;
    private Float accuracy;
    private Double altitude;
    private Float x;
    private Float y;
    private Float z;

    public String getTime()
    {
        return time;
    }
    public Double getLatitude()
    {
        return latitude;
    }
    public Double getLongitude()
    {
        return longitude;
    }
    public Float getBearing()
    {
        return bearing;
    }
    public Float getSpeed()
    {
        return speed;
    }
    public Float getAccuracy()
    {
        return accuracy;
    }
    public Double getAltitude()
    {
        return altitude;
    }
    public Float getX()
    {
        return x;
    }
    public Float getY()
    {
        return y;
    }
    public Float getZ()
    {
        return z;
    }

    public void setTime(String iTime)
    {
        time = iTime;
    }
    public void setLatitude(Double iLatitude)
    {
        latitude = iLatitude;
    }
    public void setLongitude(Double iLongitude)
    {
        longitude = iLongitude;
    }
    public void setBearing(Float iBearing)
    {
        bearing = iBearing;
    }
    public void setSpeed(Float iSpeed)
    {
        speed = iSpeed;
    }
    public void setAccuracy(Float iAccuracy)
    {
        accuracy = iAccuracy;
    }
    public void setAltitude(Double iAltitude)
    {
        altitude = iAltitude;
    }
    public void setX(Float iX)
    {
        x = iX;
    }
    public void setY(Float iY)
    {
        y = iY;
    }
    public void setZ(Float iZ)
    {
        z = iZ;
    }
}
