package cz.hatoff.geofort.store.crawlers.elasticsearch;


public class ElasticsearchCacheDocument {

    private String code;
    private String json;
    private String type = "geocache";


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "ElasticsearchCacheDocument{" +
                "type='" + type + '\'' +
                ", code='" + code + '\'' +
                '}';
    }
}
