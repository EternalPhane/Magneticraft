package com.cout970.magneticraft.tileentity.modules.conveyor_belt

enum class Route(val leftSide: Boolean, val isRect: Boolean, val isShort: Boolean) {
    LEFT_FORWARD(true, true, false),
    RIGHT_FORWARD(false, true, false),
    LEFT_SHORT(true, false, true),
    LEFT_LONG(true, false, false),
    RIGHT_SHORT(false, false, true),
    RIGHT_LONG(false, false, false);

}