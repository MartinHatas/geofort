package cz.hatoff.geofort.feeder.querychecker;


import java.net.URL;

public class CheckedPocketQuery {

    private String queryName;
    private URL downloadUrl;

    public CheckedPocketQuery(String queryName, URL downloadUrl) {
        this.queryName = queryName;
        this.downloadUrl = downloadUrl;
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
}
