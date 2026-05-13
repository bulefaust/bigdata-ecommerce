package com.bigdata.ecommerce.controller;

import com.bigdata.ecommerce.service.FlashSaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/flash-sale")
public class FlashSaleController {

    @Autowired
    private FlashSaleService flashSaleService;

    @GetMapping("/current")
    public List<Map<String, Object>> currentSales() {
        return flashSaleService.getCurrentFlashSales();
    }

    @GetMapping("/upcoming")
    public List<Map<String, Object>> upcomingSales() {
        return flashSaleService.getUpcomingFlashSales();
    }
}
