package com.cafeattack.springboot.Service;

import org.springframework.http.ResponseEntity;

public interface MapServiceImpl {
    ResponseEntity menu_Page(Integer member_id);

    //ResponseEntity change_Info();
}