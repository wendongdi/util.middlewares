//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package wdd.utils.mongo.entity;

import com.alibaba.fastjson.annotation.JSONField;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
import java.util.Date;

public abstract class BaseEntity implements Serializable {
    private static final long serialVersionUID = 20130411L;
    @JSONField(name = "_id")
    private String id;
    private boolean isValid;
    private boolean isDeleted;
    private Date createTime;
    private Date updateTime;

    public BaseEntity() {
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean getIsValid() {
        return this.isValid;
    }

    public void setIsValid(boolean isValid) {
        this.isValid = isValid;
    }

    public boolean getIsDeleted() {
        return this.isDeleted;
    }

    public void setIsDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    @JsonSerialize(
            using = JsonDateSerializer.class
    )
    public Date getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @JsonSerialize(
            using = JsonDateSerializer.class
    )
    public Date getUpdateTime() {
        return this.updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public abstract String toMD5();
}
