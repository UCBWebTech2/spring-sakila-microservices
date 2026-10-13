package com.ucb.sakila.inventoryservice.shared;

import lombok.Getter;

// code viaja en el faultstring como "CODE: message".
@Getter
public abstract class InventoryServiceException extends RuntimeException {

    private final String code;

    protected InventoryServiceException(String code, String message) {
        super(message);
        this.code = code;
    }
}
