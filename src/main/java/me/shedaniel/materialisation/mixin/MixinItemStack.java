package me.shedaniel.materialisation.mixin;

import me.shedaniel.materialisation.MaterialisationUtils;
import me.shedaniel.materialisation.items.MaterialisedMiningTool;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {
    
    @Shadow
    public abstract Item getItem();
    
    @Shadow
    public abstract CompoundTag getTag();
    
    /**
     * Disable italic on tools
     */
    @Inject(method = "hasCustomName", at = @At("HEAD"), cancellable = true)
    public void hasDisplayName(CallbackInfoReturnable<Boolean> callbackInfo) {
        if (getItem() instanceof MaterialisedMiningTool)
            callbackInfo.setReturnValue(false);
    }

//    @Inject(method = "getAttributeModifiers", at = @At(value = "INVOKE",
//                                                       target = "Lnet/minecraft/item/Item;getModifiers(Lnet/minecraft/entity/EquipmentSlot;)Lcom/google/common/collect/Multimap;",
//                                                       shift = At.Shift.BEFORE), cancellable = true)
//    public void getAttributeModifiers(EquipmentSlot slot, CallbackInfoReturnable<Multimap<String, EntityAttributeModifier>> callbackInfo) {
//        if (getItem() instanceof MaterialisedMiningTool) {
//            HashMultimap<String, EntityAttributeModifier> multimap = HashMultimap.create();
//            if (slot == EquipmentSlot.MAINHAND) {
//                if (MaterialisationUtils.getToolDurability((ItemStack) (Object) this) > 0)
//                    multimap.put(EntityAttributes.ATTACK_DAMAGE.getId(), new EntityAttributeModifier(MaterialisationUtils.getItemModifierDamage(), "Tool modifier", MaterialisationUtils.getToolAttackDamage((ItemStack) (Object) this), EntityAttributeModifier.Operation.ADDITION));
//                else
//                    multimap.put(EntityAttributes.ATTACK_DAMAGE.getId(), new EntityAttributeModifier(MaterialisationUtils.getItemModifierDamage(), "Tool modifier", -10000, EntityAttributeModifier.Operation.ADDITION));
//            }
//            callbackInfo.setReturnValue(multimap);
//        }
//    }
    
    @Inject(method = "isDamageable", at = @At("HEAD"), cancellable = true)
    public void isDamageable(CallbackInfoReturnable<Boolean> returnable) {
        if (getItem() instanceof MaterialisedMiningTool) {
            CompoundTag compoundTag_1 = getTag();
            returnable.setReturnValue(compoundTag_1 == null || !compoundTag_1.getBoolean("Unbreakable"));
        }
    }
    
    @Inject(method = "getDamage", at = @At("HEAD"), cancellable = true)
    public void getDamage(CallbackInfoReturnable<Integer> returnable) {
        if (getItem() instanceof MaterialisedMiningTool) {
            int maxDurability = MaterialisationUtils.getToolMaxDurability((ItemStack) (Object) this);
            returnable.setReturnValue(maxDurability - MaterialisationUtils.getToolDurability((ItemStack) (Object) this) - 1);
        }
    }
    
    @Inject(method = "getMaxDamage", at = @At("HEAD"), cancellable = true)
    public void getMaxDamage(CallbackInfoReturnable<Integer> returnable) {
        if (getItem() instanceof MaterialisedMiningTool)
            returnable.setReturnValue(MaterialisationUtils.getToolMaxDurability((ItemStack) (Object) this));
    }
    
    @Inject(method = "setDamage", at = @At("HEAD"), cancellable = true)
    public void setDamage(int damage, CallbackInfo info) {
        if (getItem() instanceof MaterialisedMiningTool) {
            int maxDurability = MaterialisationUtils.getToolMaxDurability((ItemStack) (Object) this);
            MaterialisationUtils.setToolDurability((ItemStack) (Object) this, maxDurability - MathHelper.clamp(damage, 0, maxDurability));
            info.cancel();
        }
    }
    
}
