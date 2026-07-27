package com.github.epsilon.i18n;

/**
 * Core interface for translation components.
 * Implementation is provided by the common layer.
 */
public interface ITranslateComponent {
    String getName();

    String getTranslation(String key);

    ITranslateComponent createChild(String name);
}
