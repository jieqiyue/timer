# 性能优化指南

## 已修复的性能问题

### 1. ✅ 移除无限动画
**问题**: TimerDisplay 使用了 `infiniteRepeatable` 动画，导致持续的重组和性能问题。

**修复**: 
- 移除了无限重复的脉冲动画
- 使用简单的弹簧动画，只在状态变化时触发
- 添加了 `tween` 动画规格以控制动画时长

### 2. ✅ 优化卡片动画
**问题**: ActivityCard 的按压状态可能不会正确重置。

**修复**:
- 添加了 `finishedListener` 确保动画完成后重置状态
- 使用 `StiffnessMedium` 替代 `StiffnessLow` 以加快动画速度
- 为 elevation 动画添加了 `tween(150)` 以提高响应速度

## 性能优化建议

### 模拟器设置
如果应用在模拟器中仍然卡顿，请检查：

1. **增加模拟器内存**:
   - 打开 AVD Manager
   - 编辑虚拟设备
   - 增加 RAM 到至少 2048 MB
   - 增加 VM heap 到 512 MB

2. **启用硬件加速**:
   - 确保启用了 Hardware - GLES 2.0
   - 在 AVD 设置中启用 "Use Host GPU"

3. **使用更快的模拟器**:
   - 使用 x86_64 架构而不是 ARM
   - 使用较新的 Android 版本（API 30+）

### 代码优化

#### 1. 使用 remember 和 derivedStateOf
```kotlin
// 避免不必要的重组
val formattedTime = remember(elapsedTime) {
    formatTime(elapsedTime)
}
```

#### 2. 稳定的参数
确保传递给 Composable 的参数是稳定的：
```kotlin
@Stable
data class ActivityWithDuration(...)
```

#### 3. 避免在 Composable 中创建对象
```kotlin
// ❌ 不好 - 每次重组都创建新对象
val gradientColors = listOf(color, color.copy(alpha = 0.9f))

// ✅ 好 - 使用 remember
val gradientColors = remember(color) {
    listOf(color, color.copy(alpha = 0.9f))
}
```

### 数据库优化

#### 1. 使用索引
确保频繁查询的字段有索引：
```kotlin
@Entity(
    indices = [Index("activityId"), Index("startTime")]
)
```

#### 2. 限制查询结果
```kotlin
@Query("SELECT * FROM activity_records ORDER BY startTime DESC LIMIT 100")
```

#### 3. 使用分页
对于大量数据，使用 Paging 3：
```kotlin
@Query("SELECT * FROM activity_records ORDER BY startTime DESC")
fun getRecordsPaged(): PagingSource<Int, ActivityRecordEntity>
```

### UI 优化

#### 1. LazyColumn/LazyGrid 优化
```kotlin
LazyColumn(
    // 使用 key 以提高性能
    items(records, key = { it.id }) { record ->
        RecordItem(record)
    }
)
```

#### 2. 避免过度嵌套
```kotlin
// ❌ 不好 - 过度嵌套
Box {
    Column {
        Row {
            Box { ... }
        }
    }
}

// ✅ 好 - 扁平化
Column {
    Row { ... }
}
```

#### 3. 使用 Modifier 缓存
```kotlin
// 在 Composable 外部定义常用的 Modifier
private val CardModifier = Modifier
    .fillMaxWidth()
    .padding(16.dp)
```

### 动画优化

#### 1. 限制动画数量
- 同时运行的动画不要超过 3-4 个
- 避免在列表项中使用复杂动画

#### 2. 使用合适的动画规格
```kotlin
// 快速动画
animationSpec = tween(150)

// 弹性动画
animationSpec = spring(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMedium
)
```

#### 3. 避免无限动画
- 不要在列表项中使用无限动画
- 只在必要时使用脉冲效果

### 内存优化

#### 1. 及时取消 Coroutine
```kotlin
override fun onCleared() {
    super.onCleared()
    timerJob?.cancel()
}
```

#### 2. 使用 Flow 而不是 LiveData
Flow 更轻量级，内存占用更少。

#### 3. 清理资源
```kotlin
DisposableEffect(key) {
    onDispose {
        // 清理资源
    }
}
```

## 性能监控

### 使用 Android Profiler
1. 打开 Android Studio
2. View → Tool Windows → Profiler
3. 监控 CPU、内存和网络使用

### 检查重组次数
```kotlin
@Composable
fun DebugComposition() {
    val count = remember { mutableStateOf(0) }
    SideEffect {
        count.value++
        Log.d("Recomposition", "Count: ${count.value}")
    }
}
```

### 使用 Layout Inspector
1. Tools → Layout Inspector
2. 检查视图层次
3. 查找过度嵌套

## 测试性能

### 在真实设备上测试
模拟器性能不能代表真实设备：
- 在低端设备上测试
- 在不同 Android 版本上测试
- 测试长时间运行的场景

### 压力测试
- 创建大量活动（50+）
- 创建大量记录（1000+）
- 长时间运行计时器（数小时）

## 当前性能状态

✅ **已优化**:
- 移除了无限动画
- 优化了卡片动画
- 使用了合适的动画规格
- 数据库有适当的索引
- 使用了 Flow 进行响应式数据

⚠️ **可以进一步优化**:
- 添加分页加载历史记录
- 使用 remember 缓存计算结果
- 添加性能监控日志
- 优化大列表渲染

## 故障排除

### 如果应用仍然卡顿

1. **检查 Logcat**:
   ```
   adb logcat | grep -E "ANR|GC|Choreographer"
   ```

2. **检查内存泄漏**:
   - 使用 LeakCanary
   - 检查 ViewModel 是否正确清理

3. **检查数据库查询**:
   - 启用 Room 的查询日志
   - 检查是否有慢查询

4. **简化 UI**:
   - 临时移除动画
   - 减少视觉效果
   - 检查是否是特定组件导致的问题

## 结论

通过移除无限动画和优化动画配置，应用的性能应该有显著提升。如果在真实设备上仍然有性能问题，请按照上述建议进一步优化。
