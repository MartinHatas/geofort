package cz.hatoff.geofort.feeder.unzipper;

import cz.hatoff.geofort.feeder.querychecker.CheckedPocketQuery;
import cz.hatoff.geofort.feeder.querydownloader.DownloadedPocketQuery;

import java.io.File;

public class UnzippedPocketQuery extends DownloadedPocketQuery{

    public UnzippedPocketQuery(CheckedPocketQuery checkedPocketQuery, File downloadedQueryFile) {
        super(checkedPocketQuery, downloadedQueryFile);
    }
}
