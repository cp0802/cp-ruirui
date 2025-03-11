package com.adanxing.ad.user.utils;

/**
 * @description: int与Byte数组转换
 **/
public class ByteUtils {

    /*
    int转byte[]
    *将int数值转换为占四个字节的byte数组，int低位在前，高位在后
    * */
    public static byte[] intToBytes(int value){
        byte[] bytes = new byte[4];
        for (int i = 0; i < bytes.length; i++){
            int n = bytes.length - 1 -i;
            bytes[4 - i - 1] = (byte)((value>>8*n));
        }
        return bytes;
    }


    /*
     *byte[]转int
     * 适用于低位在前高位在后
     * */
    public static int byteToInt(byte[] src, int offset){
        int value;
        value = (int) ((src[offset] & 0xFF)
                | ((src[offset+1] & 0xFF)<<8)
                | ((src[offset+2] & 0xFF)<<16)
                | ((src[offset+3] & 0xFF)<<24));
        return value;
    }

    /*
     *int转byte[]
     *将int数值转换为占四个字节的byte数组，int高位在前低位在后
     * */
    public static byte[] intToBytes2(int value) {
        byte[] bytes = new byte[4];
        for (int i = 0; i < bytes.length; i++){
            int n = bytes.length - 1 -i;
            bytes[i] = (byte)((value>>8*n));
        }
        return bytes;
    }

    /**
     byte[]转int，高位在前低位在后
     */

    public static int byteToInt2(byte[] src, int offset) {
        int value;
        value = (int) ( ((src[offset] & 0xFF)<<24)
                |((src[offset+1] & 0xFF)<<16)
                |((src[offset+2] & 0xFF)<<8)
                |(src[offset+3] & 0xFF));
        return value;
    }

    public static void main(String[] args) {
        System.out.println("int转byte[]低位在钱高位在后");
        byte[] bytes = intToBytes(1999);
        for (int n = 0; n < bytes.length; n++){
            System.out.println(bytes[n]);
        }
        System.out.println("byte[]转int地位在前高位在后");
        int value = byteToInt(bytes,0);
        System.out.println(value);

        System.out.println("----------------------------");

        System.out.println("byte[]转int地位在前高位在后");
        byte[] bytes2 = intToBytes2(1999);
        for (int n = 0; n < bytes2.length; n++){
            System.out.println(bytes[n]);
        }
        System.out.println("byte[]转int地位在前高位在后");
        int value2 = byteToInt2(bytes2,0);
        System.out.println(value2);
    }
}

