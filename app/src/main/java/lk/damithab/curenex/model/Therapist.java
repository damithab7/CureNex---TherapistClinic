package lk.damithab.curenex.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Therapist {
    private String therapistId;
    private String uid;
    private String name;
    private String serviceId;
    private String genderId;
    private String title; /// Dr. Mrs. Ms.
    private String bio;
    private double rate; /// Price per hour
    private float rating; /// Star Rating
    private String therapistImage;
    private String workEmail;
    private String workMobileNo;

}
