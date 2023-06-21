package com.affehund.voidtotem.core;

import net.minecraft.world.level.dimension.DimensionType;

public interface ILivingEntityMixin {

    boolean isFallDamageImmune();

    void setFallDamageImmune(boolean isImmune);

    long getLastSaveBlockPosAsLong();

    void setLastSaveBlockPosAsLong(long pos);

    DimensionType getLastSaveBlockDim();

    void setLastSaveBlockDim(DimensionType dimensionType);
}

