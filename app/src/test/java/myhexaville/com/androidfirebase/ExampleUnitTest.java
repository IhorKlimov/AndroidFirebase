package myhexaville.com.androidfirebase;

import org.junit.Test;

import static myhexaville.com.androidfirebase.Constants.NAMES;
import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        for (int i = 0; i < 1000; i++) {
            System.out.println(NAMES[(int) (Math.random() * NAMES.length)]);
        }

        assertEquals(4, 2 + 2);
    }
}