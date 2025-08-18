package net.sploder12.potioncraft.util;

import net.minecraft.util.ActionResult;

public class ActionResultUtils
{
    // silly minecraft making things harder
    public static ActionResult success(boolean client) {
        if (client) return ActionResult.SUCCESS;
        return ActionResult.SUCCESS_SERVER;
    }
}
