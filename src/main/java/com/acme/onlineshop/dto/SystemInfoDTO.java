package com.acme.onlineshop.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "INFO: Memory/Disk space is measured in GB")
public record SystemInfoDTO(
        @Schema(description = "System attribute name") String name,
        @Schema(description = "Current value of system attribute") float currentValue) {
}
