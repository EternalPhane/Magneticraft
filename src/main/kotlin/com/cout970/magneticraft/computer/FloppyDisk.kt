package com.cout970.magneticraft.computer

import com.cout970.magneticraft.MOD_ID
import com.cout970.magneticraft.api.computer.IFloppyDisk
import com.cout970.magneticraft.item.ComputerItems
import com.cout970.magneticraft.util.*
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.FMLCommonHandler
import java.io.File
import java.util.*

class FloppyDisk(val stack: ItemStack) : IFloppyDisk {

    companion object {
        // Avoid loading the same floppy drive twice
        private val cache = mutableMapOf<String, String>()
    }

    override fun getStorageFile(): File {

        if (stack.itemDamage == 0) {
            // user created disks
            val parent = File(FMLCommonHandler.instance().savesDirectory, "./disks")

            if (!parent.exists()) parent.mkdir()

            return File(parent, "floppy_${serialNumber.toHex()}.img")

        } else {
            // disks copied from pre-existent ROMs
            val parent = File(FMLCommonHandler.instance().savesDirectory, "./disks")
            if (!parent.exists()) parent.mkdir()


            val baseName = ComputerItems.defaultDisks
                    .toList()
                    .find { it.second == stack.itemDamage }
                    ?.first
                    ?: "bios"

            val source = "$baseName.bin"

            val bytes = ComputerItems::class.java
                    .getResourceAsStream("/assets/$MOD_ID/cpu/$source")
                    ?.readBytes()
                    ?: ByteArray(0)

            val file: File
            if (stack.getString("label") in cache) {
                file = File(cache[stack.getString("label")])
                file.writeBytes(bytes)

            } else {
                file = createTempFile(directory = parent)
                file.writeBytes(bytes)
                file.deleteOnExit()
                cache[stack.getString("label")] = file.absolutePath

            }
            return file
        }
    }

    override fun getSerialNumber(): Int {
        val nbt = stack.checkNBT()
        if (!nbt.hasKey("serialNumber")) {

            val num = Random()
                    .ints(8)
                    .toArray()
                    .map { "0123456789ABCDEF"[it and 0xF] }
                    .joinToString("")
                    .let { java.lang.Long.decode("0x$it").toInt() }

            nbt.add("serialNumber", num)
        }
        return nbt.getInteger("serialNumber")
    }

    override fun getLabel(): String {
        return stack.getString("label")
    }

    override fun setLabel(str: String) = stack.setString("label", label)

    override fun getSectorCount(): Int = stack.getInteger("sectorCount")

    override fun getAccessTime(): Int = stack.getInteger("accessTime")

    override fun canRead(): Boolean = if (stack.itemDamage == 0) stack.getBoolean("canRead") else true

    override fun canWrite(): Boolean = if (stack.itemDamage == 0) stack.getBoolean("canWrite") else false
}