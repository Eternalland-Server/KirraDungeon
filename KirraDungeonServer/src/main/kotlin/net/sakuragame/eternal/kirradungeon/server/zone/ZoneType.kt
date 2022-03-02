package net.sakuragame.eternal.kirradungeon.server.zone

/**
 * 副本类型.
 *
 * @property DEFAULT 默认副本, 击杀所有怪物和 BOSS 则结算胜利.
 * @property SPECIAL 特殊副本, 没有 BOSS. 怪物会复活, 直到玩家死亡淘汰或超时才能被结算.
 * @property UNLIMITED 武神塔, 只有一个 BOSS, 每次击杀都会获得新的礼包然后继续向上.
 *
 */
enum class ZoneType {
    DEFAULT, SPECIAL, UNLIMITED
}