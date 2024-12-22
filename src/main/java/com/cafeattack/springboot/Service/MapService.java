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
            SELECT c.cafe_name AS cafename,
                   (SELECT json_agg(cc.category) 
                    FROM cafe_categorypk cc 
                    WHERE cc.cafe_id = c.cafe_id) AS categories,
                   c.address, c.time, c.phone, c.latitude, c.longitude,
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

            // 카테고리 정보 추가 (배열 형태)
            if (result[1] != null) {
                String categoriesJson = (String) result[1];
                ArrayNode categoriesArray = (ArrayNode) objectMapper.readTree(categoriesJson);
                cafeInfo.set("categories", categoriesArray);
            } else {
                cafeInfo.putArray("categories"); // 빈 배열 반환
            }

            cafeInfo.put("address", (String) result[2]);
            cafeInfo.put("time", (String) result[3]);
            cafeInfo.put("phone", (String) result[4]);
            cafeInfo.put("latitude", result[5] != null ? ((Number) result[5]).doubleValue() : 0.0); // 위도 추가
            cafeInfo.put("longitude", result[6] != null ? ((Number) result[6]).doubleValue() : 0.0); // 경도 추가
            cafeInfo.put("avgscore", result[7] != null ? Math.round(((Number) result[7]).doubleValue() * 10) / 10.0 : 0.0);
            cafeInfo.put("heart", (Boolean) result[8]);
            cafeInfo.put("reviewcount", ((Number) result[9]).intValue());

            // 최근 리뷰 이미지 및 날짜 추가
            if (result[10] != null) {
                String recentReviewsJson = (String) result[10];
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
            SELECT c.cafe_name AS cafename,
                   (SELECT json_agg(cc.category) 
                    FROM cafe_categorypk cc 
                    WHERE cc.cafe_id = c.cafe_id) AS categories,
                   c.address, c.time, c.phone, c.avg_score, 
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

                        // 카테고리 정보 추가
                        String categoriesJson = cafeResult.getString("categories");
                        if (categoriesJson != null) {
                            ArrayNode categoriesArray = (ArrayNode) objectMapper.readTree(categoriesJson);
                            ((ObjectNode) cafeInfo).set("categories", categoriesArray);
                        } else {
                            ((ObjectNode) cafeInfo).putArray("categories");
                        }

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
    public String searchAllCafe(double longitude, double latitude, String query) {
        String jsonString = null;

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            // SQL 쿼리 - 카페 검색
            String cafeSearchQuery = """
            SELECT cafe_name, cafe_id, address, 
                   (6371 * acos(cos(radians(?)) * cos(radians(latitude)) 
                   * cos(radians(longitude) - radians(?)) + sin(radians(?)) 
                   * sin(radians(latitude)))) AS distance
            FROM cafe
            WHERE LOWER(cafe_name) LIKE ?
            ORDER BY distance ASC
        """;

            try (PreparedStatement statement = connection.prepareStatement(cafeSearchQuery)) {
                // 파라미터 설정
                statement.setDouble(1, latitude); // 중심 좌표의 위도
                statement.setDouble(2, longitude); // 중심 좌표의 경도
                statement.setDouble(3, latitude); // 중심 좌표의 위도 (계산용)
                statement.setString(4, "%" + query.toLowerCase() + "%"); // 검색어

                try (ResultSet resultSet = statement.executeQuery()) {
                    // JSON 변환
                    ObjectMapper objectMapper = new ObjectMapper();
                    ArrayNode resultsArray = objectMapper.createArrayNode();

                    while (resultSet.next()) {
                        ObjectNode cafeNode = objectMapper.createObjectNode();
                        cafeNode.put("place_name", resultSet.getString("cafe_name"));
                        cafeNode.put("id", resultSet.getInt("cafe_id"));
                        cafeNode.put("road_address_name", resultSet.getString("address"));
                        cafeNode.put("distance", Math.round(resultSet.getDouble("distance") * 1000)); // 미터 단위로 변환
                        resultsArray.add(cafeNode);
                    }

                    // 결과 JSON 생성
                    ObjectNode responseNode = objectMapper.createObjectNode();
                    responseNode.set("documents", resultsArray);
                    jsonString = objectMapper.writeValueAsString(responseNode);
                }
            }
        } catch (SQLException e) {
            throw new BaseException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
        } catch (Exception e) {
            throw new BaseException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
        }
        return jsonString;
    }


    // 카페 검색 (카테고리 별)
    public ResponseEntity<String> searchCafe(double longitude, double latitude, String query, int categoryId) {
        String jsonString = null;

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            // SQL 쿼리 - 카테고리별 카페 검색
            String cafeSearchQuery = """
            SELECT c.cafe_name AS place_name, c.cafe_id AS id, c.address AS road_address_name,
                   (6371 * acos(cos(radians(?)) * cos(radians(c.latitude)) 
                   * cos(radians(c.longitude) - radians(?)) + sin(radians(?)) 
                   * sin(radians(c.latitude)))) AS distance
            FROM cafe c
            JOIN cafe_categorypk cc ON c.cafe_id = cc.cafe_id
            WHERE LOWER(c.cafe_name) LIKE ? 
            AND cc.category = ?
            ORDER BY distance ASC
        """;

            try (PreparedStatement statement = connection.prepareStatement(cafeSearchQuery)) {
                // 파라미터 설정
                statement.setDouble(1, latitude); // 중심 좌표의 위도
                statement.setDouble(2, longitude); // 중심 좌표의 경도
                statement.setDouble(3, latitude); // 중심 좌표의 위도 (계산용)
                statement.setString(4, "%" + query.toLowerCase() + "%"); // 검색어
                statement.setInt(5, categoryId); // 카테고리

                try (ResultSet resultSet = statement.executeQuery()) {
                    // JSON 변환
                    ObjectMapper objectMapper = new ObjectMapper();
                    ArrayNode resultsArray = objectMapper.createArrayNode();

                    while (resultSet.next()) {
                        ObjectNode cafeNode = objectMapper.createObjectNode();
                        cafeNode.put("place_name", resultSet.getString("place_name"));
                        cafeNode.put("id", resultSet.getInt("id"));
                        cafeNode.put("road_address_name", resultSet.getString("road_address_name"));
                        cafeNode.put("distance", Math.round(resultSet.getDouble("distance") * 1000)); // 미터 단위로 변환
                        resultsArray.add(cafeNode);
                    }

                    // 결과 JSON 생성
                    ObjectNode responseNode = objectMapper.createObjectNode();
                    responseNode.set("documents", resultsArray);
                    jsonString = objectMapper.writeValueAsString(responseNode);
                }
            }
        } catch (SQLException e) {
            throw new BaseException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
        } catch (Exception e) {
            throw new BaseException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
        }
        return new ResponseEntity<>(jsonString, HttpStatus.OK);
    }

}
