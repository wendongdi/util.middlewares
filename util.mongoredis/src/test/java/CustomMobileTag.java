import wdd.utils.mongo.annotation.Document;
import wdd.utils.mongo.entity.BaseEntity;

import java.util.HashSet;
import java.util.Set;

@Document(isCache = true, db = "zhiziyun-data", document = "customMobileTag")
public class CustomMobileTag extends BaseEntity {
    public static final long serialVersionUID = -19497934721713016L;
    public String siteId;
    public String name;
    public Integer count = 0;
    /**
     * 0 MD5
     * 1 SHA1
     * 2 原文
     * 3 MAC
     */
    public Integer encryptWay = 0;
    public String encryptName;
    public Set<String> mobileDeviceSet = new HashSet<>();
    public Boolean isFromProbe;
    public Set<String> mobileDeviceMACSet = new HashSet<>();
    public Set<String> mobileDeviceMACSetForSms = new HashSet<>();
    public Boolean macNeedConvert = true;
    public Double budget = 0d;
    public Boolean isForSms;
    public Integer deviceCount = 0;
    /**
     * PROBE(0, "到店人群"),
     * WIFI(1, "WIFI人群"),
     * ADCLICK(2, "广告点击人群");
     * UCLOUD(3,"精选移动人群")
     * LBS(4,"轨迹人群") 轨迹人群的id与其tagid一致
     */
    public String segmentType = "";
    /**
     * 0 标签未采集
     * 1 标签正在采集&正在打标签
     * 2 标签完成采集&正在打标签
     * 3 打完标签
     */
    public Integer status = 0;

    @Override
    public String toMD5() {
        return null;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getEncryptWay() {
        return encryptWay;
    }

    public void setEncryptWay(Integer encryptWay) {
        this.encryptWay = encryptWay;
    }

    public String getEncryptName() {
        return encryptName;
    }

    public void setEncryptName(String encryptName) {
        this.encryptName = encryptName;
    }

    public Set<String> getMobileDeviceSet() {
        return mobileDeviceSet;
    }

    public void setMobileDeviceSet(Set<String> mobileDeviceSet) {
        this.mobileDeviceSet = mobileDeviceSet;
    }

    public Boolean getFromProbe() {
        return isFromProbe;
    }

    public void setFromProbe(Boolean fromProbe) {
        isFromProbe = fromProbe;
    }

    public Set<String> getMobileDeviceMACSet() {
        return mobileDeviceMACSet;
    }

    public void setMobileDeviceMACSet(Set<String> mobileDeviceMACSet) {
        this.mobileDeviceMACSet = mobileDeviceMACSet;
    }

    public Set<String> getMobileDeviceMACSetForSms() {
        return mobileDeviceMACSetForSms;
    }

    public void setMobileDeviceMACSetForSms(Set<String> mobileDeviceMACSetForSms) {
        this.mobileDeviceMACSetForSms = mobileDeviceMACSetForSms;
    }

    public Boolean getMacNeedConvert() {
        return macNeedConvert;
    }

    public void setMacNeedConvert(Boolean macNeedConvert) {
        this.macNeedConvert = macNeedConvert;
    }

    public Double getBudget() {
        return budget;
    }

    public void setBudget(Double budget) {
        this.budget = budget;
    }

    public Boolean getForSms() {
        return isForSms;
    }

    public void setForSms(Boolean forSms) {
        isForSms = forSms;
    }

    public Integer getDeviceCount() {
        return deviceCount;
    }

    public void setDeviceCount(Integer deviceCount) {
        this.deviceCount = deviceCount;
    }

    public String getSegmentType() {
        return segmentType;
    }

    public void setSegmentType(String segmentType) {
        this.segmentType = segmentType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
