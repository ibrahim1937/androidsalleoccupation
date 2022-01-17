package ma.ibrahimchahboune.salleoccupationqr.utils;

import ma.ibrahimchahboune.salleoccupationqr.beans.User;

public class DataHolder {

    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    private static final DataHolder holder = new DataHolder();
    public static DataHolder getInstance() {return holder;}
}
