package projcet.neverland.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projcet.neverland.entity.Statistics;
import projcet.neverland.service.StatisticsService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/get")
    public ResponseEntity<?> getStatistics(@RequestParam String userId) {
        try {
            Statistics stat = statisticsService.getStatistics(userId);
            return ResponseEntity.ok(stat);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body("통계 정보 없음");
        }
    }
}
