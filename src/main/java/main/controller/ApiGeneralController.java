package main.controller;

import java.net.ResponseCache;
import main.api.response.ResponseInit;
import main.service.GeneralService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiGeneralController {

    @Autowired
    private GeneralService generalService;

    @GetMapping(value = "api/init")
    public ResponseEntity<ResponseInit> init() {
        return new ResponseEntity<>(generalService.getInitData(), HttpStatus.OK);
    }
}
