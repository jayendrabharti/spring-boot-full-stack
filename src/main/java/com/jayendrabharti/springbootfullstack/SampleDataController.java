package com.jayendrabharti.springbootfullstack;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api")
public class SampleDataController {
    public record User(int id, String name, String email) { }

    @GetMapping("/user")
    public User getSampleUser() {
        return new User(1, "Jay", "jay.bharti2804@gmail.com");
    }

    @GetMapping("/message")
    public String getSampleMessage() {
        return "Hello from Spring Boot!";
    }
}
