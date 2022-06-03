package org.chaincode;

import static org.apache.commons.codec.digest.HmacAlgorithms.HMAC_SHA_512;

import org.apache.commons.codec.digest.HmacUtils;

public class HmacService {

    private String sharedSecret;

    public HmacService(String sharedSecret) {
        this.sharedSecret = sharedSecret;
    }

    public String calculateHmac(String data) {
        return new HmacUtils(HMAC_SHA_512, sharedSecret).hmacHex(data);
    }

    public boolean checkHmac(String data, String hmacHex) {
        return calculateHmac(data).equals(hmacHex);
    }

}