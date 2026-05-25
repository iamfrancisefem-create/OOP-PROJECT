package com.pms.scheduler;

import com.pms.entity.Task;
import com.pms.entity.enums.NotificationType;
import com.pms.repository.TaskRepository;
import com.pms.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeadlineScheduler {

    private final TaskRepository taskRepository;
    private final NotificationService notificationService;

    /**
     * Checks for overdue tasks and approaching deadlines every morning at 8:00 AM.
     * Cron expression: "0 0 8 * * *" (Second, Minute, Hour, Day, Month, Day-of-week).
     * For demonstration/test purposes, let's also allow a 10-minute interval check or keep standard 8 AM.
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void checkDeadlines() {
        log.info("Deadline Scheduler started checking task deadlines...");
        LocalDate today = LocalDate.now();

        // 1. Process approaching deadlines (within 3 days)
        LocalDate threshold = today.plusDays(3);
        List<Task> approachingTasks = taskRepository.findTasksApproachingDeadline(today, threshold);
        
        for (Task task : approachingTasks) {
            if (task.getAssignedTo() != null) {
                String title = "Deadline Approaching!";
                String message = String.format("Task '%s' is due on %s. Please review progress.", 
                        task.getTitle(), task.getDeadline());
                
                notificationService.createNotification(
                        task.getAssignedTo(), 
                        title, 
                        message, 
                        NotificationType.DEADLINE_APPROACHING
                );
            }
        }

        // 2. Process overdue tasks
        List<Task> overdueTasks = taskRepository.findOverdueTasks(today);
        for (Task task : overdueTasks) {
            if (task.getAssignedTo() != null) {
                String title = "Task Overdue Alert!";
                String message = String.format("Task '%s' was due on %s but is still not marked as DONE.", 
                        task.getTitle(), task.getDeadline());
                
                notificationService.createNotification(
                        task.getAssignedTo(), 
                        title, 
                        message, 
                        NotificationType.DEADLINE_OVERDUE
                );
            }
        }
        
        log.info("Deadline Scheduler finished task checks. Approaching alerts sent: {}, Overdue alerts sent: {}", 
                approachingTasks.size(), overdueTasks.size());
    }
}
