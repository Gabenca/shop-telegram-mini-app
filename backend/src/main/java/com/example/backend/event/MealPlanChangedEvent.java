package com.example.backend.event;

public class MealPlanChangedEvent extends DomainEvent {
    public MealPlanChangedEvent(Long coupleId, Long actorUserId) {
        super(coupleId, actorUserId);
    }
}
