package com.daangn.clone.encryption;

import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class AES128Test {

    @Autowired
    AES128 aes128;

    @Test
    public void test(){
        //given
        List<String> list = Arrays.asList("image1.jpeg", "image2.jpeg", "image3.jpeg", "image4.png", "image5.png");

        //when : 어쨋든 확실한건  -> "모든 파일의" 확장자가 jpeg 또는 png 이면 true
        boolean result = list.stream()
                .map(i -> FilenameUtils.getExtension(i))
                .allMatch(e -> e.equals("jpeg") || e.equals("png"));


        //then
        System.out.println("result = " + result);



    }
}