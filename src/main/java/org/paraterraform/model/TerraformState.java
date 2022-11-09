package org.paraterraform.model;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

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

    String updatedBy;
}
