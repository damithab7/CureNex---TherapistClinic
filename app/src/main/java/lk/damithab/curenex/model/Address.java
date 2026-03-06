package lk.damithab.curenex.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Address implements Serializable {
    private String addressId;
    private String uid;
    private String name;
    private String email;
    private String contact;
    private String address1;
    private String address2;
    private String city;
    private String postcode;
}
