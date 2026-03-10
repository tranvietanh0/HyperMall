package com.hypermall.notification.provider;

import com.hypermall.notification.entity.Notification;

public interface NotificationProvider {

    boolean send(Notification notification);

    String getProviderName();
}
