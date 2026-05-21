package io.github.promptt001.coordinatecracker.cracker;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import io.github.promptt001.coordinatecracker.data.EnumRotation;

/**
 * Collects a scan region's matches and serializes them in stable distance order.
 */
final class MatchCollector {
    private final List<MatchRecord> matches = new ArrayList<MatchRecord>();

    void add(int x, int y, int z, EnumRotation viewDirection) {
        this.matches.add(new MatchRecord(x, y, z, viewDirection));
    }

    int size() {
        return this.matches.size();
    }

    String toSortedMatchString() {
        if(this.matches.isEmpty()) {
            return "";
        }

        this.matches.sort(new Comparator<MatchRecord>() {
            @Override
            public int compare(MatchRecord a, MatchRecord b) {
                int byDistance = compareLong(a.distanceSquared(), b.distanceSquared());
                if(byDistance != 0) return byDistance;

                int byAbsX = Math.abs(a.x) - Math.abs(b.x);
                if(byAbsX != 0) return byAbsX;

                int byAbsZ = Math.abs(a.z) - Math.abs(b.z);
                if(byAbsZ != 0) return byAbsZ;

                if(a.y != b.y) return a.y - b.y;
                if(a.x != b.x) return a.x - b.x;
                if(a.z != b.z) return a.z - b.z;
                return a.viewDirection.value - b.viewDirection.value;
            }
        });

        StringBuilder output = new StringBuilder(this.matches.size() * 16);
        for(MatchRecord match : this.matches) {
            appendMatch(output, match.x, match.y, match.z, match.viewDirection);
        }
        return output.toString();
    }

    private static int compareLong(long a, long b) {
        return a < b ? -1 : (a == b ? 0 : 1);
    }

    private static void appendMatch(StringBuilder matchBuffer, int x, int y, int z, EnumRotation viewDirection) {
        matchBuffer.append(x)
            .append(' ')
            .append(y)
            .append(' ')
            .append(z)
            .append(" facing ")
            .append(BruteforceThread.facingLabel(viewDirection))
            .append(System.lineSeparator());
    }

    private static final class MatchRecord {
        final int x;
        final int y;
        final int z;
        final EnumRotation viewDirection;

        MatchRecord(int x, int y, int z, EnumRotation viewDirection) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.viewDirection = viewDirection;
        }

        long distanceSquared() {
            return (long) this.x * (long) this.x + (long) this.z * (long) this.z;
        }
    }
}
