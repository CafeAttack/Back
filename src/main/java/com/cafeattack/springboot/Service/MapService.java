package com.cafeattack.springboot.Service;

import com.cafeattack.springboot.Domain.Entity.Cafe;
import com.cafeattack.springboot.Exception.BaseException;
import com.cafeattack.springboot.Repository.CafeRepository;
import com.cafeattack.springboot.Repository.CategoryRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.time.format.DateTimeFormatter;
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
    public String getAllCafesFromMap(String longitude, String latitude, int radius) {
        String apiUrl = "https://dapi.kakao.com/v2/local/search/category.json";
        String jsonString  = null;

        try {
            // URL 구성
            String addr = apiUrl + "?category_group_code=CE7" + "&x=" + longitude + "&y=" + latitude + "&radius="
                    + radius + "&sort=distance";
            URL url = new URL(addr);
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("Authorization", "KakaoAK " + apiKey);
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            StringBuffer docJson = new StringBuffer();
            String line;
            while((line=reader.readLine()) != null) {
                docJson.append(line);
            }
            jsonString = docJson.toString();
            reader.close();

            // JSON 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            ArrayNode documents = (ArrayNode)jsonNode.get("documents");

            ArrayNode filteredDocuments = objectMapper.createArrayNode();

            for (JsonNode place : documents) {
                String categoryName = place.get("category_name").asText();

                ObjectNode filteredPlace = objectMapper.createObjectNode();
                filteredPlace.put("id", place.get("id").asText());
                filteredPlace.put("x", place.get("x").asText());
                filteredPlace.put("y", place.get("y").asText());
                // 제외해야할 정보 있으면 더 제외하기
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


    public String getCafeFromMap(int categoryId, String longitude, String latitude, int radius) {
        String apiUrl = "https://dapi.kakao.com/v2/local/search/category.json";
        String jsonString  = null;

        try {
            // URL 구성
            String addr = apiUrl + "?category_group_code=CE7" + "&x=" + longitude + "&y=" + latitude + "&radius="
                    + radius + "&sort=distance";
            URL url = new URL(addr);
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("Authorization", "KakaoAK " + apiKey);
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            StringBuffer docJson = new StringBuffer();
            String line;
            while((line=reader.readLine()) != null) {
                docJson.append(line);
            }
            jsonString = docJson.toString();
            reader.close();

            // JSON 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            ArrayNode documents = (ArrayNode)jsonNode.get("documents");

            Set<String> cafeIds = new HashSet<>();
            try (Connection dbConnection = dataSource.getConnection()) {
                String dbQuery = "SELECT c.cafeid FROM cafe c JOIN category cat ON c.cafeid = cat.cafeid WHERE cat.category = ?";
                try (PreparedStatement pstmt = dbConnection.prepareStatement(dbQuery)) {
                    pstmt.setInt(1, categoryId);  // categoryId를 SQL 쿼리에 안전하게 바인딩
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            cafeIds.add(rs.getString("cafeId"));  // 조회된 cafeId를 HashSet에 추가
                        }
                    }
                }
            } catch (Exception e) {
                throw new BaseException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
            }

            ArrayNode filteredDocuments = objectMapper.createArrayNode();
            for (JsonNode place : documents) {
                String placeId = place.get("id").asText();

                if (cafeIds.contains(placeId)) {
                    ObjectNode filteredPlace = objectMapper.createObjectNode();
                    filteredPlace.put("id", place.get("id").asText());
                    filteredPlace.put("x", place.get("x").asText());
                    filteredPlace.put("y", place.get("y").asText());
                    // 제외해야할 정보 있으면 더 제외하기
                    filteredDocuments.add(filteredPlace);
                }
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


    // 카페 선택 (간략한 정보)
    public ResponseEntity<String> getShortCafes(Integer cafeId, int memberId) {
        String jsonString = null;

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            // SQL 쿼리  - 카페 정보 가져옴
            String cafeQuery = "SELECT c.cafename, c.address, c.time, c.phone, STRING_AGG(cat.category::text, ',') AS categories, " +
                    "EXISTS(SELECT 1 FROM bookmark b WHERE b.memberid = ? AND b.relation_cafeid = ?) " +
                    "AS heart FROM cafe c LEFT JOIN category cat ON c.cafeid = cat.cafeid " +
                    "WHERE c.cafeid = ? GROUP BY c.cafeid";

            String reviewCountQuery = "SELECT COUNT(*) AS reviewCount FROM review WHERE cafeid = ?";

            String recentReviewQuery = "SELECT r.reviewdate, rp.picurl FROM reviewpics rp " +
                    "JOIN review r ON rp.reviewid = r.reviewid " +
                    "WHERE r.cafeid = ? AND rp.picurl IS NOT NULL " +
                    "ORDER BY r.reviewdate DESC LIMIT 3";

            String avgScoreQuery = "SELECT AVG(reviewscore) AS avgscore FROM review WHERE cafeid = ?";

            try (PreparedStatement statement = connection.prepareStatement(cafeQuery);
                PreparedStatement reviewCountStatement = connection.prepareStatement(reviewCountQuery);
                PreparedStatement recentReviewStatement = connection.prepareStatement(recentReviewQuery);
                PreparedStatement avgScoreStatement = connection.prepareStatement(avgScoreQuery)) {

                // 카페 정보 쿼리 실행
                statement.setInt(1, memberId);
                statement.setInt(2, cafeId);
                statement.setInt(3, cafeId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        // JSON 변환
                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonNode cafeInfo = objectMapper.createObjectNode();

                        ((ObjectNode) cafeInfo).put("cafename", resultSet.getString("cafename"));

                        //  평균 평점 쿼리 실행
                        avgScoreStatement.setInt(1, cafeId);
                        try (ResultSet avgScoreResultSet = avgScoreStatement.executeQuery()) {
                            if (avgScoreResultSet.next()) {
                                double avgScore = avgScoreResultSet.getDouble("avgscore");
                                double roundedAvgScore = Math.round(avgScore * 10.0) / 10.0;
                                ((ObjectNode) cafeInfo).put("avgscore", roundedAvgScore);
                            }
                        }

                        ((ObjectNode) cafeInfo).put("address", resultSet.getString("address"));
                        ((ObjectNode) cafeInfo).put("time", resultSet.getString("time"));
                        ((ObjectNode) cafeInfo).put("phone", resultSet.getString("phone"));

                        // 카테고리 번호 설정
                        String categoryIds = resultSet.getString("categories");
                        if (categoryIds != null && !categoryIds.isEmpty()) {
                            ((ObjectNode) cafeInfo).put("categories", categoryIds);
                        } else {
                            ((ObjectNode) cafeInfo).put("categories", "카테고리 없음");
                        }

                        // 북마크 여부 확인
                        boolean heart = resultSet.getBoolean("heart");
                        ((ObjectNode) cafeInfo).put("heart", heart);

                        // 리뷰 개수 쿼리 실행
                        reviewCountStatement.setInt(1, cafeId);
                        try (ResultSet reviewCountResultSet = reviewCountStatement.executeQuery()) {
                            if (reviewCountResultSet.next()) {
                                ((ObjectNode) cafeInfo).put("reviewcount",  reviewCountResultSet.getInt("reviewcount"));
                            }
                        }

                        // 최근 리뷰 3개 가져오기
                        recentReviewStatement.setInt(1, cafeId);
                        try (ResultSet recentReviewResultSet = recentReviewStatement.executeQuery()) {
                            ArrayNode recentReviewArray = objectMapper.createArrayNode();

                            while (recentReviewResultSet.next()) {
                                ObjectNode reviewNode = objectMapper.createObjectNode();

                                // reviewdate 문자열을 가져온 후 형식 변경 (YYYY-MM-DD -> YY.MM.DD)
                                String reviewDate = recentReviewResultSet.getString("reviewdate");
                                if (reviewDate != null && reviewDate.length() >= 10) { // null 및 잘못된 데이터 확인
                                    String formattedDate = reviewDate.substring(2, 4) + "." + reviewDate.substring(5, 7) + "." + reviewDate.substring(8, 10);
                                    reviewNode.put("reviewdate", formattedDate);
                                }

                                reviewNode.put("picurl", recentReviewResultSet.getString("picurl"));
                                recentReviewArray.add(reviewNode);
                            }
                            ((ObjectNode) cafeInfo).set("recentReviews", recentReviewArray);
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

    // 카페 정보 더보기
    public ResponseEntity<String> getMoreCafes(Integer cafeId, int memberId) {
        String jsonString = null;

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            // SQL 쿼리  - 카페 정보 가져옴
            String cafeQuery = "SELECT c.cafename, c.address, c.time, c.phone, STRING_AGG(cat.category::text, ',') AS categories, " +
                    "EXISTS(SELECT 1 FROM bookmark b WHERE b.memberid = ? AND b.relation_cafeid = ?) " +
                    "AS heart FROM cafe c LEFT JOIN category cat ON c.cafeid = cat.cafeid " +
                    "WHERE c.cafeid = ? GROUP BY c.cafeid";

            String reviewCountQuery = "SELECT COUNT(*) AS reviewCount FROM review WHERE cafeid = ?";

            String allReviewQuery = "SELECT r.reviewwriter AS nickname, r.reviewdate, r.reviewscore, r.reviewtext, rp.picurl " +
                            "FROM review r LEFT JOIN reviewpics rp ON r.reviewid = rp.reviewid " +
                            "WHERE r.cafeid = ? ORDER BY r.reviewdate DESC";

            String avgScoreQuery = "SELECT AVG(reviewscore) AS avgscore FROM review WHERE cafeid = ?";

            try (PreparedStatement statement = connection.prepareStatement(cafeQuery);
                 PreparedStatement reviewCountStatement = connection.prepareStatement(reviewCountQuery);
                 PreparedStatement allReviewsStatement = connection.prepareStatement(allReviewQuery);
                 PreparedStatement avgScoreStatement = connection.prepareStatement(avgScoreQuery)) {

                // 카페 정보 쿼리 실행
                statement.setInt(1, memberId);
                statement.setInt(2, cafeId);
                statement.setInt(3, cafeId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        // JSON 변환
                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonNode cafeInfo = objectMapper.createObjectNode();

                        ((ObjectNode) cafeInfo).put("cafename", resultSet.getString("cafename"));

                        boolean heart = resultSet.getBoolean("heart");
                        ((ObjectNode) cafeInfo).put("heart", heart);
                        ((ObjectNode) cafeInfo).put("address", resultSet.getString("address"));
                        ((ObjectNode) cafeInfo).put("time", resultSet.getString("time"));
                        ((ObjectNode) cafeInfo).put("phone", resultSet.getString("phone"));

                        // 카테고리 번호 설정
                        String categoryIds = resultSet.getString("categories");
                        if (categoryIds != null && !categoryIds.isEmpty()) {
                            ((ObjectNode) cafeInfo).put("categories", categoryIds);
                        } else {
                            ((ObjectNode) cafeInfo).put("categories", "카테고리 없음");
                        }

                        // 리뷰 개수 쿼리 실행
                        reviewCountStatement.setInt(1, cafeId);
                        try (ResultSet reviewCountResultSet = reviewCountStatement.executeQuery()) {
                            if (reviewCountResultSet.next()) {
                                ((ObjectNode) cafeInfo).put("reviewcount",  reviewCountResultSet.getInt("reviewcount"));
                            }
                        }

                        //  평균 평점 쿼리 실행
                        avgScoreStatement.setInt(1, cafeId);
                        try (ResultSet avgScoreResultSet = avgScoreStatement.executeQuery()) {
                            if (avgScoreResultSet.next()) {
                                double avgScore = avgScoreResultSet.getDouble("avgscore");
                                double roundedAvgScore = Math.round(avgScore * 10.0) / 10.0;
                                ((ObjectNode) cafeInfo).put("avgscore", roundedAvgScore);
                            }
                        }

                        // 모든 리뷰 가져오기
                        allReviewsStatement.setInt(1, cafeId);
                        try (ResultSet allReviewsResultSet = allReviewsStatement.executeQuery()) {
                            ArrayNode allReviewsArray = objectMapper.createArrayNode();

                            while (allReviewsResultSet.next()) {
                                ObjectNode reviewNode = objectMapper.createObjectNode();

                                reviewNode.put("nickname", allReviewsResultSet.getString("nickname"));

                                // reviewdate 문자열을 가져온 후 형식 변경 (YYYY-MM-DD -> YY.MM.DD)
                                String reviewDate = allReviewsResultSet.getString("reviewdate");
                                if (reviewDate != null && reviewDate.length() >= 10) { // null 및 잘못된 데이터 확인
                                    String formattedDate = reviewDate.substring(2, 4) + "." + reviewDate.substring(5, 7) + "." + reviewDate.substring(8, 10);
                                    reviewNode.put("reviewdate", formattedDate);
                                }

                                reviewNode.put("reviewscore", allReviewsResultSet.getInt("reviewscore"));
                                reviewNode.put("reviewtext", allReviewsResultSet.getString("reviewtext"));
                                reviewNode.put("picurl", allReviewsResultSet.getString("picurl"));

                                allReviewsArray.add(reviewNode);
                            }
                            ((ObjectNode) cafeInfo).set("reviews", allReviewsArray);
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
