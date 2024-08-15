package com.cafeattack.springboot.Controller;

import com.cafeattack.springboot.Exception.BaseException;
import com.cafeattack.springboot.Service.MapService;
import com.cafeattack.springboot.common.BaseErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

@RestController
@RequestMapping("/map")
@RequiredArgsConstructor
public class MapController {

    private final MapService mapService;

    @GetMapping(value = "/main", produces = "application/json;charset=UTF-8")
    public String getAllCafe (@RequestParam("longitude") String longitude,
                              @RequestParam("latitude") String latitude,
                              @RequestParam("radius") int radius) {
        return mapService.getAllCafeFromMap(longitude, latitude, radius);
    }

    @GetMapping(value = "/search", produces = "application/json;charset=UTF-8")
    public String searchCafesByKeyword(@RequestParam("longitude") String longitude,
                                       @RequestParam("latitude") String latitude,
                                       @RequestParam("radius") int radius,
                                       @RequestParam("query") String query) {
        return mapService.searchCafe(longitude, latitude, radius, query);
    }
}
