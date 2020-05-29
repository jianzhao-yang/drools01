/*
 Navicat Premium Data Transfer

 Source Server         : dat
 Source Server Type    : MySQL
 Source Server Version : 50726
 Source Host           : 10.5.85.58:3306
 Source Schema         : dh_test

 Target Server Type    : MySQL
 Target Server Version : 50726
 File Encoding         : 65001

 Date: 29/05/2020 16:58:53
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for t_r_rule_info
-- ----------------------------
DROP TABLE IF EXISTS `t_r_rule_info`;
CREATE TABLE `t_r_rule_info`  (
  `id` int(11) NOT NULL,
  `sceneId` int(11) NULL DEFAULT NULL,
  `content` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_r_rule_info
-- ----------------------------
INSERT INTO `t_r_rule_info` VALUES (1, 1, 'package com.xu.drools;\r\nimport com.xu.drools.bean.Person;\r\nrule \"2\"\r\n\r\n	when\r\n        $p : Person(age > 30);\r\n    then\r\n        $p.setName(\"李四\");\r\n		System.out.println(\"hello, young xu2!\");\r\nend\r\n\r\nquery \"people2\"\r\n    person : Person( age > 30 )\r\nend', 'test');

SET FOREIGN_KEY_CHECKS = 1;
