package org.example.universityservice.controller.paper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.commondb.utils.RestBean;
import org.example.universityservice.entity.paper.Paper;
import org.example.universityservice.entity.paper.Suggestion;
import org.example.universityservice.service.paper.PaperService;
import org.example.universityservice.service.paper.SuggestionService;
import org.example.universityservice.vo.paper.PaperVO;
import org.example.universityservice.vo.paper.SuggestionVO;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/paper")
@RequiredArgsConstructor
public class PaperController {

    private final PaperService paperService;
    private final SuggestionService suggestionService;

    /**
     * 获取当前用户的论文列表
     */
    @GetMapping("/list")
    public RestBean<List<Paper>> getPaperList(HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null) {
            return RestBean.fail("用户不存在");
        }
        Long userId = Long.parseLong(userIdStr);
        List<Paper> papers = paperService.getPapersByUserId(userId);
        return RestBean.success(papers);
    }

    /**
     * 根据ID获取论文详情
     */
    @GetMapping("/{id}")
    public RestBean<Paper> getPaperById(@PathVariable Long id) {
        Paper paper = paperService.getPaperById(id);
        if (paper == null) {
            return RestBean.fail("论文不存在");
        }
        return RestBean.success(paper);
    }

    /**
     * 创建论文
     */
    @PostMapping("/create")
    public RestBean<String> createPaper(@Valid @RequestBody PaperVO paperVO,
                                        HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null) {
            return RestBean.fail("用户不存在");
        }
        Long userId = Long.parseLong(userIdStr);
        boolean success = paperService.createPaper(paperVO, userId);
        if (success) {
            return RestBean.success("创建成功");
        }
        return RestBean.fail("创建失败");
    }

    /**
     * 更新论文
     */
    @PutMapping("/update")
    public RestBean<String> updatePaper(@Valid @RequestBody PaperVO paperVO) {
        if (paperVO.getId() == null) {
            return RestBean.fail("论文ID不能为空");
        }
        boolean success = paperService.updatePaper(paperVO);
        if (success) {
            return RestBean.success("更新成功");
        }
        return RestBean.fail("更新失败");
    }

    /**
     * 删除论文
     */
    @DeleteMapping("/{id}")
    public RestBean<String> deletePaper(@PathVariable Long id,
                                        HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null) {
            return RestBean.fail("用户不存在");
        }
        Long userId = Long.parseLong(userIdStr);
        boolean success = paperService.deletePaper(id, userId);
        if (success) {
            return RestBean.success("删除成功");
        }
        return RestBean.fail("删除失败");
    }

    /**
     * 获取论文的修改意见列表
     */
    @GetMapping("/suggestions/{paperId}")
    public RestBean<List<Suggestion>> getSuggestions(@PathVariable Long paperId) {
        List<Suggestion> suggestions = suggestionService.getSuggestionsByPaperId(paperId);
        return RestBean.success(suggestions);
    }

    /**
     * 添加修改意见
     */
    @PostMapping("/suggestion/add")
    public RestBean<String> addSuggestion(@Valid @RequestBody SuggestionVO suggestionVO) {
        boolean success = suggestionService.addSuggestion(suggestionVO);
        if (success) {
            return RestBean.success("添加成功");
        }
        return RestBean.fail("添加失败");
    }

    /**
     * 删除修改意见
     */
    @DeleteMapping("/suggestion/{id}")
    public RestBean<String> deleteSuggestion(@PathVariable Long id,
                                             @RequestParam Long paperId) {
        boolean success = suggestionService.deleteSuggestion(id, paperId);
        if (success) {
            return RestBean.success("删除成功");
        }
        return RestBean.fail("删除失败");
    }
}