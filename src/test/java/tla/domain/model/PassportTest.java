package tla.domain.model;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

class PassportTest {

    private ObjectMapper mapper;

    @BeforeEach
    public void init() {
        mapper = new ObjectMapper();
    }

    private Passport loadFromFile(String filename) throws Exception {
        return mapper.readValue(
            new File(
                String.format("src/test/resources/sample/passport/%s", filename)
            ),
            Passport.class
        );
    }

    @Test
    void addProperties() {
        Passport p = new Passport();
        p.add("key", new Passport("val1"));
        p.add("key", new Passport("val2"));
        assertEquals(1, p.getProperties().size(), "number of keys in passport should be 1");
        List<Passport> values = p.getProperties().get("key");
        assertEquals(2, values.size(), "number of values provided for passport key 'key' should be 2");
        assertAll("passport node under key 'key' should contain both 'val1' and 'val2'",
            () -> assertEquals("val1", values.get(0).toString()),
            () -> assertEquals("val2", values.get(1).toString())
        );
    }

    /**
     * tests whether the {@link Passport#add} method works on Passport nodes that have
     * been explicitly instantiated as a value-holding leaf, i.e. using the {@link Passport#Passport(String)}
     * constructor. 
     */
    @Test
    void addProperties_refitNodeType() {
        Passport p = new Passport("val");
        p.add("key", new Passport("val1"));
        p.add("key", new Passport("val2"));
        assertEquals(1, p.getProperties().size(), "number of keys in passport should be 1");
        List<Passport> values = p.getProperties().get("key");
        assertEquals(2, values.size(), "number of values provided for passport key 'key' should be 2");
        assertAll("passport node under key 'key' should contain both 'val1' and 'val2'",
            () -> assertEquals("val1", values.get(0).toString()),
            () -> assertEquals("val2", values.get(1).toString())
        );
    }

    @Test
    void deserializeFromString_emptyLeafList() throws Exception {
        Passport pp = mapper.readValue(
            "{\"key\": [{\"subkey\": []}]}", 
            Passport.class
        );
        assertEquals(pp.getProperties().keySet().size(), 1, "passport size should be 1");
        assertTrue(pp.getProperties().containsKey("key"), "passport should contain key 'key'");

        List<Passport> children = pp.getProperties().get("key");
        assertEquals(1, children.size(), "number of child nodes should be 1");
        Passport spp = children.get(0);
        assertEquals(1, spp.getProperties().size(), "subkey passport size should be 1");
        assertTrue(spp instanceof Passport);
    }

    @Test
    void deserializeFromString_emptyLeaf() throws Exception {
        Passport pp = mapper.readValue(
            "{\"key\": [{\"subkey\": [{}]}]}", 
            Passport.class
        );
        Passport subkeyPassport = pp.getProperties().get("key").get(0);
        Passport leafValuePassport = subkeyPassport.getProperties().get("subkey").get(0);
        assertTrue(leafValuePassport instanceof Passport);
        assertEquals(0, leafValuePassport.getProperties().size(), "leaf node should be empty");
    }

    @Test
    void deserializeFromString_stringLeaf() throws Exception {
        Passport pp = mapper.readValue(
            "{\"key\": [\"val\"]}",
            Passport.class
        );
        assertEquals("val", pp.getProperties().get("key").get(0).toString(), "first value should be 'val'");
    }

    @Test
    void deserializeFromString_reference_level0() throws Exception {
        Passport pp = mapper.readValue(
            "{\"id\": \"ID\", \"eclass\": \"BTSThsEntry\"}",
            Passport.class
        );
        assertTrue(pp != null, "deserialized thesaurus reference passport leaf should not be null");
    }

    @Test
    void extractPaths_level0() throws Exception {
        Passport p = mapper.readValue(
            "{\"a\": []}",
            Passport.class
        );
        List<String> paths = p.extractPaths();
        assertAll("selector extraction should yield empty result",
            () -> assertEquals(0, paths.size(), "no selectors should be extracted from passport (no values)"),
            () -> assertEquals("", String.join(", ", paths), "there should be no selectors")
        );
    }

    @Test
    void extractPaths_level1() {
        Passport pp = new Passport();
        Passport leaf = new Passport("val");
        pp.add("key", leaf);
        List<String> paths = pp.extractPaths();
        assertEquals(1, paths.size(), "number of property paths extracted should be 1");
        assertEquals("key", paths.get(0), "extracted selector path should be 'key'");
    }

    @Test
    void extractPaths_level1_multipleValues() throws Exception {
        Passport p = mapper.readValue(
            "{\"a\": [\"b\", \"c\", \"d\"]}",
            Passport.class
        );
        List<String> paths = p.extractPaths();
        assertEquals(1, paths.size(), "number of property selectors extracted should be 1");
        assertEquals("a", paths.get(0), "only extractable selector should be 'a'");
    }

    @Test
    void extractPaths_level2_basic() throws Exception {
        Passport p = mapper.readValue(
            "{\"a\": [{\"b\": [\"c\"]}]}",
            Passport.class
        );
        List<String> paths = p.extractPaths();
        assertEquals(1, paths.size(), "number of property selectors extracted should be 1");
        assertEquals("a.b", paths.get(0), "only extractable selector should be 'a.b'");
    }

    @Test
    void extractPaths_level2_emptyValues() throws Exception {
        Passport p = mapper.readValue(
            "{\"a\": [{\"b\": [{}], \"c\": [], \"d\": [\"e\"]}]}",
            Passport.class
        );
        List<String> paths = p.extractPaths();
        assertAll("should only extract selector 'a.d'",
            () -> assertEquals(1, paths.size(), "exactly 1 selector should be extractable"),
            () -> assertEquals("a.d", String.join(", ", paths), "only extractable selector should be 'a.d'")
        );
    }

    @Test
    void extractPaths_level2() throws Exception {
        Passport pp = mapper.readValue(
            "{\"key\": [{\"subkey\": [\"val\"]}]}", 
            Passport.class
        );
        List<String> paths = pp.extractPaths();
        assertEquals(1, paths.size(), "number of property paths extracted should be 1");
        assertEquals("key.subkey", paths.get(0), "extracted selector path should be 'key.subkey'");
    }

    @Test
    void extractPaths_level3() throws Exception {
        Passport p = mapper.readValue(
            "{\"a\": [{\"l\": [{\"m\": [{\"n\": [\"o\"], \"p\": [{}], \"q\": []}], \"h\": [{\"i\": [\"j\", \"k\", \"l\", \"m\", \"n\"]}]}, {\"b\": [{\"c\": [\"d\"], \"e\": [{\"f\": [\"g\"]}]}]}]}]}",
            Passport.class
        );
        List<String> paths = p.extractPaths();
        Collections.sort(paths);
        assertAll("extracted selectors should be correct",
            () -> assertEquals(4, paths.size(), "number of selector paths should be 4"),
            () -> assertTrue(!paths.contains("a.l.m.p"), "selector 'a.l.m.p' should not be extracted (empty value)"),
            () -> assertTrue(!paths.contains("a.l.m.q"), "selector 'a.l.m.q' should not be extracted (no values)"),
            () -> assertEquals("a.l.b.c", paths.get(0), "first path should be albc"),
            () -> assertEquals("a.l.b.e.f", paths.get(1), "second path should be albef"),
            () -> assertEquals("a.l.h.i", paths.get(2), "third path should be alhi"),
            () -> assertEquals("a.l.m.n", paths.get(3), "fourth path should be almn")
        );
    }

    @Test
    void extractProperty_singleValue_level1() throws Exception {
        Passport pp = new Passport();
        Passport leaf = new Passport("val");
        pp.add("key", leaf);
        List<Passport> values = pp.extractProperty("key");
        assertTrue(values.contains(leaf), "extracted leaf values for key 'key' should contain "+leaf);
    }

    @Test
    void extractProperty_singleValue_level2() throws Exception {
        Passport pp = mapper.readValue(
            "{\"key1\": [{\"key2\": [\"val\"]}]}",
            Passport.class
        );
        // TODO
    }

    @Test
    void extractProperty_reference_level3() throws Exception {
        Passport pp = mapper.readValue(
            "{\"date\": [{\"date\": [{\"date\": [{\"eclass\": \"BTSThsEntry\", \"type\": \"date\", \"id\": \"FCJURX24JZGXZEKP3TW36U3ZFA\", \"name\": \"Ramses II. Usermaatre-Setepenre\"}]}]}]}",
            Passport.class
        );
        List<Passport> leafs = pp.extractProperty("date", "date", "date");
        assertEquals(1, leafs.size(), "number of leaf nodes under selector should be 1");
        Passport leaf = leafs.get(0);
        assertEquals("FCJURX24JZGXZEKP3TW36U3ZFA", leaf.getId(), "ID value of only ref leaf under selector should be 'FCJURX24JZGXZEKP3TW36U3ZFA'");
    }

    @Test
    void extractProperty_invalidSelector() throws Exception {
        Passport p = mapper.readValue(
            "{\"a\": [{\"b\": [{\"c\": [\"d\"]}]}]}",
            Passport.class
        );
        assertThrows(
            Exception.class,
            () -> {p.extractProperty("a.b.f");}
        );
    }

    @Test
    void extractProperty_validSelector() throws Exception {
        Passport p = mapper.readValue(
            "{\"a\": [{\"e\": [{\"c\": [\"f\"]}], \"b\": [{\"c\": [\"d\"]}]}]}",
            Passport.class
        );
        assertDoesNotThrow(
            () -> {p.extractProperty("a.e.c");},
            "dead end in legal selector lookup should not throw exception"
        );
        List<Passport> leafs = p.extractProperty("a.e.c");
        assertEquals(1, leafs.size(), "number of extracted values should be 1");
        assertEquals("f", leafs.get(0).toString(), "extracted value should be 'f'");
    }

    @Test
    void extractProperty_shortSelector() throws Exception {
        Passport p = mapper.readValue(
            "{\"a\": [{\"e\": [{\"c\": [\"f\"]}], \"b\": [{\"c\": [\"d\"]}]}]}",
            Passport.class
        );
        List<Passport> leafs = p.extractProperty("a.e");
        assertEquals(1, leafs.size(), "number of selected nodes should be 1");
        assertEquals(
            Map.of("c", List.of("f")).toString(),
            leafs.get(0).toString(),
            "extracted value list should contain map object"
        );
    }

}
