package org.nv95.openmanga.providers.staff;

import android.support.annotation.NonNull;

import org.nv95.openmanga.providers.MangaProvider;

/**
 * Created by nv95 on 23.07.16.
 */
public class ProviderSummary {
    public String name;
    @NonNull
    public Class<? extends MangaProvider> aClass;
    public int lang;

    public ProviderSummary(String name, @NonNull Class<? extends MangaProvider> aClass, int lang) {
        this.name = name;
        this.aClass = aClass;
        this.lang = lang;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProviderSummary that = (ProviderSummary) o;

        return aClass.equals(that.aClass);

    }

    @Override
    public int hashCode() {
        return aClass.hashCode();
    }
}
