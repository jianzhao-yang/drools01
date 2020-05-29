package com.loujinhe.droolsdynamicloading.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.loujinhe.droolsdynamicloading.dao.RulesDao;
import com.loujinhe.droolsdynamicloading.model.RuleInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 规则信息业务
 *
 * @author loujinhe
 * @date 2019/4/14 00:20
 */
@Service
public class RuleInfoServiceImpl implements IRuleInfoService {
    @Autowired
    private RulesDao rulesDao;

    /**
     * 获取给定场景下的规则信息列表
     *
     * @param sceneId 场景ID
     * @return 规则列表
     */
    public List<RuleInfo> getRuleInfoListBySceneId(Long sceneId) {
        Map<Long, List<RuleInfo>> sceneId2RuleInfoListMap = getRuleInfoListMap();
        return sceneId2RuleInfoListMap.get(sceneId);
    }

    /**
     * 获取场景与规则信息列表的Map
     *
     * @return 场景规则信息列表Map，Map(sceneId : List < RuleInfo >)
     */
    public Map<Long, List<RuleInfo>> getRuleInfoListMap() {
        Map<Long, List<RuleInfo>> sceneId2RuleInfoListMap = new HashMap<>();
        List<RuleInfo> allRuleInfos = selectRuleInfoList();
        for (RuleInfo ruleInfo : allRuleInfos) {
            List<RuleInfo> ruleInfos = sceneId2RuleInfoListMap.computeIfAbsent(ruleInfo.getSceneId(), k -> new ArrayList<>());
            ruleInfos.add(ruleInfo);
        }
        return sceneId2RuleInfoListMap;
    }

    /**
     * 生成规则信息列表，注意场景id和规则id的对应关系
     *
     * @return 规则信息列表
     */
    @Override
    public List<RuleInfo> selectRuleInfoList() {
        return rulesDao.selectList(new QueryWrapper<RuleInfo>() {});
//        int sceneNum = 5;
//        int ruleNumPerScene = 3;
//        int sceneFactor = 10000;
//
//        List<RuleInfo> ruleInfos = new ArrayList<>(sceneNum * ruleNumPerScene);
//        for (int i = 0; i < sceneNum; i++) {
//            long sceneId = sceneFactor * (i + 1);
//            for (int j = 0; j < ruleNumPerScene; j++) {
//                long id = sceneId + (j + 1);
//                ruleInfos.add(selectRuleInfo(sceneId, id));
//            }
//        }

//        return ruleInfos;
    }

    /**
     * 生成规则信息
     *
     * @param sceneId 场景ID
     * @param id      规则ID
     * @return 规则信息
     */
    @Override
    public RuleInfo selectRuleInfo(long sceneId, long id) {
        RuleInfo ruleInfo = new RuleInfo();
        ruleInfo.setId(id);
        ruleInfo.setSceneId(sceneId);
        ruleInfo.setContent(selectRuleContent(sceneId, id));
        return ruleInfo;
    }

    /**
     * 生成规则内容，每个场景id对应一个package，每个规则对应一个唯一的规则名
     *
     * 每次生成规则时记录时间戳，用来验证动态加载效果
     *
     * @param sceneId 场景ID
     * @param id      规则ID
     * @return 规则内容
     */
    public String selectRuleContent(long sceneId, long id) {
        QueryWrapper<RuleInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id).and(i -> i.eq("sceneId",sceneId));
        RuleInfo ruleInfo = rulesDao.selectOne(queryWrapper);
        if (ruleInfo == null) {
            return null;
        }
        return  ruleInfo.getContent();
//        String sceneIdStr = String.valueOf(sceneId);
//        String idStr = String.valueOf(id);
//        String nowStr = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
//
//        String content = "package rules.scene_{0};\n" +
//                "\n" +
//                "rule \"rule_{1}\"\n" +
//                "    when\n" +
//                "        eval(true);\n" +
//                "    then\n" +
//                "        System.out.println(\"{2} [{3}, {4}]\");\n" +
//                "end\n";
//        return MessageFormat.format(content, sceneIdStr, idStr, nowStr, sceneIdStr, idStr);
    }
}
