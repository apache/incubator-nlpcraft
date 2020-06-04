/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nlpcraft.common.util;

import java.util.*;

/**
 * Copied & modified from: https://github.com/peet/hashids.java/blob/master/src/HashidsJava/Hashids.java
 * Copyright (C) Peet Goddard 
 */
public class NCIdGenerator {
    private static final String DEFAULT_ALPHABET = "xcS4F6h89aUbideAI7tkynuopqrXCgTE5GBKHLMjfRsz";
    private static final int[] PRIMES = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43};
    private static final int[] SEPS_INDICES = {0, 4, 8, 12};

    private String alphabet;
    private int minHashLen;

    private String salt = "";

    private ArrayList<Character> seps = new ArrayList<>();
    private ArrayList<Character> guards = new ArrayList<>();

    /**
     *
     */
    NCIdGenerator() {
        this("");
    }

    /**
     *
     * @param salt
     */
    NCIdGenerator(String salt) {
        this(salt, 0);
    }

    /**
     *
     * @param salt
     * @param minHashLen
     */
    NCIdGenerator(String salt, int minHashLen) {
        this(salt, minHashLen, DEFAULT_ALPHABET);
    }

    /**
     *
     * @param salt
     * @param minHashLen
     * @param alphabet
     */
    NCIdGenerator(String salt, int minHashLen, String alphabet) {
        if (alphabet == null || alphabet.trim().isEmpty())
            throw new IllegalArgumentException("Alphabet must not be empty.");

        if (salt != null)
            this.salt = salt;

        if (minHashLen > 0)
            this.minHashLen = minHashLen;

        this.alphabet = join(new LinkedHashSet<>(Arrays.asList(alphabet.split(""))), "");

        if (this.alphabet.length() < 4)
            throw new IllegalArgumentException("Alphabet must contain at least 4 unique characters.");

        for (int prime : PRIMES)
            if (prime < this.alphabet.length()) {
                char c = this.alphabet.charAt(prime - 1);
                seps.add(c);
                this.alphabet = this.alphabet.replace(c, ' ');
            }

        for (int index : SEPS_INDICES)
            if (index < seps.size()) {
                guards.add(seps.get(index));
                seps.remove(index);
            }

        this.alphabet = consistentShuffle(this.alphabet.replaceAll(" ", ""), this.salt);
    }

    /**
     *
     * @param nums
     * @return
     */
    public String encrypt(long... nums) {
        return encode(nums, alphabet, salt, minHashLen);
    }

    /**
     *
     * @param hash
     * @return
     */
    public long[] decrypt(String hash) {
        return decode(hash);
    }

    /**
     *
     * @param nums
     * @param alphabet
     * @param salt
     * @param minHashLen
     * @return
     */
    private String encode(long[] nums, String alphabet, String salt, int minHashLen) {
        StringBuilder ret = new StringBuilder();
        String seps = consistentShuffle(join(this.seps, ""), join(nums, ""));
        char lotteryChar = 0;

        for (int i = 0; i < nums.length; i++) {
            if (i == 0) {
                StringBuilder lotterySalt = new StringBuilder(join(nums, "-"));

                for (long number : nums)
                    lotterySalt.append("-").append((number + 1) * 2);

                String lottery = consistentShuffle(alphabet, lotterySalt.toString());

                lotteryChar = lottery.charAt(0);

                ret.append(lotteryChar);

                alphabet = lotteryChar + alphabet.replaceAll(String.valueOf(lotteryChar), "");
            }

            alphabet = consistentShuffle(alphabet, ((int) lotteryChar & 12345) + salt);

            ret.append(hash(nums[i], alphabet));

            if (i + 1 < nums.length)
                ret.append(seps.charAt((int) ((nums[i] + i) % seps.length())));
        }

        if (ret.length() < minHashLen) {
            int firstIndex = 0;

            for (int i = 0; i < nums.length; i++)
                firstIndex += (i + 1) * nums[i];

            int guardIndex = firstIndex % guards.size();
            char guard = guards.get(guardIndex);

            ret.insert(0, guard);

            if (ret.length() < minHashLen) {
                guardIndex = (guardIndex + ret.length()) % guards.size();
                guard = guards.get(guardIndex);

                ret.append(guard);
            }
        }

        while (ret.length() < minHashLen) {
            long[] padArray = new long[]{alphabet.charAt(1), alphabet.charAt(0)};
            String padLeft = encode(padArray, alphabet, salt, 0);
            String padRight = encode(padArray, alphabet, join(padArray, ""), 0);

            ret = new StringBuilder(padLeft + ret + padRight);

            int excess = ret.length() - minHashLen;

            if (excess > 0)
                ret = new StringBuilder(ret.substring(excess / 2, excess / 2 + minHashLen));

            alphabet = consistentShuffle(alphabet, salt + ret);
        }

        return ret.toString();
    }

    /**
     *
     * @param number
     * @param alphabet
     * @return
     */
    private String hash(long number, String alphabet) {
        StringBuilder hash = new StringBuilder();

        while (number > 0) {
            hash.insert(0, alphabet.charAt((int) (number % alphabet.length())));

            number = number / alphabet.length();
        }

        return hash.toString();
    }

    /**
     *
     * @param hash
     * @param alphabet
     * @return
     */
    private long unhash(String hash, String alphabet) {
        long num = 0;

        for (int i = 0; i < hash.length(); i++) {
            int pos = alphabet.indexOf(hash.charAt(i));

            num += pos * (long) Math.pow(alphabet.length(), hash.length() - i - 1);
        }

        return num;
    }

    /**
     *
     * @param hash
     * @return
     */
    private long[] decode(String hash) {
        List<Long> ret = new ArrayList<>();
        String originalHash = hash;

        if (hash != null && !hash.isEmpty()) {
            String alphabet = "";

            char lotteryChar = 0;

            for (char guard : guards)
                hash = hash.replaceAll(String.valueOf(guard), " ");

            String[] hashSplit = hash.split(" ");

            hash = hashSplit[hashSplit.length == 3 || hashSplit.length == 2 ? 1 : 0];

            for (char sep : seps)
                hash = hash.replaceAll(String.valueOf(sep), " ");

            String[] hashArray = hash.split(" ");

            for (int i = 0; i < hashArray.length; i++) {
                String subHash = hashArray[i];

                if (subHash != null && !subHash.isEmpty() && i == 0) {
                    lotteryChar = hash.charAt(0);

                    subHash = subHash.substring(1);

                    alphabet = lotteryChar + this.alphabet.replaceAll(String.valueOf(lotteryChar), "");
                }

                if (alphabet.length() > 0) {
                    assert subHash != null;
                    
                    alphabet = consistentShuffle(alphabet, ((int) lotteryChar & 12345) + salt);
                    
                    ret.add(unhash(subHash, alphabet));
                }
            }
        }

        long[] nums = longListToPrimitiveArray(ret);

        if (!encrypt(nums).equals(originalHash))
            return new long[0];

        return nums;
    }

    /**
     *
     * @param alphabet
     * @param salt
     * @return
     */
    private static String consistentShuffle(String alphabet, String salt) {
        StringBuilder ret = new StringBuilder();

        if (!alphabet.isEmpty()) {
            List<String> alphabetArr = charArrayToStringList(alphabet.toCharArray());

            if (salt == null || salt.isEmpty())
                salt = new String(new char[]{'\0'});

            int[] sortArr = new int[salt.length()];

            for (int i = 0; i < salt.length(); i++)
                sortArr[i] = salt.charAt(i);

            for (int i = 0; i < sortArr.length; i++) {
                boolean add = true;

                for (int k = i; k != sortArr.length + i - 1; k++) {
                    int nextIndex = (k + 1) % sortArr.length;

                    if (add)
                        sortArr[i] += sortArr[nextIndex] + (k * i);
                    else
                        sortArr[i] -= sortArr[nextIndex];

                    add = !add;
                }

                sortArr[i] = Math.abs(sortArr[i]);
            }

            int i = 0;

            while (alphabetArr.size() > 0) {
                int pos = sortArr[i];

                if (pos >= alphabetArr.size())
                    pos %= alphabetArr.size();

                ret.append(alphabetArr.get(pos));

                alphabetArr.remove(pos);

                i = ++i % sortArr.length;
            }
        }

        return ret.toString();
    }

    /**
     *
     * @return
     */
    public String getSalt() {
        return salt;
    }

    /**
     *
     * @return
     */
    public String getAlphabet() {
        return alphabet;
    }

    /**
     *
     * @return
     */
    public int getMinHashLength() {
        return minHashLen;
    }

    /**
     *
     * @param longs
     * @return
     */
    private static long[] longListToPrimitiveArray(List<Long> longs) {
        long[] longArr = new long[longs.size()];

        int i = 0;

        for (long l : longs)
            longArr[i++] = l;

        return longArr;
    }

    /**
     *
     * @param chars
     * @return
     */
    private static List<String> charArrayToStringList(char[] chars) {
        ArrayList<String> lst = new ArrayList<>(chars.length);

        for (char c : chars)
            lst.add(String.valueOf(c));

        return lst;
    }

    /**
     *
     * @param a
     * @param del
     * @return
     */
    private static String join(long[] a, String del) {
        ArrayList<String> strLst = new ArrayList<>(a.length);

        for (long l : a)
            strLst.add(String.valueOf(l));

        return join(strLst, del);
    }

    /**
     *
     * @param s
     * @param del
     * @return
     */
    private static String join(Collection<?> s, String del) {
        Iterator<?> iter = s.iterator();

        if (iter.hasNext()) {
            StringBuilder builder = new StringBuilder(s.size());

            builder.append(iter.next());

            while (iter.hasNext()) {
                builder.append(del);
                builder.append(iter.next());
            }

            return builder.toString();
        }
        
        return "";
    }
}
