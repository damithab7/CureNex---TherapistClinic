package lk.damithab.curenex.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Reviews {
    private String reviewId;
    private String type;
    private float reviewRate;
    private String reviewText;
    private String typeId;
    private String uid;
}
