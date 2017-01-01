package com.example.yaish8.shiftorginizer.Utils.user;

import java.io.Serializable;

/**
 * Created by Nimrod
 */

public class User implements Serializable {
    public static String firstName,lastName,userName,password,id,firstConnection,email,company,permission,ownerId,address,birthDay,phoneNumber;
    public User(String firstName, String lastName, String userName, String password, String id, String firstConnection, String email, String company, String permission, String ownerId,String address,String birthDay,String phoneNumber){
        this.firstName = firstName;
        this.lastName = lastName;
        this.userName = userName;
        this.password = password;
        this.id = id;
        this.firstConnection = firstConnection;
        this.email = email;
        this.company = company;
        this.permission = permission;
        this.ownerId = ownerId;
        this.address=address;
        this.birthDay=birthDay;
        this.phoneNumber=phoneNumber;
    }
}
