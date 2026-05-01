package org.smart_elder_system.resourcescheduling.service;

import org.springframework.stereotype.Service;

@Service
public class ResourceSchedulingService {

    public String getModuleScope() {
        return "资源调度模块：负责床位管理、护理排班与服务资源调度";
    }
}
