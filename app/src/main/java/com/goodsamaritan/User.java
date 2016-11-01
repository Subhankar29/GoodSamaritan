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

    public User(){} //Required by Firebase database
    public User(String uid,String name,String gender,String phone,List<Contacts.ContactItem> contactItemList,String credit_points){
        this.uid=uid;
        this.name=name;
        this.gender=gender;
        this.phone=phone;
        this.contactItemList=contactItemList;
        this.credit_points=credit_points;
        this.location=new UserLocation();
        this.location.latitude="0";
        this.location.longitude="0";
    }

}
