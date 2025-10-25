package com.youthconnect.edge_functions.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TTSRequest {
    private String text;
    private String voice;  // alloy, echo, fable, onyx, nova, shimmer
    private Double speed;  // 0.25 to 4.0
}
