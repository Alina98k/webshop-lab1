package com.example.webshop.util;

import org.mindrot.jbcrypt.BCrypt;

public class OneTimeHash {
    public static void main(String[] args) {
        String raw = "123456";
        String hash = BCrypt.hashpw(raw, BCrypt.gensalt(10));
        System.out.println(hash);
    }
}
