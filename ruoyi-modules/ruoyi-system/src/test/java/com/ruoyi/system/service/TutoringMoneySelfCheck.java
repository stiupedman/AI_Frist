package com.ruoyi.system.service;

import java.math.BigDecimal;
import com.ruoyi.common.core.exception.ServiceException;

public final class TutoringMoneySelfCheck
{
    public static void main(String[] args)
    {
        assert TutoringMoney.fee(new BigDecimal("100.00")).compareTo(new BigDecimal("10.00")) == 0;
        assert TutoringMoney.fee(new BigDecimal("0.05")).compareTo(new BigDecimal("0.01")) == 0;
        assertInvalid(new BigDecimal("0"));
        assertInvalid(new BigDecimal("1.001"));
        assertInvalid(new BigDecimal("100000000"));
    }

    private static void assertInvalid(BigDecimal amount)
    {
        try { TutoringMoney.requireAmount(amount, "金额"); }
        catch (ServiceException expected) { return; }
        throw new AssertionError("expected invalid amount: " + amount);
    }
}
