package ma.ibrahimchahboune.salleoccupationqr.beans;

import com.google.gson.annotations.SerializedName;

public class Bloc {
    @SerializedName("_id")
    private String _id;
    @SerializedName("name")
    private String name;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
