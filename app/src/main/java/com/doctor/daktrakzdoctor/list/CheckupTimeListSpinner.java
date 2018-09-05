package com.doctor.daktrakzdoctor.list;

/**
 * Created by amit ji on 8/1/2018.
 */

public class CheckupTimeListSpinner {

    private String timeid;
    private String timetype;

    public CheckupTimeListSpinner(String TimeId, String TimeType){
        this.timeid=TimeId;
        this.timetype=TimeType;
    }

    public String getTimeid() {
        return timeid;
    }

    public void setTimeid(String timeid) {
        this.timeid = timeid;
    }

    public String getTimetype() {
        return timetype;
    }

    public void setTimetype(String timetype) {
        this.timetype = timetype;
    }

}
