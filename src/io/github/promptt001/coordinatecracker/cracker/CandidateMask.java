package io.github.promptt001.coordinatecracker.cracker;

import java.util.Arrays;

/**
 * Dense bitset with range-wise constraint application for scan chunks.
 */
final class CandidateMask {
    private final long[] words;
    private final int bitCount;
    private final long tailMask;

    CandidateMask(int bitCount) {
        this.bitCount = bitCount;
        this.words = new long[(bitCount + 63) >>> 6];
        int tailBits = bitCount & 63;
        this.tailMask = tailBits == 0 ? -1L : (1L << tailBits) - 1L;
    }

    void setAll() {
        Arrays.fill(this.words, -1L);
        if(this.words.length > 0) {
            this.words[this.words.length - 1] &= this.tailMask;
        }
    }

    void andRange(int bitIndex, long allowedBits, int bitCount) {
        int wordIndex = bitIndex >>> 6;
        int bitOffset = bitIndex & 63;
        long rangeMask = bitCount == 64 ? -1L : (1L << bitCount) - 1L;
        allowedBits &= rangeMask;

        if(bitOffset == 0) {
            this.words[wordIndex] &= ~rangeMask | allowedBits;
            return;
        }

        int firstBits = Math.min(bitCount, 64 - bitOffset);
        long firstMask = (1L << firstBits) - 1L;
        this.words[wordIndex] &= ~(firstMask << bitOffset) | ((allowedBits & firstMask) << bitOffset);

        int remainingBits = bitCount - firstBits;
        if(remainingBits > 0) {
            long secondMask = (1L << remainingBits) - 1L;
            this.words[wordIndex + 1] &= ~secondMask | ((allowedBits >>> firstBits) & secondMask);
        }
    }

    boolean isEmpty() {
        for(long word : this.words) {
            if(word != 0L) {
                return false;
            }
        }
        return true;
    }

    int nextSetBit(int fromIndex) {
        if(fromIndex >= this.bitCount) {
            return -1;
        }
        int wordIndex = fromIndex >>> 6;
        long word = this.words[wordIndex] & (-1L << (fromIndex & 63));
        while(true) {
            if(word != 0L) {
                int bit = (wordIndex << 6) + Long.numberOfTrailingZeros(word);
                return bit < this.bitCount ? bit : -1;
            }
            wordIndex++;
            if(wordIndex >= this.words.length) {
                return -1;
            }
            word = this.words[wordIndex];
        }
    }
}
