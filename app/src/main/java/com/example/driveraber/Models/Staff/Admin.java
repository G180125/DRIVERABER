package com.example.driveraber.Models.Staff;

public class Admin extends Staff{
    private String documentID;
    private UserPolicy userPolicy;
    private DriverPolicy driverPolicy;

    public Admin(){}

    public Admin(String email, String documentID) {
        super(email);
        this.documentID = documentID;
        this.userPolicy = new UserPolicy();
        this.driverPolicy = new DriverPolicy();
    }

    public String getDocumentID() {
        return documentID;
    }

    public void setDocumentID(String documentID) {
        this.documentID = documentID;
    }

    public UserPolicy getUserPolicy() {
        return userPolicy;
    }

    public void setUserPolicy(UserPolicy userPolicy) {
        this.userPolicy = userPolicy;
    }

    public DriverPolicy getDriverPolicy() {
        return driverPolicy;
    }

    public void setDriverPolicy(DriverPolicy driverPolicy) {
        this.driverPolicy = driverPolicy;
    }
}
