package com.luksosilva.dbcomparator.util;

import javafx.scene.Parent;

public class FxLoadResult<T, U> {
    public final T node;
    public final U controller;

    public FxLoadResult(T node, U controller) {
        this.node = node;
        this.controller = controller;
    }
}
