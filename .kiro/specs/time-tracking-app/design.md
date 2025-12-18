# 设计文档

## 概述

时间记录APP是一个原生Android应用，采用现代Android开发技术栈构建。应用使用Kotlin作为主要开发语言，遵循MVVM架构模式，使用Jetpack Compose构建声明式UI，Room数据库进行本地数据持久化。

### 技术栈

- **语言**: Kotlin
- **UI框架**: Jetpack Compose
- **架构模式**: MVVM (Model-View-ViewModel)
- **依赖注入**: Hilt
- **数据库**: Room
- **异步处理**: Kotlin Coroutines + Flow
- **导航**: Jetpack Navigation Compose
- **最低SDK版本**: API 24 (Android 7.0)
- **目标SDK版本**: API 34 (Android 14)

## 架构

### 整体架构

应用采用分层架构，遵循Clean Architecture原则：

```
┌─────────────────────────────────────┐
│         Presentation Layer          │
│  (Composables + ViewModels)         │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│          Domain Layer               │
│     (Use Cases + Models)            │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│           Data Layer                │
│  (Repositories + Data Sources)      │
└─────────────────────────────────────┘
```

### 模块结构

```
app/
├── data/
│   ├── local/
│   │   ├── dao/
│   │   ├── entity/
│   │   └── database/
│   └── repository/
├── domain/
│   ├── model/
│   ├── repository/
│   └── usecase/
├── presentation/
│   ├── home/
│   ├── timer/
│   ├── statistics/
│   ├── components/
│   └── theme/
└── di/
```

## 组件和接口

### 1. 数据层 (Data Layer)

#### 1.1 数据库实体

**ActivityEntity**
```kotlin
@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val color: Int,
    val iconName: String,
    val createdAt: Long,
    val isDeleted: Boolean = false
)
```

**ActivityRecordEntity**
```kotlin
@Entity(
    tableName = "activity_records",
    foreignKeys = [
        ForeignKey(
            entity = ActivityEntity::class,
            parentColumns = ["id"],
            childColumns = ["activityId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("activityId"), Index("startTime")]
)
data class ActivityRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val activityId: Long,
    val startTime: Long,
    val endTime: Long,
    val totalDuration: Long, // in milliseconds
    val notes: String? = null
)
```

#### 1.2 DAO接口

**ActivityDao**
```kotlin
@Dao
interface ActivityDao {
    @Query("SELECT * FROM activities WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun getAllActivities(): Flow<List<ActivityEntity>>
    
    @Insert
    suspend fun insertActivity(activity: ActivityEntity): Long
    
    @Update
    suspend fun updateActivity(activity: ActivityEntity)
    
    @Query("UPDATE activities SET isDeleted = 1 WHERE id = :activityId")
    suspend fun softDeleteActivity(activityId: Long)
    
    @Query("SELECT * FROM activities WHERE id = :activityId")
    suspend fun getActivityById(activityId: Long): ActivityEntity?
}
```

**ActivityRecordDao**
```kotlin
@Dao
interface ActivityRecordDao {
    @Insert
    suspend fun insertRecord(record: ActivityRecordEntity): Long
    
    @Query("SELECT * FROM activity_records WHERE activityId = :activityId ORDER BY startTime DESC")
    fun getRecordsByActivity(activityId: Long): Flow<List<ActivityRecordEntity>>
    
    @Query("SELECT * FROM activity_records WHERE startTime >= :startTime AND startTime < :endTime ORDER BY startTime ASC")
    fun getRecordsByDateRange(startTime: Long, endTime: Long): Flow<List<ActivityRecordEntity>>
    
    @Query("SELECT SUM(totalDuration) FROM activity_records WHERE activityId = :activityId")
    fun getTotalDurationByActivity(activityId: Long): Flow<Long?>
    
    @Query("SELECT * FROM activity_records WHERE DATE(startTime/1000, 'unixepoch', 'localtime') = DATE(:timestamp/1000, 'unixepoch', 'localtime') ORDER BY startTime ASC")
    fun getRecordsByDay(timestamp: Long): Flow<List<ActivityRecordEntity>>
}
```

#### 1.3 Repository实现

**ActivityRepository**
```kotlin
interface ActivityRepository {
    fun getAllActivities(): Flow<List<Activity>>
    suspend fun createActivity(name: String, color: Int, iconName: String): Long
    suspend fun updateActivity(activity: Activity)
    suspend fun deleteActivity(activityId: Long)
    fun getActivityWithTotalDuration(activityId: Long): Flow<ActivityWithDuration>
}
```

**ActivityRecordRepository**
```kotlin
interface ActivityRecordRepository {
    suspend fun createRecord(activityId: Long, startTime: Long, endTime: Long, duration: Long): Long
    fun getRecordsByActivity(activityId: Long): Flow<List<ActivityRecord>>
    fun getRecordsByDateRange(startTime: Long, endTime: Long): Flow<List<ActivityRecord>>
    fun getRecordsByDay(timestamp: Long): Flow<List<ActivityRecord>>
    fun getDailyDurations(year: Int, month: Int): Flow<Map<Int, Long>>
}
```

### 2. 领域层 (Domain Layer)

#### 2.1 领域模型

**Activity**
```kotlin
data class Activity(
    val id: Long,
    val name: String,
    val color: Int,
    val iconName: String,
    val createdAt: Long
)
```

**ActivityWithDuration**
```kotlin
data class ActivityWithDuration(
    val activity: Activity,
    val totalDurationMillis: Long
) {
    val totalHours: Float
        get() = totalDurationMillis / (1000f * 60 * 60)
}
```

**ActivityRecord**
```kotlin
data class ActivityRecord(
    val id: Long,
    val activityId: Long,
    val activityName: String,
    val activityColor: Int,
    val startTime: Long,
    val endTime: Long,
    val totalDuration: Long,
    val notes: String?
)
```

**TimerState**
```kotlin
sealed class TimerState {
    object Idle : TimerState()
    data class Running(
        val activityId: Long,
        val startTime: Long,
        val elapsedTime: Long
    ) : TimerState()
    data class Paused(
        val activityId: Long,
        val startTime: Long,
        val elapsedTime: Long
    ) : TimerState()
}
```

#### 2.2 Use Cases

**StartTimerUseCase**
```kotlin
class StartTimerUseCase @Inject constructor() {
    operator fun invoke(activityId: Long): TimerState.Running {
        return TimerState.Running(
            activityId = activityId,
            startTime = System.currentTimeMillis(),
            elapsedTime = 0L
        )
    }
}
```

**SaveActivityRecordUseCase**
```kotlin
class SaveActivityRecordUseCase @Inject constructor(
    private val repository: ActivityRecordRepository
) {
    suspend operator fun invoke(
        activityId: Long,
        startTime: Long,
        duration: Long
    ): Result<Long> {
        return try {
            val endTime = startTime + duration
            val recordId = repository.createRecord(activityId, startTime, endTime, duration)
            Result.success(recordId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

**GetDailyStatisticsUseCase**
```kotlin
class GetDailyStatisticsUseCase @Inject constructor(
    private val repository: ActivityRecordRepository
) {
    operator fun invoke(timestamp: Long): Flow<DailyStatistics> {
        return repository.getRecordsByDay(timestamp).map { records ->
            DailyStatistics(
                date = timestamp,
                records = records,
                totalDuration = records.sumOf { it.totalDuration }
            )
        }
    }
}
```

**GetYearHeatmapDataUseCase**
```kotlin
class GetYearHeatmapDataUseCase @Inject constructor(
    private val repository: ActivityRecordRepository
) {
    operator fun invoke(year: Int, activityId: Long?): Flow<YearHeatmapData> {
        return flow {
            val monthlyData = mutableMapOf<Int, Map<Int, Long>>()
            for (month in 1..12) {
                repository.getDailyDurations(year, month).collect { dailyDurations ->
                    monthlyData[month] = dailyDurations
                }
            }
            emit(YearHeatmapData(year, monthlyData))
        }
    }
}
```

### 3. 表现层 (Presentation Layer)

#### 3.1 主页面 (Home Screen)

**HomeViewModel**
```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val startTimerUseCase: StartTimerUseCase
) : ViewModel() {
    
    val activitiesWithDuration: StateFlow<List<ActivityWithDuration>>
    val timerState: StateFlow<TimerState>
    
    fun onActivityCardClick(activityId: Long)
    fun onCreateActivity(name: String, color: Int, iconName: String)
    fun onDeleteActivity(activityId: Long)
}
```

**HomeScreen Composable**
- 使用LazyVerticalGrid展示活动卡片
- 每个卡片显示活动名称、图标、颜色和累计小时数
- 支持添加新活动的FloatingActionButton
- 长按卡片显示编辑/删除选项

#### 3.2 计时器页面 (Timer Screen)

**TimerViewModel**
```kotlin
@HiltViewModel
class TimerViewModel @Inject constructor(
    private val saveActivityRecordUseCase: SaveActivityRecordUseCase
) : ViewModel() {
    
    val timerState: StateFlow<TimerState>
    val elapsedTime: StateFlow<Long>
    
    fun pauseTimer()
    fun resumeTimer()
    fun finishTimer()
    
    private fun startTimerTick()
}
```

**TimerScreen Composable**
- 全屏显示当前活动信息
- 大号数字显示已用时间（HH:MM:SS格式）
- 暂停/继续按钮
- 结束按钮
- 使用LaunchedEffect实现每秒更新

#### 3.3 统计页面 (Statistics Screen)

**StatisticsViewModel**
```kotlin
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val getDailyStatisticsUseCase: GetDailyStatisticsUseCase,
    private val getYearHeatmapDataUseCase: GetYearHeatmapDataUseCase,
    private val activityRepository: ActivityRepository
) : ViewModel() {
    
    val viewMode: StateFlow<StatisticsViewMode> // Year, Month, Day
    val selectedActivity: StateFlow<Activity?>
    val heatmapData: StateFlow<YearHeatmapData?>
    val dailyRecords: StateFlow<List<ActivityRecord>>
    
    fun setViewMode(mode: StatisticsViewMode)
    fun setSelectedActivity(activity: Activity?)
    fun selectDate(timestamp: Long)
}
```

**StatisticsScreen Composable**
- 顶部工具栏：视图模式切换（年/月/日）+ 活动筛选器
- 年度视图：12个月的热力图，使用Canvas绘制
- 月度视图：日历网格视图
- 日度视图：活动记录列表

**HeatmapView Composable**
```kotlin
@Composable
fun HeatmapView(
    data: YearHeatmapData,
    onDayClick: (Long) -> Unit
) {
    // 使用Canvas绘制热力图
    // 颜色强度映射：0小时=浅色，最大值=深色
    // 支持点击交互
}
```

### 4. 导航结构

```kotlin
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Timer : Screen("timer/{activityId}")
    object Statistics : Screen("statistics")
    object ActivityDetail : Screen("activity/{activityId}")
}
```

## 数据模型

### 数据流

1. **创建活动记录流程**:
   ```
   User clicks ActivityCard 
   → HomeViewModel.onActivityCardClick()
   → Navigate to TimerScreen
   → TimerViewModel starts timer
   → User clicks finish
   → SaveActivityRecordUseCase
   → ActivityRecordRepository.createRecord()
   → Room Database
   ```

2. **统计数据查询流程**:
   ```
   User navigates to Statistics
   → StatisticsViewModel loads data
   → GetYearHeatmapDataUseCase
   → ActivityRecordRepository queries
   → Room Database with aggregation
   → Flow emits updates
   → UI recomposes
   ```

### 数据库关系

```
ActivityEntity (1) ──────< (N) ActivityRecordEntity
     │
     └─ 软删除标记 (isDeleted)
```

## 错误处理

### 错误类型

1. **数据库错误**
   - 使用try-catch包装所有数据库操作
   - 返回Result类型封装成功/失败状态
   - 在ViewModel层处理并显示用户友好的错误消息

2. **计时器状态错误**
   - 验证状态转换的合法性
   - 在应用被杀死时保存计时器状态到SharedPreferences
   - 应用重启时恢复计时器状态

3. **数据一致性错误**
   - 使用数据库事务确保原子性操作
   - 外键约束保证引用完整性

### 错误恢复策略

```kotlin
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val retry: (() -> Unit)? = null) : UiState<Nothing>()
}
```

## 测试策略

### 单元测试

1. **ViewModel测试**
   - 使用MockK模拟Repository
   - 测试状态转换逻辑
   - 验证Use Case调用

2. **Use Case测试**
   - 测试业务逻辑
   - 验证数据转换
   - 边界条件测试

3. **Repository测试**
   - 使用In-Memory Room数据库
   - 测试CRUD操作
   - 验证Flow数据流

### UI测试

1. **Compose UI测试**
   - 使用ComposeTestRule
   - 测试用户交互
   - 验证UI状态变化

2. **导航测试**
   - 测试屏幕间导航
   - 验证参数传递

### 集成测试

- 端到端场景测试
- 数据持久化验证
- 后台计时准确性测试

## UI/UX设计

### Material Design 3

- 使用Material 3组件库
- 动态颜色主题支持
- 遵循Material Design动画指南

### 颜色方案

**热力图颜色映射**
```kotlin
fun getDurationColor(duration: Long, maxDuration: Long): Color {
    val intensity = (duration.toFloat() / maxDuration).coerceIn(0f, 1f)
    return when {
        intensity == 0f -> Color.Gray.copy(alpha = 0.1f)
        intensity < 0.25f -> Color(0xFF9BE9A8) // 浅绿
        intensity < 0.5f -> Color(0xFF40C463)  // 中绿
        intensity < 0.75f -> Color(0xFF30A14E) // 深绿
        else -> Color(0xFF216E39)              // 最深绿
    }
}
```

### 动画

1. **卡片点击动画**: 缩放 + 淡入淡出
2. **页面切换**: 滑动转场
3. **计时器数字**: 翻页效果
4. **热力图**: 渐显动画

### 响应式布局

- 使用Modifier.fillMaxWidth()和权重布局
- 支持横屏和竖屏
- 适配不同屏幕尺寸（手机、平板）

## 性能优化

### 数据库优化

1. 为常用查询字段添加索引
2. 使用分页加载历史记录
3. 定期清理过期数据

### UI性能

1. 使用LazyColumn/LazyGrid延迟加载
2. 记忆化Composable（remember, derivedStateOf）
3. 避免不必要的重组

### 内存管理

1. 及时取消Coroutine
2. 使用Flow而非LiveData减少内存占用
3. 图片资源使用矢量图

## 安全和隐私

1. 所有数据存储在本地，不上传到服务器
2. 使用Android Keystore加密敏感数据（如果需要）
3. 遵循Android权限最佳实践

## 未来扩展

1. **数据导出**: 支持导出CSV/JSON格式
2. **云同步**: 可选的云端备份功能
3. **提醒功能**: 定时提醒用户记录活动
4. **目标设置**: 为每个活动设置每日/每周目标
5. **小部件**: 主屏幕快捷计时小部件
6. **数据分析**: 更多统计图表（饼图、折线图等）
