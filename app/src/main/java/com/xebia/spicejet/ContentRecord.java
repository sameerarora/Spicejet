package com.xebia.spicejet;

import java.io.Serializable;

public class ContentRecord implements Serializable {

    private static final long serialVersionUID = 1234L;

    String name;
    String locationURL;
    String playUrl;
    long timeout;

    public ContentRecord(String name, String locationURL, long timeout) {
        this.name = name;
        this.locationURL = locationURL;
        this.timeout = timeout;
    }

    public void setPlayUrl(String playUrl) {
        this.playUrl = playUrl;
    }

    public String getPlayUrl() {
        return playUrl;
    }

    public String getName() {
        return name;
    }

    public String getLocationURL() {
        return locationURL;
    }

    public long getTimeout() {
        return timeout;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContentRecord that = (ContentRecord) o;

        return !(name != null ? !name.equals(that.name) : that.name != null);

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
