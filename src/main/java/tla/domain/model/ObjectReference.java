package tla.domain.model;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.AbstractMap.SimpleEntry;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class ObjectReference implements Comparable<ObjectReference> {

    @NonNull
    private String id;
    @NonNull
    private String eclass;
    private String type;
    private String name;

    @JsonCreator
    public ObjectReference(
        @JsonProperty(value = "id", required = true) String id,
        @JsonProperty(value = "eclass", required = true) String eclass,
        @JsonProperty(value = "type", required = false) String type,
        @JsonProperty(value = "name", required = false) String name
    ) {
        this.id = id;
        this.eclass = eclass;
        this.type = type;
        this.name = name;
    }

    @Override
    public String toString() {
        return Collections.unmodifiableMap(
            Stream.of(
                new SimpleEntry<>("id", id),
                new SimpleEntry<>("name", name),
                new SimpleEntry<>("type", type),
                new SimpleEntry<>("eclass", eclass)
            ).collect(
                Collectors.toMap(
                    (e) -> e.getKey(),
                    (e) -> e.getValue()
                )
            )
        ).toString();
    }

    @Override
    public int compareTo(ObjectReference arg0) {
        if (this.getEclass().equals(arg0.getEclass())) {
            return this.getId().compareTo(arg0.getId());
        }
        return this.getEclass().compareTo(arg0.getEclass());
    }

}