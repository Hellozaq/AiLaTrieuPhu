package org.example.util;

public class PasswordMigration {
    public static void main(String[] args) {
        String plainPassword = "123";
        String encryptedPassword = EncryptionUtil.encrypt(plainPassword);
        String decryptedPassword = EncryptionUtil.decrypt(encryptedPassword);
        System.out.println("Encrypted password: " + encryptedPassword);
        System.out.println("Decrypted password: " + decryptedPassword);
    }
}