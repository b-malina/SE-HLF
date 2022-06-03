package org.chaincode;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.spec.IvParameterSpec;

public class SE {
    public static void main(String[] args) throws Exception {
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(new byte[128]);
        Key symmetricKey1 = Utils.generateSymmetricKey(secureRandom, 128, new byte[128]);
        IvParameterSpec ivParameterSpec = Utils.generateIv();
        // secureRandom.getInstance("SHA1PRNG");
        // Key symmetricKey2 = Utils.generateSymmetricKey(secureRandom, numberBits,//
        // seed);

        BigInteger P = BigInteger.probablePrime(128, secureRandom);
        System.out.println("\n" + "\n" + "  Prime P= " + P);// Arrays.toStrixng(secureRandom.getSeed(numberBits)).length());

        // JsonParser parser = new JsonParser();
        // Object obj = parser.parse(new FileReader("organizations/Org1/Org1
        // Admin.id"));
        // JsonObject jsonObject = (JsonObject) obj;
        // JsonElement credentials = jsonObject.get("credentials");
        // JsonElement certificate = credentials.getAsJsonObject().get("certificate");
        // JsonElement privateKey = credentials.getAsJsonObject().get("privateKey");

        List<String> docNames = Arrays.asList("text1.txt", "text2.txt", "text3.txt");
        HashMap<String, String> keywordsDict = new HashMap<>();

        //////////////////////////////// KEYWORDS ////////////////////////////////
        keywordsDict.put("plaintext", "text1.txt");
        keywordsDict.put("this", "text2.txt");
        keywordsDict.put("pt", "text3.txt");

        System.out.println("\ninit keydict");
        for (Map.Entry entry : keywordsDict.entrySet())
            System.out.println("keydict= " + entry.toString());
        int ks = keywordsDict.size(), ds = docNames.size();
        if (ks != ds)
            System.out.println("number of keywords is not the same as number of documents");

        String[][] A = new String[ds + 2][ks + 2];
        for (int d = 0; d < A.length; d++)
            for (int k = 0; k < A[0].length; k++)
                A[d][k] = "0";

        Utils.buildIndex(docNames, keywordsDict, symmetricKey1, A, P,
                secureRandom, ivParameterSpec);
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < A[0].length; j++)
                System.out.print(A[i][j] + " ");
            System.out.println("ARR\n" + "\n");
        }

        //////////////////////////////// SEARCH ////////////////////////////////
        String keyword = "this";
        Trapdoor trapdoor = Utils.buildTrapdoor(symmetricKey1, keyword, P, ivParameterSpec);
        System.out.println("\n" + "TRAPDOOR " + trapdoor.c + "     -       " + trapdoor.d);

        Map<BigInteger, String> X = Utils.search(trapdoor, symmetricKey1, A, P);

        List<String> docIDS = Utils.decryptIDs(X, symmetricKey1, ivParameterSpec);

        System.out.println("\n" + "len(docids)= " + docIDS.size());

        System.out.println("-------------------------------------------------------------------");
        for (String docID : docIDS) {
            System.out.println("\n\nFound keyword " + keyword + " in document " + docID);
            String docContent = new String(Files.readAllBytes(Paths.get(docID)));
            System.out.println("\nContent of document:\n-------------------------\n" + docContent + "\n");
        }

    }

}

// System.out.println("\n" + "AAAAVCDVSV1 " + privateKey);
// System.out.println("\n" + "AAAAVCDVSV2 " + privateKey.toString());

// ECParameterSpec ecParameterSpec =
// ECNamedCurveTable.getParameterSpec("secp256r1");
// ECNamedCurveSpec params = new ECNamedCurveSpec("secp160r2",
// ecParameterSpec.getCurve(), ecParameterSpec.getG(),
// ecParameterSpec.getN());
// ECPoint publicPoint = ECPointUtil.decodePoint(params.getCurve(),
// DatatypeConverter.parseHexBinary(privateKey.toString()));
// ECPrivateKeySpec pubKeySpec = new ECPrivateKeySpec(publicPoint, params);
// KeyFactory kf = KeyFactory.getInstance("EC");
// PublicKey publicKey = kf.generatePublic(pubKeySpec);
// System.out.println("\n" + "AAAAVCDVSV " + priv);
// }
