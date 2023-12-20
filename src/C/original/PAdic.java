/*
The MIT License (MIT)

Copyright (c) 2014 Vladislav Kaverin

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package C.original;

import java.math.BigInteger;
import java.util.Arrays;

public final class PAdic{

    private static final int len;
    private static final int limit;
    private static final boolean[] isPrime;
    private static int precalculatedPrimes;
    private final int base;
    private final int digits[];
    private final int order;

    private static enum Operation {
        ADDITION,
        SUBTRACTION,
        MULTIPLICATION,
        DIVISION
    }

    static {
        len = (1 << 7);
        limit = (len / 3) << 1;
        precalculatedPrimes = (1 << 16);
        isPrime = new boolean[precalculatedPrimes];
        doEratostheneSieve();
    }

    /**
     * Constructs p-adic number from integer value.
     * @param value integer value in base 10.
     * @param base base of the p-adic number.
     *             Notice that base must be a prime number.
     */
    public PAdic(final BigInteger value, final int base) {
        PAdic.checkForPrime(base);
    
        this.digits = new int[PAdic.len];
        this.base = base;

        final BigInteger bigBase = new BigInteger("" + base);

        final boolean isNegative = (value.signum() < 0);
        BigInteger current = value.abs();
        int pos = 0;

        while (!BigInteger.ZERO.equals(current)) {
            digits[pos] = (int) (current.mod(bigBase).longValue());
            current = current.divide(bigBase);
            ++pos;
        }

        if (isNegative) {
            final PAdic temp = this.negative();
            for (int i = 0; i < PAdic.len; ++i) {
                this.digits[i] = temp.digits[i];
            }
        }

        int order = 0;

        while (order < PAdic.len && digits[order] == 0) {
            ++order;
        }

        this.order = order < PAdic.len ? order : 0;
    }

    /**
     * Constructs p-adic number from its string representation in canonical form.
     * Indexes increase from right to left.
     * For example, 1234 means 1 * (p^3) + 2 * (p^2) + 3 * (p^1) + 4 * (p^0).
     * This method is acceptable only in case when base is less or equal to 7.
     * @param number string that represents p-adic number.
     *              It can be either integer value or floating point value.
     *              Notice that point can be defined by '.' symbol only.
     * @param base base of the p-adic number.
     *             Notice that base must be a prime number.
     */
    public PAdic(final String number, final int base) {
        if (base > 7) {
            throw new RuntimeException("Sorry, it is impossible to determine what number it is in canonical form if base is larger than 7. \n Use rational fraction or sequence representation.");
        }

        PAdic.checkForPrime(base);
            
        this.digits = new int[PAdic.len];
        this.base = base;

        final String value = number.trim();

        final int pointAt = value.lastIndexOf('.');
        int posInString = value.length() - 1;
        int posInDigits = 0;

        while (posInString >= 0) {

            if (posInString == pointAt) {
                --posInString;
                continue;
            }

            if (!Character.isDigit(value.charAt(posInString))) {
                throw new RuntimeException("There can be only digits in the number, no letters or special symbols except of one floating point");
            } else if (value.charAt(posInString) - '0' >= this.base) {
                throw new RuntimeException("P-adic number cannot contain digits that are greater or equal to base.");
            }

            digits[posInDigits] = value.charAt(posInString) - '0';

            --posInString;
            ++posInDigits;
        }

        int pos = 0;

        while (pos < PAdic.len && digits[pos] == 0) {
            ++pos;
        }

        int order;

        if (pointAt != -1 ) {
            order = -(value.length() - pointAt - 1);

            if (order < 0 ) {
                final int offset = Math.min(-order, pos);
                for (int i = 0; i + offset < PAdic.len; ++i) {
                    digits[i] = digits[i + offset];
                }

                order += offset;
            }
        } else {
            if (pos == PAdic.len) {
                pos = 0;
            }

            order = pos;
        }

        this.order = order;
    }

    /**
     * Constructs p-adic number from rational fraction.
     * @param numerator numerator of the fraction in base 10. Must be integer value.
     * @param denominator denominator of the fracture in base 10. Denominator must be positive.
     * @param base base of the p-adic number.
     *             Notice that base must be a prime number.
     */
    public PAdic(final BigInteger numerator, final BigInteger denominator, final int base) {
        PAdic.checkForPrime(base);

        final BigInteger gcd = numerator.abs().gcd(denominator.abs());
        final BigInteger actualNumerator = numerator.divide(gcd);
        final BigInteger actualDenominator = denominator.divide(gcd);

        final PAdic pAdicNumerator = new PAdic(actualNumerator, base);
        final PAdic pAdicDenominator = new PAdic(actualDenominator, base);

        final PAdic pAdicResult = pAdicNumerator.divide(pAdicDenominator);

        this.digits = Arrays.copyOfRange(pAdicResult.digits, 0, PAdic.len);
        this.order = pAdicResult.order;
        this.base = pAdicResult.base;
    }

    /**
     * Constructs p-adic number from number sequence.
     * Indexes of coefficients in the sequence go from smaller to bigger.
     * For example, 7-adic number 123.456 can be built as following:
     * <pre>
     *     final int[] sequence = {6, 5, 4, 3, 2 1};
     *     final int order = -3;
     *     final int base = 7;
     *     final PAdic pAdicNumber = new PAdic(sequence, order, base);
     * </pre>
     * In case of inconsistency between sequence and order it will be resolved in way that order wins.
     * For example, if you will try to build number from sequence <code>{0, 0, 0, 0, 0, 1, 2, 3}</code>
     * with order 2 then result will be 32100 and first three zeros won't be taken into account.
     * In the same way you can construct number 32100: its sequence representation must be 1, 2, 3 and order is equal to 2.
     * @param sequence sequence of p-adic digits that p-adic number must be built from.
     *               All the digits must be nonnegative and less than base of the p-adic number.
     * @param order order of the p-adic number.
     * @param base base of of p-adic number.
     *             Notice that base must be a prime number.
     */
    public PAdic(final int[] sequence, final int order, final int base) {
        this(sequence, order, base, true);
    }

    private PAdic(final int[] sequence, final int order, final int base, final boolean recalculateSequence) {
        PAdic.checkForPrime(base);
    
        this.base = base;
        this.digits = new int[PAdic.len];

        int startPosition = 0;
        int startInSequence = 0;

        if (recalculateSequence) {
            int pos = 0;
            while (pos < sequence.length && sequence[pos] == 0) {
                ++pos;
            }

            if (order > 0) {
                startInSequence = pos;
                startPosition = order;
            } else if (order < 0 && pos > order) {
                startPosition = 0;
                startInSequence = pos;
            }
        }

        for (int i = startPosition, posInSequence = startInSequence; posInSequence < sequence.length && i < PAdic.len; ++i, ++posInSequence) {
            if (sequence[posInSequence] < 0) {
                throw new RuntimeException("P-adic number cannot be built from sequence that contains negative numbers.");
            } else if (sequence[posInSequence] >= this.base) {
                throw new RuntimeException("P-adic number cannot be built from sequence that contains digits that are greater or equal to base.");
            }

            this.digits[i] = sequence[posInSequence];
        }
        this.order = order;
    }

    /**
     * Returns value that gives zero in addition with this number.
     * @return number that is opposite to this one.
     */
    public PAdic negative() {
        int pos = 0;
        
        final int[] sequence = new int[PAdic.len];

        while (pos < PAdic.len && this.digits[pos] == 0) {
            ++pos;
        }

        if (pos < PAdic.len) {
            sequence[pos] = base - this.digits[pos];
        }

        for (int i = pos + 1; i < PAdic.len; ++i) {
            sequence[i] = base - this.digits[i] - 1;
        }
        
        return new PAdic(sequence, this.order, this.base);
    }

    /**
     * Returns result of sum this p-adic number with <code>added</code>.
     * @param added p-adic number to be added.
     * @return p-adic number that is result of sum.
     */
    public PAdic add(final PAdic added) {
        PAdic.checkForBaseEquality(this, added);
    
        if (this.getOrder() < 0 || added.getOrder() < 0) {
            final int leftOperandOrder = Math.min(this.getOrder(), 0);
            final int rightOperandOrder = Math.min(added.getOrder(), 0);
            final int diff = leftOperandOrder - rightOperandOrder;
            final int offset = Math.abs(diff);
            return diff < 0 ? this.add(added, offset) : added.add(this, offset);
        }

        return add(added, 0);
    }

    private PAdic add(final PAdic added, int offset) {
        PAdic.checkForBaseEquality(this, added);

        final int[] result = new int[PAdic.len];
        int toNext = 0;

        for (int i = 0; i < offset; ++i) {
            result[i] = digits[i];
        }

        for (int i = 0; i + offset < PAdic.len; ++i) {
            final int next = digits[i + offset] + added.digits[i] + toNext;
            toNext = next / base;
            result[i + offset] = next % base;
        }

        final int order = PAdic.calculateOrder(result, this.getOrder(), added.getOrder(), Operation.ADDITION);

        return new PAdic(result, order, this.base, false);
    }

    /**
     * Returns difference of p-adic number and <code>subtracted</code> value.
     * @param subtracted p-adic number to be subtracted.
     * @return p-adic number that is result of subtraction.
     */
    public PAdic subtract(final PAdic subtracted) {
        PAdic.checkForBaseEquality(this, subtracted);
        
        PAdic actual = null;
        final int[] digits = new int[PAdic.len];
        boolean haveActual = false;

        if (subtracted.getOrder() < 0 && subtracted.getOrder() < this.getOrder()) {
            final int diff = Math.abs(subtracted.getOrder() - Math.min(this.getOrder(), 0));

            for (int i = PAdic.len - 1; i - diff >= 0; --i) {
                final int idx = i - diff;
                digits[i] = this.digits[idx];
            }

            final int newOrder = Math.min(this.getOrder(), 0) - diff;
            actual = new PAdic(digits, newOrder, this.base, false);
            haveActual = true;
        }

        if (!haveActual) {
            actual = (PAdic) this.clone();
        }

        if (actual.getOrder() < 0 && subtracted.getOrder() >= 0) {
                return actual.subtract(subtracted, -actual.getOrder());
        }

        final int offset;
        if (actual.getOrder() < 0 || subtracted.getOrder() < 0) {

            // Need to shift digits in such way that point was exactly under point.
            // Example:
            // _12345.67890  =>  _12345.67890           _      100000   =>    _100000.00000
            //       123.45  =>     123.45000    OR       12345.12345   =>      12345.12345
            //  ^^^^^^^^^^^       ^^^^^^^^^^^             ^^^^^^^^^^^          ^^^^^^^^^^^^

            final int leftOperandOrder = Math.min(actual.getOrder(), 0);
            final int rightOperandOrder = Math.min(subtracted.getOrder(), 0);
            offset = Math.abs(leftOperandOrder - rightOperandOrder);
        } else {
            offset = 0;
        }

        return actual.subtract(subtracted, offset);
    }

    private PAdic subtract(final PAdic subtracted, final int offset) {
        PAdic.checkForBaseEquality(this, subtracted);
        
        final int[] result = new int[PAdic.len];
        boolean takeOne;

        for (int i = 0; i < offset; ++i) {
            result[i] = digits[i];
        }

        for (int i = 0; i + offset < PAdic.len; ++i) {
            final int idx = i + offset;
            if (digits[idx] < subtracted.digits[i]) {
                takeOne = true;
                int j = idx + 1;

                while (j < PAdic.len && takeOne) {
                    if (digits[j] == 0) {
                        digits[j] = base - 1;
                    } else {
                        --digits[j];
                        takeOne = false;
                    }

                    ++j;
                }
                digits[idx] += base;
            }
            result[idx] = digits[idx] - subtracted.digits[i];
        }

        final int order = PAdic.calculateOrder(result, this.getOrder(), subtracted.getOrder(), Operation.SUBTRACTION);

        return new PAdic(result, order, this.base, false);
    }

    /**
     * Returns result of multiplication of this p-adic number by <code>multiplier</code> value.
     * @param multiplier value to multiply this p-adic number by.
     * @return p-adic number that is result of multiplication.
     */
    public PAdic multiply(final PAdic multiplier) {
        PAdic.checkForBaseEquality(this, multiplier);

        PAdic result = new PAdic(new int[] {0}, 0, this.base);

        for (int i = 0; i < PAdic.len; ++i) {
            final int temp[] = multiplyToInteger(digits, multiplier.digits[i]);
            final PAdic adder = new PAdic(temp, 0, this.base, false);
            result = result.add(adder, i);
        }

        // In some cases when we multiply numbers, it may happens that
        // real index of the most right non-zero coefficient gets greater than it must be.
        // For example, multiplying ...00000.1 (order = -1)  by ...000010 (order = 1)
        // we will get ...0000010 (order = 1) that's incorrect, because its order must be zero.
        // So, the order calculated correctly, but we need to shift the result a little to the right.

        final int minOrder = Math.min(this.getOrder(), multiplier.getOrder());
        final int maxOrder = Math.max(this.getOrder(), multiplier.getOrder());

        if (minOrder < 0 && 0 < maxOrder) {
            int pos = 0;

            while (pos < -minOrder && result.digits[pos] == 0) {
                ++pos;
            }

            for (int i = 0; i + pos < PAdic.len; ++i) {
                result.digits[i] = result.digits[i + pos];
            }
        }

        final int order = PAdic.calculateOrder(result.digits, this.getOrder(), multiplier.getOrder(), Operation.MULTIPLICATION);

        return new PAdic(result.digits, order, this.base, false);
    }

    /**
     * Returns result of division of this p-adic number by <code>divisor</code> value.
     * @param divisor value to divide this p-adic number by.
     * @return p-adic number that is result of division.
     */
    public PAdic divide(final PAdic divisor) {
        PAdic.checkForBaseEquality(this, divisor);
        
        final int[] result = new int[PAdic.len];
        final int[] dividedDigits = new int[PAdic.len];
        final int[] divisorDigits= new int[PAdic.len];

        int pos = 0;

        while (pos < PAdic.len && this.digits[pos] == 0 && divisor.digits[pos] == 0) {
            ++pos;
        }

        for (int i = 0; i + pos < PAdic.len; ++i) {
            dividedDigits[i] = this.digits[i + pos];
            divisorDigits[i] = divisor.digits[i + pos];
        }

        int dividedOrder = this.getOrder() - pos;
        int divisorOrder = divisor.getOrder() - pos;

        pos = 0;
        while (pos < PAdic.len && divisorDigits[pos] == 0) {
            ++pos;
        }

        for (int i = 0; i + pos < PAdic.len; ++i) {
            divisorDigits[i] = divisorDigits[i + pos];
        }
        dividedOrder -= pos;
        divisorOrder -= pos;

        if (divisorOrder < 0 && divisorOrder < dividedOrder) {
            final int diff = Math.min(dividedOrder, 0) - divisorOrder;
            for (int i = PAdic.len - 1; i - diff >= 0; --i) {
                final int idx = i - diff;
                dividedDigits[i] = dividedDigits[idx];
                dividedDigits[idx] = 0;
            }
            dividedOrder += diff;
            divisorOrder = 0;
        }

        PAdic divided = new PAdic(dividedDigits, 0, this.base, false);
        final PAdic actualDivisor = new PAdic(divisorDigits, 0, this.base);

        for (int i = 0; i < PAdic.len; ++i) {
            final int digit = findMultiplier(divided.digits[i], actualDivisor.digits[0]);

            if (digit == -1) {
                throw new RuntimeException("CALCULATION FAILED. Couldn't find multiplier x satisfying " + divided.digits[i] + " = x" + actualDivisor.digits[0] + " (mod " + this.base + ").");
            }

            final int[] tmp = multiplyToInteger(actualDivisor.digits, digit);

            result[i] = digit;
            divided = divided.subtract(new PAdic(tmp, 0, this.base, false), i);
        }

        final int order = PAdic.calculateOrder(result, dividedOrder, divisorOrder, Operation.DIVISION);

        return new PAdic(result, order, this.base, false);
    }

    /**
     * Returns order of p-adic number.
     * Order of p-adic number is maximal power <i>n</i> of <i>p</i> so <i>p</i> in power <i>n</i> divides the number.
     * @return order of the number.
     */
    public int getOrder() {
        return order;
    }

    private static int calculateOrder(final int[] digits, final int firstOrder, final int secondOrder, Operation operation) {
        int order = 0;

        if (operation == Operation.ADDITION || operation == Operation.SUBTRACTION){
            order = Math.min(firstOrder, secondOrder);
        }

        if (operation == Operation.MULTIPLICATION) {
            order = firstOrder + secondOrder;
        }

        if (operation == Operation.DIVISION) {
            order = firstOrder - secondOrder;
        }

        int pos = 0;

        while (pos < PAdic.len && digits[pos] == 0) {
            ++pos;
        }

        if (pos == PAdic.len) {
            return 0;
        }

        if (0 <= order && order < pos) {
            order = pos;
        }

        if (order < 0) {
            final int min = Math.min(-order, pos);
            for (int i = 0; i + min < PAdic.len; ++i) {
                digits[i] = digits[i + min];
            }
            order += pos;
        }

        return order;
    }

    private int[] multiplyToInteger(final int[] number, final int multiplier) {
        int toNext = 0;

        final int[] result = new int[PAdic.len];
        for (int i = 0; i < number.length; ++i) {
            final int next = number[i] * multiplier + toNext;
            toNext = next / base;
            result[i] = next % base;
        }

        return result;
    }

    private int findMultiplier(final int mod, final int multiplier) {
        for (int i = 0; i < base; ++i) {
            if ((multiplier * i) % base == mod) {
                return i;
            }
        }

        return -1;
    }

    private static void doEratostheneSieve() {
        Arrays.fill(isPrime, true);
        isPrime[0] = isPrime[1] = false;
    
        for (int i = 2; i * i < precalculatedPrimes; ++i) {
            if (!isPrime[i]) {
                continue;
            }
            
            for (int j = i + i; j < precalculatedPrimes; j += i) {
                isPrime[j] = false;
            }
        }
    }
    
    static void checkForPrime(final int base) {
        if (!(base < precalculatedPrimes)) {
            throw new RuntimeException("Sorry, " + base + " is too large number to be a base and I cannot be sure that it's prime. Enter a prime number that is less than " + precalculatedPrimes + ".");
        }

        final boolean isPrimeBase = base > 1 && isPrime[base];

        if (!isPrimeBase) {
            throw new RuntimeException("Base " + base + " is not prime. Base must be a prime number.");
        }
    }

    private static void checkForBaseEquality(final PAdic first, final PAdic second) {
        final boolean areEqual = (first.base == second.base);
        
        if (!areEqual) {
            throw new RuntimeException("Mathematical operations can be done only with p-adic numbers that have the same base.");
        }
    }

    @Override
    protected Object clone(){
        return new PAdic(this.digits, this.order, this.base, false);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(PAdic.len);
        final boolean oneDigitBase = base <= 7;
        int pos = PAdic.limit - 1;

        while (pos >= 0 && digits[pos] == 0) {
            --pos;
        }

        if (pos == -1) {
            ++pos;
        }

        String suffix = !oneDigitBase ? "_" : "";

        for (int i = pos; i >= Math.abs(order); --i) {
            result.append(digits[i] + suffix);
        }

        if (order < 0) {
            if (!oneDigitBase && result.length() > 0) {
                result.delete(result.length() - 1, result.length());
            }
            result.append('.');
        }

        for (int i = Math.abs(order) - 1; i >= 0; --i) {
            result.append(digits[i] + suffix);
        }

        if (result.charAt(0) == '.') {
            result.insert(0, "0");
        }

        if (!result.toString().startsWith("0.")) {
            pos = 0;

            while (pos < result.length() && result.charAt(pos) == '0') {
                ++pos;
            }

            if (pos == result.length()) {
                --pos;
            }

            result.delete(0, pos);
        }

        if (!oneDigitBase) {
            result.delete(result.length() - 1, result.length());
        }

        return result.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        if (!(obj instanceof PAdic)) {
            return false;
        }

        PAdic number = (PAdic) obj;

        if (this.base != number.base) {
            return false;
        }

        if (this.order != number.order) {
            return false;
        }

        for (int i = 0; i < PAdic.limit; ++i) {
            if (this.digits[i] != number.digits[i]) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        final int prime = 31;

        for (int i = 0; i < PAdic.limit; ++i) {
            hash = hash * prime + digits[i];
        }

        hash = hash * prime + order;
        hash = hash * prime + base;

        return hash;
    }
}
