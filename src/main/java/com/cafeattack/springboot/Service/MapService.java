package com.cafeattack.springboot.Service;

import com.cafeattack.springboot.Domain.Dto.response.CafeCategoryLocationDto;
import com.cafeattack.springboot.Domain.Dto.response.CafeLocationDto;
import com.cafeattack.springboot.Domain.Entity.Cafe;
import com.cafeattack.springboot.Exception.BaseException;
import com.cafeattack.springboot.Repository.CafeCategoryPKRepository;
import com.cafeattack.springboot.Repository.CafeRepository;
import com.cafeattack.springboot.Repository.CategoryRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.*;
import java.math.BigDecimal;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MapService {

    private final String apiKey;
    private final DataSource dataSource;
    private final CafeRepository cafeRepository;
    private final CategoryRepository categoryRepository;
    private final CafeCategoryPKRepository cafeCategoryPKRepository;

    @Autowired
    private EntityManager entityManager;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;


    // 카테고리 관계 없이 모든 카페 지도에서 보기
    public List<CafeLocationDto> getAllCafesFromMap(Double longitude, Double latitude) {
        List<Object[]> results = cafeRepository.findCafesWithinRadius(latitude, longitude);

        return results.stream()
                .map(row -> new CafeLocationDto(
                        (Integer) row[0],
                        ((BigDecimal) row[1]).doubleValue(),    // latitude
                        ((BigDecimal) row[2]).doubleValue()    // longitude
                ))
                .collect(Collectors.toList());
    }

    public List<CafeCategoryLocationDto> getCafeFromMap(int categoryId, double longitude, double latitude) {
        List<Object[]> results = cafeRepository.findCafesByCategoryAndRadius(categoryId, longitude, latitude);

        return results.stream()
                .map(row ->
                    new CafeCategoryLocationDto(
                        (Integer) row[0],
                        ((BigDecimal) row[1]).doubleValue(),
                        ((BigDecimal) row[2]).doubleValue())
                )
                .collect(Collectors.toList());
    }



    // 카페 선택 (간략한 정보)
    public ResponseEntity<String> getShortCafes(Integer cafeId, int memberId) {
        try {
            // 카페 정보 쿼리
            String cafeQuery = """
            SELECT c.cafe_name AS cafename, c.address, c.time, c.phone,
                   COALESCE((SELECT AVG(r.review_score) FROM review r WHERE r.cafe_id = :cafeId), 0) AS avgscore,
                   EXISTS(SELECT 1 FROM bookmark b 
                          JOIN group_cafepk gcp ON b.group_id = gcp.group_id 
                          WHERE b.member_id = :memberId AND gcp.cafe_id = :cafeId) AS heart,
                   (SELECT COUNT(*) FROM review r WHERE r.cafe_id = :cafeId) AS reviewcount,
                   (SELECT json_agg(
                                json_build_object(
                                    'picurl', rp.picurl,
                                    'reviewdate', r.review_date
                                )
                          )
                          FROM reviewpics rp
                          JOIN review r ON rp.reviewid = r.review_id
                          WHERE r.cafe_id = :cafeId AND rp.picurl IS NOT NULL
                          GROUP BY r.review_date, rp.picurl
                          ORDER BY r.review_date DESC LIMIT 3) AS recentreviews
            FROM cafe c 
            WHERE c.cafe_id = :cafeId
        """;

            Query query = entityManager.createNativeQuery(cafeQuery);
            query.setParameter("memberId", memberId);
            query.setParameter("cafeId", cafeId);

            Object[] result = (Object[]) query.getSingleResult();

            // JSON 변환
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode cafeInfo = objectMapper.createObjectNode();

            cafeInfo.put("cafename", (String) result[0]);
            cafeInfo.put("address", (String) result[1]);
            cafeInfo.put("time", (String) result[2]);
            cafeInfo.put("phone", (String) result[3]);
            cafeInfo.put("avgscore", result[4] != null ? Math.round(((Number) result[4]).doubleValue() * 10) / 10.0 : 0.0);
            cafeInfo.put("heart", (Boolean) result[5]);
            cafeInfo.put("reviewcount", ((Number) result[6]).intValue());

            // 최근 리뷰 이미지 및 날짜 추가
            if (result[7] != null) {
                String recentReviewsJson = (String) result[7];
                ArrayNode recentReviewArray = (ArrayNode) objectMapper.readTree(recentReviewsJson);
                cafeInfo.set("recentReviews", recentReviewArray);
            } else {
                cafeInfo.putArray("recentReviews"); // 빈 배열 반환
            }

            return ResponseEntity.ok(objectMapper.writeValueAsString(cafeInfo));

        } catch (NoResultException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cafe Not Found");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while processing request");
        }
    }


    // 카페 정보 더보기
    public ResponseEntity<String> getMoreCafes(Integer cafeId, int memberId) {
        String jsonString = null;

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            // SQL 쿼리
            String cafeQuery = """
            SELECT c.cafe_name AS cafename, c.address, c.time, c.phone, c.avg_score, 
                   EXISTS(SELECT 1 FROM bookmark b 
                          JOIN group_cafepk gcp ON b.group_id = gcp.group_id 
                          WHERE b.member_id = ? AND gcp.cafe_id = ?) AS heart
            FROM cafe c
            WHERE c.cafe_id = ?
        """;

            String reviewQuery = """
            SELECT r.review_writer AS nickname, r.review_date, r.review_score, r.review_text, rp.picurl 
            FROM review r 
            LEFT JOIN reviewpics rp ON r.review_id = rp.reviewid 
            WHERE r.cafe_id = ? 
            ORDER BY r.review_date DESC
        """;

            String amenitiesQuery = """
            SELECT amenities 
            FROM review_amenities ra 
            WHERE ra.review_id IN (SELECT r.review_id FROM review r WHERE r.cafe_id = ?)
        """;

            String tagsQuery = """
            SELECT t.tag_name AS tag, COUNT(rt.tag_id) AS count 
            FROM tag t 
            JOIN review_tagspk rt ON t.tag_id = rt.tag_id 
            JOIN review r ON rt.review_id = r.review_id 
            WHERE r.cafe_id = ? 
            GROUP BY t.tag_name 
            ORDER BY count DESC
        """;

            try (PreparedStatement cafeStmt = connection.prepareStatement(cafeQuery);
                 PreparedStatement reviewStmt = connection.prepareStatement(reviewQuery);
                 PreparedStatement amenitiesStmt = connection.prepareStatement(amenitiesQuery);
                 PreparedStatement tagsStmt = connection.prepareStatement(tagsQuery)) {

                // 카페 정보 쿼리
                cafeStmt.setInt(1, memberId);
                cafeStmt.setInt(2, cafeId);
                cafeStmt.setInt(3, cafeId);

                try (ResultSet cafeResult = cafeStmt.executeQuery()) {
                    if (cafeResult.next()) {
                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonNode cafeInfo = objectMapper.createObjectNode();

                        ((ObjectNode) cafeInfo).put("cafename", cafeResult.getString("cafename"));
                        ((ObjectNode) cafeInfo).put("address", cafeResult.getString("address"));
                        ((ObjectNode) cafeInfo).put("time", cafeResult.getString("time"));
                        ((ObjectNode) cafeInfo).put("phone", cafeResult.getString("phone"));
                        ((ObjectNode) cafeInfo).put("avgscore", cafeResult.getDouble("avg_score"));
                        ((ObjectNode) cafeInfo).put("heart", cafeResult.getBoolean("heart"));

                        // 리뷰 정보
                        reviewStmt.setInt(1, cafeId);
                        try (ResultSet reviewResult = reviewStmt.executeQuery()) {
                            ArrayNode reviewsArray = objectMapper.createArrayNode();
                            while (reviewResult.next()) {
                                ObjectNode reviewNode = objectMapper.createObjectNode();
                                reviewNode.put("nickname", reviewResult.getString("nickname"));
                                String reviewDate = reviewResult.getString("review_date");
                                if (reviewDate != null) {
                                    reviewNode.put("reviewdate", reviewDate.substring(2, 4) + "." + reviewDate.substring(5, 7) + "." + reviewDate.substring(8, 10));
                                }
                                reviewNode.put("reviewscore", reviewResult.getInt("review_score"));
                                reviewNode.put("reviewtext", reviewResult.getString("review_text"));
                                reviewNode.put("picurl", reviewResult.getString("picurl"));
                                reviewsArray.add(reviewNode);
                            }
                            ((ObjectNode) cafeInfo).set("reviews", reviewsArray);
                        }

                        // 편의시설
                        amenitiesStmt.setInt(1, cafeId);
                        try (ResultSet amenitiesResult = amenitiesStmt.executeQuery()) {
                            ArrayNode amenitiesArray = objectMapper.createArrayNode();
                            while (amenitiesResult.next()) {
                                ObjectNode amenityNode = objectMapper.createObjectNode();
                                amenityNode.put("name", amenitiesResult.getString("amenities"));
                                amenitiesArray.add(amenityNode);
                            }
                            ((ObjectNode) cafeInfo).set("amenities", amenitiesArray);
                        }

                        // 태그 정보
                        tagsStmt.setInt(1, cafeId);
                        try (ResultSet tagsResult = tagsStmt.executeQuery()) {
                            ArrayNode tagsArray = objectMapper.createArrayNode();
                            while (tagsResult.next()) {
                                ObjectNode tagNode = objectMapper.createObjectNode();
                                tagNode.put("tag", tagsResult.getString("tag"));
                                tagNode.put("count", tagsResult.getInt("count"));
                                tagsArray.add(tagNode);
                            }
                            ((ObjectNode) cafeInfo).set("tags", tagsArray);
                        }

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
            List<Cafe> cafes = cafeCategoryPKRepository.findByCategoryId(categoryId);

            for (JsonNode place : documents) {
                String placeId = place.get("id").asText();

                if (cafes.stream().anyMatch(cafe -> String.valueOf(cafe.getCafeId()).equals(placeId))) {
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
