package com.example.driveraber.Models.Staff;

import java.util.List;

public class DriverPolicy {
    private List<Policy> documents;
    private List<Policy> respect;
    private List<Policy> law;
    private List<Policy> vehicle;
    private List<Policy> practice;

    public DriverPolicy(){}

    public DriverPolicy(List<Policy> documents, List<Policy> respect, List<Policy> law, List<Policy> vehicle, List<Policy> practice) {
        this.documents = documents;
        this.respect = respect;
        this.law = law;
        this.vehicle = vehicle;
        this.practice = practice;
    }

    public List<Policy> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Policy> documents) {
        this.documents = documents;
    }

    public List<Policy> getRespect() {
        return respect;
    }

    public void setRespect(List<Policy> respect) {
        this.respect = respect;
    }

    public List<Policy> getLaw() {
        return law;
    }

    public void setLaw(List<Policy> law) {
        this.law = law;
    }

    public List<Policy> getVehicle() {
        return vehicle;
    }

    public void setVehicle(List<Policy> vehicle) {
        this.vehicle = vehicle;
    }

    public List<Policy> getPractice() {
        return practice;
    }

    public void setPractice(List<Policy> practice) {
        this.practice = practice;
    }
}
