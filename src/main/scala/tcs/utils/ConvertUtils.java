package tcs.utils;

import org.bouncycastle.crypto.digests.RIPEMD160Digest;

import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;


/**
 * Created by Livio on 06/09/2017.
 */
public class ConvertUtils{

    public static byte[] hexToBytes(String hex){
        int byteLength = hex.length()/2;
        byte[] bytes = new byte[byteLength];
        for(int i = 0; i < byteLength;i++){
            bytes[i] = (byte) Integer.parseInt(hex.substring(i*2,((i*2)+2)),16);
        }
        return bytes;
    }

    public static String asciiToBinToHex(String arg) throws UnsupportedEncodingException{

        return bytesToHex(arg.getBytes("UTF-8"));
    }

    public static String bytesToHex(byte[] bytes){
        char[] charsK = "0123456789ABCDEF".toCharArray();
        char[] chars = new char[bytes.length*2];
        int c;
        for(int i = 0; i < bytes.length;i++){
            c = bytes[i] & 0xFF;
            chars[i*2] = charsK[c >>> 4];
            chars[i*2+1]=charsK[c & 0x0F];
        }
        return new String(chars);
    }

    public static String XORToString(String first, String second) throws XORDataException, StringNotHexException{

        String resultString = bytesToHex(XORToBytes(first,second));
        System.out.println("Result XOR is"+ resultString);
        return resultString;
    }

    public static byte[] XORToBytes(String first, String second) throws XORDataException, StringNotHexException{
        Pattern hexPatttern = Pattern.compile("[0-9a-fA-F]*");
        if(hexPatttern.matcher(first).matches() == false)throw new StringNotHexException(first);
        if(hexPatttern.matcher(second).matches() == false)throw new StringNotHexException(second);
        if(first.length() != second.length()) throw new XORDataException(first +"\tand\t" +second);

        byte[] firstBytes = hexToBytes(first);
        byte[] secondBytes = hexToBytes(second);

        byte[] xor = XORToBytes(firstBytes,secondBytes);
        System.out.println("xor in hex ==="+ bytesToHex(xor));
        return xor;
    }

    public static byte[] XORToBytes(byte[] first, byte[] second) throws XORDataException, StringNotHexException{
        if(first.length != second.length)throw new XORDataException(first.length +"\tand\t"+ second.length);

        byte[] result = new byte[(first.length + second.length)/2];

        for(int i = 0; i < result.length; i++){
            result[i] = (byte)(first[i] ^ second[i]);
        }
        return result;
    }

    public static byte[] getRIPEMD160Digest(byte[] bytes) {
        RIPEMD160Digest digest = new RIPEMD160Digest();
        digest.update(bytes, 0, bytes.length);
        byte[] ripmemdHash = new byte[20];
        digest.doFinal(ripmemdHash, 0);
        return ripmemdHash;
    }

}