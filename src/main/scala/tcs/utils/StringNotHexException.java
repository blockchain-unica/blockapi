package tcs.utils;

/**
 * Created by Livio on 06/09/2017.
 */
public class StringNotHexException extends Exception{
    String message;
    public StringNotHexException(String string){
        message = string + "\tis not an Hexadecimal Character";
    }
    public String getMessage(){
        return message;
    }
}