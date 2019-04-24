package wdd.utils.http.exception;

import com.alibaba.fastjson.JSON;
import org.apache.http.StatusLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpException extends Exception {
    private static Logger logger = LoggerFactory.getLogger(HttpException.class);

    private StatusLine statusLine;

    public HttpException(StatusLine statusLine) {
        super(JSON.toJSONString(statusLine));
        this.statusLine = statusLine;
    }

    public static Logger getLogger() {
        return logger;
    }

    public static void setLogger(Logger logger) {
        HttpException.logger = logger;
    }

    public StatusLine getStatusLine() {
        return statusLine;
    }

    public void setStatusLine(StatusLine statusLine) {
        this.statusLine = statusLine;
    }
}
