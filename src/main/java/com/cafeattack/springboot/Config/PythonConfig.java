package com.cafeattack.springboot.Config;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PythonConfig {
    public static String[] getCafeInformations(int cafeId) {
        try {
            // Python 스크립트 실행하면서 cafeId 인자 전달
            ProcessBuilder processBuilder = new ProcessBuilder("python",
                    "data.py", String.valueOf(cafeId));
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Python 스크립트의 출력 결과 읽기
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();

            // 스크립트 실행 종료 기다림
            process.waitFor();

            // 평점과 리뷰 개수 콤마로 구분된 문자열에서 추출
            if (line != null && !line.isEmpty()) {
                String[] parts  = line.split(",");
                if (parts.length == 2) {
                    return new String[] {parts[0], parts[1]};
                }
            }
            return new String[] {"N/A", "N/A"}; // 기본값
        } catch (Exception e) {
            e.printStackTrace();
            return new String[] {"N/A", "N/A"}; // 에러 시 기본 값
        }
    }
}
