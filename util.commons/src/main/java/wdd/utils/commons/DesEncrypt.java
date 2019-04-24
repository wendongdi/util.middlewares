package wdd.utils.commons;

import javax.crypto.Cipher;
import java.security.Key;
import java.security.Security;

public class DesEncrypt {

	private static String strDefaultKey = "abcDEF123";
    private Cipher encryptCipher = null;
    private Cipher decryptCipher = null;
  
  
    /** 
     * 默认构造方法，使用默认密钥 
     * @throws Exception
     */  
    public DesEncrypt() throws Exception {
        this(strDefaultKey);  
    }  
  
  
    /** 
     * 指定密钥构造方法 
     * @param strKey 指定的密钥 
     * @throws Exception
     */  
    public DesEncrypt(String strKey) throws Exception {
        Security.addProvider(new com.sun.crypto.provider.SunJCE());
        Key key = getKey(strKey.getBytes());
        encryptCipher = Cipher.getInstance("DES");
        encryptCipher.init(Cipher.ENCRYPT_MODE, key);
        decryptCipher = Cipher.getInstance("DES");
        decryptCipher.init(Cipher.DECRYPT_MODE, key);
    }  
  
  
    /** 
     * 加密字符串 
     * @param strIn 需加密的字符串 
     * @return 加密后的字符串 
     * @throws Exception
     */  
    public String encrypt(String strIn) throws Exception {
        return byteArr2HexStr(encrypt(strIn.getBytes()));  
    }  
      
      
    /** 
     * 加密字节数组 
     * @param arrB 需加密的字节数组 
     * @return 加密后的字节数组 
     * @throws Exception
     */  
    public byte[] encrypt(byte[] arrB) throws Exception {
        return encryptCipher.doFinal(arrB);  
    }  
  
      
      
    /** 
     * 解密字符串 
     * @param strIn 需解密的字符串 
     * @return 解密后的字符串 
     * @throws Exception
     */  
    public String decrypt(String strIn) throws Exception {
        return new String(decrypt(hexStr2ByteArr(strIn)));
    }  
    
    private byte[] decrypt(byte[] arrB) throws Exception {
        return decryptCipher.doFinal(arrB);  
    }  
    
    private Key getKey(byte[] arrBTmp) throws Exception {
        byte[] arrB = new byte[8];  
        for (int i = 0; i < arrBTmp.length && i < arrB.length; i++) {  
            arrB[i] = arrBTmp[i];  
        }  
        Key key = new javax.crypto.spec.SecretKeySpec(arrB, "DES");
        return key;  
    }  
    
    private static String byteArr2HexStr(byte[] arrB) throws Exception {
        int iLen = arrB.length;  
        StringBuffer sb = new StringBuffer(iLen * 2);
        for (int i = 0; i < iLen; i++) {  
            int intTmp = arrB[i];  
            while (intTmp < 0) {  
                intTmp = intTmp + 256;  
            }  
            if (intTmp < 16) {  
                sb.append("0");  
            }  
            sb.append(Integer.toString(intTmp, 16));
        }  
        return sb.toString();  
    } 
    
    private static byte[] hexStr2ByteArr(String strIn) throws Exception {
        byte[] arrB = strIn.getBytes();  
        int iLen = arrB.length;  
        byte[] arrOut = new byte[iLen / 2];  
        for (int i = 0; i < iLen; i = i + 2) {  
            String strTmp = new String(arrB, i, 2);
            arrOut[i / 2] = (byte) Integer.parseInt(strTmp, 16);
        }  
        return arrOut;  
    }  
}
