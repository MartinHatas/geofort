package cz.hatoff.geofort.store.unzipper;


import java.io.File;
import java.util.List;

public class UnzippedPocketQuery {

    private File pqDirectory;
    private List<File> extractedFiles;

    public UnzippedPocketQuery(File pqDirectory, List<File> extractedFiles) {
        this.pqDirectory = pqDirectory;
        this.extractedFiles = extractedFiles;
    }

    public File getPqDirectory() {
        return pqDirectory;
    }

    public void setPqDirectory(File pqDirectory) {
        this.pqDirectory = pqDirectory;
    }

    public List<File> getExtractedFiles() {
        return extractedFiles;
    }

    public void setExtractedFiles(List<File> extractedFiles) {
        this.extractedFiles = extractedFiles;
    }
}
