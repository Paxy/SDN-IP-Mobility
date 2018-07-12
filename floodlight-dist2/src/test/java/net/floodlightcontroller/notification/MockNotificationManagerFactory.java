package net.floodlightcontroller.notification;

public class MockNotificationManagerFactory implements
    INotificationManagerFactory {

    
    public <T> INotificationManager getNotificationManager(Class<T> clazz) {
        return null;
    }

}
