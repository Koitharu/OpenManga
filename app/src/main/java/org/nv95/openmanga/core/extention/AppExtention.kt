package org.nv95.openmanga.core.extention

import android.content.Context
import androidx.annotation.StringRes
import org.koin.core.context.GlobalContext

/**
 * Application
 */
val app: Context by lazy { GlobalContext.get().koin.get<Context>() }

/**
 * Возращает строку из ресурсов
 * @param text id идентификатор в ресурсах
 */
fun getString(@StringRes text: Int) = app.getString(text)

/**
 * Возращает строку из ресурсов
 * @param text id идентификатор в ресурсах
 */
fun getString(@StringRes text: Int, vararg values: String) = app.getString(text, *values)