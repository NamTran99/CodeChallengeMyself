package umagic.ai.aiart.retrofit

import android.util.Log

class TokenUtils private constructor() {
    companion object {
        private var isLoadingSuccess: Boolean = false
        private var tokenUtils: TokenUtils? = null

        val tokenUtilsInstance: TokenUtils?
            get() = tokenUtils

        fun initLib() {
            try {
                System.loadLibrary("tk")
                setLoadingSuccess(true)
            } catch (e: Throwable) {
                setLoadingSuccess(false)
                Log.e("initLib", "Load Lib Fail ${e.message}")
            }
            tokenUtils = TokenUtils()
        }

        fun isLoadingSuccess(): Boolean {
            return isLoadingSuccess
        }

        private fun setLoadingSuccess(success: Boolean) {
            isLoadingSuccess = success
        }

        private fun setTokenUtils(utils: TokenUtils) {
            tokenUtils = utils
        }
    }

    external fun paramsToken(str: String): String
}