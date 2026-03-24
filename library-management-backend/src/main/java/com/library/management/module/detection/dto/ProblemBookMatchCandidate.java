package com.library.management.module.detection.dto;

import com.library.management.module.problembook.entity.ProblemBook;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 问题书目匹配候选项。
 *
 * 预先缓存标准化后的题名，避免全库检测时重复规范化和重复创建中间对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProblemBookMatchCandidate {

    private ProblemBook problemBook;

    private String normalizedBookName;

    private int normalizedBookNameLength;
}
