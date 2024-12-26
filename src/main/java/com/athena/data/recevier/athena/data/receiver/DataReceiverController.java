package com.athena.data.recevier.athena.data.receiver;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DataReceiverController {

    @GetMapping("/receive")
    public String receive() {
        return "Hello World!";
    }
}
