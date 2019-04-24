package wdd.utils.http.exception;

/**
 * 同步错误类
 *
 */
public class ApiSyncException extends Exception {
    public ApiSyncException(String api, String code, String msg) {
        super("\t" + api + " [" + code + "] " + msg + " ");
    }

    public ApiSyncException(String api, int code, String msg) {
        super("\t" + api + " [" + code + "] " + msg + " ");
    }
}
