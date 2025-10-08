package com.example.librarymanagementsystem.controller;

import com.example.librarymanagementsystem.entity.Member;
import com.example.librarymanagementsystem.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/members")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @GetMapping
    public String listMembers(Model model) {
        model.addAttribute("members", memberService.getAllMembers());
        return "members";
    }

    @GetMapping("/new")
    public String showMemberForm(Model model) {
        model.addAttribute("member", new Member());
        return "member-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<Member> optional = memberService.getMemberById(id);
        if (optional.isPresent()) {
            model.addAttribute("member", optional.get());
            return "member-form";
        } else {
            return "redirect:/members";
        }
    }

    @PostMapping("/save")
    public String saveMember(@ModelAttribute Member member, RedirectAttributes redirectAttributes) {
        try {
            memberService.saveMember(member);
            redirectAttributes.addFlashAttribute("success", "Member saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error saving member: " + e.getMessage());
        }
        return "redirect:/members";
    }

    @GetMapping("/delete/{id}")
    public String deleteMember(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Member> memberOptional = memberService.getMemberById(id);
            if (memberOptional.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Member not found!");
                return "redirect:/members";
            }

            Member member = memberOptional.get();
            memberService.deleteMember(id);
            redirectAttributes.addFlashAttribute("success", "Member \"" + member.getName() + "\" deleted successfully!");
        } catch (IllegalStateException e) {
            String errorMessage = e.getMessage();
            if (errorMessage.contains("active loan")) {
                redirectAttributes.addFlashAttribute("error", "⛔ " + errorMessage);
            } else if (errorMessage.contains("loan history")) {
                redirectAttributes.addFlashAttribute("error", "⚠️ " + errorMessage);
            } else {
                redirectAttributes.addFlashAttribute("error", "⛔ " + errorMessage);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "⛔ Unexpected error: " + e.getMessage());
        }
        return "redirect:/members";
    }

    // Method for viewing member's loan information
    @GetMapping("/loans/{id}")
    public String viewMemberLoans(@PathVariable Long id, Model model) {
        Optional<Member> memberOptional = memberService.getMemberById(id);
        if (memberOptional.isEmpty()) {
            return "redirect:/members";
        }

        Member member = memberOptional.get();
        model.addAttribute("member", member);
        model.addAttribute("activeLoans", memberService.getActiveLoansForMember(id));
        model.addAttribute("returnedLoans", memberService.getReturnedLoansForMember(id));
        model.addAttribute("totalLoans", memberService.getTotalLoansForMember(id));

        return "member-loans";
    }

    // API endpoint for validating duplicate name
    @GetMapping("/api/validate-name")
    @ResponseBody
    public boolean validateName(@RequestParam String value, @RequestParam(required = false) String currentId) {
        List<Member> members = memberService.getAllMembers();
        Long currentMemberId = currentId != null && !currentId.isEmpty() ? Long.parseLong(currentId) : null;

        return members.stream()
                .filter(member -> !member.getId().equals(currentMemberId)) // Exclude current member in edit mode
                .anyMatch(member -> member.getName().equalsIgnoreCase(value.trim()));
    }

    // API endpoint for validating duplicate email
    @GetMapping("/api/validate-email")
    @ResponseBody
    public boolean validateEmail(@RequestParam String value, @RequestParam(required = false) String currentId) {
        List<Member> members = memberService.getAllMembers();
        Long currentMemberId = currentId != null && !currentId.isEmpty() ? Long.parseLong(currentId) : null;

        return members.stream()
                .filter(member -> !member.getId().equals(currentMemberId)) // Exclude current member in edit mode
                .anyMatch(member -> member.getEmail().equalsIgnoreCase(value.trim()));
    }

    // API endpoint for validating duplicate phone
    @GetMapping("/api/validate-phone")
    @ResponseBody
    public boolean validatePhone(@RequestParam String value, @RequestParam(required = false) String currentId) {
        List<Member> members = memberService.getAllMembers();
        Long currentMemberId = currentId != null && !currentId.isEmpty() ? Long.parseLong(currentId) : null;

        // Only validate if phone number is provided (since it's optional)
        if (value == null || value.trim().isEmpty()) {
            return false; // Empty phone is not a duplicate
        }

        return members.stream()
                .filter(member -> !member.getId().equals(currentMemberId)) // Exclude current member in edit mode
                .filter(member -> member.getPhone() != null && !member.getPhone().trim().isEmpty()) // Only compare with non-empty phones
                .anyMatch(member -> member.getPhone().equalsIgnoreCase(value.trim()));
    }
}