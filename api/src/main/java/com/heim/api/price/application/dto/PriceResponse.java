package com.heim.api.price.application.dto;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PriceResponse {
    private BigDecimal price;
    private String formattedPrice;
    private double distanceKm;
    private double timeMin;
    private String formattedDistance;
    private String formattedDuration;
    private List<Map<String, Double>> route;

    public PriceResponse(BigDecimal price,double distanceKm, double timeMin, List<Map<String, Double>> route) {
        this.price = price;
        this.formattedPrice = NumberFormat.getNumberInstance(new Locale("es", "CO")).format(price) + " COP";
        this.distanceKm = distanceKm;
        this.timeMin = timeMin;
        this.formattedDistance = String.format("%.1f km", distanceKm);
        this.formattedDuration = String.format("%.0f min", timeMin);
        this.route = route;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getFormattedPrice() {
        return formattedPrice;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public double getTimeMin() {
        return timeMin;
    }

    public String getFormattedDistance() {
        return formattedDistance;
    }

    public String getFormattedDuration() {
        return formattedDuration;
    }

    public List<Map<String, Double>> getRoute() {
        return route;
    }
}

