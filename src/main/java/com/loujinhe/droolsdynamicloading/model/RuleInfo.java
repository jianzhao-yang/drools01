package com.loujinhe.droolsdynamicloading.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;

/**
 * 规则信息
 *
 * @author loujinhe
 * @date 2019/4/14 00:13
 */
@Data
@ToString
@TableName("t_r_rule_info")
public class RuleInfo {

    /**
     * 规则id，全局唯一
     */
    @TableField("id")
    @TableId
    private Long id;

    /**
     * 场景id，一个场景对应多个规则，一个场景对应一个业务场景，一个场景对应一个kmodule
     */
    @TableField("sceneId")
    private Long sceneId;

    /**
     * 规则内容，既drl文件内容
     */
    @TableField("content")
    private String content;

    /**
     * rule描述
     */
    @TableField("name")
    private String name;

}
