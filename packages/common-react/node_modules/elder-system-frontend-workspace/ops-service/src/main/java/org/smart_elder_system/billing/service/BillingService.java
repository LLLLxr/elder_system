package org.smart_elder_system.billing.service;

import org.springframework.stereotype.Service;

@Service
public class BillingService {

    public String getModuleScope() {
        return "计费模块：负责费用规则、账单生成、支付与结算管理";
    }
}
