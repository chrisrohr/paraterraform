package org.paraterra.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

@Value
@Builder
@Jacksonized
public class StateLock {

    @JsonProperty("ID")
    String id;

    @JsonProperty("Operation")
    String operation;

    @JsonProperty("Info")
    String info;

    @JsonProperty("Who")
    String lockedBy;

    @JsonProperty("Version")
    String version;

    @JsonProperty("Created")
    Instant createdAt;

    @JsonProperty("Path")
    String path;

    @With
    String stateName;

}
