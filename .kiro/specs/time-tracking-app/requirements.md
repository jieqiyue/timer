# 需求文档

## 简介

时间记录APP是一个帮助用户追踪和统计日常活动时间的Android应用。用户可以通过点击不同的活动卡片来开始计时，记录完成后可以查看详细的统计数据，包括按年、月、日维度的可视化展示。

## 术语表

- **TimeTrackingApp**: 时间记录Android应用系统
- **AndroidDevice**: 运行Android操作系统的设备
- **ActivityCard**: 活动卡片，代表一个可追踪的活动类型（如练琴、健身等）
- **TimerSession**: 计时会话，记录单次活动的开始、暂停、结束时间
- **ActivityRecord**: 活动记录，一次完整的活动时间记录
- **StatisticsView**: 统计视图，展示活动数据的页面
- **HeatmapView**: 热力图视图，类似GitHub贡献图的可视化展示
- **User**: 使用应用的用户

## 需求

### 需求 1: 活动卡片管理

**用户故事:** 作为用户，我想要创建和管理不同的活动卡片，以便追踪不同类型的活动。

#### 验收标准

1. THE TimeTrackingApp SHALL display all ActivityCards on the home page
2. WHEN User creates a new ActivityCard, THE TimeTrackingApp SHALL save the ActivityCard with a unique identifier and display name
3. WHEN User selects an ActivityCard, THE TimeTrackingApp SHALL allow User to edit the ActivityCard name and color
4. WHEN User requests to delete an ActivityCard, THE TimeTrackingApp SHALL remove the ActivityCard and all associated ActivityRecords after confirmation
5. THE TimeTrackingApp SHALL display each ActivityCard with a distinct visual appearance including custom color and icon
6. THE TimeTrackingApp SHALL calculate and display the total accumulated hours for each ActivityCard based on all associated ActivityRecords
7. WHEN new ActivityRecords are created, THE TimeTrackingApp SHALL update the displayed total hours on the corresponding ActivityCard within 1 second

### 需求 2: 计时功能

**用户故事:** 作为用户，我想要通过点击活动卡片来开始、暂停和结束计时，以便准确记录活动时间。

#### 验收标准

1. WHEN User taps an ActivityCard, THE TimeTrackingApp SHALL start a new TimerSession and display the elapsed time
2. WHILE a TimerSession is active, THE TimeTrackingApp SHALL update the displayed elapsed time every second
3. WHEN User taps the pause button during an active TimerSession, THE TimeTrackingApp SHALL pause the timer and preserve the elapsed time
4. WHEN User taps the resume button on a paused TimerSession, THE TimeTrackingApp SHALL continue timing from the paused elapsed time
5. WHEN User taps the finish button, THE TimeTrackingApp SHALL create an ActivityRecord with the total elapsed time and end the TimerSession

### 需求 3: 活动记录存储

**用户故事:** 作为用户，我想要系统自动保存我的活动记录，以便后续查看和统计。

#### 验收标准

1. WHEN a TimerSession is finished, THE TimeTrackingApp SHALL create an ActivityRecord containing the activity type, start time, end time, and total duration
2. THE TimeTrackingApp SHALL persist all ActivityRecords to local storage
3. WHEN User reopens the application, THE TimeTrackingApp SHALL load all previously saved ActivityRecords
4. THE TimeTrackingApp SHALL associate each ActivityRecord with its corresponding ActivityCard
5. WHEN User deletes an ActivityCard, THE TimeTrackingApp SHALL handle the associated ActivityRecords according to User preference

### 需求 4: 统计页面 - 年度视图

**用户故事:** 作为用户，我想要查看年度统计数据，以便了解全年的活动分布情况。

#### 验收标准

1. WHEN User navigates to StatisticsView and selects year dimension, THE TimeTrackingApp SHALL display a HeatmapView showing 12 months
2. THE TimeTrackingApp SHALL render each day within each month as a small square in the HeatmapView
3. THE TimeTrackingApp SHALL calculate the total duration for each day and apply a color intensity based on the duration
4. WHEN the daily duration is zero, THE TimeTrackingApp SHALL display the square with the lightest color or neutral color
5. WHEN the daily duration increases, THE TimeTrackingApp SHALL display the square with progressively darker colors
6. WHEN User taps a day square, THE TimeTrackingApp SHALL navigate to the day detail view showing all ActivityRecords for that day

### 需求 5: 统计页面 - 月度视图

**用户故事:** 作为用户，我想要查看月度统计数据，以便了解单月的活动详情。

#### 验收标准

1. WHEN User navigates to StatisticsView and selects month dimension, THE TimeTrackingApp SHALL display a calendar view for the selected month
2. THE TimeTrackingApp SHALL display each day with a visual indicator showing the total activity duration
3. THE TimeTrackingApp SHALL calculate and display the total duration for the selected month
4. WHEN User selects a specific ActivityCard filter, THE TimeTrackingApp SHALL display statistics only for that activity type
5. THE TimeTrackingApp SHALL allow User to navigate between different months

### 需求 6: 统计页面 - 日度视图

**用户故事:** 作为用户，我想要查看某一天的详细活动记录列表，以便了解当天的具体活动情况。

#### 验收标准

1. WHEN User navigates to StatisticsView and selects day dimension, THE TimeTrackingApp SHALL display a list of all ActivityRecords for that day
2. THE TimeTrackingApp SHALL display each ActivityRecord with the activity name, start time, end time, and duration
3. THE TimeTrackingApp SHALL sort ActivityRecords by start time in chronological order
4. THE TimeTrackingApp SHALL calculate and display the total duration for all activities on that day
5. WHEN User taps an ActivityRecord, THE TimeTrackingApp SHALL display detailed information including any notes or metadata

### 需求 7: 用户界面设计

**用户故事:** 作为用户，我想要一个美观且易用的界面，以便获得良好的使用体验。

#### 验收标准

1. THE TimeTrackingApp SHALL implement a modern and clean visual design following Material Design guidelines with consistent spacing and typography
2. THE TimeTrackingApp SHALL use smooth animations for transitions between screens and state changes
3. THE TimeTrackingApp SHALL provide clear visual feedback for all User interactions within 100 milliseconds
4. THE TimeTrackingApp SHALL support both light and dark color themes following Android system theme preferences
5. THE TimeTrackingApp SHALL ensure all interactive elements have a minimum touch target size of 48x48 density-independent pixels for accessibility

### 需求 8: 数据过滤和筛选

**用户故事:** 作为用户，我想要能够按活动类型筛选统计数据，以便专注查看特定活动的时间分布。

#### 验收标准

1. WHEN User is in StatisticsView, THE TimeTrackingApp SHALL provide a filter option to select specific ActivityCards
2. WHEN User applies an activity filter, THE TimeTrackingApp SHALL update all visualizations to show only data for the selected activity
3. WHEN User selects "All Activities" filter, THE TimeTrackingApp SHALL display combined data for all activities
4. THE TimeTrackingApp SHALL persist the User's filter selection when navigating between different time dimensions
5. THE TimeTrackingApp SHALL display the currently active filter prominently in the StatisticsView


### 需求 9: Android平台兼容性

**用户故事:** 作为Android用户，我想要应用能够在我的设备上流畅运行，以便正常使用所有功能。

#### 验收标准

1. THE TimeTrackingApp SHALL support AndroidDevices running Android API level 24 (Android 7.0) and above
2. THE TimeTrackingApp SHALL adapt the user interface to different screen sizes and orientations
3. WHEN AndroidDevice screen orientation changes, THE TimeTrackingApp SHALL preserve the current TimerSession state
4. THE TimeTrackingApp SHALL request only necessary permissions from the User
5. WHEN the application is minimized or backgrounded during an active TimerSession, THE TimeTrackingApp SHALL continue timing and maintain accurate elapsed time
