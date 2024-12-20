package com.cafeattack.springboot.Controller;

import com.amazonaws.Response;
import com.cafeattack.springboot.Domain.Dto.request.NewlyRecordRequestDTO;
import com.cafeattack.springboot.Domain.Dto.response.EditRecordsResponseDTO;
import com.cafeattack.springboot.Service.RecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/record")
public class RecordController {
    private final RecordService recordService;

    @GetMapping("/{memberId}/orderdate")
    public ResponseEntity DateorderPage(@PathVariable("memberId") int memberId) {
        return recordService.getDateorderPage(memberId);
    }

    @GetMapping("/{memberId}/{cafeId}")
    public ResponseEntity getCafePage(@PathVariable("memberId") int memberId, @PathVariable("cafeId") int cafeId) {
        return recordService.getCafePage(memberId, cafeId);
    }

    @GetMapping("/{memberId}/{cafeId}/new")
    public ResponseEntity getEnrollPage(@PathVariable("memberId") int memberId, @PathVariable("cafeId") int cafeId) {
        return recordService.getEnrollPage(memberId, cafeId);
    }

    @PostMapping("/{memberId}/{cafeId}/new")
    public ResponseEntity newlyRecord(@PathVariable("memberId") int memberId, @PathVariable("cafeId") int cafeId, @RequestBody NewlyRecordRequestDTO newlyRecordRequestDTO) {
        return recordService.newlyRecord(memberId, cafeId, newlyRecordRequestDTO);
    }

    @GetMapping("/{memberId}/{cafeId}/{recordId}")
    public ResponseEntity getEditPage(@PathVariable("memberId") int memberId, @PathVariable("cafeId") int cafeId, @PathVariable("recordId") int recordId) {
        return recordService.getEditPage(memberId, cafeId, recordId);
    }

    @PatchMapping("/{memberId}/{cafeId}/{recordId}")
    public ResponseEntity editPage(@PathVariable("memberId") int memberId, @PathVariable("cafeId") int cafeId, @PathVariable("recordId") int recordId, @RequestBody EditRecordsResponseDTO editRecordsResponseDTO) {
        return recordService.editPage(memberId, cafeId, recordId, editRecordsResponseDTO);
    }
}
