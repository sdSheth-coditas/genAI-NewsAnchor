package com.news.newsingestion.dto;

import lombok.Data;
import java.util.List;

@Data
public class SignUpRequest {
    private String email;
    private String password;
    private String name;
    private List<String> topics;
}
