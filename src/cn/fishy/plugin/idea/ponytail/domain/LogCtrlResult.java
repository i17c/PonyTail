package cn.fishy.plugin.idea.ponytail.domain;

import java.io.Serializable;

/**
 * User: duxing
 * Date: 2015-08-25 16:43
 */
public class LogCtrlResult implements Serializable {
    private static final long serialVersionUID = 7428801230426928840L;
    //是否成功
    private boolean success = false;
    //返回数据对象
    private ViewLog log;

    public LogCtrlResult() {
    }

    public LogCtrlResult(boolean success) {
        this.success = success;
    }

    public LogCtrlResult(boolean success, ViewLog log) {
        this.success = success;
        this.log = log;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public ViewLog getLog() {
        return log;
    }

    public void setLog(ViewLog log) {
        this.log = log;
    }
}
