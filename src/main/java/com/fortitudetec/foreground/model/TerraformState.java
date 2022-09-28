package com.fortitudetec.foreground.model;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Value
@Builder
@Jacksonized
public class TerraformState {

    Long id;

    @NotBlank
    String name;

    @NotBlank
    String content;

    @NotNull
    @Builder.Default
    Instant uploadedAt = Instant.now();

}
