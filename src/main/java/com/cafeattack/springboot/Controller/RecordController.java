package com.cafeattack.springboot.Controller;

import com.amazonaws.Response;
import com.cafeattack.springboot.Service.RecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/record")
public class RecordController {
    private final RecordService recordService;

    @GetMapping("/{memberId}/orderdate")
    public ResponseEntity DateorderPage(@PathVariable("memberId") int memberId) {
        return recordService.getDateorderPage(memberId);
    }

    @GetMapping("/{memberId}/ordername")
    public ResponseEntity NameorderPage(@PathVariable("memberId") int memberId) {
        return recordService.getNameorderPage(memberId);
    }

    @GetMapping("/{memberId}/orderfrequency")
    public ResponseEntity FreqorderPage(@PathVariable("memberId") int memberId) {
        return recordService.getFreqorderPage(memberId);
    }

    @GetMapping("/{memberId}/{cafeId}")
    public ResponseEntity getCafePage(@PathVariable("memberId") int memberId, @PathVariable("cafeId") int cafeId) {
        return recordService.getCafePage(memberId, cafeId);
    }
}
