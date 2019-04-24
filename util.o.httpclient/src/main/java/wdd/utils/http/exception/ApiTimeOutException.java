package wdd.utils.http.exception;

public class ApiTimeOutException extends Exception {
    private String url;
    private String msg;
    private int ms;

    public ApiTimeOutException(String url, String msg, int ms) {
        super('\t' + url + " [" + 555 + "] " + msg + "(" + ms + "ms)" + " ");
        this.url = url;
        this.msg = msg;
        this.ms = ms;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getMs() {
        return ms;
    }

    public void setMs(int ms) {
        this.ms = ms;
    }
}
