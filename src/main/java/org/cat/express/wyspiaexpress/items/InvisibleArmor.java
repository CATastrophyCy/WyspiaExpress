package org.cat.express.wyspiaexpress.items;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.cat.express.wyspiaexpress.WyspiaExpress;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

public class InvisibleArmor {
    public static final RegistryEntry<ArmorMaterial> INVISIBLE_MATERIAL = registerArmorMaterial("invisible_material",
            ()-> new ArmorMaterial(Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.HELMET,0 );
                map.put(ArmorItem.Type.CHESTPLATE,0 );
                map.put(ArmorItem.Type.LEGGINGS,0 );
                map.put(ArmorItem.Type.BOOTS,0 );
            }), 10000, SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND, ()-> Ingredient.EMPTY,
                    List.of(new ArmorMaterial.Layer(Identifier.of(WyspiaExpress.MOD_ID, "invisible_material"))),0,0));

    public static RegistryEntry<ArmorMaterial> registerArmorMaterial (String name, Supplier<ArmorMaterial> material){
        return Registry.registerReference(Registries.ARMOR_MATERIAL, Identifier.of(WyspiaExpress.MOD_ID, "invisible_material"), material.get());
    }

    public static final Item INVISIBLE_HELMET = Registry.register(Registries.ITEM, Identifier.of(WyspiaExpress.MOD_ID, "invisible_helmet"),
            new ArmorItem(INVISIBLE_MATERIAL,ArmorItem.Type.HELMET,new Item.Settings()));

    public static final Item INVISIBLE_CHESTPLATE = Registry.register(Registries.ITEM, Identifier.of(WyspiaExpress.MOD_ID, "invisible_chestplate"),
            new ArmorItem(INVISIBLE_MATERIAL,ArmorItem.Type.CHESTPLATE,new Item.Settings()));

    public static final Item INVISIBLE_LEGGINGS = Registry.register(Registries.ITEM, Identifier.of(WyspiaExpress.MOD_ID, "invisible_leggings"),
            new ArmorItem(INVISIBLE_MATERIAL,ArmorItem.Type.LEGGINGS,new Item.Settings()));

    public static final Item INVISIBLE_BOOTS = Registry.register(Registries.ITEM, Identifier.of(WyspiaExpress.MOD_ID, "invisible_boots"),
            new ArmorItem(INVISIBLE_MATERIAL,ArmorItem.Type.BOOTS,new Item.Settings()));

    public static void initialize(){}
}
