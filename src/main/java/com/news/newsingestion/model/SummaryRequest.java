package com.news.newsingestion.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@ToString(callSuper=true)
public class SummaryRequest {

    String topic;
    LocalDate date;
}
