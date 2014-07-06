package cz.hatoff.geofort.sequencer.downloader;


public class GpxFile {

    private String code;
    private byte[] gxp;

    public GpxFile(String code, byte[] gxp) {
        this.code = code;
        this.gxp = gxp;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public byte[] getGxp() {
        return gxp;
    }

    public void setGxp(byte[] gxp) {
        this.gxp = gxp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GpxFile gpxFile = (GpxFile) o;

        if (!code.equals(gpxFile.code)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }

    @Override
    public String toString() {
        return "GpxFile{" +
                "code='" + code + '\'' +
                '}';
    }
}
