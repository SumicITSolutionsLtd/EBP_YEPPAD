package com.youthconnect.edge_functions.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * USSD Menu DTO
 * Represents a USSD menu screen
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UssdMenuDTO {
    private String menuId;
    private String title;
    private List<UssdMenuItemDTO> items;
    private String footer;
    private Boolean isEndMenu;
}
