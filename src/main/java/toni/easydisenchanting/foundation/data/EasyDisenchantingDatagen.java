package toni.easydisenchanting.foundation.data;

#if FABRIC
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import toni.easydisenchanting.EasyDisenchanting;

public class EasyDisenchantingDatagen  implements DataGeneratorEntrypoint {

    @Override
    public String getEffectiveModId() {
        return EasyDisenchanting.ID;
    }

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        var pack = fabricDataGenerator.createPack();
        pack.addProvider(ConfigLangDatagen::new);
    }
}
#endif