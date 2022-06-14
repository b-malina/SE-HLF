/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.chaincode;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import static org.apache.commons.codec.digest.HmacAlgorithms.HMAC_SHA_512;
import org.apache.commons.codec.digest.HmacUtils;

import com.owlike.genson.Genson;

@DataType()
public class Matrix {

    private final static Genson genson = new Genson();
    @Property()
    private Integer rows;
    @Property()
    private Integer cols;
    @Property()
    private String[][] matrix;

    public Matrix() {
    }

    public Matrix(String[][] matrix) {
        this.matrix = matrix;
    }

    public Matrix(Integer rows, Integer cols, String[][] matrix) {
        this.matrix = matrix;
    }

    public void setMatrix(String[][] matrix) {
        this.matrix = matrix;
    }

    public String[][] getMatrix() {
        return matrix;
    }

    public String toJSONString() {
        return genson.serialize(this).toString();
    }

    public String toJSONString(String[][] matrix) {
        return genson.serialize(matrix).toString();
    }

    public static Matrix fromJSONString(String json) {
        Matrix matrix = genson.deserialize(json, Matrix.class);
        return matrix;
    }

    public static String[][] fromSearch(String json) {
        System.out.println("from search" + json);
        Matrix matrix = genson.deserialize(json, Matrix.class);
        String[][] mx = matrix.getMatrix();
        int rows = (int) Math.ceil(Math.sqrt(mx[0].length));
        int cols = rows;
        String[][] newMatrix = new String[rows][cols];
        int k = 0;
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                newMatrix[i][j] = mx[0][k++];

        return newMatrix;
    }

    public static Matrix fromJSONFile(String pathName) throws IOException {
        String json = new String(Files.readAllBytes(Paths.get(pathName)));
        Matrix matrix = genson.deserialize(json, Matrix.class);
        return matrix;
    }

    public static String[] search(String secret, String dd, String cc, String PP, String[][] A)
            throws IllegalStateException {
        BigInteger d = new BigInteger(dd);
        BigInteger c = new BigInteger(cc);
        BigInteger P = new BigInteger(PP);

        int len = A[0].length;
        for (int l = 1; l < len; l++) {
            if (A[0][l] != null) {
                BigInteger a = new BigInteger(A[0][l], 16);

                if (a.toString(16) != "0") {
                    BigInteger mult = c.multiply(a).mod(P);
                    String hash = new HmacUtils(HMAC_SHA_512, secret).hmacHex(mult.toString(16));
                    BigInteger hashBigInt = new BigInteger(hash, 16);

                    if (d.equals(hashBigInt)) {
                        return getDocumentIndices(l, A);
                    }
                }
            }

        }
        return new String[0];
    }

    public static String[] getDocumentIndices(int index, String[][] A) {
        // Map<BigInteger, String>
        List<String> docs = new ArrayList<>();
        System.out.println("ININDICES...====" + A.length + " =" + A[0].length);

        // A[i][index];
        for (int i = 0; i < A.length; i++) {

            if (!A[i][index].equals("0")) {
                // BigInteger occ = new BigInteger(A[i][index]);
                String doc = A[index][0];
                docs.add(doc);
            }
        }
        for (String d : docs)
            System.out.println("DONE...====" + d);
        String[] docsArr = new String[docs.size()];
        int i = 0;
        for (String d : docs)
            docsArr[i] = d;

        return docsArr;
    }

}
