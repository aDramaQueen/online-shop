package com.acme.onlineshop.security;

import org.passay.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Validator for new passwords. Passwords must follow some rules to provide a minimum of security.
 * <ul>
 *     <li>Password must have a minimum length</li>
 *     <li>Password must hold a certain number of upper case characters</li>
 *     <li>Password must hold a certain number of lower case characters</li>
 *     <li>Password must hold a certain number of digits</li>
 *     <li>Password must hold a certain number of special characters</li>
 *     <li>...</li>
 * </ul>
 *
 * @see <a href="http://www.passay.org/reference/">Passay Reference Guide</a>
 */
public class PasswordValidator {

    private List<String> lastMessages = List.of();
    private final org.passay.PasswordValidator validator = buildValidator();

    /**
     * Returns errors from last password test.
     *
     * @return {@link List} of errors as {@link String}
     */
    public List<String> getErrorMessages() {
        return lastMessages;
    }

    /**
     * Validates given password, against a set of rules. If the given password fails the test,
     * the reasons will be saved. This information can be retrieved via {@link #getErrorMessages()}
     *
     * @param password New password, which will be tested
     * @return <code>TRUE</code> if password is strong enough, <code>FALSE</code> otherwise
     */
    public boolean isValid(String password) {
        RuleResult result = validator.validate(new PasswordData(password));
        if (result.isValid()) {
            return true;
        } else {
            lastMessages = validator.getMessages(result);
            return false;
        }
    }

    /**
     * ATTENTION: length must be equal or greater than sum of lower, upper, digits, special
     *
     * @return A random password
     */
    public static String generateValidPassword() {
        PasswordGenerator generator = new PasswordGenerator();
        return generator.generatePassword(14, getCharacterRules(4, 4, 4, 2));
    }

    private static List<CharacterRule> getCharacterRules(int lower, int upper, int digits, int special) {
        return List.of(
                // at least X lower-case characters
                new CharacterRule(GermanCharacterData.LowerCase, lower),
                //new CharacterRule(EnglishCharacterData.LowerCase, lower),

                // at least X upper-case characters
                new CharacterRule(GermanCharacterData.UpperCase, upper),
                //new CharacterRule(EnglishCharacterData.UpperCase, upper),

                // at least X digit characters
                new CharacterRule(EnglishCharacterData.Digit, digits),

                // at least X symbols (special characters)
                new CharacterRule(EnglishCharacterData.Special, special)
        );
    }

    private static org.passay.PasswordValidator buildValidator() {
        List<Rule> rules = new ArrayList<>();
        // length between 8 and 64 characters
        rules.add(new LengthRule(8, 64));
        // define some illegal sequences that will fail when >= 5 chars long
        // alphabetical is of the form 'abcde', numerical is '34567', qwery is 'asdfg'
        // the false parameter indicates that wrapped sequences are allowed; e.g. 'xyzabc'
        rules.add(new IllegalSequenceRule(GermanSequenceData.Alphabetical, 5, false));
        rules.add(new IllegalSequenceRule(EnglishSequenceData.Alphabetical, 5, false));
        rules.add(new IllegalSequenceRule(EnglishSequenceData.Numerical, 5, false));
        rules.add(new IllegalSequenceRule(EnglishSequenceData.USQwerty, 5, false));
        rules.add(new IllegalSequenceRule(EnglishSequenceData.USQwerty, 5, false));
        // no whitespace
        rules.add(new WhitespaceRule());
        rules.addAll(getCharacterRules(1, 1, 1, 1));

        return new org.passay.PasswordValidator(rules);
    }
}
