# 时间记录 APP (Time Tracking App)

一个帮助用户追踪和统计日常活动时间的Android应用。

## 技术栈

- **语言**: Kotlin
- **UI框架**: Jetpack Compose
- **架构模式**: MVVM (Model-View-ViewModel)
- **依赖注入**: Hilt
- **数据库**: Room
- **异步处理**: Kotlin Coroutines + Flow
- **导航**: Jetpack Navigation Compose
- **最低SDK版本**: API 24 (Android 7.0)
- **目标SDK版本**: API 34 (Android 14)

## 项目结构

```
app/src/main/java/com/timetracking/app/
├── data/                      # 数据层
│   ├── local/
│   │   ├── dao/              # Room DAO接口
│   │   ├── entity/           # Room实体类
│   │   └── database/         # Room数据库
│   └── repository/           # Repository实现
├── domain/                    # 领域层
│   ├── model/                # 领域模型
│   ├── repository/           # Repository接口
│   └── usecase/              # Use Cases
├── presentation/              # 表现层
│   ├── home/                 # 主页面
│   ├── timer/                # 计时器页面
│   ├── statistics/           # 统计页面
│   ├── components/           # 可复用组件
│   └── theme/                # Material Design主题
└── di/                       # 依赖注入模块
```

## 主要依赖

- Jetpack Compose BOM 2023.10.01
- Hilt 2.48
- Room 2.6.1
- Navigation Compose 2.7.5
- Kotlin Coroutines 1.7.3

## 构建项目

1. 克隆仓库
2. 使用Android Studio打开项目
3. 同步Gradle依赖
4. 运行应用

## 开发指南

详细的需求、设计和实施计划请参考：
- [需求文档](.kiro/specs/time-tracking-app/requirements.md)
- [设计文档](.kiro/specs/time-tracking-app/design.md)
- [实施计划](.kiro/specs/time-tracking-app/tasks.md)
