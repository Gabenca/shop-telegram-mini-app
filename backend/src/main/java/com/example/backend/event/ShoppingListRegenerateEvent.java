package com.example.backend.event;

public class ShoppingListRegenerateEvent extends DomainEvent {
    public ShoppingListRegenerateEvent(Long coupleId, Long actorUserId) {
        super(coupleId, actorUserId);
    }
}
