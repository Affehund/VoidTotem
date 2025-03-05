package com.affehund.voidtotem.core;

import net.minecraft.world.level.dimension.DimensionType;

public interface ILivingEntityMixin {

    boolean voidtotem$isFallDamageImmune();

    void voidtotem$setFallDamageImmune(boolean isImmune);

    long voidtotem$getLastSaveBlockPosAsLong();

    void voidtotem$setLastSaveBlockPosAsLong(long pos);

    DimensionType voidtotem$getLastSaveBlockDim();

    void voidtotem$setLastSaveBlockDim(DimensionType dimensionType);
}
