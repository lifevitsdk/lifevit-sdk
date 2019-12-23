package es.lifevit.sdk.pillreminder;

public class LifevitSDKPillReminderMessageData extends LifevitSDKPillReminderData {

    private String messageText;

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageText() {
        return messageText;
    }
}
