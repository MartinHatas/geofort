package cz.hatoff.geofort.feeder.querydownloader;


import cz.hatoff.geofort.feeder.querychecker.CheckedPocketQuery;

import java.io.File;

public class DownloadedPocketQuery extends CheckedPocketQuery {

    private File downloadedQueryFile;

    public DownloadedPocketQuery(CheckedPocketQuery checkedPocketQuery, File downloadedQueryFile) {
        super(checkedPocketQuery.getQueryName(), checkedPocketQuery.getDownloadUrl(), checkedPocketQuery.getUpdateDate());
        this.downloadedQueryFile = downloadedQueryFile;
    }

    public File getDownloadedQueryFile() {
        return downloadedQueryFile;
    }

    public void setDownloadedQueryFile(File downloadedQueryFile) {
        this.downloadedQueryFile = downloadedQueryFile;
    }

    @Override
    public String toString() {
        return "DownloadedPocketQuery{" +
                "downloadedQueryFile=" + downloadedQueryFile +
                '}' +
                super.toString();
    }
}
