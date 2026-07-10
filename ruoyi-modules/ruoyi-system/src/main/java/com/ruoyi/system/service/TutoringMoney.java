package com.ruoyi.system.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import com.ruoyi.common.core.exception.ServiceException;

final class TutoringMoney
{
    private static final BigDecimal MAX = new BigDecimal("99999999.99");

    private TutoringMoney() {}

    static BigDecimal requireAmount(BigDecimal amount, String label)
    {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0
            || amount.compareTo(MAX) > 0 || amount.scale() > 2)
        {
            throw new ServiceException(label + "必须大于0、最多两位小数且不超过99999999.99");
        }
        return amount;
    }

    static BigDecimal fee(BigDecimal amount)
    {
        return requireAmount(amount, "付款金额").multiply(new BigDecimal("0.10"))
            .setScale(2, RoundingMode.HALF_UP);
    }
}
