import wdd.utils.http.HttpRequest;
import wdd.utils.http.exception.ApiTimeOutException;
import wdd.utils.http.exception.HttpException;

import java.io.IOException;

public class test {
    public static void main(String[] args) throws ApiTimeOutException, IOException, HttpException {
        HttpRequest.instance().get("http://dataexchange.zhiziyun.com/dataexchange/");
    }
}
