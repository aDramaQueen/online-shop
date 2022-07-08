package com.acme.onlineshop.security;

import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordEncrypter {

    /**
     * <p>Offers the delegated password encoder for Spring Security as a bean</p>
     *
     * @return Password encoder
     * @see <a href="https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html#authentication-password-storage-configuration">Spring Delegated Pasword Encoder</a>
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return buildArgon2Encrypter();
    }

    /**
     * <p>Bcrypt password encrypter</p>
     * <br>
     * @see BCryptPasswordEncoder
     * @see <a href="https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html#authentication-password-storage-bcrypt">Spring Pasword Encoder - BCrypt</a>
     */
    private BCryptPasswordEncoder buildBCryptEncrypter() {
        int strength = 10;

        return new BCryptPasswordEncoder(strength);
    }

    /**
     * <p>Argon2 password encrypter</p>
     * <br>
     * @see Argon2PasswordEncoder
     * @see <a href="https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html#authentication-password-storage-argon2">Spring Pasword Encoder - Argon2</a>
     */
    private Argon2PasswordEncoder buildArgon2Encrypter() {
        int saltLength = 16;    // salt length in bytes
        int hashLength = 32;    // hash length in bytes
        int parallelism = 1;    // more than 1, currently not supported by Spring Security
        int memory = 4096;      // memory costs
        int iterations = 3;

        return new Argon2PasswordEncoder(saltLength, hashLength, parallelism, memory, iterations);
    }

}
