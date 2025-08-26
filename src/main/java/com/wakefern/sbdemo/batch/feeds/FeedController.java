package com.wakefern.sbdemo.batch.feeds;

import com.wakefern.sbdemo.util.WebUtils;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;


@Controller
@RequestMapping("/feeds")
public class FeedController {

    private final FeedService feedService;
    private final FeedExecutionHistoryService executionHistoryService;

    public FeedController(final FeedService feedService, final FeedExecutionHistoryService executionHistoryService) {
        this.feedService = feedService;
        this.executionHistoryService = executionHistoryService;
    }

    @GetMapping
    public String list(final Model model) {
        model.addAttribute("feeds", feedService.findAll());
        return "feeds/list";
    }

    @GetMapping("/add")
    public String add(@ModelAttribute("feed") final FeedDTO feedDTO) {
        return "feeds/add";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute("feed") @Valid final FeedDTO feedDTO,
            final BindingResult bindingResult, final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "feeds/add";
        }
        feedService.create(feedDTO);
        redirectAttributes.addFlashAttribute(WebUtils.MSG_SUCCESS, WebUtils.getMessage("feed.create.success"));
        return "redirect:/feeds";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable(name = "id") final Long id, final Model model) {
        model.addAttribute("feed", feedService.get(id));
        return "feeds/edit";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable(name = "id") final Long id,
            @ModelAttribute("feed") @Valid final FeedDTO feedDTO,
            final BindingResult bindingResult, final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "feeds/edit";
        }
        feedService.update(id, feedDTO);
        redirectAttributes.addFlashAttribute(WebUtils.MSG_SUCCESS, WebUtils.getMessage("feed.update.success"));
        return "redirect:/feeds";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable(name = "id") final Long id,
            final RedirectAttributes redirectAttributes) {
        feedService.delete(id);
        redirectAttributes.addFlashAttribute(WebUtils.MSG_INFO, WebUtils.getMessage("feed.delete.success"));
        return "redirect:/feeds";
    }

    @GetMapping("/execute/{id}")
    public String execute(@PathVariable(name = "id") final Long id, final Model model) {
        FeedDTO feed = feedService.get(id);
        
        FeedExecutionDTO execution = new FeedExecutionDTO();
        execution.setFeedId(feed.getId());
        execution.setFeedName(feed.getName());
        execution.setFeedType(feed.getType());
        
        model.addAttribute("execution", execution);
        model.addAttribute("feed", feed);
        return "feeds/execute";
    }

    @PostMapping("/execute/{id}")
    public String execute(@PathVariable(name = "id") final Long id,
            @ModelAttribute("execution") final FeedExecutionDTO executionDTO,
            final Model model, final RedirectAttributes redirectAttributes) {
        try {
            ExecutionResult result = feedService.executeFeed(id, executionDTO.getParameters());
            
            // Add execution result to model to display on the same page
            model.addAttribute("executionResult", result);
            
            // Also add the feed and execution data back to model
            FeedDTO feed = feedService.get(id);
            executionDTO.setFeedId(feed.getId());
            executionDTO.setFeedName(feed.getName());
            executionDTO.setFeedType(feed.getType());
            
            model.addAttribute("execution", executionDTO);
            model.addAttribute("feed", feed);
            
            // Add flash message for success/failure
            if (result.getStatus() == ExecutionResult.Status.SUCCESS) {
                model.addAttribute(WebUtils.MSG_SUCCESS, WebUtils.getMessage("feed.execute.success"));
            } else if (result.getStatus() == ExecutionResult.Status.FAILED) {
                model.addAttribute(WebUtils.MSG_ERROR, "Execution failed: " + result.getError());
            }
            
            return "feeds/execute";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(WebUtils.MSG_ERROR, "Failed to execute feed: " + e.getMessage());
            return "redirect:/feeds";
        }
    }

    @GetMapping("/{id}/history")
    public String history(@PathVariable(name = "id") final Long id, final Model model) {
        try {
            FeedDTO feed = feedService.get(id);
            model.addAttribute("feed", feed);
            
            // Get execution history
            List<FeedExecution> executions = executionHistoryService.getExecutionHistory(id);
            List<ExecutionHistoryViewModel> executionViews = executions.stream()
                    .map(this::mapToHistoryViewModel)
                    .collect(java.util.stream.Collectors.toList());
            model.addAttribute("executions", executionViews);
            
            // Get statistics
            FeedExecutionHistoryService.FeedExecutionStats stats = executionHistoryService.getExecutionStats(id);
            model.addAttribute("stats", stats);
            
            return "feeds/history";
        } catch (Exception e) {
            model.addAttribute(WebUtils.MSG_ERROR, "Failed to load execution history: " + e.getMessage());
            return "redirect:/feeds";
        }
    }

    @GetMapping("/{id}/history/{executionId}")
    public String historyDetail(@PathVariable(name = "id") final Long feedId,
                                @PathVariable(name = "executionId") final Long executionId,
                                final Model model) {
        try {
            FeedDTO feed = feedService.get(feedId);
            model.addAttribute("feed", feed);
            
            // Get detailed execution
            java.util.Optional<FeedExecution> executionOpt = executionHistoryService.getExecutionWithDetails(executionId);
            if (executionOpt.isPresent()) {
                FeedExecution execution = executionOpt.get();
                
                // Verify execution belongs to this feed
                if (!execution.getFeedId().equals(feedId)) {
                    throw new RuntimeException("Execution does not belong to this feed");
                }
                
                ExecutionDetailViewModel executionView = mapToDetailViewModel(execution);
                model.addAttribute("execution", executionView);
                
                return "feeds/history-detail";
            } else {
                model.addAttribute(WebUtils.MSG_ERROR, "Execution not found");
                return "redirect:/feeds/" + feedId + "/history";
            }
        } catch (Exception e) {
            model.addAttribute(WebUtils.MSG_ERROR, "Failed to load execution details: " + e.getMessage());
            return "redirect:/feeds/" + feedId + "/history";
        }
    }

    private ExecutionHistoryViewModel mapToHistoryViewModel(FeedExecution execution) {
        ExecutionHistoryViewModel viewModel = new ExecutionHistoryViewModel();
        viewModel.setId(execution.getId());
        viewModel.setFeedId(execution.getFeedId());
        viewModel.setStatus(execution.getStatus().name());
        viewModel.setStatusClass(getStatusClass(execution.getStatus().name()));
        viewModel.setStartTime(execution.getStartTime());
        viewModel.setEndTime(execution.getEndTime());
        viewModel.setDurationMillis(execution.getDurationMillis());
        viewModel.setDurationFormatted(formatDuration(execution.getDurationMillis()));
        viewModel.setParameters(execution.getParameters());
        viewModel.setError(execution.getError());
        viewModel.setCreatedAt(execution.getCreatedAt());
        return viewModel;
    }

    private ExecutionDetailViewModel mapToDetailViewModel(FeedExecution execution) {
        ExecutionDetailViewModel viewModel = new ExecutionDetailViewModel();
        viewModel.setId(execution.getId());
        viewModel.setFeedId(execution.getFeedId());
        viewModel.setStatus(execution.getStatus().name());
        viewModel.setStatusClass(getStatusClass(execution.getStatus().name()));
        viewModel.setStartTime(execution.getStartTime());
        viewModel.setEndTime(execution.getEndTime());
        viewModel.setDurationMillis(execution.getDurationMillis());
        viewModel.setDurationFormatted(formatDuration(execution.getDurationMillis()));
        viewModel.setParameters(execution.getParameters());
        viewModel.setError(execution.getError());
        viewModel.setCreatedAt(execution.getCreatedAt());
        
        // Map stages
        if (execution.getStages() != null) {
            List<StageViewModel> stageViews = execution.getStages().stream()
                    .map(this::mapToStageViewModel)
                    .collect(java.util.stream.Collectors.toList());
            viewModel.setStages(stageViews);
        }
        
        // Map logs  
        if (execution.getLogs() != null) {
            List<String> logMessages = execution.getLogs().stream()
                    .map(FeedExecutionLog::getMessage)
                    .collect(java.util.stream.Collectors.toList());
            viewModel.setLogs(logMessages);
        }
        
        return viewModel;
    }

    private StageViewModel mapToStageViewModel(FeedExecutionStage stage) {
        StageViewModel viewModel = new StageViewModel();
        viewModel.setId(stage.getId());
        viewModel.setName(stage.getName());
        viewModel.setDescription(stage.getDescription());
        viewModel.setStatus(stage.getStatus().name());
        viewModel.setStatusClass(getStatusClass(stage.getStatus().name()));
        viewModel.setStartTime(stage.getStartTime());
        viewModel.setEndTime(stage.getEndTime());
        viewModel.setDurationMillis(stage.getDurationMillis());
        viewModel.setDurationFormatted(formatDuration(stage.getDurationMillis()));
        viewModel.setParameters(stage.getParameters());
        viewModel.setError(stage.getError());
        viewModel.setStageOrder(stage.getStageOrder());
        return viewModel;
    }

    private String getStatusClass(String status) {
        return switch (status) {
            case "SUCCESS" -> "bg-success";
            case "FAILED" -> "bg-danger";
            case "IN_PROGRESS" -> "bg-warning text-dark";
            case "PENDING" -> "bg-secondary";
            default -> "bg-light";
        };
    }

    private String formatDuration(Long durationMillis) {
        if (durationMillis == null || durationMillis == 0) {
            return "0ms";
        }
        
        if (durationMillis < 1000) {
            return durationMillis + "ms";
        } else if (durationMillis < 60000) {
            return String.format("%.1fs", durationMillis / 1000.0);
        } else {
            long minutes = durationMillis / 60000;
            long seconds = (durationMillis % 60000) / 1000;
            return minutes + "m " + seconds + "s";
        }
    }

    // View Model Classes
    public static class ExecutionHistoryViewModel {
        private Long id;
        private Long feedId;
        private String status;
        private String statusClass;
        private java.time.LocalDateTime startTime;
        private java.time.LocalDateTime endTime;
        private Long durationMillis;
        private String durationFormatted;
        private String parameters;
        private String error;
        private java.time.LocalDateTime createdAt;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getFeedId() { return feedId; }
        public void setFeedId(Long feedId) { this.feedId = feedId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getStatusClass() { return statusClass; }
        public void setStatusClass(String statusClass) { this.statusClass = statusClass; }
        public java.time.LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(java.time.LocalDateTime startTime) { this.startTime = startTime; }
        public java.time.LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(java.time.LocalDateTime endTime) { this.endTime = endTime; }
        public Long getDurationMillis() { return durationMillis; }
        public void setDurationMillis(Long durationMillis) { this.durationMillis = durationMillis; }
        public String getDurationFormatted() { return durationFormatted; }
        public void setDurationFormatted(String durationFormatted) { this.durationFormatted = durationFormatted; }
        public String getParameters() { return parameters; }
        public void setParameters(String parameters) { this.parameters = parameters; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        public java.time.LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class ExecutionDetailViewModel extends ExecutionHistoryViewModel {
        private List<StageViewModel> stages;
        private List<String> logs;

        public List<StageViewModel> getStages() { return stages; }
        public void setStages(List<StageViewModel> stages) { this.stages = stages; }
        public List<String> getLogs() { return logs; }
        public void setLogs(List<String> logs) { this.logs = logs; }
    }

    public static class StageViewModel {
        private Long id;
        private String name;
        private String description;
        private String status;
        private String statusClass;
        private java.time.LocalDateTime startTime;
        private java.time.LocalDateTime endTime;
        private Long durationMillis;
        private String durationFormatted;
        private String parameters;
        private String error;
        private Integer stageOrder;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getStatusClass() { return statusClass; }
        public void setStatusClass(String statusClass) { this.statusClass = statusClass; }
        public java.time.LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(java.time.LocalDateTime startTime) { this.startTime = startTime; }
        public java.time.LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(java.time.LocalDateTime endTime) { this.endTime = endTime; }
        public Long getDurationMillis() { return durationMillis; }
        public void setDurationMillis(Long durationMillis) { this.durationMillis = durationMillis; }
        public String getDurationFormatted() { return durationFormatted; }
        public void setDurationFormatted(String durationFormatted) { this.durationFormatted = durationFormatted; }
        public String getParameters() { return parameters; }
        public void setParameters(String parameters) { this.parameters = parameters; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        public Integer getStageOrder() { return stageOrder; }
        public void setStageOrder(Integer stageOrder) { this.stageOrder = stageOrder; }
    }

}
