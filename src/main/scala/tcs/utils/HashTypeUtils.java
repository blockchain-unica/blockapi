package tcs.utils;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.crypto.TransactionSignature;

import java.util.ArrayList;

/**
 * Created by
 *         Chelo Fabrizio
 *         Lafhouli Hicham
 *         Meloni Antonello
 */


public class HashTypeUtils {

    public static ArrayList<Integer> parsing(byte[] script) {

        if (script == null) {

            return  null;

        } else {

            ArrayList list = new ArrayList<>();

            for (int x = 0; x < script.length - 2; x++) {

                if (script[x] == 0x30 && script[x + 2] == 0x2) {

                    if ((((script[x + 1]& 0xff ) + 3) + x ) <= script.length) {
                        byte[] sig = new byte[(script[x + 1]& 0xff ) + 3];
                        for (int y = 0; y < (script[x + 1] & 0xff ) + 3; y++) {
                            sig[y] = script[x + y];
                        }
                        boolean flag = TransactionSignature.isEncodingCanonical(sig);
                        if (flag) {
                            list.add((int) (sig[sig.length - 1] & 0xff ));
                            x+=(script[x + 1]& 0xff ) + 3;
                        }
                    }
                }
            }

            return list;

        }

    }
    public static Transaction.SigHash getHashType(int hashType){
        switch(hashType){
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
