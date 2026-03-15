package lk.damithab.curenex.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    private String notificationId;
    private String title;
    private String message;
    private String image;
    private String date;
    private String uid;
}
