package com.thomasmore.ezgreen.model;

/**
 * Created by Tomas-Laptop on 24/04/2017.
 */

public class User {
    private String user_id;
    private String first_name;
    private String last_name;
    private String email;
    private String profile_picture_url;
    private String password;
    private String newPassword;
    private String token;

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setProfile_picture_url(String profile_picture_url) {
        this.profile_picture_url = profile_picture_url;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public String getProfile_picture_url() {
        return profile_picture_url;
    }

    public String getEmail() {
        return email;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
