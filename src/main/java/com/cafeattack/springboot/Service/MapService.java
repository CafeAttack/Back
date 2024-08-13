package com.cafeattack.springboot.Service;

import com.cafeattack.springboot.Exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

@Service
@RequiredArgsConstructor
public class MapService {

    private final String apiKey;

    public String getAllCafeFromMap(int radius) {
        String apiUrl = "https://dapi.kakao.com/v2/local/search/category.json";
        String jsonString  = null;

        try {
            // URL 구성
            String addr = apiUrl + "?category_group_code=CE7&radius=" + radius;
            URL url = new URL(addr);
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("Authorization", "KakaoAK" + apiKey);
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            StringBuffer docJson = new StringBuffer();
            String line;
            while((line=reader.readLine()) != null) {
                docJson.append(line);
            }
            jsonString = docJson.toString();
            reader.close();
        } catch (MalformedURLException e) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        } catch (IOException e) {
            throw new BaseException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
        }
        return jsonString;
    }
}
