package com.example.ex4.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VideoController {

    @GetMapping("/video/{country}/{langauge}")
    public String video(@PathVariable String country,
                        @PathVariable String langauge) {
        return "Video allowed for " + country + " " + langauge;
    }
}
