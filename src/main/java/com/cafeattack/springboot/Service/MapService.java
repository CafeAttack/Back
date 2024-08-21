package com.cafeattack.springboot.Service;

import com.cafeattack.springboot.Domain.Entity.Cafe;
import com.cafeattack.springboot.Domain.Entity.Category;
import com.cafeattack.springboot.Exception.BaseException;
import com.cafeattack.springboot.Repository.CafeRepository;
import com.cafeattack.springboot.Repository.CategoryRepository;
import com.cafeattack.springboot.Util.RedisUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import javax.sql.DataSource;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MapService {

    private final String apiKey;
    private final DataSource dataSource;
    private final CafeRepository cafeRepository;
    private final CategoryRepository categoryRepository;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    // 카테고리 관계 없이 모든 카페 지도에서 보기
    public String getAllCafesFromMap() {
        String jsonString  = null;

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            String query = "SELECT cafeid, latitude, longitude FROM cafe";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    ArrayNode cafeArray = objectMapper.createArrayNode();

                    while (resultSet.next()) {
                        ObjectNode cafeObject = objectMapper.createObjectNode();
                        cafeObject.put("cafeid", String.valueOf(resultSet.getInt("cafeid")));
                        cafeObject.put("longitude", resultSet.getBigDecimal("longitude"));
                        cafeObject.put("latitude", resultSet.getBigDecimal("latitude"));

                        cafeArray.add(cafeObject);
                    }
                    jsonString = objectMapper.writeValueAsString(cafeArray);
                }
            }
        } catch (SQLException e) {
            throw new BaseException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
        } catch (Exception e) {
            throw new BaseException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
        }
        return jsonString;
    }

    // 카테고리별 카페 지도에서 보기
    public String getCafeFromMap(int categoryId) {
        String jsonString  = null;

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            String query = "SELECT c.cafeid, c.latitude, c.longitude FROM cafe c " +
                    "JOIN category cat ON c.cafeid = cat.cafeid WHERE cat.category = ?";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, categoryId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    ArrayNode cafeArray = objectMapper.createArrayNode();

                    while (resultSet.next()) {
                        ObjectNode cafeObject = objectMapper.createObjectNode();
                        cafeObject.put("cafeid", String.valueOf(resultSet.getInt("cafeid")));
                        cafeObject.put("longitude", resultSet.getBigDecimal("longitude"));
                        cafeObject.put("latitude", resultSet.getBigDecimal("latitude"));

                        cafeArray.add(cafeObject);
                    }
                    jsonString = objectMapper.writeValueAsString(cafeArray);
                }
            }
        } catch (SQLException e) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        } catch (Exception e) {
            throw new BaseException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
        }
        return jsonString;
    }


    // 카페 선택 (간략한 정보)
    public ResponseEntity<String> getShortCafes(Integer cafeId, int memberId) {
        String jsonString = null;

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            // SQL 쿼리  - 카페 정보 가져옴
            String query = "SELECT cafename, address, phone From cafe WHERE cafeid = ?";

            // 카페의 모든 카테고리 가져옴
            String categoryQuery = "SELECT category FROM category WHERE cafeid = ?";

            // 사용자 그룹에서 북마크 여부 확인 - 존재 여부만 확인
            String bookmarkQuery = "SELECT 1 FROM bookmark WHERE memberid = ? AND relation_cafeid = ?";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, cafeId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        // JSON 변환
                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonNode cafeInfo = objectMapper.createObjectNode();

                        ((ObjectNode) cafeInfo).put("cafename", resultSet.getString("cafename"));
                        ((ObjectNode) cafeInfo).put("address", resultSet.getString("address"));
                        ((ObjectNode) cafeInfo).put("phone", resultSet.getString("phone"));

                        StringBuilder categoryNames = new StringBuilder();

                        // 카테고리 조회
                        try (PreparedStatement categoryStatement = connection.prepareStatement(categoryQuery)) {
                            categoryStatement.setInt(1, cafeId);

                            try (ResultSet categorySet = categoryStatement.executeQuery()) {
                                while (categorySet.next()) {
                                    int categoryId = categorySet.getInt("category");

                                    String categoryName = getCategoryName(categoryId);
                                    if (categoryName != null) {
                                        if (categoryNames.length() > 0) {
                                            categoryNames.append(" / ");
                                        }
                                        categoryNames.append(categoryName);
                                    }
                                }
                            }
                        }

                        if (categoryNames.length() > 0) {
                            categoryNames.append(" 카페");
                        }
                        ((ObjectNode) cafeInfo).put("categories", categoryNames.toString());

                        // 북마크 여부 확인
                        boolean isBookmarked = false;
                        try (PreparedStatement bookmarkStatement = connection.prepareStatement(bookmarkQuery)) {
                            bookmarkStatement.setInt(1, memberId);
                            bookmarkStatement.setInt(2, cafeId);

                            try (ResultSet bookmarkResultSet = bookmarkStatement.executeQuery()) {
                                if (bookmarkResultSet.next()) {
                                    isBookmarked = true;
                                }
                            }
                        }
                        ((ObjectNode) cafeInfo).put("isBookmarked", isBookmarked);

                        jsonString = objectMapper.writeValueAsString(cafeInfo);
                        return ResponseEntity.ok(jsonString);
                    } else {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cafe Not Found");
                    }
                }
            }
        } catch (SQLException e) {
            throw new BaseException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
        } catch (Exception e) {
            throw new BaseException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
        }
    }

    // 카테고리 이름 매핑 함수
    private String getCategoryName(int categoryId) {
        switch (categoryId) {
            case 1: return "테이크 아웃";
            case 2: return "감성";
            case 3: return "프랜차이즈";
            case 4: return "카공";
            case 5: return "테마";
            default: return null;
        }
    }


    // 카페 검색 (ALL)
    public String searchAllCafe(String longitude, String latitude, int radius, String query) {
        String apiUrl = "https://dapi.kakao.com/v2/local/search/keyword.json";
        String jsonString  = null;

        try {
            String addr = apiUrl + "?category_group_code=CE7&y=" + latitude + "&x=" + longitude + "&radius=" + radius
                    + "&query=" + java.net.URLEncoder.encode(query, "UTF-8");
            URL url = new URL(addr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Authorization", "KakaoAK " + apiKey);

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            StringBuffer docJson = new StringBuffer();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                docJson.append(line);
            }
            bufferedReader.close();
            jsonString = docJson.toString();

            // JSON 파싱 & 필터링
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            ArrayNode documents = (ArrayNode)jsonNode.get("documents");
            ArrayNode filteredDocuments = objectMapper.createArrayNode();

            for (JsonNode place : documents) {
                ObjectNode filteredPlace = objectMapper.createObjectNode();
                filteredPlace.put("place_name", place.get("place_name").asText());
                filteredPlace.put("id", place.get("id").asText());
                filteredPlace.put("road_address_name", place.get("road_address_name").asText());
                filteredPlace.put("distance", Integer.valueOf(place.get("distance").asText()));
                filteredDocuments.add(filteredPlace);
            }

            // 필터링된 결과 JSON 변환
            ((ObjectNode) jsonNode).set("documents", filteredDocuments);
            jsonString = objectMapper.writeValueAsString(jsonNode);
        } catch (MalformedURLException e) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        } catch (IOException e) {
            throw new BaseException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
        }
        return jsonString;
    }

    // 카페 검색 (카테고리 별)
    public ResponseEntity<String> searchCafe(String longitude, String latitude, int radius, String query, int categoryId) {
        String apiUrl = "https://dapi.kakao.com/v2/local/search/keyword.json";
        String jsonString = null;

        try {
            String addr = apiUrl + "?category_group_code=CE7&y=" + latitude + "&x=" + longitude
                    + "&radius=" + radius + "&query=" + java.net.URLEncoder.encode(query, "UTF-8");
            URL url = new URL(addr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Authorization", "KakaoAK " + apiKey);

            // API 응답 받기
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            StringBuffer docJson = new StringBuffer();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                docJson.append(line);
            }
            bufferedReader.close();
            jsonString = docJson.toString();

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            ArrayNode documents = (ArrayNode)jsonNode.get("documents");
            ArrayNode filteredDocuments = objectMapper.createArrayNode();

            // DB에서 카테고리 ID로 필터링된 카페 정보 가져오기
            List<Cafe> cafes = categoryRepository.findByCategoryId(categoryId);

            for (JsonNode place : documents) {
                String placeId = place.get("id").asText();

                if (cafes.stream().anyMatch(cafe -> String.valueOf(cafe.getCafeid()).equals(placeId))) {
                    ObjectNode filteredPlace = objectMapper.createObjectNode();
                    filteredPlace.put("place_name", place.get("place_name").asText());
                    filteredPlace.put("id", place.get("id").asText());
                    filteredPlace.put("road_address_name", place.get("road_address_name").asText());
                    filteredPlace.put("distance", Integer.valueOf(place.get("distance").asText()));
                    filteredDocuments.add(filteredPlace);
                }
            }
            ((ObjectNode) jsonNode).set("documents", filteredDocuments);
            jsonString = objectMapper.writeValueAsString(jsonNode);

            return new ResponseEntity<>(jsonString, HttpStatus.OK);
        } catch (MalformedURLException e) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        } catch (IOException e) {
            throw new BaseException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
        }
    }
}
