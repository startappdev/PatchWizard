package utils;

import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;

public class Notification {
    private static final NotificationGroup NOTIFICATION_GROUP = new NotificationGroup("-", NotificationDisplayType.BALLOON, true);

    public static void err(Project curProject, String msg) {
        notify(curProject, msg, NotificationType.ERROR);
    }

    public static void out(Project curProject, String msg) {
        notify(curProject, msg, NotificationType.INFORMATION);
    }

    private static void notify(Project curProject, String msg, NotificationType type) {
        Notifications.Bus.notify(NOTIFICATION_GROUP.createNotification(msg, type), curProject);
    }
}
