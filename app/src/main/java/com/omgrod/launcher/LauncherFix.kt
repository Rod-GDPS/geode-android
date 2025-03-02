package com.omgrod.launcher

import androidx.annotation.Keep
import com.omgrod.launcher.utils.Constants

@Keep
object LauncherFix {
    fun loadLibrary() {
        System.loadLibrary(Constants.LAUNCHER_FIX_LIB_NAME)
    }

    external fun setDataPath(dataPath: String)

    external fun setOriginalDataPath(dataPath: String)

    external fun performExceptionsRenaming()

    external fun enableCustomSymbolList(symbolsPath: String)
}