package com.rhytham.redisapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request payload to set a key-value pair with optional TTL")
public class KeyValueRequest {

    @NotBlank(message = "Key must not be blank")
    @Schema(description = "The unique key", example = "username")
    private String key;

    @NotBlank(message = "Value must not be blank")
    @Schema(description = "The associated value", example = "Hello123")
    private String value;

    @Min(value = 1,message = "TTL must be greater than 0")
    @Schema(description = "Time to live in seconds (optional)", example = "60", nullable = true)
    private Long ttl;
}
