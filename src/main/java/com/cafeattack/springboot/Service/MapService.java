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
import org.springframework.web.bind.annotation.GetMapping;
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


    // 정보 얻어오는 용도
    public String getCafeInformsFromMap(String longitude, String latitude, int radius) {
        String apiUrl = "https://dapi.kakao.com/v2/local/search/category.json";
        List<JsonNode> allResults = new ArrayList<>();
        Set<String> uniqueIds = new HashSet<>();  // 이미 수집된 데이터의 ID를 추적
        int page = 1;
        int total_count = 0;
        int collected_count = 0;
        int maxPage = 1;  // 초기 maxPage는 1로 설정, 첫 요청에서 계산됨

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            boolean isEnd = false;
            while(page <= maxPage && !isEnd) {
                // URL 구성
                String addr = apiUrl + "?category_group_code=CE7" + "&x=" + longitude + "&y=" + latitude + "&radius="
                        + radius + "&sort=distance&page=" + page;
                URL url = new URL(addr);
                URLConnection connection = url.openConnection();
                connection.setRequestProperty("Authorization", "KakaoAK " + apiKey);

                // 데이터 읽기
                String jsonString;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
                    StringBuffer docJson = new StringBuffer();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        docJson.append(line);
                    }
                    jsonString = docJson.toString();
                }
                // JSON 파싱
                JsonNode jsonNode = objectMapper.readTree(jsonString);
                JsonNode documents = jsonNode.path("documents");

                // 디버깅: 페이지 번호와 첫 번째 결과 출력
                if (!documents.isEmpty()) {
                    System.out.println("Page: " + page + ", First result: " + documents.get(0).toString());
                }

                // 첫 페이지에서 total_count 및 maxPage 계산
                if (page == 1) {
                    total_count = jsonNode.path("meta").path("total_count").asInt();
                    maxPage = (int)Math.ceil(total_count/15.0);  // 실제 조회 가능한 페이지 수 계산
                    System.out.println("Total_count:" + total_count + ", Max Page: " + maxPage);
                }

                for (JsonNode document : documents) {
                    allResults.add(document);
                }

                // 수집된 결과 개수 업데이트
                collected_count += documents.size();
                System.out.println("Collected count: " + collected_count);

                page++;
            }
            saveResultsToFile(allResults);

            return "완료";

        } catch (MalformedURLException e) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        } catch (IOException e) {
            throw new BaseException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
        }
    }

    // 결과를 파일로 저장하는 메서드
    private void saveResultsToFile(List<JsonNode> results) {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File("cafe_results.json");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            String jsonString = objectMapper.writeValueAsString(results);
            writer.write(jsonString);
            System.out.println("API 결과가 파일에 저장되었습니다.: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("파일 저장 중 오류 발생: " + e.getMessage());
        }
    }

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
            filteredDocuments.addAll(documents);

            /*
            for (JsonNode place : documents) {
                String categoryName = place.get("category_name").asText();

                ObjectNode filteredPlace = objectMapper.createObjectNode();
                filteredPlace.put("category_name", place.get("category_name").asText());
                filteredPlace.put("id", place.get("id").asText());
                filteredPlace.put("place_name", place.get("place_name").asText());
                filteredPlace.put("x", place.get("x").asText());
                filteredPlace.put("y", place.get("y").asText());
                // 제외해야할 정보 있으면 더 제외하기
                filteredDocuments.add(filteredPlace);
            }*/

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

    // 카테고리별 카페 지도에서 보기
    public String getCafeFromMap(int categoryId, String longitude, String latitude, int radius) {
        String apiUrl = "https://dapi.kakao.com/v2/local/search/category.json";
        String jsonString  = null;

        try {
            // categoryId로 DB에서 해당 카페의 ID 조회

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

            if (categoryId == 1) {
                for (JsonNode place : documents) {
                    String categoryName = place.get("category_name").asText();

                    ObjectNode filteredPlace = objectMapper.createObjectNode();
                    filteredPlace.put("category_name", place.get("category_name").asText());
                    filteredPlace.put("id", place.get("id").asText());
                    filteredPlace.put("place_name", place.get("place_name").asText());
                    filteredPlace.put("x", place.get("x").asText());
                    filteredPlace.put("y", place.get("y").asText());
                    // 제외해야할 정보 있으면 더 제외하기
                    filteredDocuments.add(filteredPlace);
                }
            } else if (categoryId >= 2 && categoryId <= 6) {
                Set<String> dbCafeIds = new HashSet<>();
                try (Connection dbConnection = dataSource.getConnection()) {
                    String dbQuery = "SELECT c.cafeId FROM Cafe c JOIN Category cat ON c.cafeId = cat.cafeId WHERE cat.categoryId=?";
                    try (PreparedStatement preparedStatement = dbConnection.prepareStatement(dbQuery)) {
                        preparedStatement.setInt(1, categoryId);  // categoryId를 SQL 쿼리에 안전하게 바인딩
                        try (ResultSet resultSet = preparedStatement.executeQuery()) {
                            while (resultSet.next()) {
                                dbCafeIds.add(resultSet.getString("cafeId"));  // 조회된 cafeId를 HashSet에 추가
                            }
                        }
                    }
                } catch (Exception e) {
                    throw new BaseException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
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
    public ResponseEntity<String> getShortCafes(String cafeId, int memberId) {
        String jsonString = null;

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            // SQL 쿼리  -- 피그마 보고 더 추가하기
            String query = "SELECT place_name, address_name, id, road_address_name From cafes " +
                    "WHERE id = ?";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, cafeId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        // JSON 변환
                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonNode cafeInfo = objectMapper.createObjectNode();

                        ((ObjectNode) cafeInfo).put("place_name", resultSet.getString("place_name"));
                        ((ObjectNode) cafeInfo).put("address_name", resultSet.getString("address_name"));
                        ((ObjectNode) cafeInfo).put("id", resultSet.getString("id"));
                        ((ObjectNode) cafeInfo).put("road_address_name", resultSet.getString("road_address_name"));

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
                filteredPlace.put("distance", place.get("distance").asText());
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
                    filteredPlace.put("distance", place.get("distance").asText());
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
