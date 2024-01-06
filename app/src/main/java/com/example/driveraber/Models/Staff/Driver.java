package com.example.driveraber.Models.Staff;

import com.example.driveraber.Models.Booking.Booking;
import com.example.driveraber.Models.User.Gender;
import com.example.driveraber.Models.User.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Driver extends Staff{
    private String name;
    private String phone;
    private Gender gender;
    private String licenseNumber;
    private Double totalDrive;
    private String avatar;
    private String avatarUploadDate;
    private List<String> title;
    private boolean permission;
    private boolean active;
    private String documentID;
    private String status;
    private List<Booking> bookings;
    private List<User> chattedUser;
    private List<Schedule> scheduleList;

    public Driver(){};

    public Driver(String email, String name, String phone, Gender gender, String licenseNumber, Double totalDrive, String avatar, String avatarUploadDate, List<String> title, boolean permission, boolean active, String documentID) {
        super(email);
        this.name = name;
        this.phone = phone;
        this.gender = gender;
        this.licenseNumber = licenseNumber;
        this.totalDrive = totalDrive;
        this.avatar = avatar;
        this.avatarUploadDate = avatarUploadDate;
        this.title = title;
        this.permission = permission;
        this.active = active;
        this.documentID = documentID;
        this.status = "Register Pending";
        this.bookings = new ArrayList<>();
        this.chattedUser = new ArrayList<>();
        this.scheduleList = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public Double getTotalDrive() {
        return totalDrive;
    }

    public void setTotalDrive(Double totalDrive) {
        this.totalDrive = totalDrive;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getAvatarUploadDate() {
        return avatarUploadDate;
    }

    public void setAvatarUploadDate(String avatarUploadDate) {
        this.avatarUploadDate = avatarUploadDate;
    }

    public List<String> getTitle() {
        return title;
    }

    public void setTitle(List<String> title) {
        this.title = title;
    }

    public boolean isPermission() {
        return permission;
    }

    public void setPermission(boolean permission) {
        this.permission = permission;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getDocumentID() {
        return documentID;
    }

    public void setDocumentID(String documentID) {
        this.documentID = documentID;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }

    public List<User> getChattedUser() {
        return chattedUser;
    }

    public void setChattedUser(List<User> chattedUser) {
        this.chattedUser = chattedUser;
    }

    public List<Schedule> getScheduleList() {
        return scheduleList;
    }

    public void setScheduleList(List<Schedule> scheduleList) {
        this.scheduleList = scheduleList;
    }

    public String isAcceptable(Booking booking, Gender gender){
        if(!gender.equals(this.gender)){
            return "Gender-based Issue";
        }

        Schedule newBookingSchedule = new Schedule(booking.getBookingTime(), booking.getETA(), booking.getBookingDate());

        if (hasScheduleConflict(newBookingSchedule)) {
            return "Schedule Conflict";
        } else {
            if(this.scheduleList == null){
                List<Schedule> list = new ArrayList<>();
                list.add(newBookingSchedule);

                setScheduleList(list);
            } else {
                this.scheduleList.add(newBookingSchedule);
            }
        }

        return "Acceptable";
    }

    private boolean hasScheduleConflict(Schedule newBookingSchedule) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", java.util.Locale.getDefault());

        try {
            Date newBookingStartTime = sdf.parse(newBookingSchedule.getStartTime());
            Date newBookingEndTime = sdf.parse(newBookingSchedule.getEndTIme());

            for (Schedule existingSchedule : scheduleList) {
                Date existingStartTime = sdf.parse(existingSchedule.getStartTime());
                Date existingEndTime = sdf.parse(existingSchedule.getEndTIme());

                if (newBookingStartTime.before(existingEndTime) && newBookingEndTime.after(existingStartTime)) {
                    return true; // Schedule conflict
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return false; // No conflict
    }
}