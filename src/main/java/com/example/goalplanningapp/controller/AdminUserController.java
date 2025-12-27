package com.example.goalplanningapp.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.goalplanningapp.entity.User;
import com.example.goalplanningapp.service.AdminUserService;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {
	// DI
	private final AdminUserService adminUserService;
	public AdminUserController(AdminUserService adminUserService) {
		this.adminUserService = adminUserService;
	}
	
	@GetMapping
	public String listUsers(
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) String status,
			@RequestParam(defaultValue = "createdAt") String sort,
			@RequestParam(defaultValue = "desc") String direction,
			@RequestParam(defaultValue = "0") int page,
			Model model
	) {
	    if (keyword != null) {
	        keyword = keyword.trim(); // 前後の空白除去
	        if (keyword.isEmpty()) {
	            keyword = null; // 空文字を null に変換
	        }
	    }
		// 次の方向を計算
		Sort sortObj = direction.equals("asc") ? Sort.by(sort).ascending() : Sort.by(sort).descending();
	    Pageable pageable = PageRequest.of(page, 10, sortObj);
	    // 検索
	    Page<User> userPage = adminUserService.searchUsers(keyword, status, pageable);
		
		model.addAttribute("userPage", userPage);
	    model.addAttribute("users", userPage.getContent());

	    model.addAttribute("keyword", keyword);
	    model.addAttribute("status", status);
	    model.addAttribute("sort", sort);
	    model.addAttribute("direction", direction);
	    model.addAttribute("currentPage", page);
	    
	    // 列ごとの次方向を計算
	    model.addAttribute("idNextDirection", "id".equals(sort) && "asc".equals(direction) ? "desc" : "asc");
	    model.addAttribute("createdAtNextDirection", "createdAt".equals(sort) && "asc".equals(direction) ? "desc" : "asc");

	    return "admin/users/list";
	}
	
	
	
	
	
	
	
	
	
	
	// 復活
	@PostMapping("/{id}/restore")
	public String restore(@PathVariable Integer id,
						  RedirectAttributes redirectAttributes) {
		adminUserService.restore(id);
		redirectAttributes.addFlashAttribute("successMessage","ユーザーを復活させました。");
		return "redirect:/admin/users";
	}
	
}
