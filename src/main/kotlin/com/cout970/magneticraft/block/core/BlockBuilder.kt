package com.cout970.magneticraft.block.core

import com.cout970.magneticraft.AABB
import com.cout970.magneticraft.util.resource
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.ICapabilityProvider


/**
 * Created by cout970 on 2017/06/08.
 */
class BlockBuilder {

    var constructor: (Material, List<IStatesEnum>) -> BlockBase = { a, b ->
        BlockBase.states_ = b
        BlockBase(a)
    }
    var tileConstructor: (Material, List<IStatesEnum>, (World, IBlockState) -> TileEntity?) -> BlockBase = { a, b, c ->
        BlockBase.states_ = b
        BlockTileBase(c, a)
    }
    var customModels: List<Pair<String, ResourceLocation>> = emptyList()

    var factory: ((World, IBlockState) -> TileEntity?)? = null
    var registryName: ResourceLocation? = null
    var material: Material? = null
    var creativeTab: CreativeTabs? = null
    var boundingBox: ((BoundingBoxArgs) -> AABB)? = null
    var onActivated: ((OnActivatedArgs) -> Boolean)? = null
    var stateMapper: ((IBlockState) -> ModelResourceLocation)? = null
    var onBlockPlaced: ((OnBlockPlacedArgs) -> IBlockState)? = null
    var pickBlock: ((PickBlockArgs) -> ItemStack)? = null
    var canPlaceBlockOnSide: ((CanPlaceBlockOnSideArgs) -> Boolean)? = null
    var capabilityProvider: ICapabilityProvider? = null
    var onNeighborChanged: ((OnNeighborChangedArgs) -> Unit)? = null
    var states: List<IStatesEnum>? = null
    var hardness = 1.5f
    var explosionResistance = 10.0f
    var overrideItemModel = true
    var enableOcclusionOptimization = true
    var translucent = false
    var alwaysDropDefault = false

    var hasCustomModel: Boolean = false
        set(value) {
            field = value
            enableOcclusionOptimization = false
            translucent = true
        }

    fun withName(name: String): BlockBuilder {
        registryName = resource(name)
        return this
    }

    fun build(): BlockBase {
        requireNotNull(registryName) { "registryName was null" }
        requireNotNull(material) { "material was null" }

        val block = if (factory != null)
            tileConstructor(material!!, states ?: listOf(IStatesEnum.default), factory!!)
        else
            constructor(material!!, states ?: listOf(IStatesEnum.default))

        block.apply {
            registryName = this@BlockBuilder.registryName!!
            creativeTab?.let { setCreativeTab(it) }
            boundingBox?.let { aabb = it }
            setHardness(hardness)
            setResistance(explosionResistance)
            unlocalizedName = "${registryName?.resourceDomain}.${registryName?.resourcePath}"
            onActivated = this@BlockBuilder.onActivated
            stateMapper = this@BlockBuilder.stateMapper
            enableOcclusionOptimization = this@BlockBuilder.enableOcclusionOptimization
            translucent_ = this@BlockBuilder.translucent
            setLightOpacity(if (translucent_) 0 else 255)
            onBlockPlaced = this@BlockBuilder.onBlockPlaced
            customModels = this@BlockBuilder.customModels
            pickBlock = this@BlockBuilder.pickBlock
            overrideItemModel = this@BlockBuilder.overrideItemModel
            canPlaceBlockOnSide = this@BlockBuilder.canPlaceBlockOnSide
            capabilityProvider = this@BlockBuilder.capabilityProvider
            onNeighborChanged = this@BlockBuilder.onNeighborChanged
            alwaysDropDefault = this@BlockBuilder.alwaysDropDefault
        }
        return block
    }

    fun factoryOf(func: () -> TileEntity): ((World, IBlockState) -> TileEntity?) = { _, _ -> func() }

    fun copy(func: BlockBuilder.() -> Unit): BlockBuilder {
        val newBuilder = BlockBuilder()

        newBuilder.constructor = constructor
        newBuilder.tileConstructor = tileConstructor
        newBuilder.customModels = customModels
        newBuilder.factory = factory
        newBuilder.registryName = registryName
        newBuilder.material = material
        newBuilder.creativeTab = creativeTab
        newBuilder.boundingBox = boundingBox
        newBuilder.onActivated = onActivated
        newBuilder.stateMapper = stateMapper
        newBuilder.onBlockPlaced = onBlockPlaced
        newBuilder.pickBlock = pickBlock
        newBuilder.states = states
        newBuilder.hardness = hardness
        newBuilder.explosionResistance = explosionResistance
        newBuilder.enableOcclusionOptimization = enableOcclusionOptimization
        newBuilder.translucent = translucent
        newBuilder.overrideItemModel = overrideItemModel
        newBuilder.canPlaceBlockOnSide = canPlaceBlockOnSide
        newBuilder.capabilityProvider = capabilityProvider
        newBuilder.onNeighborChanged = onNeighborChanged
        newBuilder.alwaysDropDefault = alwaysDropDefault

        func(newBuilder)
        return newBuilder
    }
}

