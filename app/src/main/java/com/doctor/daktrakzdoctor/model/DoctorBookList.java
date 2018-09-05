package com.doctor.daktrakzdoctor.model;

import java.util.HashMap;

public class DoctorBookList extends HashMap<String, String> {

public String getId() {
        return id;
        }

public void setId(String id) {
        this.id = id;
        }

public String getDoctorname() {
        return doctorname;
        }

public void setDoctorname(String doctorname) {
        this.doctorname = doctorname;
        }

public String getLat() {
        return lat;
        }

public void setLat(String lat) {
        this.lat = lat;
        }

public String getLag() {
        return lag;
        }

public void setLag(String lag) {
        this.lag = lag;
        }

public String getAddress() {
        return address;
        }

public void setAddress(String address) {
        this.address = address;
        }

public String getQualification() {
        return qualification;
        }

public void setQualification(String qualification) {
        this.qualification = qualification;
        }

public String getSpecification() {
        return specification;
        }

public void setSpecification(String specification) {
        this.specification = specification;
        }

public String getPhone() {
        return phone;
        }

public void setPhone(String phone) {
        this.phone = phone;
        }

        public String getEmailid() {
                return emailid;
        }

        public void setEmailid(String emailid) {
                this.emailid = emailid;
        }

        public String getDoctor_img() {
                return doctor_img;
        }

        public void setDoctor_img(String doctor_img) {
                this.doctor_img = doctor_img;
        }

        String id;
        String doctorname;
        String lat;
        String lag;
        String address;
        String qualification;
        String specification;
        String phone;
        String emailid;
        String doctor_img;
  }