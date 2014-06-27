package cz.hatoff.geofort.feeder.querychecker;


import java.net.URL;
import java.util.Date;

public class CheckedPocketQuery {

    protected String queryName;
    protected URL downloadUrl;
    protected Date updateDate;
    protected String emailSubject;

    public CheckedPocketQuery(String queryName, URL downloadUrl, Date updateDate, String emailSubject) {
        this.queryName = queryName;
        this.downloadUrl = downloadUrl;
        this.updateDate = updateDate;
        this.emailSubject = emailSubject;
    }

    public String getQueryName() {
        return queryName;
    }

    public void setQueryName(String queryName) {
        this.queryName = queryName;
    }

    public URL getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(URL downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public String getEmailSubject() {
        return emailSubject;
    }

    public void setEmailSubject(String emailSubject) {
        this.emailSubject = emailSubject;
    }

    @Override
    public String toString() {
        return "PocketQuery{" +
                "queryName='" + queryName + '\'' +
                ", downloadUrl=" + downloadUrl +
                ", updateDate=" + updateDate +
                ", emailSubject='" + emailSubject + '\'' +
                '}';
    }
}
