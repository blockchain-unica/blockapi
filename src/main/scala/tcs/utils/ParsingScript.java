package tcs.utils;
import tcs.blockchain.Transaction;
import org.bitcoinj.crypto.TransactionSignature;
import java.util.ArrayList;
import java.util.Arrays;

public class ParsingScript {

    /*
    Tipologie di input

    Pay To Public Key Hash (P2PKH)

    Pubkey script: OP_DUP OP_HASH160 <PubKeyHash> OP_EQUALVERIFY OP_CHECKSIG
    Signature script: <sig> <pubkey>

     Pay To Script Hash (P2SH)

     Pubkey script: OP_HASH160 <Hash160(redeemScript)> OP_EQUAL
     Signature script: <sig> [sig] [sig...] <redeemScript>

     Multisig

     Pubkey script: <m> <A pubkey> [B pubkey] [C pubkey...] <n> OP_CHECKMULTISIG
     Signature script: OP_0 <A sig> [B sig] [C sig...]

     Pubkey

     Pubkey script: <pubkey> OP_CHECKSIG
     Signature script: <sig>

     Pubkey Script: OP_RETURN <0 to 40 bytes of data>
     (Null data scripts cannot be spent, so there's no signature script.)

     */

    public static ArrayList<Integer> compute(byte[] script){
        ArrayList<Integer>  Signatures=new ArrayList<>();
        byte[] sig={};

        boolean cond=true;

        int start=0,end=0;
        try {
            if (script == null) {
                return null;
            } else {
                if (script[0] == 0) {
                    start = 2;
                    int num = script[1] & 0xff;
                    end = num + start;
                    sig = Arrays.copyOfRange(script, start, end);
                } else {
                    start = 1;
                    int num = script[0] & 0xff;
                    end = num + start;
                    sig = Arrays.copyOfRange(script, start, end);

                }
                while (cond) {
                    if (TransactionSignature.isEncodingCanonical(sig)) {
                        int hashType = (sig[sig.length - 1] & 0xff);
                        Signatures.add(hashType);
                        start = end + 1;
                        if (end < script.length) {
                            int num = script[end] & 0xff;
                            if (num + start < script.length) {
                                end = num + start;
                                sig = Arrays.copyOfRange(script, start, end);
                            } else {
                                cond = false;
                            }
                        } else {
                            cond = false;
                        }
                    } else {
                        cond = false;
                    }

                }
                return Signatures;
            }
            }catch(Exception e){
                return null;
            }

    }
    public static void  tester(){
        //https://blockchain.info/it/rawtx/d889cbf731023bb898907ee1ce876b5ee78fb150808ec7e65aec6e378b91c56c
        String testmultisig="00483045022100d215665d62b2b85fd32291b191ed8a7fd530ba4db80fe3a538bee467eb54df0c02206bbfe14f77949b6eedeef04481780fa5438aa1da04e3301c2e645a8a81df506001473044022072e1cda6b0f5ce27ebc655339b671067ee24e5a571d982f421548733d84c97c102203ae56ccae9bed88509df31f406f6177757b7eec4b0369adcadf3f0efbbc9bc57014cc952410491bba2510912a5bd37da1fb5b1673010e43d2c6d812c514e91bfa9f2eb129e1c183329db55bd868e209aac2fbc02cb33d98fe74bf23f0c235d6126b1d8334f864104865c40293a680cb9c020e7b1e106d8c1916d3cef99aa431a56d253e69256dac09ef122b1a986818a7cb624532f062c1d1f8722084861c5c3291ccffef4ec687441048d2455d2403e08708fc1f556002f1b6cd83f992d085097f9974ab08a28838f07896fbab08f39495e15fa6fad6edbfb1e754e35fa1c7844c41f322a1863d4621353ae";
        //https://blockchain.info/rawtx/b41fbfd4198062759bd938ae1932e214219629fbd38798364bcff5a67b55ffb7
        String testmultisig2="0048304502200365c87a066d1bd54a02f07f4c6f6b3533189668075d1089a621838c506bad33022100c946a24d52a92c0bf6a6483e681330f6c7c06aeaa2c9b11b0a8e5256a2c2228701473044022009e829f2273bfe20ae8d096fbdfbbf3d0c065a9f100804df30254127b0a16a2302204bf68787e344adfd1044cf40923dd2b84de5c6331a5d5d974c603b3a798e37dd01475221033020a41c77ea94aa3a3e975745b5f29b1e850d66cc5c0cddb853f70b34aea8762102e0cf31c6063a5841fbb449e041feb074206596513a3a5f4512ad18ab0114c16252ae";
        //https://blockchain.info/it/rawtx/7aa068cab7f11aa25f34f32bd7b3480790af2facdf8192c59985e6282c336465
        String testmultisig3="004730440220695a28c42daa23c13e192e36a20d03a2a79994e0fe1c3c6b612d0ae23743064602200ca19003e7c1ce0cecb0bbfba9a825fc3b83cf54e4c3261cd15f080d24a8a5b901483045022100aa9096ce71995c24545694f20ab0482099a98c99b799c706c333c521e51db66002206578f023fa46f4a863a6fa7f18b95eebd1a91fcdf6ce714e8795d902bd6b682b014c69522102b66fcb1064d827094685264aaa90d0126861688932eafbd1d1a4ba149de3308b21025cab5e31095551582630f168280a38eb3a62b0b3e230b20f8807fc5463ccca3c21021098babedb3408e9ac2984adcf2a8e4c48e56a785065893f76d0fa0ff507f01053ae";
        //https://blockchain.info/it/rawtx/f759759bc998ec96879e4ae8c1639e8a186e0d507401eb32e4479de64d340605
        String test="483045022100cf822811fb316a42844f8b30a5e1026ec8ab7df58e1b28a1a8cf2d6522f9390302204549cdcb6956bdfbea59698aea443281a208972fa02a6cb3f796aaa3191bff96014104f539b68ab0d5c0c75a78e81a86d76aafd83fa7978d55f347fb583a461af37b72babce7680b67e5760e6af74024e2526d8670d94158697b72f665a770f8fa58fe";
        //malformed input
        String test1="3045022100cf822811fb316a42844f8b30a5e1026ec8ab7df58e1b28a1a8cf2d6522f9390302204549cdcb6956bdfbea59698aea443281a208972fa02a6cb3f796aaa3191bff96014104f539b68ab0d5c0c75a78e81a86d76aafd83fa7978d55f347fb583a461af37b72babce7680b67e5760e6af74024e2526d8670d94158697b72f665a770f8fa58fe";



    }

}