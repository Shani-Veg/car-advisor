package com.shani.caradvisor.agent;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Identifies a document in a recording without keeping the document. */
final class DocumentHash {

    private DocumentHash() {
    }

    static String sha256(byte[] document) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(document));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
