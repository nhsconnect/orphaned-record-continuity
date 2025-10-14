package uk.nhs.prm.deductions.pdsadaptor.testing;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public class MapBuilder {
    public static Map<String, Object> create(Consumer<MapBuilder> spec) {
        MapBuilder builder = new MapBuilder();
        spec.accept(builder);
        return builder.build();
    }

    public static String json(Consumer<MapBuilder> spec) {
        MapBuilder builder = new MapBuilder();
        spec.accept(builder);
        return builder.toJson();
    }

    public static MapBuilder mapBuilder() {
        return new MapBuilder();
    }

    public static List<Object> array(Object... items) {
        return asList(items);
    }

    public static List<Object> array(Function<MapBuilder, Object>... itemSpecs) {
        return asList(itemSpecs).stream().map(spec -> {
            MapBuilder builder = mapBuilder();
            var returnValue = spec.apply(builder);
            if (returnValue == builder) {
                return builder.build();
            }
            return returnValue;
        }).collect(toList());
    }

    private Map<String, Object> map = new HashMap<>();

    public MapBuilder with(String key, Object value) {
        map.put(key, value);
        return this;
    }

    public MapBuilder nesting(String key, Consumer<MapBuilder> nestedMapSpec) {
        map.put(key, create(nestedMapSpec));
        return this;
    }

    public MapBuilder with(String key, Consumer<MapBuilder> nestedMapSpec) {
        return nesting(key, nestedMapSpec);
    }

    public MapBuilder kv(String key, Object value) {
        return with(key, value);
    }

    public MapBuilder kv(String key, Consumer<MapBuilder> nestedMapSpec) {
        return with(key, nestedMapSpec);
    }

    public Map<String, Object> build() {
        return map;
    }

    public String toJson() {
        return new JSONObject(map).toString();
    }
}
