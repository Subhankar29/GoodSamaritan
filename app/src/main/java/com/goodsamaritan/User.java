package com.goodsamaritan;


import com.goodsamaritan.drawer.contacts.Contacts;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mayank on 19/10/16.
 */

public class User {
    String uid;
    String name;
    String gender;
    String phone;
    List<Contacts.ContactItem> contactItemList=new ArrayList<Contacts.ContactItem>();
    UserLocation location;
    String credit_points;
    String is_online;
    String last_online;
    String isAvailable;
    Password password;

    public User(){} //Required by Firebase database
    public User(String uid,String name,String gender,String phone,List<Contacts.ContactItem> contactItemList,String credit_points,String password){
        this.uid=uid;
        this.name=name;
        this.gender=gender;
        this.phone=phone;
        this.contactItemList=contactItemList;
        this.credit_points=credit_points;
        this.location=new UserLocation();
        this.location.latitude="0";
        this.location.longitude="0";
        this.password= new Password();
        this.password.currentPassword = password;
        this.password.inputPassword = "";
    }

}

class Password{
    String currentPassword;
    String inputPassword;

    public Password(){} //Required by Firebase database

}
