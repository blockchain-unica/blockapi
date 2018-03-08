package tcs.utils;
import org.bitcoinj.core.Transaction;


public class HashTypeUtils {
    public static Transaction.SigHash getHashType(int hasType){
        switch(hasType){
            case 0x01: return Transaction.SigHash.ALL;
            case 0x02: return Transaction.SigHash.NONE;
            case 0x03: return Transaction.SigHash.SINGLE;
            case 0x80: return Transaction.SigHash.ANYONECANPAY;
            case 0x81: return Transaction.SigHash.ANYONECANPAY_ALL;
            case 0x82: return Transaction.SigHash.ANYONECANPAY_NONE;
            case 0x83: return Transaction.SigHash.ANYONECANPAY_SINGLE;
            default:
                return  Transaction.SigHash.UNSET;
        }
    }
}
