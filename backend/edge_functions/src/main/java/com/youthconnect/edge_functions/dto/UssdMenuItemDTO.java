package com.youthconnect.edge_functions.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * USSD Menu Item DTO
 * Individual menu option
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UssdMenuItemDTO {
    private String key;
    private String label;
    private String action;
    private String nextMenu;
}