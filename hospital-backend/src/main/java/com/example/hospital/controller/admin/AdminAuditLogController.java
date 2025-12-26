package com.example.hospital.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.hospital.common.Result;
import com.example.hospital.entity.AuditLog;
import com.example.hospital.service.AdminAuditLogService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/audit-logs")
public class AdminAuditLogController {

    private final AdminAuditLogService auditLogService;

    public AdminAuditLogController(AdminAuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public Result<IPage<AuditLog>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String roleType,
            @RequestParam(required = false) String keyword
    ) {
        return Result.success(
                auditLogService.pageLogs(page, size, roleType, keyword)
        );
    }
}
