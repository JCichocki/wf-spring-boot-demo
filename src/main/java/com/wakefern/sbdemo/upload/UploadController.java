package com.wakefern.sbdemo.upload;

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
@RequestMapping("/uploads")
public class UploadController {

    private final UploadService uploadService;

    public UploadController(final UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @GetMapping
    public String list(final Model model) {
        model.addAttribute("uploads", uploadService.findAll());
        return "upload/list";
    }

    @GetMapping("/add")
    public String add(@ModelAttribute("upload") final UploadDTO uploadDTO) {
        return "upload/add";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute("upload") @Valid final UploadDTO uploadDTO,
            final BindingResult bindingResult, final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "upload/add";
        }
        uploadService.create(uploadDTO);
        redirectAttributes.addFlashAttribute(WebUtils.MSG_SUCCESS, WebUtils.getMessage("upload.create.success"));
        return "redirect:/uploads";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable(name = "id") final Long id, final Model model) {
        model.addAttribute("upload", uploadService.get(id));
        return "upload/edit";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable(name = "id") final Long id,
            @ModelAttribute("upload") @Valid final UploadDTO uploadDTO,
            final BindingResult bindingResult, final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "upload/edit";
        }
        uploadService.update(id, uploadDTO);
        redirectAttributes.addFlashAttribute(WebUtils.MSG_SUCCESS, WebUtils.getMessage("upload.update.success"));
        return "redirect:/uploads";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable(name = "id") final Long id,
            final RedirectAttributes redirectAttributes) {
        uploadService.delete(id);
        redirectAttributes.addFlashAttribute(WebUtils.MSG_INFO, WebUtils.getMessage("upload.delete.success"));
        return "redirect:/uploads";
    }

}
