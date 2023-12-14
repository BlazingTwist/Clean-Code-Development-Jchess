package jchess.game.server.session;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

public class BucketTreeMapTest {
    private BucketTreeMap<Integer, String> bucketMap;

    @BeforeEach
    public void setup() {
        bucketMap = new BucketTreeMap<>();
        bucketMap.put(1, "A");
        bucketMap.put(2, "B");
        bucketMap.put(2, "C");
    }

    @Test
    public void test_putAndRetrieve() {
        Set<String> bucket1 = bucketMap.get(1);
        Set<String> bucket2 = bucketMap.get(2);

        Assertions.assertEquals(1, bucket1.size(), "Bucket for key '1' should have 1 entry");
        Assertions.assertEquals(2, bucket2.size(), "Bucket for key '2' should have 2 entries");

        Assertions.assertTrue(bucket1.contains("A"), "Bucket for key '1' should contain 'A'");
        Assertions.assertTrue(bucket2.contains("B"), "Bucket for key '2' should contain 'B'");
        Assertions.assertTrue(bucket2.contains("C"), "Bucket for key '2' should contain 'C'");
    }

    @Test
    public void test_removeAndRetrieve() {
        Assertions.assertTrue(bucketMap.remove(1, "A"), "Remove for existing element should return true");
        Assertions.assertNull(bucketMap.get(1), "Empty bucket should be removed");
        Assertions.assertFalse(bucketMap.remove(1, "A"), "Remove for removed element should return false");
        Assertions.assertFalse(bucketMap.remove(2, "D"), "Remove for missing element should return false");

        Assertions.assertTrue(bucketMap.remove(2, "B"), "Remove for existing element should return true");
        Assertions.assertFalse(bucketMap.get(2).contains("B"), "Bucket for key '2' should still exist and contain B");
        Assertions.assertFalse(bucketMap.remove(2, "B"), "Remove for removed element should return false");
    }
}
