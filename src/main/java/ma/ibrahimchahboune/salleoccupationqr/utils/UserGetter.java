package ma.ibrahimchahboune.salleoccupationqr.utils;

import android.content.SharedPreferences;

import ma.ibrahimchahboune.salleoccupationqr.beans.User;

public class UserGetter {

    private SharedPreferences sharedPreferences;
    private User user;

    public UserGetter(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }


    public User getUser(){
        user.setEmail(sharedPreferences.getString("email", null));
        user.setFirstName(sharedPreferences.getString("firstName", null));
        user.setSecondName(sharedPreferences.getString("secondName", null));
        user.setId(sharedPreferences.getString("id", null));
        return user;
    }
}
