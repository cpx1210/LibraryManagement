package com.library.management.module.detection.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 公开上传页提交人信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BooklistUploadSubmitterDTO {

    private String submitterName;

    private String department;

    private String email;

    private String employeeNo;

    private String mobile;
}
