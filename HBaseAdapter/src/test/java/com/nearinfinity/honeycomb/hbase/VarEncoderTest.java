package com.nearinfinity.honeycomb.hbase;

import com.google.common.primitives.UnsignedBytes;
import net.java.quickcheck.Generator;
import net.java.quickcheck.generator.CombinedGenerators;
import net.java.quickcheck.generator.PrimitiveGenerators;
import net.java.quickcheck.generator.distribution.Distribution;
import net.java.quickcheck.generator.iterable.Iterables;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VarEncoderTest {
    // POSITIVE_NORMAL distribution gives more probability to numbers near 0.
    // This is an attempt to get a uniform distribution over the number of
    // significant bytes in the long, instead of a uniform distribution over the
    // range of the long.  It still doesn't do enough; we need an exponential distribution.
    private Generator<Long> uLongGen = PrimitiveGenerators.longs(0, Long.MAX_VALUE, Distribution.POSITIV_NORMAL);

    private Generator<byte[]> bytesGen = CombinedGenerators.byteArrays();

    @Test
    public void testULongEncDec() {
        for (long n : Iterables.toIterable(uLongGen)) {
            Assert.assertEquals(n, VarEncoder.decodeULong(VarEncoder.encodeULong(n)));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testULongEncFailsWhenNeg() {
        VarEncoder.encodeULong(-1);
    }

    @Test
    public void testULongEncSort() {
        List<Long> longs = new ArrayList<Long>();
        List<byte[]> bytes = new ArrayList<byte[]>();

        for (long n : Iterables.toIterable(uLongGen)) {
            longs.add(new Long(n));
            bytes.add(VarEncoder.encodeULong(n));
        }

        Collections.sort(longs);
        Collections.sort(bytes, UnsignedBytes.lexicographicalComparator());

        for (int i = 0; i < longs.size(); i++) {
            long n = longs.get(i);
            byte[] nBytes = bytes.get(i);
            Assert.assertEquals(n, VarEncoder.decodeULong(nBytes));
            Assert.assertArrayEquals(nBytes, VarEncoder.encodeULong(n));
        }
    }

    @Test
    public void testBytesEncDec() {
        for (byte[] b : Iterables.toIterable(bytesGen)) {
            Assert.assertArrayEquals(b, VarEncoder.decodeBytes(VarEncoder.encodeBytes(b)));
        }
    }

    @Test
    public void testBytesEncSort() {
        List<byte[]> bytes = new ArrayList<byte[]>();
        List<byte[]> encodedBytes = new ArrayList<byte[]>();

        for (byte[] b : Iterables.toIterable(bytesGen)) {
            bytes.add(b);
            encodedBytes.add(VarEncoder.encodeBytes(b));
        }

        Collections.sort(bytes, new ByteArrayComparator());
        Collections.sort(encodedBytes, UnsignedBytes.lexicographicalComparator());

        for (int i = 0; i < bytes.size(); i++) {
            byte[] b = bytes.get(i);
            byte[] encodedB = encodedBytes.get(i);
            Assert.assertArrayEquals(b, VarEncoder.decodeBytes(encodedB));
            Assert.assertArrayEquals(encodedB, VarEncoder.encodeBytes(b));
        }
    }
}