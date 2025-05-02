package com.heim.api.price.infraestructure.controller;

import com.heim.api.price.application.dto.PriceRequest;
import com.heim.api.price.application.dto.PriceResponse;
import com.heim.api.price.service.PriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/price/")
@CrossOrigin("*")
public class PricingController {

    private final PriceService priceService;

    @Autowired
    public PricingController(PriceService priceService) {
        this.priceService = priceService;
    }

    @PostMapping("calculate")
    public ResponseEntity<PriceResponse> getPrice(@RequestBody PriceRequest priceRequest) {
        System.out.println("precio calculado" + priceRequest);
        PriceResponse response = priceService.calculatePrice(priceRequest);

        return ResponseEntity.ok(response);
    }
}

