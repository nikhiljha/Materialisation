package me.shedaniel.materialisation.modmenu;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.materialisation.api.PartMaterial;
import me.shedaniel.materialisation.api.PartMaterials;
import me.shedaniel.materialisation.config.ConfigHelper;
import me.shedaniel.materialisation.config.ConfigPack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;

public class MaterialisationMaterialsScreen extends Screen {
    
    Screen parent;
    private Object lastDescription;
    private MaterialisationMaterialListWidget materialList;
    private MaterialisationDescriptionListWidget descriptionList;
    
    protected MaterialisationMaterialsScreen(Screen parent) {
        super(new TranslatableText("config.title.materialisation"));
        this.parent = parent;
    }
    
    public static void overlayBackground(int x1, int y1, int x2, int y2, int red, int green, int blue, int startAlpha, int endAlpha) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        MinecraftClient.getInstance().getTextureManager().bindTexture(DrawableHelper.BACKGROUND_TEXTURE);
        int width = MinecraftClient.getInstance().getWindow().getScaledWidth();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        buffer.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
        buffer.vertex(x1, y2, 0.0D).texture(0, y2 / 32.0F).color(red, green, blue, endAlpha).next();
        buffer.vertex(x2, y2, 0.0D).texture(width / 32.0F, y2 / 32.0F).color(red, green, blue, endAlpha).next();
        buffer.vertex(x2, y1, 0.0D).texture(width / 32.0F, y1 / 32.0F).color(red, green, blue, startAlpha).next();
        buffer.vertex(x1, y1, 0.0D).texture(0, y1 / 32.0F).color(red, green, blue, startAlpha).next();
        tessellator.draw();
    }
    
    @Override
    public void tick() {
        super.tick();
        if (ConfigHelper.loading) {
            MinecraftClient.getInstance().openScreen(new MaterialisationLoadingConfigScreen(this));
        }
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (int_1 == 256 && this.shouldCloseOnEsc()) {
            client.openScreen(parent);
            return true;
        }
        return super.keyPressed(int_1, int_2, int_3);
    }
    
    @Override
    protected void init() {
        super.init();
        addButton(new ButtonWidget(width - 104, 4, 100, 20, new TranslatableText("config.button.materialisation.install"), var1 -> {
            client.openScreen(new MaterialisationInstallScreen(this));
        }));
        addButton(new ButtonWidget(59, 4, 85, 20, new TranslatableText("config.button.materialisation.reload"), var1 -> {
            if (!ConfigHelper.loading) {
                MinecraftClient.getInstance().openScreen(new MaterialisationLoadingConfigScreen(this));
                ConfigHelper.loadConfigAsync();
            }
        }));
        addButton(new ButtonWidget(4, 4, 50, 20, new TranslatableText("gui.back"), var1 -> {
            client.openScreen(parent);
        }));
        children.add(materialList = new MaterialisationMaterialListWidget(client, width / 2 - 10, height, 28 + 5, height - 5, DrawableHelper.BACKGROUND_TEXTURE));
        children.add(descriptionList = new MaterialisationDescriptionListWidget(client, width / 2 - 10, height, 28 + 5, height - 5, DrawableHelper.BACKGROUND_TEXTURE));
        materialList.setLeftPos(5);
        descriptionList.setLeftPos(width / 2 + 5);
        if (lastDescription != null) {
            if (lastDescription instanceof ConfigPack)
                descriptionList.addPack(((ConfigPack) lastDescription).getConfigPackInfo(), (ConfigPack) lastDescription);
            if (lastDescription instanceof PartMaterial)
                descriptionList.addMaterial(this, (PartMaterial) lastDescription);
        }
        ConfigPack defaultPack = PartMaterials.getDefaultPack();
        PartMaterials.getMaterialPacks().forEach(materialsPack -> {
            if (materialsPack == defaultPack)
                return;
            materialList.addItem(new MaterialisationMaterialListWidget.PackEntry(materialsPack.getConfigPackInfo()) {
                @Override
                public void onClick() {
                    lastDescription = materialsPack;
                    descriptionList.addPack(materialsPack.getConfigPackInfo(), materialsPack);
                }
            });
            materialsPack.getKnownMaterials().forEach(partMaterial -> {
                materialList.addItem(new MaterialisationMaterialListWidget.MaterialEntry(partMaterial) {
                    @Override
                    public void onClick() {
                        lastDescription = partMaterial;
                        descriptionList.addMaterial(MaterialisationMaterialsScreen.this, partMaterial);
                    }
                });
            });
        });
        if (defaultPack.getKnownMaterials().count() > 0) {
            materialList.addItem(new MaterialisationMaterialListWidget.PackEntry(defaultPack.getConfigPackInfo()) {
                @Override
                public void onClick() {
                    lastDescription = defaultPack;
                    descriptionList.addPack(defaultPack.getConfigPackInfo(), defaultPack);
                }
            });
            defaultPack.getKnownMaterials().forEach(partMaterial -> {
                materialList.addItem(new MaterialisationMaterialListWidget.MaterialEntry(partMaterial) {
                    @Override
                    public void onClick() {
                        lastDescription = partMaterial;
                        descriptionList.addMaterial(MaterialisationMaterialsScreen.this, partMaterial);
                    }
                });
            });
        }
    }
    
    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float delta) {
        renderBackgroundTexture(0);
        materialList.render(stack, mouseX, mouseY, delta);
        descriptionList.render(stack, mouseX, mouseY, delta);
        overlayBackground(0, 0, width, 28, 64, 64, 64, 255, 255);
        overlayBackground(0, height - 5, width, height, 64, 64, 64, 255, 255);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE);
        RenderSystem.disableAlphaTest();
        RenderSystem.shadeModel(7425);
        RenderSystem.disableTexture();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
        buffer.vertex(0, 28 + 4, 0.0D).texture(0.0F, 1.0F).color(0, 0, 0, 0).next();
        buffer.vertex(this.width, 28 + 4, 0.0D).texture(1.0F, 1.0F).color(0, 0, 0, 0).next();
        buffer.vertex(this.width, 28, 0.0D).texture(1.0F, 0.0F).color(0, 0, 0, 255).next();
        buffer.vertex(0, 28, 0.0D).texture(0.0F, 0.0F).color(0, 0, 0, 255).next();
        tessellator.draw();
        RenderSystem.enableTexture();
        RenderSystem.shadeModel(7424);
        RenderSystem.enableAlphaTest();
        RenderSystem.disableBlend();
        drawCenteredText(stack, textRenderer, title, width / 2, 10, 16777215);
        super.render(stack, mouseX, mouseY, delta);
    }
    
}
