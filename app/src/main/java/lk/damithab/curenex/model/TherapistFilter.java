package lk.damithab.curenex.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TherapistFilter {
    private double startPrice;
    private double endPrice;
    private String serviceId;
    private String genderId;
}
