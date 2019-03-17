package com.lobxy.societyentrancesystem.Model;

public class User {
    public User() {
    }

    private String name, email, password, contact, uid, flat, block, qrImageURL;

    public User(String uid, String name, String email, String password, String contact, String flat, String block, String qrImageURL) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.contact = contact;
        this.uid = uid;
        this.flat = flat;
        this.block = block;
        this.qrImageURL = qrImageURL;
    }

    public String getQrImageURL() {
        return qrImageURL;
    }

    public void setQrImageURL(String qrImageURL) {
        this.qrImageURL = qrImageURL;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFlat() {
        return flat;
    }

    public void setFlat(String flat) {
        this.flat = flat;
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }
}
