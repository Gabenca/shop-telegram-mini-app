package com.example.backend.event;

public abstract class DomainEvent {
    private final Long coupleId;
    private final Long actorUserId;

    protected DomainEvent(Long coupleId, Long actorUserId) {
        this.coupleId = coupleId;
        this.actorUserId = actorUserId;
    }

    public Long getCoupleId() { return coupleId; }
    public Long getActorUserId() { return actorUserId; }
}
