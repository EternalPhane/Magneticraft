package com.cout970.magneticraft.gui.client.components.bars

import com.cout970.magneticraft.api.energy.IElectricNode
import com.cout970.magneticraft.api.heat.IHeatNode
import com.cout970.magneticraft.misc.gui.formatHeat
import com.cout970.magneticraft.util.clamp
import com.cout970.magneticraft.util.vector.Vec2d
import net.minecraftforge.energy.IEnergyStorage

/**
 * Created by cout970 on 09/07/2016.
 */
class CompElectricBar(val node: IElectricNode, pos: Vec2d)
    : CompVerticalBar(ElectricBarProvider(node), 0, pos, tooltip = { listOf(String.format("%.2fV", node.voltage)) }) {

    class ElectricBarProvider(val node: IElectricNode) : IBarProvider {
        override fun getLevel(): Float = clamp(node.voltage / 120.0, 1.0, 0.0).toFloat()
    }
}

class CompRfBar(val node: IEnergyStorage, pos: Vec2d)
    : CompVerticalBar(RfBarProvider(node), 7, pos, tooltip = { listOf("${node.energyStored}RF") }) {

    class RfBarProvider(val node: IEnergyStorage) : IBarProvider {
        override fun getLevel(): Float = node.energyStored / node.maxEnergyStored.toFloat()
    }
}

class CompHeatBar(val node: IHeatNode, pos: Vec2d)
    : CompVerticalBar(HeatBarProvider(node), 2, pos, tooltip = { listOf(formatHeat(node.temperature)) }) {
    //CallbackBarProvider(this::getTemperature, ::WATER_BOILING_POINT, ::STANDARD_AMBIENT_TEMPERATURE)

    class HeatBarProvider(val node: IHeatNode) : IBarProvider {
        override fun getLevel(): Float = node.temperature.toFloat() / 4000f
    }
}


