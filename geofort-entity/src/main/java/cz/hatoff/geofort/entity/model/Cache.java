package cz.hatoff.geofort.entity.model;


import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "CACHE")
public class Cache {

    @Id
    @Column(name = "CODE")
    private String code;

    @Lob
    @Column(name = "GPX")
    private byte[] gpx;

    @Column(name = "LAST_UPDATE")
    private Date lastUpdate;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public byte[] getGpx() {
        return gpx;
    }

    public void setGpx(byte[] gpx) {
        this.gpx = gpx;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Cache cache = (Cache) o;

        if (code != null ? !code.equals(cache.code) : cache.code != null) return false;
        if (lastUpdate != null ? !lastUpdate.equals(cache.lastUpdate) : cache.lastUpdate != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = code != null ? code.hashCode() : 0;
        result = 31 * result + (lastUpdate != null ? lastUpdate.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Cache{" +
                "code='" + code + '\'' +
                ", lastUpdate=" + lastUpdate +
                '}';
    }
}
