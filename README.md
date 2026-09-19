# Mira 学习助手

面向小米 Pad 6 Pro 的横屏学习工具，使用 Kotlin 与 Jetpack Compose 原生开发。

## 当前功能

- 作业计时：25/35/45/60 分钟，支持暂停恢复、精确后台通知和最近完成记录。
- 离线字典：内置 3500 个常用汉字，可按汉字、无声调拼音和现代词语查询，可切换同音搜索结果和关联字，支持系统普通话朗读与逐笔笔顺演示。
- 课程表：按工作日查看，可添加、编辑和删除课程，提供时间格式及冲突检查，本地保存。
- 天气：上海、北京、青岛、深圳、成都实时天气，使用 Open-Meteo 免密钥接口。

## 构建

在 Android Studio 中打开本目录并等待 Gradle 同步，或运行：

```powershell
$env:JAVA_HOME='D:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat assembleDebug
```

调试 APK 生成于 `app/build/outputs/apk/debug/app-debug.apk`。

## 离线数据与许可

- 字形、拼音、释义：`mapull/chinese-dictionary`（MIT，原始项目也提示数据仍需人工校核）。
- 现代组词排序：`jelleverheyen/hsk-vocabulary`（MIT）。
- 笔顺矢量路径：`Make Me a Hanzi`（Arphic Public License）。
- 许可原文随 APK 放在 `app/src/main/assets/licenses/`；数据库可通过 `tools/build_dictionary.py` 重建。

## 小米 Pad 6 Pro 实机截图

- `artifacts/screenshots/home.png`
- `artifacts/screenshots/dictionary.png`
- `artifacts/screenshots/timer.png`
- `artifacts/screenshots/schedule.png`
- `artifacts/screenshots/weather.png`
