package wdd.utils.commons;

import java.security.MessageDigest;


/**
 * DES加密与解密，对传递的token进行解密
 */
public class Sha1 {

    public static String getSha1(String str) {
        if (str == null || str.length() == 0) {
            return null;
        }
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            MessageDigest mdTemp = MessageDigest.getInstance("SHA1");
            mdTemp.update(str.getBytes("UTF-8"));

            byte[] md = mdTemp.digest();
            int j = md.length;
            char buf[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                buf[k++] = hexDigits[byte0 >>> 4 & 0xf];
                buf[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(buf);
        } catch (Exception e) {
            // TODO: handle exception
            return null;
        }
    }


    public static void main(String[] args) {
        System.out.println(getSha1("30:84:505:FF").length());
        System.out.println(getSha1("CCD6E1CD-8C4B-40CB-8A62-4BBC7AFE07D6").length());
    }

}
