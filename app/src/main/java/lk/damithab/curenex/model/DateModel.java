package lk.damithab.curenex.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DateModel {
    private String dayName;
    private String dateNum;
    private int dayOfWeek;   // Firestore 'dayOfWeek' field
    private String fullDate; //  'bookings' for that specific day
    private boolean isPTO;
}