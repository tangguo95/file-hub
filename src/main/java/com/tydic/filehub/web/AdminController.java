package com.tydic.filehub.web;

import com.tydic.filehub.dto.CodeFileOper;
import com.tydic.filehub.dto.ConfigPullDatasource;
import com.tydic.filehub.mapper.uoc.CodeFileOperMapper;
import com.tydic.filehub.scheduler.SchedulerManager;
import com.tydic.filehub.service.CodeFileOperAdminService;
import com.tydic.filehub.service.DynamicDatasourceAdminService;
import com.tydic.filehub.service.JobExecutionLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final CodeFileOperAdminService codeFileOperAdminService;
    private final DynamicDatasourceAdminService dynamicDatasourceAdminService;
    private final JobExecutionLogService jobExecutionLogService;
    private final SchedulerManager schedulerManager;
    private final CodeFileOperMapper codeFileOperMapper;

    @GetMapping("/")
    public String index() {
        return "redirect:/admin/jobs";
    }

    @GetMapping("/admin/jobs")
    public String jobs(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Integer state,
                       @RequestParam(required = false) Long editId,
                       Model model) {
        CodeFileOper current = codeFileOperAdminService.findById(editId);
        model.addAttribute("jobs", codeFileOperAdminService.search(keyword, state));
        model.addAttribute("jobForm", current.getId() == null ? defaultJob() : current);
        model.addAttribute("recentLogs", jobExecutionLogService.recent(20));
        model.addAttribute("keyword", keyword);
        model.addAttribute("state", state);
        return "admin/jobs";
    }

    @GetMapping("/admin/files")
    public String files(@RequestParam(required = false) String keyword,
                        @RequestParam(required = false) Integer state,
                        @RequestParam(required = false) Long editId,
                        Model model) {
        CodeFileOper current = codeFileOperAdminService.findById(editId);
        if (current.getId() != null) {
            current.setServerPassword("");
        }
        model.addAttribute("files", codeFileOperAdminService.search(keyword, state));
        model.addAttribute("fileForm", current.getId() == null ? defaultJob() : current);
        model.addAttribute("keyword", keyword);
        model.addAttribute("state", state);
        return "admin/files";
    }

    @GetMapping("/admin/datasources")
    public String datasources(@RequestParam(required = false) String keyword,
                              @RequestParam(required = false) String editCode,
                              Model model) {
        ConfigPullDatasource current = dynamicDatasourceAdminService.find(editCode);
        if (current.getId() != null) {
            current.setPassword("");
        }
        model.addAttribute("datasources", dynamicDatasourceAdminService.list(keyword));
        model.addAttribute("datasourceForm", current.getId() == null ? defaultDatasource() : current);
        model.addAttribute("keyword", keyword);
        return "admin/datasources";
    }

    @PostMapping("/admin/jobs/save")
    public String saveJob(@Valid @ModelAttribute("jobForm") CodeFileOper jobForm,
                          BindingResult bindingResult,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "任务表单校验失败");
            return "redirect:/admin/jobs";
        }
        codeFileOperAdminService.save(jobForm);
        redirectAttributes.addFlashAttribute("message", "任务已保存");
        return "redirect:/admin/jobs";
    }

    @PostMapping("/admin/jobs/{fileOperCode}/enable")
    public String enableJob(@PathVariable String fileOperCode, RedirectAttributes redirectAttributes) {
        CodeFileOper job = codeFileOperMapper.selectByFileOperCode(fileOperCode);
        job.setJobEnabled(1);
        codeFileOperAdminService.save(job);
        redirectAttributes.addFlashAttribute("message", "任务已启用");
        return "redirect:/admin/jobs";
    }

    @PostMapping("/admin/jobs/{fileOperCode}/disable")
    public String disableJob(@PathVariable String fileOperCode, RedirectAttributes redirectAttributes) {
        CodeFileOper job = codeFileOperMapper.selectByFileOperCode(fileOperCode);
        job.setJobEnabled(0);
        codeFileOperAdminService.save(job);
        redirectAttributes.addFlashAttribute("message", "任务已停用");
        return "redirect:/admin/jobs";
    }

    @PostMapping("/admin/jobs/{fileOperCode}/trigger")
    public String trigger(@PathVariable String fileOperCode, RedirectAttributes redirectAttributes) {
        schedulerManager.triggerNow(fileOperCode);
        redirectAttributes.addFlashAttribute("message", "任务已触发");
        return "redirect:/admin/jobs";
    }

    @PostMapping("/admin/files/save")
    public String saveFile(@ModelAttribute("fileForm") CodeFileOper fileForm, RedirectAttributes redirectAttributes) {
        codeFileOperAdminService.save(fileForm);
        redirectAttributes.addFlashAttribute("message", "文件任务已保存");
        return "redirect:/admin/files";
    }

    @PostMapping("/admin/files/{id}/delete")
    public String deleteFile(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        codeFileOperAdminService.delete(id);
        redirectAttributes.addFlashAttribute("message", "文件任务已删除");
        return "redirect:/admin/files";
    }

    @PostMapping("/admin/datasources/save")
    public String saveDatasource(@ModelAttribute("datasourceForm") ConfigPullDatasource datasourceForm,
                                 RedirectAttributes redirectAttributes) throws Exception {
        dynamicDatasourceAdminService.save(datasourceForm);
        redirectAttributes.addFlashAttribute("message", "动态数据源已保存");
        return "redirect:/admin/datasources";
    }

    @PostMapping("/admin/datasources/{datasourceCode}/test")
    public String testDatasource(@PathVariable String datasourceCode, RedirectAttributes redirectAttributes) {
        boolean success = dynamicDatasourceAdminService.testConnection(datasourceCode);
        redirectAttributes.addFlashAttribute("message", success ? "连接测试成功" : "连接测试失败");
        return "redirect:/admin/datasources";
    }

    @PostMapping("/admin/datasources/{datasourceCode}/toggle")
    public String toggleDatasource(@PathVariable String datasourceCode, RedirectAttributes redirectAttributes) {
        dynamicDatasourceAdminService.toggle(datasourceCode);
        redirectAttributes.addFlashAttribute("message", "动态数据源状态已切换");
        return "redirect:/admin/datasources";
    }

    private CodeFileOper defaultJob() {
        CodeFileOper record = new CodeFileOper();
        record.setState(1);
        record.setJobEnabled(0);
        record.setConcurrentMode("SERIAL");
        record.setOperType(1);
        record.setDownloadDealType(1);
        record.setSplitLabel(",");
        record.setFileFormat("CSV");
        record.setStopOnRowError(1);
        return record;
    }

    private ConfigPullDatasource defaultDatasource() {
        ConfigPullDatasource record = new ConfigPullDatasource();
        record.setState(1);
        record.setDatasourceType(1);
        record.setDriveName("com.mysql.cj.jdbc.Driver");
        return record;
    }
}
