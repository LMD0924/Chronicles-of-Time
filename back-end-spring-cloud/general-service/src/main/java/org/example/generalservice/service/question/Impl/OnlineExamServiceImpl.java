/**
 * 文件说明：在线练习、考试和错题练习业务实现。
 */
package org.example.generalservice.service.question.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.generalservice.dto.question.ExamStartRequest;
import org.example.generalservice.dto.question.ExamSubmitAnswer;
import org.example.generalservice.dto.question.ExamSubmitRequest;
import org.example.generalservice.entity.AnswerRecords;
import org.example.generalservice.entity.MistakeRecord;
import org.example.generalservice.entity.PracticeSession;
import org.example.generalservice.entity.QuestionBank;
import org.example.generalservice.mapper.question.AnswerRecordsMapper;
import org.example.generalservice.mapper.question.MistakeRecordMapper;
import org.example.generalservice.mapper.question.PracticeSessionMapper;
import org.example.generalservice.mapper.question.QuestionBankMapper;
import org.example.generalservice.service.question.OnlineExamService;
import org.example.generalservice.vo.question.ExamDetailVO;
import org.example.generalservice.vo.question.ExamHistoryVO;
import org.example.generalservice.vo.question.ExamStartVO;
import org.example.generalservice.vo.question.ExamSubmitVO;
import org.example.generalservice.vo.question.QuestionResultVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnlineExamServiceImpl implements OnlineExamService {

    private static final int DEFAULT_QUESTION_COUNT = 10;

    private final QuestionBankMapper questionBankMapper;
    private final PracticeSessionMapper practiceSessionMapper;
    private final AnswerRecordsMapper answerRecordsMapper;
    private final MistakeRecordMapper mistakeRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExamStartVO startExam(ExamStartRequest request) {
        Long userId = requireUserId(request.getUserId());
        String mode = normalizeMode(request.getMode());
        int questionCount = request.getQuestionCount() == null || request.getQuestionCount() <= 0
                ? DEFAULT_QUESTION_COUNT : request.getQuestionCount();

        List<QuestionBank> candidates = "mistake".equals(mode)
                ? getMistakeCandidates(userId, request)
                : getApprovedCandidates(userId, request);

        List<QuestionBank> selected = drawQuestions(candidates, questionCount, "mistake".equals(mode));
        if (selected.isEmpty()) {
            throw new IllegalArgumentException("当前条件下暂无可用题目，请调整分类、知识点或难度");
        }

        PracticeSession session = new PracticeSession();
        session.setUserId(userId);
        session.setSessionType(mode);
        session.setTitle(StringUtils.hasText(request.getTitle()) ? request.getTitle() : defaultTitle(mode));
        session.setCategoryLevel(request.getCategoryLevel());
        session.setSubjectName(request.getSubjectName());
        session.setKnowledgePoints(joinPoints(request.getKnowledgePoints()));
        session.setDifficultyLevel(request.getDifficultyLevel());
        session.setQuestionIds(selected.stream().map(q -> String.valueOf(q.getId())).collect(Collectors.joining(",")));
        session.setTotalQuestions(selected.size());
        session.setAnsweredQuestions(0);
        session.setCorrectCount(0);
        session.setWrongCount(0);
        session.setScoreTotal(selected.stream().mapToInt(this::scoreOf).sum());
        session.setScoreObtained(0);
        session.setDurationSeconds(request.getDurationSeconds());
        session.setAntiCheatEnabled(("exam".equals(mode) && !Boolean.FALSE.equals(request.getAntiCheatEnabled())) ? 1 : 0);
        session.setSuspiciousCount(0);
        session.setStartedAt(LocalDateTime.now());
        session.setStatus(1);
        practiceSessionMapper.insert(session);

        ExamStartVO vo = new ExamStartVO();
        vo.setSessionId(session.getId());
        vo.setMode(mode);
        vo.setTotalQuestions(selected.size());
        vo.setDurationSeconds(session.getDurationSeconds());
        vo.setAntiCheatEnabled(session.getAntiCheatEnabled() == 1);
        vo.setDrawStrategy("先按用户私有题库、审核通过、分类、知识点和难度过滤，再按少做优先、错题率、随机扰动加权抽题");
        vo.setQuestions(selected.stream().map(this::hideAnswer).collect(Collectors.toList()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExamSubmitVO submitExam(ExamSubmitRequest request) {
        Long userId = requireUserId(request.getUserId());
        PracticeSession session = practiceSessionMapper.selectById(request.getSessionId());
        if (session == null || !userId.equals(session.getUserId())) {
            throw new IllegalArgumentException("考试记录不存在或无权提交");
        }
        if (session.getStatus() != null && session.getStatus() == 2) {
            return buildSubmitResult(session, getQuestionResults(userId, session.getId()));
        }

        List<ExamSubmitAnswer> submittedAnswers = request.getAnswers() == null ? Collections.emptyList() : request.getAnswers();
        Map<Long, ExamSubmitAnswer> answerMap = submittedAnswers.stream()
                .filter(a -> a.getQuestionId() != null)
                .collect(Collectors.toMap(ExamSubmitAnswer::getQuestionId, a -> a, (a, b) -> b, LinkedHashMap::new));

        List<Long> sessionQuestionIds = splitIds(session.getQuestionIds());
        if (sessionQuestionIds.isEmpty()) {
            sessionQuestionIds = new ArrayList<>(answerMap.keySet());
        }
        List<QuestionBank> questions = questionBankMapper.selectBatchIds(sessionQuestionIds);
        questions = questions.stream()
                .filter(q -> userId.equals(q.getCreatedBy()))
                .collect(Collectors.toList());

        int correctCount = 0;
        int wrongCount = 0;
        int answeredCount = 0;
        int scoreTotal = questions.stream().mapToInt(this::scoreOf).sum();
        int scoreObtained = 0;
        List<QuestionResultVO> details = new ArrayList<>();

        for (QuestionBank question : questions) {
            ExamSubmitAnswer answer = answerMap.get(question.getId());
            String userAnswer = answer == null ? null : normalizeAnswer(answer.getUserAnswer());
            boolean answered = StringUtils.hasText(userAnswer);
            boolean correct = answered && answerEquals(userAnswer, question.getCorrectAnswer(), question.getQuestionType());
            int questionScore = correct ? scoreOf(question) : 0;

            if (answered) {
                answeredCount++;
                if (correct) {
                    correctCount++;
                } else {
                    wrongCount++;
                }
            }
            scoreObtained += questionScore;

            AnswerRecords record = new AnswerRecords();
            record.setUserId(userId);
            record.setSessionId(session.getId());
            record.setQuestionId(question.getId());
            record.setSubjectName(question.getSubjectName());
            record.setQuestionType(question.getQuestionType());
            record.setCategoryLevel(question.getCategoryLevel());
            record.setKnowledgePoint(question.getKnowledgePoint());
            record.setUserAnswer(userAnswer);
            record.setCorrectAnswer(question.getCorrectAnswer());
            record.setIsCorrect(correct ? 1 : 0);
            record.setScore(questionScore);
            record.setAnswerTimeSeconds(answer == null ? null : answer.getAnswerTimeSeconds());
            record.setMistakeAdded(answered && !correct ? 1 : 0);
            record.setExamSession(String.valueOf(session.getId()));
            record.setAnswerAt(LocalDateTime.now());
            answerRecordsMapper.insert(record);

            questionBankMapper.incrementUseCount(question.getId());
            if (answered && !correct) {
                questionBankMapper.incrementMistakeCount(question.getId());
                upsertMistake(userId, question, userAnswer, record.getId());
            } else if (answered && correct && "mistake".equals(session.getSessionType())) {
                markMistakeReviewed(userId, question.getId(), true);
            }

            details.add(toQuestionResult(question, userAnswer, correct, questionScore, record.getAnswerTimeSeconds()));
        }

        int unansweredCount = Math.max(0, session.getTotalQuestions() - answeredCount);
        LocalDateTime now = LocalDateTime.now();
        session.setAnsweredQuestions(answeredCount);
        session.setCorrectCount(correctCount);
        session.setWrongCount(wrongCount);
        session.setScoreTotal(scoreTotal);
        session.setScoreObtained(scoreObtained);
        session.setDurationSeconds(request.getDurationSeconds());
        session.setSuspiciousCount(request.getSuspiciousCount() == null ? 0 : request.getSuspiciousCount());
        session.setFinishedAt(now);
        session.setStatus(2);
        session.setUpdatedAt(now);
        practiceSessionMapper.updateById(session);

        ExamSubmitVO vo = buildSubmitResult(session, details);
        vo.setUnansweredCount(unansweredCount);
        return vo;
    }

    @Override
    public List<ExamHistoryVO> getHistory(Long userId, String mode) {
        Long currentUserId = requireUserId(userId);
        LambdaQueryWrapper<PracticeSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PracticeSession::getUserId, currentUserId)
                .eq(PracticeSession::getStatus, 2)
                .eq(StringUtils.hasText(mode), PracticeSession::getSessionType, normalizeMode(mode))
                .orderByDesc(PracticeSession::getFinishedAt)
                .last("LIMIT 100");
        return practiceSessionMapper.selectList(wrapper).stream()
                .map(this::toHistoryVO)
                .collect(Collectors.toList());
    }

    @Override
    public ExamDetailVO getDetail(Long userId, Long sessionId) {
        Long currentUserId = requireUserId(userId);
        PracticeSession session = practiceSessionMapper.selectById(sessionId);
        if (session == null || !currentUserId.equals(session.getUserId())) {
            throw new IllegalArgumentException("考试记录不存在或无权查看");
        }
        ExamDetailVO vo = new ExamDetailVO();
        vo.setSession(toHistoryVO(session));
        vo.setDetails(getQuestionResults(currentUserId, sessionId));
        return vo;
    }

    private List<QuestionBank> getApprovedCandidates(Long userId, ExamStartRequest request) {
        LambdaQueryWrapper<QuestionBank> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionBank::getCreatedBy, userId)
                .eq(QuestionBank::getAuditStatus, "approved")
                .eq(QuestionBank::getStatus, 1)
                .eq(StringUtils.hasText(request.getCategoryLevel()), QuestionBank::getCategoryLevel, request.getCategoryLevel())
                .eq(StringUtils.hasText(request.getSubjectName()), QuestionBank::getSubjectName, request.getSubjectName())
                .eq(StringUtils.hasText(request.getQuestionType()), QuestionBank::getQuestionType, request.getQuestionType())
                .eq(StringUtils.hasText(request.getDifficultyLevel()), QuestionBank::getDifficultyLevel, request.getDifficultyLevel());

        List<QuestionBank> questions = questionBankMapper.selectList(wrapper);
        return filterByKnowledgePoints(questions, request.getKnowledgePoints());
    }

    private List<QuestionBank> getMistakeCandidates(Long userId, ExamStartRequest request) {
        LambdaQueryWrapper<MistakeRecord> mistakeWrapper = new LambdaQueryWrapper<>();
        mistakeWrapper.eq(MistakeRecord::getUserId, userId)
                .eq(MistakeRecord::getMastered, false)
                .isNotNull(MistakeRecord::getQuestionId)
                .orderByDesc(MistakeRecord::getLastMistakeAt);
        List<Long> questionIds = mistakeRecordMapper.selectList(mistakeWrapper).stream()
                .map(MistakeRecord::getQuestionId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .collect(Collectors.toList());
        if (questionIds.isEmpty()) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<QuestionBank> questionWrapper = new LambdaQueryWrapper<>();
        questionWrapper.in(QuestionBank::getId, questionIds)
                .eq(QuestionBank::getCreatedBy, userId)
                .eq(QuestionBank::getAuditStatus, "approved")
                .eq(QuestionBank::getStatus, 1)
                .eq(StringUtils.hasText(request.getCategoryLevel()), QuestionBank::getCategoryLevel, request.getCategoryLevel())
                .eq(StringUtils.hasText(request.getSubjectName()), QuestionBank::getSubjectName, request.getSubjectName())
                .eq(StringUtils.hasText(request.getQuestionType()), QuestionBank::getQuestionType, request.getQuestionType())
                .eq(StringUtils.hasText(request.getDifficultyLevel()), QuestionBank::getDifficultyLevel, request.getDifficultyLevel());
        return filterByKnowledgePoints(questionBankMapper.selectList(questionWrapper), request.getKnowledgePoints());
    }

    private List<QuestionBank> filterByKnowledgePoints(List<QuestionBank> questions, List<String> requestedPoints) {
        if (requestedPoints == null || requestedPoints.isEmpty()) {
            return questions;
        }
        Set<String> required = requestedPoints.stream()
                .filter(StringUtils::hasText)
                .map(this::normalizePoint)
                .collect(Collectors.toSet());
        if (required.isEmpty()) {
            return questions;
        }
        return questions.stream()
                .filter(q -> {
                    Set<String> actual = splitPoints(q.getKnowledgePoint());
                    actual.retainAll(required);
                    return !actual.isEmpty();
                })
                .collect(Collectors.toList());
    }

    private List<QuestionBank> drawQuestions(List<QuestionBank> candidates, int limit, boolean mistakeMode) {
        List<QuestionBank> shuffled = new ArrayList<>(candidates);
        Collections.shuffle(shuffled);
        shuffled.sort(Comparator.comparingDouble(q -> -scoreWeight(q, mistakeMode)));
        if (shuffled.size() <= limit) {
            return shuffled;
        }
        return new ArrayList<>(shuffled.subList(0, limit));
    }

    private double scoreWeight(QuestionBank question, boolean mistakeMode) {
        int useCount = question.getUseCount() == null ? 0 : question.getUseCount();
        BigDecimal mistakeRate = question.getMistakeRate() == null ? BigDecimal.ZERO : question.getMistakeRate();
        double usePenalty = 1.0 / (1 + useCount);
        double mistakeBonus = mistakeRate.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP).doubleValue();
        double random = Math.random() * 0.25;
        return usePenalty * 0.55 + mistakeBonus * (mistakeMode ? 0.35 : 0.2) + random;
    }

    private void upsertMistake(Long userId, QuestionBank question, String userAnswer, Long answerRecordId) {
        LambdaQueryWrapper<MistakeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MistakeRecord::getUserId, userId).eq(MistakeRecord::getQuestionId, question.getId());
        MistakeRecord existing = mistakeRecordMapper.selectOne(wrapper);
        LocalDateTime now = LocalDateTime.now();
        if (existing == null) {
            MistakeRecord record = new MistakeRecord();
            record.setUserId(userId);
            record.setQuestionId(question.getId());
            record.setSubjectName(question.getSubjectName());
            record.setMistakeName(trimTitle(question.getQuestionTitle()));
            record.setMistakeType(question.getQuestionType());
            record.setQuestionOptions(question.getOptions());
            record.setStudentChoice(userAnswer);
            record.setWrongAnswer(userAnswer);
            record.setCorrectAnswer(question.getCorrectAnswer());
            record.setAnswerAnalysis(question.getAnswerAnalysis());
            record.setKnowledgePoint(question.getKnowledgePoint());
            record.setLastAnswerRecordId(answerRecordId);
            record.setMistakeCount(1);
            record.setReviewCount(0);
            record.setMastered(false);
            record.setMistakeDate(LocalDate.now());
            record.setLastMistakeAt(now);
            record.setNextReviewDate(LocalDate.now().plusDays(1));
            mistakeRecordMapper.insert(record);
            return;
        }

        existing.setStudentChoice(userAnswer);
        existing.setWrongAnswer(userAnswer);
        existing.setCorrectAnswer(question.getCorrectAnswer());
        existing.setAnswerAnalysis(question.getAnswerAnalysis());
        existing.setQuestionOptions(question.getOptions());
        existing.setKnowledgePoint(question.getKnowledgePoint());
        existing.setLastAnswerRecordId(answerRecordId);
        existing.setMistakeCount((existing.getMistakeCount() == null ? 0 : existing.getMistakeCount()) + 1);
        existing.setMastered(false);
        existing.setMistakeDate(LocalDate.now());
        existing.setLastMistakeAt(now);
        existing.setNextReviewDate(LocalDate.now().plusDays(1));
        existing.setUpdatedAt(now);
        mistakeRecordMapper.updateById(existing);
    }

    private void markMistakeReviewed(Long userId, Long questionId, boolean correct) {
        LambdaQueryWrapper<MistakeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MistakeRecord::getUserId, userId).eq(MistakeRecord::getQuestionId, questionId);
        MistakeRecord record = mistakeRecordMapper.selectOne(wrapper);
        if (record == null) {
            return;
        }
        record.setReviewCount((record.getReviewCount() == null ? 0 : record.getReviewCount()) + 1);
        record.setLastReviewDate(LocalDate.now());
        record.setNextReviewDate(correct ? LocalDate.now().plusDays(7) : LocalDate.now().plusDays(1));
        if (correct) {
            record.setMastered(true);
        }
        record.setUpdatedAt(LocalDateTime.now());
        mistakeRecordMapper.updateById(record);
    }

    private List<QuestionResultVO> getQuestionResults(Long userId, Long sessionId) {
        LambdaQueryWrapper<AnswerRecords> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AnswerRecords::getUserId, userId)
                .eq(AnswerRecords::getSessionId, sessionId)
                .orderByAsc(AnswerRecords::getCreatedAt);
        List<AnswerRecords> records = answerRecordsMapper.selectList(wrapper);
        if (records.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> questionIds = records.stream().map(AnswerRecords::getQuestionId).collect(Collectors.toList());
        Map<Long, QuestionBank> questionMap = questionBankMapper.selectBatchIds(questionIds).stream()
                .collect(Collectors.toMap(QuestionBank::getId, q -> q, (a, b) -> a));
        List<QuestionResultVO> result = new ArrayList<>();
        for (AnswerRecords record : records) {
            QuestionBank question = questionMap.get(record.getQuestionId());
            if (question == null) {
                continue;
            }
            result.add(toQuestionResult(
                    question,
                    record.getUserAnswer(),
                    record.getIsCorrect() != null && record.getIsCorrect() == 1,
                    record.getScore(),
                    record.getAnswerTimeSeconds()
            ));
        }
        return result;
    }

    private QuestionResultVO toQuestionResult(QuestionBank question, String userAnswer, boolean correct, Integer score, Integer answerTimeSeconds) {
        QuestionResultVO vo = new QuestionResultVO();
        vo.setQuestionId(question.getId());
        vo.setQuestionTitle(question.getQuestionTitle());
        vo.setQuestionType(question.getQuestionType());
        vo.setCategoryLevel(question.getCategoryLevel());
        vo.setSubjectName(question.getSubjectName());
        vo.setKnowledgePoint(question.getKnowledgePoint());
        vo.setDifficultyLevel(question.getDifficultyLevel());
        vo.setOptions(question.getOptions());
        vo.setUserAnswer(userAnswer);
        vo.setCorrectAnswer(question.getCorrectAnswer());
        vo.setAnswerAnalysis(question.getAnswerAnalysis());
        vo.setCorrect(correct);
        vo.setScore(score == null ? 0 : score);
        vo.setAnswerTimeSeconds(answerTimeSeconds);
        return vo;
    }

    private QuestionBank hideAnswer(QuestionBank source) {
        QuestionBank target = new QuestionBank();
        target.setId(source.getId());
        target.setCreatedBy(source.getCreatedBy());
        target.setSubjectName(source.getSubjectName());
        target.setQuestionType(source.getQuestionType());
        target.setCategoryLevel(source.getCategoryLevel());
        target.setKnowledgePoint(source.getKnowledgePoint());
        target.setQuestionTitle(source.getQuestionTitle());
        target.setOptions(source.getOptions());
        target.setDifficultyLevel(source.getDifficultyLevel());
        target.setScoreValue(source.getScoreValue());
        target.setUseCount(source.getUseCount());
        target.setMistakeCount(source.getMistakeCount());
        target.setMistakeRate(source.getMistakeRate());
        target.setAuditStatus(source.getAuditStatus());
        return target;
    }

    private ExamSubmitVO buildSubmitResult(PracticeSession session, List<QuestionResultVO> details) {
        ExamSubmitVO vo = new ExamSubmitVO();
        vo.setSessionId(session.getId());
        vo.setTotalQuestions(session.getTotalQuestions());
        vo.setAnsweredQuestions(session.getAnsweredQuestions());
        vo.setCorrectCount(session.getCorrectCount());
        vo.setWrongCount(session.getWrongCount());
        vo.setUnansweredCount(Math.max(0, nullToZero(session.getTotalQuestions()) - nullToZero(session.getAnsweredQuestions())));
        vo.setScoreTotal(session.getScoreTotal());
        vo.setScoreObtained(session.getScoreObtained());
        vo.setScorePercent(scorePercent(session.getScoreObtained(), session.getScoreTotal()));
        vo.setDurationSeconds(session.getDurationSeconds());
        vo.setSuspiciousCount(session.getSuspiciousCount());
        vo.setDetails(details);
        return vo;
    }

    private ExamHistoryVO toHistoryVO(PracticeSession session) {
        ExamHistoryVO vo = new ExamHistoryVO();
        vo.setSessionId(session.getId());
        vo.setMode(session.getSessionType());
        vo.setTitle(session.getTitle());
        vo.setCategoryLevel(session.getCategoryLevel());
        vo.setSubjectName(session.getSubjectName());
        vo.setKnowledgePoints(session.getKnowledgePoints());
        vo.setDifficultyLevel(session.getDifficultyLevel());
        vo.setTotalQuestions(session.getTotalQuestions());
        vo.setCorrectCount(session.getCorrectCount());
        vo.setWrongCount(session.getWrongCount());
        vo.setScoreTotal(session.getScoreTotal());
        vo.setScoreObtained(session.getScoreObtained());
        vo.setScorePercent(scorePercent(session.getScoreObtained(), session.getScoreTotal()));
        vo.setDurationSeconds(session.getDurationSeconds());
        vo.setSuspiciousCount(session.getSuspiciousCount());
        vo.setStartedAt(session.getStartedAt());
        vo.setFinishedAt(session.getFinishedAt());
        return vo;
    }

    private boolean answerEquals(String userAnswer, String correctAnswer, String questionType) {
        if (!StringUtils.hasText(correctAnswer)) {
            return false;
        }
        String left = normalizeAnswer(userAnswer);
        String right = normalizeAnswer(correctAnswer);
        if ("多选".equals(questionType) || "multiple".equalsIgnoreCase(questionType)) {
            return splitAnswer(left).equals(splitAnswer(right));
        }
        return left.equalsIgnoreCase(right);
    }

    private Set<String> splitAnswer(String answer) {
        if (!StringUtils.hasText(answer)) {
            return Collections.emptySet();
        }
        String normalized = answer.replace("，", ",").replace("、", ",").replace(";", ",");
        Set<String> set = new HashSet<>();
        for (String item : normalized.split(",")) {
            String value = item.trim();
            if (StringUtils.hasText(value)) {
                set.add(value.toUpperCase(Locale.ROOT));
            }
        }
        return set;
    }

    private Set<String> splitPoints(String points) {
        if (!StringUtils.hasText(points)) {
            return new HashSet<>();
        }
        String normalized = points.replace("，", ",").replace("、", ",").replace(";", ",");
        Set<String> set = new HashSet<>();
        for (String point : normalized.split(",")) {
            String value = normalizePoint(point);
            if (StringUtils.hasText(value)) {
                set.add(value);
            }
        }
        return set;
    }

    private String normalizePoint(String point) {
        return point == null ? "" : point.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeAnswer(String answer) {
        return answer == null ? "" : answer.trim().replace("，", ",");
    }

    private String normalizeMode(String mode) {
        if (!StringUtils.hasText(mode)) {
            return "exam";
        }
        String value = mode.trim().toLowerCase(Locale.ROOT);
        if ("mistake".equals(value) || "review".equals(value) || "错题".equals(value)) {
            return "mistake";
        }
        if ("practice".equals(value) || "练习".equals(value)) {
            return "practice";
        }
        return "exam";
    }

    private String defaultTitle(String mode) {
        if ("mistake".equals(mode)) {
            return "错题练习";
        }
        if ("practice".equals(mode)) {
            return "在线练习";
        }
        return "正式考试";
    }

    private Long requireUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("缺少用户信息");
        }
        return userId;
    }

    private String joinPoints(List<String> points) {
        if (points == null || points.isEmpty()) {
            return null;
        }
        return points.stream().filter(StringUtils::hasText).map(String::trim).collect(Collectors.joining(","));
    }

    private List<Long> splitIds(String ids) {
        if (!StringUtils.hasText(ids)) {
            return new ArrayList<>();
        }
        List<Long> result = new ArrayList<>();
        for (String item : ids.split(",")) {
            try {
                result.add(Long.parseLong(item.trim()));
            } catch (NumberFormatException ignored) {
                // 忽略历史脏数据。
            }
        }
        return result;
    }

    private String trimTitle(String title) {
        if (title == null) {
            return "";
        }
        return title.length() <= 100 ? title : title.substring(0, 100);
    }

    private int scoreOf(QuestionBank question) {
        return question.getScoreValue() == null || question.getScoreValue() <= 0 ? 2 : question.getScoreValue();
    }

    private int scorePercent(Integer scoreObtained, Integer scoreTotal) {
        int total = nullToZero(scoreTotal);
        if (total == 0) {
            return 0;
        }
        return BigDecimal.valueOf(nullToZero(scoreObtained))
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 0, RoundingMode.HALF_UP)
                .intValue();
    }

    private int nullToZero(Integer value) {
        return value == null ? 0 : value;
    }
}
