package com.gdxsoft.easyweb.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests for UObjectValue.convert() — especially the String → ArrayList/List conversion
 * added to fix repeated "Cannot cast java.lang.String to java.util.ArrayList" INFO logs.
 */
public class UObjectValueTest {

    // ======================== Target POJOs for integration tests ========================

    /** POJO with List setter — used to test full setValueAccurate flow */
    public static class PojoWithList {
        private List<String> tags;

        public void setTags(List<String> tags) {
            this.tags = tags;
        }

        public List<String> getTags() {
            return tags;
        }
    }

    /** POJO with ArrayList setter */
    public static class PojoWithArrayList {
        private ArrayList<String> items;

        public void setItems(ArrayList<String> items) {
            this.items = items;
        }

        public ArrayList<String> getItems() {
            return items;
        }
    }

    /** POJO with a String setter — used to verify non-List types are unaffected */
    public static class PojoWithString {
        private String name;

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    // ======================== Static convert() method tests ========================

    @Test
    public void testConvertStringToArrayList() {
        Object result = UObjectValue.convert(ArrayList.class, "a,b,c");
        assertNotNull(result);
        assertTrue(result instanceof ArrayList, "Result should be ArrayList");
        ArrayList<?> list = (ArrayList<?>) result;
        assertEquals(3, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
        assertEquals("c", list.get(2));
    }

    @Test
    public void testConvertStringToArrayListWithSpaces() {
        Object result = UObjectValue.convert(ArrayList.class, " a , b , c ");
        assertNotNull(result);
        assertTrue(result instanceof ArrayList);
        ArrayList<?> list = (ArrayList<?>) result;
        assertEquals(3, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
        assertEquals("c", list.get(2));
    }

    @Test
    public void testConvertSingleValueToList() {
        Object result = UObjectValue.convert(ArrayList.class, "hello");
        assertNotNull(result);
        assertTrue(result instanceof ArrayList);
        ArrayList<?> list = (ArrayList<?>) result;
        assertEquals(1, list.size());
        assertEquals("hello", list.get(0));
    }

    @Test
    public void testConvertEmptyStringToList() {
        Object result = UObjectValue.convert(ArrayList.class, "");
        assertNotNull(result);
        assertTrue(result instanceof ArrayList);
        ArrayList<?> list = (ArrayList<?>) result;
        assertEquals(0, list.size());
    }

    @Test
    public void testConvertBlankStringToList() {
        Object result = UObjectValue.convert(ArrayList.class, "   ");
        assertNotNull(result);
        assertTrue(result instanceof ArrayList);
        ArrayList<?> list = (ArrayList<?>) result;
        assertEquals(0, list.size());
    }

    @Test
    public void testJavaObjectRefNotConverted() {
        // Java default toString() format contains '@' — should NOT be converted,
        // falls through to t.cast() which returns CAST_ERROR
        String objectRef = "[com.gdxsoft.easyweb.define.database.Field@4eeac485, "
                + "com.gdxsoft.easyweb.define.database.Field@4e022a8e]";
        Object result = UObjectValue.convert(ArrayList.class, objectRef);

        assertEquals(UObjectValue.CAST_ERROR, result,
                "Java object reference should NOT be converted to list");
    }

    @Test
    public void testEmailAddressNotConverted() {
        // Email addresses contain '@' — should NOT be auto-converted
        String emails = "user@example.com, admin@test.org";
        Object result = UObjectValue.convert(ArrayList.class, emails);

        assertEquals(UObjectValue.CAST_ERROR, result,
                "Email addresses containing '@' should NOT be auto-converted");
    }

    @Test
    public void testConvertToListInterface() {
        Object result = UObjectValue.convert(List.class, "x,y,z");
        assertNotNull(result);
        assertTrue(result instanceof List);
        List<?> list = (List<?>) result;
        assertEquals(3, list.size());
        assertEquals("x", list.get(0));
        assertEquals("y", list.get(1));
        assertEquals("z", list.get(2));
    }

    @Test
    public void testNonStringSourceFallsThrough() {
        // If source is already an ArrayList, it falls through to t.cast()
        ArrayList<String> src = new ArrayList<>();
        src.add("existing");
        Object result = UObjectValue.convert(ArrayList.class, src);
        assertNotNull(result);
        assertTrue(result instanceof ArrayList);
        assertEquals(1, ((ArrayList<?>) result).size());
    }

    @Test
    public void testNonListTypeUnaffected() {
        // String → String conversion should still work normally
        Object result = UObjectValue.convert(String.class, "hello");
        assertEquals("hello", result, "String→String conversion should be unaffected");
    }

    @Test
    public void testArrayTypeUnaffected() {
        // String → String[] conversion should still work (existing behavior)
        Object result = UObjectValue.convert(String[].class, "a,b,c");
        assertNotNull(result);
        assertTrue(result instanceof String[]);
        String[] arr = (String[]) result;
        assertEquals(3, arr.length);
        assertEquals("a", arr[0]);
        assertEquals("b", arr[1]);
        assertEquals("c", arr[2]);
    }

    // ======================== Integration tests via setValueAccurate ========================

    @Test
    public void testSetValueAccurateWithListField() {
        PojoWithList pojo = new PojoWithList();
        UObjectValue ov = new UObjectValue();
        ov.setObject(pojo);

        String result = ov.setValueAccurate("setTags", "java, python, go");
        assertNull(result, "setValueAccurate should return null on success");

        List<String> tags = pojo.getTags();
        assertNotNull(tags);
        assertEquals(3, tags.size());
        assertEquals("java", tags.get(0));
        assertEquals("python", tags.get(1));
        assertEquals("go", tags.get(2));
    }

    @Test
    public void testSetValueAccurateWithArrayListField() {
        PojoWithArrayList pojo = new PojoWithArrayList();
        UObjectValue ov = new UObjectValue();
        ov.setObject(pojo);

        String result = ov.setValueAccurate("setItems", "alpha, beta");
        assertNull(result, "setValueAccurate should return null on success");

        ArrayList<String> items = pojo.getItems();
        assertNotNull(items);
        assertEquals(2, items.size());
        assertEquals("alpha", items.get(0));
        assertEquals("beta", items.get(1));
    }

    @Test
    public void testSetValueAccurateWithSingleValueToList() {
        PojoWithList pojo = new PojoWithList();
        UObjectValue ov = new UObjectValue();
        ov.setObject(pojo);

        String result = ov.setValueAccurate("setTags", "only-one");
        assertNull(result);

        List<String> tags = pojo.getTags();
        assertNotNull(tags);
        assertEquals(1, tags.size());
        assertEquals("only-one", tags.get(0));
    }

    @Test
    public void testSetValueAccurateWithEmptyValueToList() {
        PojoWithList pojo = new PojoWithList();
        UObjectValue ov = new UObjectValue();
        ov.setObject(pojo);

        String result = ov.setValueAccurate("setTags", "");
        assertNull(result);

        List<String> tags = pojo.getTags();
        assertNotNull(tags);
        assertEquals(0, tags.size());
    }

    @Test
    public void testSetValueAccurateWithObjectRefNotConverted() {
        PojoWithList pojo = new PojoWithList();
        // Initialize with a known value to verify it's NOT overwritten
        ArrayList<String> original = new ArrayList<>();
        original.add("original");
        pojo.setTags(original);

        UObjectValue ov = new UObjectValue();
        ov.setObject(pojo);

        String result = ov.setValueAccurate("setTags",
                "[com.example.Class@12345, com.example.Class@67890]");
        assertNotNull(result, "setValueAccurate should return error for object ref");
        assertTrue(result.contains(UObjectValue.CAST_ERROR),
                "Should contain CAST_ERROR");

        // The original tags should remain unchanged
        List<String> tags = pojo.getTags();
        assertEquals(1, tags.size());
        assertEquals("original", tags.get(0));
    }

    @Test
    public void testSetValueAccurateWithStringFieldUnaffected() {
        PojoWithString pojo = new PojoWithString();
        UObjectValue ov = new UObjectValue();
        ov.setObject(pojo);

        String result = ov.setValueAccurate("setName", "test-name");
        assertNull(result);
        assertEquals("test-name", pojo.getName());
    }

    @Test
    public void testSetValueAccurateWithNumberAsStringToList() {
        PojoWithList pojo = new PojoWithList();
        UObjectValue ov = new UObjectValue();
        ov.setObject(pojo);

        String result = ov.setValueAccurate("setTags", "1, 2, 3, 4, 5");
        assertNull(result);

        List<String> tags = pojo.getTags();
        assertEquals(5, tags.size());
        assertEquals("1", tags.get(0));
        assertEquals("5", tags.get(4));
    }

    // ======================== Edge cases ========================

    @Test
    public void testConvertCommaOnlyString() {
        // Java String.split(",") discards trailing empty strings
        // (consistent with existing array conversion behavior)
        Object result = UObjectValue.convert(ArrayList.class, ",,");
        assertNotNull(result);
        assertTrue(result instanceof ArrayList);
        ArrayList<?> list = (ArrayList<?>) result;
        assertEquals(0, list.size(), "Trailing empties discarded by split()");
    }

    @Test
    public void testConvertTrailingComma() {
        // Java split(",") discards trailing empty — "a,b," → ["a","b"]
        Object result = UObjectValue.convert(ArrayList.class, "a,b,");
        assertNotNull(result);
        ArrayList<?> list = (ArrayList<?>) result;
        assertEquals(2, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
    }

    @Test
    public void testConvertLeadingCommas() {
        // Leading empty strings are preserved by split() — ",,a,b" → ["","","a","b"]
        Object result = UObjectValue.convert(ArrayList.class, ",,a,b");
        assertNotNull(result);
        ArrayList<?> list = (ArrayList<?>) result;
        assertEquals(4, list.size());
        assertEquals("", list.get(0));
        assertEquals("", list.get(1));
        assertEquals("a", list.get(2));
        assertEquals("b", list.get(3));
    }
}
