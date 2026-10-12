package com.example.vendorcrm.controller;

import com.example.vendorcrm.dto.CommandCentreResponse;
import com.example.vendorcrm.dto.OperationalOrderDto;
import com.example.vendorcrm.service.CommandCentreService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * VENDOR -> own vendor only (decided on the server). ADMIN -> all vendors.
 */
@RestController
@RequestMapping("/api/command-centre")
public class CommandCentreController {

    private final CommandCentreService service;

    public CommandCentreController(CommandCentreService service) {
        this.service = service;
    }

    @GetMapping("/overview")
    public CommandCentreResponse overview(Authentication auth,
                                          @RequestParam(defaultValue = "today") String range) {
        return service.overview(auth, range);
    }

    @GetMapping("/orders")
    public List<OperationalOrderDto> orders(Authentication auth,
                                            @RequestParam(defaultValue = "today") String range,
                                            @RequestParam(defaultValue = "all") String tab,
                                            @RequestParam(required = false) String search,
                                            @RequestParam(defaultValue = "50") int limit) {
        return service.orders(auth, range, tab, search, limit);
    }
}
