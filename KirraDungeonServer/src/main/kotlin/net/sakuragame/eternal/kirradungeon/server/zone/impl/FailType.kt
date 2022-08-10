package net.sakuragame.eternal.kirradungeon.server.zone.impl

/**
 * 副本失败类型
 *
 * @property OVERTIME 超时
 * @property ALL_DIED 全灭
 * @property CUSTOM 自定义
 */
enum class FailType {
    OVERTIME, ALL_DIED, CUSTOM;
}