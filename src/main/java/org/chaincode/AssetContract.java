/*
 * SPDX-License-Identifier: Apache-2.0
 */
package org.chaincode;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;

@Contract(name = "AssetContract", info = @Info(title = "Asset contract", description = "My Smart Contract", version = "0.0.1", license = @License(name = "Apache-2.0", url = ""), contact = @Contact(email = "se@example.com", name = "se", url = "http://se.me")))
@Default
public class AssetContract implements ContractInterface {
    public AssetContract() {

    }

    @Transaction()
    public boolean documentExists(Context ctx, String documentId) {
        byte[] buffer = ctx.getStub().getState(documentId);
        return (buffer != null && buffer.length > 0);
    }

    @Transaction()
    public void createDocument(Context ctx, String documentId, String documentName, String keyword, String content) {
        boolean existsDocument = documentExists(ctx, documentId);
        if (existsDocument) {
            throw new RuntimeException("The document " + documentId + " already exists");
        }
        Document document = new Document(documentId, documentName, keyword, content);

        ctx.getStub().putState(documentId, document.toJSONString().getBytes(UTF_8));
    }

    @Transaction()
    public Document readDocument(Context ctx, String documentId) {
        boolean exists = documentExists(ctx, documentId);
        if (!exists) {
            throw new RuntimeException("The document " + documentId + " does not exist");
        }

        Document newDocument = Document.fromJSONString(new String(ctx.getStub().getState(documentId), UTF_8));
        return newDocument;
    }

    // @Transaction()
    // public void updateAsset(Context ctx, String assetId, String newValue) {
    // boolean exists = assetExists(ctx, assetId);
    // if (!exists) {
    // throw new RuntimeException("The asset " + assetId + " does not exist");
    // }
    // Asset asset = new Asset();
    // asset.setValue(newValue);

    // ctx.getStub().putState(assetId, asset.toJSONString().getBytes(UTF_8));
    // }

    @Transaction()
    public void deleteDocument(Context ctx, String documentId) {
        boolean exists = documentExists(ctx, documentId);
        if (!exists) {
            throw new RuntimeException("The document " + documentId + " does not exist");
        }
        ctx.getStub().delState(documentId);
    }

    @Transaction()
    public boolean matrixExists(Context ctx, String matrixId) {
        byte[] buffer = ctx.getStub().getState(matrixId);
        return (buffer != null && buffer.length > 0);
    }

    @Transaction()
    public void storeIndexMatrix(Context ctx, String matrixId, String json) throws IOException {

        boolean existsMatrix = documentExists(ctx, matrixId);
        if (existsMatrix) {
            throw new RuntimeException("The matrix " + matrixId + " already exists");
        }

        Matrix matrix = Matrix.fromJSONString(json);

        ctx.getStub().putState(matrixId, matrix.toJSONString().getBytes(UTF_8));

    }

    @Transaction()
    public void storeIndexMatrixFromPath(Context ctx, String matrixId, String filePath) throws IOException {

        boolean existsMatrix = documentExists(ctx, matrixId);
        if (existsMatrix) {
            throw new RuntimeException("The matrix " + matrixId + " already exists");
        }

        Matrix matrix = Matrix.fromJSONFile(filePath);

        ctx.getStub().putState(matrixId, matrix.toJSONString().getBytes(UTF_8));

    }

    @Transaction()
    public String[][] readIndexMatrix(Context ctx, String matrixId) {
        boolean exists = matrixExists(ctx, matrixId);
        if (!exists) {
            throw new RuntimeException("The matrix " + matrixId + " does not exist");
        }
        Matrix newMatrix = Matrix.fromJSONString(new String(ctx.getStub().getState(matrixId), UTF_8));
        return newMatrix.getMatrix();
    }

    @Transaction()
    public String[] search(Context ctx, String matrixId, String secret, String d, String c, String P) {
        boolean exists = matrixExists(ctx, matrixId);
        System.out.println("in first search !! ");

        if (!exists) {
            throw new RuntimeException("The matrix " + matrixId + " does not exist");
        }
        String[][] A = Matrix.fromSearch(new String(ctx.getStub().getState(matrixId), UTF_8));
        System.out.println("search !! " + A.length + " " + A[0].length);
        for (int i = 0; i < A.length; i++)
            for (int j = 0; j < A[0].length; j++)
                System.out.println(i + "+" + j + "=" + A[i][j]);
        return Matrix.search(secret, d, c, P, A);
    }

}
