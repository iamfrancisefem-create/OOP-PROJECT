package com.pms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamRequest {

    @NotBlank(message = "Team name is required")
    @Size(max = 100, message = "Team name must be at most 100 characters")
    private String name;

    private String description;
}
