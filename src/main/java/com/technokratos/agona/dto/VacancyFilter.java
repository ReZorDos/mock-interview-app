package com.technokratos.agona.dto;

import com.technokratos.agona.enums.ScheduleType;
import com.technokratos.agona.enums.VacancyLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VacancyFilter {

    private String title;
    private String city;
    private String skill;
    private VacancyLevel level;
    private ScheduleType schedule;
    private Integer salaryFrom;
    private Integer salaryTo;
    private String sort = "newest";
}
