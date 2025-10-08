package com.youthconnect.edge_functions.client;

import com.youthconnect.edge_functions.dto.MentorshipSessionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "mentor-service")
public interface MentorServiceClient {

    @PostMapping("/api/mentorship/sessions")
    MentorshipSessionDTO createSession(@RequestBody MentorshipSessionDTO sessionDTO);
}
