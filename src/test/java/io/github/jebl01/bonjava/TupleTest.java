package io.github.jebl01.bonjava;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by jesblo on 31/05/16.
 */
public class TupleTest {
    @Test
    public void canMapTuple2() {
        Tuple.Tuple2<String, Long> stringAndLong = Tuple.of("test", 123L);
        String result = stringAndLong.map(sl -> sl.v1 + ":" + sl.v2);
        assertEquals("test:123", result);
    }

    @Test
    public void canMapTuple2WithBiFunction() {
        Tuple.Tuple2<String, Long> stringAndLong = Tuple.of("test", 123L);
        String result = stringAndLong.map((s, l) -> s + ":" + l);
        assertEquals("test:123", result);
    }

    @Test
    public void canMapTuple5() {
        Tuple.Tuple5<String, String, String, Long, Integer> tuple = Tuple.of("t", "e", "st", 1L, 2);
        String result = tuple.map(t -> t.v1 + t.v2 + t.v3 + t.v4 + t.v5);
        assertEquals("test12", result);
    }

    @Test
    public void canSplit2() {
        Tuple.Tuple2<String, String> expected = Tuple.of("1", "2");
        Assert.assertEquals(expected, Tuple.Tuple2.split("1.2", "\\.", true));
    }

    @Test
    public void canSplit3() {
        Tuple.Tuple3<String, String, String> expected = Tuple.of("1", "2", "3");
        Assert.assertEquals(expected, Tuple.Tuple3.split("1.2.3", "\\.", true));
    }

    @Test
    public void canSplit4() {
        Tuple.Tuple4<String, String, String, String> expected = Tuple.of("1", "2", "3", "4");
        Assert.assertEquals(expected, Tuple.Tuple4.split("1.2.3.4", "\\.", true));
    }

    @Test
    public void canSplit5() {
        Tuple.Tuple5<String, String, String, String, String> expected = Tuple.of("1", "2", "3", "4", "5");
        Assert.assertEquals(expected, Tuple.Tuple5.split("1.2.3.4.5", "\\.", true));
    }

    @Test
    public void canSplit6() {
        Tuple.Tuple6<String, String, String, String, String, String> expected = Tuple.of("1", "2", "3", "4", "5", "6");
        Assert.assertEquals(expected, Tuple.Tuple6.split("1.2.3.4.5.6", "\\.", true));
    }

    @Test
    public void canSplit7() {
        Tuple.Tuple7<String, String, String, String, String, String, String> expected = Tuple.of("1",
                "2",
                "3",
                "4",
                "5",
                "6",
                "7");
        Assert.assertEquals(expected, Tuple.Tuple7.split("1.2.3.4.5.6.7", "\\.", true));
    }

    @Test
    public void canSplit8() {
        Tuple.Tuple8<String, String, String, String, String, String, String, String> expected = Tuple.of("1",
                "2",
                "3",
                "4",
                "5",
                "6",
                "7",
                "8");
        Assert.assertEquals(expected, Tuple.Tuple8.split("1.2.3.4.5.6.7.8", "\\.", true));
    }

    @Test
    public void canSplit9() {
        Tuple.Tuple9<String, String, String, String, String, String, String, String, String> expected = Tuple.of("1",
                "2",
                "3",
                "4",
                "5",
                "6",
                "7",
                "8",
                "9");
        Assert.assertEquals(expected, Tuple.Tuple9.split("1.2.3.4.5.6.7.8.9", "\\.", true));
    }

    @Test
    public void canSplit10() {
        Tuple.Tuple10<String, String, String, String, String, String, String, String, String, String> expected = Tuple.of("1",
                "2",
                "3",
                "4",
                "5",
                "6",
                "7",
                "8",
                "9",
                "10");
        Assert.assertEquals(expected, Tuple.Tuple10.split("1.2.3.4.5.6.7.8.9.10", "\\.", true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void willThrowOnInvalidSplit() {
        Tuple.Tuple3.split("test.value", "\\.", true);
    }

    @Test
    public void willRespectLimit() {
        Tuple.Tuple2<String, String> expected = Tuple.of("test", "value");
        Assert.assertEquals(expected, Tuple.Tuple2.split("test.value.3", "\\.", true));
    }

    @Test
    public void willPreserveWhitespace() {
        Tuple.Tuple3<String, String, String> expected = Tuple.of("test ", "value  ", " 3");
        Assert.assertEquals(expected, Tuple.Tuple3.split("test .value  . 3", "\\.", false));
    }

    @Test
    public void willTrimWhitespace() {
        Tuple.Tuple3<String, String, String> expected = Tuple.of("test", "value", "3");
        Assert.assertEquals(expected, Tuple.Tuple3.split(" test   . value  . 3 ", "\\.", true));
    }
}
