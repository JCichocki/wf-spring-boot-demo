package com.wakefern.sbdemo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/processes")
public class ProcessController {

    @GetMapping
    public String list(Model model) {
        // TODO: Add actual process data when available
        return "processes/index";
    }

    @GetMapping("/list")
    public String listView(Model model) {
        // TODO: Add process list logic
        return "processes/list";
    }
}