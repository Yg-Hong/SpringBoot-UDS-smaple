package com.example.test;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
public class ExampleController {

    @GetMapping("/test")
    public Mono<String> getTest() {
        return Mono.just("Hello from Unix Domain Socket!");
    }

    @PostMapping("/echo")
    public Mono<String> echo(@RequestBody String message) {
        return Mono.just("Echo : " + message);
    }
}
