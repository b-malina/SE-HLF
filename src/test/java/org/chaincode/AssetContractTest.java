/*
 * SPDX-License-Identifier: Apache License 2.0
 */

package org.chaincode;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public final class AssetContractTest {

    @Nested
    class AssetExists {
        @Test
        public void noProperAsset() {

            AssetContract contract = new AssetContract();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);

            when(stub.getState("10001")).thenReturn(new byte[] {});
            boolean result = contract.documentExists(ctx, "10001");

            assertFalse(result);
        }

        @Test
        public void assetExists() {

            AssetContract contract = new AssetContract();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);

            when(stub.getState("10001")).thenReturn(new byte[] { 42 });
            boolean result = contract.documentExists(ctx, "10001");

            assertTrue(result);

        }

        @Test
        public void noKey() {
            AssetContract contract = new AssetContract();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);

            when(stub.getState("10002")).thenReturn(null);
            boolean result = contract.documentExists(ctx, "10002");

            assertFalse(result);

        }

    }

    @Nested
    class AssetCreates {

        @Test
        public void newAssetCreate() {
            AssetContract contract = new AssetContract();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);

            // String json =
            // "{\"documentId\":\"10001\",\"documentName\":\"TheAsset\",\"keyword\":\"Keyword\",\"content\":\"Content\"}";
            String json = "{\"content\":\"Content\",\"id\":\"10001\",\"keyword\":\"Keyword\",\"name\":\"TheAsset\"}";

            contract.createDocument(ctx, "10001", "TheAsset", "Keyword", "Content");

            verify(stub).putState("10001", json.getBytes(UTF_8));
        }

        @Test
        public void alreadyExists() {
            AssetContract contract = new AssetContract();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);

            when(stub.getState("10002")).thenReturn(new byte[] { 42 });

            Exception thrown = assertThrows(RuntimeException.class, () -> {
                contract.createDocument(ctx, "10002", "TheAsset", "Keyword", "Content");
            });

            System.out.println("AA " + thrown.getMessage());
            assertEquals(thrown.getMessage(), "The document 10002 already exists");

        }

    }

    @Test
    public void assetRead() {
        AssetContract contract = new AssetContract();
        Context ctx = mock(Context.class);
        ChaincodeStub stub = mock(ChaincodeStub.class);
        when(ctx.getStub()).thenReturn(stub);

        Document asset = new Document();
        asset.setContent("New Content");

        String json = asset.toJSONString();
        when(stub.getState("10001")).thenReturn(json.getBytes(StandardCharsets.UTF_8));

        Document returnedAsset = contract.readDocument(ctx, "10001");
        assertEquals(returnedAsset.getContent(), asset.getContent());
    }

    // @Nested
    // class AssetUpdates {
    // @Test
    // public void updateExisting() {
    // AssetContract contract = new AssetContract();
    // Context ctx = mock(Context.class);
    // ChaincodeStub stub = mock(ChaincodeStub.class);
    // when(ctx.getStub()).thenReturn(stub);
    // when(stub.getState("10001")).thenReturn(new byte[] { 42 });

    // contract.updateAsset(ctx, "10001", "updates");

    // String json = "{\"value\":\"updates\"}";
    // verify(stub).putState("10001", json.getBytes(UTF_8));
    // }

    // @Test
    // public void updateMissing() {
    // AssetContract contract = new AssetContract();
    // Context ctx = mock(Context.class);
    // ChaincodeStub stub = mock(ChaincodeStub.class);
    // when(ctx.getStub()).thenReturn(stub);

    // when(stub.getState("10001")).thenReturn(null);

    // Exception thrown = assertThrows(RuntimeException.class, () -> {
    // contract.updateAsset(ctx, "10001", "TheAsset");
    // });

    // assertEquals(thrown.getMessage(), "The asset 10001 does not exist");
    // }

    // }

    @Test
    public void assetDelete() {
        AssetContract contract = new AssetContract();
        Context ctx = mock(Context.class);
        ChaincodeStub stub = mock(ChaincodeStub.class);
        when(ctx.getStub()).thenReturn(stub);
        when(stub.getState("10001")).thenReturn(null);

        Exception thrown = assertThrows(RuntimeException.class, () -> {
            contract.deleteDocument(ctx, "10001");
        });

        assertEquals(thrown.getMessage(), "The document 10001 does not exist");
    }

}
