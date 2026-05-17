package me.sallos.moreblock.client.renderer;

import me.sallos.moreblock.client.model.ImportedBlockItemModel;
import me.sallos.moreblock.item.ImportedBlockItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class ImportedBlockItemRenderer extends GeoItemRenderer<ImportedBlockItem> {
    public ImportedBlockItemRenderer() {
        super(new ImportedBlockItemModel());
    }
}
