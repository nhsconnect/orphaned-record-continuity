package uk.nhs.prm.deductions.pdsadaptor.testing;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.nhs.prm.deductions.pdsadaptor.testing.MapBuilder.*;

public class MapBuilderTest {

    @Test
    public void mapBuilderShouldMakeAFlatMapCheaply() {
        var map = mapBuilder().with("bob", "foo").with("sue", "too").build();

        assertThat(map.get("bob")).isEqualTo("foo");
        assertThat(map.get("sue")).isEqualTo("too");
    }

    @Test
    public void mapBuilderShouldProvideCreateMethodForSingleCallCreationWithLambda() {
        var map = create(mb -> mb
                .with("bob", "foo")
                .with("sue", "too"));

        assertThat(map.get("bob")).isEqualTo("foo");
        assertThat(map.get("sue")).isEqualTo("too");
    }

    @Test
    public void createShouldAllowNesting() {
        var map = create(m -> m
                .with("foo", create(a -> a.with("a", "ha")))
                .with("noo", "now"));

        assertThat(map.get("foo")).isInstanceOf(Map.class);
        assertThat(((Map) map.get("foo")).get("a")).isEqualTo("ha");
        assertThat(map.get("noo")).isEqualTo("now");
    }

    @Test
    public void createShouldProvideAnExplicitNestingForm() {
        var map = create(m -> m
                .nesting("foo", a -> a.with("a", "ha"))
                .with("noo", "now"));

        assertThat(map.get("foo")).isInstanceOf(Map.class);
        assertThat(((Map) map.get("foo")).get("a")).isEqualTo("ha");
        assertThat(map.get("noo")).isEqualTo("now");
    }

    @Test
    public void createShouldProvideAnImplicitNestingForm() {
        var map = create(m -> m
                .with("foo", a -> a.with("a", "ha"))
                .with("noo", "now"));

        assertThat(map.get("foo")).isInstanceOf(Map.class);
        assertThat(((Map) map.get("foo")).get("a")).isEqualTo("ha");
        assertThat(map.get("noo")).isEqualTo("now");
    }

    @Test
    public void shouldProvideShortReasonableAliases() {
        var map = create(m -> m
                .kv("foo", a -> a.kv("a", "ha"))
                .kv("noo", "now"));

        assertThat(map.get("foo")).isInstanceOf(Map.class);
        assertThat(((Map) map.get("foo")).get("a")).isEqualTo("ha");
        assertThat(map.get("noo")).isEqualTo("now");
    }

    @Test
    public void canAlsoProvideJson() {
        var json = mapBuilder()
                .kv("foo", a -> a.kv("a", "ha"))
                .kv("noo", "now").toJson();

        var map = new JSONObject(json);
        assertThat(((JSONObject) map.get("foo")).get("a")).isEqualTo("ha");
        assertThat(map.get("noo")).isEqualTo("now");
    }

    @Test
    public void andHasASweetShortFormForJsonCreation() {
        var json = json(jb -> jb
                .kv("foo", a -> a.kv("a", "ha"))
                .kv("noo", "now"));

        var map = new JSONObject(json);
        assertThat(((JSONObject) map.get("foo")).get("a")).isEqualTo("ha");
        assertThat(map.get("noo")).isEqualTo("now");
    }

    @Test
    public void andSupportsArraysOfLiterals() {
        var json = json(jb -> jb.kv("wow", array("a", "b")));

        assertThat(json).isEqualTo("{\"wow\":[\"a\",\"b\"]}");
    }

    @Test
    public void andSupportsLambdaSpecsForArrayElements() {
        var map = create(jb -> jb.kv("wow", array(
                a -> "a",
                b -> b.kv("type", "nested")
                      .kv("name", "b"))));

        var wowArray = (List<Object>) map.get("wow");
        assertThat(wowArray.get(0)).isEqualTo("a");
        var objectFromMap = (Map<String, Object>) wowArray.get(1);
        assertThat(objectFromMap.get("type")).isEqualTo("nested");
        assertThat(objectFromMap.get("name")).isEqualTo("b");
    }
}

