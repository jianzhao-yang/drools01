package com.loujinhe.droolsdynamicloading.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.loujinhe.droolsdynamicloading.model.RuleInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RulesDao extends BaseMapper<RuleInfo> {

}
