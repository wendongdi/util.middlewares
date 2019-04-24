package wdd.utils.hbase.entity;

public class Column {
    private String family;
    private String qualifier;
    private String value;

    public Column(String family, String qualifier, String value) {
        this.family = family;
        this.qualifier = qualifier;
        this.value = value;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public String getQualifier() {
        return qualifier;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
