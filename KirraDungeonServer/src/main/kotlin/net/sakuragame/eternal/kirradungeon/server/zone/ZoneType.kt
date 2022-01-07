package net.sakuragame.eternal.kirradungeon.server.zone

/**
 * 副本类型.
 *
 * @property DEFAULT 默认副本.
 * @property MONSTER_PARTY 怪物嘉年华. (每天固定一个时间点开放, 开放多长时间后关闭开放)
 * @property COINS 金币怪怪. (整张地图随机刷新怪物)
 *
 */
enum class ZoneType {
    DEFAULT, MONSTER_PARTY, COINS
}