package com.example.driveraber.Models.Staff;

import java.util.List;

public class UserPolicy {
    private List<Policy> respect;
    private List<Policy> experience;
    private List<Policy> account;
    private List<Policy> emergency;

    public UserPolicy(){}

    public UserPolicy(List<Policy> respect, List<Policy> experience, List<Policy> account, List<Policy> emergency) {
        this.respect = respect;
        this.experience = experience;
        this.account = account;
        this.emergency = emergency;
    }

    public List<Policy> getRespect() {
        return respect;
    }

    public void setRespect(List<Policy> respect) {
        this.respect = respect;
    }

    public List<Policy> getExperience() {
        return experience;
    }

    public void setExperience(List<Policy> experience) {
        this.experience = experience;
    }

    public List<Policy> getAccount() {
        return account;
    }

    public void setAccount(List<Policy> account) {
        this.account = account;
    }

    public List<Policy> getEmergency() {
        return emergency;
    }

    public void setEmergency(List<Policy> emergency) {
        this.emergency = emergency;
    }

    @Override
    public String toString() {
        return "UserPolicy{" +
                "respect=" + respect +
                ", experience=" + experience +
                ", account=" + account +
                ", emergency=" + emergency +
                '}';
    }
}

