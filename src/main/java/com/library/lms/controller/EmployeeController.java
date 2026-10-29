package com.library.lms.controller;

import com.library.lms.entity.Employee;
import com.library.lms.service.EmployeeService;
import com.library.lms.web.FlashService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
public class EmployeeController {

    private final EmployeeService employeeService;
    private final FlashService flashService;

    public EmployeeController(EmployeeService employeeService, FlashService flashService) {
        this.employeeService = employeeService;
        this.flashService = flashService;
    }

    @GetMapping("/employees")
    public String list(Model model) {
        model.addAttribute("employees", employeeService.listActive());
        return "employees";
    }

    @GetMapping("/employees/add")
    public String addForm() {
        return "add_employee";
    }

    @PostMapping("/employees/add")
    public String add(@RequestParam("name") String name,
                      @RequestParam(value = "designation", required = false) String designation,
                      @RequestParam(value = "email", required = false) String email,
                      @RequestParam(value = "phone", required = false) String phone,
                      @RequestParam(value = "salary", required = false) BigDecimal salary,
                      @RequestParam(value = "permanent_address", required = false) String permanentAddress,
                      @RequestParam(value = "temporary_address", required = false) String temporaryAddress) {
        employeeService.create(name, null, email, phone, designation, salary,
                permanentAddress, temporaryAddress);
        flashService.add("success", "Employee added.");
        return "redirect:/employees";
    }

    @GetMapping("/employees/edit/{rowNum}")
    public String editForm(@PathVariable("rowNum") Integer rowNum, Model model) {
        Employee employee = employeeService.getByRowNum(rowNum)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found."));
        model.addAttribute("employee", employee);
        model.addAttribute("rowNum", rowNum);
        return "edit_employee";
    }

    @PostMapping("/employees/edit/{rowNum}")
    public String edit(@PathVariable("rowNum") Integer rowNum,
                       @RequestParam("name") String name,
                       @RequestParam(value = "password", required = false) String password,
                       @RequestParam(value = "email", required = false) String email,
                       @RequestParam(value = "phone", required = false) String phone,
                       @RequestParam(value = "designation", required = false) String designation,
                       @RequestParam(value = "salary", required = false) BigDecimal salary,
                       @RequestParam(value = "permanent_address", required = false) String permanentAddress,
                       @RequestParam(value = "temporary_address", required = false) String temporaryAddress) {
        employeeService.update(rowNum, name, password, email, phone, designation, salary,
                permanentAddress, temporaryAddress);
        flashService.add("success", "Employee updated.");
        return "redirect:/employees";
    }

    @PostMapping("/employees/delete/{rowNum}")
    public String delete(@PathVariable("rowNum") Integer rowNum) {
        employeeService.softDelete(rowNum);
        flashService.add("success", "Employee deleted.");
        return "redirect:/employees";
    }
}
