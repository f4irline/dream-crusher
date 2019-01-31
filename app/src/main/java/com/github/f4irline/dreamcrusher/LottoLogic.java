package com.github.f4irline.dreamcrusher;

import java.util.TreeSet;

public class LottoLogic {

    static TreeSet<Integer> randomNumbers;

    /**
     * Generates 7 random numbers for the lotto.
     */
    public static void generateLottoNumbers() {
        randomNumbers = new TreeSet<>();
        for (int i = 0; i < 7; i++) {
            int randomNumber = (int) Math.round(Math.random() * 39) + 1;
            if (!randomNumbers.contains(randomNumber)) {
                randomNumbers.add(randomNumber);
            } else {
                i--;
            }
        }
    }

    /**
     * Checks how many numbers are same in the user selection and the
     * random generated numbers.
     *
     * @param userNumbers the user selection
     * @return amount of the same numbers
     */
    public static int checkNumbers(TreeSet<Integer> userNumbers) {
        int sameNumbers = 0;
        for (int number : userNumbers) {
            if (randomNumbers.contains(number)) {
                sameNumbers++;
            }
        }
        return sameNumbers;
    }

    /**
     * Returns the random numbers. The random numbers could very well be just
     * called straight from the LottoLogic (since it's static), but the purpose here
     * was to get random numbers to the service from here and broadcast them from
     * the service to the main activity.
     *
     * @return the random numbers of the current iteration.
     */
    public static TreeSet<Integer> getRandomNumbers() {
        return randomNumbers;
    }
}
