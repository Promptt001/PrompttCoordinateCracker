package io.github.promptt001.coordinatecracker.cracker;

import java.util.Arrays;

/**
 * Row-padded candidate bitset for scan chunks.
 *
 * Each Z row starts on a word boundary, so applying shifted plane rows is a
 * single word update instead of an arbitrary dense-bit range update. Public
 * indexes returned by nextSetBit remain compact row-major candidate indexes
 * so callers do not need to know about the padding words.
 */
final class CandidateMask {
    private final long[] words;
    private final int width;
    private final int depth;
    private final int wordsPerRow;
    private final int bitCount;
    private final long rowTailMask;

    CandidateMask(int bitCount) {
        this(bitCount, bitCount == 0 ? 0 : 1);
    }

    CandidateMask(int width, int depth) {
        if(width < 0 || depth < 0) {
            throw new IllegalArgumentException("Candidate mask dimensions must be non-negative.");
        }
        long bitCountLong = (long) width * (long) depth;
        long wordCountLong = (long) ((width + 63) >>> 6) * (long) depth;
        if(bitCountLong > Integer.MAX_VALUE || wordCountLong > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Candidate mask is too large; reduce radius or increase thread count.");
        }
        this.width = width;
        this.depth = depth;
        this.wordsPerRow = (width + 63) >>> 6;
        this.bitCount = (int) bitCountLong;
        this.words = new long[(int) wordCountLong];
        int tailBits = width & 63;
        this.rowTailMask = tailBits == 0 ? -1L : (1L << tailBits) - 1L;
    }

    void setAll() {
        Arrays.fill(this.words, -1L);
        if(this.wordsPerRow > 0) {
            for(int z = 0; z < this.depth; z++) {
                this.words[rowBase(z) + this.wordsPerRow - 1] &= this.rowTailMask;
            }
        }
    }

    void setWord(int localZ, int wordInRow, long allowedBits, int bitCount) {
        this.words[rowBase(localZ) + wordInRow] = allowedBits & lowBitsMask(bitCount);
    }

    void andWord(int localZ, int wordInRow, long allowedBits, int bitCount) {
        this.words[rowBase(localZ) + wordInRow] &= allowedBits & lowBitsMask(bitCount);
    }

    void andRange(int bitIndex, long allowedBits, int bitCount) {
        long rangeMask = lowBitsMask(bitCount);
        allowedBits &= rangeMask;
        for(int i = 0; i < bitCount && bitIndex + i < this.bitCount; i++) {
            if(((allowedBits >>> i) & 1L) == 0L) {
                clearCompactBit(bitIndex + i);
            }
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
        if(fromIndex >= this.bitCount || this.width == 0 || this.depth == 0) {
            return -1;
        }

        int row = fromIndex / this.width;
        int localX = fromIndex - (row * this.width);
        int wordInRow = localX >>> 6;
        long word = this.words[rowBase(row) + wordInRow] & (-1L << (localX & 63));

        while(row < this.depth) {
            while(wordInRow < this.wordsPerRow) {
                if(word != 0L) {
                    int bit = Long.numberOfTrailingZeros(word);
                    int x = (wordInRow << 6) + bit;
                    if(x < this.width) {
                        return row * this.width + x;
                    }
                    break;
                }
                wordInRow++;
                if(wordInRow < this.wordsPerRow) {
                    word = this.words[rowBase(row) + wordInRow];
                }
            }
            row++;
            if(row >= this.depth) {
                return -1;
            }
            wordInRow = 0;
            word = this.words[rowBase(row)];
        }
        return -1;
    }

    int wordsPerRow() {
        return this.wordsPerRow;
    }

    private int rowBase(int localZ) {
        return localZ * this.wordsPerRow;
    }

    private void clearCompactBit(int compactIndex) {
        int row = compactIndex / this.width;
        int localX = compactIndex - (row * this.width);
        this.words[rowBase(row) + (localX >>> 6)] &= ~(1L << (localX & 63));
    }

    private static long lowBitsMask(int bitCount) {
        return bitCount >= Long.SIZE ? -1L : (1L << bitCount) - 1L;
    }
}
