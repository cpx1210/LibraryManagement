package com.library.management.module.detection.dto;

import com.library.management.module.problembook.entity.ProblemBook;
import com.library.management.module.sensitiveword.entity.SensitiveWords;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 任务级检测参考数据快照。
 *
 * 用于避免在大批量检测时对每本书重复查询敏感词、问题书目和出版社白名单。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetectionReferenceDataSnapshot {

    @Builder.Default
    private List<SensitiveWords> keywordSensitiveWords = Collections.emptyList();

    @Builder.Default
    private List<SensitiveWords> bookNameSensitiveWords = Collections.emptyList();

    @Builder.Default
    private List<SensitiveWords> authorSensitiveWords = Collections.emptyList();

    @Builder.Default
    private Map<String, ProblemBook> problemBookByIsbn = Collections.emptyMap();

    @Builder.Default
    private List<ProblemBook> problemBooks = Collections.emptyList();

    @Builder.Default
    private List<ProblemBookMatchCandidate> problemBookNameCandidates = Collections.emptyList();

    @Builder.Default
    private Set<String> activeWhitelistPublishers = Collections.emptySet();
}
