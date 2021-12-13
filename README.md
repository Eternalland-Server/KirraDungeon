# KirraDungeon

> 维护者: @kirraObj (咸蛋)

## 简介

简单的副本插件, 基于 `Kotlin & TabooLib 6` 进行开发.

跟 `DungeonSystem` 挂钩.

## 项目结构

- KirraDungeonClient
    - 实现副本远程操作的模块, 对接 DungeonSystem-Client.
- KirraDungeonPlot
    - 实现玩家初次进入剧情的模块, 对接 DungeonSystem-Client.
- KirraDungeonServer
    - 实现副本服务器实例的模块, 对接 DungeonSystem-Server.
