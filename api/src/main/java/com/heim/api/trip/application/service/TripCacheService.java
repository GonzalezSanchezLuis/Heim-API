package com.heim.api.trip.application.service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.heim.api.trip.application.dto.TripRequest;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TripCacheService {
    private final IMap<Long, TripRequest> tripCache;
    private final Map<Long, Map<Long, Integer>> notificationCounters = new ConcurrentHashMap<>();

    public  TripCacheService(HazelcastInstance hazelcastInstance){
        this.tripCache = hazelcastInstance.getMap("tripRequests");
    }

    public void storeTrip(Long userId, TripRequest request) {
        tripCache.put(userId, request);
    }

    public TripRequest getTrip(Long userId) {
        return tripCache.get(userId);
    }

    public void removeTrip(Long userId) {
        tripCache.remove(userId);
    }

    public int getNotificationCount(Long tripId, Long driverId) {
        return notificationCounters.getOrDefault(tripId, Collections.emptyMap())
                .getOrDefault(driverId, 0);
    }

    public void incrementNotificationCount(Long tripId, Long driverId) {
        notificationCounters.computeIfAbsent(tripId, k -> new ConcurrentHashMap<>())
                .merge(driverId, 1, Integer::sum);
    }


}
