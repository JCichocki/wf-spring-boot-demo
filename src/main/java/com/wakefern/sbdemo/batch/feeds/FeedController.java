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


@Controller
@RequestMapping("/feeds")
public class FeedController {

    private final FeedService feedService;

    public FeedController(final FeedService feedService) {
        this.feedService = feedService;
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

}
