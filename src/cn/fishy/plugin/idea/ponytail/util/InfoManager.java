package cn.fishy.plugin.idea.ponytail.util;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;

/**
 * User: duxing
 * Date: 2015-08-25 19:41
 */
public class InfoManager {

    public static void info(Project p,String content){
        send(p,"[PonyTail]",content,NotificationType.INFORMATION);
    }
    public static void error(Project p,String content){
        send(p,"[PonyTail]",content,NotificationType.ERROR);
    }
    public static void send(Project p, String title,String content,NotificationType type){
        Notification notification = new Notification("",title,content, type);
        MessageBus mb = getMessageBus(p);
        mb.syncPublisher(Notifications.TOPIC).notify(notification);
        notification.hideBalloon();
    }

    public static MessageBus getMessageBus(Project p) {
        return p == null ? ApplicationManager.getApplication().getMessageBus() : p.getMessageBus();
    }
}
