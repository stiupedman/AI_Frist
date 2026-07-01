package com.ruoyi.system.service;

import com.ruoyi.common.core.exception.ServiceException;

public final class TutoringStatus
{
    public static final String REQUEST_OPEN = "0";
    public static final String REQUEST_MATCHED = "1";
    public static final String REQUEST_COMPLETED = "2";

    public static final String MATCH_APPLIED = "0";
    public static final String MATCH_ACCEPTED = "1";
    public static final String MATCH_COMPLETED = "2";
    public static final String MATCH_REJECTED = "3";

    private TutoringStatus()
    {
    }

    public static void requireRequestTransition(String current, String target)
    {
        if (!canRequestTransition(current, target))
        {
            throw new ServiceException("当前需求状态不允许此操作");
        }
    }

    public static void requireMatchTransition(String current, String target)
    {
        if (!canMatchTransition(current, target))
        {
            throw new ServiceException("当前订单状态不允许此操作");
        }
    }

    static boolean canRequestTransition(String current, String target)
    {
        return REQUEST_OPEN.equals(current) && REQUEST_MATCHED.equals(target)
            || REQUEST_MATCHED.equals(current) && REQUEST_COMPLETED.equals(target);
    }

    static boolean canMatchTransition(String current, String target)
    {
        return MATCH_APPLIED.equals(current) && (MATCH_ACCEPTED.equals(target) || MATCH_REJECTED.equals(target))
            || MATCH_ACCEPTED.equals(current) && MATCH_COMPLETED.equals(target);
    }

    public static void main(String[] args)
    {
        assert canRequestTransition(REQUEST_OPEN, REQUEST_MATCHED);
        assert !canRequestTransition(REQUEST_OPEN, REQUEST_COMPLETED);
        assert canMatchTransition(MATCH_APPLIED, MATCH_ACCEPTED);
        assert !canMatchTransition(MATCH_REJECTED, MATCH_ACCEPTED);
    }
}
