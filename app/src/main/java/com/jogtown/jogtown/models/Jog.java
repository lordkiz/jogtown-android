package com.jogtown.jogtown.models;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Delete;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.PrimaryKey;
import androidx.room.Query;

@Entity(tableName = "jogs")
public class Jog {

    @PrimaryKey
    public int id;
    public int userId;
    public String name;

    public int distance;
    public int duration;
    public float calories;
    public float averageSpeed;
    public int averagePace;
    public float startLatitude;
    public float endLatitude;
    public float startLongitude;
    public float endLongitude;
    public String paces; //Should be a list, but using String to save to DB
    public String speeds; //Should be a list, but using String to save to DB
    public String coordinates; //Should be a list, but using String to save to DB
    public float hydration;
    public float maxSpeed;
    public int maxPace;
    public float minAltitude;
    public float maxAltitude;
    public int totalAscent;
    public int totalDescent;

    public Jog() {

    }

    public static class Builder {
        public int userId;
        public String name;
        public int distance;
        public int duration;
        public float calories;
        public float averageSpeed;
        public int averagePace;
        public float startLatitude;
        public float endLatitude;
        public float startLongitude;
        public float endLongitude;
        public String paces; //Should be a list, but using String to save to DB
        public String speeds; //Should be a list, but using String to save to DB
        public String coordinates; //Should be a list, but using String to save to DB
        public float hydration;
        public float maxSpeed;
        public int maxPace;
        public float minAltitude;
        public float maxAltitude;
        public int totalAscent;
        public int totalDescent;


        public Builder addUserId(int userId) {
            this.userId = userId;
            return this;
        }

        public Builder addName(String name) {
            this.name = name;
            return this;
        }

        public Builder addDistance(int distance) {
            this.distance = distance;
            return this;
        }

        public Builder addDuration(int duration) {
            this.duration = duration;
            return this;
        }

        public Builder addCalories(float calories) {
            this.calories = calories;
            return this;
        }

        public Builder addAverageSpeed(float avSpeed) {
            this.averageSpeed = avSpeed;
            return this;
        }

        public Builder addAveragePace(int avPace) {
            this.averagePace = avPace;
            return this;
        }

        public Builder addMaxPace(int maxPace) {
            this.maxPace = maxPace;
            return this;
        }

        public Builder addMaxSpeed(float maxSpeed) {
            this.maxSpeed = maxSpeed;
            return this;
        }

        public Builder addPaces(String paces) {
            this.paces = paces;
            return this;
        }

        public Builder addSpeeds(String speeds) {
            this.speeds = speeds;
            return this;
        }

        public Builder addCoordinates(String coordinates) {
            this.coordinates = coordinates;
            return this;
        }

        public Builder addHydration(float hydration) {
            this.hydration = hydration;
            return this;
        }

        public Builder addMinAltitude(float minAltitude) {
            this.minAltitude = minAltitude;
            return this;
        }

        public Builder addMaxAltitude(float addMaxAltitude) {
            this.maxAltitude = addMaxAltitude;
            return this;
        }

        public Builder addTotalAscent(int totalAscent) {
            this.totalAscent = totalAscent;
            return this;
        }
        public Builder addTotalDescent(int totalDescent) {
            this.totalDescent = totalDescent;
            return this;
        }

        public Builder addStartLatitude(float startLatitude) {
            this.startLatitude = startLatitude;
            return this;
        }

        public Builder addEndLatitude(float endLatitude) {
            this.endLatitude = endLatitude;
            return this;
        }

        public Builder addStartLongitude(float startLongitude) {
            this.startLongitude = startLongitude;
            return this;
        }

        public Builder addEndLongitude(float endLongitude) {
            this.endLongitude = endLongitude;
            return this;
        }


        public Jog build () {
            Jog jogsData = new Jog();
            jogsData.id = (int) Math.round(Math.random() * 10);
            jogsData.userId = this.userId;
            jogsData.name = this.name;
            jogsData.distance = this.distance;
            jogsData.duration = this.duration;
            jogsData.calories = this.calories;
            jogsData.averageSpeed = this.averageSpeed;
            jogsData.averagePace = this.averagePace;
            jogsData.startLatitude = this.startLatitude;
            jogsData.endLatitude = this.endLatitude;
            jogsData.startLongitude = this.startLongitude;
            jogsData.endLongitude = this.endLongitude;
            jogsData.paces = this.paces;
            jogsData.speeds = this.speeds;
            jogsData.coordinates = this.coordinates;
            jogsData.hydration = this.hydration;
            jogsData.maxSpeed = this.maxSpeed;
            jogsData.maxPace = this.maxPace;
            jogsData.minAltitude = this.minAltitude;
            jogsData.maxAltitude = this.maxAltitude;
            jogsData.totalAscent = this.totalAscent;
            jogsData.totalDescent = this.totalDescent;

            return jogsData;
        }
    }


}
