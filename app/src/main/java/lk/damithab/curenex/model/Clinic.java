package lk.damithab.curenex.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Clinic {
    private String name;
    private String address;
    private String emergency;
    private String email;
    private String phone;
}
