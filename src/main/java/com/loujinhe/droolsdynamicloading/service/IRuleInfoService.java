package com.loujinhe.droolsdynamicloading.service;

import com.loujinhe.droolsdynamicloading.model.RuleInfo;

import java.util.List;

public interface IRuleInfoService {
    List<RuleInfo> selectRuleInfoList();

    RuleInfo selectRuleInfo(long sceneId, long id);

}
