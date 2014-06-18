package com.joysee.tvbox.settings.upgrade.entity;

import java.util.ArrayList;
import java.util.List;

public class Patchs {
    public static final String START_TAG = "Patchs";
    private List<Patch> patchs;

    public Patchs() {
        patchs = new ArrayList<Patch>();
    }

    public void addPatchToList(Patch ver) {
        if (ver != null) {
            patchs.add(ver);
        }
    }

    public List<Patch> getPatchs() {
        return patchs;
    }

}
