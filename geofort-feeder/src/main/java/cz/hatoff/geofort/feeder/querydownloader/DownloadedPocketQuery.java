package cz.hatoff.geofort.feeder.querydownloader;


import java.io.File;

public class DownloadedPocketQuery {

    private File downloadedQueryFile;

    public DownloadedPocketQuery(File downloadedQueryFile) {
        this.downloadedQueryFile = downloadedQueryFile;
    }

    public File getDownloadedQueryFile() {
        return downloadedQueryFile;
    }

    public void setDownloadedQueryFile(File downloadedQueryFile) {
        this.downloadedQueryFile = downloadedQueryFile;
    }
}
