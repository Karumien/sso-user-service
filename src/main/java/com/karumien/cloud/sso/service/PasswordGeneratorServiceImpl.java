package com.karumien.cloud.sso.service;

import java.security.SecureRandom;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.karumien.cloud.sso.api.model.Policy;

/**
 * Password Generator by Policy definition.
 *
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 3. 10. 2019 17:18:41
 */
@Service
public class PasswordGeneratorServiceImpl implements PasswordGeneratorService {

    @Value(value = "${generator.password.lowercase:abcdefghijklmnopqrstuvwxyz}")
    private String lowercase;

    @Value(value = "${generator.password.uppercase:ABCDEFGJKLMNPRSTUVWXYZ}")
    private String uppercase;

    @Value(value = "${generator.password.numbers:0123456789}")
    private String numbers;

    @Value(value = "${generator.password.specials:'^$?!@%_-:#&'}")
    private String specials;

    @Value(value = "${generator.password.all:'abcdefghijklmnopqrstuvwxyzABCDEFGJKLMNPRSTUVWXYZ0123456789^$?!@%_-:#&'}")
    private String all;

    /**
     * {@inheritDoc}
     */
    @Override
    public String generate(Policy policy) {

        Random random = new SecureRandom();

        StringBuilder password = new StringBuilder();

        insert(password, random, policy.getMinDigits(), numbers);
        insert(password, random, policy.getMinLowerCase(), lowercase);
        insert(password, random, policy.getMinUpperCase(), uppercase);
        insert(password, random, policy.getMinSpecialChars(), specials);

        int minLength = policy.getMinLength() == null ? 6 : policy.getMinLength();
        insert(password, random, minLength - password.length(), all);

        return password.toString();

    }

    private void insert(StringBuilder password, Random random, Integer minDigits, String chars) {
        if (minDigits != null) {
            for (int i = 0; i < minDigits; i++) {
                char newChar = chars.toCharArray()[random.nextInt(chars.length())];
                if (password.length() == 0) {
                    password.append(newChar);
                } else {
                    password.insert(random.nextInt(password.length()), newChar);
                }
            }
        }
    }

}
