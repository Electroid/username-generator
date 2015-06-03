package me.electroid.nicknamer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.Random;

/**
* A username generator that mutates strings into minecraft usernames.
* @author ElectroidFilms
*
*/
public class MinecraftNameGenerator {

    /** Mutation number constants. */
    private static final int MAX_MUTATIONS = 3;
    private static final int MAX_NUMBERS = 4;
    private static final int MAX_YEAR_RANGE = 15;
    private static final int MAX_UNDERSCORES = 2;
    private static final int MAX_CAPITALIZED = 3;
    private static final int MAX_PHOENETIC_REPLACEMENTS = 1;

    /** String comparison constants. */
    private static final String NUMBER = "\\d";
    private static final String UPPERCASE_LETTER = "[A-Z]";
    private static final String UNDERSCORE = "_";

    /** Verification variables. */
    private static final String MOJANG_URL = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String LETTER_REGEX = "[a-zA-Z0-9_]";
    private static final String USERNAME_REGEX = "[a-zA-Z0-9_]{1,16}";
    private static final int MAX_USERNAME_LENGTH = 16;

    private final int minNameLength;

    /**
    * Create a new minecraft name generator.
    * @param minNameLength The minimum characters allowed.
    */
    public MinecraftNameGenerator(int minNameLength) {
        this.minNameLength = minNameLength;
    }

    /**
    * Generate a username based on the seed string provided.
    * @param seed The base for generating the username.
    * @return The new username.
    */
    public String generate(String seed) {
        String username = scrambleNumbers(seed);
        int amount = random(MAX_MUTATIONS - 1) + 1;
        for (int i = 0; i <= amount; i++) {
            int action = random(8);
            switch (action) {
                default:
                case  0: username = addNumbers(username);
                case  1: username = addYear(username);
                case  2: username = addRandomUnderscores(username);
                case  3: username = addStrategicalUnderscores(username);
                case  4: username = addLazyUnderscore(username);
                case  5: username = addPhoneticReplacements(username);
                case  6: username = addRandomCapitalization(username);
                case  7: username = addLogicalCapitalization(username);
            }
        }
        /** Recursive methods to ensure valid username. */
        return verifyUsername(username);
    }

    /**
    * Recursive methods to ensure username is always valid.
    * @param seeds The username to verify,
    * @return The verifed username.
    */
    private String verifyUsername(String username) {
        if (username.length() > MAX_USERNAME_LENGTH - 1) {
            verifyUsername(username.substring(0, username.length() * (2 / 3)));
        } else if (username.length() < this.minNameLength) {
            verifyUsername(randomLetter() + username);
        } else if (!Pattern.compile(USERNAME_REGEX, Pattern.CASE_INSENSITIVE).matcher(username).matches()) {
            StringBuilder builder = new StringBuilder();
            for (String character : toChars(username)) {
                if (character.matches(LETTER_REGEX)) {
                    builder.append(character);
                }
            }
            verifyUsername(builder.toString());
        } else if (doesAlreadyExist(username)) {
            verifyUsername(randomLetter() + username.substring(1, username.length()));
        }
        return username;
    }

    /**
    * Generate a collection of usernames based on the seed strings provided.
    * @param seeds The bases for generating the collection of usernames.
    * @return The new usernames.
    */
    public Collection<String> bulkGenerate(Collection<String> seeds) {
        Collection<String> usernames = new ArrayList<String>();
        for (String seed : seeds) {
            usernames.add(generate(seed));
        }
        return usernames;
    }

    /**
    * Scrambles the values of numbers in a string.
    * @param string The string to modify.
    * @return The new string with scrambled numbers.
    */
    private String scrambleNumbers(String string) {
        StringBuilder builder = new StringBuilder();
        for (String character : toChars(string)) {
            if (character.matches(NUMBER)) {
                character = String.valueOf(random());
            }
            builder.append(character);
        }
        return builder.toString();
    }

    /**
    * Clear all numbers in the string.
    * @param string The string to modify.
    * @return The string without numbers.
    */
    private String clearNumbers(String string) {
        StringBuilder builder = new StringBuilder();
        for (String character : toChars(string)) {
            if (!character.matches(NUMBER)) {
                builder.append(character);
            }
        }
        return builder.toString();
    }

    /**
    * Add numbers to the front or back of a string.
    * @param string The string to modify.
    * @return The new string with numbers.
    */
    private String addNumbers(String string) {
        string = clearNumbers(string);
        boolean front = randomBoolean();
        int length = random(MAX_NUMBERS - 1) + 1;
        String numbers = "";
        for (int i = 0; i <= length; i++) {
            numbers += String.valueOf(random());
        }
        if (front) {
            return numbers += string;
        } else {
            return string += numbers;
        }
    }

    /**
    * Adds a year (ie. 2015) to the end of the string.
    * @param string The string to modify.
    * @return The new string with the year.
    */
    private String addYear(String string) {
        string = clearNumbers(string);
        StringBuilder builder = new StringBuilder();
        final Calendar now = Calendar.getInstance();
        final int year = now.get(Calendar.YEAR);
        int range = year - MAX_YEAR_RANGE;
        for (String character : toChars(string)) {
            if (!character.matches(NUMBER)) {
                builder.append(character);
            }
        }
        return builder.toString() + randomInRange(range, year);
    }

    /**
    * Clear any underscores from the string.
    * @param string The string to modify.
    * @return The string without any underscores.
    */
    private String clearUnderscores(String string) {
        StringBuilder builder = new StringBuilder();
        for (String character : toChars(string)) {
            if (!character.matches(UNDERSCORE)) {
                builder.append(character);
            }
        }
        return builder.toString();
    }

    /**
    * Reduce the amount of underscores in the string.
    * @param string The string to modify.
    * @return The new string.
    */
    private String reduceUnderscores(String string) {
        int count = 0;
        for (String character : toChars(string)) {
            if (character.matches(UNDERSCORE)) {
                count++;
            }
        }
        if (count > MAX_UNDERSCORES) {
            return addStrategicalUnderscores(clearUnderscores(string));
        }
        return string;
    }

    /**
    * Add underscores to the string at random indexes.
    * @param string The string to modify.
    * @return The new string with random indexed underscores.
    */
    private String addRandomUnderscores(String string) {
        string = clearUnderscores(string);
        for (int c = 1; c <= random(MAX_UNDERSCORES - 1) + 1; c++) {
            StringBuilder builder = new StringBuilder();
            String[] characters = toChars(string);
            int index = randomIndex(string);
            for (int i = 0; i < string.length(); i++) {
                if (i == index) {
                    if (randomBoolean()) {
                        builder.append(characters[i] + UNDERSCORE);
                    } else {
                        builder.append(UNDERSCORE + characters[i]);
                    }
                } else {
                    builder.append(characters[i]);
                }
            }
            string = builder.toString();
        }
        return reduceUnderscores(string);
    }

    /**
    * Add underscores at stragical locations. (ie. before/after words)
    * @param string The string to modify.
    * @return The new string with strategic underscores.
    */
    private String addStrategicalUnderscores(String string) {
        string = clearUnderscores(string);
        StringBuilder builder = new StringBuilder();
        if (countChars(string, UPPERCASE_LETTER) >= 2) {
            /** Arbitrary assumption that each word starts with an uppercase letter. */
            for (String word : string.split("(?=\\p{Upper})")) {
                if (randomBoolean()) {
                    builder.append(word + UNDERSCORE);
                } else {
                    builder.append(word);
                }
            }
            return reduceUnderscores(builder.toString());
        } else {
            /** Failsafe if no capital letters. */
            return addRandomUnderscores(string);
        }
    }

    /**
    * Add a underscore either directly in the front, or in the back of a string.
    * @param string The string to modify.
    * @return The new string with the lazy underscore.
    */
    private String addLazyUnderscore(String string) {
        string = clearUnderscores(string);
        if (randomBoolean()) {
            return reduceUnderscores(string + UNDERSCORE);
        } else {
            return reduceUnderscores(UNDERSCORE + string);
        }
    }

    /**
    * Add common phoenetic replacements to the string.
    * @param string The string to modify.
    * @return The new string with phoenetic replacements.
    */
    private String addPhoneticReplacements(String string) {
        Map<String, String> map = new HashMap<String, String>() {
            private static final long serialVersionUID = 3061300641616454089L;
            {
                put("0","O"); put("S","Z"); put("1","I"); put("3","E");
            }};
            int count = 0;
            StringBuilder builder = new StringBuilder();
            for (String character : toChars(string)) {
                boolean upperCase = randomBoolean();
                if (map.containsKey(character)) {
                    if (count <= MAX_PHOENETIC_REPLACEMENTS) {
                        if (upperCase) {
                            builder.append(map.get(character).toUpperCase());
                        } else {
                            builder.append(map.get(character).toLowerCase());
                        }
                        count++;
                    }
                } else if (map.containsValue(character)) {
                    for (Map.Entry<String, String> entry : map.entrySet()) {
                        if (entry.getValue().equals(character)) {
                            if (count <= MAX_PHOENETIC_REPLACEMENTS) {
                                if (upperCase) {
                                    builder.append(entry.getKey().toUpperCase());
                                } else {
                                    builder.append(entry.getKey().toLowerCase());
                                }
                                count++;
                            }
                        }
                    }
                } else {
                    builder.append(character);
                }
            }
            return builder.toString();
        }

    /**
    * Reduce the amount of capitalization in the string.
    * @param string The string to modify.
    * @return The new string.
    */
    private String reduceCapitalization(String string) {
        int count = 0;
        for (String character : toChars(string)) {
            if (character.matches(UPPERCASE_LETTER)) {
                count++;
            }
        }
        if (count > MAX_CAPITALIZED) {
            return addLogicalCapitalization(string.toLowerCase());
        }
        return string;
    }

    /**
    * Add random indexed capitalization to the string.
    * @param string The string to modify.
    * @return The new string.
    */
    private String addRandomCapitalization(String string) {
        int amount = random(MAX_CAPITALIZED - 1) + 1;
        StringBuilder builder = new StringBuilder();
        String[] characters = toChars(string);
        for (int i = 1; i <= amount; i++) {
            int index = random(string.length() - 1);
            characters[index] = characters[index].toUpperCase();
        }
        for (String character : characters) {
            builder.append(character);
        }
        return reduceCapitalization(builder.toString());
    }

    /**
    * Add semi-random capitalization to the string.
    * @param string The string to modify.
    * @return The new string.
    */
    private String addLogicalCapitalization(String string) {
        StringBuilder builder = new StringBuilder();
        String[] characters = toChars(string);
        boolean addThird = randomBoolean();
        int firstIndex = 0;
        int secondIndex = randomIndex(string);
        int thirdIndex = randomInRange(secondIndex, string.length() - 1);
        for (int i = 0; i < string.length(); i++) {
            if (i == firstIndex || i == secondIndex) {
                builder.append(characters[i].toUpperCase());
            } else if (addThird && i == thirdIndex) {
                builder.append(characters[i].toUpperCase());
            } else {
                builder.append(characters[i]);
            }
        }
        return reduceCapitalization(builder.toString());
    }

    /**
    * Verify that a username is not registered in Mojang's database.
    * @param name The username to verify.
    * @return If the username is unique and not taken.
    */
    private boolean doesAlreadyExist(String name) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new URL(MOJANG_URL + name).openStream()));
            String data = in.readLine();
            in.close();
            if (data == null) {
                return false;
            } else {
                return true;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return true;
        }
    }

    /**
    * Return a random integer from 0-9.
    * @return The random integer.
    */
    private int random() {
        return new Random().nextInt(10);
    }


    /**
    * Return a random integer given a range starting from 0.
    * @param range The range of the random query.
    * @return The random integer.
    */
    private int random(int range) {
        if (range != 0) {
            return new Random().nextInt(range);
        }
        else {
            return 0;
        }
    }

    /**
    * Return a random boolean.
    * @return The random boolean.
    */
    private boolean randomBoolean() {
        return new Random().nextBoolean();
    }

    /**
    * Get a random index given a string.
    * @param string The string to get the indexes from.
    * @return An integer index.
    */
    private int randomIndex(String string) {
        return random(string.length());
    }

    /**
    * Get a random number within a range.
    * @param start The start of the range.
    * @param end The end of the range.
    * @return An integer index.
    */
    private int randomInRange(int start, int end) {
        return new Random().nextInt(end - start) + start;
    }

    /**
    * Break down a string into its characters.
    * @param string The string to break up.
    * @return An array of characters.
    */
    private String[] toChars(String string) {
        return string.split("");
    }

    /**
    * Count the number of characters in a string that match a regex.
    * @param string The string to query.
    * @param regex The regex to match.
    * @return The amount of matches.
    */
    private int countChars(String string, String regex) {
        int count = 0;
        for (String character : toChars(string)) {
            if (character.matches(regex)) {
                count++;
            }
        }
        return count;
    }

    /**
    * Get a random letter from the alphabet.
    * @return A random character from the alphabet.
    */
    public char randomLetter() {
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        int index = randomIndex(alphabet);
        return alphabet.charAt(index);
    }

}
