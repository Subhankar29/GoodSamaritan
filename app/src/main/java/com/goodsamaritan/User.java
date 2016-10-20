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

    public User(){}
    public User(String uid,String name,String gender,String phone,List<Contacts.ContactItem> contactItemList){
        this.uid=uid;
        this.name=name;
        this.gender=gender;
        this.phone=phone;
        this.contactItemList=contactItemList;
    }

}
