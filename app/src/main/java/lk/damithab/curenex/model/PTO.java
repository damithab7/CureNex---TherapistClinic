package lk.damithab.curenex.model;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PTO {
    private PTODate date;
    private String ptoId;
    private String reason;
    private PTOTime time;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PTODate {
        private String endDate;
        private String startDate;
        private String type;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PTOTime {
        private String endTime;
        private String startTime;
        private String type;
    }
}
