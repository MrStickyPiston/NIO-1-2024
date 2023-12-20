import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

public class FractionToPAdic {

     static Scanner scanner = new Scanner(System.in);

     public static void main(String[] args) {
          int base = scanner.nextInt();

          BigInteger numerator = scanner.nextBigInteger();
          BigInteger denominator = scanner.nextBigInteger();

          PAdic n = new PAdic(numerator, denominator, base);
          String s = n.toString();

          //System.out.println(s);

          int cut = 0;
          String repeating = "";

          for (int i = 0; i < s.length(); i++){

               String subString = findRepeatingSubstring(s.substring(i));
               int _c = s.split(subString, -1).length-1;

               if (_c >= cut){
                    cut = _c;
                    repeating = subString;
               } else {
                    break;
               }
          }

          String[] a = s.split(repeating, -1);

          if (Objects.equals(a[a.length - 1], "")) {
               System.out.println(repeating);
          } else {
               System.out.printf("%s|%s", repeating, a[a.length - 1]);
          }
     }

     public static String findRepeatingSubstring(String s) {
          int n = s.length();

          for (int len = 1; len <= n / 2; len++) {
               for (int i = 0; i <= n - 2 * len; i++) {
                    String substring1 = s.substring(i, i + len);
                    String substring2 = s.substring(i + len, i + 2 * len);

                    if (substring1.equals(substring2)) {
                         return substring1;
                    }
               }
          }

          return "Null";
     }
}

class Utils {
     public static char toChar(int n){
          if (n >= 10 && n <= 35) {
               return (char) ('a' + (n - 10));
          } else {
               return (char)(n+'0');
          }
     }
}

final class PAdic {

     private static final int len;
     private static final int limit;
     private final int base;
     private final int[] digits;
     private final int order;

     private static enum Operation {
          ADDITION,
          SUBTRACTION,
          MULTIPLICATION,
          DIVISION
     }

     static {
          len = (1 << 8);
          limit = (len / 3) << 1;
     }

     /**
      * Constructs p-adic number from integer value.
      * @param value integer value in base 10.
      * @param base base of the p-adic number.
      *             Notice that base must be a prime number.
      */
     public PAdic(final BigInteger value, final int base) {
          //PAdic.checkForPrime(base);

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
              System.arraycopy(temp.digits, 0, this.digits, 0, PAdic.len);
          }

          int order = 0;

          while (order < PAdic.len && digits[order] == 0) {
               ++order;
          }

          this.order = order < PAdic.len ? order : 0;
     }

     /**
      * Constructs p-adic number from rational fraction.
      * @param numerator numerator of the fraction in base 10. Must be integer value.
      * @param denominator denominator of the fracture in base 10. Denominator must be positive.
      * @param base base of the p-adic number.
      *             Notice that base must be a prime number.
      */
     public PAdic(final BigInteger numerator, final BigInteger denominator, final int base) {

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
      * @param base base of p-adic number.
      *             Notice that base must be a prime number.
      */
     public PAdic(final int[] sequence, final int order, final int base) {
          this(sequence, order, base, true);
     }

     private PAdic(final int[] sequence, final int order, final int base, final boolean recalculateSequence) {

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
               } else if (order < 0) {
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

     private PAdic subtract(final PAdic subtracted, final int offset) {
          PAdic.checkForBaseEquality(this, subtracted);

          final int[] result = new int[PAdic.len];
          boolean takeOne;

          if (offset >= 0) System.arraycopy(digits, 0, result, 0, offset);

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
               result.append(Utils.toChar(digits[i])).append(suffix);
          }

          if (order < 0) {
               if (!oneDigitBase && result.length() > 0) {
                    result.delete(result.length() - 1, result.length());
               }
               result.append('.');
          }

          for (int i = Math.abs(order) - 1; i >= 0; --i) {
               result.append(Utils.toChar(digits[i])).append(suffix);
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

          return result.toString().replace("_", "");
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
