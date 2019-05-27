//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package wdd.utils.mongo.entity;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
import java.util.Date;

public abstract class BaseEntity implements Serializable {
    private static final long serialVersionUID = 20130411L;
    private String _id;
    private String _class;
    private boolean isValid;
    private boolean isDeleted;
    private Date createTime;
    private Date updateTime;

    public BaseEntity() {
    }

    public String get_id() {
        return this._id;
    }

    public void set_id(String id) {
        this._id = id;
    }

    public String get_class() {
        return this._class;
    }

    public void set_class(String _class) {
        this._class = _class;
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
