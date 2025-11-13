package org.sploder.potioncraft.common.meta.templates;

public interface TemplateResolver {

    <T extends MetaEffectTemplate> void register(T template);
    <T extends MetaEffectTemplate> void unregister(T template);

    void clear();

    MetaEffectTemplate get(String identifier);
}

