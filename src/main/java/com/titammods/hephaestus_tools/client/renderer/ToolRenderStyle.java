package com.titammods.hephaestus_tools.client.renderer;

public interface ToolRenderStyle {

    boolean showLayer(int layer);

    boolean showOverlays();

    int tint(int argb);

    boolean translucent();
}