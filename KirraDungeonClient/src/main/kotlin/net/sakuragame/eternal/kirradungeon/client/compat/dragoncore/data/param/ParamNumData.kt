package net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.param

data class ParamNumData(var param1: Int, var param2: Int, var param3: Int, var param4: Int) {

    fun isSame(other: ParamNumData): Boolean {
        return param1 == other.param1 && param2 == other.param2 && param3 == other.param3 && param4 == other.param4
    }

    fun isCategoryChanged(other: ParamNumData): Boolean {
        return param1 != other.param1
    }

    fun isScreenChanged(other: ParamNumData): Boolean {
        return param2 != other.param2
    }
}