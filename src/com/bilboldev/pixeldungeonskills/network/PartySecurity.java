package com.bilboldev.pixeldungeonskills.network;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Locale;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

final class PartySecurity {
    static final String MAGIC = "Skillful Pixel Dungeon LAN";
    private static final String ALPHABET = "0123456789ABCDEFGHJKMNPQRSTVWXYZ";
    private static final SecureRandom RANDOM = new SecureRandom();
    private final KeyPair keys;
    private final byte[] nonce = new byte[32];
    private final String code;
    private final boolean host;
    private byte[] ownProof, peerProof, sendKey, receiveKey, sendPrefix, receivePrefix;
    private long sent, received;

    PartySecurity(String code, boolean host) throws GeneralSecurityException {
        this.code = normalize(code);
        this.host = host;
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(new ECGenParameterSpec("secp256r1"), RANDOM);
        keys = generator.generateKeyPair();
        RANDOM.nextBytes(nonce);
    }

    static String newCode() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            if (i != 0 && i % 4 == 0) result.append('-');
            result.append(ALPHABET.charAt(RANDOM.nextInt(32)));
        }
        return result.toString();
    }

    static String normalize(String input) {
        if (input == null) throw new IllegalArgumentException("Enter the host's pairing code");
        String code = input.replace("-", "").replace(" ", "").toUpperCase(Locale.ROOT)
                .replace('O', '0').replace('I', '1').replace('L', '1');
        if (code.length() != 12) throw new IllegalArgumentException("Enter the 12-character pairing code");
        for (int i = 0; i < code.length(); i++) {
            if (ALPHABET.indexOf(code.charAt(i)) < 0) throw new IllegalArgumentException("Invalid pairing code");
        }
        return code;
    }

    String publicKey() {
        return hex(keys.getPublic().getEncoded());
    }
    String nonce() {
        return hex(nonce);
    }
    String proof() {
        return hex(ownProof);
    }

    void agree(String publicKey, String remoteNonce, String clientHero, String hostHero)
            throws GeneralSecurityException, IOException {
        byte[] encoded = unhex(publicKey, 256);
        byte[] otherNonce = unhex(remoteNonce, 32);
        if (otherNonce.length != 32) throw new GeneralSecurityException("Invalid nonce");
        ECPublicKey remote = (ECPublicKey)KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(encoded));
        ECParameterSpec ours = ((ECPublicKey)keys.getPublic()).getParams();
        ECParameterSpec theirs = remote.getParams();
        if (!ours.getCurve().equals(theirs.getCurve()) || !ours.getGenerator().equals(theirs.getGenerator())
                || !ours.getOrder().equals(theirs.getOrder()) || ours.getCofactor() != theirs.getCofactor()) {
            throw new GeneralSecurityException("Different curve");
        }
        KeyAgreement agreement = KeyAgreement.getInstance("ECDH");
        agreement.init(keys.getPrivate());
        agreement.doPhase(remote, true);
        byte[] shared = agreement.generateSecret();
        byte[] transcript = transcript(clientHero, hostHero,
                host ? otherNonce : nonce, host ? nonce : otherNonce,
                host ? encoded : keys.getPublic().getEncoded(), host ? keys.getPublic().getEncoded() : encoded);
        byte[] salt = mac(code.getBytes(StandardCharsets.US_ASCII), transcript);
        byte[] secret = mac(salt, shared);
        Arrays.fill(shared, (byte)0);
        byte[] clientProof = derive(secret, "client proof", transcript);
        byte[] hostProof = derive(secret, "host proof", transcript);
        ownProof = host ? hostProof : clientProof;
        peerProof = host ? clientProof : hostProof;
        byte[] clientKey = derive(secret, "client to host key", transcript);
        byte[] hostKey = derive(secret, "host to client key", transcript);
        byte[] clientPrefix = Arrays.copyOf(derive(secret, "client nonce prefix", transcript), 4);
        byte[] hostPrefix = Arrays.copyOf(derive(secret, "host nonce prefix", transcript), 4);
        sendKey = host ? hostKey : clientKey;
        receiveKey = host ? clientKey : hostKey;
        sendPrefix = host ? hostPrefix : clientPrefix;
        receivePrefix = host ? clientPrefix : hostPrefix;
        Arrays.fill(secret, (byte)0);
    }

    void verify(String proof) throws GeneralSecurityException {
        if (peerProof == null || !MessageDigest.isEqual(peerProof, unhex(proof, 32))) {
            throw new GeneralSecurityException("Pairing code did not match");
        }
    }

    static byte[] encryptSetup(byte[] plaintext, boolean discovery) throws GeneralSecurityException {
        if (plaintext.length > (discovery ? 2048 : 4096) - 29) {
            throw new GeneralSecurityException("Setup message is too large");
        }
        byte[] nonce = new byte[12];
        RANDOM.nextBytes(nonce);
        Cipher cipher = setupCipher(Cipher.ENCRYPT_MODE, nonce, discovery);
        byte[] ciphertext = cipher.doFinal(plaintext);
        return ByteBuffer.allocate(13 + ciphertext.length).put((byte)2).put(nonce).put(ciphertext).array();
    }

    static byte[] decryptSetup(byte[] frame, boolean discovery) throws GeneralSecurityException {
        if (frame.length < 29 || frame.length > (discovery ? 2048 : 4096) || frame[0] != 2) {
            throw new GeneralSecurityException("Invalid setup frame");
        }
        byte[] nonce = Arrays.copyOfRange(frame, 1, 13);
        Cipher cipher = setupCipher(Cipher.DECRYPT_MODE, nonce, discovery);
        return cipher.doFinal(frame, 13, frame.length - 13);
    }

    private static Cipher setupCipher(int mode, byte[] nonce, boolean discovery) throws GeneralSecurityException {

        byte[] key = MessageDigest.getInstance("SHA-256").digest(
                (MAGIC + ":setup:" + PartyConnection.FORK_ID).getBytes(StandardCharsets.UTF_8));
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(mode, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, nonce));
        cipher.updateAAD(new byte[]{2, (byte)(discovery ? 1 : 0)});
        return cipher;
    }

    byte[] encrypt(byte[] plaintext) throws GeneralSecurityException {
        if (sendKey == null || sent == Long.MAX_VALUE) throw new GeneralSecurityException("Session key unavailable");
        long counter = sent++;
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(sendKey, "AES"), parameters(sendPrefix, counter));
        cipher.updateAAD(aad(host, counter));
        byte[] ciphertext = cipher.doFinal(plaintext);
        return ByteBuffer.allocate(9 + ciphertext.length).put((byte)1).putLong(counter).put(ciphertext).array();
    }

    byte[] decrypt(byte[] frame) throws GeneralSecurityException {
        if (receiveKey == null || frame.length < 25 || frame[0] != 1) throw new GeneralSecurityException("Invalid encrypted frame");
        long counter = ByteBuffer.wrap(frame, 1, 8).getLong();
        if (counter != received || counter == Long.MAX_VALUE) throw new GeneralSecurityException("Invalid message counter");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(receiveKey, "AES"), parameters(receivePrefix, counter));
        cipher.updateAAD(aad(!host, counter));
        byte[] plaintext = cipher.doFinal(frame, 9, frame.length - 9);
        received++;
        return plaintext;
    }

    private static GCMParameterSpec parameters(byte[] prefix, long counter) {
        return new GCMParameterSpec(128, ByteBuffer.allocate(12).put(prefix).putLong(counter).array());
    }

    private static byte[] aad(boolean fromHost, long counter) {
        byte[] magic = MAGIC.getBytes(StandardCharsets.UTF_8);
        byte[] fork = PartyConnection.FORK_ID.getBytes(StandardCharsets.US_ASCII);
        return ByteBuffer.allocate(magic.length + fork.length + 13).put(magic).put(fork).putInt(PartyConnection.PROTOCOL)
                .put((byte)(fromHost ? 1 : 0)).putLong(counter).array();
    }

    private static byte[] transcript(String clientHero, String hostHero, byte[] clientNonce,
                                     byte[] hostNonce, byte[] clientKey, byte[] hostKey) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream output = new DataOutputStream(bytes);
        output.writeUTF(MAGIC);
        output.writeInt(PartyConnection.PROTOCOL);
        output.writeUTF(PartyConnection.FORK_ID);
        output.writeUTF(clientHero);
        output.writeUTF(hostHero);
        for (byte[] part : new byte[][]{clientNonce, hostNonce, clientKey, hostKey}) {
            output.writeInt(part.length);
            output.write(part);
        }
        return bytes.toByteArray();
    }

    private static byte[] derive(byte[] secret, String label, byte[] transcript) throws GeneralSecurityException {
        byte[] name = label.getBytes(StandardCharsets.US_ASCII);
        return mac(secret, ByteBuffer.allocate(name.length + transcript.length + 1).put(name).put(transcript).put((byte)1).array());
    }

    private static byte[] mac(byte[] key, byte[] bytes) throws GeneralSecurityException {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(bytes);
    }

    static String hex(byte[] bytes) {
        char[] result = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            result[i * 2] = Character.forDigit((bytes[i] >>> 4) & 15, 16);
            result[i * 2 + 1] = Character.forDigit(bytes[i] & 15, 16);
        }
        return new String(result);
    }

    static byte[] unhex(String text, int maximum) throws GeneralSecurityException {
        if (text == null || text.length() == 0 || text.length() > maximum * 2 || text.length() % 2 != 0) {
            throw new GeneralSecurityException("Invalid handshake field");
        }
        byte[] bytes = new byte[text.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            int high = Character.digit(text.charAt(i * 2), 16);
            int low = Character.digit(text.charAt(i * 2 + 1), 16);
            if (high < 0 || low < 0) throw new GeneralSecurityException("Invalid handshake field");
            bytes[i] = (byte)((high << 4) | low);
        }
        return bytes;
    }
}
