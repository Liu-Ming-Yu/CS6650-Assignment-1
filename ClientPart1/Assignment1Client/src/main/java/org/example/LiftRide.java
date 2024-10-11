package org.example;

public class LiftRide {
    private int skierID;
    private int resortID;
    private int liftID;
    private String seasonID;
    private String dayID;
    private int time;

    public int getSkierID() {
        return skierID;
    }

    public int getResortID() {
        return resortID;
    }

    public int getLiftID() {
        return liftID;
    }

    public String getSeasonID() {
        return seasonID;
    }

    public String getDayID() {
        return dayID;
    }

    public int getTime() {
        return time;
    }

    public void setSkierID(int skierID) {
        this.skierID = skierID;
    }

    public void setResortID(int resortID) {
        this.resortID = resortID;
    }

    public void setLiftID(int liftID) {
        this.liftID = liftID;
    }

    public void setSeasonID(String seasonID) {
        this.seasonID = seasonID;
    }

    public void setDayID(String dayID) {
        this.dayID = dayID;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
