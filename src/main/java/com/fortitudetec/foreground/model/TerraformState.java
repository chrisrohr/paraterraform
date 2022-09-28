package com.fortitudetec.foreground.model;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Value
@Builder
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
