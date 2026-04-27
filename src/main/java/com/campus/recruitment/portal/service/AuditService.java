package com.campus.recruitment.portal.service;

import com.campus.recruitment.portal.model.AuditLog;
import com.campus.recruitment.portal.model.User;
import com.campus.recruitment.portal.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class AuditService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public void log(User user, String action, String details) {
        String ipAddress = getClientIp();
        AuditLog log = new AuditLog(user, action, details, ipAddress);
        auditLogRepository.save(log);
    }

    private String getClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String xfHeader = request.getHeader("X-Forwarded-For");
            if (xfHeader == null) {
                return request.getRemoteAddr();
            }
            return xfHeader.split(",")[0];
        }
        return "Unknown";
    }
}
