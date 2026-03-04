package net.jetluna.api.report;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ReportManager {

    public static class Report {
        public final UUID id;
        public final String sender;
        public final String target;
        public final String reason;
        public final long timestamp;

        public Report(String sender, String target, String reason) {
            this.id = UUID.randomUUID();
            this.sender = sender;
            this.target = target;
            this.reason = reason;
            this.timestamp = System.currentTimeMillis();
        }
    }

    private static final List<Report> activeReports = new ArrayList<>();

    public static void addReport(String sender, String target, String reason) {
        activeReports.add(new Report(sender, target, reason));
    }

    // НОВОЕ: Получить список уникальных ников, на которых поданы жалобы
    public static List<String> getReportedPlayers() {
        return activeReports.stream()
                .map(r -> r.target)
                .distinct() // Убираем дубликаты
                .collect(Collectors.toList());
    }

    // НОВОЕ: Получить все жалобы на конкретного игрока
    public static List<Report> getReportsFor(String target) {
        return activeReports.stream()
                .filter(r -> r.target.equals(target))
                .collect(Collectors.toList());
    }

    // НОВОЕ: Удалить все жалобы на конкретного игрока (когда админ вынес вердикт)
    public static void clearReportsFor(String target) {
        activeReports.removeIf(r -> r.target.equals(target));
    }

    public static List<Report> getReports() {
        return activeReports;
    }
}