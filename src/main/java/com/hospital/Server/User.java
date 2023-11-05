package com.hospital.Server;

public class User {
    private  String user;
    private  String type;

    public User(String user,String type)
    {
        this.type=type;
        this.user=user;
    }
    public String getUser()
    {
        return  this.user;
    }
    public String getType()
    {
        return  this.type;
    }
}

