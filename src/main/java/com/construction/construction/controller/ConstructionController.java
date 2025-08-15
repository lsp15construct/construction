package com.construction.construction.controller;

import com.construction.construction.dao.ConstructionDAO;
import com.construction.construction.dao.UTMConverter;
import com.construction.construction.model.ConstructionColumn;
import com.construction.construction.model.KmBucketSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/construction")
@RequiredArgsConstructor
public class ConstructionController {

    private final ConstructionDAO constructionDao;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "startSheet", required = false) Integer startSheet,
            @RequestParam(value = "endSheet", required = false) Integer endSheet,
            @RequestParam(value = "rowStart", required = false) Integer rowStart,
            @RequestParam(value = "rowEnd", required = false) Integer rowEnd
    ) {
        try {
            // Defaults to -1 (which means "all") if not provided
            if (startSheet == null) startSheet = -1;
            if (endSheet == null) endSheet = -1;
            if (rowStart == null) rowStart = -1;
            if (rowEnd == null) rowEnd = -1;

            constructionDao.importFromExcel(file, startSheet, endSheet, rowStart, rowEnd);
            System.out.println("✅ Upload finished");
            return ResponseEntity.ok("Uploaded and saved successfully.");
        } catch (Exception e) {
            e.printStackTrace(); // For easier debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/test-convert")
    public ResponseEntity<?> testConvertUTMToLatLng(
            @RequestParam double easting,
            @RequestParam double northing,
            @RequestParam(defaultValue = "47" ) int zone,
            @RequestParam(defaultValue = "true") boolean isNorth
    ) {
        double[] latLng = UTMConverter.convertUTMToLatLng(easting, northing, zone, isNorth);
        Map<String, Double> response = new HashMap<>();
        response.put("latitude", latLng[0]);
        response.put("longitude", latLng[1]);
        return ResponseEntity.ok(response);
    }

    // 📍 ดึงข้อมูลทั้งหมด
    @GetMapping("/all")
    public List<ConstructionColumn> getAllColumns() {
        return constructionDao.findAll();
    }

    // 📍 ดึงข้อมูลเฉพาะช่วง กม. (เช่น กม.0-1)
    @GetMapping("/km-range")
    public List<ConstructionColumn> getByKmRange(@RequestParam double startKm, @RequestParam double endKm) {
        return constructionDao.findByKmRange(startKm, endKm);
    }

    // 📍 ดึงจำนวนต้นในช่วง กม.
    @GetMapping("/count")
    public int countByKmRange(@RequestParam double startKm, @RequestParam double endKm) {
        return constructionDao.countByKmRange(startKm, endKm);
    }

    @GetMapping("/km-bucket-summary")
    public ResponseEntity<KmBucketSummary> getKmBucketSummary(
            @RequestParam("line") String line,
            @RequestParam("startKm") double startKm,
            @RequestParam("endKm") double endKm) {
        try {
            KmBucketSummary summary = constructionDao.getKmBucketSummary(line, startKm, endKm);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 📍 เพิ่มข้อมูลใหม่ (ใช้สำหรับ debug หรือ import ทีละรายการ)
    @PostMapping("/add")
    public void insertColumn(@RequestBody ConstructionColumn column) {
        constructionDao.insert(column);
    }

}