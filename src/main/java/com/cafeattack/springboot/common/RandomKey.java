package com.cafeattack.springboot.common;

import java.util.Random;

public class RandomKey {
    public static String createKey() {
        StringBuffer key = new StringBuffer();
        Random random = new Random();

        for (int i = 0; i < 6; i++) {
            key.append(random.nextInt(10));
        }

        return key.toString();
    }
}
