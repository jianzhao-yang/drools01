# drools-dynamic-loading

#### 介绍
Drools动态加载规则

2019-04-15 Drools动态加载规则功能

# 规则引擎Drools动态加载规则 顶 原

[![img](https://www.oschina.net/img/hot3.png)](https://www.oschina.net/action/visit/ad?id=1184)

### 背景

最近，因工作需要做了规则引擎的调研，对比了多个规则引擎后，最终选择开源规则引擎Drools。

Drools的优点很多，而我决定使用Drools的原因主要是：

- 非常活跃的社区支持（JBoss支持）；
- 快速的执行速度；
- 完善的功能；
- 国外金融领域使用比较多；

当然，Drools也有很多缺点：

- 复杂（功能越多也意味着越复杂）；
- 文档欠缺（官方文档混乱、缺少中文文档）；
- 学习成本高；

为了能完全掌控Drools，最近一直在学习，使用各种方法学习Drools，通过在线文章、官方文档、官方API、视频教程等学习，并且有潜伏进一个Drools学习交流群，发现群里同学问的最多的一个问题就是：怎么样通过数据库动态加载规则？正好，我们也打算采用从数据库动态加载规则，并且我正好解决了这个问题，现在写篇文章分享出来给大家，希望能帮助到大家。

### 事例

#### 事例说明

首先，通过代码生成规则模拟从数据库查询规则，每条规则记录包含：id（规则id，全局唯一）、sceneId（场景id，一个场景对应多个规则）、content（规则内容，既drl文件内容）。

然后，按场景动态加载规则，每个场景对应一个实际的业务场景，通过场景隔离不同的业务。

最后，执行应用指定场景规则，模拟不同业务场景下的规则应用。

#### 事例实现

##### 1. 创建Maven项目，引入依赖

```
    <properties>
        <drools.version>7.20.0.Final</drools.version>
        <java.version>1.8</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.kie</groupId>
            <artifactId>kie-api</artifactId>
            <version>${drools.version}</version>
        </dependency>
        <dependency>
            <groupId>org.drools</groupId>
            <artifactId>drools-core</artifactId>
            <version>${drools.version}</version>
        </dependency>
        <dependency>
            <groupId>org.drools</groupId>
            <artifactId>drools-compiler</artifactId>
            <version>${drools.version}</version>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
```

##### 2. 创建规则记录结构

```
package com.loujinhe.droolsdynamicloading.model;

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
public class RuleInfo {

    /**
     * 规则id，全局唯一
     */
    private Long id;

    /**
     * 场景id，一个场景对应多个规则，一个场景对应一个业务场景，一个场景对应一个kmodule
     */
    private Long sceneId;

    /**
     * 规则内容，既drl文件内容
     */
    private String content;

}
```

##### 3. 创建规则查询接口

这里通过代码生成规则记录模拟从数据库查询规则记录。

```
package com.loujinhe.droolsdynamicloading.service;

import com.loujinhe.droolsdynamicloading.model.RuleInfo;
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
public class RuleInfoService {

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
        List<RuleInfo> allRuleInfos = generateRuleInfoList();
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
    private List<RuleInfo> generateRuleInfoList() {
        int sceneNum = 5;
        int ruleNumPerScene = 3;
        int sceneFactor = 10000;

        List<RuleInfo> ruleInfos = new ArrayList<>(sceneNum * ruleNumPerScene);
        for (int i = 0; i < sceneNum; i++) {
            long sceneId = sceneFactor * (i + 1);
            for (int j = 0; j < ruleNumPerScene; j++) {
                long id = sceneId + (j + 1);
                ruleInfos.add(generateRuleInfo(sceneId, id));
            }
        }

        return ruleInfos;
    }

    /**
     * 生成规则信息
     *
     * @param sceneId 场景ID
     * @param id      规则ID
     * @return 规则信息
     */
    private RuleInfo generateRuleInfo(long sceneId, long id) {
        RuleInfo ruleInfo = new RuleInfo();
        ruleInfo.setId(id);
        ruleInfo.setSceneId(sceneId);
        ruleInfo.setContent(generateRuleContent(sceneId, id));
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
    private String generateRuleContent(long sceneId, long id) {
        String sceneIdStr = String.valueOf(sceneId);
        String idStr = String.valueOf(id);
        String nowStr = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

        String content = "package rules.scene_{0};\n" +
                "\n" +
                "rule \"rule_{1}\"\n" +
                "    when\n" +
                "        eval(true);\n" +
                "    then\n" +
                "        System.out.println(\"{2} [{3}, {4}]\");\n" +
                "end\n";
        return MessageFormat.format(content, sceneIdStr, idStr, nowStr, sceneIdStr, idStr);
    }
}
```

##### 4. 创建规则加载器

可以加载全部场景下的规则，也可以加载指定场景下的规则。

```
package com.loujinhe.droolsdynamicloading.drools;

import com.loujinhe.droolsdynamicloading.model.RuleInfo;
import com.loujinhe.droolsdynamicloading.service.RuleInfoService;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.runtime.KieContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 规则加载器
 *
 * @author loujinhe
 * @date 2019/4/14 00:06
 */
@Component
public class RuleLoader implements ApplicationRunner {

    /**
     * key:kcontainerName,value:KieContainer，每个场景对应一个KieContainer
     */
    private final ConcurrentMap<String, KieContainer> kieContainerMap = new ConcurrentHashMap<>();

    @Autowired
    private RuleInfoService ruleInfoService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        reloadAll();
    }

    /**
     * 构造kcontainerName
     *
     * @param sceneId 场景ID
     * @return kcontainerName
     */
    private String buildKcontainerName(long sceneId) {
        return "kcontainer_" + sceneId;
    }

    /**
     * 构造kbaseName
     *
     * @param sceneId 场景ID
     * @return kbaseName
     */
    private String buildKbaseName(long sceneId) {
        return "kbase_" + sceneId;
    }

    /**
     * 构造ksessionName
     *
     * @param sceneId 场景ID
     * @return ksessionName
     */
    private String buildKsessionName(long sceneId) {
        return "ksession_" + sceneId;
    }

    KieContainer getKieContainerBySceneId(long sceneId) {
        return kieContainerMap.get(buildKcontainerName(sceneId));
    }

    /**
     * 重新加载所有规则
     */
    public void reloadAll() {
        Map<Long, List<RuleInfo>> sceneId2RuleInfoListMap = ruleInfoService.getRuleInfoListMap();
        for (Map.Entry<Long, List<RuleInfo>> entry : sceneId2RuleInfoListMap.entrySet()) {
            long sceneId = entry.getKey();
            reload(sceneId, entry.getValue());
        }
        System.out.println("reload all success");
    }

    /**
     * 重新加载给定场景下的规则
     *
     * @param sceneId 场景ID
     */
    public void reload(Long sceneId) {
        List<RuleInfo> ruleInfos = ruleInfoService.getRuleInfoListBySceneId(sceneId);
        reload(sceneId, ruleInfos);
        System.out.println("reload success");
    }

    /**
     * 重新加载给定场景给定规则列表，对应一个kmodule
     *
     * @param sceneId   场景ID
     * @param ruleInfos 规则列表
     */
    private void reload(long sceneId, List<RuleInfo> ruleInfos) {
        KieServices kieServices = KieServices.get();
        KieModuleModel kieModuleModel = kieServices.newKieModuleModel();
        KieBaseModel kieBaseModel = kieModuleModel.newKieBaseModel(buildKbaseName(sceneId));
        kieBaseModel.setDefault(true);
        kieBaseModel.addPackage(MessageFormat.format("rules.scene_{0}", String.valueOf(sceneId)));
        kieBaseModel.newKieSessionModel(buildKsessionName(sceneId));

        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        for (RuleInfo ruleInfo : ruleInfos) {
            String fullPath = MessageFormat.format("src/main/resources/rules/scene_{0}/rule_{1}.drl", String.valueOf(sceneId), String.valueOf(ruleInfo.getId()));
            kieFileSystem.write(fullPath, ruleInfo.getContent());
        }
        kieFileSystem.writeKModuleXML(kieModuleModel.toXML());

        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem).buildAll();
        Results results = kieBuilder.getResults();
        if (results.hasMessages(Message.Level.ERROR)) {
            System.out.println(results.getMessages());
            throw new IllegalStateException("rule error");
        }

        KieContainer kieContainer = kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId());
        kieContainerMap.put(buildKcontainerName(sceneId), kieContainer);
    }
}
```

##### 5. 创建KieSession辅助类

通过该辅助类可以获取指定场景下的KieSession，通过该KieSession可以与指定场景规则交互。

```
package com.loujinhe.droolsdynamicloading.drools;

import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * KieSession助手类
 *
 * @author loujinhe
 * @date 2019/4/15 22:29
 */
@Component
public class KieSessionHelper {

    @Autowired
    private RuleLoader ruleLoader;

    /**
     * 获取KieSession
     *
     * @param sceneId 场景ID
     * @return KieSession
     */
    public KieSession getKieSessionBySceneId(long sceneId) {
        return ruleLoader.getKieContainerBySceneId(sceneId).getKieBase().newKieSession();
    }
}
```

##### 6. 创建业务入口

包含动态加载指定场景规则功能，以及触发指定场景下的规则功能。

```
package com.loujinhe.droolsdynamicloading.controller;

import com.loujinhe.droolsdynamicloading.drools.KieSessionHelper;
import com.loujinhe.droolsdynamicloading.drools.RuleLoader;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 规则测试
 *
 * @author loujinhe
 * @date 2019/4/14 01:35
 */
@RequestMapping("rule")
@RestController
public class RuleController {

    @Autowired
    private RuleLoader ruleLoader;

    @Autowired
    private KieSessionHelper kieSessionHelper;

    @GetMapping("/")
    public String index() {
        System.out.println("index");
        return "success";
    }

    /**
     * 重新加载所有规则
     */
    @GetMapping("reload")
    public String reload() {
        System.out.println("reload all");
        ruleLoader.reloadAll();
        return "success";
    }

    /**
     * 重新加载给定场景下的规则
     *
     * @param sceneId 场景ID
     */
    @GetMapping("reload/{sceneId}")
    public String reload(@PathVariable("sceneId") Long sceneId) {
        System.out.println("reload scene:" + sceneId);
        ruleLoader.reload(sceneId);
        return "success";
    }

    /**
     * 触发给定场景规则
     *
     * @param sceneId 场景ID
     */
    @GetMapping("fire/{sceneId}")
    public String fire(@PathVariable("sceneId") Long sceneId) {
        System.out.println("fire scene:" + sceneId);
        KieSession kieSession = kieSessionHelper.getKieSessionBySceneId(sceneId);
        kieSession.fireAllRules();
        kieSession.dispose();
        return "success";
    }

}
```

##### 7. 启动服务

默认加载所有场景下的规则，启动时日志如下：

```
/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/bin/java -XX:TieredStopAtLevel=1 -noverify -Dspring.output.ansi.enabled=always -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=54524 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=127.0.0.1 -Dspring.liveBeansView.mbeanDomain -Dspring.application.admin.enabled=true "-javaagent:/Applications/IntelliJ IDEA.app/Contents/lib/idea_rt.jar=54525:/Applications/IntelliJ IDEA.app/Contents/bin" -Dfile.encoding=UTF-8 -classpath /Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/jre/lib/charsets.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/jre/lib/deploy.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/jre/lib/ext/cldrdata.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/jre/lib/ext/dnsns.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/jre/lib/ext/jaccess.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/jre/lib/ext/jfxrt.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/jre/lib/ext/localedata.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/jre/lib/ext/nashorn.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/jre/lib/ext/sunec.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/jre/lib/ext/sunjce_provider.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/jre/lib/ext/sunpkcs11.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/jre/lib/ext/zipfs.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/jre/lib/javaws.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/jre/lib/jce.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/jre/lib/jfr.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/jre/lib/jfxswt.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/jre/lib/jsse.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/jre/lib/management-agent.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/jre/lib/plugin.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/jre/lib/resources.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/jre/lib/rt.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/lib/ant-javafx.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/lib/dt.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/lib/javafx-mx.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/lib/jconsole.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/lib/packager.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/lib/sa-jdi.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/lib/tools.jar:/Users/loujinhe/project/IdeaProjects/drools-dynamic-loading/target/classes:/Users/loujinhe/.m2/repository/org/kie/kie-api/7.20.0.Final/kie-api-7.20.0.Final.jar:/Users/loujinhe/.m2/repository/org/kie/soup/kie-soup-maven-support/7.20.0.Final/kie-soup-maven-support-7.20.0.Final.jar:/Users/loujinhe/.m2/repository/org/slf4j/slf4j-api/1.7.26/slf4j-api-1.7.26.jar:/Users/loujinhe/.m2/repository/org/drools/drools-core/7.20.0.Final/drools-core-7.20.0.Final.jar:/Users/loujinhe/.m2/repository/org/mvel/mvel2/2.4.4.Final/mvel2-2.4.4.Final.jar:/Users/loujinhe/.m2/repository/org/kie/kie-internal/7.20.0.Final/kie-internal-7.20.0.Final.jar:/Users/loujinhe/.m2/repository/org/kie/soup/kie-soup-commons/7.20.0.Final/kie-soup-commons-7.20.0.Final.jar:/Users/loujinhe/.m2/repository/org/kie/soup/kie-soup-project-datamodel-commons/7.20.0.Final/kie-soup-project-datamodel-commons-7.20.0.Final.jar:/Users/loujinhe/.m2/repository/org/kie/soup/kie-soup-project-datamodel-api/7.20.0.Final/kie-soup-project-datamodel-api-7.20.0.Final.jar:/Users/loujinhe/.m2/repository/commons-codec/commons-codec/1.11/commons-codec-1.11.jar:/Users/loujinhe/.m2/repository/org/drools/drools-compiler/7.20.0.Final/drools-compiler-7.20.0.Final.jar:/Users/loujinhe/.m2/repository/org/antlr/antlr-runtime/3.5.2/antlr-runtime-3.5.2.jar:/Users/loujinhe/.m2/repository/org/eclipse/jdt/core/compiler/ecj/4.6.1/ecj-4.6.1.jar:/Users/loujinhe/.m2/repository/com/thoughtworks/xstream/xstream/1.4.10/xstream-1.4.10.jar:/Users/loujinhe/.m2/repository/xmlpull/xmlpull/1.1.3.1/xmlpull-1.1.3.1.jar:/Users/loujinhe/.m2/repository/xpp3/xpp3_min/1.1.4c/xpp3_min-1.1.4c.jar:/Users/loujinhe/.m2/repository/com/google/protobuf/protobuf-java/3.6.1/protobuf-java-3.6.1.jar:/Users/loujinhe/.m2/repository/org/projectlombok/lombok/1.18.6/lombok-1.18.6.jar:/Users/loujinhe/.m2/repository/org/springframework/boot/spring-boot-starter-web/2.1.4.RELEASE/spring-boot-starter-web-2.1.4.RELEASE.jar:/Users/loujinhe/.m2/repository/org/springframework/boot/spring-boot-starter/2.1.4.RELEASE/spring-boot-starter-2.1.4.RELEASE.jar:/Users/loujinhe/.m2/repository/org/springframework/boot/spring-boot/2.1.4.RELEASE/spring-boot-2.1.4.RELEASE.jar:/Users/loujinhe/.m2/repository/org/springframework/boot/spring-boot-autoconfigure/2.1.4.RELEASE/spring-boot-autoconfigure-2.1.4.RELEASE.jar:/Users/loujinhe/.m2/repository/org/springframework/boot/spring-boot-starter-logging/2.1.4.RELEASE/spring-boot-starter-logging-2.1.4.RELEASE.jar:/Users/loujinhe/.m2/repository/ch/qos/logback/logback-classic/1.2.3/logback-classic-1.2.3.jar:/Users/loujinhe/.m2/repository/ch/qos/logback/logback-core/1.2.3/logback-core-1.2.3.jar:/Users/loujinhe/.m2/repository/org/apache/logging/log4j/log4j-to-slf4j/2.11.2/log4j-to-slf4j-2.11.2.jar:/Users/loujinhe/.m2/repository/org/apache/logging/log4j/log4j-api/2.11.2/log4j-api-2.11.2.jar:/Users/loujinhe/.m2/repository/org/slf4j/jul-to-slf4j/1.7.26/jul-to-slf4j-1.7.26.jar:/Users/loujinhe/.m2/repository/javax/annotation/javax.annotation-api/1.3.2/javax.annotation-api-1.3.2.jar:/Users/loujinhe/.m2/repository/org/yaml/snakeyaml/1.23/snakeyaml-1.23.jar:/Users/loujinhe/.m2/repository/org/springframework/boot/spring-boot-starter-json/2.1.4.RELEASE/spring-boot-starter-json-2.1.4.RELEASE.jar:/Users/loujinhe/.m2/repository/com/fasterxml/jackson/core/jackson-databind/2.9.8/jackson-databind-2.9.8.jar:/Users/loujinhe/.m2/repository/com/fasterxml/jackson/core/jackson-annotations/2.9.0/jackson-annotations-2.9.0.jar:/Users/loujinhe/.m2/repository/com/fasterxml/jackson/core/jackson-core/2.9.8/jackson-core-2.9.8.jar:/Users/loujinhe/.m2/repository/com/fasterxml/jackson/datatype/jackson-datatype-jdk8/2.9.8/jackson-datatype-jdk8-2.9.8.jar:/Users/loujinhe/.m2/repository/com/fasterxml/jackson/datatype/jackson-datatype-jsr310/2.9.8/jackson-datatype-jsr310-2.9.8.jar:/Users/loujinhe/.m2/repository/com/fasterxml/jackson/module/jackson-module-parameter-names/2.9.8/jackson-module-parameter-names-2.9.8.jar:/Users/loujinhe/.m2/repository/org/springframework/boot/spring-boot-starter-tomcat/2.1.4.RELEASE/spring-boot-starter-tomcat-2.1.4.RELEASE.jar:/Users/loujinhe/.m2/repository/org/apache/tomcat/embed/tomcat-embed-core/9.0.17/tomcat-embed-core-9.0.17.jar:/Users/loujinhe/.m2/repository/org/apache/tomcat/embed/tomcat-embed-el/9.0.17/tomcat-embed-el-9.0.17.jar:/Users/loujinhe/.m2/repository/org/apache/tomcat/embed/tomcat-embed-websocket/9.0.17/tomcat-embed-websocket-9.0.17.jar:/Users/loujinhe/.m2/repository/org/hibernate/validator/hibernate-validator/6.0.16.Final/hibernate-validator-6.0.16.Final.jar:/Users/loujinhe/.m2/repository/javax/validation/validation-api/2.0.1.Final/validation-api-2.0.1.Final.jar:/Users/loujinhe/.m2/repository/org/jboss/logging/jboss-logging/3.3.2.Final/jboss-logging-3.3.2.Final.jar:/Users/loujinhe/.m2/repository/com/fasterxml/classmate/1.4.0/classmate-1.4.0.jar:/Users/loujinhe/.m2/repository/org/springframework/spring-web/5.1.6.RELEASE/spring-web-5.1.6.RELEASE.jar:/Users/loujinhe/.m2/repository/org/springframework/spring-beans/5.1.6.RELEASE/spring-beans-5.1.6.RELEASE.jar:/Users/loujinhe/.m2/repository/org/springframework/spring-webmvc/5.1.6.RELEASE/spring-webmvc-5.1.6.RELEASE.jar:/Users/loujinhe/.m2/repository/org/springframework/spring-aop/5.1.6.RELEASE/spring-aop-5.1.6.RELEASE.jar:/Users/loujinhe/.m2/repository/org/springframework/spring-context/5.1.6.RELEASE/spring-context-5.1.6.RELEASE.jar:/Users/loujinhe/.m2/repository/org/springframework/spring-expression/5.1.6.RELEASE/spring-expression-5.1.6.RELEASE.jar:/Users/loujinhe/.m2/repository/org/springframework/spring-core/5.1.6.RELEASE/spring-core-5.1.6.RELEASE.jar:/Users/loujinhe/.m2/repository/org/springframework/spring-jcl/5.1.6.RELEASE/spring-jcl-5.1.6.RELEASE.jar com.loujinhe.droolsdynamicloading.DroolsDynamicLoadingApplication

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v2.1.4.RELEASE)

2019-05-11 01:26:16.189  INFO 5866 --- [           main] c.l.d.DroolsDynamicLoadingApplication    : Starting DroolsDynamicLoadingApplication on loujinhedeMacBook-Pro.local with PID 5866 (/Users/loujinhe/project/IdeaProjects/drools-dynamic-loading/target/classes started by loujinhe in /Users/loujinhe/project/IdeaProjects/drools-dynamic-loading)
2019-05-11 01:26:16.191  INFO 5866 --- [           main] c.l.d.DroolsDynamicLoadingApplication    : No active profile set, falling back to default profiles: default
2019-05-11 01:26:16.836  INFO 5866 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8080 (http)
2019-05-11 01:26:16.851  INFO 5866 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2019-05-11 01:26:16.851  INFO 5866 --- [           main] org.apache.catalina.core.StandardEngine  : Starting Servlet engine: [Apache Tomcat/9.0.17]
2019-05-11 01:26:16.910  INFO 5866 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2019-05-11 01:26:16.910  INFO 5866 --- [           main] o.s.web.context.ContextLoader            : Root WebApplicationContext: initialization completed in 696 ms
2019-05-11 01:26:17.052  INFO 5866 --- [           main] o.s.s.concurrent.ThreadPoolTaskExecutor  : Initializing ExecutorService 'applicationTaskExecutor'
2019-05-11 01:26:17.203  INFO 5866 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
2019-05-11 01:26:17.206  INFO 5866 --- [           main] c.l.d.DroolsDynamicLoadingApplication    : Started DroolsDynamicLoadingApplication in 1.224 seconds (JVM running for 1.626)
2019-05-11 01:26:17.220  INFO 5866 --- [           main] o.k.a.i.utils.ServiceDiscoveryImpl       : Loading kie.conf from  jar:file:/Users/loujinhe/.m2/repository/org/drools/drools-core/7.20.0.Final/drools-core-7.20.0.Final.jar!/META-INF/kie.conf in classloader sun.misc.Launcher$AppClassLoader@18b4aac2
2019-05-11 01:26:17.221  INFO 5866 --- [           main] o.k.a.i.utils.ServiceDiscoveryImpl       : Adding Service org.drools.core.io.impl.ResourceFactoryServiceImpl

2019-05-11 01:26:17.222  INFO 5866 --- [           main] o.k.a.i.utils.ServiceDiscoveryImpl       : Adding Service org.drools.core.marshalling.impl.MarshallerProviderImpl

2019-05-11 01:26:17.222  INFO 5866 --- [           main] o.k.a.i.utils.ServiceDiscoveryImpl       : Adding Service org.drools.core.concurrent.ExecutorProviderImpl

2019-05-11 01:26:17.222  INFO 5866 --- [           main] o.k.a.i.utils.ServiceDiscoveryImpl       : Loading kie.conf from  jar:file:/Users/loujinhe/.m2/repository/org/kie/kie-internal/7.20.0.Final/kie-internal-7.20.0.Final.jar!/META-INF/kie.conf in classloader sun.misc.Launcher$AppClassLoader@18b4aac2
2019-05-11 01:26:17.223  INFO 5866 --- [           main] o.k.a.i.utils.ServiceDiscoveryImpl       : Adding Service org.kie.internal.services.KieAssemblersImpl

2019-05-11 01:26:17.223  INFO 5866 --- [           main] o.k.a.i.utils.ServiceDiscoveryImpl       : Adding Service org.kie.internal.services.KieRuntimesImpl

2019-05-11 01:26:17.224  INFO 5866 --- [           main] o.k.a.i.utils.ServiceDiscoveryImpl       : Adding Service org.kie.internal.services.KieWeaversImpl

2019-05-11 01:26:17.224  INFO 5866 --- [           main] o.k.a.i.utils.ServiceDiscoveryImpl       : Adding Service org.kie.internal.services.KieBeliefsImpl

2019-05-11 01:26:17.224  INFO 5866 --- [           main] o.k.a.i.utils.ServiceDiscoveryImpl       : Loading kie.conf from  jar:file:/Users/loujinhe/.m2/repository/org/drools/drools-compiler/7.20.0.Final/drools-compiler-7.20.0.Final.jar!/META-INF/kie.conf in classloader sun.misc.Launcher$AppClassLoader@18b4aac2
2019-05-11 01:26:17.225  INFO 5866 --- [           main] o.k.a.i.utils.ServiceDiscoveryImpl       : Adding Service org.drools.compiler.kie.builder.impl.KieServicesImpl

2019-05-11 01:26:17.226  INFO 5866 --- [           main] o.k.a.i.utils.ServiceDiscoveryImpl       : Adding Service org.drools.compiler.builder.impl.KnowledgeBuilderFactoryServiceImpl

2019-05-11 01:26:17.778  INFO 5866 --- [           main] o.d.c.k.builder.impl.KieRepositoryImpl   : KieModule was added: MemoryKieModule[releaseId=org.default:artifact:1.0.0]
2019-05-11 01:26:17.800  INFO 5866 --- [           main] o.d.c.k.builder.impl.KieRepositoryImpl   : KieModule was added: MemoryKieModule[releaseId=org.default:artifact:1.0.0]
2019-05-11 01:26:17.816  INFO 5866 --- [           main] o.d.c.k.builder.impl.KieRepositoryImpl   : KieModule was added: MemoryKieModule[releaseId=org.default:artifact:1.0.0]
2019-05-11 01:26:17.831  INFO 5866 --- [           main] o.d.c.k.builder.impl.KieRepositoryImpl   : KieModule was added: MemoryKieModule[releaseId=org.default:artifact:1.0.0]
2019-05-11 01:26:17.846  INFO 5866 --- [           main] o.d.c.k.builder.impl.KieRepositoryImpl   : KieModule was added: MemoryKieModule[releaseId=org.default:artifact:1.0.0]
reload all success
```

##### 8. 验证

**8.1 首先，分别触发场景10000、20000、30000下的规则，访问url及日志如下：**
http://localhost:8080/rule/fire/10000
http://localhost:8080/rule/fire/20000
http://localhost:8080/rule/fire/30000

```
2019-05-11 01:32:01.844  INFO 5866 --- [nio-8080-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2019-05-11 01:32:01.844  INFO 5866 --- [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2019-05-11 01:32:01.851  INFO 5866 --- [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 7 ms

fire scene:10000
20190511012617 [10000, 10002]
20190511012617 [10000, 10003]
20190511012617 [10000, 10001]

fire scene:20000
20190511012617 [20000, 20001]
20190511012617 [20000, 20003]
20190511012617 [20000, 20002]

fire scene:30000
20190511012617 [30000, 30001]
20190511012617 [30000, 30002]
20190511012617 [30000, 30003]
```

通过分析日志发现，规则是按场景隔离的，并且规则的生成时间戳都是：20190511012617。

**8.2 然后，加载场景20000下的规则，访问url及日志如下：**
http://localhost:8080/rule/reload/20000

```
reload scene:20000
2019-05-11 01:34:45.988  INFO 5866 --- [nio-8080-exec-9] o.d.c.k.builder.impl.KieRepositoryImpl   : KieModule was added: MemoryKieModule[releaseId=org.default:artifact:1.0.0]
reload success
```

通过分析日志发现，规则加载成功（应该）。

**8.3 最后，分别触发场景10000、20000、30000下的规则，访问url及日志如下：**
http://localhost:8080/rule/fire/10000
http://localhost:8080/rule/fire/20000
http://localhost:8080/rule/fire/30000

```
fire scene:10000
20190511012617 [10000, 10002]
20190511012617 [10000, 10003]
20190511012617 [10000, 10001]

fire scene:20000
20190511013445 [20000, 20001]
20190511013445 [20000, 20002]
20190511013445 [20000, 20003]

fire scene:30000
20190511012617 [30000, 30001]
20190511012617 [30000, 30002]
20190511012617 [30000, 30003]
```

通过分析日志发现，场景10000、30000的时间戳是一样的，跟第一次加载时的时间戳是一样的，而场景20000的时间戳不一样，为20190511013445正好是我们刚才手动动态加载时的时间戳，说明动态加载指定场景规则成功。

#### 事例总结

整个动态加载规则的核心代码就几行，核心代码如下：

```java
/**
     * 重新加载给定场景给定规则列表，对应一个kmodule
     *
     * @param sceneId   场景ID
     * @param ruleInfos 规则列表
     */
    private void reload(long sceneId, List<RuleInfo> ruleInfos) {
        KieServices kieServices = KieServices.get();
        KieModuleModel kieModuleModel = kieServices.newKieModuleModel();
        KieBaseModel kieBaseModel = kieModuleModel.newKieBaseModel(buildKbaseName(sceneId));
        kieBaseModel.setDefault(true);
        kieBaseModel.addPackage(MessageFormat.format("rules.scene_{0}", String.valueOf(sceneId)));
        kieBaseModel.newKieSessionModel(buildKsessionName(sceneId));

        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        for (RuleInfo ruleInfo : ruleInfos) {
            String fullPath = MessageFormat.format("src/main/resources/rules/scene_{0}/rule_{1}.drl", String.valueOf(sceneId), String.valueOf(ruleInfo.getId()));
            kieFileSystem.write(fullPath, ruleInfo.getContent());
        }
        kieFileSystem.writeKModuleXML(kieModuleModel.toXML());

        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem).buildAll();
        Results results = kieBuilder.getResults();
        if (results.hasMessages(Message.Level.ERROR)) {
            System.out.println(results.getMessages());
            throw new IllegalStateException("rule error");
        }

        KieContainer kieContainer = kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId());
        kieContainerMap.put(buildKcontainerName(sceneId), kieContainer);
    }
```

通过分析代码发现，动态加载规则的方式跟通过kmodule.xml配置文件加载规则的方式是对应的，如：KieModuleModel 对应kmodule节点，KieBaseModel对应kbase节点，并且最终也是通过转换成kmodule.xml配置文件的方式加载规则的，如：kieFileSystem.writeKModuleXML(kieModuleModel.toXML())。

### 总结

Drools很好很强大，文档很烂很差劲，希望我的绵薄之力可以节省大家的宝贵时间。

**备注**
代码已提交到码云：https://gitee.com/loujinhe/drools-dynamic-loading


 