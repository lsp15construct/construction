package com.construction.construction.dao;

//import com.construction.model.ConstructionColumn;
//import lombok.RequiredArgsConstructor;
import com.construction.construction.model.ConstructionColumn;
import com.construction.construction.model.KmBucketSummary;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.usermodel.Cell;


import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import java.io.InputStream;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ConstructionDAO {

    private final JdbcTemplate jdbcTemplate;

    public class HeaderInfo {
        private int kmStartCol;

        private int kmEndCol;
        public int kmCol;
        public int installCol;
        public int nCol;
        public int eCol;
        public int headerRowIndex;
        public int nRowIndex;

        public int baseRow;

        public HeaderInfo(int kmCol, int installCol, int nCol, int eCol, int baseRow) {
            this.kmCol = kmCol;
            this.kmStartCol = kmStartCol;
            this.kmEndCol = kmEndCol;
            this.installCol = installCol;
            this.nCol = nCol;
            this.eCol = eCol;
            this.headerRowIndex = headerRowIndex;
            this.nRowIndex = nRowIndex;
            this.baseRow = baseRow;
        }
    }


    private final RowMapper<ConstructionColumn> mapper = (rs, rowNum) -> {
        ConstructionColumn col = new ConstructionColumn();
        col.setId(rs.getLong("id"));
        col.setLine(rs.getString("line"));
        col.setLabel(rs.getString("label"));
        col.setKm(rs.getDouble("km"));
        col.setLat(rs.getDouble("lat"));
        col.setLng(rs.getDouble("lng"));
        col.setUtmEasting(rs.getDouble("utm_easting"));
        col.setUtmNorthing(rs.getDouble("utm_northing"));
        col.setInstalled(rs.getBoolean("installed"));
        col.setZone(rs.getString("zone"));
        return col;
    };

    public List<ConstructionColumn> findAll() {
        String sql = "SELECT * FROM construction";
        return jdbcTemplate.query(sql, mapper);
    }

    public List<ConstructionColumn> findByKmRange(double startKm, double endKm) {
        String sql = "SELECT * FROM construction WHERE km BETWEEN ? AND ?";
        return jdbcTemplate.query(sql, mapper, startKm, endKm);
    }

    public int countByKmRange(double startKm, double endKm) {
        String sql = "SELECT COUNT(*) FROM construction WHERE km BETWEEN ? AND ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, startKm, endKm);
    }


    public void insert(ConstructionColumn column) {
        String sql = "INSERT INTO construction (line, label, km, lat, lng, utm_easting, utm_northing, installed, zone) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                column.getLine(),
                column.getLabel(),
                column.getKm(),
                column.getLat(),
                column.getLng(),
                column.getUtmEasting(),
                column.getUtmNorthing(),
                column.getInstalled(),
                column.getZone());
    }

    //    OG สแกนแถวระบุชัดเจน
//    public void importFromExcel(MultipartFile file) throws Exception {
//        try (InputStream is = file.getInputStream()) {
//            Workbook workbook = new XSSFWorkbook(is);
//            DataFormatter formatter = new DataFormatter();
//
//            int totalInserted = 0; // ✅ นับรวมทุก sheet
//
//            for (int sheetIndex = 9; sheetIndex < 10; sheetIndex++) {
//                Sheet sheet = workbook.getSheetAt(sheetIndex);
//                String sheetName = workbook.getSheetName(sheetIndex);
//
//                // 🔹 Extract "line" from sheet name เช่น "Line รวม B3" → "B3"
//                String[] split = sheetName.trim().split(" ");
//                String line = split.length >= 3 ? split[2] : "UNKNOWN";
//
//                for (Row row : sheet) {
//                    int rowNum = row.getRowNum() + 1;
//                    if (rowNum <= 5) continue; // ข้าม header 5 แถวแรก
//
//                    if (totalInserted >= 200) {
//                        System.out.println("✅ Reached 200 rows. Stopping import.");
//                        return;
//                    }
//
//                    Cell roadCell = row.getCell(1);
//                    Cell kmSubCell = row.getCell(3);
//                    Cell installedCell = row.getCell(4);
//                    Cell nCell = row.getCell(5);
//                    Cell eCell = row.getCell(6);
//
//                    if (roadCell == null || kmSubCell == null || nCell == null || eCell == null) {
//                        System.out.println("⛔️ Missing cell at row " + rowNum + ", sheet: " + sheetName);
//                        continue;
//                    }
//
//                    try {
//                        String road = formatter.formatCellValue(roadCell).trim();
//                        String label = road.split(" ")[0].trim();
//                        int kmMain = Integer.parseInt(road.replaceAll("[^0-9]", ""));
//                        double kmSub = Double.parseDouble(formatter.formatCellValue(kmSubCell));
//                        double km = kmMain + (kmSub / 1000.0);
//                        double n = Double.parseDouble(formatter.formatCellValue(nCell));
//                        double e = Double.parseDouble(formatter.formatCellValue(eCell));
//                        boolean installed = (installedCell != null && !formatter.formatCellValue(installedCell).trim().isEmpty());
//                        double[] latLng = UTMConverter.convertUTMToLatLng(e, n, 47, true);
//
//                        ConstructionColumn column = new ConstructionColumn();
//                        column.setLabel(label);
//                        column.setLine(line);
//                        column.setKm(km);
//                        column.setLat(latLng[0]);
//                        column.setLng(latLng[1]);
//                        column.setUtmEasting(e);
//                        column.setUtmNorthing(n);
//                        column.setInstalled(installed);
//                        column.setZone("47N");
//
//                        insert(column);
//                        totalInserted++;
//                        System.out.println("✅ Inserted row " + rowNum + " → total: " + totalInserted);
//
//                    } catch (NumberFormatException ex) {
//                        System.out.println("⚠️ Failed to parse number at row " + rowNum + ", sheet: " + sheetName + ". Skipping.");
//                    } catch (Exception ex) {
//                        System.out.println("❌ Error at row " + rowNum + ", sheet: " + sheetName + ": " + ex.getMessage());
//                    }
//                }
//            }
//        }
//    }


//    public void importFromExcel(MultipartFile file) throws Exception {
//        try (InputStream is = file.getInputStream()) {
//            Workbook workbook = new XSSFWorkbook(is);
//            DataFormatter formatter = new DataFormatter();
//
//            int totalInserted = 0;
//            int startSheet = 9;
//            int endSheet = 10;
//
//            int rowStart = 1;  // 👈 เริ่มนับจากข้อมูลแถวที่ 1 (ไม่ใช่ Excel row ที่ 1)
//            int rowEnd = 200;
//
//            for (int sheetIndex = startSheet; sheetIndex < endSheet; sheetIndex++) {
//                Sheet sheet = workbook.getSheetAt(sheetIndex);
//                String sheetName = workbook.getSheetName(sheetIndex);
//
//                // ✅ กำหนด row เริ่มต้นของข้อมูลในชีทนี้ (อาจต้องแก้เป็น map ถ้าแต่ละชีทไม่เท่ากัน)
//                int dataRowStartIndex = 5; // เช่น ชีทนี้เริ่มที่แถว 6 (row index = 5)
//
//                String[] split = sheetName.trim().split(" ");
//                String line = split.length >= 3 ? split[2] : "UNKNOWN";
//
//                int dataRowCounter = 0;
//
//                for (Row row : sheet) {
//                    int rowNum = row.getRowNum();
//                    if (rowNum < dataRowStartIndex) continue;
//
//                    dataRowCounter++;
//
//                    if (dataRowCounter < rowStart) continue;
//                    if (dataRowCounter > rowEnd ) {
//                        System.out.println("✅ Reached rowEnd = " + rowEnd + ", stopping.");
//                        return;
//                    }
//
//                    Cell roadCell = row.getCell(1);
//                    Cell plusCell = row.getCell(2);
//                    Cell kmSubCell = row.getCell(3);
//                    Cell installedCell = row.getCell(4);
//                    Cell nCell = row.getCell(5);
//                    Cell eCell = row.getCell(6);
//
//                    if (roadCell == null || kmSubCell == null || nCell == null || eCell == null) {
//                        System.out.println("⛔️ Incomplete data at Excel row " + (rowNum + 1) + ", skipping.");
//                        continue;
//                    }
//
//                    try {
//                        String road = formatter.formatCellValue(roadCell).trim();
//                        String plus = formatter.formatCellValue(plusCell).trim();
//                        String kmSubStr = formatter.formatCellValue(kmSubCell).trim();
//
//                        String label = road.split(" ")[0].trim();
//                        String kmMainStr = road.replaceAll("[^0-9]", "");
//                        int kmMain = Integer.parseInt(kmMainStr);
//                        double kmSub = kmSubStr.isEmpty() ? 0.0 : Double.parseDouble(kmSubStr);
//                        double km = kmMain + (kmSub / 1000.0);
//
//                        double n = Double.parseDouble(formatter.formatCellValue(nCell));
//                        double e = Double.parseDouble(formatter.formatCellValue(eCell));
//                        boolean installed = (installedCell != null && !formatter.formatCellValue(installedCell).trim().isEmpty());
//                        double[] latLng = UTMConverter.convertUTMToLatLng(e, n, 47, true);
//
//                        ConstructionColumn column = new ConstructionColumn();
//                        column.setLabel(label);
//                        column.setLine(line);
//                        column.setKm(km);
//                        column.setLat(latLng[0]);
//                        column.setLng(latLng[1]);
//                        column.setUtmEasting(e);
//                        column.setUtmNorthing(n);
//                        column.setInstalled(installed);
//                        column.setZone("47N");
//
//                        insert(column);
//                        totalInserted++;
//                        System.out.println("✅ Inserted row (data #" + dataRowCounter + "): km = " + km);
//
//                    } catch (Exception ex) {
//                        System.out.println("❌ Error at Excel row " + (rowNum + 1) + ": " + ex.getMessage());
//                    }
//                }
//            }
//        }
//    }

    private double parseKmFlexible(String text) {
        if (text == null || text.trim().isEmpty()) return Double.NaN;

        try {
            text = text.replaceAll("[\\t\\n\\r]", " ").trim();
            String[] tokens = text.split("\\s+|\\+");
            double km = 0.0;

            for (String token : tokens) {
                token = token.trim();
                if (token.matches("\\d+(\\.\\d+)?")) {
                    if (km == 0.0) {
                        km = Double.parseDouble(token);
                    } else {
                        km += Double.parseDouble(token) / 1000.0;
                    }
                }
            }

            if (km == 0.0) return Double.NaN;
            return km;

        } catch (Exception e) {
            return Double.NaN;
        }
    }

    public HeaderInfo detectHeader(Sheet sheet, String sheetName) {
        DataFormatter formatter = new DataFormatter();

        for (int i = 0; i < sheet.getPhysicalNumberOfRows() - 1; i++) {
            Row row1 = sheet.getRow(i);       // แถวที่อาจมี "กม."
            Row row2 = sheet.getRow(i + 1);   // แถวถัดไปที่อาจมี "N" และ "E"

            if (row1 == null || row2 == null) continue;

            int kmCol = -1, nCol = -1, eCol = -1, installCol = -1;

            // 🔍 หา "กม." ใน row1
            for (Cell cell : row1) {
                String text = formatter.formatCellValue(cell).trim();
                if (text.equals("กม.")) {
                    kmCol = cell.getColumnIndex();
                    break;
                }
            }

            if (kmCol == -1) continue; // ยังไม่เจอ "กม." → ข้ามไป

            // 🔍 หา "N" และ "E" ใน row2
            for (Cell cell : row2) {
                String text = formatter.formatCellValue(cell).trim();
                if (text.equalsIgnoreCase("N")) nCol = cell.getColumnIndex();
                if (text.equalsIgnoreCase("E")) eCol = cell.getColumnIndex();
            }

            // ❗ ถ้าเจอทั้ง "N" และ "E" แล้ว ถือว่าเป็น header ถูกต้อง
            if (nCol != -1 && eCol != -1) {

                // 🔍 หา installCol: อิงจาก merged หรือขยับถัดจาก kmCol
                for (CellRangeAddress range : sheet.getMergedRegions()) {
                    if (range.isInRange(row1.getRowNum(), kmCol)) {
                        installCol = range.getLastColumn() + 1;
                        break;
                    }
                }

                if (installCol == -1) {
                    installCol = kmCol + 1;
                }

                System.out.println("📘 Sheet: " + sheetName);
                System.out.println("📌 Header Detected:");
                System.out.println("   - kmCol       = " + kmCol);
                System.out.println("   - installCol  = " + installCol);
                System.out.println("   - nCol        = " + nCol);
                System.out.println("   - eCol        = " + eCol);
                System.out.println("   - headerRow   = " + i + " (Excel Row: " + (i + 1) + ")");

                return new HeaderInfo(kmCol, installCol, nCol, eCol, i);
            }
        }

        System.out.println("⛔️ Header not found in sheet: " + sheetName);
        return null;
    }

    public void importFromExcel(MultipartFile file, Integer startSheet, Integer endSheet, Integer rowStart, Integer rowEnd) throws Exception {
        try (InputStream is = file.getInputStream()) {
            Workbook workbook = new XSSFWorkbook(is);
            DataFormatter formatter = new DataFormatter();

            int sheetCount = workbook.getNumberOfSheets();

            // 🟢 หากเป็น -1 ให้ใช้ค่าครอบคลุมทั้งหมด
            int sheetStart = (startSheet == null || startSheet < 0) ? 0 : startSheet;
            int sheetEnd = (endSheet == null || endSheet < 0) ? sheetCount - 1 : endSheet;

            for (int sheetIndex = sheetStart; sheetIndex <= sheetEnd; sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                String sheetName = workbook.getSheetName(sheetIndex);
                String[] split = sheetName.trim().split(" ");
                String line = split.length >= 3 ? split[2] : "UNKNOWN";

                deleteByLine(line);

                HeaderInfo header = detectHeader(sheet, sheetName);
                if (header == null) continue;

                if (header.baseRow < 2) {
                    System.out.println("⚠️ Warning: Header detected too early at row " + (header.baseRow + 1) + ". This may be incorrect.");
                }

                int actualRowStart = (rowStart == null || rowStart < 0) ? header.baseRow + 1 : header.baseRow + rowStart + 2;
                int actualRowEnd = (rowEnd == null || rowEnd < 0) ? sheet.getLastRowNum() : header.baseRow + rowEnd + 1;

                for (int rowNum = actualRowStart; rowNum <= actualRowEnd; rowNum++) {
                    Row row = sheet.getRow(rowNum);
                    if (row == null) continue;

                    Cell kmCell = row.getCell(header.kmCol);
                    Cell installCell = row.getCell(header.installCol);
                    Cell nCell = row.getCell(header.nCol);
                    Cell eCell = row.getCell(header.eCol);

                    if (kmCell == null || nCell == null || eCell == null) {
                        System.out.println("⛔️ Incomplete data at row " + (rowNum + 1) + ", skipping.");
                        continue;
                    }

                    try {
                        StringBuilder kmBuilder = new StringBuilder();
                        for (int offset = 0; offset <= 2; offset++) {
                            Cell c = row.getCell(header.kmCol + offset);
                            if (c != null) {
                                String val = formatter.formatCellValue(c).trim();
                                if (!val.isEmpty()) {
                                    kmBuilder.append(val).append(" ");
                                }
                            }
                        }

                        String kmText = kmBuilder.toString().trim();
                        double km = parseKmFlexible(kmText);

                        String nText = formatter.formatCellValue(nCell).trim();
                        String eText = formatter.formatCellValue(eCell).trim();
                        Double n = null, e = null;
                        double[] latLng = null;

                        try {
                            n = Double.parseDouble(nText);
                            e = Double.parseDouble(eText);
                            latLng = UTMConverter.convertUTMToLatLng(e, n, 47, true);
                        } catch (NumberFormatException nfex) {
                            System.out.println("⚠️ Non-numeric N or E at row " + (rowNum + 1) + ": N='" + nText + "', E='" + eText + "'");
                        }

                        boolean installed = false;
                        if (installCell != null) {
                            String installText = formatter.formatCellValue(installCell).trim().toLowerCase();
                            if (!installText.isEmpty() && !installText.matches("[\\d.+ ]+")) {
                                installed = true;
                            }
                        }

                        String label = kmText.split(" ")[0].trim();

                        ConstructionColumn column = new ConstructionColumn();
                        column.setLabel(label);
                        column.setLine(line);
                        column.setKm(km);
                        column.setLat(latLng != null ? latLng[0] : null);
                        column.setLng(latLng != null ? latLng[1] : null);
                        column.setUtmEasting(e);
                        column.setUtmNorthing(n);
                        column.setInstalled(installed);
                        column.setZone("47N");

                        if (Double.isNaN(km)) {
                            System.out.println("⛔️ Skipped row " + (rowNum + 1) + ": KM is null or invalid.");
                            continue;
                        }

                        insert(column);
                        System.out.println("✅ Inserted row " + (rowNum + 1) + ": km = " + km + ", N = " + n + ", E = " + e);

                    } catch (Exception ex) {
                        System.out.println("❌ Error at row " + (rowNum + 1) + ": " + ex.getMessage());
                    }
                }
            }
        }
    }

    public void deleteByLine(String line) {
        String sql = "DELETE FROM construction WHERE line = ?";
        jdbcTemplate.update(sql, line);
        System.out.println("🗑️ Cleared existing data for line: " + line);
    }

    public KmBucketSummary getKmBucketSummary(String line, double startKm, double endKm) {
        String sql = "SELECT " +
                "COUNT(*) AS totalCount, " +
                "SUM(CASE WHEN installed = true THEN 1 ELSE 0 END) AS installedCount, " +
                "SUM(CASE WHEN installed = false THEN 1 ELSE 0 END) AS notInstalledCount " +
                "FROM construction " +
                "WHERE (? IS NULL OR ? = '' OR line = ?) " +
                "AND km BETWEEN ? AND ?";

        return jdbcTemplate.queryForObject(
                sql,
                new Object[]{line, line, line, startKm, endKm},
                (rs, rowNum) -> {
                    int total = rs.getInt("totalCount");
                    int installed = rs.getInt("installedCount");
                    int notInstalled = rs.getInt("notInstalledCount");

                    return new KmBucketSummary(
                            (line == null || line.isEmpty()) ? "All Lines" : line,
                            startKm,
                            endKm,
                            total,
                            installed,
                            notInstalled
                    );
                }
        );
    }








}





