package org.example.usercenter.controller;

import jakarta.servlet.http.HttpServletRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.commondb.utils.RestBean;
import org.example.usercenter.entity.*;
import org.example.usercenter.entity.ResumeComplete;
import org.example.usercenter.service.*;
import org.springframework.web.bind.annotation.*;

/*
 * @Author:总会落叶
 * @Date:2026/3/27
 * @Description:
 */
@RestController
@RequestMapping("api/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;
    private final ResumeCertificateService resumeCertificateService;
    private final ResumeEducationService resumeEducationService;
    private final ResumeProjectService resumeProjectService;
    private final ResumeSkillService resumeSkillService;
    private final ResumeSocialExperienceService resumeSocialExperienceService;
    private final ResumeWorkExperienceService resumeWorkExperienceService;

    private RestBean<Long> requireCurrentUserResumeId(HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null) {
            return RestBean.fail("用户未登录");
        }
        Long userId = Long.parseLong(userIdStr);
        Resume resume = resumeService.getByUserId(userId);
        if (resume == null) {
            return RestBean.fail("必须先添加简历的个人基本信息");
        }
        return RestBean.success(resume.getId());
    }


    /**
     * 新增简历主表
     */
    @PostMapping("addResume")
    public RestBean<Long> addResume(@RequestBody Resume resume,
                                      HttpServletRequest request){
        String userIdStr = request.getHeader("X-User-Id");
        if(userIdStr == null){
            return RestBean.fail("用户未登录");
        }
        Long userId = Long.parseLong(userIdStr);
        
        Resume existing = resumeService.getByUserId(userId);

        // ✅ 加上这一行：空的生日改成 null
        if (resume.getBirthDate() != null && resume.getBirthDate().trim().isEmpty()) {
            resume.setBirthDate(null);
        }

        if (existing == null) {
            resume.setUserId(userId);
            resumeService.addResume(resume);
            // 返回真正生成/写入后的 resume.id，避免前端把 insert 行数当成外键
            return RestBean.success("简历新增成功", resume.getId());
        }

        // upsert：存在就更新（前端保存基本信息会一直调用 addResume）
        existing.setName(resume.getName());
        existing.setJobTitle(resume.getJobTitle());
        existing.setEmail(resume.getEmail());
        existing.setPhone(resume.getPhone());
        existing.setAddress(resume.getAddress());
        existing.setBirthDate(resume.getBirthDate());
        existing.setSelfEvaluation(resume.getSelfEvaluation());
        existing.setAvatar(resume.getAvatar());
        resumeService.updateById(existing);
        return RestBean.success("简历更新成功", existing.getId());
    }

    //============ 证书相关接口===========
    /**
     *新增证书
     */
    @PostMapping("addCertificate")
    public RestBean<ResumeCertificate> addCertificate(@RequestBody ResumeCertificate resumeCertificate,
                                            HttpServletRequest request){
        RestBean<Long> resumeIdBean = requireCurrentUserResumeId(request);
        if (resumeIdBean.getCode() != 200) {
            return RestBean.fail(resumeIdBean.getMsg());
        }
        resumeCertificate.setResumeId(resumeIdBean.getData());
        Integer result = resumeCertificateService.addCertificate(resumeCertificate);
        if (result != null && result > 0) {
            return RestBean.success("证书新增成功", resumeCertificate);
        }
        return RestBean.fail("证书新增失败");
    }
    //============ 教育经历相关接口===========
    /**
     * 新增教育经历
     */
    @PostMapping("addEducation")
    public RestBean<ResumeEducation> addEducation(@RequestBody ResumeEducation resumeEducation,
                                          HttpServletRequest request){
        RestBean<Long> resumeIdBean = requireCurrentUserResumeId(request);
        if (resumeIdBean.getCode() != 200) {
            return RestBean.fail(resumeIdBean.getMsg());
        }
        resumeEducation.setResumeId(resumeIdBean.getData());

        Integer result = resumeEducationService.addEducation(resumeEducation);
        if(result != null && result > 0) {
            return RestBean.success("教育经历新增成功", resumeEducation);
        }
        return RestBean.fail("教育经历新增失败");
    }
    //============ 项目经验相关接口============
    /**
     * 新增项目经验
     */
    @PostMapping("addProject")
    public RestBean<ResumeProject> addProject(@RequestBody ResumeProject resumeProject,
                                        HttpServletRequest request){
        RestBean<Long> resumeIdBean = requireCurrentUserResumeId(request);
        if (resumeIdBean.getCode() != 200) {
            return RestBean.fail(resumeIdBean.getMsg());
        }
        resumeProject.setResumeId(resumeIdBean.getData());
        Integer result = resumeProjectService.addProject(resumeProject);
        if (result != null && result > 0) {
            return RestBean.success("项目经验新增成功", resumeProject);
        }
        return RestBean.fail("项目经验新增失败");
    }
    //============ 技能特长相关接口============
    /**
     * 新增技能特长
     */
    @PostMapping("addSkill")
    public RestBean<ResumeSkill> addSkill(@RequestBody ResumeSkill resumeSkill,
                                      HttpServletRequest request){
        RestBean<Long> resumeIdBean = requireCurrentUserResumeId(request);
        if (resumeIdBean.getCode() != 200) {
            return RestBean.fail(resumeIdBean.getMsg());
        }
        resumeSkill.setResumeId(resumeIdBean.getData());
        Integer result = resumeSkillService.addSkill(resumeSkill);
        if (result != null && result > 0) {
            return RestBean.success("技能特长新增成功", resumeSkill);
        }
        return RestBean.fail("技能特长新增失败");
        }
    //============ 社会经历相关接口============
    /**
     * 新增社会经历
     */
    @PostMapping("addSocialExperience")
    public RestBean<ResumeSocialExperience> addSocialExperience(@RequestBody ResumeSocialExperience resumeSocialExperience,
                                                HttpServletRequest request){
        RestBean<Long> resumeIdBean = requireCurrentUserResumeId(request);
        if (resumeIdBean.getCode() != 200) {
            return RestBean.fail(resumeIdBean.getMsg());
        }
        resumeSocialExperience.setResumeId(resumeIdBean.getData());
        Integer result = resumeSocialExperienceService.addSocialExperience(resumeSocialExperience);
        if (result != null && result > 0) {
            return RestBean.success("社会经历新增成功", resumeSocialExperience);
        }
        return RestBean.fail("社会经历新增失败");
        }
    //============ 工作经历相关接口============
    /**
     * 新增工作经历
     */
    @PostMapping("addWorkExperience")
    public RestBean<ResumeWorkExperience> addWorkExperience(@RequestBody ResumeWorkExperience resumeWorkExperience,
                                              HttpServletRequest request){
        RestBean<Long> resumeIdBean = requireCurrentUserResumeId(request);
        if (resumeIdBean.getCode() != 200) {
            return RestBean.fail(resumeIdBean.getMsg());
        }
        resumeWorkExperience.setResumeId(resumeIdBean.getData());
        Integer result = resumeWorkExperienceService.addWorkExperience(resumeWorkExperience);
        if (result != null && result > 0) {
            return RestBean.success("工作经历新增成功", resumeWorkExperience);
        }
        return RestBean.fail("工作经历新增失败");
        }

    //============ 工作经历 CRUD ============
    @PostMapping("updateWorkExperience")
    public RestBean<ResumeWorkExperience> updateWorkExperience(@RequestBody ResumeWorkExperience resumeWorkExperience,
                                                                HttpServletRequest request) {
        RestBean<Long> resumeIdBean = requireCurrentUserResumeId(request);
        if (resumeIdBean.getCode() != 200) {
            return RestBean.fail(resumeIdBean.getMsg());
        }
        Long id = resumeWorkExperience.getId();
        if (id == null) return RestBean.fail("缺少id");

        ResumeWorkExperience db = resumeWorkExperienceService.getOne(
                new QueryWrapper<ResumeWorkExperience>()
                        .eq("id", id)
                        .eq("resume_id", resumeIdBean.getData())
        );
        if (db == null) {
            return RestBean.fail("无权操作此记录");
        }

        resumeWorkExperience.setResumeId(resumeIdBean.getData());
        boolean updated = resumeWorkExperienceService.updateById(resumeWorkExperience);
        if (!updated) return RestBean.fail("工作经历更新失败");
        return RestBean.success("工作经历更新成功", resumeWorkExperienceService.getById(id));
    }

    @PostMapping("deleteWorkExperience")
    public RestBean<Integer> deleteWorkExperience(@RequestBody ResumeWorkExperience resumeWorkExperience,
                                                    HttpServletRequest request) {
        RestBean<Long> resumeIdBean = requireCurrentUserResumeId(request);
        if (resumeIdBean.getCode() != 200) {
            return RestBean.fail(resumeIdBean.getMsg());
        }
        Long id = resumeWorkExperience.getId();
        if (id == null) return RestBean.fail("缺少id");

        ResumeWorkExperience db = resumeWorkExperienceService.getOne(
                new QueryWrapper<ResumeWorkExperience>()
                        .eq("id", id)
                        .eq("resume_id", resumeIdBean.getData())
        );
        if (db == null) {
            return RestBean.fail("无权删除此记录");
        }

        boolean removed = resumeWorkExperienceService.removeById(id);
        return RestBean.success("工作经历删除成功", removed ? 1 : 0);
    }

    //============ 教育经历 CRUD ============
    @PostMapping("updateEducation")
    public RestBean<ResumeEducation> updateEducation(@RequestBody ResumeEducation resumeEducation,
                                                      HttpServletRequest request) {
        RestBean<Long> resumeIdBean = requireCurrentUserResumeId(request);
        if (resumeIdBean.getCode() != 200) {
            return RestBean.fail(resumeIdBean.getMsg());
        }
        Long id = resumeEducation.getId();
        if (id == null) return RestBean.fail("缺少id");

        ResumeEducation db = resumeEducationService.getOne(
                new QueryWrapper<ResumeEducation>()
                        .eq("id", id)
                        .eq("resume_id", resumeIdBean.getData())
        );
        if (db == null) return RestBean.fail("无权操作此记录");

        resumeEducation.setResumeId(resumeIdBean.getData());
        boolean updated = resumeEducationService.updateById(resumeEducation);
        if (!updated) return RestBean.fail("教育经历更新失败");
        return RestBean.success("教育经历更新成功", resumeEducationService.getById(id));
    }

    @PostMapping("deleteEducation")
    public RestBean<Integer> deleteEducation(@RequestBody ResumeEducation resumeEducation,
                                               HttpServletRequest request) {
        RestBean<Long> resumeIdBean = requireCurrentUserResumeId(request);
        if (resumeIdBean.getCode() != 200) {
            return RestBean.fail(resumeIdBean.getMsg());
        }
        Long id = resumeEducation.getId();
        if (id == null) return RestBean.fail("缺少id");

        ResumeEducation db = resumeEducationService.getOne(
                new QueryWrapper<ResumeEducation>()
                        .eq("id", id)
                        .eq("resume_id", resumeIdBean.getData())
        );
        if (db == null) return RestBean.fail("无权删除此记录");

        boolean removed = resumeEducationService.removeById(id);
        return RestBean.success("教育经历删除成功", removed ? 1 : 0);
    }

    //============ 项目经验 CRUD ============
    @PostMapping("updateProject")
    public RestBean<ResumeProject> updateProject(@RequestBody ResumeProject resumeProject,
                                                 HttpServletRequest request) {
        RestBean<Long> resumeIdBean = requireCurrentUserResumeId(request);
        if (resumeIdBean.getCode() != 200) {
            return RestBean.fail(resumeIdBean.getMsg());
        }
        Long id = resumeProject.getId();
        if (id == null) return RestBean.fail("缺少id");

        ResumeProject db = resumeProjectService.getOne(
                new QueryWrapper<ResumeProject>()
                        .eq("id", id)
                        .eq("resume_id", resumeIdBean.getData())
        );
        if (db == null) return RestBean.fail("无权操作此记录");

        resumeProject.setResumeId(resumeIdBean.getData());
        boolean updated = resumeProjectService.updateById(resumeProject);
        if (!updated) return RestBean.fail("项目经验更新失败");
        return RestBean.success("项目经验更新成功", resumeProjectService.getById(id));
    }

    @PostMapping("deleteProject")
    public RestBean<Integer> deleteProject(@RequestBody ResumeProject resumeProject,
                                            HttpServletRequest request) {
        RestBean<Long> resumeIdBean = requireCurrentUserResumeId(request);
        if (resumeIdBean.getCode() != 200) {
            return RestBean.fail(resumeIdBean.getMsg());
        }
        Long id = resumeProject.getId();
        if (id == null) return RestBean.fail("缺少id");

        ResumeProject db = resumeProjectService.getOne(
                new QueryWrapper<ResumeProject>()
                        .eq("id", id)
                        .eq("resume_id", resumeIdBean.getData())
        );
        if (db == null) return RestBean.fail("无权删除此记录");

        boolean removed = resumeProjectService.removeById(id);
        return RestBean.success("项目经验删除成功", removed ? 1 : 0);
    }

    //============ 技能特长 CRUD ============
    @PostMapping("updateSkill")
    public RestBean<ResumeSkill> updateSkill(@RequestBody ResumeSkill resumeSkill,
                                              HttpServletRequest request) {
        RestBean<Long> resumeIdBean = requireCurrentUserResumeId(request);
        if (resumeIdBean.getCode() != 200) {
            return RestBean.fail(resumeIdBean.getMsg());
        }
        Long id = resumeSkill.getId();
        if (id == null) return RestBean.fail("缺少id");

        ResumeSkill db = resumeSkillService.getOne(
                new QueryWrapper<ResumeSkill>()
                        .eq("id", id)
                        .eq("resume_id", resumeIdBean.getData())
        );
        if (db == null) return RestBean.fail("无权操作此记录");

        resumeSkill.setResumeId(resumeIdBean.getData());
        boolean updated = resumeSkillService.updateById(resumeSkill);
        if (!updated) return RestBean.fail("技能特长更新失败");
        return RestBean.success("技能特长更新成功", resumeSkillService.getById(id));
    }

    @PostMapping("deleteSkill")
    public RestBean<Integer> deleteSkill(@RequestBody ResumeSkill resumeSkill,
                                          HttpServletRequest request) {
        RestBean<Long> resumeIdBean = requireCurrentUserResumeId(request);
        if (resumeIdBean.getCode() != 200) {
            return RestBean.fail(resumeIdBean.getMsg());
        }
        Long id = resumeSkill.getId();
        if (id == null) return RestBean.fail("缺少id");

        ResumeSkill db = resumeSkillService.getOne(
                new QueryWrapper<ResumeSkill>()
                        .eq("id", id)
                        .eq("resume_id", resumeIdBean.getData())
        );
        if (db == null) return RestBean.fail("无权删除此记录");

        boolean removed = resumeSkillService.removeById(id);
        return RestBean.success("技能特长删除成功", removed ? 1 : 0);
    }

    //============ 证书荣誉 CRUD ============
    @PostMapping("updateCertificate")
    public RestBean<ResumeCertificate> updateCertificate(@RequestBody ResumeCertificate resumeCertificate,
                                                           HttpServletRequest request) {
        RestBean<Long> resumeIdBean = requireCurrentUserResumeId(request);
        if (resumeIdBean.getCode() != 200) {
            return RestBean.fail(resumeIdBean.getMsg());
        }
        Long id = resumeCertificate.getId();
        if (id == null) return RestBean.fail("缺少id");

        ResumeCertificate db = resumeCertificateService.getOne(
                new QueryWrapper<ResumeCertificate>()
                        .eq("id", id)
                        .eq("resume_id", resumeIdBean.getData())
        );
        if (db == null) return RestBean.fail("无权操作此记录");

        resumeCertificate.setResumeId(resumeIdBean.getData());
        boolean updated = resumeCertificateService.updateById(resumeCertificate);
        if (!updated) return RestBean.fail("证书更新失败");
        return RestBean.success("证书更新成功", resumeCertificateService.getById(id));
    }

    @PostMapping("deleteCertificate")
    public RestBean<Integer> deleteCertificate(@RequestBody ResumeCertificate resumeCertificate,
                                                HttpServletRequest request) {
        RestBean<Long> resumeIdBean = requireCurrentUserResumeId(request);
        if (resumeIdBean.getCode() != 200) {
            return RestBean.fail(resumeIdBean.getMsg());
        }
        Long id = resumeCertificate.getId();
        if (id == null) return RestBean.fail("缺少id");

        ResumeCertificate db = resumeCertificateService.getOne(
                new QueryWrapper<ResumeCertificate>()
                        .eq("id", id)
                        .eq("resume_id", resumeIdBean.getData())
        );
        if (db == null) return RestBean.fail("无权删除此记录");

        boolean removed = resumeCertificateService.removeById(id);
        return RestBean.success("证书删除成功", removed ? 1 : 0);
    }

    //============ 社会经历 CRUD ============
    @PostMapping("updateSocialExperience")
    public RestBean<ResumeSocialExperience> updateSocialExperience(@RequestBody ResumeSocialExperience resumeSocialExperience,
                                                                      HttpServletRequest request) {
        RestBean<Long> resumeIdBean = requireCurrentUserResumeId(request);
        if (resumeIdBean.getCode() != 200) {
            return RestBean.fail(resumeIdBean.getMsg());
        }
        Long id = resumeSocialExperience.getId();
        if (id == null) return RestBean.fail("缺少id");

        ResumeSocialExperience db = resumeSocialExperienceService.getOne(
                new QueryWrapper<ResumeSocialExperience>()
                        .eq("id", id)
                        .eq("resume_id", resumeIdBean.getData())
        );
        if (db == null) return RestBean.fail("无权操作此记录");

        resumeSocialExperience.setResumeId(resumeIdBean.getData());
        boolean updated = resumeSocialExperienceService.updateById(resumeSocialExperience);
        if (!updated) return RestBean.fail("社会经历更新失败");
        return RestBean.success("社会经历更新成功", resumeSocialExperienceService.getById(id));
    }

    @PostMapping("deleteSocialExperience")
    public RestBean<Integer> deleteSocialExperience(@RequestBody ResumeSocialExperience resumeSocialExperience,
                                                      HttpServletRequest request) {
        RestBean<Long> resumeIdBean = requireCurrentUserResumeId(request);
        if (resumeIdBean.getCode() != 200) {
            return RestBean.fail(resumeIdBean.getMsg());
        }
        Long id = resumeSocialExperience.getId();
        if (id == null) return RestBean.fail("缺少id");

        ResumeSocialExperience db = resumeSocialExperienceService.getOne(
                new QueryWrapper<ResumeSocialExperience>()
                        .eq("id", id)
                        .eq("resume_id", resumeIdBean.getData())
        );
        if (db == null) return RestBean.fail("无权删除此记录");

        boolean removed = resumeSocialExperienceService.removeById(id);
        return RestBean.success("社会经历删除成功", removed ? 1 : 0);
    }

    /**
     * 根据userId获取完整简历信息
     */
    @GetMapping("getCompleteResume")
    public RestBean<ResumeComplete> getCompleteResume(HttpServletRequest request){
        String userIdStr = request.getHeader("X-User-Id");
        if(userIdStr == null){
            return RestBean.fail("用户未登录");
        }
        Long userId = Long.parseLong(userIdStr);
        ResumeComplete resumeComplete = resumeService.getCompleteResumeByUserId(userId);
        return RestBean.success("获取简历成功", resumeComplete);
    }

}