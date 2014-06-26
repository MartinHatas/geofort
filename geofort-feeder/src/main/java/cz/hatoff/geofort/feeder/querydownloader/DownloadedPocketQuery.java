package cz.hatoff.geofort.feeder.querydownloader;


import cz.hatoff.geofort.feeder.querychecker.CheckedPocketQuery;

public class DownloadedPocketQuery extends CheckedPocketQuery {

    private byte[] downloadedQuery;

    public DownloadedPocketQuery(CheckedPocketQuery checkedPocketQuery, byte[] downloadedQuery) {
        super(checkedPocketQuery.getQueryName(), checkedPocketQuery.getDownloadUrl(), checkedPocketQuery.getUpdateDate());
        this.downloadedQuery = downloadedQuery;
    }

    public byte[] getDownloadedQuery() {
        return downloadedQuery;
    }

    public void setDownloadedQuery(byte[] downloadedQuery) {
        this.downloadedQuery = downloadedQuery;
    }

    @Override
    public String toString() {
        return "DownloadedPocketQuery{" +
                "downloadedQuery=" + getQueryName() +
                '}';
    }
}
