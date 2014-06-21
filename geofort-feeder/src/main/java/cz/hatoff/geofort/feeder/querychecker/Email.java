package cz.hatoff.geofort.feeder.querychecker;


public class Email {

    private String from;
    private String subject;
    private String content;

    public Email(String from, String subject, String content) {
        this.from = from;
        this.subject = subject;
        this.content = content;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
