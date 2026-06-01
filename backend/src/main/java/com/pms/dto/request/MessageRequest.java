package com.pms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// OOP — ENCAPSULATION: private fields with Lombok @Data — data carrier for API layer
public class MessageRequest {

    @NotBlank(message = "Message content is required")
    private String content;

    @NotNull(message = "Receiver ID is required")
    private Long receiverId;
}
