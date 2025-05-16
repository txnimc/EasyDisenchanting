package toni.easydisenchanting;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import toni.easydisenchanting.foundation.config.AllConfigs;

#if fabric
import toni.easydisenchanting.foundation.AnvilFabricEvents.AnvilUpdateEvent;
import toni.easydisenchanting.foundation.AnvilFabricEvents.AnvilRepairEvent;
#elif forge
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
#elif neo
import net.neoforged.neoforge.event.entity.player.AnvilRepairEvent;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
#endif

#if mc >= 211
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.enchantment.ItemEnchantments;
#endif

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

#if forgelike @EventBusSubscriber(modid = EasyDisenchanting.ID) #endif
public class AnvilModifier {

    #if forgelike @SubscribeEvent #endif
    public static void onRepair(AnvilRepairEvent event) {
        var left = event.getLeft();
        var right = event.getRight();

        if (right.getItem() == Items.BOOK) {
            checkGiveItems(event, left, right);
        }
        else if (left.getItem() == Items.BOOK) {
            checkGiveItems(event, right, left);
        }
        else if (left.getItem() == Items.ENCHANTED_BOOK) {
            checkGiveItems(event, right, left);
        }
    }

    private static void checkGiveItems(AnvilRepairEvent event, ItemStack left, ItemStack right) {
        if (!left.isEnchanted() || left.getItem() == Items.ENCHANTED_BOOK)
            return;

        if (AllConfigs.common().return_value.get() == 0)
            return;

        ItemStack disenchanted = left.copy();
        setEnchantments(#if mc >= 211 ItemEnchantments.EMPTY #else new HashMap<>() #endif, disenchanted);
        if (AllConfigs.common().fixed_value.get() == 0) {
            event.getEntity().giveExperienceLevels(1);
        }

        if (!event.getEntity().getInventory().add(disenchanted)) {
            event.getEntity().drop(disenchanted, true);
        }
    }


    #if forgelike @SubscribeEvent #endif
    public static void onUpdate(AnvilUpdateEvent event) {
        var left = event.getLeft();
        var right = event.getRight();

        checkUpdate(event, left, right);
        checkUpdate(event, right, left);

        checkMerge(event, right, left);
    }

    private static void checkUpdate(AnvilUpdateEvent event, ItemStack left, ItemStack right) {
        if (!left.isEnchanted() || left.getItem() == Items.ENCHANTED_BOOK || right.getItem() != Items.BOOK || right.getCount() != 1)
            return;

        ItemStack finalBook = new ItemStack(Items.ENCHANTED_BOOK);

        var toRemove = getEnchantments(left);
        setEnchantments(toRemove, finalBook);
        event.setOutput(finalBook);

        event.setCost(getCost(left, right, toRemove));
    }

    private static void checkMerge(AnvilUpdateEvent event, ItemStack left, ItemStack right) {
        if (!left.isEnchanted() || right.getItem() != Items.ENCHANTED_BOOK || right.getCount() != 1)
            return;

        ItemStack finalBook = new ItemStack(Items.ENCHANTED_BOOK);
        var toRemove = getEnchantments(left);
        var toMerge = getEnchantments(right);

        #if mc >= 211
        var mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);

        HashSet<Holder<Enchantment>> allEnchants = new HashSet<>();
        allEnchants.addAll(toRemove.keySet());
        allEnchants.addAll(toMerge.keySet());

        for (var key : allEnchants) {
            var a = toRemove.getLevel(key);
            var b = toMerge.getLevel(key);

            var combinedLevel = Math.min(key.value().getMaxLevel(), a == b ? a + 1 : Math.max(a, b));
            mutable.set(key, combinedLevel);
        }

        var enchantments = mutable.toImmutable();
        #else
        var enchantments = new HashMap<Enchantment,Integer>();

        HashSet<Enchantment> allEnchants = new HashSet<>();
        allEnchants.addAll(toRemove.keySet());
        allEnchants.addAll(toMerge.keySet());

        for (var key : allEnchants) {
            var a = toRemove.getOrDefault(key, 0);
            var b = toMerge.getOrDefault(key, 0);

            var combinedLevel = Math.min(key.getMaxLevel(), a == b ? a + 1 : Math.max(a, b));
            enchantments.put(key, combinedLevel);
        }
        #endif

        setEnchantments(enchantments, finalBook);
        event.setOutput(finalBook);

        event.setCost(getCost(left, right, enchantments));
    }


    private static int getCost(ItemStack left, ItemStack right, #if mc >= 211 ItemEnchantments #else Map<Enchantment, Integer> #endif enchantsToRemove) {
        int costFactor = 0;
        ItemStack rightCopy = right.copy();
        setEnchantments(enchantsToRemove, rightCopy);
        for (var enchant : enchantsToRemove.keySet()) {
            if (enchant != null) {
                #if mc >= 211
                int enchantValue = Math.min(enchant.value().getMaxLevel(), enchantsToRemove.getLevel(enchant));
                int rarity = enchant.value().getAnvilCost();
                #else
                int enchantValue = Math.min(enchant.getMaxLevel(), enchantsToRemove.get(enchant));
                int rarity = switch (enchant.getRarity()) {
                    case COMMON -> 1;
                    case UNCOMMON -> 2;
                    case RARE -> 4;
                    case VERY_RARE -> 8;
                };
                #endif

                rarity = Math.max(1, rarity / 2);
                costFactor += rarity * enchantValue;
            }
        }

        var fixedValue = AllConfigs.common().fixed_value.get();
        double factor_value = AllConfigs.common().factor_value.get();
        return fixedValue != 1000
            ? Math.max(1, fixedValue)
            : factor_value > 0.0 ? (int) Math.round(costFactor * factor_value) : 1;
    }

    private static #if mc >= 211 ItemEnchantments #else Map<Enchantment, Integer> #endif getEnchantments(ItemStack stack) {
        #if mc >= 211
        if (stack.getItem() == Items.ENCHANTED_BOOK)
            return stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        else
            return stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        #else
        return EnchantmentHelper.getEnchantments(stack);
        #endif
    }

    private static void setEnchantments(#if mc >= 211 ItemEnchantments #else Map<Enchantment, Integer> #endif enchantments, ItemStack stack) {
        #if mc >= 211
        if (stack.getItem() == Items.ENCHANTED_BOOK)
            stack.set(DataComponents.STORED_ENCHANTMENTS, enchantments);
        else
            stack.set(DataComponents.ENCHANTMENTS, enchantments);
        #else
        EnchantmentHelper.setEnchantments(enchantments, stack);
        #endif
    }
}