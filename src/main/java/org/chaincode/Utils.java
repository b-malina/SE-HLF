package org.chaincode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

class Trapdoor {
    BigInteger d;
    BigInteger c;

    public Trapdoor(BigInteger d, BigInteger c) {
        this.d = d;
        this.c = c;
    }
}

public class Utils {
    static HmacService hmacService = null;

    public static KeyPair generateKeyPair(SecureRandom secureRandom, int numberBits, byte[] seed)
            throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("AES");
        secureRandom.setSeed(seed);
        keyPairGenerator.initialize(numberBits, secureRandom);
        KeyPair keyPair = keyPairGenerator.genKeyPair();
        return keyPair;
    }

    public static Key generateSymmetricKey(SecureRandom secureRandom, int numberBits, byte[] seed)
            throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        secureRandom.setSeed(seed);
        keyGenerator.init(numberBits, secureRandom);
        return keyGenerator.generateKey();
    }

    public static List<String> buildIndex(List<String> docNames, Map<String, String> keywordsDict, Key symmetricKey,
            String[][] A, BigInteger P, SecureRandom secureRandom, IvParameterSpec ivParameterSpec)
            throws Exception {
        List<String> encDocNames = new ArrayList<>();
        hmacService = new HmacService(Arrays.toString(symmetricKey.getEncoded()));

        // String w1 = hmacService.calculateHmac("abc");
        // BigInteger b1 = new BigInteger(w1, 16).mod(P).modInverse(P);
        // BigInteger b2 = b1.modInverse(P);
        // System.out.println("AAAAAAABBCC");
        // System.out.println(b1);
        // System.out.println(b2);
        // System.out.println(b1.multiply(b2));
        // System.out.println(b1.multiply(b2).mod(P));

        // compute middle (row,col > 0) values based on frequency
        int t = 1;
        for (Map.Entry<String, String> entry : keywordsDict.entrySet()) {
            String W = entry.getKey();
            String docID = entry.getValue();
            String docContent = new String(Files.readAllBytes(Paths.get(docID)));
            String[] docContentArr = docContent.split(" ");
            File encDocName = new File(docID.split(".txt")[0] + "_enc.txt");

            // empty file first
            PrintWriter writer = new PrintWriter(encDocName);
            writer.print("");
            writer.close();
            FileWriter fw = new FileWriter(encDocName, true);
            BufferedWriter bw = new BufferedWriter(fw);

            int u = 0;
            while (u < docContentArr.length) {
                System.out.println(u + " " + t + " " + docContentArr[u] + " " + W);

                // TODO : split punctuation
                if (docContentArr[u].toLowerCase().equals(W.toLowerCase())) {
                    // System.out.println(u + " " + t + " " + docContentArr[u] + " " + W);
                    System.out.println("yes");
                    BigInteger val = new BigInteger(A[u + 1][t]);
                    val = val.add(new BigInteger("1")).mod(P);
                    A[u + 1][t] = val.toString(); // hmacService.calculateHmac(val.toString());
                }
                String encDocWord = encrypt(docContentArr[u], symmetricKey, ivParameterSpec);
                // System.out.println("1--- " + docContentArr[u] + " ----->>>> " + encDocWord);
                bw.append(encDocWord + "__");
                u++;
            }
            bw.flush();
            bw.close();
            encDocNames.add(encDocName.getName());
            t++;
        }

        // A[0][0] - empty
        // shift all doc ids one row below
        // shift all hashes one column to the right
        t = 0;
        for (Map.Entry<String, String> entry : keywordsDict.entrySet()) {
            String W = entry.getKey();
            String docID = entry.getValue();

            String digest = hmacService.calculateHmac(W);
            BigInteger a = new BigInteger(digest, 16).mod(P);
            a = a.modInverse(P);
            // first row = inverse(hash(kw))
            A[0][t + 1] = a.toString(16);
            // first column = ENC(docId)
            A[t + 1][0] = encrypt(docID, symmetricKey, ivParameterSpec);
            System.out.println(
                    "\n" + "keyword  " + W + " is now hashed Keyword " + digest + " in buildingex for doc " + docID);

            t++;
        }
        BigInteger R = BigInteger.valueOf(secureRandom.nextLong());
        // mask frequencies
        for (int n = 1; n < A.length; n++)
            for (int m = 1; m < A[0].length; m++) {

                BigInteger val = new BigInteger(A[n][m]);
                System.out.println("bef AADASAA " + A[n][m]);
                // A[n + 1][m + 1] = encrypt(val.toString(), symmetricKey, ivParameterSpec);
                A[n][m] = val.multiply(R).mod(P).toString();
                // System.out.println("aft AADASAA " + A[n][m]);

            }
        return encDocNames;
    }

    public static Trapdoor buildTrapdoor(Key symmetricKey, String keyword, BigInteger P,
            IvParameterSpec ivParameterSpec)
            throws InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException,
            SignatureException, IllegalStateException, UnsupportedEncodingException,
            InvalidAlgorithmParameterException {

        String bByte = encrypt(keyword, symmetricKey, ivParameterSpec);
        BigInteger b = new BigInteger(bByte, 16).mod(P);
        System.out.print("\nb==\n" + b);

        String aByte = hmacService.calculateHmac(keyword);
        BigInteger a = new BigInteger(aByte, 16).mod(P);

        BigInteger c = a.multiply(b).mod(P);

        String dByte = hmacService.calculateHmac(b.toString(16));
        BigInteger d = new BigInteger(dByte, 16);

        Trapdoor trapdoor = new Trapdoor(d, c);
        System.out.println("\nFINAL TRAPDOOR for " + keyword + " \nTw.d=" + d + "\nTw.c= " + c);
        return trapdoor;
    }

    public static Map<BigInteger, String> search(Trapdoor trapdoor, Key sessionKey, String[][] A, BigInteger P)
            throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, IllegalStateException,
            UnsupportedEncodingException, DecoderException {

        int len = A[0].length;
        for (int l = 1; l < len; l++) {
            BigInteger a = new BigInteger(A[0][l], 16);
            System.out.println("IN SEARCH CURR= " + a);
            if (a.toString(16) != "0") {
                // a = a.modInverse(P);

                BigInteger mult = trapdoor.c.multiply(a).mod(P);
                String hash = hmacService.calculateHmac(mult.toString(16));
                BigInteger hashBigInt = new BigInteger(hash, 16);

                System.out.println("\n\n\ntrdoor d=\n" + trapdoor.d);
                System.out.println("hash=\n" + hashBigInt);
                System.out.println("trdoor d=\n" + trapdoor.d.mod(P));
                System.out.println("hash=\n" + hashBigInt.mod(P) + "\n\n\n");

                if (trapdoor.d.equals(hashBigInt))
                    return getDocumentIndices(l, A);
            }

        }
        return new TreeMap<>();
    }

    public static Map<BigInteger, String> getDocumentIndices(int index, String[][] A) throws DecoderException {
        Map<BigInteger, String> docs = new TreeMap<>();

        // A[i][index];
        for (int i = 1; i < A.length; i++) {
            if (!A[i][index].equals("0")) {
                BigInteger occ = new BigInteger(A[i][index]);
                String doc = A[index][0];
                docs.put(occ, doc);
            }
        }
        return docs;
    }

    public static List<String> decryptIDs(Map<BigInteger, String> X, Key symmetricKey, IvParameterSpec ivParameterSpec)
            throws Exception {
        List<String> decryptedIDs = new ArrayList<String>();
        for (Map.Entry<BigInteger, String> entry : X.entrySet())
            System.out.println("Before " + entry.toString());

        for (Map.Entry<BigInteger, String> entry : X.entrySet()) {
            String value = decrypt(entry.getValue(), symmetricKey, ivParameterSpec);
            decryptedIDs.add(value);
            X.put(entry.getKey(), value);
        }

        for (Map.Entry<BigInteger, String> entry : X.entrySet())
            System.out.println("After " + entry.toString());

        return decryptedIDs;
    }

    public static IvParameterSpec generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    public static String encrypt(String input, Key key,
            IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] cipherText = cipher.doFinal(input.getBytes());
        return Hex.encodeHexString(cipherText);
    }

    public static String decrypt(String cipherText, Key key,
            IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException, DecoderException, UnsupportedEncodingException {

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        byte[] cipherTextBytes = Hex.decodeHex(cipherText.toCharArray());
        cipherTextBytes = cipher.doFinal(cipherTextBytes);
        return new String(cipherTextBytes);
    }

}

// for(

// int l = 1;l<A.length;l++)for(
// int ll = 1;ll<A[0].length;ll++)
// {
// BigInteger a = new BigInteger(A[l][ll], 16);
// System.out.println("IN SEARCH CURR= " + a);
// if (a.toString(16) != "0") {
// System.out.println("trdoor d=\n" + trapdoor.d);
// System.out.println("trdoor c=\n" + trapdoor.c);

// a = a.modInverse(P);
// BigInteger mult = trapdoor.c.multiply(a);

// System.out.println("\n" + "SEARCH mod inverse: before a=" + a);
// System.out.println("\n" + "SEARCH mod inverse: before c=" + trapdoor.c);
// System.out.println("\n" + "SEARCH mod inverse: mult a*c=" + mult);
// System.out.println("\n" + "SMODEARCH mod inverse: mult a*c=" + mult.mod(P));

// String hash = hmacService.calculateHmac(mult.toString(16));
// String hash2 = hmacService.calculateHmac(mult.mod(P).toString(16));
// BigInteger hashBigInt = new BigInteger(hash, 16);

// System.out.println("\n\n trapdoor.d mod=\n" + trapdoor.d.mod(P) + "\nhash=\n"
// + hashBigInt + "\n"
// + hashBigInt.mod(P));
// System.out.println("\nhash2=\n" + hash2);

// if (trapdoor.d.equals(hashBigInt)) {
// // X = A[l];
// System.out.println("aa Found d = H mod p, X[l=" + l + "]=" + X[l]);
// System.out.println("aa Found d = H mod p, A[l=" + l + "][0]=" + A[l][0]);
// // TODO : CHECK INDICES
// X[l] = A[l][0];
// }
// }

// }return X;

// }