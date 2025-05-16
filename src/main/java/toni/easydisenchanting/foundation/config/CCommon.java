package toni.easydisenchanting.foundation.config;

import toni.lib.config.ConfigBase;

public class CCommon extends ConfigBase {
    public final ConfigInt fixed_value = i(1000, "Fixed Experience Cost", "Here you can set a fixed value for any enchantment transfer from item to book. Default is 1000 which disables the fixed value.");
    public final ConfigInt return_value = i(1, "Should Return", "Should the anvil give you back the item you disenchanted? Change this value to 0 to block the original item from being returned. Default is 1.");
    public final ConfigFloat factor_value = f(1.0f, 0f, 1000f, "Experience Cost Factor", "Here you can set a factor to multiply any enchantment transfer XP cost by. This will be overwritten if a fixed value other than 1000 is defined. Default is 1.0.");

    @Override
    public String getName() {
        return "common";
    }
}
