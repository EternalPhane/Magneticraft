package com.cout970.magneticraft.block.core

import com.cout970.magneticraft.Magneticraft
import com.cout970.magneticraft.misc.block.get
import com.cout970.magneticraft.misc.player.sendMessage
import com.cout970.magneticraft.misc.tileentity.getTile
import com.cout970.magneticraft.misc.world.isServer
import com.cout970.magneticraft.tileentity.core.TileBase
import com.cout970.magneticraft.tileentity.modules.ModuleElectricity
import com.cout970.magneticraft.util.vector.*
import net.minecraft.block.Block
import net.minecraft.block.properties.IProperty
import net.minecraft.block.properties.PropertyEnum
import net.minecraft.block.state.IBlockState
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.IStringSerializable
import net.minecraft.util.math.AxisAlignedBB
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilityProvider

/**
 * Created by cout970 on 2017/06/30.
 */
object CommonMethods {

    fun pickDefaultBlock(args: PickBlockArgs): ItemStack {
        return ItemStack(args.default.item, 1, 0)
    }

    fun enableAutoConnectWires(args: OnActivatedArgs): Boolean {
        if (args.playerIn.isSneaking && args.playerIn.heldItemMainhand.isEmpty) {
            val te = args.worldIn.getTile<TileBase>(args.pos) ?: return false
            val electricModule = te.container.modules.find { it is ModuleElectricity } as? ModuleElectricity ?: return false

            electricModule.autoConnectWires = !electricModule.autoConnectWires
            if (!electricModule.autoConnectWires) {
                electricModule.clearWireConnections()
            }
            if (args.worldIn.isServer) {
                if (electricModule.autoConnectWires) {
                    args.playerIn.sendMessage("text.magneticraft.auto_connect.activate")
                } else {
                    args.playerIn.sendMessage("text.magneticraft.auto_connect.deactivate")
                }
            }
            return true
        }
        return false
    }

    fun placeWithFacing(it: OnBlockPlacedArgs): IBlockState {
        return it.defaultValue.withProperty(PROPERTY_FACING, Facing.of(it.facing))
    }

    fun placeWithOrientation(it: OnBlockPlacedArgs): IBlockState {
        val placer = it.placer ?: return it.defaultValue
        return it.defaultValue.withProperty(PROPERTY_ORIENTATION, Orientation.of(placer.horizontalFacing))
    }

    fun delegateToModule(args: OnActivatedArgs): Boolean {
        val tile = args.worldIn.getTile<TileBase>(args.pos) ?: return false
        val method = tile.container.modules.find { it is IOnActivated } as? IOnActivated ?: return false
        return method.onActivated(args)
    }

    fun openGui(args: OnActivatedArgs): Boolean {
        return if (args.worldIn.isServer && !args.playerIn.isSneaking) {
            args.playerIn.openGui(Magneticraft, -1, args.worldIn, args.pos.xi, args.pos.yi, args.pos.zi)
            true
        } else false
    }

    fun <T> providerFor(cap: Capability<T>?, handler: T): ICapabilityProvider? {
        return object : ICapabilityProvider {

            @Suppress("UNCHECKED_CAST")
            override fun <T : Any?> getCapability(capability: Capability<T>, facing: EnumFacing?): T? = handler as? T

            override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean = capability == cap
        }
    }

    /**
     * The base value is associated to the NORTH direction
     */
    fun updateBoundingBoxWithFacing(base: AxisAlignedBB): (BoundingBoxArgs) -> AxisAlignedBB {
        return { (state) ->
            val facing = state[PROPERTY_FACING]?.facing ?: EnumFacing.DOWN
            val center = vec3Of(0.5)
            facing.rotateBox(center, base)
        }
    }

    // Common properties
    val PROPERTY_FACING = PropertyEnum.create("facing", Facing::class.java)!!
    val PROPERTY_ORIENTATION = PropertyEnum.create("orientation", Orientation::class.java)!!

    enum class Facing(override val stateName: String,
                      val facing: EnumFacing,
                      override val isVisible: Boolean) : IStatesEnum, IStringSerializable {

        DOWN("down", EnumFacing.DOWN, true),
        UP("up", EnumFacing.UP, false),
        NORTH("north", EnumFacing.NORTH, false),
        SOUTH("south", EnumFacing.SOUTH, false),
        EAST("east", EnumFacing.EAST, false),
        WEST("west", EnumFacing.WEST, false);

        override fun getName() = name.toLowerCase()
        override val properties: List<IProperty<*>> get() = listOf(PROPERTY_FACING)

        override fun getBlockState(block: Block): IBlockState {
            return block.defaultState.withProperty(PROPERTY_FACING, this)
        }

        companion object {
            fun of(facing: EnumFacing): Facing = when (facing) {
                EnumFacing.DOWN -> DOWN
                EnumFacing.UP -> UP
                EnumFacing.NORTH -> NORTH
                EnumFacing.SOUTH -> SOUTH
                EnumFacing.WEST -> WEST
                EnumFacing.EAST -> EAST
            }
        }
    }

    enum class Orientation(override val stateName: String,
                           override val isVisible: Boolean,
                           val facing: EnumFacing) : IStatesEnum, IStringSerializable {

        NORTH("north", true, EnumFacing.NORTH),
        SOUTH("south", false, EnumFacing.SOUTH),
        EAST("east", false, EnumFacing.EAST),
        WEST("west", false, EnumFacing.WEST);

        override fun getName() = name.toLowerCase()
        override val properties: List<IProperty<*>> get() = listOf(PROPERTY_ORIENTATION)

        override fun getBlockState(block: Block): IBlockState {
            return block.defaultState.withProperty(PROPERTY_ORIENTATION, this)
        }

        companion object {
            fun of(facing: EnumFacing): Orientation = when (facing) {
                EnumFacing.NORTH -> Orientation.NORTH
                EnumFacing.SOUTH -> Orientation.SOUTH
                EnumFacing.WEST -> Orientation.WEST
                EnumFacing.EAST -> Orientation.EAST
                else -> Orientation.NORTH
            }
        }
    }
}