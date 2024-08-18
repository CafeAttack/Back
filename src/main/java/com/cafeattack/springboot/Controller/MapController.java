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

    /*
    // 카페 정보 받아올것임
    @GetMapping(value = "/main", produces = "application/json;charset=UTF-8")
    public String getAllCafe (@RequestParam("longitude") String longitude,
                              @RequestParam("latitude") String latitude,
                              @RequestParam("radius") int radius) {
        return mapService.getCafeInformsFromMap(longitude, latitude, radius);
    } */

    // 카테고리 관계없이 모든 카페 지도에서 불러오기
    @GetMapping(value = "/main", produces = "application/json;charset=UTF-8")
    public String getCafes (@RequestParam("longitude") String longitude,
                            @RequestParam("latitude") String latitude,
                            @RequestParam("radius") int radius) {
        return mapService.getAllCafesFromMap(longitude, latitude, radius);
    }

    // 카테고리별 카페 지도에서 보기
    @GetMapping(value = "/main/{categoryId}", produces = "application/json;charset=UTF-8")
    public String getAllCafe (@PathVariable("categoryId") int categoryId,
                                @RequestParam("longitude") String longitude,
                              @RequestParam("latitude") String latitude,
                              @RequestParam("radius") int radius) {
        return mapService.getCafeFromMap(categoryId, longitude, latitude, radius);
    }

    // 카페 선택 (간략한 정보)


    // 카페 정보 더보기


    // 카페 검색
    @GetMapping(value = "/search", produces = "application/json;charset=UTF-8")
    public String searchCafesByKeyword(@RequestParam("longitude") String longitude,
                                       @RequestParam("latitude") String latitude,
                                       @RequestParam("radius") int radius,
                                       @RequestParam("query") String query) {
        return mapService.searchCafe(longitude, latitude, radius, query);
    }
}
