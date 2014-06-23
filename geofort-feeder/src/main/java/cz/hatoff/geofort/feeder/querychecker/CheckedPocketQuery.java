package cz.hatoff.geofort.feeder.querychecker;


import java.net.URL;
import java.util.Date;

public class CheckedPocketQuery {

    private String queryName;
    private URL downloadUrl;
    private Date updateDate;

    public CheckedPocketQuery(String queryName, URL downloadUrl, Date updateDate) {
        this.queryName = queryName;
        this.downloadUrl = downloadUrl;
        this.updateDate = updateDate;
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

    @Override
    public String toString() {
        return "CheckedPocketQuery{" +
                "queryName='" + queryName + '\'' +
                ", downloadUrl=" + downloadUrl +
                ", updateDate=" + updateDate +
                '}';
    }
}
