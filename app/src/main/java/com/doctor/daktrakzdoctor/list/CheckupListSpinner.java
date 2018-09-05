package com.doctor.daktrakzdoctor.list;

/**
 * Created by amit ji on 7/31/2018.
 */

public class CheckupListSpinner {
    private String type;
    private String payment;

    public CheckupListSpinner(String Type, String Payment){
        this.type=Type;
        this.payment=Payment;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPayment() {
        return payment;
    }

    public void setPayment(String payment) {
        this.payment = payment;
    }

}
