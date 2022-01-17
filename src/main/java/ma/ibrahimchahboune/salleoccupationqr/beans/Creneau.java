package ma.ibrahimchahboune.salleoccupationqr.beans;

public class Creneau {

    private String _id;
    private String startTime;
    private String endTime;

    public Creneau(String _id, String startTime, String endTime) {
        this._id = _id;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Creneau() {
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }


    @Override
    public String toString() {
        return startTime + " - " + endTime;
    }
}
