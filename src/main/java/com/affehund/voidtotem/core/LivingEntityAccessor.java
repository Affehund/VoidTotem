package com.affehund.voidtotem.core;

public interface LivingEntityAccessor {
    
    boolean isFallDamageImmune();

    void setFallDamageImmune(boolean isImmune);

    long getLastSaveBlockPosAsLong();

    void setLastSaveBlockPosAsLong(long pos);
}
