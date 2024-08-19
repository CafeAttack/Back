package com.cafeattack.springboot.Controller;

import com.cafeattack.springboot.Service.MapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/map")
@RequiredArgsConstructor
public class MapController {

    private final MapService mapService;

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
    @GetMapping(value = "/{cafeId}/{memberId}", produces = "application/json;charset=UTF-8")
    public ResponseEntity<String> getShortInformations (@PathVariable("cafeId") String cafeId,
                                                @PathVariable("memberId") Integer memberId) {
        return mapService.getShortCafes(cafeId, memberId);
    }

    // 카페 정보 더보기


    // 카페 검색 (ALL)
    @GetMapping(value = "/search", produces = "application/json;charset=UTF-8")
    public String searchAllCafesByKeyword(@RequestParam("longitude") String longitude,
                                       @RequestParam("latitude") String latitude,
                                       @RequestParam("radius") int radius,
                                       @RequestParam("query") String query) {
        return mapService.searchAllCafe(longitude, latitude, radius, query);
    }

    // 카페 검색 (카테고리 별)
    @GetMapping(value = "/search/{categoryId}", produces = "application/json;charset=UTF-8")
    public ResponseEntity<String> searchCafesByKeyword(@RequestParam("longitude") String longitude,
                                       @RequestParam("latitude") String latitude,
                                       @RequestParam("radius") int radius,
                                       @RequestParam("query") String query,
                                       @PathVariable("categoryId") int categoryId) {
        return mapService.searchCafe(longitude, latitude, radius, query, categoryId);
    }
}
