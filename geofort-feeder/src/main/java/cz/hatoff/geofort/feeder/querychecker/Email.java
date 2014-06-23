package cz.hatoff.geofort.feeder.querychecker;


import java.util.Date;

public class Email {

    private String from;
    private String subject;
    private String content;
    private Date updateDate;

    public Email(String from, String subject, String content, Date updateDate) {
        this.from = from;
        this.subject = subject;
        this.content = content;
        this.updateDate = updateDate;
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

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }
}
