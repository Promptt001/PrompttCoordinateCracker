package io.github.promptt001.coordinatecracker.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import io.github.promptt001.coordinatecracker.data.EnumBlockType;
import io.github.promptt001.coordinatecracker.math.Matrix3;
import io.github.promptt001.coordinatecracker.math.Vector3;
import io.github.promptt001.coordinatecracker.utils.MathHelper;

/**
 * Reads and writes the plain-text pattern format used by the Swing editor.
 */
public final class PatternCodec {
    public static final int UNKNOWN_VALUE = 4;

    private PatternCodec() {}

    public static PatternData emptyPattern(int patternSize, int patternCenter) {
        Matrix3 pattern = new Matrix3(new Vector3(patternSize, patternSize, patternSize), new Vector3(patternCenter, patternCenter, patternCenter));
        Matrix3 blockTypes = new Matrix3(new Vector3(patternSize, patternSize, patternSize), new Vector3(patternCenter, patternCenter, patternCenter));
        fillUnknown(pattern, blockTypes, patternSize);
        return new PatternData(pattern, blockTypes);
    }

    public static PatternData load(File file, int patternSize, int patternCenter) throws IOException {
        if(file == null) throw new IllegalArgumentException("Pattern file must not be null.");
        Matrix3 pattern = new Matrix3(new Vector3(patternSize, patternSize, patternSize), new Vector3(patternCenter, patternCenter, patternCenter));
        Matrix3 blockTypes = new Matrix3(new Vector3(patternSize, patternSize, patternSize), new Vector3(patternCenter, patternCenter, patternCenter));
        fillUnknown(pattern, blockTypes, patternSize);

        int currentLayer = 0;
        int currentRow = 0;
        int lineNumber = 0;

        try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String rawLine;
            while((rawLine = reader.readLine()) != null) {
                lineNumber++;
                String line = rawLine.trim();
                if(line.isEmpty() || line.startsWith("#")) continue;

                if(isSeparator(line)) {
                    if(currentRow != patternSize) {
                        throw formatError(lineNumber, "Depth layer " + (currentLayer + 1) + " has " + currentRow + " row(s); expected " + patternSize + " before a separator.");
                    }
                    if(currentLayer >= patternSize - 1) {
                        throw formatError(lineNumber, "Unexpected extra layer separator after depth layer " + (currentLayer + 1) + ".");
                    }
                    currentLayer++;
                    currentRow = 0;
                    continue;
                }

                if(currentLayer >= patternSize) {
                    throw formatError(lineNumber, "Too many depth layers; expected exactly " + patternSize + ".");
                }
                if(currentRow >= patternSize) {
                    throw formatError(lineNumber, "Too many rows in depth layer " + (currentLayer + 1) + "; expected a separator before this row.");
                }

                String[] values = line.split("\\s+");
                if(values.length != patternSize) {
                    throw formatError(lineNumber, "Depth layer " + (currentLayer + 1) + ", row " + (currentRow + 1) + " has " + values.length + " value(s); expected " + patternSize + ".");
                }

                for(int x = 0; x < patternSize; x++) {
                    PatternToken token = parseToken(values[x], lineNumber, x + 1);
                    Vector3 pos = new Vector3(x, currentLayer, currentRow);
                    pattern.setValue(pos, token.rotationValue);
                    blockTypes.setValue(pos, token.blockType.getId());
                }
                currentRow++;
            }
        }

        if(currentLayer != patternSize - 1 || currentRow != patternSize) {
            throw new IllegalArgumentException("Pattern ended after depth layer " + (currentLayer + 1) + " with " + currentRow + " row(s); expected exactly " + patternSize + " layers of " + patternSize + " rows.");
        }

        return new PatternData(pattern, blockTypes);
    }

    public static void save(File file, Matrix3 pattern, Matrix3 blockTypes, int patternSize) throws IOException {
        if(file == null) throw new IllegalArgumentException("Pattern file must not be null.");
        if(pattern == null) throw new IllegalArgumentException("pattern must not be null.");
        if(blockTypes == null) throw new IllegalArgumentException("blockTypes must not be null.");

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("# Pattern token format: block_type:state, where state is 0..3 depending on the selected block profile. Use ? for unknown. Rows match the visible GUI grid.\n");
            writer.write("# Supported block types: " + EnumBlockType.supportedTokenList() + ".\n");
            for(int y = 0; y < patternSize; ++y) {
                for(int z = 0; z < patternSize; ++z) {
                    for(int x = 0; x < patternSize; ++x) {
                        Vector3 pos = new Vector3(x, y, z);
                        int rotationValue = pattern.getValue(pos);
                        EnumBlockType blockType = EnumBlockType.fromId(blockTypes.getValue(pos));
                        String token = rotationValue == UNKNOWN_VALUE ? "?" : blockType.getFileToken() + ":" + rotationValue;
                        writer.write(token);
                        if(x < patternSize - 1) writer.write(" ");
                    }
                    writer.write("\n");
                }
                if(y < patternSize - 1) {
                    writer.write(repeat('-', (patternSize * 4) - 1));
                    writer.write("\n");
                }
            }
        }
    }

    private static void fillUnknown(Matrix3 pattern, Matrix3 blockTypes, int patternSize) {
        for(int y = 0; y < patternSize; ++y) {
            for(int x = 0; x < patternSize; ++x) {
                for(int z = 0; z < patternSize; ++z) {
                    Vector3 pos = new Vector3(x, y, z);
                    pattern.setValue(pos, UNKNOWN_VALUE);
                    blockTypes.setValue(pos, EnumBlockType.DEEPSLATE.getId());
                }
            }
        }
    }

    private static PatternToken parseToken(String rawToken, int lineNumber, int columnNumber) {
        if(rawToken == null) throw formatError(lineNumber, columnNumber, "Empty pattern token.");
        String token = rawToken.trim();
        if(token.isEmpty() || token.equals("?") || token.equalsIgnoreCase("unknown") || token.equals(".")) {
            return new PatternToken(EnumBlockType.DEEPSLATE, UNKNOWN_VALUE);
        }

        // Backward compatibility with the original one-number format. In old
        // saves, deepslate could be recorded as raw model variants 0..3. The
        // current deepslate profile has only two visible states, so migrate 2/3
        // to their visible modulo-two equivalents instead of preserving values
        // that can never match. Value 4 remains unknown.
        if(MathHelper.isInteger(token)) {
            int value = Integer.valueOf(token);
            if(value < 0 || value > UNKNOWN_VALUE) {
                throw formatError(lineNumber, columnNumber, "Numeric pattern values must be 0, 1, 2, 3, or 4.");
            }
            return new PatternToken(EnumBlockType.DEEPSLATE, value == UNKNOWN_VALUE ? UNKNOWN_VALUE : (value & 1));
        }

        String[] pieces = token.split("[:=,/]", 2);
        if(pieces.length != 2) {
            throw formatError(lineNumber, columnNumber, "Invalid token '" + token + "'. Use block_type:rotation, such as deepslate:1, or ? for unknown. Supported block types: " + EnumBlockType.supportedTokenList() + ".");
        }

        EnumBlockType blockType = EnumBlockType.tryFromToken(pieces[0]);
        if(blockType == null) {
            if(EnumBlockType.isRemovedStaticToken(pieces[0])) {
                throw formatError(lineNumber, columnNumber, "Unsupported non-randomized block '" + pieces[0].trim() + "' in token '" + token + "'. Static blocks do not reduce this coordinate search; leave this cell unknown instead.");
            }
            String unsupportedReason = EnumBlockType.unsupportedReasonForToken(pieces[0]);
            if(unsupportedReason != null) {
                throw formatError(lineNumber, columnNumber, unsupportedReason);
            }
            throw formatError(lineNumber, columnNumber, "Unknown block type '" + pieces[0].trim() + "' in token '" + token + "'. Supported block types: " + EnumBlockType.supportedTokenList() + ".");
        }

        if(!MathHelper.isInteger(pieces[1])) {
            throw formatError(lineNumber, columnNumber, "Invalid state in token '" + token + "'. State must be 0, 1, 2, or 3.");
        }

        int rotationValue = Integer.valueOf(pieces[1]);
        if(rotationValue < 0 || rotationValue > 3) {
            throw formatError(lineNumber, columnNumber, "Invalid state in token '" + token + "'. State must be 0, 1, 2, or 3.");
        }
        if(rotationValue >= blockType.getGuiStateCount()) {
            throw formatError(lineNumber, columnNumber, "Invalid state in token '" + token + "'. " + blockType.getDisplayName() + " supports GUI value 0" + (blockType.getGuiStateCount() > 1 ? ".." + (blockType.getGuiStateCount() - 1) : " only") + ".");
        }

        return new PatternToken(blockType, rotationValue);
    }

    private static boolean isSeparator(String line) {
        if(line.isEmpty()) return false;
        for(int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if(c != '-' && c != '=') return false;
        }
        return true;
    }

    private static IllegalArgumentException formatError(int lineNumber, String message) {
        return new IllegalArgumentException("Pattern format error on line " + lineNumber + ": " + message);
    }

    private static IllegalArgumentException formatError(int lineNumber, int columnNumber, String message) {
        return new IllegalArgumentException("Pattern format error on line " + lineNumber + ", token " + columnNumber + ": " + message);
    }

    private static String repeat(char c, int count) {
        StringBuilder builder = new StringBuilder(Math.max(0, count));
        for(int i = 0; i < count; i++) builder.append(c);
        return builder.toString();
    }

    private static final class PatternToken {
        final EnumBlockType blockType;
        final int rotationValue;

        PatternToken(EnumBlockType blockType, int rotationValue) {
            this.blockType = blockType;
            this.rotationValue = rotationValue;
        }
    }
}
