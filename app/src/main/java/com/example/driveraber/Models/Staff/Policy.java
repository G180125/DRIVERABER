package com.example.driveraber.Models.Staff;

public class Policy {
    private String policy;

    public Policy(){}

    public Policy(String policy) {
        this.policy = policy;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    @Override
    public String toString() {
        return "Policy{" +
                "policy='" + policy + '\'' +
                '}';
    }
}

