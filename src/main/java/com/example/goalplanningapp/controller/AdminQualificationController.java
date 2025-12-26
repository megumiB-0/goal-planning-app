package com.example.goalplanningapp.controller;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.goalplanningapp.entity.Qualification;
import com.example.goalplanningapp.form.QualificationRegisterForm;
import com.example.goalplanningapp.service.AdminQualificationService;
import com.example.goalplanningapp.service.QualificationService;



@Controller
@RequestMapping("/admin/qualifications")
public class AdminQualificationController {
	private final QualificationService qualificationService;
	private final AdminQualificationService adminQualificationService;
	public AdminQualificationController(QualificationService qualificationService,
										AdminQualificationService adminQualificationService) {
		this.qualificationService = qualificationService;
		this.adminQualificationService = adminQualificationService;
	}
	
	// 一覧・検索
	@GetMapping
	public String List(@RequestParam(required = false) String keyword,
					   @RequestParam(defaultValue = "0") int page,
					   Model model) {
		int pageSize = 10;
		Page<Qualification> qualificationsPage = adminQualificationService.findAdminQualifications(keyword,page,pageSize);
		model.addAttribute("qualificationsPage", qualificationsPage);
		model.addAttribute("keyword", keyword);
		return "admin/qualifications/list";
	}
	
	// 資格情報登録　フォーム表示
	@GetMapping("/new")
	public String register(Model model) {
		model.addAttribute("qualificationRegisterForm", new QualificationRegisterForm());
		model.addAttribute("isEdit", false); // 新規登録なので false
		return "admin/qualifications/form";
	}
	
	// 資格情報登録　登録
	@PostMapping("/save")
	public String create(QualificationRegisterForm form, RedirectAttributes redirectAttributes) {
		   Qualification q;
		   if (form.getId() != null) {
		        // 編集の場合
		        q = qualificationService.findQualificationById(form.getId()).orElseThrow();
		        q.setName(form.getName());
		        q.setEstimatedMinutes(form.getEstimatedHours() * 60);
		        adminQualificationService.saveByAdmin(q);
		        redirectAttributes.addFlashAttribute("successMessage", "更新に成功しました。");
		    } else {
		        // 新規登録の場合
		        q = new Qualification();
		        q.setName(form.getName());
		        q.setEstimatedMinutes(form.getEstimatedHours() * 60);
		        q.setUser(null); // 管理者用

		        // 仮の root_qualification_id を設定して NOT NULL 制約を回避
		        q.setRootQualificationId(0);

		        // まず保存して ID を生成
		        q = adminQualificationService.saveByAdmin(q);

		        // 生成された ID を root_qualification_id にセットして再保存
		        if (q.getId() != null) {
		            q.setRootQualificationId(q.getId());
		            adminQualificationService.saveByAdmin(q);
		        }

		        redirectAttributes.addFlashAttribute("successMessage", "登録に成功しました。");
		    }

	    
		return "redirect:/admin/qualifications";
	    
	}
	
	
	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable Integer id, Model model) {
		Qualification q = qualificationService.findQualificationById(id).orElseThrow();
		QualificationRegisterForm form = new QualificationRegisterForm();
		form.setId(q.getId()); // 編集時のみId取得
		form.setName(q.getName());
		form.setEstimatedHours(q.getEstimatedMinutes() / 60.0); // 分 → 時間に変換
		form.setRootQualificationId(q.getRootQualificationId()); 
		model.addAttribute("qualificationRegisterForm", form);
		model.addAttribute("qualificationId", q.getId()); // 更新用に ID を送る
		model.addAttribute("isEdit", true); // 編集モード		
		return "admin/qualifications/form";
	}
	
	@PostMapping("/{id}/delete")
	public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
		adminQualificationService.delete(id);
	    redirectAttributes.addFlashAttribute("successMessage", 
	    "資格を削除しました。");
		return "redirect:/admin/qualifications";
	}
}
