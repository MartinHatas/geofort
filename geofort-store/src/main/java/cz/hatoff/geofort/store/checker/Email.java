package cz.hatoff.geofort.store.checker;


import java.util.Date;

public class Email {

    private String from;
    private String subject;
    private Date updateDate;
    private byte[] zipArchive;

    public Email(String from, String subject, Date updateDate, byte[] zipArchive) {
        this.from = from;
        this.subject = subject;
        this.updateDate = updateDate;
        this.zipArchive = zipArchive;
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

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public byte[] getZipArchive() {
        return zipArchive;
    }

    public void setZipArchive(byte[] zipArchive) {
        this.zipArchive = zipArchive;
    }

    @Override
    public String toString() {
        return "Email{" +
                "updateDate=" + updateDate +
                ", subject='" + subject + '\'' +
                ", from='" + from + '\'' +
                '}';
    }
}
