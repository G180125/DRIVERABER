package com.example.driveraber.Models.Notification;

public class InAppNotification {
    private String date;
    private String title;
    private String body;
    private String user;
    private String bookingID;
    private boolean isRead;

    public InAppNotification() {
    }

    public InAppNotification(String date, String title, String user,String body, String bookingID) {
        this.date = date;
        this.title = title;
        this.user = user;
        this.body = body;
        this.bookingID = bookingID;
        this.isRead = false;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getBookingID() {
        return bookingID;
    }

    public void setBookingID(String bookingID) {
        this.bookingID = bookingID;
    }

    public boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }

    @Override
    public String toString() {
        return "InAppNotification{" +
                "date='" + date + '\'' +
                ", title='" + title + '\'' +
                ", body='" + body + '\'' +
                ", user='" + user + '\'' +
                ", bookingID='" + bookingID + '\'' +
                ", isRead=" + isRead +
                '}';
    }
}