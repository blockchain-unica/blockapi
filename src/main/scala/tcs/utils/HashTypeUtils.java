package tcs.utils;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.crypto.TransactionSignature;

import java.util.ArrayList;

/**
<<<<<<< HEAD
 * Created by
 * Chelo Fabrizio
 * Lafhouli Hicham
 * Meloni Antonello
 *
 * This class provides a static method to get all the hash type values inside a script with multiple signatures,
 * and also provide a static method to identify the specific hash type.
 */
=======
  * Created by
  * Chelo Fabrizio
  * Lafhouli Hicham
  * Meloni Antonello
  */
>>>>>>> fd567310de429b739598b976d2b9d130b2df12d8


public class HashTypeUtils {

    /**
     *
     * @param script : the script containing all the signatures, represented as a sequence of bytes
     * @return : an ArrayList of numbers representing the hash types values for the signaures
     */

    public static ArrayList<Integer> parse(byte[] script) {

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
<<<<<<< HEAD

    /**
     *
     * @param hashType : an integer representing the hash type value, obtained from the last byte of a signature
     * @return: an enum value representing the specific hash type
     */

=======
    
>>>>>>> fd567310de429b739598b976d2b9d130b2df12d8
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