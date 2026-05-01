package org.smart_elder_system.safetyemergency.service;

import org.springframework.stereotype.Service;

@Service
public class SafetyEmergencyService {

    public String getModuleScope() {
        return "安防应急模块：负责告警事件处置、应急流程与闭环记录";
    }
}
